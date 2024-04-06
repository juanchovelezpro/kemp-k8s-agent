package com.kemp

import com.kemp.client.KubeClient
import com.kemp.utils.toJson

fun main() {
    val k8sClient = KubeClient()
    println(k8sClient.getServerResources().toJson())

}