package com.kemp.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.kubernetes.client.util.generic.KubernetesApiResponse
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject

fun KubernetesApiResponse<DynamicKubernetesListObject>.getItems(): List<DynamicKubernetesObject>? {
    return if (this.isSuccess) this.`object`.items else null
}

fun KubernetesApiResponse<DynamicKubernetesObject>.getItem(): DynamicKubernetesObject? {
    return if (this.isSuccess) this.`object` else null
}

fun List<DynamicKubernetesObject>.asJsonList(): List<JsonObject> {
    val jsonList = mutableListOf<JsonObject>()
    this.forEach { jsonList.add(it.raw) }
    return jsonList
}

fun List<DynamicKubernetesObject>.asStringJsonList(): String {
    return this.asJsonList().toString()
}

fun DynamicKubernetesObject.asJsonObject(): JsonObject {
    return this.raw
}

fun DynamicKubernetesObject.asStringJson(): String {
    return this.asJsonObject().toString()
}

fun Any.toJson(): String {
    return Gson().toJson(this)
}