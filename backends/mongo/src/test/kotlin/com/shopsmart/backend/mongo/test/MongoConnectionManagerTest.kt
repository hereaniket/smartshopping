package com.shopsmart.backend.mongo.test

import com.shopsmart.backend.mongo.MongoConnectionManager
import org.junit.Test

class MongoConnectionManagerTest {


    @Test
    fun testMongoConnection(){
        MongoConnectionManager.ini()
    }
}