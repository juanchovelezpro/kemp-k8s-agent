package com.kemp.client

import com.google.gson.JsonParser
import com.kemp.model.GenericException
import com.kemp.utils.getItem
import com.kemp.utils.getItems
import io.kubernetes.client.Discovery
import io.kubernetes.client.Discovery.APIResource
import io.kubernetes.client.custom.V1Patch
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
    fun findAPIResource(resourcePlural: String): APIResource {
        return serverResources.find { it.resourcePlural == resourcePlural }
            ?: throw GenericException("Resource not found", 400, "There is no a resource with plural $resourcePlural")
    }

    fun findAPIResourceByKind(kind: String): APIResource {
        return serverResources.find { it.kind == kind }
            ?: throw GenericException("Resource not found", 400, "There is no a resource with kind $kind")
    }

    fun createDynamicApi(resource: APIResource): DynamicKubernetesApi {
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
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.list() else api.list(namespace)
        result.throwsApiException()
        return result.getItems()
    }

    /**
     * This is a mimic of "kubectl create resource my-resource.yaml"
     */
    fun createResource(json: String): Boolean {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val kind = jsonObject?.get("kind")?.asString ?: throw GenericException(
            "JSON Object without kind",
            400,
            "The kind parameter could not be found."
        )
        val resource = findAPIResourceByKind(kind)
        val api = createDynamicApi(resource)
        val result = api.create(DynamicKubernetesObject(jsonObject)).throwsApiException()
        return result.isSuccess
    }

    fun patchResource(json: String): Boolean {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val name = jsonObject.get("metadata").asJsonObject?.get("name")?.asString ?: throw GenericException(
            "JSON Object without metadata.name",
            400,
            "The name parameter in metadata could not be found"
        )
        val kind = jsonObject.get("kind")?.asString ?: throw GenericException(
            "JSON Object without kind",
            400,
            "The kind parameter could not be found."
        )
        val resource = findAPIResourceByKind(kind)
        val api = createDynamicApi(resource)

        val result = if (resource.namespaced) {
            val namespace =
                jsonObject.get("metadata")?.asJsonObject?.get("namespace")?.asString ?: throw GenericException(
                    "JSON Object without metadata.namespace",
                    400,
                    "The namespace parameter in metadata could not be found"
                )
            api.patch(namespace, name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(json)).throwsApiException()

        } else {
            api.patch(name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(json)).throwsApiException()
        }
        return result.isSuccess
    }

    fun applyResource() {
        TODO("")
    }

    /**
     * This is a mimic of "kubectl get resource resourceName"
     */
    fun getResource(resourcePlural: String, name: String, namespace: String? = ""): DynamicKubernetesObject? {
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.get(name) else api.get(namespace, name)
        result.throwsApiException()
        return result.getItem()
    }

    /**
     * This is a mimic of "kubectl delete resource"
     */
    fun deleteResource(resourcePlural: String, name: String, namespace: String? = ""): Boolean {
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.delete(name) else api.delete(namespace, name)
        result.throwsApiException()
        return result.isSuccess
    }

}