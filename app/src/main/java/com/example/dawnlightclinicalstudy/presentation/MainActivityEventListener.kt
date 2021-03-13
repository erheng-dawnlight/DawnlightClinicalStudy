package com.example.dawnlightclinicalstudy.presentation

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class MainActivityEventListener {

    private val patchButtonClickedChannel = BroadcastChannel<Unit>(1)

    val lastDiscoveredPatchFlow: Flow<Unit> = patchButtonClickedChannel.asFlow()

    fun onPatchSelected() {
        patchButtonClickedChannel.offer(Unit)
    }
}