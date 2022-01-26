package com.shopsmart.backend.mongo.sync

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider

object MongoConnectionManager {

    fun init(connectionString: String = "mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000"): MongoClient{

        val pojoCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        val settings: MongoClientSettings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry)
            .applyConnectionString(ConnectionString(connectionString))
            .build()

        return MongoClients.create(settings)
    }
}