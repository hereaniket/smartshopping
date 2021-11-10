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
        get("/"){
            call.respondText("Hello Aniket")
        }
    }
}