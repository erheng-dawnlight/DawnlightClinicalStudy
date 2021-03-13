package com.example.dawnlightclinicalstudy.usecases.main

import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.domain.LifeSignalEvent
import org.json.JSONObject

class LifeSignalDataParsingUseCase(
    val lifeSignalRepository: LifeSignalRepository
) {

    fun onDataReceived(eventString: String, json: JSONObject) {
        when (val event = LifeSignalEvent.fromString(eventString)) {
            LifeSignalEvent.ON_DISCOVERY -> {
                lifeSignalRepository.onDiscoveredPatch(json)
            }
        }
    }

    fun getConnectedPatchData(): JSONObject {
        return lifeSignalRepository.lastDiscoveredPatchChannel.value
    }
}