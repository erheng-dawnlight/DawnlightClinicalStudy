package com.example.dawnlightclinicalstudy.data

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONObject

class LifeSignalRepository {

    val lastDiscoveredPatchChannel = ConflatedBroadcastChannel<JSONObject>()

    val lastDiscoveredPatchFlow: Flow<JSONObject> = lastDiscoveredPatchChannel
        .asFlow()
        .distinctUntilChanged()

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }
}