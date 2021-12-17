package com.shopsmart.endpoints.action

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import com.shopsmart.endpoints.beans.AutoComplete
import com.shopsmart.endpoints.beans.Product
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

private const val smartshopping_db_name = "smartshopping"
private lateinit var mongoClient: MongoClient
private lateinit var mongoDb: MongoDatabase

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            host("*")
            header(HttpHeaders.ContentType)
        }
        install(ContentNegotiation){
            register(ContentType.Application.Json, JacksonConverter())
        }
        mongoClient = MongoConnectionManager.init()
        mongoDb = mongoClient.getDatabase(smartshopping_db_name)

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
            val prodTagColl = mongoDb.getCollection("product_tags")
            val prodList = mutableListOf<Product>()

            prodTagColl.find(and(eq("product_tag",autoComplete.token),eq("store_name", "walmart")))
                .limit(5)
                .forEach {
                    prodList.add(Product(it.getString("store_name"),
                        it.getString("prod_id"),
                        it.getString("product_full_name"),
                        mutableListOf<String>("")))
                }
            call.respond(HttpStatusCode.OK, prodList)
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
                val storeColl = mongoDb.getCollection("store")

                val docs = mutableListOf<Document>()

                order.append("store_name", shoppingList.store_name)
                shoppingList.items.forEach{
                    val doc = Document()
                    doc.append("name",it.name)
                    doc.append("quantity",it.quantity)
                    doc.append("prodId", it.prodId)
                    docs.add(doc)
                }
                order.append("items", docs)
                order.append("order_id", orderId)
                storeColl.insertOne(order)
            }

            call.response.header("correlationId",orderId)
            call.respond(HttpStatusCode.OK, order.toJson())
        }
    }
}

fun validate(shoppingList: ShoppingList) {

}