package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ChildEntity
import com.example.data.VaccineRecordEntity
import com.example.notification.NotificationHelper
import com.example.ui.SyncState
import com.example.ui.VaccineViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleekHeader(
    child: ChildEntity,
    children: List<ChildEntity>,
    selectedChild: ChildEntity?,
    showChildOptionMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onSelectChild: (ChildEntity) -> Unit,
    onAddChildClick: () -> Unit,
    notificationHelper: NotificationHelper,
    modifier: Modifier = Modifier
) {
    val initials = remember(child.name) {
        val parts = child.name.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val p1 = parts[parts.size - 2].firstOrNull()?.uppercase() ?: ""
            val p2 = parts[parts.size - 1].firstOrNull()?.uppercase() ?: ""
            p1 + p2
        } else if (parts.isNotEmpty()) {
            parts.first().take(2).uppercase()
        } else {
            "👶"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F9FC))
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Sleek Child Profile with Avatar Badge and Dropdown Switcher
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onShowMenuChange(true) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant circle initials avatar block (48x48)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFDBEAFE), shape = CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .shadow(1.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB)
                )
            }

            // Info text block
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = child.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch baby child",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${calculateAgeText(child.dateOfBirth)} • ${child.gender}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            // Anchored Dropdown Menu
            Box {
                DropdownMenu(
                    expanded = showChildOptionMenu,
                    onDismissRequest = { onShowMenuChange(false) }
                ) {
                    children.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFDBEAFE), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (c.gender == "Nam") Icons.Default.Face else Icons.Default.FaceRetouchingNatural,
                                        contentDescription = "Child",
                                        tint = if (c.id == child.id) Color(0xFF2563EB) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            onClick = {
                                onSelectChild(c)
                                onShowMenuChange(false)
                            }
                        )
                    }
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Thêm bé mới", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color(0xFF2563EB)
                            )
                        },
                        onClick = {
                            onShowMenuChange(false)
                            onAddChildClick()
                        }
                    )
                }
            }
        }

        // Right side: Active Notification Bell (HTML design style)
        IconButton(
            onClick = {
                notificationHelper.triggerImmediateNotification(
                    "Theo dõi vắc-xin tự động",
                    "Đã kiểm tra lịch trình tiêm vắc-xin của ${child.name}. Dữ liệu đồng nhất và sẵn sàng offline."
                )
            },
            modifier = Modifier
                .size(38.dp)
                .background(Color.White, shape = CircleShape)
                .border(1.dp, Color(0xFFE2E8F0), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification Bell Tracker",
                tint = Color(0xFF475569),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VaccineViewModel,
    notificationHelper: NotificationHelper,
    modifier: Modifier = Modifier
) {
    val children by viewModel.children.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()
    val records by viewModel.activeChildRecords.collectAsStateWithLifecycle()
    val upcomingRecords by viewModel.upcomingVaccines.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val sheetUrl by viewModel.sheetUrl.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    var showAddChildDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<VaccineRecordEntity?>(null) }
    var showChildOptionMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.updateActiveTab(0) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Lịch tiêm") },
                    label = { Text("Lịch Tiêm", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.updateActiveTab(1) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Thống kê") },
                    label = { Text("Thống Kê", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.updateActiveTab(2) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Nhắc lịch") },
                    label = { Text("Nhắc Lịch", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { viewModel.updateActiveTab(3) },
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = "Quản lý / Sync") },
                    label = { Text("Đồng Bộ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0 && selectedChild != null) {
                FloatingActionButton(
                    onClick = { showAddChildDialog = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_child")
                ) {
                    Icon(Icons.Default.ChildCare, contentDescription = "Thêm em bé")
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = Color(0xFFF7F9FC) // Sleek cool-grey background
        ) {
            when {
                children.isEmpty() -> {
                    EmptyStateScreen(
                        onAddBabyClick = { showAddChildDialog = true }
                    )
                }
                selectedChild == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Render the fully dynamic sleek child profile header at the very top of pages!
                        SleekHeader(
                            child = selectedChild!!,
                            children = children,
                            selectedChild = selectedChild,
                            showChildOptionMenu = showChildOptionMenu,
                            onShowMenuChange = { showChildOptionMenu = it },
                            onSelectChild = { viewModel.selectChild(it) },
                            onAddChildClick = { showAddChildDialog = true },
                            notificationHelper = notificationHelper
                        )

                        // Rest of screen content takes the remaining height
                        Box(modifier = Modifier.weight(1f)) {
                            Crossfade(
                                targetState = activeTab,
                                label = "tabSwitch"
                            ) { tab ->
                                when (tab) {
                                    0 -> ScheduleTab(
                                        child = selectedChild!!,
                                        records = records,
                                        onRecordClick = { editingRecord = it }
                                    )
                                    1 -> StatsTab(
                                        child = selectedChild!!,
                                        records = records
                                    )
                                    2 -> RemindersTab(
                                        upcomingRecords = upcomingRecords,
                                        notificationHelper = notificationHelper
                                    )
                                    3 -> SyncTab(
                                        child = selectedChild!!,
                                        childrenList = children,
                                        viewModel = viewModel,
                                        syncState = syncState,
                                        sheetUrl = sheetUrl,
                                        onSelectChild = { viewModel.selectChild(it) },
                                        onDeleteChild = { viewModel.deleteChild(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to add a child (or registering first child)
    if (showAddChildDialog) {
        AddChildDialog(
            onDismiss = { showAddChildDialog = false },
            onConfirm = { name, dob, gender, notes ->
                viewModel.addChild(name, dob, gender, notes)
                showAddChildDialog = false
            }
        )
    }

    // Modal to edit/save vaccination record details
    if (editingRecord != null) {
        EditRecordDialog(
            record = editingRecord!!,
            onDismiss = { editingRecord = null },
            onConfirm = { isCompleted, actualDate, location, notes ->
                viewModel.saveVaccineDetails(editingRecord!!, isCompleted, actualDate, location, notes)
                editingRecord = null
            },
            onToggleStatusShort = { isCompleted ->
                viewModel.toggleVaccineCompletion(editingRecord!!, isCompleted)
                editingRecord = null
            }
        )
    }
}

// FORMATTING DATE HELPER
private fun formatEpochDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

// DYNAMIC NATURAL AGE CALCULATOR HELPER
private fun calculateAgeText(dobMillis: Long): String {
    val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
    val now = Calendar.getInstance()
    var years = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
    var days = now.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months--
        val prevMonth = now.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years--
        months += 12
    }

    val sb = StringBuilder()
    if (years > 0) sb.append("$years tuổi ")
    if (months > 0) sb.append("$months thg ")
    if (days > 0 || sb.isEmpty()) sb.append("$days ngày")
    return sb.toString().trim()
}

// --- TAB 0: VACCINATION SCHEDULE ---
// --- TAB 0: VACCINATION SCHEDULE ---
@Composable
fun ScheduleTab(
    child: ChildEntity,
    records: List<VaccineRecordEntity>,
    onRecordClick: (VaccineRecordEntity) -> Unit
) {
    val completedCount = remember(records) { records.count { it.isCompleted } }
    val totalCount = remember(records) { records.size }
    val overdueCount = remember(records) { records.count { !it.isCompleted && it.scheduledDate < System.currentTimeMillis() } }

    val nextAppointment = remember(records) { records.firstOrNull { !it.isCompleted } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Next Appointment Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB) // Sleek rich blue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), clip = false),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Abstract vector background graphics (transparent circles)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                            .size(110.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NHẮC HẸN SẮP TỚI",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (nextAppointment != null) {
                            Text(
                                text = nextAppointment.vaccineName,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phòng bệnh: ${nextAppointment.diseasePrevented} • Mũi ${nextAppointment.doseNumber}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = formatEpochDate(nextAppointment.scheduledDate),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Time",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "08:30 AM",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Hoàn thành toàn bộ lộ trình",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tất cả vắc-xin đã tiêm chủng đầy đủ! Chúc bé yêu phát triển khỏe mạnh.",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats Bento Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completed Bento Card (Light Emerald Green design style)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = BorderStroke(1.dp, Color(0xFFD1FAE5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "HOÀN THÀNH",
                            color = Color(0xFF047857),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d/%02d", completedCount, totalCount),
                            color = Color(0xFF064E3B),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mũi tiêm đã hoàn thành",
                            color = Color(0xFF059669),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Warnings Bento Card (Light Orange theme)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "CẢNH BÁO",
                            color = Color(0xFFC2410C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d", overdueCount),
                            color = Color(0xFF7C2D12),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (overdueCount > 0) "Mũi trễ lịch hẹn" else "Mũi trễ hẹn phát hiện",
                            color = Color(0xFFEA580C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Section header details
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "LỊCH TRÌNH CHI TIẾT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "XEM TẤT CẢ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        if (records.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Vaccines,
                                contentDescription = "Empty Schedule",
                                modifier = Modifier.size(56.dp),
                                tint = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Lịch tiêm trống. Hãy sync từ Google Sheet\nhoặc thiết lập để khởi tạo lịch trình vắc-xin.",
                                textAlign = TextAlign.Center,
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(records) { record ->
                VaccineItemRow(record = record, onClick = { onRecordClick(record) })
            }
        }
    }
}

@Composable
fun VaccineItemRow(
    record: VaccineRecordEntity,
    onClick: () -> Unit
) {
    val isPastDue = !record.isCompleted && record.scheduledDate < System.currentTimeMillis()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // matching slate borders
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp), clip = false)
            .testTag("vaccine_item_card_${record.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block category: recommended age in months formatted nicely
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", record.recommendedAgeInMonths),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info Text Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.vaccineName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                 val statusTextDetails = if (record.isCompleted) {
                    val dateStr = formatEpochDate(record.actualDate ?: System.currentTimeMillis())
                    if (record.location.isNotBlank()) "$dateStr • ${record.location}" 
                    else "$dateStr • Đã tiêm"
                } else {
                    "${formatEpochDate(record.scheduledDate)} • Chờ tiêm"
                }
                
                Text(
                    text = statusTextDetails,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "Bệnh phòng: ${record.diseasePrevented} (Mũi ${record.doseNumber})",
                    fontSize = 11.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Right Status Check Box Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = when {
                            record.isCompleted -> Color(0xFFD1FAE5) // light emerald
                            isPastDue -> Color(0xFFFEE2E2) // light red
                            else -> Color(0xFFF1F5F9) // light neutral
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        record.isCompleted -> Icons.Default.Check
                        isPastDue -> Icons.Default.PriorityHigh
                        else -> Icons.Default.HourglassEmpty
                    },
                    contentDescription = "Status indicator",
                    tint = when {
                        record.isCompleted -> Color(0xFF059669)
                        isPastDue -> Color(0xFFEF4444)
                        else -> Color(0xFF94A3B8)
                    },
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// --- TAB 1: BEAUTIFUL STATISTICS ---
@Composable
fun StatsTab(
    child: ChildEntity,
    records: List<VaccineRecordEntity>
) {
    val total = records.size
    val completed = records.count { it.isCompleted }
    val pending = total - completed
    val percentFloat = if (total > 0) completed.toFloat() / total else 0f
    val percentText = (percentFloat * 100).toInt()

    val overdue = records.count { !it.isCompleted && it.scheduledDate < System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tiến độ tiêm chủng của ${child.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        // Radial Circular Progress Chart
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val progressColor = MaterialTheme.colorScheme.primary
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = percentFloat * 360f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentText%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Đã hoàn thành",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats breakdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$completed", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Đã Tiêm", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$overdue", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(text = "Trễ Hẹn", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$pending", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "Chờ Tiêm", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Checklist Status Panel
        Text(
            text = "Trạng Thái Chi Tiết",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        // Custom Bar graph for age breakdown
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tỷ lệ bao phủ vaccine theo Khuyến Nghị",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )

                // Age group 0-6m
                val age0_6 = records.filter { it.recommendedAgeInMonths in 0..6 }
                ProgressRow(label = "Nhóm Sơ sinh - 6 tháng tuổi", list = age0_6)

                // Age group 7-12m
                val age7_12 = records.filter { it.recommendedAgeInMonths in 7..12 }
                ProgressRow(label = "Nhóm 7 tháng - 12 tháng tuổi", list = age7_12)

                // Age group 13m+
                val age13Plus = records.filter { it.recommendedAgeInMonths > 12 }
                ProgressRow(label = "Nhóm Trên 1 tuổi (13-24 tháng)", list = age13Plus)
            }
        }
    }
}

@Composable
fun ProgressRow(label: String, list: List<VaccineRecordEntity>) {
    val total = list.size
    val done = list.count { it.isCompleted }
    val fraction = if (total > 0) done.toFloat() / total else 0f
    val pct = (fraction * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "$done/$total ($pct%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = fraction,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// --- TAB 2: REMINDERS & TEST ALERTS ---
@Composable
fun RemindersTab(
    upcomingRecords: List<VaccineRecordEntity>,
    notificationHelper: NotificationHelper
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lịch Trình Hẹn Tiêm Sắp Tới",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Diagnostic test push trigger
            Button(
                onClick = {
                    notificationHelper.triggerImmediateNotification(
                        "Kiểm tra nhắc hẹn Sổ Tiêm Chủng",
                        "Tính năng tự động nhắc lịch hoạt động hoàn toàn ổn định! Bạn sẽ nhận thông báo khi có lịch tiêm."
                    )
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("trigger_test_notif")
            ) {
                Icon(Icons.Default.BugReport, contentDescription = "Test", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Thông Báo", fontSize = 10.sp)
            }
        }

        // Informational card about automatic notification alerts
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Alert Tip",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hệ thống sẽ tự động quét và lên lịch nhắc nhở vào lúc 8:00 Sáng vào đúng ngày tiêm chủng quy định. Hãy cấp quyền đẩy thông báo nếu hệ thống yêu cầu.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (upcomingRecords.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = "Done",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tuyệt vời! Hiện tại không có lịch hẹn nào\nđang trễ hạn hoặc chưa hoàn thành.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Take first 15 records for upcoming view
                items(upcomingRecords.take(15)) { record ->
                    ElevatedCard(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (record.scheduledDate < System.currentTimeMillis()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = record.vaccineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Phòng bệnh: ${record.diseasePrevented}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatEpochDate(record.scheduledDate),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (record.scheduledDate < System.currentTimeMillis()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(text = "Mũi số ${record.doseNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: MANAGE CHILDS & SYNC GOOGLE SHEETS ---
@Composable
fun SyncTab(
    child: ChildEntity,
    childrenList: List<ChildEntity>,
    viewModel: VaccineViewModel,
    syncState: SyncState,
    sheetUrl: String,
    onSelectChild: (ChildEntity) -> Unit,
    onDeleteChild: (ChildEntity) -> Unit
) {
    var editSheetUrl by remember { mutableStateOf(sheetUrl) }
    // Initialize text field with cached state
    LaunchedEffect(sheetUrl) {
        editSheetUrl = sheetUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Đồng Bộ Với Google Sheets",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Hướng dẫn chuẩn bị Google Sheets:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "1. Google Sheet phải có các tiêu đề cột sau ở dòng đầu tiên (tiếng Việt hoặc Anh):\n    • 'Bé' hoặc 'Child Name'\n    • 'Vắc xin' hoặc 'Vaccine Name' (Mũi tiêm)\n    • 'Mũi số' hoặc 'Dose Number'\n    • 'Bệnh ngừa' hoặc 'Disease'\n    • 'Ngày dự kiến' hoặc 'Scheduled Date' (định dạng dd/MM/yyyy hoặc YYYY-MM-DD)\n    • 'Đã tiêm' hoặc 'Completed' (Có / Đúng / Đã tiêm)\n    • 'Ngày tiêm' hoặc 'Actual Date'\n    • 'Địa điểm' hoặc 'Location'\n    • 'Ghi chú' (Notes)\n\n2. Chia sẻ bảng tính Google Sheet ở chế độ 'Bất kỳ ai có liên kết đều có thể xem' (Anyone with link can view).\n\n3. Copy link dán vào ô bên dưới rồi bấm Đồng Bộ.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input Link
        OutlinedTextField(
            value = editSheetUrl,
            onValueChange = {
                editSheetUrl = it
                viewModel.setSheetUrl(it)
            },
            label = { Text("Đường dẫn liên kết Google Sheet") },
            placeholder = { Text("https://docs.google.com/spreadsheets/d/.../edit") },
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_sheet_field_url"),
            leadingIcon = { Icon(Icons.Default.InsertDriveFile, contentDescription = "Drive Icon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        // Action Sync Button
        Button(
            onClick = { viewModel.syncWithGoogleSheet() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sync_sheet_button")
        ) {
            Icon(Icons.Default.Sync, contentDescription = "Sync Now")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đồng Bộ Cho Bé: ${child.name}", fontWeight = FontWeight.Bold)
        }

        // Parse Results Alert UI
        when (syncState) {
            is SyncState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Đang kết nối Drive và đồng bộ dữ liệu...", fontSize = 12.sp)
                    }
                }
            }
            is SyncState.Success -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Sync complete", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Đồng bộ thành công! Đã nạp ${syncState.count} bản ghi tiêm chủng cho bé ${child.name}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            is SyncState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = "Sync error", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = syncState.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            is SyncState.Idle -> { /* do nothing */ }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION MANAGING KIDS ---
        Text(
            text = "Danh Sách Hồ Sơ Em Bé (${childrenList.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            childrenList.forEach { baby ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (baby.id == child.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (baby.id == child.id) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { onSelectChild(baby) }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (baby.gender == "Nam") Color(0xFFCBE5FF) else Color(0xFFFFD4E5),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (baby.gender == "Nam") Icons.Default.Male else Icons.Default.Female,
                                    contentDescription = "Gender",
                                    tint = if (baby.gender == "Nam") Color(0xFF00569E) else Color(0xFF9E003D)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = baby.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Sinh: ${formatEpochDate(baby.dateOfBirth)} (${calculateAgeText(baby.dateOfBirth)})", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        IconButton(
                            onClick = { onDeleteChild(baby) },
                            modifier = Modifier.testTag("delete_baby_btn_${baby.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa bé", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// --- POPUP: ADD CHILD DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, dob: Long, gender: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") } // Default boys

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var dobMillis by remember { mutableStateOf(calendar.timeInMillis) }
    var formatDobString by remember { mutableStateOf(formatEpochDate(dobMillis)) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sel = Calendar.getInstance()
            sel.set(Calendar.YEAR, year)
            sel.set(Calendar.MONTH, month)
            sel.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dobMillis = sel.timeInMillis
            formatDobString = formatEpochDate(dobMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Đăng Ký Hồ Sơ Bé",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên của bé") },
                    placeholder = { Text("Nhập ví dụ: Bé Bo") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().testTag("baby_name_input")
                )

                // Date of Birth selection trigger button
                Column {
                    Text(text = "Ngày sinh của bé", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth().testTag("baby_dob_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = formatDobString, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày")
                        }
                    }
                }

                // Gender Segmented Selection
                Column {
                    Text(text = "Giới tính", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { gender = "Nam" },
                            modifier = Modifier.weight(1f).testTag("gender_boy"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gender == "Nam") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (gender == "Nam") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Nam (Boy)")
                        }
                        Button(
                            onClick = { gender = "Nữ" },
                            modifier = Modifier.weight(1f).testTag("gender_girl"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gender == "Nữ") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (gender == "Nữ") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Nữ (Girl)")
                        }
                    }
                }

                // Description Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú đặc điểm (nếu có)") },
                    placeholder = { Text("Ví dụ: Nhóm máu O, dị ứng bơ...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("baby_notes_input")
                )

                // Bottom Panel Action triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy bỏ")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, dobMillis, gender, notes)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1.5f).testTag("submit_child_profile_btn")
                    ) {
                        Text("Lưu Hồ Sơ")
                    }
                }
            }
        }
    }
}

// --- POPUP: EDIT VACCINE RECORD DETAIL ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRecordDialog(
    record: VaccineRecordEntity,
    onDismiss: () -> Unit,
    onConfirm: (isCompleted: Boolean, actualDate: Long?, location: String, notes: String) -> Unit,
    onToggleStatusShort: (isCompleted: Boolean) -> Unit
) {
    var completedState by remember { mutableStateOf(record.isCompleted) }
    var location by remember { mutableStateOf(record.location) }
    var notes by remember { mutableStateOf(record.notes) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var actualDateMillis by remember { mutableStateOf(record.actualDate ?: calendar.timeInMillis) }
    var actualDateString by remember { mutableStateOf(formatEpochDate(actualDateMillis)) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sel = Calendar.getInstance()
            sel.set(Calendar.YEAR, year)
            sel.set(Calendar.MONTH, month)
            sel.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            actualDateMillis = sel.timeInMillis
            actualDateString = formatEpochDate(actualDateMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Cập nhật mũi tiêm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = record.vaccineName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Bệnh phòng ngừa: ${record.diseasePrevented}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Divider()

                // State Toggle Row trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Đã tiêm chủng mũi này?", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = completedState,
                        onCheckedChange = { completedState = it },
                        modifier = Modifier.testTag("vaccine_completed_switch")
                    )
                }

                // If completed is active, display target actual date selection and location attributes
                if (completedState) {
                    // Actual Date picker
                    Column {
                        Text(text = "Ngày tiêm thực tế", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth().testTag("pick_actual_date_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = actualDateString, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày")
                            }
                        }
                    }

                    // Location / Center input
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Địa điểm tiêm (VD: VNVC, Trạm Y Tế)") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("vaccine_location_field")
                    )
                }

                // Clinic reaction / Symptom feedback Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú (phản ứng sốt, lô vaccine...)") },
                    placeholder = { Text("Ví dụ: Bé sốt nhẹ 38 độ, uống hạ sốt đỡ.") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("vaccine_notes_field")
                )

                // Save buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy bỏ")
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                completedState,
                                if (completedState) actualDateMillis else null,
                                location,
                                notes
                            )
                        },
                        modifier = Modifier.weight(1.5f).testTag("save_dose_details_btn")
                    ) {
                        Text("Lưu Lịch")
                    }
                }
            }
        }
    }
}

// --- SCREEN LAYOUTS: FIRST-LAUNCH EMPTY STATE ---
@Composable
fun EmptyStateScreen(
    onAddBabyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High fidelity generated child protective hero image banner
        Image(
            painter = painterResource(id = R.drawable.img_baby_protect),
            contentDescription = "Bảo vệ bé yêu tiêm chủng",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sổ Tiêm Chủng Cho Bé",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chào mừng ba mẹ đến với ứng dụng quản lý tiêm chủng thông minh. Tự động nhắc nhở lịch tiêm đúng hẹn, lưu trữ lịch sử phản ứng sau tiêm, đồng bộ trực tiếp với Google Sheets Drive nhanh gọn.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onAddBabyClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_add_baby_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tạo Hồ Sơ Bé Ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
