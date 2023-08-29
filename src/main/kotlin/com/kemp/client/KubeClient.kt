package com.kemp.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.kubernetes.client.Discovery
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.generic.KubernetesApiResponse
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject

class KubeClient(val client: ApiClient) {

    private val discovery: Discovery = Discovery(client)
    private val versionApi: VersionApi = VersionApi(client)
    private val serverResources: Set<Discovery.APIResource> = getServerResources()
    private val gson = Gson()

    fun getServerVersion(): VersionInfo {
        return versionApi.code
    }

    fun getServerResources(): Set<Discovery.APIResource> {
        return discovery.findAll()
    }

    fun findAPIResource(resourcePlural: String): Discovery.APIResource? {
        return serverResources.find { it.resourcePlural == resourcePlural }
    }

    fun listResources(
        apiGroup: String?,
        apiVersion: String?,
        resourcePlural: String?,
        namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val api = DynamicKubernetesApi(apiGroup, apiVersion, resourcePlural, client)
        val list = if (namespace.isNullOrEmpty()) {
            api.list()
        } else {
            api.list(namespace)
        }
        return list.getItems()
    }

    fun listResources(
        resourcePlural: String,
        namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val apiResource = findAPIResource(resourcePlural)
        return listResources(apiResource?.group, apiResource?.preferredVersion, resourcePlural, namespace)
    }

    fun listResourcesAsStringJsonList(
        resourcePlural: String,
        namespace: String? = ""
    ): String? {
        return listResources(resourcePlural, namespace)?.asStringJsonList()
    }

    fun KubernetesApiResponse<DynamicKubernetesListObject>.getItems(): List<DynamicKubernetesObject>? {
        return if (this.isSuccess) {
            this.`object`.items
        } else {
            null
        }
    }

    fun List<DynamicKubernetesObject>.asJsonList(): List<JsonObject> {
        val jsonList = mutableListOf<JsonObject>()
        this.forEach { jsonList.add(it.raw) }
        return jsonList
    }

    fun List<DynamicKubernetesObject>.asStringJsonList(): String {
        return this.asJsonList().toString()
    }

}