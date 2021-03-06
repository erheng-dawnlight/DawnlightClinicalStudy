package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dawnlightclinicalstudy.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubjectInputViewModel @Inject constructor() : ViewModel() {

    data class State(
        val subjectId: String = "",
        val buttonText: Int = R.string.patch
    )

    val state = mutableStateOf(State())

    fun subjectIdTextChanged(text: String) {
        state.value = state.value.copy(subjectId = text)
    }

}