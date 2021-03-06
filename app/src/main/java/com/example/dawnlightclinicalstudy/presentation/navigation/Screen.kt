package com.example.dawnlightclinicalstudy.presentation.navigation

sealed class Screen(val route: String) {

    object SubjectId : Screen("subject_id")

}