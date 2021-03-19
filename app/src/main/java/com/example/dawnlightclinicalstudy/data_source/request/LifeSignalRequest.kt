package com.example.dawnlightclinicalstudy.data_source.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LifeSignalRequest(
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "batch_result") val batchResult: List<LifeSignalPatchData>,
) {
    @JsonClass(generateAdapter = true)
    data class LifeSignalPatchData(
        @Json(name = "current_time") val currentTime: Long,
        @Json(name = "ECG_CH_A") val eCGAs: List<Int>,
        @Json(name = "ECG_CH_D") val eCGDs: List<Int>,
    )
}