package com.shopsmart.backend.service.db

import com.mongodb.MongoClientSettings
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters.*
import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.conversions.Bson

class SmartShopDBManager() {
    private val mongoClient = MongoConnectionManager.init()
    private val mongoDb = mongoClient.getDatabase("smartshopping")

    fun searchProductTags(token: String, store_id: String): FindIterable<Document> {
        return mongoDb.getCollection("product_tags")
            .find(and(eq("tags", token), eq("storeId", store_id)))
            .limit(10)
    }

    suspend fun updateOne(collectionNm: String, bson: Bson, updatedDoc: Bson): Boolean {
        return withContext(Dispatchers.Default) {
            if (mongoClient != null) {
                mongoDb.getCollection(collectionNm)
                    .updateOne(bson, updatedDoc)
                    .wasAcknowledged()
            }
            false
        }
    }

    suspend fun insertOne(collectionNm: String, data: Document) {
        return withContext(Dispatchers.Default) {
            mongoDb.getCollection(collectionNm)
                .insertOne(data)

        }
    }

    suspend fun <T : Any> search(collectionNm: String, collectionType: T, bson: Bson, limit: Int): List<T> {
        return withContext(Dispatchers.Default) {
            val items = mongoDb.getCollection(collectionNm, collectionType::class.java)
                .find(bson)
                .limit(limit)
               if (items.first() != null) {
                   items.toList()
               }else{
                   listOf()
               }
        }
    }

    suspend fun search(collectionNm: String, bson: Bson, limit: Int): List<Document> {
        return withContext(Dispatchers.Default) {
            mongoDb.getCollection(collectionNm)
                .find(bson)
                .limit(limit)
                .toList()
        }
    }

    suspend fun searchProductTags(bson: Bson, limit: Int): List<Document> {
        return withContext(Dispatchers.Default) {
            mongoDb.getCollection("product_tags")
                .find(bson)
                .limit(limit)
                .toList()
        }
    }

    suspend fun fetchProductInfo(prodId: String, storeId: String): Document? {
        return withContext(Dispatchers.Default) {
            mongoDb.getCollection("store")
                .find(and(eq("prodId", prodId), eq("storeId", storeId)))
                .firstOrNull()
        }
    }
}