package com.shopsmart.backend.mongo.async

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients

object AsyncConnectionManager {
    const val connStr = "mongodb://localhost";

    /*
    standalone localhost connection
     */
    fun init(): MongoClient {
        lateinit var mongoClient: MongoClient
        try {
            mongoClient = MongoClients.create(ConnectionString(connStr))
        }catch (e:Exception){
            println("Error $e")
        }

        return mongoClient
    }

}

fun main() {
    AsyncConnectionManager.init()
}