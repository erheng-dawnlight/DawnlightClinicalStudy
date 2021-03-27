package com.example.dawnlightclinicalstudy.presentation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionScreen
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionViewModel
import com.example.dawnlightclinicalstudy.presentation.ui.monitor.GraphMonitorScreen
import com.example.dawnlightclinicalstudy.presentation.ui.monitor.GraphMonitorViewModel
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectIdInputScreen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectIdInputViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.SubjectIdInput.route) {
        composable(route = Screen.SubjectIdInput.route) { backStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            val viewModel: SubjectIdInputViewModel =
                viewModel(Screen.SubjectIdInput.route, factory)
            SubjectIdInputScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }

        composable(route = Screen.HotspotConnection.route) { backStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            val viewModel: HotspotConnectionViewModel =
                viewModel(Screen.HotspotConnection.route, factory)
            HotspotConnectionScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }

        composable(route = Screen.GraphMonitor.route) { backStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            val viewModel: GraphMonitorViewModel =
                viewModel(Screen.GraphMonitor.route, factory)
            GraphMonitorScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }
    }
}