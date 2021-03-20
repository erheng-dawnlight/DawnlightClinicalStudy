package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.domain.LifeSignalFilteredData
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.utils.TimeUtil.millisToMinutesColinSeconds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.random.Random

@FlowPreview
@HiltViewModel
class GraphMonitorViewModel @Inject constructor(
    private val repository: LifeSignalRepository,
    private val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val toolbarTitle: StringWrapper? = null,
        val patchData: SingleEvent<LifeSignalFilteredData>? = null,
        val isStarted: Boolean = false,
        val buttonText: StringWrapper = StringWrapper.Res(R.string.start),
        val timerText: StringWrapper =
            StringWrapper.Text(millisToMinutesColinSeconds(DEFAULT_SESSION_TIME_MILLIS)),
        val postureText: StringWrapper? = null,
        val warningText: StringWrapper? = null,

        val goBack: SingleEvent<Unit>? = null,
    )

    val state = mutableStateOf(State())
    var lastPatchId = ""

    private var countDownTimer: CountDownTimer? = null

    init {
        state.value = state.value.copy(
            toolbarTitle = StringWrapper.Text(repository.subjectId),
            postureText = generatePostureText(),
        )

        repository.filteredDataFlow
            .onEach {
                state.value = state.value.copy(patchData = SingleEvent(it))
            }
            .launchIn(viewModelScope)

        repository.statusFlow
            .onEach {
                state.value = state.value.copy(
                    warningText = StringWrapper.Res(
                        if (it >= 55) {
                            R.string.patch_not_available
                        } else {
                            0
                        }
                    ),
                )
            }
            .launchIn(viewModelScope)

        mainActivityEventListener.startHotspotService()
    }

    private fun generatePostureText(): StringWrapper {
        return arrayOf(
            R.string.lying_left,
            R.string.lying_right,
            R.string.lying_up,
            R.string.sitting_in_the_chair,
        ).let { StringWrapper.Res(it[Random.nextInt(it.size)]) }
    }

    fun bottomButtonClicked() {
        if (state.value.isStarted) {
            abortMonitor()
        } else {
            startMonitor()
        }
    }

    private fun startMonitor() {
        startTimer()
        state.value = state.value.copy(
            buttonText = StringWrapper.Res(R.string.abort),
            isStarted = true,
        )
    }

    private fun abortMonitor() {
        countDownTimer?.cancel()
        countDownTimer = null
        state.value = state.value.copy(
            buttonText = StringWrapper.Res(R.string.start),
            isStarted = false,
            timerText = StringWrapper.Text(
                millisToMinutesColinSeconds(DEFAULT_SESSION_TIME_MILLIS)
            ),
        )
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(DEFAULT_SESSION_TIME_MILLIS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                state.value = state.value.copy(
                    timerText = StringWrapper.Text(
                        millisToMinutesColinSeconds(millisUntilFinished)
                    )
                )
            }

            override fun onFinish() {
                countDownTimer = null
            }
        }.start()
    }

    fun warningTextClicked() {
        state.value = state.value.copy(goBack = SingleEvent(Unit))
    }

    companion object {
        const val DEFAULT_SESSION_TIME_MILLIS = 60000L
    }
}