package com.shopsmart.endpoints.beans.entity

data class ProductInfo(
    val storeId: String,
    val prodId: String,
    val prodShortNm: String,
    val prodFullNm: String,
    val prodBrand: String,
    val prodType: String,
    val deptInfo: DepartmentInfo,
    val aisleInfo: AisleInfo,
)

data class DepartmentInfo(
    val deptNm: String,
    val inStoreBannerNm: String,
)

data class AisleInfo(
    val aisleNm: String,
    val aisleSeqNo: Int,
    val inStoreBannerNm: String,
    val rackSeqNo: Int,
    val rackSecNm: String,
)

data class OrderInfo(
    val orderId: String,
    val storeId: String,
    val nameOnOrder: String,
    val emailId: String,
    val mobile: String,
    val totalAmt: Double,
    val prodIdList: List<String>,
)