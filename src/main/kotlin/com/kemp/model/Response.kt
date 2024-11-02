package com.kemp.model

import kotlinx.serialization.Serializable

@Serializable
data class Response(val type: String, val data: String)
