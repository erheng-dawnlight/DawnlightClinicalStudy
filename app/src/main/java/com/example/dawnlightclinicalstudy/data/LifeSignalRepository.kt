package com.example.dawnlightclinicalstudy.data

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class LifeSignalRepository {

    var subjectId: String = ""

    val lastDiscoveredPatchChannel = ConflatedBroadcastChannel<JSONObject>()
    val lastDiscoveredPatchFlow: Flow<String> = lastDiscoveredPatchChannel
        .asFlow()
        .map { it.getJSONObject("PatchInfo").getString("PatchId") }

    val statusChannel = ConflatedBroadcastChannel<Boolean>()
    val statusFlow: Flow<Boolean> = statusChannel
        .asFlow()

    val filteredDataChannel =
        ConflatedBroadcastChannel<Triple<ArrayList<Int>, ArrayList<Int>, ArrayList<Int>>>()
    val filteredDataFlow: Flow<Triple<ArrayList<Int>, ArrayList<Int>, ArrayList<Int>>> =
        filteredDataChannel
            .asFlow()

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }

    fun onStatus(isReady: Boolean) {
        statusChannel.offer(isReady)
    }

    fun onFilteredData(triple: Triple<ArrayList<Int>, ArrayList<Int>, ArrayList<Int>>) {
        filteredDataChannel.offer(triple)
    }
}