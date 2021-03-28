package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.data_source.request.LifeSignalRequest
import com.example.dawnlightclinicalstudy.data_source.request.OpenSessionRequest
import com.example.dawnlightclinicalstudy.domain.LifeSignalFilteredData
import com.example.dawnlightclinicalstudy.domain.Posture
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.utils.TimeUtil.millisToMinutesColinSeconds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@FlowPreview
@HiltViewModel
class GraphMonitorViewModel @Inject constructor(
    private val repository: LifeSignalRepository,
    private val userSessionRepository: UserSessionRepository,
    private val mainActivityEventListener: MainActivityEventListener,
) : ViewModel() {

    data class State(
        val toolbarTitle: StringWrapper? = null,
        val patchData: SingleEvent<LifeSignalFilteredData>? = null,
        val timerState: TimerState = TimerState.NOT_STARTED,
        val buttonText: StringWrapper = StringWrapper.Res(R.string.start),
        val timerText: StringWrapper =
            StringWrapper.Text(millisToMinutesColinSeconds(DEFAULT_SESSION_TIME_MILLIS)),
        val postureText: StringWrapper? = null,
        val warningText: StringWrapper? = null,

        val goBack: SingleEvent<Unit>? = null,
        val navigateTo: SingleEvent<String>? = null,
    )

    enum class TimerState {
        NOT_STARTED,
        STARTED,
        FINISHED,
        ;
    }

    val state = mutableStateOf(State())
    var lastPatchId = ""

    private var countDownTimer: CountDownTimer? = null

    init {
        state.value = state.value.copy(
            toolbarTitle = StringWrapper.Text(userSessionRepository.subjectId),
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
            Posture.LEFT to R.string.lying_left,
            Posture.RIGHT to R.string.lying_right,
            Posture.UP to R.string.lying_up,
            Posture.SIT to R.string.sitting_in_the_chair,
        ).let {
            val posture = it[Random.nextInt(it.size)]
            userSessionRepository.posture = posture.first
            StringWrapper.Res(posture.second)
        }
    }

    fun bottomButtonClicked() {
        when (state.value.timerState) {
            TimerState.NOT_STARTED -> {
                startMonitor()
            }
            TimerState.STARTED -> {
                abortMonitor()
            }
            TimerState.FINISHED -> {
                state.value = state.value.copy(
                    navigateTo = SingleEvent(Screen.UsbTransfer.route)
                )
            }
        }
    }

    private fun startMonitor() {
        startTimer()
        state.value = state.value.copy(
            buttonText = StringWrapper.Res(R.string.abort),
            timerState = TimerState.STARTED,
        )
        openSession()
    }

    private fun openSession() {
        viewModelScope.launch {
            userSessionRepository.openSession(
                OpenSessionRequest(
                    userSessionRepository.deviceIds,
                    userSessionRepository.posture.type,
                    System.currentTimeMillis(),
                    userSessionRepository.subjectId,
                )
            )
        }
    }

    private fun abortMonitor() {
        countDownTimer?.cancel()
        countDownTimer = null
        state.value = state.value.copy(
            buttonText = StringWrapper.Res(R.string.start),
            timerState = TimerState.NOT_STARTED,
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
                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() % 5 == 0) {
                    uploadSignal()
                }
            }

            override fun onFinish() {
                uploadSignal()
                countDownTimer = null
                state.value = state.value.copy(
                    buttonText = StringWrapper.Res(R.string.next),
                    timerState = TimerState.FINISHED,
                )
            }
        }.start()
    }

    fun warningTextClicked() {
        state.value = state.value.copy(goBack = SingleEvent(Unit))
    }

    private fun uploadSignal() {
        viewModelScope.launch {
            val request = repository.filteredDataList.map {
                LifeSignalRequest(
                    "ecg",
                    it.currentTime,
                    "rawData",
                    LifeSignalRequest.LifeSignalRequestData(
                        it.ecg0,
                        it.ecg1,
                        it.hr,
                    ),
                )
            }
            repository.filteredDataList.clear()
            repository.uploadSignal(repository.patchId, request)
        }
    }

    companion object {
        const val DEFAULT_SESSION_TIME_MILLIS = 60000L
    }
}