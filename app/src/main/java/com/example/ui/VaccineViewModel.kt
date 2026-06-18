package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val count: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

class VaccineViewModel(private val repository: VaccineRepository) : ViewModel() {

    // Children representation
    val children: StateFlow<List<ChildEntity>> = repository.allChildren
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected child (null if no child has been created yet)
    private val _selectedChild = MutableStateFlow<ChildEntity?>(null)
    val selectedChild: StateFlow<ChildEntity?> = _selectedChild.asStateFlow()

    // Active Child's vaccine records
    val activeChildRecords: StateFlow<List<VaccineRecordEntity>> = _selectedChild
        .flatMapLatest { child ->
            if (child != null) {
                repository.getRecordsForChild(child.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Upcoming reminders across all kids
    val upcomingVaccines: StateFlow<List<VaccineRecordEntity>> = repository.allUpcomingVaccines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sheet Sync URL text
    private val _sheetUrl = MutableStateFlow("")
    val sheetUrl: StateFlow<String> = _sheetUrl.asStateFlow()

    // Google Sheet Sync State
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // UI Tab selection (0 = Lịch tiêm, 1 = Thống kê, 2 = Nhắc hẹn, 3 = Quản lý bé / Đồng bộ Sheet)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    init {
        // Automatically select the first child when list changes and none is selected
        viewModelScope.launch {
            children.collect { list ->
                if (_selectedChild.value == null && list.isNotEmpty()) {
                    _selectedChild.value = list.first()
                }
            }
        }
    }

    fun selectChild(child: ChildEntity) {
        _selectedChild.value = child
    }

    fun updateActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
        if (tabIndex != 3) {
            clearSyncState()
        }
    }

    fun setSheetUrl(url: String) {
        _sheetUrl.value = url
    }

    fun clearSyncState() {
        _syncState.value = SyncState.Idle
    }

    // Add baby
    fun addChild(name: String, dateOfBirth: Long, gender: String, notes: String) {
        viewModelScope.launch {
            val child = ChildEntity(
                name = name,
                dateOfBirth = dateOfBirth,
                gender = gender,
                notes = notes
            )
            val newId = repository.insertChild(child)
            // Select the newly added child
            val updatedChild = child.copy(id = newId.toInt())
            _selectedChild.value = updatedChild
        }
    }

    // Delete baby
    fun deleteChild(child: ChildEntity) {
        viewModelScope.launch {
            repository.deleteChild(child)
            // Reset selection
            if (_selectedChild.value?.id == child.id) {
                _selectedChild.value = children.value.firstOrNull { it.id != child.id }
            }
        }
    }

    // Toggle vaccine dose
    fun toggleVaccineCompletion(record: VaccineRecordEntity, isCompleted: Boolean, actualDate: Long? = null) {
        viewModelScope.launch {
            val updated = record.copy(
                isCompleted = isCompleted,
                actualDate = if (isCompleted) (actualDate ?: System.currentTimeMillis()) else null
            )
            repository.updateVaccineRecord(updated)
        }
    }

    // Update full dose detail (including center location details)
    fun saveVaccineDetails(
        record: VaccineRecordEntity,
        isCompleted: Boolean,
        actualDate: Long?,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            val updated = record.copy(
                isCompleted = isCompleted,
                actualDate = if (isCompleted) (actualDate ?: System.currentTimeMillis()) else null,
                location = location,
                notes = notes
            )
            repository.updateVaccineRecord(updated)
        }
    }

    // Sync from Google Sheet Link
    fun syncWithGoogleSheet() {
        val child = _selectedChild.value
        val url = _sheetUrl.value
        if (child == null) {
            _syncState.value = SyncState.Error("Không có hồ sơ bé nào đang hoạt động. Hãy thêm bé trước!")
            return
        }
        if (url.isBlank()) {
            _syncState.value = SyncState.Error("Vui lòng nhập đường liên kết Google Sheet.")
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            try {
                val recordCount = repository.syncWithGoogleSheet(child.id, url)
                _syncState.value = SyncState.Success(recordCount)
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.localizedMessage ?: "Lỗi không xác định khi đồng bộ.")
            }
        }
    }
}

class VaccineViewModelFactory(private val repository: VaccineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaccineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VaccineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
