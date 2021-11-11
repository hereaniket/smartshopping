package com.shopsmart.backend.mongo

import com.mongodb.client.MongoClients

object MongoConnectionManager {

    fun ini(){
        val connStr = "mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000";

        val mongoClient = MongoClients.create(connStr)
        mongoClient.listDatabases().forEach {
            print(it.toString())
        }
        print(mongoClient.toString())
    }
}