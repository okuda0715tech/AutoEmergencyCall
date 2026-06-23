package com.kurodai0715.autoemergencycall.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phoneNumber: String = "",
    val relation: String = "",
)