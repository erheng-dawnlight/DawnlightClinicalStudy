package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.ui.component.AppTopBar
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionView
import com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection.HotspotConnectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun HotspotConnectionScreen(
    viewModel: HotspotConnectionViewModel,
    navController: NavController,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    titleStringRes = R.string.turn_on_hotspot,
                    icon = Icons.Filled.ArrowBack,
                ) {
                    navController.popBackStack()
                }
            }) {
            HotspotConnectionView(
                patchIdText = viewModel.state.value.patchId,
                onNextButtonClicked = viewModel::nextButtonClicked,
                enableNextButton = viewModel.state.value::enableNextButton
            )
        }
    }
}