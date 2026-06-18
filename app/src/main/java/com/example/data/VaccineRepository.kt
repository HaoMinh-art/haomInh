package com.example.data

import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class VaccineRepository(private val vaccineDao: VaccineDao, private val notificationHelper: NotificationHelper?) {

    val allChildren: Flow<List<ChildEntity>> = vaccineDao.getAllChildrenFlow()

    val allUpcomingVaccines: Flow<List<VaccineRecordEntity>> = vaccineDao.getAllUpcomingVaccinesFlow()

    fun getRecordsForChild(childId: Int): Flow<List<VaccineRecordEntity>> =
        vaccineDao.getVaccineRecordsForChildFlow(childId)

    suspend fun getChildById(childId: Int): ChildEntity? =
        vaccineDao.getChildById(childId)

    suspend fun insertChild(child: ChildEntity): Long {
        val childId = vaccineDao.insertChild(child)
        // Auto populate standard vaccination schedule
        populateDefaultSchedule(childId.toInt(), child.dateOfBirth)
        return childId
    }

    suspend fun deleteChild(child: ChildEntity) {
        vaccineDao.clearVaccineRecordsForChild(child.id)
        vaccineDao.deleteChild(child)
    }

    suspend fun insertVaccineRecord(record: VaccineRecordEntity) {
        vaccineDao.insertVaccineRecord(record)
        scheduleSystemNotification(record)
    }

    suspend fun updateVaccineRecord(record: VaccineRecordEntity) {
        vaccineDao.updateVaccineRecord(record)
        if (!record.isCompleted) {
            scheduleSystemNotification(record)
        } else {
            cancelSystemNotification(record)
        }
    }

    suspend fun deleteVaccineRecord(record: VaccineRecordEntity) {
        vaccineDao.deleteVaccineRecord(record)
        cancelSystemNotification(record)
    }

    private fun scheduleSystemNotification(record: VaccineRecordEntity) {
        notificationHelper?.scheduleVaccineReminder(record)
    }

    private fun cancelSystemNotification(record: VaccineRecordEntity) {
        notificationHelper?.cancelVaccineReminder(record)
    }

    // Helper to add months to dob
    private fun calculateScheduledDate(dob: Long, recommendedMonths: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dob
        cal.add(Calendar.MONTH, recommendedMonths)
        return cal.timeInMillis
    }

    private suspend fun populateDefaultSchedule(childId: Int, dob: Long) {
        val list = mutableListOf<VaccineRecordEntity>()

        // 1. Sơ sinh (0 tháng)
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Lao (BCG)",
                diseasePrevented = "Bệnh Lao",
                recommendedAgeInMonths = 0,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 0)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Viêm gan B (Sơ sinh)",
                diseasePrevented = "Viêm gan B",
                recommendedAgeInMonths = 0,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 0)
            )
        )

        // 2. 2 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "6 trong 1 (Bạch hầu, ho gà, uốn ván, bại liệt, Hib, viêm gan B) - Mũi 1",
                diseasePrevented = "Bạch hầu, Ho gà, Uốn ván, Bại liệt, Hib, Viêm gan B",
                recommendedAgeInMonths = 2,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 2)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Phế cầu (Synflorix / Prevenar 13) - Mũi 1",
                diseasePrevented = "Viêm màng não, viêm phổi, viêm tai giữa do phế cầu",
                recommendedAgeInMonths = 2,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 2)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Uống ngừa tiêu chảy cấp Rota - Lần 1",
                diseasePrevented = "Viêm dạ dày, ruột do virus Rota",
                recommendedAgeInMonths = 2,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 2)
            )
        )

        // 3. 3 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "6 trong 1 (Mũi 2)",
                diseasePrevented = "Bạch hầu, Ho gà, Uốn ván, Bại liệt, Hib, Viêm gan B",
                recommendedAgeInMonths = 3,
                doseNumber = 2,
                scheduledDate = calculateScheduledDate(dob, 3)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Uống ngừa tiêu chảy cấp Rota - Lần 2",
                diseasePrevented = "Viêm dạ dày, ruột do virus Rota",
                recommendedAgeInMonths = 3,
                doseNumber = 2,
                scheduledDate = calculateScheduledDate(dob, 3)
            )
        )

        // 4. 4 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "6 trong 1 (Mũi 3)",
                diseasePrevented = "Bạch hầu, Ho gà, Uốn ván, Bại liệt, Hib, Viêm gan B",
                recommendedAgeInMonths = 4,
                doseNumber = 3,
                scheduledDate = calculateScheduledDate(dob, 4)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Phế cầu - Mũi 2",
                diseasePrevented = "Viêm màng não, viêm phổi do phế cầu",
                recommendedAgeInMonths = 4,
                doseNumber = 2,
                scheduledDate = calculateScheduledDate(dob, 4)
            )
        )

        // 5. 6 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Não mô cầu B/C - Mũi 1",
                diseasePrevented = "Viêm màng não, nhiễm khuẩn huyết do não mô cầu khuẩn",
                recommendedAgeInMonths = 6,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 6)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Cúm mùa (Vaxigrip Tetra) - Mũi 1",
                diseasePrevented = "Cúm",
                recommendedAgeInMonths = 6,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 6)
            )
        )

        // 6. 7 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Cúm mùa (Vaxigrip Tetra) - Mũi 2",
                diseasePrevented = "Cúm",
                recommendedAgeInMonths = 7,
                doseNumber = 2,
                scheduledDate = calculateScheduledDate(dob, 7)
            )
        )

        // 7. 9 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Sởi đơn (MVVac) hoặc Sởi - Quai bị - Rubella",
                diseasePrevented = "Sởi (hoặc cả Sởi, Quai bị, Rubella sớm)",
                recommendedAgeInMonths = 9,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 9)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Viêm não Nhật Bản Jevax - Mũi 1",
                diseasePrevented = "Viêm não Nhật Bản",
                recommendedAgeInMonths = 9,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 9)
            )
        )

        // 8. 12 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Sởi - Quai bị - Rubella (MMR-II) - Mũi 1",
                diseasePrevented = "Sởi, Quai bị, Rubella",
                recommendedAgeInMonths = 12,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 12)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Thủy đậu (Varilrix / Varivax) - Mũi 1",
                diseasePrevented = "Thủy đậu",
                recommendedAgeInMonths = 12,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 12)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Viêm gan A - Mũi 1",
                diseasePrevented = "Viêm gan siêu vi A",
                recommendedAgeInMonths = 12,
                doseNumber = 1,
                scheduledDate = calculateScheduledDate(dob, 12)
            )
        )

        // 9. 18 tháng tuổi
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Bạch hầu, ho gà, uốn ván, bại liệt, Hib - Mũi nhắc (6 trong 1)",
                diseasePrevented = "Bạch hấu, Ho gà, Uốn ván, Bại liệt, Hib",
                recommendedAgeInMonths = 18,
                doseNumber = 4,
                scheduledDate = calculateScheduledDate(dob, 18)
            )
        )
        list.add(
            VaccineRecordEntity(
                childId = childId,
                vaccineName = "Sởi - Quai bị - Rubella - Mũi nhắc (MMR-II)",
                diseasePrevented = "Sởi, Quai bị, Rubella",
                recommendedAgeInMonths = 18,
                doseNumber = 2,
                scheduledDate = calculateScheduledDate(dob, 18)
            )
        )

        // Bulk insert to Room
        vaccineDao.insertVaccineRecords(list)

        // Schedule notification reminders for all upcoming defaults
        list.forEach { scheduleSystemNotification(it) }
    }

    suspend fun syncWithGoogleSheet(childId: Int, sheetUrl: String): Int {
        try {
            val rows = SheetsSyncService.fetchAndParseSheet(sheetUrl)
            if (rows.isEmpty()) return 0

            // Clear old vaccination data for this child
            vaccineDao.clearVaccineRecordsForChild(childId)

            val childRecords = rows.map { row ->
                VaccineRecordEntity(
                    childId = childId,
                    vaccineName = row.vaccineName,
                    diseasePrevented = row.diseasePrevented.ifBlank { "Lây lan từ Google Sheet" },
                    recommendedAgeInMonths = 0, // Custom Sheet-defined
                    doseNumber = row.doseNumber,
                    scheduledDate = row.scheduledDate,
                    actualDate = row.actualDate,
                    isCompleted = row.isCompleted,
                    location = row.location,
                    notes = row.notes.ifBlank { "Đồng bộ từ Google Sheet" }
                )
            }

            vaccineDao.insertVaccineRecords(childRecords)

            // Reschedule reminders
            childRecords.forEach {
                if (!it.isCompleted) {
                    scheduleSystemNotification(it)
                }
            }

            return childRecords.size
        } catch (e: Exception) {
            throw e
        }
    }
}
