package com.shopsmart.backend.mongo.test

import com.mongodb.client.model.Filters
import com.shopsmart.backend.mongo.sync.MongoConnectionManager
import org.bson.Document
import org.bson.conversions.Bson
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

fun main() {

    // INSERT FROM FILE ## separated
    dataUploaderTest("/Users/aniketpathak/Documents/Projects/ShopSmart/backends/mongo/src/test/resources/data/sample.txt")

    // TO DELETE SOMETHING
    /*val deleted = deleteMany(Filters.eq("storeId", "walmart001"), "store", "smartshopping")
    println("deleted ?$deleted")*/
}

fun dataUploaderTest(fileName: String) {
    var products = mutableListOf<Document>()
    var prodTags = mutableListOf<Document>()
    try {
        val reader = BufferedReader(FileReader(fileName));

        reader.lines()
            .forEach { item ->
                val split = item.split("##")

                val prod = Document()
                prod.append("storeId", split[0])
                prod.append("prodId", split[1])
                prod.append("prodShortNm", split[2])
                prod.append("prodFullNm", split[3])
                prod.append("prodBrand", split[4])
                prod.append("prodType", split[5])

                val department = Document()
                department.append("deptNm", split[6])
                department.append("inStoreBannerNm", split[7])
                prod.append("department", department)

                val aisle = Document()
                aisle.append("inStoreBannerNm", split[8])
                aisle.append("aisleNm", split[9])
                aisle.append("aisleSeqNo", split[10])
                aisle.append("rackSeqNo", split[11])
                aisle.append("rackSecNm", split[12])
                prod.append("aisle", aisle)



                val prodTag = Document()
                prodTag.append("storeId", split[0])
                prodTag.append("prodId", split[1])
                prodTag.append("prodFullNm", split[3])

                if (split[13] != null) {
                    val tagsArr = split[13].split(",")
                        .map { it.trim().lowercase() }
                        .flatMap {
                            var counter = 1
                            val list = mutableListOf<String>()
                            while (counter <= it.length) {
                                list.add(it.substring(0, counter))
                                counter++
                            }
                            list
                        }
                    prodTag.append("tags", tagsArr)
                }

                products.add(prod)
                prodTags.add(prodTag)
            }

        val mongoClient = MongoConnectionManager.init()
        val mongoDb = mongoClient.getDatabase("smartshopping")

        mongoDb.getCollection("store").insertMany(products)

        mongoDb.getCollection("product_tags").insertMany(prodTags)

        reader.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun deleteMany(bson: Bson, collName: String, dbName: String): Boolean {
    val mongoClient = MongoConnectionManager.init()
    val mongoDb = mongoClient.getDatabase(dbName)
    val mongoCollection = mongoDb.getCollection(collName)

    return mongoCollection.deleteMany(bson)
        .wasAcknowledged()
}