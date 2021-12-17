package com.shopsmart.backend.mongo.test

import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import org.bson.Document
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
            .filter(Document("order_id","5b127b3e-edb7-4141-9dcc-3f84c5323af3"))
            .first()

        Assert.assertNotNull(mongoClient)
        mongoClient.close()
    }
}