package com.kemp

import com.kemp.client.KubeClient
import com.kemp.model.KubeFormatType
import com.kemp.utils.asJsonList
import com.kemp.utils.toJson
import io.kubernetes.client.util.generic.options.ListOptions
import io.kubernetes.client.util.generic.options.PatchOptions

fun main() {
    val k8sClient = KubeClient()
    val serverResources = k8sClient.getServerResources()
    for(resource in serverResources){
        print("${resource.resourcePlural} ")
    }
    println()
    println(k8sClient.getServerVersion().gitVersion)
    val objects = k8sClient.listObjects("configmaps", namespace = "kube-system")
    for(obj in objects){
        println(obj.metadata.name)
    }
    val objectToCreate = """
        apiVersion: v1
        kind: Namespace
        metadata:
          name: example
    """.trimIndent()
    k8sClient.applyObject(objectToCreate, KubeFormatType.YAML)
    val namespaces = k8sClient.listObjects("namespaces")
    for(ns in namespaces){
        println(ns.metadata.name)
    }
}