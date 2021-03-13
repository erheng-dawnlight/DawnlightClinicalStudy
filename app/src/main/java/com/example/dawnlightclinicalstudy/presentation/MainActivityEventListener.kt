package com.example.dawnlightclinicalstudy.presentation

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class MainActivityEventListener {

    private val patchButtonClickedChannel = ConflatedBroadcastChannel<Unit>()
    val lastDiscoveredPatchFlow: Flow<Unit> = patchButtonClickedChannel.asFlow()

    private val startHotspotServiceChannel = ConflatedBroadcastChannel<Unit>()
    val startHotspotServiceFlow: Flow<Unit> = startHotspotServiceChannel.asFlow()

    fun onPatchSelected() {
        patchButtonClickedChannel.offer(Unit)
    }

    fun startHotspotService() {
        startHotspotServiceChannel.offer(Unit)
    }
}