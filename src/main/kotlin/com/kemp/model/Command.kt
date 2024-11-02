package com.kemp.model

import kotlinx.serialization.Serializable

@Serializable
data class Command(val type: String, val details: String)
