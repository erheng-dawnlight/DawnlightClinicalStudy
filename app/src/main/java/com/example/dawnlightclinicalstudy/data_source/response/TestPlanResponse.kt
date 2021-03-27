package com.example.dawnlightclinicalstudy.data_source.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestPlanResponse(
    @Json(name = "data") val data: List<TestPlanResponseData>,
) {

    @JsonClass(generateAdapter = true)
    data class TestPlanResponseData(
        @Json(name = "location") val location: String,
        @Json(name = "room") val room: String,
        @Json(name = "deviceInfo") val deviceInfo: Map<String, String>,
        @Json(name = "startTs") val startTs: Long,
        @Json(name = "endTs") val endTs: Long,
    )
}
