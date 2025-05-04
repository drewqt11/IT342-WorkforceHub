package cit.edu.workforcehub.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the DashboardScreen that handles state saving
 * across configuration changes and process death
 */
class DashboardViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    
    companion object {
        private const val KEY_IS_CLOCKED_IN = "is_clocked_in"
        private const val KEY_CURRENT_TIME = "current_time"
        private const val KEY_HOURS_WORKED = "hours_worked"
        private const val KEY_BREAK_TIME = "break_time"
        private const val KEY_ACTIVE_BREAK = "active_break"
    }
    
    // State values stored in SavedStateHandle will survive process death
    
    val isClockedIn: LiveData<Boolean> = savedStateHandle.getLiveData(KEY_IS_CLOCKED_IN, false)
    val currentTime: LiveData<String> = savedStateHandle.getLiveData(KEY_CURRENT_TIME, "00:00:00")
    val hoursWorked: LiveData<String> = savedStateHandle.getLiveData(KEY_HOURS_WORKED, "00:00:00")
    val breakTime: LiveData<String> = savedStateHandle.getLiveData(KEY_BREAK_TIME, "00:00")
    val activeBreak: LiveData<String?> = savedStateHandle.getLiveData<String?>(KEY_ACTIVE_BREAK, null)
    
    // Update functions to modify state
    
    fun updateClockState(isClockedIn: Boolean) {
        savedStateHandle[KEY_IS_CLOCKED_IN] = isClockedIn
    }
    
    fun updateCurrentTime(time: String) {
        savedStateHandle[KEY_CURRENT_TIME] = time
    }
    
    fun updateHoursWorked(hours: String) {
        savedStateHandle[KEY_HOURS_WORKED] = hours
    }
    
    fun updateBreakTime(time: String) {
        savedStateHandle[KEY_BREAK_TIME] = time
    }
    
    fun updateActiveBreak(breakType: String?) {
        savedStateHandle[KEY_ACTIVE_BREAK] = breakType
    }
    
    // Clear all saved state (used when logging out)
    fun clearState() {
        savedStateHandle[KEY_IS_CLOCKED_IN] = false
        savedStateHandle[KEY_CURRENT_TIME] = "00:00:00"
        savedStateHandle[KEY_HOURS_WORKED] = "00:00:00"
        savedStateHandle[KEY_BREAK_TIME] = "00:00"
        savedStateHandle[KEY_ACTIVE_BREAK] = null
    }
} 