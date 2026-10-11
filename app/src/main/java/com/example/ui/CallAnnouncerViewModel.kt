package com.example.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CallLogEntity
import com.example.service.CallAnnouncerService
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementBuilder
import com.example.util.AnnouncementResult
import com.example.util.PairSpokenMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PermissionStatus(
    val hasPhoneState: Boolean = false,
    val hasCallLog: Boolean = false,
    val hasContacts: Boolean = false,
    val hasNotifications: Boolean = false
) {
    val allGranted: Boolean
        get() = hasPhoneState && hasCallLog && hasContacts && hasNotifications
}

class CallAnnouncerViewModel(application: Application) : AndroidViewModel(application) {

    private val appSettings = AppSettings.getInstance(application)
    private val database = AppDatabase.getInstance(application)

    val settings = appSettings.settingsFlow

    val callLogs: StateFlow<List<CallLogEntity>> = database.callLogDao()
        .getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isTtsSpeaking = DirectTtsAnnouncer.isSpeaking
    val isTtsReady = DirectTtsAnnouncer.isReady

    // Simulator input state
    private val _testPhoneNumber = MutableStateFlow("078546")
    val testPhoneNumber: StateFlow<String> = _testPhoneNumber.asStateFlow()

    private val _testContactName = MutableStateFlow("Yves MTN")
    val testContactName: StateFlow<String> = _testContactName.asStateFlow()

    private val _isSimulatingRinging = MutableStateFlow(false)
    val isSimulatingRinging: StateFlow<Boolean> = _isSimulatingRinging.asStateFlow()

    private val _permissionsStatus = MutableStateFlow(PermissionStatus())
    val permissionsStatus: StateFlow<PermissionStatus> = _permissionsStatus.asStateFlow()

    private var simulationJob: Job? = null

    init {
        DirectTtsAnnouncer.initialize(application)
        checkPermissions(application)

        if (settings.value.isEnabled) {
            CallAnnouncerService.start(application)
        }
    }

    fun checkPermissions(context: Context) {
        val hasPhoneState = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val hasCallLog = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        val hasContacts = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _permissionsStatus.value = PermissionStatus(
            hasPhoneState = hasPhoneState,
            hasCallLog = hasCallLog,
            hasContacts = hasContacts,
            hasNotifications = hasNotifications
        )
    }

    fun setTestPhoneNumber(number: String) {
        _testPhoneNumber.value = number
    }

    fun setTestContactName(name: String) {
        _testContactName.value = name
    }

    fun getLivePreview(
        number: String = _testPhoneNumber.value,
        name: String = _testContactName.value,
        prefix: String = settings.value.prefixText,
        pairMode: PairSpokenMode = settings.value.pairMode
    ): AnnouncementResult {
        return AnnouncementBuilder.buildAnnouncement(
            contactName = name.ifBlank { null },
            phoneNumber = number.ifBlank { null },
            prefix = prefix,
            pairMode = pairMode
        )
    }

    fun toggleMasterEnabled() {
        val nextState = !settings.value.isEnabled
        appSettings.updateSettings { it.copy(isEnabled = nextState) }
        val context = getApplication<Application>()
        if (nextState) {
            CallAnnouncerService.start(context)
        } else {
            CallAnnouncerService.stop(context)
            DirectTtsAnnouncer.stop()
        }
    }

    fun updatePrefix(prefix: String) {
        appSettings.updateSettings { it.copy(prefixText = prefix) }
    }

    fun updatePairMode(mode: PairSpokenMode) {
        appSettings.updateSettings { it.copy(pairMode = mode) }
    }

    fun updateRepeatCount(count: Int) {
        appSettings.updateSettings { it.copy(repeatCount = count) }
    }

    fun updateSpeechRate(rate: Float) {
        appSettings.updateSettings { it.copy(speechRate = rate) }
    }

    fun updateSpeechPitch(pitch: Float) {
        appSettings.updateSettings { it.copy(speechPitch = pitch) }
    }

