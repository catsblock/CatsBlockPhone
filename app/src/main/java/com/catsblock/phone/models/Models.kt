package com.catsblock.phone.models

enum class CallStatus(val label: String) {
    ATTENDED("Ligação aceitada"),
    MISSED("Ligação Não atendida"),
    DROPPED("Ligação Caiu"),
    REJECTED("Ligação Negada")
}

data class CallLog(
    val id: String,
    val name: String?,
    val number: String,
    val timestampMs: Long,
    val timeString: String,
    val status: CallStatus,
    val country: String = "Brasil",
    val state: String = "Goiás",
    val city: String = "Anápolis",
    val hasRecording: Boolean = false,
    val recordingDurationSecs: Int = 0
)

enum class RecordSetting {
    DISABLED, UNKNOWN_ONLY, CONTACTS_ONLY, ALL
}

data class UserSettings(
    val recordSetting: RecordSetting = RecordSetting.DISABLED,
    val autoRejectUnknown: Boolean = false,
    val blockedNumbers: List<String> = emptyList(),
    val language: String = "BR"
)
