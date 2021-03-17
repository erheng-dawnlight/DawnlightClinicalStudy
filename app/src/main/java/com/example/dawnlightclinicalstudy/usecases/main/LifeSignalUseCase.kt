package com.example.dawnlightclinicalstudy.usecases.main

import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.data_source.response.SensorDataResponse
import com.example.dawnlightclinicalstudy.domain.LifeSignalEvent
import com.squareup.moshi.Moshi
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

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
                        lifeSignalRepository.onStatus(true)
                    }
                }
            }
            LifeSignalEvent.ON_FILTERED_DATA -> {
                val sensorData: JSONObject = json.getJSONObject("SensorData")

                val ecg0Arr: JSONArray? = if (sensorData.has("ECG_CH_D")) {
                    sensorData.getJSONArray("ECG_CH_D")
                } else {
                    null
                }
                val ecg0 = ArrayList<Int>()

                if (ecg0Arr != null) {
                    for (j in 0 until ecg0Arr.length()) {
                        val elem = ecg0Arr.getInt(j)
                        if (elem != -524288) {
                            ecg0.add(elem)
                        } else {
                            ecg0.add(0)
                        }
                    }
                }

                val ecg1Arr: JSONArray? = if (sensorData.has("ECG_CH_A")) {
                    sensorData.getJSONArray("ECG_CH_A")
                } else {
                    null
                }
                val ecg1 = ArrayList<Int>()

                if (ecg1Arr != null) {
                    for (j in 0 until ecg1Arr.length()) {
                        val elem = ecg1Arr.getInt(j)
                        if (elem != -524288) {
                            ecg1.add(elem)
                        } else {
                            ecg1.add(0)
                        }
                    }
                }

                val respArr: JSONArray? = if (sensorData.has("Respiration")) {
                    sensorData.getJSONArray("Respiration")
                } else {
                    null
                }
                val resp = ArrayList<Int>()

                if (respArr != null) {
                    for (j in 0 until respArr.length()) {
                        val elem = respArr.getInt(j)
                        if (elem != -524288) {
                            resp.add(elem)
                        } else {
                            resp.add(0)
                        }
                    }
                }

                if (sensorData.has("HR")) {
                    val hrArray = sensorData.getJSONArray("HR")
                    val hr = ArrayList<Int>()
                    for (j in 0 until hrArray.length()) {
                        hr.add(hrArray.getInt(j))
                    }
                }

                if (sensorData.has("RR_OUT")) {
                    val rrArray = sensorData.getJSONArray("RR_OUT")
                    val rr = ArrayList<Int>()
                    for (j in 0 until rrArray.length()) {
                        rr.add(rrArray.getInt(j))
                    }
                }

                if (sensorData.has("TEMPERATURE")) {
                    var TEMP_CODE_PER_DEG = 100.0
                    if (sensorData.has("TEMP_CODE_PER_DEG")) {
                        TEMP_CODE_PER_DEG = sensorData.getDouble("TEMP_CODE_PER_DEG")
                    }
                    val tempArray = sensorData.getJSONArray("Temperature")
                    val temp =
                        (tempArray.getInt(tempArray.length() - 1) / TEMP_CODE_PER_DEG).toFloat()
                    val df = DecimalFormat("#.0")
                    var str = "-"
                    if (temp > 0) {
                        str = df.format(temp.toDouble())
                    }
                }
                lifeSignalRepository.onFilteredData(Triple(ecg0, ecg1, resp))
            }
        }
    }

    fun getConnectedPatchDataForConfiguration(): LifeSignalUseCaseCallback {
        lifeSignalRepository.lastDiscoveredPatchChannel.value.let {
            val patchStatus = (it.get("Capability") as JSONObject).getInt("PatchStatus")
            return when {
                patchStatus >= 55 -> {
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

    data class SelectPatch(
        val json: JSONObject, val shouldConfigure: Boolean
    ) : LifeSignalUseCaseCallback()
}