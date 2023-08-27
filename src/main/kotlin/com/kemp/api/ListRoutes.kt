package com.kemp.api

import com.kemp.client.KubeClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.listObjects() {

    get("/nodes") {
        val nodes = KubeClient.listObjectsAsStringJsonList("nodes")
        call.respondNullable(nodes)
    }

    get("/namespaces") {
        val namespaces = KubeClient.listObjectsAsStringJsonList("namespaces")
        call.respondNullable(namespaces)
    }

    get("/pods") {
        val pods = KubeClient.listObjectsAsStringJsonList("pods")
        call.respondNullable(pods)
    }

    get("/deployments") {
        val deployments = KubeClient.listObjectsAsStringJsonList("deployments")
        call.respondNullable(deployments)
    }

    get("/replicasets") {
        val replicasets = KubeClient.listObjectsAsStringJsonList("replicasets")
        call.respondNullable(replicasets)
    }

    get("/statefulsets") {
        val statefulsets = KubeClient.listObjectsAsStringJsonList("statefulsets")
        call.respondNullable(statefulsets)
    }

    get("/daemonsets") {
        val daemonsets = KubeClient.listObjectsAsStringJsonList("daemonsets")
        call.respondNullable(daemonsets)
    }

    get("/jobs") {
        val jobs = KubeClient.listObjectsAsStringJsonList("jobs")
        call.respondNullable(jobs)
    }

    get("/cronjobs") {
        val cronjobs = KubeClient.listObjectsAsStringJsonList("cronjobs")
        call.respondNullable(cronjobs)
    }

    get("/horizontalpodautoscalers") {
        val hpas = KubeClient.listObjectsAsStringJsonList("horizontalpodautoscalers")
        call.respondNullable(hpas)
    }

    get("/services") {
        val services = KubeClient.listObjectsAsStringJsonList("services")
        call.respondNullable(services)
    }

    get("/ingresses") {
        val ingresses = KubeClient.listObjectsAsStringJsonList("ingresses")
        call.respondNullable(ingresses)
    }

    get("/ingressclasses") {
        val ingressclasses = KubeClient.listObjectsAsStringJsonList("ingressclasses")
        call.respondNullable(ingressclasses)
    }

    get("/networkpolicies") {
        val networkpolicies = KubeClient.listObjectsAsStringJsonList("networkpolicies")
        call.respondNullable(networkpolicies)
    }

    get("/persistentvolumes") {
        val pvs = KubeClient.listObjectsAsStringJsonList("persistentvolumes")
        call.respondNullable(pvs)
    }

    get("/persistentvolumeclaims") {
        val pvcs = KubeClient.listObjectsAsStringJsonList("persistentvolumeclaims")
        call.respondNullable(pvcs)
    }

    get("/storageclasses") {
        val pvcs = KubeClient.listObjectsAsStringJsonList("storageclasses")
        call.respondNullable(pvcs)
    }

    get("/configmaps") {
        val cms = KubeClient.listObjectsAsStringJsonList("configmaps")
        call.respondNullable(cms)
    }

    get("/secrets") {
        val secrets = KubeClient.listObjectsAsStringJsonList("secrets")
        call.respondNullable(secrets)
    }


}