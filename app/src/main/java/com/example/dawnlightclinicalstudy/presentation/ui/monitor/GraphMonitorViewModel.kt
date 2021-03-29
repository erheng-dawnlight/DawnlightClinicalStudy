package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.data_source.request.LifeSignalRequest
import com.example.dawnlightclinicalstudy.data_source.request.OpenCloseSessionRequest
import com.example.dawnlightclinicalstudy.domain.*
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.utils.TimeUtil.millisToMinutesColinSeconds
import com.example.dawnlightclinicalstudy.presentation.utils.TimeUtil.secondsToMinutesColinSeconds
import com.example.dawnlightclinicalstudy.usecases.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class GraphMonitorViewModel @Inject constructor(
    private val repository: LifeSignalRepository,
    private val userSessionRepository: UserSessionRepository,
    private val mainActivityEventListener: MainActivityEventListener,
    private val sessionManager: SessionManager,
) : ViewModel() {

    data class State(
        val toolbarTitle: StringWrapper? = null,
        val patchData: SingleEvent<LifeSignalFilteredData>? = null,
        val timerState: TimerState = TimerState.NOT_STARTED,
        val buttonText: StringWrapper = StringWrapper.Res(R.string.start),
        val timerText: StringWrapper? = null,
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

    private var countDownTimer: CountDownTimer? = null

    init {
        state.value = state.value.copy(
            toolbarTitle = getToolBarTitle(),
            postureText = getPostureText(),
            timerText = getTimerText(),
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

    private fun getToolBarTitle(): StringWrapper {
        return StringWrapper.Text("${userSessionRepository.subjectId} (${sessionManager.currentSessionIndex + 1}/${sessionManager.totalSessions}) ")
    }

    private fun getPostureText(): StringWrapper {
        return StringWrapper.Res(Posture.getStringRes((sessionManager.currentSession() as Session.PostureSession).posture))
    }

    private fun getTimerText(): StringWrapper {
        return StringWrapper.Text(secondsToMinutesColinSeconds(userSessionRepository.eachSessionSecond.toLong()))
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
                if (sessionManager.hasNextSession()) {
                    val nextSession = sessionManager.nextSession()
                    if (nextSession is Session.PostureSession) {
                        state.value = state.value.copy(
                            navigateTo = SingleEvent(Screen.GraphMonitor.route)
                        )
                    }
                } else {
                    state.value = state.value.copy(
                        navigateTo = SingleEvent(Screen.UsbTransfer.route)
                    )
                }
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
                OpenCloseSessionRequest(
                    userSessionRepository.deviceIds,
                    (sessionManager.currentSession() as Session.PostureSession).posture.type,
                    System.currentTimeMillis(),
                    userSessionRepository.subjectId,
                )
            )
        }
    }

    private fun closeSession() {
        viewModelScope.launch {
            userSessionRepository.closeSession(
                OpenCloseSessionRequest(
                    userSessionRepository.deviceIds,
                    (sessionManager.currentSession() as Session.PostureSession).posture.type,
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
                secondsToMinutesColinSeconds(userSessionRepository.eachSessionSecond.toLong())
            ),
        )
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(
            TimeUnit.SECONDS.toMillis(userSessionRepository.eachSessionSecond.toLong()),
            1000,
        ) {
            override fun onTick(millisUntilFinished: Long) {
                state.value = state.value.copy(
                    timerText = StringWrapper.Text(
                        millisToMinutesColinSeconds(millisUntilFinished)
                    )
                )
                val secondUntilFinish = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
                if (secondUntilFinish % 5 == 0 && secondUntilFinish != 0) {
                    uploadSignal()
                }
            }

            override fun onFinish() {
                uploadSignal()
                closeSession()
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
}