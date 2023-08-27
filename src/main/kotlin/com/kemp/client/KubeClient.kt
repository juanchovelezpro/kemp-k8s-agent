package com.kemp.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.kubernetes.client.Discovery
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.generic.KubernetesApiResponse
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject

object KubeClient {

    private val client: ApiClient = ClientBuilder.defaultClient()
    private val discovery: Discovery
    private val versionApi: VersionApi
    private val serverResources: Set<Discovery.APIResource>
    val gson = Gson()

    init {
        Configuration.setDefaultApiClient(client)
        discovery = Discovery()
        versionApi = VersionApi()
        serverResources = getServerResources()
    }

    fun getServerVersion(): VersionInfo {
        return versionApi.code
    }

    fun getServerResources(): Set<Discovery.APIResource> {
        return discovery.findAll()
    }

    fun findAPIResource(resourcePlural: String): Discovery.APIResource? {
        return serverResources.find { it.resourcePlural == resourcePlural }
    }

    fun listObjects(
        apiGroup: String?,
        apiVersion: String?,
        resourcePlural: String?,
        namespace: String = ""
    ): List<DynamicKubernetesObject>? {
        val api = DynamicKubernetesApi(apiGroup, apiVersion, resourcePlural, client)
        val list = if (namespace.isNotEmpty()) {
            api.list(namespace)
        } else {
            api.list()
        }
        return list.getItems()
    }

    fun listObjects(
        resourcePlural: String,
        namespace: String = ""
    ): List<DynamicKubernetesObject>? {
        val apiResource = findAPIResource(resourcePlural)
        return listObjects(apiResource?.group, apiResource?.preferredVersion, resourcePlural, namespace)
    }

    fun listObjectsAsStringJsonList(
        resourcePlural: String,
        namespace: String = ""
    ): String? {
        return listObjects(resourcePlural, namespace)?.asStringJsonList()
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

    fun Any.toJson(): String {
        return gson.toJson(this)
    }

}