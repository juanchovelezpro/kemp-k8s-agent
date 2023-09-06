package com.kemp.client

import com.google.gson.JsonParser
import com.kemp.model.GenericException
import com.kemp.model.KubeFormatType
import com.kemp.utils.*
import io.kubernetes.client.Discovery
import io.kubernetes.client.Discovery.APIResource
import io.kubernetes.client.apimachinery.GroupVersionKind
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject
import io.kubernetes.client.util.generic.options.CreateOptions
import io.kubernetes.client.util.generic.options.PatchOptions

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
            ?: throw GenericException("Resource not found", 404, "There is no a resource with plural $resourcePlural")
    }

    fun findAPIResourceByKind(kind: String): APIResource {
        return serverResources.find { it.kind == kind } ?: throw GenericException(
            "Resource not found", 404, "There is no a resource with kind $kind"
        )
    }

    fun findAPIResourceByGroupVersionKind(groupVersionKind: GroupVersionKind): APIResource {
        val group = groupVersionKind.group
        val version = groupVersionKind.version
        val kind = groupVersionKind.kind
        return serverResources.find {
            it.kind == kind && it.group == group && it.versions.contains(version)
        } ?: throw GenericException(
            "Resource not found", 404, "There is no a resource with group: $group, version: $version and kind: $kind"
        )
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
        resourcePlural: String, namespace: String? = ""
    ): List<DynamicKubernetesObject>? {
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.list() else api.list(namespace)
        return result.getItems()
    }

    /**
     * This is a mimic of "kubectl create resource my-resource.yaml"
     */
    fun createResource(json: String, fieldManager: String = "kemp-apply"): Boolean {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val kind = jsonObject?.get("kind")?.asString ?: ""
        val resource = findAPIResourceByKind(kind)
        val api = createDynamicApi(resource)
        val createOptions = CreateOptions()
        createOptions.fieldManager = fieldManager
        val result = api.create(DynamicKubernetesObject(jsonObject), createOptions).throwsApiException()
        return result.isSuccess
    }

    /**
     * This is a mimic of kubectl apply my-resource.yaml
     */
    fun applyObject(
        objectConfiguration: String,
        format: KubeFormatType,
        fieldManager: String = "kemp-apply",
        force: Boolean = false
    ): Boolean {
        val dynamicObject = loadDynamicK8sObject(objectConfiguration, format)
        val groupVersionKind = dynamicObject.getGroupVersionKind()
        val metadata = dynamicObject.metadata
        val name = metadata.name
        val resource = findAPIResourceByGroupVersionKind(groupVersionKind)
        val api = DynamicKubernetesApi(resource.group, groupVersionKind.version, resource.resourcePlural, client)
        val patchOptions = PatchOptions()
        patchOptions.apply {
            this.force = force
            this.fieldManager = fieldManager
        }
        val result = if (resource.namespaced) {
            val namespace = metadata.namespace
            api.patch(
                namespace, name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(dynamicObject.asStringJson()), patchOptions
            )
        } else {
            api.patch(name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(dynamicObject.asStringJson()), patchOptions)
        }
        result.throwsApiException()
        return result.isSuccess
    }

    /**
     * This is a mimic of "kubectl get resource resourceName"
     */
    fun getObject(resourcePlural: String, name: String, namespace: String? = ""): DynamicKubernetesObject? {
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.get(name) else api.get(namespace, name)
        return result.getItem()
    }

    /**
     * This is a mimic of "kubectl delete resource"
     */
    fun deleteObject(resourcePlural: String, name: String, namespace: String? = ""): Boolean {
        val resource = findAPIResource(resourcePlural)
        val api = createDynamicApi(resource)
        val result = if (namespace.isNullOrEmpty()) api.delete(name) else api.delete(namespace, name)
        result.throwsApiException()
        return result.isSuccess
    }

}