package com.shopsmart.endpoints.action

import com.mongodb.client.MongoClient
import com.shopsmart.backend.mongo.MongoConnectionManager
import com.shopsmart.endpoints.beans.AutoComplete
import com.shopsmart.endpoints.beans.ShoppingList
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.bson.Document
import java.util.*

lateinit var mongoClient: MongoClient

fun main() {
    val server = embeddedServer(Netty, 8080) {
        install(CORS) {
            host("*")
            header(HttpHeaders.ContentType)
        }
        install(ContentNegotiation){
            register(ContentType.Application.Json, JacksonConverter())
        }

        mongoClient = MongoConnectionManager.init()

        shoppingService()
    }.start(wait = true)
}

fun Application.shoppingService() {

    routing {
        get("/") {
            call.respondText("Hello Kotlin")
        }
        post("v1/search/autocomplete/"){
            val autoComplete = call.receive<AutoComplete>()
            //TODO
            call.respond(HttpStatusCode.OK, autoComplete)
        }

        post("v1/register/"){
            call.respondText("Registration API")
        }

        post("v1/navigate/"){
            call.respondText("Navigation API")
        }

        post("v1/validate/"){
            val shoppingList = call.receive<ShoppingList>()
            val orderId = UUID.randomUUID().toString()
            val order = Document()

            if(mongoClient != null) {
                val mongoDb = mongoClient.getDatabase("shopsmart")
                val mongoCollection = mongoDb.getCollection("sore")

                val docs = mutableListOf<Document>()

                order.append("store_name", shoppingList.store_name)
                shoppingList.items.forEach{
                    val doc = Document()
                    doc.append("name",it.name)
                    doc.append("quantity",it.quantity)
                    docs.add(doc)
                }
                order.append("items", docs)
                order.append("order_id", orderId)
                mongoCollection.insertOne(order)
            }

            call.response.header("correlationId",orderId)
            call.respond(HttpStatusCode.OK, order.toJson())
        }
    }
}

fun validate(shoppingList: ShoppingList) {

}