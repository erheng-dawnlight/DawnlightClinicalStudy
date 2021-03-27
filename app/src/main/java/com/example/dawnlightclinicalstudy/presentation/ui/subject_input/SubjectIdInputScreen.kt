package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun SubjectIdInputScreen(
    viewModel: SubjectIdInputViewModel,
    navController: NavHostController,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            keyboardController?.hideSoftwareKeyboard()
                            viewModel.keyboardOnNext()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 72.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = LocalContext.current.getString(
                        R.string.location,
                    ),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(16.dp, 32.dp, 16.dp, 0.dp),
                )

                Text(
                    text = viewModel.state.value.location,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(16.dp, 0.dp),
                )

                Text(
                    text = LocalContext.current.getString(
                        R.string.room,
                    ),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
                )

                Text(
                    text = viewModel.state.value.room,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(16.dp, 0.dp),
                )

                Text(
                    text = LocalContext.current.getString(R.string.devices),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
                )

                viewModel.state.value.deviceInfo.forEach {
                    Text(
                        text = it.second,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(16.dp, 0.dp),
                    )
                    Text(
                        text = it.first,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(16.dp, 0.dp),
                    )
                }

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