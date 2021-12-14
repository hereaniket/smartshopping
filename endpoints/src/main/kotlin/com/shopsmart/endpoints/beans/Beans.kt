package com.shopsmart.endpoints.beans

import kotlinx.serialization.Serializable

@Serializable
data class Item(val name: String, val quantity: Int)
@Serializable
data class ShoppingList(val store_name: String, val items: List<Item>)

@Serializable
data class AutoComplete(val token: String)