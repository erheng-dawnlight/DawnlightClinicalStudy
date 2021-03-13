package com.example.dawnlightclinicalstudy.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val useCase: LifeSignalUseCase,
    val activityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val selectedPatch: SingleEvent<JSONObject>? = null,
        val startHotspotService: SingleEvent<Unit>? = null,
        val buttonText: String = ""
    )

    val state = mutableStateOf(State())

    init {
        activityEventListener.lastDiscoveredPatchFlow
            .onEach {
                state.value = state.value.copy(
                    selectedPatch = SingleEvent(useCase.getConnectedPatchData())
                )
            }
            .launchIn(viewModelScope)

        activityEventListener.startHotspotServiceFlow
            .onEach {
                state.value = state.value.copy(
                    startHotspotService = SingleEvent(Unit)
                )
            }
            .launchIn(viewModelScope)
    }

    fun lifeSignalDataReceived(event: String, json: JSONObject) {
        useCase.onDataReceived(event, json)
    }
}