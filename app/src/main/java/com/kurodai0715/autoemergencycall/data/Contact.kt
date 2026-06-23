package com.kurodai0715.autoemergencycall.data

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String = "",
    val phoneNumber: String = "",
    val relation: String = "",
)