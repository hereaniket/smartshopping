package com.smartshopping.be.action

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

internal fun main() {

    embeddedServer(Netty, 8080) {
        shoppingService()
    }
}

fun Application.shoppingService() {
    routing {
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