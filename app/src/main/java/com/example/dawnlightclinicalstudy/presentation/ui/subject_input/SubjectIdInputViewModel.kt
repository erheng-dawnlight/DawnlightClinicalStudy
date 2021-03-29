package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.data.DataState
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.domain.Posture
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random.Default.nextBoolean

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
        val postures: List<Posture>? = null,
        val posturesCheckboxSelections: List<Boolean>? = null,
        val sessionTimeSec: String = "60",
        val navigateTo: SingleEvent<String>? = null,
    )

    val state = mutableStateOf(State())

    init {
        val (postures, checkboxSelections) = generatePostures()
        state.value = state.value.copy(
            postures = postures,
            posturesCheckboxSelections = checkboxSelections,
        )
        fetchTestPlan()
    }

    fun subjectIdTextChanged(text: String) {
        state.value = state.value.copy(subjectId = text)
    }

    fun sessionTimeTextChanged(text: String) {
        state.value = state.value.copy(sessionTimeSec = text)
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

    fun postureChecked(index: Int, isChecked: Boolean) {
        state.value.posturesCheckboxSelections?.toMutableList()?.apply {
            this[index] = isChecked
            state.value = state.value.copy(
                posturesCheckboxSelections = this,
            )
        }
    }

    private fun generatePostures(): Pair<List<Posture>, List<Boolean>> {
        val lyingPostures =
            mutableListOf(Posture.LEFT, Posture.RIGHT, Posture.UP).apply { shuffle() }
        if (nextBoolean()) {
            lyingPostures.add(0, Posture.SIT)
        } else {
            lyingPostures.add(Posture.SIT)
        }
        return lyingPostures to listOf(true, true, true, true)
    }
}
