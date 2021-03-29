package com.example.dawnlightclinicalstudy.data_source.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenCloseSessionRequest(
    @Json(name = "deviceIds") val deviceIds: List<String>,
    @Json(name = "position") val position: String,
    @Json(name = "ts") val ts: Long,
    @Json(name = "userId") val subjectId: String,
)