package com.kemp.client

import com.google.gson.JsonParser
import com.kemp.model.GenericException
import com.kemp.utils.getItem
import com.kemp.utils.getItems
import io.kubernetes.client.Discovery
import io.kubernetes.client.Discovery.APIResource
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.Yaml
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
    fun findAPIResource(resourcePlural: String?): APIResource {
        return serverResources.find { it.resourcePlural == resourcePlural }
            ?: throw GenericException("Resource not found", 400, "There is no a resource with plural $resourcePlural")
    }

    fun createDynamicApi(resourcePlural: String): DynamicKubernetesApi {
        val resource = findAPIResource(resourcePlural)
        return DynamicKubernetesApi(resource.group, resource.preferredVersion, resource.resourcePlural, client)
    }

    /**
     * List resources of the specified group, version and resourcePlural name.
     *
     * This is a mimic of "kubectl get resourcePlural"
     */
    // Right now only filtering namespaced, what about labels, annotations, as kubectl does with selector.
    fun listResources(
        resourcePlural: String,
        namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val api = createDynamicApi(resourcePlural)
        return if (namespace.isNullOrEmpty()) api.list().getItems() else api.list(namespace).getItems()
    }

    /**
     * This is a mimic of "kubectl create resource my-resource.yaml"
     */
    fun createResource(resourcePlural: String, json: String): DynamicKubernetesObject? {
        val api = createDynamicApi(resourcePlural)
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val result = api.create(DynamicKubernetesObject(jsonObject))
        return if (result.isSuccess) result.getItem() else throw Exception("Error creating object ${result.getItem()?.metadata?.name}")
    }

    fun patchResource(resourcePlural: String, json: String) {
        val api = createDynamicApi(resourcePlural)
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val yaml = Yaml.loadAs("", DynamicKubernetesObject::class.java)
    }

    fun applyResource() {
        TODO("")
    }

    /**
     * This is a mimic of "kubectl get resource resourceName"
     */
    fun getResource(resourcePlural: String, name: String, namespace: String? = ""): DynamicKubernetesObject? {
        val api = createDynamicApi(resourcePlural)
        return if (namespace.isNullOrEmpty()) api.get(name).getItem() else api.get(namespace, name).getItem()
    }

    /**
     * This is a mimic of "kubectl delete resource"
     */
    fun deleteResource(resourcePlural: String, name: String, namespace: String? = ""): DynamicKubernetesObject? {
        val api = createDynamicApi(resourcePlural)
        return if (namespace.isNullOrEmpty()) api.delete(name).getItem() else api.delete(namespace, name).getItem()
    }

}