package com.example.dawnlightclinicalstudy.domain

import com.example.dawnlightclinicalstudy.R

enum class Posture(val type: String) {
    UP("up"),
    LEFT("left"),
    RIGHT("right"),
    SIT("sit"),
    UNKNOWN(""),
    ;

    companion object {
        fun getStringRes(posture: Posture): Int {
            return when (posture) {
                UP -> R.string.lying_up
                LEFT -> R.string.lying_left
                RIGHT -> R.string.lying_right
                SIT -> R.string.sitting_in_the_chair
                UNKNOWN -> 0
            }
        }
    }
}
