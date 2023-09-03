package com.kemp.model

data class KubeClientEntity(
    val name: String,
    val authType: KubeAuthType,
    val url: String = "",
    val token: String = "",
    val kubeConfig: String = ""
)
