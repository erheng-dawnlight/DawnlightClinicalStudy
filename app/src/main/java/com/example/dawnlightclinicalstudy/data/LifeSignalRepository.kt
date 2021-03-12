package com.example.dawnlightclinicalstudy.data

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONObject

class LifeSignalRepository {

    private val lastDiscoveredPatchChannel = BroadcastChannel<JSONObject>(1)

    val lastDiscoveredPatchFlow: Flow<JSONObject> = lastDiscoveredPatchChannel
        .asFlow()
        .distinctUntilChanged()

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }
}