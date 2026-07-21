package com.catsblock.phone.viewmodel

import android.app.Application
import android.content.Context
import android.provider.CallLog as SystemCallLog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catsblock.phone.models.CallLog
import com.catsblock.phone.models.CallStatus
import com.catsblock.phone.models.RecordSetting
import com.catsblock.phone.models.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber = _dialedNumber.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallLog>>(emptyList())
    val callHistory = _callHistory.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings = _settings.asStateFlow()

    private val _activeCallTimer = MutableStateFlow(0L)
    val activeCallTimer = _activeCallTimer.asStateFlow()
    private var callTimerJob: Job? = null

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            _isLoading.value = false
            loadRealCallLogs(application)
        }
    }

    private suspend fun loadRealCallLogs(context: Context) {
        val logs = mutableListOf<CallLog>()
        try {
            withContext(Dispatchers.IO) {
                val cursor = context.contentResolver.query(
                    SystemCallLog.Calls.CONTENT_URI,
                    arrayOf(
                        SystemCallLog.Calls._ID,
                        SystemCallLog.Calls.CACHED_NAME,
                        SystemCallLog.Calls.NUMBER,
                        SystemCallLog.Calls.DATE,
                        SystemCallLog.Calls.TYPE,
                        SystemCallLog.Calls.DURATION
                    ),
                    null,
                    null,
                    "${SystemCallLog.Calls.DATE} DESC LIMIT 50"
                )

                cursor?.use {
                    val idIdx = it.getColumnIndex(SystemCallLog.Calls._ID)
                    val nameIdx = it.getColumnIndex(SystemCallLog.Calls.CACHED_NAME)
                    val numIdx = it.getColumnIndex(SystemCallLog.Calls.NUMBER)
                    val dateIdx = it.getColumnIndex(SystemCallLog.Calls.DATE)
                    val typeIdx = it.getColumnIndex(SystemCallLog.Calls.TYPE)
                    val durationIdx = it.getColumnIndex(SystemCallLog.Calls.DURATION)

                    while (it.moveToNext()) {
                        val id = if (idIdx != -1) it.getString(idIdx) else "0"
                        val name = if (nameIdx != -1) it.getString(nameIdx) else null
                        val number = if (numIdx != -1) it.getString(numIdx) ?: "Desconhecido" else "Desconhecido"
                        val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                        val type = if (typeIdx != -1) it.getInt(typeIdx) else SystemCallLog.Calls.INCOMING_TYPE
                        val duration = if (durationIdx != -1) it.getInt(durationIdx) else 0

                        val status = when (type) {
                            SystemCallLog.Calls.INCOMING_TYPE,
                            SystemCallLog.Calls.OUTGOING_TYPE -> CallStatus.ATTENDED
                            SystemCallLog.Calls.MISSED_TYPE -> CallStatus.MISSED
                            SystemCallLog.Calls.REJECTED_TYPE -> CallStatus.REJECTED
                            else -> CallStatus.DROPPED
                        }

                        val timeStr = android.text.format.DateFormat.format("HH:mm", date).toString()

                        logs.add(
                            CallLog(
                                id = id,
                                name = name,
                                number = number,
                                timestampMs = date,
                                timeString = timeStr,
                                status = status,
                                hasRecording = duration > 10,
                                recordingDurationSecs = if (duration > 0) duration else 30
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback empty list if permission not available yet
        }
        _callHistory.value = logs
    }

    fun grantPermissions() {
        _permissionsGranted.value = true
        loadRealCallLogs(getApplication())
    }

    fun appendDigit(digit: String) {
        _dialedNumber.value += digit
        if (_dialedNumber.value == "*#2026#*") {
            _navigationEvent.value = "TEST_MODE"
            _dialedNumber.value = ""
        }
    }

    fun removeLastDigit() {
        if (_dialedNumber.value.isNotEmpty()) {
            _dialedNumber.value = _dialedNumber.value.dropLast(1)
        }
    }

    fun clearDialedNumber() {
        _dialedNumber.value = ""
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun startCallTimer() {
        _activeCallTimer.value = 0L
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCallTimer.value += 1
                if (_activeCallTimer.value >= 604800) { // 7 days safety limit
                    stopCallTimer()
                    _navigationEvent.value = "AUTO_HANGUP"
                    break
                }
            }
        }
    }

    fun stopCallTimer() {
        callTimerJob?.cancel()
    }

    fun formatCallTime(seconds: Long): String {
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            days > 0 -> String.format("%02d:%02d:%02d:%02d", days, hours, minutes, secs)
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%02d:%02d", minutes, secs)
        }
    }

    fun updateRecordSetting(setting: RecordSetting) {
        _settings.update { it.copy(recordSetting = setting) }
    }

    fun toggleAutoReject() {
        _settings.update { it.copy(autoRejectUnknown = !it.autoRejectUnknown) }
    }
}
