package com.shopsmart.endpoints.beans.api

import kotlinx.serialization.Serializable

@Serializable
data class Item(val itemName: String, val quantity: Int, val prodId: String)

@Serializable
data class ShoppingList(val orderId: String, val storeId: String, var items: List<Item>)

@Serializable
data class AutoComplete(val token: String, val storeId: String)

data class SendToEmail(val emailId: String, val orderId: String)

data class Status(val statusCd: String, val errorCode: String?=null, val errorDesc: String?=null)

data class TaggedProducts(
    val storeId: String,
    val productId: Int,
    val productFullName: String,
    var tags: List<String>? = null,
)

data class PurchaseOrder(
    val orderId: String,
    val storeId: String,
    var nameOnOrder: String? = null,
    var maskedEmailOnOrder: String? = null,
    var maskedPhoneOnOrder: String? = null,
    var totalAmount: Double? = null,
)

data class PurchaseOrderNavigation(
    var order: PurchaseOrder? = null,
    var listOfDept: List<PurchaseProdDepartment>? = null,
)

data class PurchaseProdDepartment(
    var departmentName: String? = null,
    var listOfAisle: List<PurchaseProdAisle>? = null,
)

data class PurchaseProdAisle(
    var aisleName: String? = null,
    var listOfRack: List<PurchaseProdRack>? = null,
)

data class PurchaseProdRack(
    var rackSeq: Int? = null,
    var listOfSection: List<PurchaseProdSection>? = null,
)

data class PurchaseProdSection(
    var sectionSeq: String? = null,
    var listOfProduct: List<PurchaseProduct>? = null,
)

data class PurchaseProduct(
    var productId: String? = null,
    var productDisplayName: String? = null,
    var productQuantity: Int? = 1,
    var productRackSeqNo: Int? = null,
    val unitPrice: Double? = 0.0,
    val isAltProduct: Boolean? = false,
    val originalProductName: String? = null,
    val originalProductPrice: Double? = 0.0,
)