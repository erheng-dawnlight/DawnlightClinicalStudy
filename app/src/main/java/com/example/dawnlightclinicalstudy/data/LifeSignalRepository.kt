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

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }
}