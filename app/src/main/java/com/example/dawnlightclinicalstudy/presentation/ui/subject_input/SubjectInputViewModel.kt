package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class SubjectInputViewModel @Inject constructor(
    val repository: LifeSignalRepository,
    val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val subjectId: String = "",
        val patchId: String = "",
    ) {
        fun enableNextButton() = subjectId.isNotEmpty() && patchId.isNotBlank()
    }

    val state = mutableStateOf(State())

    init {
        viewModelScope.launch {
            repository.lastDiscoveredPatchFlow.onEach { jsonObject ->
                handlePatchDiscovered(jsonObject)
            }.launchIn(this)
        }
    }

    fun subjectIdTextChanged(text: String) {
        state.value = state.value.copy(subjectId = text)
    }

    private fun handlePatchDiscovered(jsonObject: JSONObject) {
        state.value = state.value.copy(
            patchId = jsonObject.getJSONObject("PatchInfo").getString("PatchId")
        )
    }

    fun nextButtonClicked() {
        mainActivityEventListener.onPatchSelected()
    }
}