package com.example.dawnlightclinicalstudy.presentation.ui.usb_transfer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.ui.component.AppTopBar
import com.example.dawnlightclinicalstudy.presentation.ui.component.BottomButton
import kotlinx.coroutines.FlowPreview

@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun UsbTransferScreen(
    viewModel: UsbTransferViewModel,
    navController: NavHostController,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    titleString = viewModel.state.value.toolbarTitle,
                    icon = Icons.Filled.Menu,
                ) {}
            }
        ) {
            Text(
                text = "Work in Progress...",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp, 16.dp),
            )
            BottomButton(
                onButtonClicked = viewModel::bottomButtonClicked,
                enabled = true,
                text = "Start Another Subject",
            )
        }
    }
}