package com.example.dawnlightclinicalstudy.data

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONObject

class LifeSignalRepository {

    var subjectId: String = ""

    private val lastDiscoveredPatchChannel = ConflatedBroadcastChannel<JSONObject>()
    val lastDiscoveredPatchFlow: Flow<JSONObject> = lastDiscoveredPatchChannel
        .asFlow()
        .distinctUntilChanged()

    fun onDiscoveredPatch(jsonObject: JSONObject) {
        lastDiscoveredPatchChannel.offer(jsonObject)
    }
}