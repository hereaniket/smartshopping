package com.shopsmart.endpoints.action

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, 8080) {
        shoppingService()
    }.start(wait = true)
}

fun Application.shoppingService() {
    routing {
        get("/") {
            call.respondText("Hello Kotlin")
        }
        post("v1/store/"){
            call.respondText("Store service API")
        }

        post("v1/register/"){
            call.respondText("Registration API")
        }

        post("v1/navigate/"){
            call.respondText("Navigation API")
        }
    }
}