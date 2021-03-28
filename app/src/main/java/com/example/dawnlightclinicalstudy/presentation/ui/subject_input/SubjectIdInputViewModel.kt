package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.data.DataState
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class SubjectIdInputViewModel @Inject constructor(
    val userSessionRepository: UserSessionRepository,
    val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val subjectId: String = "",
        val deviceInfo: List<Pair<String, String>> = emptyList(),
        val room: String = "",
        val location: String = "",
        val navigateTo: SingleEvent<String>? = null,
    )

    val state = mutableStateOf(State())

    init {
        fetchTestPlan()
    }

    fun subjectIdTextChanged(text: String) {
        state.value = state.value.copy(subjectId = text)
    }

    fun nextButtonClicked() {
        nextStep()
    }

    fun keyboardOnNext() {
        nextStep()
    }

    private fun nextStep() {
        userSessionRepository.subjectId = state.value.subjectId
        state.value = state.value.copy(
            navigateTo = SingleEvent(Screen.HotspotConnection.route)
        )
    }

    private fun fetchTestPlan() {
        viewModelScope.launch {
            when (val response = userSessionRepository.fetchTestPlan()) {
                is DataState.Success -> {
                    val data = response.value.data.getOrNull(0) ?: return@launch
                    val deviceInfo = data.deviceInfo.map { it.key to it.value }

                    userSessionRepository.deviceIds.clear()
                    deviceInfo.forEach { userSessionRepository.deviceIds.add(it.first) }

                    val location = data.location
                    val room = data.room
                    state.value = state.value.copy(
                        deviceInfo = deviceInfo,
                        location = location,
                        room = room,
                    )
                }
            }
        }
    }
}