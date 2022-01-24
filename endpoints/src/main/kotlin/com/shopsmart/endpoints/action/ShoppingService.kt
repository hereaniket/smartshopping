package com.shopsmart.endpoints.action

import com.mongodb.client.model.Filters
import com.shopsmart.backend.service.db.SmartShopDBManager
import com.shopsmart.endpoints.beans.api.*
import com.shopsmart.endpoints.beans.entity.AisleInfo
import com.shopsmart.endpoints.beans.entity.DepartmentInfo
import com.shopsmart.endpoints.beans.entity.ProductInfo
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
        install(ContentNegotiation) {
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

                post("v1/search/autocomplete/") {
                    val autoComplete = call.receive<AutoComplete>()
                    val bson = Filters.and(Filters.eq("storeId",autoComplete.storeId), Filters.text(autoComplete.token))

                    val prodList = smartShopDBManager.searchProductTags(bson, 20)
                    call.respond(HttpStatusCode.OK, prodList)
                }

                post("v1/search/matchTags/") {
                    val autoComplete = call.receive<AutoComplete>()
                    withContext(Dispatchers.IO) {

                    }
                    val bson = Filters.and(Filters.eq("storeId",autoComplete.storeId), Filters.text(autoComplete.token))
                    val productTagBson = smartShopDBManager.searchProductTags(bson, 10)

                    val taggedProducts = productTagBson.map { tag ->
                        TaggedProducts(
                            storeId = tag.getString("storeId"),
                            productId = tag.getString("prodId").toInt(),
                            productFullName = tag.getString("prodFullNm"),
                        )
                    }
                    call.respond(HttpStatusCode.OK, taggedProducts)
                }

                post("v1/validate/") {
                    val shoppingList = call.receive<ShoppingList>()
                    shoppingList.items = shoppingList.items.distinctBy { it.prodId }

                    withContext(Dispatchers.IO) {
                        val productList = buildProductFromShoppingList(shoppingList);

                        //product - quantity map
                        val prodIdQntyMap = shoppingList.items.map { it.prodId to it.quantity }.toMap()
                        val filteredSize = productList.filter { prodIdQntyMap.containsKey(it.prodId) }.size

                        //Validity for all product list size
                        if (2*shoppingList.items.size == productList.size + filteredSize) {
                            val purchaseOrderNavigation = PurchaseOrderNavigation()


                            //Department wise group
                            val groupedByDept = productList.groupBy { it.deptInfo.deptNm }
                            val listOfDepts = mutableListOf<PurchaseProdDepartment>()
                            groupedByDept.forEach { grpByDeptProd ->
                                val purchaseProdDepartment = PurchaseProdDepartment()
                                purchaseProdDepartment.departmentName = grpByDeptProd.key


                                //Aisle wise group
                                val groupedByAisleNm = grpByDeptProd.value.groupBy { it.aisleInfo.aisleNm }
                                val listOfAisles = mutableListOf<PurchaseProdAisle>()
                                groupedByAisleNm.forEach { grpByAslNmProd ->
                                    val purchaseProdAisle = PurchaseProdAisle()
                                    purchaseProdAisle.aisleName = grpByAslNmProd.key


                                    //Group by Aisle Sequence
                                    val grpByAslRackSeq = grpByAslNmProd.value
                                        .sortedWith(compareBy { it.aisleInfo.aisleSeqNo })
                                        .groupBy { it.aisleInfo.aisleSeqNo }
                                    val listOfRacks = mutableListOf<PurchaseProdRack>()
                                    grpByAslRackSeq.forEach{ grpByAslRackSeqProd ->
                                        val purchaseProdRack = PurchaseProdRack()
                                        purchaseProdRack.rackSeq = grpByAslRackSeqProd.key


                                        //Group by Rack Sequence (section)
                                        val grpByAslRackSecSeq = grpByAslRackSeqProd.value.groupBy { it.aisleInfo.rackSecNm }
                                        val listOfSections = mutableListOf<PurchaseProdSection>()
                                        grpByAslRackSecSeq.forEach{ grpByAslRackSecSeqProd ->
                                            val purchaseProdSection = PurchaseProdSection()
                                            purchaseProdSection.sectionSeq = grpByAslRackSecSeqProd.key

                                            //Sort by section and get the product
                                            val listOfProduct = mutableListOf<PurchaseProduct>()
                                            val sortedProductList = grpByAslRackSecSeqProd.value
                                                .sortedWith(compareBy { it.aisleInfo.rackSeqNo })
                                            sortedProductList.forEach { prod ->
                                                val purchaseProduct = PurchaseProduct()
                                                purchaseProduct.productDisplayName = prod.prodFullNm
                                                purchaseProduct.productId = prod.prodId
                                                purchaseProduct.productQuantity = prodIdQntyMap[prod.prodId]
                                                purchaseProduct.productRackSeqNo = prod.aisleInfo.rackSeqNo
                                                listOfProduct.add(purchaseProduct)
                                            }
                                            purchaseProdSection.listOfProduct = listOfProduct

                                            listOfSections.add(purchaseProdSection)
                                            purchaseProdRack.listOfSection = listOfSections
                                        }

                                        listOfRacks.add(purchaseProdRack)
                                        purchaseProdAisle.listOfRack = listOfRacks
                                    }

                                    listOfAisles.add(purchaseProdAisle)
                                }

                                purchaseProdDepartment.listOfAisle = listOfAisles
                                listOfDepts.add(purchaseProdDepartment)
                            }

                            purchaseOrderNavigation.listOfDept = listOfDepts
                            call.respond(HttpStatusCode.OK, purchaseOrderNavigation)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Unable to proceed")
                        }
                    }
                }

                get("v1/search/store/{zip}") {
                    withContext(Dispatchers.IO) {
                        val zip = call.receiveParameters()["zip"]
                        call.respondText(""+zip)
                    }
                }

                post("v1/save/") {
                    call.respondText("Navigation API")
                }

            }
        }
    }

}


