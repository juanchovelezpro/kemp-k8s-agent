package com.kemp.client

import com.google.gson.Gson
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.Config

object KubeManager {

    private val clients = HashMap<String, KubeClient>()
    private val gson = Gson()

    fun listClients(): MutableSet<String> {
        return clients.keys
    }

    private fun addClient(name: String, client: ApiClient): Boolean {
        return if (clients.containsKey(name)) {
            false
        } else {
            clients[name] = KubeClient(client)
            true
        }
    }

    fun addClientWithToken(
        name: String,
        url: String,
        token: String,
        validateSsl: Boolean = false
    ): Boolean {
        val client = Config.fromToken(url, token, validateSsl)
        return addClient(name, client)
    }

    fun addClientWithKubeConfig(
        name: String,
        kubeConfig: String
    ): Boolean {
        val client = Config.fromConfig(kubeConfig)
        return addClient(name, client)
    }

    fun addClientWithClusterSA(
        name: String,
    ): Boolean {
        val client = Config.fromCluster()
        return addClient(name, client)
    }

    fun addLocalClient(
        name: String
    ): Boolean {
        val client = ClientBuilder.defaultClient()
        return addClient(name, client)
    }

    fun getClient(name: String): KubeClient? {
        return clients[name]
    }

    fun removeClient(
        name: String
    ): KubeClient? {
        return clients.remove(name)
    }

    fun Any.toJson(): String {
        return gson.toJson(this)
    }

}