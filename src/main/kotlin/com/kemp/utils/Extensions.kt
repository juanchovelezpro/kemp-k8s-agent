package com.kemp.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kemp.model.KubeFormatType
import io.ktor.websocket.*
import io.kubernetes.client.apimachinery.GroupVersionKind
import io.kubernetes.client.proto.Meta.GetOptions
import io.kubernetes.client.util.generic.KubernetesApiResponse
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject
import io.kubernetes.client.util.generic.dynamic.Dynamics

fun KubernetesApiResponse<DynamicKubernetesListObject>.getItems(): List<DynamicKubernetesObject> {
    this.throwsApiException()
    return if (this.isSuccess) this.`object`.items else emptyList()
}

fun KubernetesApiResponse<DynamicKubernetesObject>.getItem(): DynamicKubernetesObject? {
    this.throwsApiException()
    return if (this.isSuccess) this.`object` else null
}

fun loadDynamicK8sObject(objectConfiguration: String, format: KubeFormatType): DynamicKubernetesObject {
    return if (format == KubeFormatType.YAML) Dynamics.newFromYaml(objectConfiguration)
    else Dynamics.newFromJson(objectConfiguration)
}

fun DynamicKubernetesObject.getGroupVersionKind(): GroupVersionKind {
    val apiVersion = this.apiVersion
    val index = apiVersion.indexOf("/")
    val group = if (index == -1) "" else apiVersion.substring(0, index)
    val version = apiVersion.substring(index + 1)
    val kind = this.kind ?: ""
    return GroupVersionKind(group, version, kind)
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

fun String.asFrameText(): Frame.Text {
    return Frame.Text(this)
}