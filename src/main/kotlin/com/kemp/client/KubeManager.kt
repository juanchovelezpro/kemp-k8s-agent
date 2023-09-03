package com.kemp.client

import com.kemp.model.GenericException
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.Config

object KubeManager {

    // How to persistent data ? Database or directly as kubernetes objects ? or this should be implemented depending on the deployment type ?
    private val clients = HashMap<String, KubeClient>()

    fun listClients(): Set<String> {
        return clients.keys
    }

    private fun addClient(name: String, client: ApiClient) {
        return if (clients.containsKey(name)) {
            throw GenericException("Cluster $name already exists!", 400, "")
        } else {
            clients[name] = KubeClient(client)
        }
    }

    fun addClientWithToken(
        name: String,
        url: String,
        token: String,
        validateSsl: Boolean = false
    ) {
        val client = Config.fromToken(url, token, validateSsl)
        addClient(name, client)
    }

    fun addClientWithKubeConfig(
        name: String,
        kubeConfig: String
    ) {
        val client = Config.fromConfig(kubeConfig)
        addClient(name, client)
    }

    fun addClientWithClusterSA(
        name: String,
    ) {
        val client = Config.fromCluster()
        addClient(name, client)
    }

    fun addLocalClient(
        name: String
    ) {
        val client = ClientBuilder.defaultClient()
        addClient(name, client)
    }

    fun getClient(name: String): KubeClient {
        return clients[name] ?: throw GenericException("Cluster $name does not exist", 400, "")
    }

    fun removeClient(
        name: String
    ): KubeClient? {
        return clients.remove(name)
    }

}