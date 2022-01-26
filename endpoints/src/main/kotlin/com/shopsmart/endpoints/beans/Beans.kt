
package com.shopsmart.endpoints.beans
/*
import kotlinx.serialization.Serializable

*/
/*@Serializable
data class Item(val name: String, val quantity: Int, val prodId: String)

@Serializable
data class ShoppingList(val storeId: String, val items: List<Item>)

@Serializable
data class AutoComplete(val token: String, val store_id: String)*//*


data class IsleInfo(
    val isle_name: String,
    val isle_display_name: String,
    val isle_number: Int,
    val section_number: Int
)

data class TaggedProducts(
    val store_id: String,
    val prod_id: Int,
    val product_full_name: String,
    val product_tag: List<String>
)

*/
/*
Entity for Product
 *//*

data class Product(
    val store_id: String,
    val prod_id: String,
    val product_full_name: String,
    val product_short_name: String,
    val product_brand: String,
    val product_section: String,
    val product_type: String,
    val isleInfo: IsleInfo
)

*/
/*
Entity for Order
 *//*

data class Order(
    val orderId: String,
    val orderTs: String,
    val totalAmount: Double,
    val productIdList: List<String>,
)

*/
/**
 * UI related Beans
 *//*

data class PurchaseNavigationInfo(
    val order: Order,
    val purchaseDepartments: List<PurchaseDepartmentInfo>
)

data class PurchaseDepartmentInfo(
    val departmentName: String,
    val productPurchaseList: List<PurchaseProductInfo>
)

data class PurchaseProductInfo(
    val productId: String,
    val product_name_on_rack: String,
    val quantity: Int,
    val unitePrice: Double,
    val purchaseIsleInfo: PurchaseIsleInfo
)

data class PurchaseIsleInfo(
    val isleDisplayName: String,
    val isleInfo: IsleInfo,
)*/
