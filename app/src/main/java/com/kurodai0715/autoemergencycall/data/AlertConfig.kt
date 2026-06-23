package com.kurodai0715.autoemergencycall.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AlertConfig(
    val id: String = UUID.randomUUID().toString(),
    val thresholdHours: Int,
    // 連絡先の ID (Contact.id) のリストを持たせることで、複数の連絡先を紐付け
    val targetContactIds: List<String>
)