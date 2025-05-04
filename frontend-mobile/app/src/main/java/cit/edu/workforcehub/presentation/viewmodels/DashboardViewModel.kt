package cit.edu.workforcehub.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.workforcehub.api.ApiHelper
import cit.edu.workforcehub.api.models.AttendanceRecord
import cit.edu.workforcehub.api.models.ClockInRequest
import cit.edu.workforcehub.api.models.EmployeeProfile
import cit.edu.workforcehub.presentation.components.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
        private const val KEY_CLOCK_IN_TIME = "clock_in_time"
        private const val KEY_ATTENDANCE_ID = "attendance_id"
        private const val KEY_WORK_START_TIMESTAMP = "work_start_timestamp"
        private const val KEY_BREAK_START_TIMESTAMP = "break_start_timestamp"
        private const val KEY_TOTAL_BREAK_MILLIS = "total_break_millis"
    }
    
    // State values stored in SavedStateHandle will survive process death
    
    val isClockedIn: LiveData<Boolean> = savedStateHandle.getLiveData(KEY_IS_CLOCKED_IN, false)
    val currentTime: LiveData<String> = savedStateHandle.getLiveData(KEY_CURRENT_TIME, "00:00:00")
    val hoursWorked: LiveData<String> = savedStateHandle.getLiveData(KEY_HOURS_WORKED, "00:00:00")
    val breakTime: LiveData<String> = savedStateHandle.getLiveData(KEY_BREAK_TIME, "00:00:00")
    val activeBreak: LiveData<String?> = savedStateHandle.getLiveData<String?>(KEY_ACTIVE_BREAK, null)
    val clockInTime: LiveData<String?> = savedStateHandle.getLiveData<String?>(KEY_CLOCK_IN_TIME, null)
    val attendanceId: LiveData<String?> = savedStateHandle.getLiveData<String?>(KEY_ATTENDANCE_ID, null)
    
    // Private saved state for calculations
    private val workStartTimestamp = savedStateHandle.getLiveData<Long>(KEY_WORK_START_TIMESTAMP, 0L)
    private val breakStartTimestamp = savedStateHandle.getLiveData<Long>(KEY_BREAK_START_TIMESTAMP, 0L)
    private val totalBreakMillis = savedStateHandle.getLiveData<Long>(KEY_TOTAL_BREAK_MILLIS, 0L)
    
    // Status for API operations
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _attendanceRecord = MutableLiveData<AttendanceRecord?>(null)
    val attendanceRecord: LiveData<AttendanceRecord?> = _attendanceRecord
    
    // Notification dialog state
    private val _showNotification = MutableLiveData<Boolean>(false)
    val showNotification: LiveData<Boolean> = _showNotification
    
    private val _notificationTitle = MutableLiveData<String>("")
    val notificationTitle: LiveData<String> = _notificationTitle
    
    private val _notificationMessage = MutableLiveData<String>("")
    val notificationMessage: LiveData<String> = _notificationMessage
    
    private val _notificationType = MutableLiveData<NotificationType>(NotificationType.INFO)
    val notificationType: LiveData<NotificationType> = _notificationType
    
    // Coroutine jobs for timers
    private var workTimerJob: Job? = null
    private var breakTimerJob: Job? = null
    
    init {
        // Check current attendance status on initialization
        checkTodayAttendance()
        
        // If clocked in, start the work timer
        if (isClockedIn.value == true && workStartTimestamp.value != 0L) {
            startWorkTimer()
        }
        
        // If on break, start the break timer
        if (activeBreak.value != null && breakStartTimestamp.value != 0L) {
            startBreakTimer(activeBreak.value)
        }
    }
    
    /**
     * Check today's attendance record from the server
     */
    fun checkTodayAttendance() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val employeeService = ApiHelper.getEmployeeService()
                val response = employeeService.getTodayAttendance()
                
                if (response.isSuccessful && response.body() != null) {
                    val record = response.body()
                    _attendanceRecord.value = record
                    
                    // Update local state based on server record
                    updateClockState(record?.clockOutTime == null)
                    
                    if (record?.clockOutTime == null) {
                        // Still clocked in
                        val formattedTime = record?.clockInTime ?: "00:00:00"
                        savedStateHandle[KEY_CLOCK_IN_TIME] = formattedTime
                        savedStateHandle[KEY_ATTENDANCE_ID] = record?.recordId
                        
                        // Calculate elapsed time and start timer
                        val nowMillis = System.currentTimeMillis()
                        // Convert the clock-in time string to a timestamp
                        val clockInMillis = record?.clockInTime?.let { parseTimeToMillis(it) } ?: nowMillis
                        savedStateHandle[KEY_WORK_START_TIMESTAMP] = clockInMillis
                        
                        // Start timer
                        startWorkTimer()
                    } else {
                        // Already clocked out
                        val formattedTime = record?.clockInTime ?: "00:00:00"
                        savedStateHandle[KEY_CLOCK_IN_TIME] = formattedTime
                        
                        // Set total hours from server
                        val hoursStr = formatHoursWorked(record?.totalHours ?: 0.0)
                        updateHoursWorked(hoursStr)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to check attendance: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Parse a time string (HH:mm:ss) to milliseconds timestamp
     */
    private fun parseTimeToMillis(timeString: String): Long {
        try {
            // Parse time string (format: "HH:mm:ss")
            val timeParts = timeString.split(":")
            if (timeParts.size >= 3) {
                val hours = timeParts[0].toInt()
                val minutes = timeParts[1].toInt()
                val seconds = timeParts[2].toInt()
                
                // Get today's date
                val today = LocalDate.now()
                
                // Combine date and time
                val dateTime = LocalDateTime.of(
                    today.year,
                    today.month,
                    today.dayOfMonth,
                    hours,
                    minutes,
                    seconds
                )
                
                // Convert to epoch millis
                return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        } catch (e: Exception) {
            // Log error or handle exception
        }
        
        // Return current time as fallback
        return System.currentTimeMillis()
    }
    
    /**
     * Clock in the user
     */
    fun clockIn() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val employeeService = ApiHelper.getEmployeeService()
                
                // Get profile to get employeeId
                val profileResponse = employeeService.getProfile()
                if (!profileResponse.isSuccessful || profileResponse.body() == null) {
                    _error.value = "Failed to get employee profile: ${profileResponse.message()}"
                    return@launch
                }
                
                val employeeId = profileResponse.body()?.employeeId
                    ?: throw Exception("Employee ID not found in profile")
                
                // Create clock-in request with employeeId
                val request = ClockInRequest(employeeId = employeeId, remarks = null, latitude = null, longitude = null)
                val response = employeeService.clockIn(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val record = response.body()
                    _attendanceRecord.value = record
                    
                    // Update UI state
                    updateClockState(true)
                    
                    // Save clock in time
                    val formattedTime = record?.clockInTime ?: "00:00:00"
                    savedStateHandle[KEY_CLOCK_IN_TIME] = formattedTime
                    savedStateHandle[KEY_ATTENDANCE_ID] = record?.recordId
                    
                    // Start work timer
                    savedStateHandle[KEY_WORK_START_TIMESTAMP] = System.currentTimeMillis()
                    savedStateHandle[KEY_TOTAL_BREAK_MILLIS] = 0L
                    updateBreakTime("00:00:00")
                    startWorkTimer()
                    
                    // Show success notification
                    println("Debug: Showing success notification for clock in")
                    showNotification(
                        "Clock In Successful",
                        "You have successfully clocked in for today.",
                        NotificationType.SUCCESS
                    )
                } else {
                    if (response.code() == 400 && response.errorBody()?.string()?.contains("already clocked in") == true) {
                        // Show the "already clocked in" notification
                        println("Debug: Showing 'already clocked in' notification")
                        showNotification(
                            "Already Clocked In",
                            "You have already clocked in today.",
                            NotificationType.WARNING
                        )
                    } else {
                        _error.value = "Failed to clock in: ${response.errorBody()?.string()}"
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("already clocked in") == true) {
                    // Show the "already clocked in" notification
                    println("Debug: Showing 'already clocked in' notification from exception")
                    showNotification(
                        "Already Clocked In",
                        "You have already clocked in today. Please clock out first before clocking in again.",
                        NotificationType.WARNING
                    )
                } else {
                    _error.value = "Failed to clock in: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Shows a notification dialog
     */
    private fun showNotification(title: String, message: String, type: NotificationType) {
        println("Debug: showNotification called - title: $title, type: $type")
        _notificationTitle.value = title
        _notificationMessage.value = message
        _notificationType.value = type
        _showNotification.value = true
        println("Debug: _showNotification set to ${_showNotification.value}")
    }
    
    /**
     * Dismisses the notification dialog
     */
    fun dismissNotification() {
        _showNotification.value = false
    }
    
    /**
     * Clock out the user
     */
    fun clockOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val id = attendanceId.value
                if (id == null) {
                    _error.value = "No active attendance record found"
                    return@launch
                }
                
                val employeeService = ApiHelper.getEmployeeService()
                
                // Get profile to get employeeId
                val profileResponse = employeeService.getProfile()
                if (!profileResponse.isSuccessful || profileResponse.body() == null) {
                    _error.value = "Failed to get employee profile: ${profileResponse.message()}"
                    return@launch
                }
                
                val employeeId = profileResponse.body()?.employeeId
                    ?: throw Exception("Employee ID not found in profile")
                
                // Create clock-out request with employeeId
                val request = ClockInRequest(employeeId = employeeId, remarks = null, latitude = null, longitude = null)
                
                // Stop timers
                stopWorkTimer()
                stopBreakTimer()
                
                // End any active break
                if (activeBreak.value != null) {
                    endBreak()
                }
                
                val response = employeeService.clockOut(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val record = response.body()
                    _attendanceRecord.value = record
                    
                    // Update UI state
                    updateClockState(false)
                    
                    // Set final hours worked from server
                    val hoursStr = formatHoursWorked(record?.totalHours ?: 0.0)
                    updateHoursWorked(hoursStr)
                    
                    // Clear timestamps
                    savedStateHandle[KEY_WORK_START_TIMESTAMP] = 0L
                    savedStateHandle[KEY_BREAK_START_TIMESTAMP] = 0L
                    savedStateHandle[KEY_TOTAL_BREAK_MILLIS] = 0L
                    savedStateHandle[KEY_ATTENDANCE_ID] = null
                } else {
                    _error.value = "Failed to clock out: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to clock out: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start a break
     */
    fun startBreak(breakType: String) {
        if (isClockedIn.value != true) return
        
        // Stop work timer
        workTimerJob?.cancel()
        
        // Update break state
        updateActiveBreak(breakType)
        savedStateHandle[KEY_BREAK_START_TIMESTAMP] = System.currentTimeMillis()
        
        // Set initial countdown time based on break type
        val initialBreakTime = when (breakType) {
            "Morning Break" -> "00:15:00" // 15 minutes
            "Lunch Break" -> "01:00:00"   // 1 hour
            "Afternoon Break" -> "01:00:00" // 1 hour
            else -> "00:15:00" // Default to 15 minutes
        }
        
        // Reset break time display to initial countdown
        updateBreakTime(initialBreakTime)
        
        // Start break timer
        startBreakTimer(breakType)
    }
    
    /**
     * End the current break
     */
    fun endBreak() {
        val breakStartTime = breakStartTimestamp.value ?: 0L
        if (breakStartTime <= 0L) return
        
        // Calculate time spent on this break
        val currentTime = System.currentTimeMillis()
        val breakDuration = currentTime - breakStartTime
        
        // Add to total break time
        val currentTotalBreak = totalBreakMillis.value ?: 0L
        val newTotalBreak = currentTotalBreak + breakDuration
        savedStateHandle[KEY_TOTAL_BREAK_MILLIS] = newTotalBreak
        
        // Update break time display
        updateBreakTime(formatBreakDuration(newTotalBreak))
        
        // Clear break state
        updateActiveBreak(null)
        savedStateHandle[KEY_BREAK_START_TIMESTAMP] = 0L
        
        // Stop break timer
        stopBreakTimer()
        
        // Restart work timer
        startWorkTimer()
    }
    
    private fun startWorkTimer() {
        // Cancel existing job if any
        workTimerJob?.cancel()
        
        workTimerJob = viewModelScope.launch {
            while (isActive) {
                val startTime = workStartTimestamp.value ?: 0L
                if (startTime > 0L) {
                    val current = System.currentTimeMillis()
                    val elapsed = current - startTime
                    val totalBreaks = totalBreakMillis.value ?: 0L
                    val netWorked = elapsed - totalBreaks
                    
                    // Update hours worked
                    updateHoursWorked(formatDuration(netWorked))
                }
                delay(1000) // Update every second
            }
        }
    }
    
    private fun startBreakTimer(breakType: String? = null) {
        // Cancel existing job if any
        breakTimerJob?.cancel()
        
        // Calculate total milliseconds for countdown based on break type
        val totalMillis = when (breakType) {
            "Morning Break" -> 15 * 60 * 1000L // 15 minutes in milliseconds
            "Lunch Break" -> 60 * 60 * 1000L   // 1 hour in milliseconds
            "Afternoon Break" -> 60 * 60 * 1000L // 1 hour in milliseconds
            else -> null // Use regular timer instead of countdown
        }
        
        breakTimerJob = viewModelScope.launch {
            if (totalMillis != null) {
                // Countdown timer mode
                val startTime = System.currentTimeMillis()
                val endTime = startTime + totalMillis
                
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    
                    // Calculate remaining time
                    val remainingMillis = endTime - currentTime
                    
                    if (remainingMillis <= 0) {
                        // Time's up - end the break
                        updateBreakTime("00:00:00")
                        endBreak()
                        break
                    }
                    
                    // Format and display remaining time
                    val remainingDisplay = formatBreakDuration(remainingMillis)
                    updateBreakTime(remainingDisplay)
                    
                    delay(1000) // Update every second
                }
            } else {
                // Regular timer mode (counting up from zero)
                while (isActive) {
                    val startTime = breakStartTimestamp.value ?: 0L
                    if (startTime > 0L) {
                        val current = System.currentTimeMillis()
                        val elapsed = current - startTime
                        
                        // Format and display current break duration
                        val breakDisplay = formatBreakDuration(elapsed)
                        
                        // Update the break time LiveData directly
                        updateBreakTime(breakDisplay)
                    }
                    delay(1000) // Update every second
                }
            }
        }
    }
    
    private fun stopWorkTimer() {
        workTimerJob?.cancel()
        workTimerJob = null
    }
    
    private fun stopBreakTimer() {
        breakTimerJob?.cancel()
        breakTimerJob = null
    }
    
    // Helper functions for time formatting
    
    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    private fun formatBreakDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    private fun formatHoursWorked(hours: Double): String {
        val totalSeconds = (hours * 3600).toInt()
        val hrs = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hrs, minutes, seconds)
    }
    
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
        // Stop timers
        workTimerJob?.cancel()
        breakTimerJob?.cancel()
        
        // Clear saved state
        savedStateHandle[KEY_IS_CLOCKED_IN] = false
        savedStateHandle[KEY_CURRENT_TIME] = "00:00:00"
        savedStateHandle[KEY_HOURS_WORKED] = "00:00:00"
        savedStateHandle[KEY_BREAK_TIME] = "00:00:00"
        savedStateHandle[KEY_ACTIVE_BREAK] = null
        savedStateHandle[KEY_CLOCK_IN_TIME] = null
        savedStateHandle[KEY_ATTENDANCE_ID] = null
        savedStateHandle[KEY_WORK_START_TIMESTAMP] = 0L
        savedStateHandle[KEY_BREAK_START_TIMESTAMP] = 0L
        savedStateHandle[KEY_TOTAL_BREAK_MILLIS] = 0L
        
        _attendanceRecord.value = null
        _error.value = null
    }
} 