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
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


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
                    val bson =
                        Filters.and(Filters.eq("storeId", autoComplete.storeId), Filters.text(autoComplete.token))

                    val prodList = smartShopDBManager.searchProductTags(bson, 20)
                    call.respond(HttpStatusCode.OK, prodList)
                }

                post("v1/search/matchTags/") {
                    val autoComplete = call.receive<AutoComplete>()
                    withContext(Dispatchers.IO) {

                    }
                    val bson =
                        Filters.and(Filters.eq("storeId", autoComplete.storeId), Filters.text(autoComplete.token))
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

                        if (productList != null && productList.isNotEmpty()){
                            //product - quantity map
                            val prodIdQntyMap = shoppingList.items.map { it.prodId to it.quantity }.toMap()
                            val filteredSize = productList.filter { prodIdQntyMap.containsKey(it.prodId) }.size
                            //Validity for all product list size
                            if (2 * shoppingList.items.size == productList.size + filteredSize) {
                                val purchaseOrderNavigation = buildProductNavigation(productList,
                                    prodIdQntyMap, orderId = shoppingList.orderId,
                                    storeId = shoppingList.storeId)

                                //Process Order
                                val orderInfoDoc = smartShopDBManager.search(
                                    "order_info", Filters.eq("orderId", shoppingList.orderId), 1
                                )

                                val prodIdList = shoppingList.items.map { it.prodId.plus(":").plus(prodIdQntyMap[it.prodId]) }

                                if (orderInfoDoc != null && orderInfoDoc.isNotEmpty()) {
                                    val orderInfo = orderInfoDoc[0]
                                    val updatedProdList = Document().append("prodIdList",prodIdList)
                                    val orderId = orderInfo.getString("orderId")
                                    smartShopDBManager.updateOne(
                                        "order_info",
                                        Filters.eq("orderId", orderId), Document().append("\$set", updatedProdList)
                                    )
                                } else {
                                    smartShopDBManager.insertOne(
                                        "order_info", Document()
                                            .append("orderId", shoppingList.orderId)
                                            .append("storeId", shoppingList.storeId)
                                            .append("nameOnOrder", "")
                                            .append("emailId", "")
                                            .append("mobile", "")
                                            .append("totalAmt", 0.0)
                                            .append("prodIdList", prodIdList)
                                    )
                                }

                                call.respond(HttpStatusCode.OK, purchaseOrderNavigation)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Unable to proceed")
                            }
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "No product found!!!")
                        }
                    }
                }

                get("v1/search/store/{zip}") {
                    withContext(Dispatchers.IO) {
                        val zip = call.parameters["zip"]
                        val store = smartShopDBManager.search("store", Filters.eq("storeZp", zip), 5)
                        call.respond(HttpStatusCode.OK, store)
                    }
                }

                get("v1/search/order/{orderId}") {
                    withContext(Dispatchers.IO) {
                        val retrieveId = call.parameters["orderId"]
                        val orderDoc = smartShopDBManager.search("order_info", Filters.eq("orderId", retrieveId), 1)
                        lateinit var navigation: PurchaseOrderNavigation

                        if (orderDoc.isNotEmpty()){
                            val order = orderDoc[0]
                            val orderId = order.getString("orderId")
                            val storeId = order.getString("storeId")
                            val prodIdQntyList = order.getList("prodIdList", String::class.java)
                            val productIdList = prodIdQntyList.map { it.split(":")[0] }
                            val productQtyMap = prodIdQntyList.map{it.split(":")}
                                .associateBy({it[0]},{it[1].toInt()})

                            val productInfoList = buildProductFromProductList(productIdList, storeId)
                            if (productInfoList != null && productInfoList.isNotEmpty()){
                                navigation = buildProductNavigation(productInfoList, productQtyMap, orderId, storeId)
                            }
                        }
                        call.respond(HttpStatusCode.OK, navigation)
                    }
                }

                post("v1/email/") {
                    withContext(Dispatchers.IO) {
                        val email = call.receive<SendToEmail>()
                        val order = smartShopDBManager.search("order_info", Filters.eq("orderId", email.orderId), 1)

                        if (order != null && order.size == 1) {
                            val orderInfo = order[0]

                            val orderId = orderInfo.getString("orderId")
                            val storeId = orderInfo.getString("storeId")
                            val prodIdQntyList = orderInfo.getList("prodIdList", String::class.java)
                            val productIdList = prodIdQntyList.map { it.split(":")[0] }
                            val productQtyList = prodIdQntyList.map { it.split(":")[1] }

                            var body: String = "Order Id " + orderId.plus("\n")
                                .plus("Products...\n")

                            buildProductFromProductList(productIdList, storeId)?.forEachIndexed { idx, item ->
                                val txt = item.aisleInfo.aisleNm
                                .plus(item.aisleInfo.aisleSeqNo)
                                .plus("/")
                                .plus(item.aisleInfo.rackSeqNo)
                                .plus(" - ")
                                .plus(item.prodFullNm)
                                .plus(", Qty: "+productQtyList.get(idx))
                                .plus("\n")
                                body += txt
                            }
                            val link = "http://localhost:63342/ShopSmart/ShopSmart.frontend/index.html?retrieveId="+orderId
                            smartShopDBManager.updateOne(
                                "order_info",
                                Filters.eq("orderId", orderId), Document().append(
                                    "\$set",
                                    Document().append("emailId", email.emailId)
                                )
                            )

                            val subject = "SmartShopping :: Your order# $orderId"
                            val sent = sendEmail(subject, email.emailId, body.plus("\n\nClick on below link to retrieve your order\n"+link))
                            if (sent != null && sent.contentEquals("SCS")){
                                call.respond(HttpStatusCode.OK, Status("SCS", "SUCCESS", "Your shopping note is sent to "+email.emailId))
                            } else {
                                call.respond(HttpStatusCode.NoContent, Status("FAL", "UNABLE_TO_SEND_EMAIL", "Email was not delivered"))
                            }
                        } else {
                            call.respond(HttpStatusCode.NoContent, Status("FAL", "No_ORDER_EXIST", "The orderId you sent does not return any value"))
                        }
                    }
                }

            }
        }
    }

}

