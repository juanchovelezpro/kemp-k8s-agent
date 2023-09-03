package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.model.KubeAuthType
import com.kemp.model.KubeClientEntity
import com.kemp.utils.toJson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.managerActions() {
    get {
        call.respondText(KubeManager.listClients().toJson(), ContentType.Application.Json)
    }

    post {
        val kubeClientEntity = call.receive<KubeClientEntity>()
        when (kubeClientEntity.authType) {
            KubeAuthType.TOKEN -> {
                KubeManager.addClientWithToken(
                    kubeClientEntity.name,
                    kubeClientEntity.url,
                    kubeClientEntity.token
                )
            }

            KubeAuthType.SA -> {
                KubeManager.addClientWithClusterSA(kubeClientEntity.name)
            }

            KubeAuthType.KUBECONFIG -> {
                KubeManager.addClientWithKubeConfig(kubeClientEntity.name, kubeClientEntity.kubeConfig)
            }

            KubeAuthType.LOCAL -> {
                KubeManager.addLocalClient(kubeClientEntity.name)
            }
        }

        call.respond("Client ${kubeClientEntity.name} was added")

    }

    route("/{cluster}") {
        get("/version") {
            val version = call.parameters["cluster"]?.let {
                KubeManager.getClient(it).getServerVersion().toJson()
            }
            call.respondText(version ?: "", ContentType.Application.Json)
        }
        get("/api-resources") {
            val serverResources = call.parameters["cluster"]?.let {
                KubeManager.getClient(it).getServerResources().toJson()
            }
            call.respondText(serverResources ?: "", ContentType.Application.Json)
        }
        delete {

        }
    }
}