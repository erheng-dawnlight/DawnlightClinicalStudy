package com.example.dawnlightclinicalstudy.presentation

import androidx.lifecycle.ViewModel
import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalDataParsingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val repository: LifeSignalRepository,
    val useCase: LifeSignalDataParsingUseCase,
) : ViewModel() {

    fun lifeSignalDataReceived(event: String, json: JSONObject) {
        useCase.onDataReceived(event, json)
    }
}