suspend fun buildProductFromProductList(productIdList: List<String>, storeId: String): List<ProductInfo>? {
    if (productIdList == null || productIdList.isEmpty()) {
        return null
    }

    val products = mutableListOf<ProductInfo>()
    productIdList.forEach { item ->
        val doc = smartShopDBManager.fetchProductInfo(item, storeId)

        if (doc != null && item == doc.getString("prodId") &&
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

suspend fun buildProductFromShoppingList(shoppingList: ShoppingList): List<ProductInfo>? {
    return buildProductFromProductList(shoppingList.items.map { it.prodId }, shoppingList.storeId)
}

suspend fun buildProductNavigation(productList: List<ProductInfo>, prodIdQntyMap: Map<String, Int>,orderId: String, storeId: String): PurchaseOrderNavigation {
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
            grpByAslRackSeq.forEach { grpByAslRackSeqProd ->
                val purchaseProdRack = PurchaseProdRack()
                purchaseProdRack.rackSeq = grpByAslRackSeqProd.key


                //Group by Rack Sequence (section)
                val grpByAslRackSecSeq =
                    grpByAslRackSeqProd.value.groupBy { it.aisleInfo.rackSecNm }
                val listOfSections = mutableListOf<PurchaseProdSection>()
                grpByAslRackSecSeq.forEach { grpByAslRackSecSeqProd ->
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

    return PurchaseOrderNavigation(
        listOfDept = listOfDepts, order = PurchaseOrder(
            orderId = orderId,
            storeId = storeId,
        )
    )
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

suspend fun sendEmail(subject: String, recipient: String, body: String): String {

    return withContext(Dispatchers.Default) {
        val username = ""
        val password = ""

        val prop = Properties()
        prop["mail.smtp.host"] = "smtp.gmail.com"
        prop["mail.smtp.port"] = "587"
        prop["mail.smtp.auth"] = "true"
        prop["mail.smtp.starttls.enable"] = "true"


        val session = Session.getInstance(prop,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        return@withContext try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipient)
            )
            message.subject = subject
            message.setText(body)
            Transport.send(message)
            "SCS"
        } catch (e: MessagingException) {
            "FAL"
        }
    }
}
