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

@FlowPreview
@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val repository: LifeSignalRepository,
    private val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val patchData: SingleEvent<LifeSignalFilteredData>? = null,
        val isStarted: Boolean = false,
        val buttonText: StringWrapper = StringWrapper.Res(R.string.start),
        val timerText: StringWrapper =
            StringWrapper.Text(millisToMinutesColinSeconds(DEFAULT_SESSION_TIME_MILLIS)),
    )

    val state = mutableStateOf(State())
    var lastPatchId = ""

    private var countDownTimer: CountDownTimer? = null

    init {
        repository.filteredDataFlow
            .onEach {
                state.value = state.value.copy(patchData = SingleEvent(it))
            }
            .launchIn(viewModelScope)

        mainActivityEventListener.startHotspotService()
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

    companion object {
        const val DEFAULT_SESSION_TIME_MILLIS = 60000L
    }
}