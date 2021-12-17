package com.shopsmart.backend.mongo.test

import com.mongodb.client.model.Filters.eq
import com.shopsmart.backend.mongo.async.AsyncConnectionManager
import org.junit.Test
import reactor.core.publisher.toMono
import java.util.concurrent.Flow

class AsyncTest {
    @Test
    fun connectionTest() {
        /*val client = AsyncConnectionManager.init()

        val collection = client.getDatabase("smartshopping").getCollection("product_tags")
        collection.find(eq("product_tag","brea"))
            .subscribe(Flow.Subscriber<Doc>)*/
    }
}
