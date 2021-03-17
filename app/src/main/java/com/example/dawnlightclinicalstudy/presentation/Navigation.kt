package com.example.dawnlightclinicalstudy.presentation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dawnlightclinicalstudy.presentation.navigation.Screen
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionScreen
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionViewModel
import com.example.dawnlightclinicalstudy.presentation.ui.monitor.MonitorScreen
import com.example.dawnlightclinicalstudy.presentation.ui.monitor.MonitorViewModel
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputScreen
import com.example.dawnlightclinicalstudy.presentation.ui.subject_input.SubjectInputViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HotspotConnection.route) {
        composable(route = Screen.SubjectId.route) { backStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            val viewModel: SubjectInputViewModel =
                viewModel(Screen.SubjectId.route, factory)
            SubjectInputScreen(
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

        composable(route = Screen.PatchGraph.route) { backStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            val viewModel: MonitorViewModel =
                viewModel(Screen.PatchGraph.route, factory)
            MonitorScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }
    }
}