suspend fun buildProductFromShoppingList(shoppingList: ShoppingList): List<ProductInfo> {
    val products = mutableListOf<ProductInfo>()
    val storeId = shoppingList.storeId;

    shoppingList.items.forEachIndexed { idx, item ->
        val prodId = item.prodId
        val doc = smartShopDBManager.fetchProductInfo(prodId, storeId)

        if (doc != null && prodId == doc.getString("prodId") &&
            storeId == doc.getString("storeId")
        ) {
            products.add(
                ProductInfo(
                    storeId = storeId,
                    prodId = doc.getString("prodId"),
                    prodShortNm = doc.getString("prodShortNm"),
                    prodFullNm = doc.getString("prodFullNm"),
                    prodBrand = doc.getString("prodBrand"),
                    prodType = doc.getString("prodType"),

                    deptInfo = DepartmentInfo(
                        deptNm = (doc["department"] as Document).getString("deptNm"),
                        inStoreBannerNm = (doc["department"] as Document).getString("inStoreBannerNm"),
                    ),

                    aisleInfo = AisleInfo(
                        aisleNm = (doc["aisle"] as Document).getString("aisleNm"),
                        aisleSeqNo = (doc["aisle"] as Document).getString("aisleSeqNo").toInt(),
                        inStoreBannerNm = (doc["aisle"] as Document).getString("inStoreBannerNm"),
                        rackSeqNo = (doc["aisle"] as Document).getString("rackSeqNo").toInt(),
                        rackSecNm = (doc["aisle"] as Document).getString("rackSecNm"),
                    )
                )
            )
        }
    }
    return products
}

fun buildNavigationOrder(shoppingList: ShoppingList): Document {
    val orderId = UUID.randomUUID().toString()
    val order = Document()
    val products = mutableListOf<Document>()

    order.append("store_id", shoppingList.storeId)
    shoppingList.items.forEach {
        val product = Document()
        product.append("name", it.itemName)
        product.append("quantity", it.quantity)
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
            if (it.prodId.isNullOrEmpty().or(it.prodId.isNullOrBlank())) "UNKNOWN"
            else "KNOWN"
        }
}
