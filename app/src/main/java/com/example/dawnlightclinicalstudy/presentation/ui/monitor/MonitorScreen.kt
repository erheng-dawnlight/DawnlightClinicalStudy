package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.ui.component.AppTopBar
import com.example.dawnlightclinicalstudy.presentation.ui.component.BottomButton
import com.example.dawnlightclinicalstudy.presentation.ui.component.PatchView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel,
    navController: NavController,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    titleString = StringWrapper.Res(R.string.monitor),
                    icon = Icons.Filled.ArrowBack,
                ) {
                    navController.popBackStack()
                }
            }) {
            Column {
                AndroidView(
                    factory = {
                        PatchView(it)
                    },
                    update = { patchView ->
                        viewModel.state.value.patchData?.maybeConsume {
                            patchView.updateData(it.first, it.second, it.third)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
                Text("dada")
                Text("yes")
                BottomButton(
                    onButtonClicked = {
                        viewModel.bottomButtonClicked()
                    },
                    enabled = true,
                    text = LocalContext.current.getString(R.string.start),
                )
            }
        }
    }
}