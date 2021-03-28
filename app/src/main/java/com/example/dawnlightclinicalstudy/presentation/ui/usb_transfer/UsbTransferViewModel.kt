package com.example.dawnlightclinicalstudy.presentation.ui.usb_transfer

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject


@FlowPreview
@HiltViewModel
class UsbTransferViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
) : ViewModel() {

    data class State(
        val toolbarTitle: StringWrapper? = null,
    )

    val state = mutableStateOf(State())

    init {
        state.value = state.value.copy(
            toolbarTitle = StringWrapper.Text(userSessionRepository.subjectId),
        )
    }

    fun bottomButtonClicked() {
        viewModelScope.launch {
            userSessionRepository.closeSession()
        }
    }
}