package com.example.dawnlightclinicalstudy.presentation.ui.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.ui.component.AppTopBar
import com.example.dawnlightclinicalstudy.presentation.ui.component.BottomButton
import com.example.dawnlightclinicalstudy.presentation.ui.component.PatchView
import com.example.dawnlightclinicalstudy.presentation.ui.component.WarningCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun GraphMonitorScreen(
    viewModel: GraphMonitorViewModel,
    navController: NavController,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    titleString = viewModel.state.value.toolbarTitle,
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
                            patchView.updateData(it.ecg0, it.ecg1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )

                Text(
                    text = viewModel.state.value.postureText?.getText(LocalContext.current) ?: "",
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 16.dp, 0.dp, 0.dp),
                )

                Text(
                    text = viewModel.state.value.timerText.getText(LocalContext.current),
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                val warningText = viewModel.state.value.warningText?.getText(LocalContext.current)
                if (warningText.isNullOrEmpty()) {
                    BottomButton(
                        onButtonClicked = {
                            viewModel.bottomButtonClicked()
                        },
                        enabled = true,
                        text = viewModel.state.value.buttonText.getText(LocalContext.current),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp, 0.dp, 0.dp, 32.dp),
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter),
                        ) {
                            WarningCard(
                                text = warningText,
                                onClick = viewModel::warningTextClicked
                            )
                        }
                    }
                }
            }
        }
    }

    viewModel.state.value.goBack?.maybeConsume {
        navController.popBackStack()
    }
}