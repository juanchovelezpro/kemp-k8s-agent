package com.kemp.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.LoggingEvent

fun Application.configureLogging(){
    install(CallLogging)
}