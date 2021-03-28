package com.example.dawnlightclinicalstudy.data

import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.example.dawnlightclinicalstudy.data_source.request.LifeSignalRequest
import com.example.dawnlightclinicalstudy.domain.LifeSignalFilteredData
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class LifeSignalRepository(
    private val retrofitService: RetrofitService,
) {

    var patchId = ""
    var filteredDataList = mutableListOf<LifeSignalFilteredData>()

    val lastDiscoveredPatchChannel = ConflatedBroadcastChannel<JSONObject>()
    val lastDiscoveredPatchFlow: Flow<String> = lastDiscoveredPatchChannel
        .asFlow()
        .map {
            patchId = it.getJSONObject("PatchInfo").getString("PatchId")
            patchId
        }

    val statusChannel = ConflatedBroadcastChannel<Int>()
    val statusFlow: Flow<Int> = statusChannel.asFlow()

    val filteredDataChannel = ConflatedBroadcastChannel<LifeSignalFilteredData>()
    val filteredDataFlow: Flow<LifeSignalFilteredData> = filteredDataChannel.asFlow()

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }

    fun onStatus(patchStatus: Int) {
        statusChannel.offer(patchStatus)
    }

    fun onFilteredData(data: LifeSignalFilteredData) {
        filteredDataList.add(data)
        filteredDataChannel.offer(data)
    }

    suspend fun uploadSignal(
        deviceId: String,
        values: List<LifeSignalRequest>,
    ) {
        retrofitService.uploadSignal(deviceId, values)
    }
}