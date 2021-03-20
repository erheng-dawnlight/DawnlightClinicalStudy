package com.example.dawnlightclinicalstudy.data

import com.example.dawnlightclinicalstudy.domain.LifeSignalFilteredData
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class LifeSignalRepository {

    var subjectId = ""
    var filteredDataList = mutableListOf<LifeSignalFilteredData>()

    val lastDiscoveredPatchChannel = ConflatedBroadcastChannel<JSONObject>()
    val lastDiscoveredPatchFlow: Flow<String> = lastDiscoveredPatchChannel
        .asFlow()
        .map { it.getJSONObject("PatchInfo").getString("PatchId") }

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
}