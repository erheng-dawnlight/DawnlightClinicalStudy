package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.domain.SingleEvent
import com.example.dawnlightclinicalstudy.domain.StringWrapper
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.ui.component.AppTopBar
import com.example.dawnlightclinicalstudy.presentation.ui.component.BottomButton
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun SubjectInputScreen(
    viewModel: SubjectInputViewModel,
    navController: NavHostController,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    titleString = StringWrapper.Res(R.string.app_name),
                    icon = Icons.Filled.Menu,
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                TextField(
                    value = viewModel.state.value.subjectId,
                    onValueChange = { viewModel.subjectIdTextChanged(it) },
                    label = {
                        Text(
                            text = LocalContext.current.getString(R.string.subject_id),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .padding(0.dp, 72.dp, 0.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                )

                BottomButton(
                    onButtonClicked = viewModel::nextButtonClicked,
                    enabled = viewModel.state.value.subjectId.isNotEmpty(),
                    text = LocalContext.current.getString(R.string.next),
                )
            }
        }

        navigate(viewModel.state.value.navigateTo, navController)
    }
}

fun navigate(navigateTo: SingleEvent<String>?, navController: NavController) {
    navigateTo?.maybeConsume { route ->
        navController.navigate(route)
    }
}