package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class SubjectIdInputViewModel @Inject constructor(
    val repository: LifeSignalRepository,
    val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val subjectId: String = "",
        val navigateTo: SingleEvent<String>? = null,
    )

    val state = mutableStateOf(State())

    fun subjectIdTextChanged(text: String) {
        state.value = state.value.copy(subjectId = text)
    }

    fun nextButtonClicked() {
        repository.subjectId = state.value.subjectId
        state.value = state.value.copy(
            navigateTo = SingleEvent(Screen.HotspotConnection.route)
        )
    }
}