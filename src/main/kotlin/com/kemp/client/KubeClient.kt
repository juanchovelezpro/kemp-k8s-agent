package com.kemp.client

import com.google.gson.JsonParser
import com.kemp.utils.asStringJson
import com.kemp.utils.asStringJsonList
import com.kemp.utils.getItem
import com.kemp.utils.getItems
import io.kubernetes.client.Discovery
import io.kubernetes.client.Discovery.APIResource
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject

class KubeClient(val client: ApiClient) {

    private val discovery: Discovery = Discovery(client)
    private val versionApi: VersionApi = VersionApi(client)
    private val serverResources: Set<APIResource> = getServerResources()

    fun getServerVersion(): VersionInfo {
        return versionApi.code
    }

    /**
     * Discovers all resources available in the Kubernetes Cluster.
     *
     * This is a mimic of "kubectl api-resources"
     */
    // This only discovers resources when the client is created... how to update resources when there are new ones after the client is created ?
    // The idea is not to call this every time we need to get a resource, only when is needed.
    fun getServerResources(): Set<APIResource> {
        return discovery.findAll()
    }


    /**
     * Find a resource by its resourcePlural name.
     */
    // How to handle when there are multiples resources but with a different group? For these cases should we do it by specifying the group too?
    // Or maybe should we return all resources with same resource name regardless of the group ? (Using all method)
    fun findAPIResource(resourcePlural: String?): APIResource? {
        return serverResources.find { it.resourcePlural == resourcePlural }
    }

    /**
     * List resources of the specified group, version and resourcePlural name.
     *
     * This is a mimic of "kubectl get resourcePlural"
     */
    // Right now only filtering namespaced, what about labels, annotations, as kubectl does with selector.
    fun listResources(
        apiResource: APIResource?,
        namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val api =
            DynamicKubernetesApi(apiResource?.group, apiResource?.preferredVersion, apiResource?.resourcePlural, client)
        return if (namespace.isNullOrEmpty()) api.list().getItems() else api.list(namespace).getItems()
    }

    fun listResources(
        resourcePlural: String,
        namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val apiResource = findAPIResource(resourcePlural)
        return listResources(apiResource, namespace)
    }

    fun listResourcesAsStringJsonList(
        resourcePlural: String,
        namespace: String? = ""
    ): String? {
        return listResources(resourcePlural, namespace)?.asStringJsonList()
    }

    fun createResource(json: String, resourcePlural: String) {
        val resource = findAPIResource(resourcePlural)
        val api = DynamicKubernetesApi(resource?.group, resource?.preferredVersion, resourcePlural, client)
        val jsonObject = JsonParser.parseString(json).asJsonObject
        api.create(DynamicKubernetesObject(jsonObject))
    }

    /**
     * This a mimic of "kubectl get resource resourceName"
     */
    fun getResource(name: String, resourcePlural: String, namespace: String? = ""): DynamicKubernetesObject? {
        val resource = findAPIResource(resourcePlural)
        val api = DynamicKubernetesApi(resource?.group, resource?.preferredVersion, resourcePlural, client)
        return if (namespace.isNullOrEmpty()) api.get(name).getItem() else api.get(namespace, name).getItem()
    }

    fun getResourceAsJson(name: String, resourcePlural: String, namespace: String? = ""): String? {
        return getResource(name, resourcePlural, namespace)?.asStringJson()
    }

    /**
     * This is a mimic of "kubectl delete resource"
     */
    fun deleteResource(name: String, resourcePlural: String, namespace: String? = ""): DynamicKubernetesObject? {
        val resource = findAPIResource(resourcePlural)
        val api = DynamicKubernetesApi(resource?.group, resource?.preferredVersion, resourcePlural, client)
        return if (namespace.isNullOrEmpty()) api.delete(name).getItem() else api.delete(namespace, name).getItem()
    }

}