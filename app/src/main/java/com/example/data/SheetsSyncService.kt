package com.example.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object SheetsSyncService {
    private val client = OkHttpClient()

    // Supported date formats
    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
        SimpleDateFormat("d/M/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    )

    fun extractSpreadsheetId(url: String): String? {
        val regex = "/spreadsheets/d/([a-zA-Z0-9-_]+)".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groups?.get(1)?.value ?: if (url.isNotBlank() && !url.contains("/")) url.trim() else null
    }

    suspend fun fetchAndParseSheet(sheetUrl: String): List<SheetVaccineRow> = withContext(Dispatchers.IO) {
        val spreadsheetId = extractSpreadsheetId(sheetUrl)
            ?: throw IllegalArgumentException("Không thể nhận diện Spreadsheet ID từ địa chỉ được cung cấp. Hãy chắc chắn bạn copy đúng link Google Sheet.")

        val exportUrl = "https://docs.google.com/spreadsheets/d/$spreadsheetId/export?format=csv"
        Log.d("SheetsSyncService", "Fetching CSV from: $exportUrl")

        val request = Request.Builder()
            .url(exportUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Không thể kết nối đến Google Sheet. Vui lòng kiểm tra lại quyền chia sẻ (bất kỳ ai có liên kết đều có thể xem). Mã lỗi: ${response.code}")
            }

            val bodyString = response.body?.string() ?: throw Exception("Nội dung Google Sheet trống rỗng.")
            return@withContext parseCsv(bodyString)
        }
    }

    private fun parseCsv(csvContent: String): List<SheetVaccineRow> {
        val rows = csvContent.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (rows.isEmpty()) return emptyList()

        // Parse header row to find matching columns
        val headers = parseCsvLine(rows[0]).map { it.lowercase() }
        Log.d("SheetsSyncService", "CSV Headers detected: $headers")

        var childIndex = -1
        var vaccineIndex = -1
        var doseIndex = -1
        var diseaseIndex = -1
        var scheduleDateIndex = -1
        var actualDateIndex = -1
        var isCompletedIndex = -1
        var notesIndex = -1
        var locationIndex = -1

        for (i in headers.indices) {
            val title = headers[i]
            when {
                title.contains("bé") || title.contains("con") || title.contains("trẻ") || title.contains("child") || title.contains("name") -> childIndex = i
                title.contains("vắc") || title.contains("vac") || title.contains("mũi tiêm") || title.contains("thuốc") -> vaccineIndex = i
                title.contains("mũi số") || title.contains("mũi") || title.contains("dose") || title.contains("lần") -> doseIndex = i
                title.contains("bệnh") || title.contains("phòng b") || title.contains("disease") -> diseaseIndex = i
                title.contains("dự kiến") || title.contains("ngày hẹn") || title.contains("ngày dự kiến") || title.contains("scheduled") || title.contains("plan") -> scheduleDateIndex = i
                title.contains("thực tế") || title.contains("ngày tiêm") || title.contains("tiêm ngày") || title.contains("actual") || title.contains("admin") -> actualDateIndex = i
                title.contains("tiêm chưa") || title.contains("đã tiêm") || title.contains("hoàn thành") || title.contains("completed") || title.contains("status") || title.contains("trạng thái") -> isCompletedIndex = i
                title.contains("ghi chú") || title.contains("note") || title.contains("mô tả") -> notesIndex = i
                title.contains("địa điểm") || title.contains("nơi") || title.contains("location") || title.contains("trung tâm") -> locationIndex = i
            }
        }

        // We require at least vaccine name map
        if (vaccineIndex == -1) {
            throw IllegalArgumentException("Không tìm thấy cột Tên Vắc Xin. Vui lòng đảm bảo Sheet của bạn chứa dòng tiêu đề có cột 'Bé', 'Vắc xin', 'Mũi tiêm', 'Ngày dự kiến', v.v.")
        }

        val results = mutableListOf<SheetVaccineRow>()

        for (rowIdx in 1 until rows.size) {
            val columns = parseCsvLine(rows[rowIdx])
            if (columns.size <= vaccineIndex) continue

            val childName = if (childIndex != -1 && childIndex < columns.size) columns[childIndex] else ""
            val vaccineName = columns[vaccineIndex]
            if (vaccineName.isBlank()) continue

            val doseString = if (doseIndex != -1 && doseIndex < columns.size) columns[doseIndex] else "1"
            val doseNumber = doseString.filter { it.isDigit() }.toIntOrNull() ?: 1

            val disease = if (diseaseIndex != -1 && diseaseIndex < columns.size) columns[diseaseIndex] else ""
            val scheduleString = if (scheduleDateIndex != -1 && scheduleDateIndex < columns.size) columns[scheduleDateIndex] else ""
            val actualString = if (actualDateIndex != -1 && actualDateIndex < columns.size) columns[actualDateIndex] else ""
            val completedString = if (isCompletedIndex != -1 && isCompletedIndex < columns.size) columns[isCompletedIndex] else ""
            val notes = if (notesIndex != -1 && notesIndex < columns.size) columns[notesIndex] else ""
            val location = if (locationIndex != -1 && locationIndex < columns.size) columns[locationIndex] else ""

            val scheduledDate = parseDate(scheduleString) ?: System.currentTimeMillis()
            val actualDate = parseDate(actualString)

            val isCompleted = when {
                completedString.lowercase().contains("đã") -> true
                completedString.lowercase().contains("tiêm") -> true
                completedString.lowercase().contains("y") -> true
                completedString.lowercase().contains("yes") -> true
                completedString.lowercase().contains("đúng") -> true
                completedString.lowercase() == "1" -> true
                actualDate != null -> true
                else -> false
            }

            results.add(
                SheetVaccineRow(
                    childName = childName,
                    vaccineName = vaccineName,
                    diseasePrevented = disease,
                    doseNumber = doseNumber,
                    scheduledDate = scheduledDate,
                    actualDate = actualDate,
                    isCompleted = isCompleted,
                    location = location,
                    notes = notes
                )
            )
        }

        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        var current = StringBuilder()
        for (i in line.indices) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim().removeSurrounding("\""))
                current = StringBuilder()
            } else {
                current.append(c)
            }
        }
        result.add(current.toString().trim().removeSurrounding("\""))
        return result
    }

    private fun parseDate(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        val clean = dateStr.trim().replace("\"", "")
        for (format in dateFormats) {
            try {
                // Ensure lenient parsing is disabled to be strict
                format.isLenient = false
                return format.parse(clean)?.time
            } catch (e: Exception) {
                // Ignore and try next
            }
        }
        // Fallback: try parsing as milliseconds directly
        return clean.toLongOrNull()
    }
}

data class SheetVaccineRow(
    val childName: String,
    val vaccineName: String,
    val diseasePrevented: String,
    val doseNumber: Int,
    val scheduledDate: Long,
    val actualDate: Long?,
    val isCompleted: Boolean,
    val location: String,
    val notes: String
)
