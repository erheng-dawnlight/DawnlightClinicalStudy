package com.example.dawnlightclinicalstudy.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalUseCase
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalUseCaseCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val useCase: LifeSignalUseCase,
    val activityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val selectPatch: SingleEvent<Pair<JSONObject, Boolean>>? = null,
        val startHotspotService: SingleEvent<Unit>? = null,
        val errorText: SingleEvent<StringWrapper>? = null,
        val buttonText: StringWrapper = StringWrapper.Text("")
    )

    val state = mutableStateOf(State())

    init {
        activityEventListener.lastDiscoveredPatchFlow
            .onEach {
                useCase.getConnectedPatchDataForConfiguration().let {
                    when (it) {
                        is LifeSignalUseCaseCallback.SelectPatch -> {
                            state.value = state.value.copy(
                                selectPatch = SingleEvent(it.json to it.shouldConfigure)
                            )
                        }
                        is LifeSignalUseCaseCallback.Error -> {
                            state.value = state.value.copy(
                                errorText = SingleEvent(it.error)
                            )
                        }
                    }
                }
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
        viewModelScope.launch(Dispatchers.IO) {
            useCase.onDataReceived(event, json)
        }
    }
}