    fun updateInitialDelay(delayMs: Long) {
        appSettings.updateSettings { it.copy(initialDelayMs = delayMs) }
    }

    fun toggleAnnounceContacts() {
        appSettings.updateSettings { it.copy(announceContacts = !it.announceContacts) }
    }

    fun toggleAnnounceUnknowns() {
        appSettings.updateSettings { it.copy(announceUnknowns = !it.announceUnknowns) }
    }

    fun toggleDuckRingtone() {
        appSettings.updateSettings { it.copy(duckRingtone = !it.duckRingtone) }
    }

    fun toggleSyncRingtoneVolume() {
        appSettings.updateSettings { it.copy(syncRingtoneVolume = !it.syncRingtoneVolume) }
    }

    fun testSpeakPreview() {
        val preview = getLivePreview()
        val s = settings.value
        DirectTtsAnnouncer.speak(
            context = getApplication(),
            text = preview.spokenText,
            repeatCount = 1,
            initialDelayMs = 0L,
            repeatDelayMs = 1200L,
            rate = s.speechRate,
            pitch = s.speechPitch,
            duckRingtone = false,
            syncRingtoneVolume = s.syncRingtoneVolume
        )
    }

    fun testSpeakWithSettings() {
        val preview = getLivePreview()
        val s = settings.value
        DirectTtsAnnouncer.speak(
            context = getApplication(),
            text = preview.spokenText,
            repeatCount = s.repeatCount,
            initialDelayMs = s.initialDelayMs,
            repeatDelayMs = s.repeatDelayMs,
            rate = s.speechRate,
            pitch = s.speechPitch,
            duckRingtone = s.duckRingtone,
            syncRingtoneVolume = s.syncRingtoneVolume
        )
    }

    fun simulateIncomingCall() {
        stopTts()
        val preview = getLivePreview()
        val s = settings.value
        _isSimulatingRinging.value = true

        // Log this test call
        viewModelScope.launch {
            try {
                database.callLogDao().insertAnnouncement(
                    CallLogEntity(
                        rawPhoneNumber = _testPhoneNumber.value,
                        contactName = _testContactName.value.ifBlank { null },
                        formattedDisplay = preview.callerIdentifier,
                        spokenAnnouncement = preview.spokenText,
                        isKnownContact = preview.isKnownContact,
                        timesAnnounced = s.repeatCount
                    )
                )
            } catch (_: Exception) {}
        }

        simulationJob = viewModelScope.launch {
            DirectTtsAnnouncer.speak(
                context = getApplication(),
                text = preview.spokenText,
                repeatCount = s.repeatCount,
                initialDelayMs = s.initialDelayMs,
                repeatDelayMs = s.repeatDelayMs,
                rate = s.speechRate,
                pitch = s.speechPitch,
                duckRingtone = s.duckRingtone,
                syncRingtoneVolume = s.syncRingtoneVolume,
                onFinish = {
                    _isSimulatingRinging.value = false
                }
            )
        }
    }

    fun answerSimulatedCall() {
        simulationJob?.cancel()
        simulationJob = null
        DirectTtsAnnouncer.stop()
        _isSimulatingRinging.value = false
    }

    fun stopTts() {
        simulationJob?.cancel()
        simulationJob = null
        DirectTtsAnnouncer.stop()
        _isSimulatingRinging.value = false
    }

    fun replayCallLog(callLog: CallLogEntity) {
        val s = settings.value
        DirectTtsAnnouncer.speak(
            context = getApplication(),
            text = callLog.spokenAnnouncement,
            repeatCount = 1,
            initialDelayMs = 0L,
            rate = s.speechRate,
            pitch = s.speechPitch
        )
    }

    fun deleteCallLog(callLog: CallLogEntity) {
        viewModelScope.launch {
            database.callLogDao().deleteAnnouncement(callLog)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            database.callLogDao().clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop current speech playback, but keep DirectTtsAnnouncer available for background calls
        stopTts()
    }
}
