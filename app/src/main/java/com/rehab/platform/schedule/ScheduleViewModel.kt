package com.rehab.platform.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehab.platform.data.model.Schedule
import com.rehab.platform.repository.RehabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val selectedDate: Date = Date(),
    val error: String? = null
)

class ScheduleViewModel(private val repository: RehabRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    
    init {
        loadSchedules()
    }
    
    fun loadSchedules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getSchedules()
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                schedules = result.getOrNull()?.schedules ?: emptyList(),
                error = result.exceptionOrNull()?.message
            )
        }
    }
    
    fun selectDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    fun getSchedulesForSelectedDate(): List<Schedule> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(_uiState.value.selectedDate)
        
        return _uiState.value.schedules.filter { schedule ->
            schedule.scheduledDate.startsWith(selectedDateStr)
        }
    }
    
    // NEW: Get schedules from today onwards (including today)
    fun getUpcomingSchedules(): List<Schedule> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        
        return _uiState.value.schedules.filter { schedule ->
            try {
                val scheduleDate = dateFormat.parse(schedule.scheduledDate)
                scheduleDate != null && !scheduleDate.before(today.time)
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.scheduledDate }
    }
    
    fun markScheduleCompleted(scheduleId: Int) {
        viewModelScope.launch {
            repository.updateSchedule(scheduleId, "completed")
            loadSchedules()
        }
    }
    
    fun deleteSchedule(scheduleId: Int) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
            loadSchedules()
        }
    }
    
    fun refresh() = loadSchedules()
}
