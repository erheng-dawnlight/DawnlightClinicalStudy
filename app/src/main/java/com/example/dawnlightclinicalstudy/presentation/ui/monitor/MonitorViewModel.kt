package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.BaseApplication
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class MonitorViewModel @Inject constructor(
    val repository: LifeSignalRepository,
    val mainActivityEventListener: MainActivityEventListener,
    val baseApplication: BaseApplication,
) : ViewModel() {

    data class State(
        val patchData: SingleEvent<Triple<ArrayList<Int>, ArrayList<Int>, ArrayList<Int>>>? = null,
        val isStarted: Boolean = false,
        val buttonText: StringWrapper = StringWrapper.Res(R.string.start),
    ) {

    }

    val state = mutableStateOf(State())
    var lastPatchId = ""

    init {
        repository.filteredDataFlow
            .onEach {
                state.value = state.value.copy(patchData = SingleEvent(it))
            }
            .launchIn(viewModelScope)

        mainActivityEventListener.startHotspotService()


    }

    fun nextButtonClicked() {

    }

    fun bottomButtonClicked() {
        if (state.value.isStarted) {

        } else {

        }
    }
}