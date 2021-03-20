package com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalUseCaseCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class HotspotConnectionViewModel @Inject constructor(
    val repository: LifeSignalRepository,
    val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val toolbarTitle: StringWrapper? = null,
        val patchId: String = "",
        val navigateTo: SingleEvent<String>? = null,
    ) {
        fun enableNextButton() = patchId.isNotBlank()
    }

    val state = mutableStateOf(State())
    var lastPatchId = ""

    init {
        state.value = state.value.copy(toolbarTitle = StringWrapper.Text(repository.subjectId))

        repository.lastDiscoveredPatchFlow
            .distinctUntilChanged()
            .onEach { handlePatchDiscovered(it) }
            .launchIn(viewModelScope)

        mainActivityEventListener.startHotspotService()
    }

    private fun handlePatchDiscovered(patchId: String) {
        if (lastPatchId != patchId) {
            lastPatchId = patchId
            state.value = state.value.copy(patchId = patchId)
        } else {
            LifeSignalUseCaseCallback.Nothing
        }
    }

    fun nextButtonClicked() {
        mainActivityEventListener.onPatchSelected()
        state.value = state.value.copy(navigateTo = SingleEvent(Screen.PatchGraph.route))
    }
}