package com.shopsmart.backend.service.db

import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters.*
import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson

class SmartShopDBManager() {
    private val mongoClient = MongoConnectionManager.init()
    private val mongoDb = mongoClient.getDatabase("smartshopping")

    fun searchProductTags(token: String, store_name: String): FindIterable<Document> {
        return mongoDb.getCollection("product_tags")
            .find(and(eq("product_tag", token), eq("store_name", store_name)))
            .limit(10)
    }

    fun saveNavigationOrder(order: Document) {
        if(mongoClient != null) {
            val storeCollection = mongoDb.getCollection("store")
            storeCollection.insertOne(order)
        }
    }

    suspend fun searchUnknownProducts(text: String): List<Document> {
        return withContext(Dispatchers.Default){
            mongoDb.getCollection("product_tags")
                .find(text(text))
                .limit(5)
                .toList()
        }
    }
}