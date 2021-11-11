package com.shopsmart.backend.mongo.test

import com.shopsmart.backend.mongo.MongoConnectionManager
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.junit.Assert
import org.junit.Test

class MongoConnectionManagerTest {


    @Test
    fun testMongoConnection(){
        val mongoClient = MongoConnectionManager.init()
        val mongoDb = mongoClient.getDatabase("shopsmart")
        val mongoCollection = mongoDb.getCollection("sore")

        val doc = Document()
        doc.append("key1","value1")
        doc.append("key2","value2")
        doc.append("key3","value3")
        mongoCollection.insertOne(doc)

        val docs = mongoCollection.countDocuments()

        val result = mongoCollection.find()
            .filter(Document("key3","value3"))
            .first()

        Assert.assertNotNull(mongoClient)
        mongoClient.close()
    }
}