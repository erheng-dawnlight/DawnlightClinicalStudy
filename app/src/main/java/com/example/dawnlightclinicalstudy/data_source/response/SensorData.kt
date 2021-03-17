package com.example.dawnlightclinicalstudy.data_source.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SensorDataResponse(
    @Json(name = "SensorData") val sensorData: SensorData,
)

@JsonClass(generateAdapter = true)
data class SensorData(
    @Json(name = "Seq") val seq: Int,
//    @Json(name = "PatchId") val patchId: String,
//    @Json(name = "ECG0") val ecg0: List<Int>,
//    @Json(name = "ECG1") val ecg1: List<Int>,
//    @Json(name = "Accel") val accel: List<Int>,
//    @Json(name = "Respiration") val respiration: List<Int>,
//    @Json(name = "vBat") val vBat: Int,
//    @Json(name = "IAGain") val iAGain: Int,
//    @Json(name = "LeadStatus") val leadStatus: Int,
//    @Json(name = "RLDInformation") val rLDInformation: Int,
//    @Json(name = "TsECG") val tsECG: Int,
//    @Json(name = "ACCEL") val aCCEL: List<Int>,
)
