package com.example.dawnlightclinicalstudy.domain

data class LifeSignalFilteredData(
    val ecg0: List<Int>,
    val ecg1: List<Int>,
    val currentTime: Long,
)