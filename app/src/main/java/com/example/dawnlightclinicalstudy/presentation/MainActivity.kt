package com.example.dawnlightclinicalstudy.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputScreen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputViewModel
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.SubjectId.route) {
                composable(route = Screen.SubjectId.route) { backStackEntry ->
                    val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
                    val viewModel: SubjectInputViewModel = viewModel("SubjectIdViewModel", factory)
                    SubjectInputScreen(viewModel = viewModel, context = LocalContext.current)
                }
            }
        }
    }
}