package com.shopsmart.backend.mongo.sync

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

object MongoConnectionManager {

    fun init(): MongoClient{
        val connStr = "mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000";
        return MongoClients.create(connStr)
    }
}