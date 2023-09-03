package com.kemp.model

data class KubeClientEntity(
    val name: String,
    val url: String = "",
    val token: String = "",
    val kubeConfig: String = "",
    val withToken: Boolean = false,
    val withServiceAccount: Boolean = false,
    val withKubeConfig: Boolean = false
)
