package com.kemp.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*

fun Application.configureExceptions(){
    install(StatusPages){

//        exception<Throwable> { call, throwable: ->
//            when(throwable){
//
//            }
//
//        }
        status(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.BadGateway
        ){ call, statusCode ->

            when(statusCode){
                HttpStatusCode.InternalServerError -> {
                    call.respond(HttpStatusCode.InternalServerError, "The path specified does not exist")
                }
            }

        }


    }
}