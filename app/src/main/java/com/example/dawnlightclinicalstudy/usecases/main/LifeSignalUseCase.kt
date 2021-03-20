package com.example.dawnlightclinicalstudy.usecases.main

import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.data_source.response.SensorDataResponse
import com.example.dawnlightclinicalstudy.domain.LifeSignalEvent
import com.example.dawnlightclinicalstudy.domain.LifeSignalFilteredData
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.squareup.moshi.Moshi
import org.json.JSONArray
import org.json.JSONObject

class LifeSignalUseCase(
    val lifeSignalRepository: LifeSignalRepository
) {

    val moshi = Moshi.Builder().build().adapter(SensorDataResponse::class.java)

    fun onDataReceived(eventString: String, json: JSONObject) {
        when (val event = LifeSignalEvent.fromString(eventString)) {
            LifeSignalEvent.ON_DISCOVERY -> {
                lifeSignalRepository.onDiscoveredPatch(json)
            }
            LifeSignalEvent.ON_STATUS -> {
                if (json.getString("status") == "command") {
                    if (json.getString("command") == "configure") {
//                        lifeSignalRepository.onStatus(true)
                    }
                }
            }
            LifeSignalEvent.ON_FILTERED_DATA -> {
                val sensorData: JSONObject = json.getJSONObject("SensorData")
                val ecg0 = ArrayList<Int>()
                if (sensorData.has("ECG_CH_D")) {
                    val array = sensorData.getJSONArray("ECG_CH_D")
                    for (i in 0 until array.length()) {
                        ecg0.add(array.getInt(i))
                    }
                }
                val ecg1 = ArrayList<Int>()
                if (sensorData.has("ECG_CH_A")) {
                    val array = sensorData.getJSONArray("ECG_CH_A")
                    for (i in 0 until array.length()) {
                        ecg1.add(array.getInt(i))
                    }
                }
                lifeSignalRepository.onFilteredData(
                    LifeSignalFilteredData(ecg0, ecg1, System.currentTimeMillis())
                )
            }
        }
    }

    fun getConnectedPatchDataForConfiguration(): LifeSignalUseCaseCallback {
        lifeSignalRepository.lastDiscoveredPatchChannel.value.let {
            val patchStatus = (it.get("Capability") as JSONObject).getInt("PatchStatus")
            return when {
                patchStatus >= 55 -> {
                    lifeSignalRepository.onStatus(patchStatus)
                    LifeSignalUseCaseCallback.Nothing
                }
                patchStatus >= 43 -> {
                    LifeSignalUseCaseCallback.SelectPatch(it, false)
                }
                else -> {
                    val configJson = it.getJSONObject("ConfigurePatch")
                    configJson.put("PatchLife", 1440)
                    val ecgSps: Int = Integer.valueOf(8)
                    configJson.put(
                        "ECGChSps",
                        JSONArray(arrayOf<Any>(ecgSps, ecgSps, 0, 0, 0, 0, 0, 0))
                    )
                    it.put("ConfigurePatch", configJson)
                    LifeSignalUseCaseCallback.SelectPatch(it, true)
                }
            }
        }
    }
}

sealed class LifeSignalUseCaseCallback {
    object Nothing : LifeSignalUseCaseCallback()

    data class Error(
        val error: StringWrapper,
    ) : LifeSignalUseCaseCallback()

    data class SelectPatch(
        val json: JSONObject, val shouldConfigure: Boolean
    ) : LifeSignalUseCaseCallback()
}