package com.example.dawnlightclinicalstudy.presentation.utils

import java.util.concurrent.TimeUnit

object TimeUtil {

    fun millisToMinutesColinSeconds(millis: Long): String {
        val min = TimeUnit.MILLISECONDS.toMinutes(millis)
        val sec = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(min)
        return String.format("%02d:%02d", min, sec)
    }
}