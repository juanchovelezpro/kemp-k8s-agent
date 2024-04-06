package com.kemp

import com.kemp.client.KubeClient
import com.kemp.utils.toJson
import io.kubernetes.client.util.generic.options.ListOptions

fun main() {
    val k8sClient = KubeClient()
    println(k8sClient.listObjects("pods", listOptions = ListOptions()))
    println(k8sClient.getServerResources().toJson())
}