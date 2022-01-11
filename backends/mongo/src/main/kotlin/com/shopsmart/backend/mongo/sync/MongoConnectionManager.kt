package com.shopsmart.backend.mongo.sync

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

object MongoConnectionManager {

    fun init(connectionString: String = "mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000"): MongoClient{
        return MongoClients.create(connectionString)
    }
}