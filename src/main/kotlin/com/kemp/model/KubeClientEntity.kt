package com.kemp.model

import kotlinx.serialization.Serializable

@Serializable
data class KubeClientEntity(
    val name: String,
    val url: String = "",
    val token: String = "",
    val kubeConfig: String = "",
    val withToken: Boolean = false,
    val withServiceAccount: Boolean = false,
    val withKubeConfig: Boolean = false
)
