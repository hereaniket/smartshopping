package com.shopsmart.endpoints.beans

import kotlinx.serialization.Serializable

@Serializable
data class Item(val name: String, val quantity: Int, val prodId: String)
@Serializable
data class ShoppingList(val store_name: String, val items: List<Item>)

@Serializable
data class AutoComplete(val token: String, val store_name: String)

@Serializable
data class Product(val store_name: String, val prod_id: Int, val product_full_name: String, val product_tag: List<String>)
