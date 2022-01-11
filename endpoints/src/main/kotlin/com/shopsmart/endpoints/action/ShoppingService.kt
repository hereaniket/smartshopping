package com.shopsmart.endpoints.action

import com.shopsmart.backend.service.db.SmartShopDBManager
import com.shopsmart.endpoints.beans.AutoComplete
import com.shopsmart.endpoints.beans.Item
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document
import java.util.*

private lateinit var smartShopDBManager: SmartShopDBManager

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            host("*")
            header(HttpHeaders.ContentType)
        }
        install(ContentNegotiation){
            register(ContentType.Application.Json, JacksonConverter())
        }

        shoppingService()
    }.start(wait = true)
}

fun Application.shoppingService() {

    launch {
        smartShopDBManager = SmartShopDBManager()
    }.invokeOnCompletion { result ->
        if (result != null) {

        } else {
            routing {
                get("/") {
                    call.respondText("Hello Kotlin")
                }

                post("v1/search/autocomplete/"){
                    val autoComplete = call.receive<AutoComplete>()
                    val prodList = smartShopDBManager.searchProductTags(autoComplete.token, autoComplete.store_name)
                        .toList()
                    call.respond(HttpStatusCode.OK, prodList)
                }

                post("v1/search/matchTags/"){
                    val autoComplete = call.receive<AutoComplete>()
                    val productsBson = smartShopDBManager.searchUnknownProducts(autoComplete.token)

                    val products = productsBson.map { product ->
                        Product(prod_id = product.getInteger("prod_id"),
                            store_name = product.getString("store_name"),
                            product_full_name = product.getString("product_full_name"),
                            product_tag = listOf())
                    }
                    call.respond(HttpStatusCode.OK, products)
                }

                post("v1/register/"){
                    call.respondText("Registration API")
                }

                post("v1/save/"){
                    call.respondText("Navigation API")
                }

                post("v1/validate/"){
                    val shoppingList = call.receive<ShoppingList>()

                    withContext(Dispatchers.IO) {
                        val knownUnknownMap = separateKnownAndUnknown(shoppingList)

                        knownUnknownMap["UNKNOWN"].orEmpty()
                            .forEach { item ->
                                val tag = smartShopDBManager.searchUnknownProducts(item.name)
                                print("Hello $tag")
                            }

                        val order = buildNavigationOrder(shoppingList)
                        smartShopDBManager.saveNavigationOrder(order)
                        call.response.header("correlationId",order.getString("order_id"))
                        call.respond(HttpStatusCode.OK, order)
                    }

                }
            }
        }
    }

}



fun buildNavigationOrder(shoppingList: ShoppingList): Document {
    val orderId = UUID.randomUUID().toString()
    val order = Document()
    val products = mutableListOf<Document>()

    order.append("store_name", shoppingList.store_name)
    shoppingList.items.forEach{
        val product = Document()
        product.append("name",it.name)
        product.append("quantity",it.quantity)
        product.append("prodId", it.prodId.trim())
        products.add(product)
    }
    order.append("items", products)
    order.append("order_id", orderId)

    return order
}

fun separateKnownAndUnknown(shoppingList: ShoppingList): Map<String, List<Item>> {

    return shoppingList.items
        .groupBy {
            if(it.prodId.isNullOrEmpty().or(it.prodId.isNullOrBlank())) "UNKNOWN"
            else "KNOWN"
        }
}
