package com.kemp.client

import com.kemp.model.GenericException
import com.kemp.model.KubeFormatType
import com.kemp.utils.getGroupVersionKind
import com.kemp.utils.getItem
import com.kemp.utils.getItems
import com.kemp.utils.loadDynamicK8sObject
import io.kubernetes.client.Discovery
import io.kubernetes.client.Discovery.APIResource
import io.kubernetes.client.apimachinery.GroupVersionKind
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.VersionApi
import io.kubernetes.client.openapi.models.VersionInfo
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject
import io.kubernetes.client.util.generic.options.DeleteOptions
import io.kubernetes.client.util.generic.options.GetOptions
import io.kubernetes.client.util.generic.options.ListOptions
import io.kubernetes.client.util.generic.options.PatchOptions

class KubeClient {

    private val client: ApiClient = ClientBuilder.defaultClient()
    private val discovery: Discovery = Discovery(client)
    private val versionApi: VersionApi = VersionApi(client)

    fun getServerVersion(): VersionInfo {
        return versionApi.code
    }

    /**
     * Discovers all resources available in the Kubernetes Cluster.
     *
     * This is a mimic of "kubectl api-resources"
     */
    fun getServerResources(): Set<APIResource> {
        return discovery.findAll()
    }

    /**
     * Find resources by their resourcePlural name.
     */
    fun findAPIResources(resourcePlural: String): List<APIResource> {
        val resources = getServerResources().filter { it.resourcePlural == resourcePlural }
        if (resources.isNotEmpty()) return resources else throw GenericException(
            "Resources not found",
            404,
            "There are no resources with the specified plural name: $resourcePlural"
        )
    }

    /**
     * Find a resource by specifying its group, version and kind.
     */
    fun findAPIResourceByGroupVersionKind(groupVersionKind: GroupVersionKind): APIResource {
        val group = groupVersionKind.group
        val version = groupVersionKind.version
        val kind = groupVersionKind.kind
        return getServerResources().find {
            it.kind == kind && it.group == group && it.versions.contains(version)
        } ?: throw GenericException(
            "Resource not found", 404, "There is no a resource with group: $group, version: $version and kind: $kind"
        )
    }

    fun createDynamicApi(group: String, version: String, resourcePlural: String): DynamicKubernetesApi {
        return DynamicKubernetesApi(group, version, resourcePlural, client)
    }

    /**
     * List resources (multiple resources from different groups) by its resourcePlural name.
     *
     * This is a mimic of "kubectl get resourcePlural"
     */
    // Right now only filtering namespaced, what about labels, annotations, as kubectl does with selector.
    fun listObjects(
        resourcePlural: String,
        namespace: String? = "",
        listOptions: ListOptions
    ): List<DynamicKubernetesObject> {
        val resources = findAPIResources(resourcePlural)
        return if (resources.size > 1) {
            val objectsList = mutableListOf<DynamicKubernetesObject>()
            resources.forEach {
                objectsList.addAll(listObjects(it.group, it.preferredVersion, it.kind, namespace, listOptions))
            }
            objectsList
        } else {
            listObjects(resources[0].group, resources[0].preferredVersion, resources[0].kind, namespace, listOptions)
        }
    }

    fun listObjects(
        group: String,
        version: String,
        kind: String,
        namespace: String? = "",
        listOptions: ListOptions
    ): List<DynamicKubernetesObject> {
        val resource = findAPIResourceByGroupVersionKind(GroupVersionKind(group, version, kind))
        val api = createDynamicApi(resource.group, version, resource.resourcePlural)
        val result = if (namespace.isNullOrEmpty()) api.list(listOptions) else api.list(namespace, listOptions)
        return result.getItems()
    }

    /**
     * This is a mimic of kubectl apply my-resource.yaml
     */
    fun applyObject(
        objectConfiguration: String,
        format: KubeFormatType,
        patchOptions: PatchOptions
    ): Boolean {
        val dynamicObject = loadDynamicK8sObject(objectConfiguration, format)
        val groupVersionKind = dynamicObject.getGroupVersionKind()
        val metadata = dynamicObject.metadata
        val name = metadata.name
        val resource = findAPIResourceByGroupVersionKind(groupVersionKind)
        val api = DynamicKubernetesApi(resource.group, groupVersionKind.version, resource.resourcePlural, client)
        val result = if (resource.namespaced) {
            val namespace = metadata.namespace
            api.patch(namespace, name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(objectConfiguration), patchOptions)
        } else {
            api.patch(name, V1Patch.PATCH_FORMAT_APPLY_YAML, V1Patch(objectConfiguration), patchOptions)
        }
        result.throwsApiException()
        return result.isSuccess
    }

    /**
     * This is a mimic of "kubectl get resource resourceName"
     */
    fun getObject(
        group: String,
        version: String,
        resourcePlural: String,
        name: String,
        namespace: String? = "",
        getOptions: GetOptions
    ): DynamicKubernetesObject? {
        //val resource = findAPIResourceByGroupVersionKind(GroupVersionKind(group, version, resourcePlural))
        val api = createDynamicApi(group, version, resourcePlural)
        val result = if (namespace.isNullOrEmpty()) {
            api.get(name, getOptions)
        } else {
            api.get(namespace, name, getOptions)
        }
        return result.getItem()
    }

    /**
     * This is a mimic of "kubectl delete resource"
     */
    fun deleteObject(
        group: String,
        version: String,
        resourcePlural: String,
        name: String,
        namespace: String? = "",
        deleteOptions: DeleteOptions
    ): Boolean {
        //val resource = findAPIResourceByGroupVersionKind(GroupVersionKind(group, version, resourcePlural))
        val api = createDynamicApi(group, version, resourcePlural)
        val result = if (namespace.isNullOrEmpty()) {
            api.delete(name, deleteOptions)
        } else {
            api.delete(namespace, name, deleteOptions)
        }
        result.throwsApiException()
        return result.isSuccess
    }

}