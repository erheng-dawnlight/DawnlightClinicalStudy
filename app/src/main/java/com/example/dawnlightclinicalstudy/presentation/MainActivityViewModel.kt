package com.example.dawnlightclinicalstudy.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalDataParsingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val useCase: LifeSignalDataParsingUseCase,
    val activityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val selectedPatch: SingleEvent<JSONObject>? = null,
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
    }

    fun lifeSignalDataReceived(event: String, json: JSONObject) {
        useCase.onDataReceived(event, json)
    }
}