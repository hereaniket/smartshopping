package com.shopsmart.backend.service.db

import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.TextSearchOptions
import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson

class SmartShopDBManager() {
    private val mongoClient = MongoConnectionManager.init()
    private val mongoDb = mongoClient.getDatabase("smartshopping")

    fun searchProductTags(token: String, store_id: String): FindIterable<Document> {
        return mongoDb.getCollection("product_tags")
            .find(and(eq("tags", token), eq("storeId", store_id)))
            .limit(10)
    }

    fun saveNavigationOrder(order: Document) {
        if(mongoClient != null) {
            val storeCollection = mongoDb.getCollection("store")
            storeCollection.insertOne(order)
        }
    }

    suspend fun searchProductTags(bson: Bson, limit: Int): List<Document> {
        return withContext(Dispatchers.Default){
            mongoDb.getCollection("product_tags")
                .find(bson)
                .limit(limit)
                .toList()
        }
    }

    suspend fun fetchProductInfo(prodId: String, storeId: String): Document?{
        return withContext(Dispatchers.Default) {
            mongoDb.getCollection("store")
                .find(and(eq("prodId", prodId), eq("storeId", storeId)))
                .firstOrNull()
        }
    }
}