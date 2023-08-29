package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.model.KubeClientEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.managerActions() {

    get("/api/manager/clusters") {
        call.respondNullable(KubeManager.listClients())
    }

    post("/api/manager/cluster") {
        val kubeClientEntity = call.receive<KubeClientEntity>()
        val response = if (kubeClientEntity.withToken) {
            KubeManager.addClientWithToken(kubeClientEntity.name, kubeClientEntity.url, kubeClientEntity.token)
        } else if (kubeClientEntity.withServiceAccount) {
            KubeManager.addClientWithClusterSA(kubeClientEntity.name)
        } else if (kubeClientEntity.withKubeConfig) {
            KubeManager.addClientWithKubeConfig(kubeClientEntity.name, kubeClientEntity.kubeConfig)
        } else {
            KubeManager.addLocalClient(kubeClientEntity.name)
        }

        if (response) {
            call.respond("Client ${kubeClientEntity.name} was added")
        } else {
            call.respond("Client ${kubeClientEntity.name} could not be added")
        }
    }

    delete("/api/manager/cluster") {

    }

}