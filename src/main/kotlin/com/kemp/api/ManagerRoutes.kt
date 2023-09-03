package com.kemp.api

import com.kemp.client.KubeManager
import com.kemp.model.KubeAuthType
import com.kemp.model.KubeClientEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.managerActions() {

    route("/api/manager") {
        get("/clusters") {
            call.respondNullable(KubeManager.listClients())
        }
        route("/cluster") {
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
            delete {

            }
        }
    }
}