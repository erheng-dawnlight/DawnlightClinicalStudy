package com.example.dawnlightclinicalstudy.data_source.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LifeSignalRequest(
    @Json(name = "component") val component: String,
    @Json(name = "ts") val currentTime: Long,
    @Json(name = "dataType") val dataType: String,
    @Json(name = "values") val values: LifeSignalRequestData,
) {
    @JsonClass(generateAdapter = true)
    data class LifeSignalRequestData(
        @Json(name = "ECG_CH_A") val eCGAs: List<Int>,
        @Json(name = "ECG_CH_D") val eCGDs: List<Int>,
        @Json(name = "HR") val hrs: List<Int>,
    )
}
