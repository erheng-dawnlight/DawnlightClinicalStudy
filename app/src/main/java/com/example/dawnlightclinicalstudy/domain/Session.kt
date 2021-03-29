package com.example.dawnlightclinicalstudy.domain

sealed class Session(
    var hasFinished: Boolean = false,
) {

    class PostureSession(
        val posture: Posture,
    ) : Session()
}