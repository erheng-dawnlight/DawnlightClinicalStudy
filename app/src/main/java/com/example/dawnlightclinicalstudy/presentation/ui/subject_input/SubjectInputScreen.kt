package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme
import com.example.dawnlightclinicalstudy.presentation.theme.Blue400
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun SubjectInputScreen(
    viewModel: SubjectInputViewModel,
    context: Context,
    onNavigateToSubjectScreen: () -> Unit,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = context.getString(R.string.app_name),
                            color = MaterialTheme.colors.secondary,
                        )
                    }
                )
            }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = context.getString(R.string.setting_wifi_hotspot_instruction),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(0.dp, 24.dp, 0.dp, 0.dp)
                        .align(Alignment.CenterHorizontally),
                )

                Button(
                    modifier = Modifier
                        .padding(16.dp, 24.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    },
                ) {
                    Text(
                        text = context.getString(R.string.go_to_hotspot_setting),
                        color = MaterialTheme.colors.secondary,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(0.dp, 72.dp, 0.dp, 0.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    if (viewModel.state.value.patchId.isNotEmpty()) {
                        Text(
                            text = context.getString(
                                R.string.xxx_has_connected,
                                viewModel.state.value.patchId,
                            ),
                            style = MaterialTheme.typography.body1,
                        )

                        Icon(
                            Icons.Outlined.Done,
                            "",
                            tint = Blue400,
                            modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                        )
                    } else {
                        Text(
                            text = context.getString(R.string.looking_for_the_device),
                            style = MaterialTheme.typography.body2,
                        )

                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(8.dp, 0.dp, 0.dp, 0.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }

                TextField(
                    value = viewModel.state.value.subjectId,
                    onValueChange = { viewModel.subjectIdTextChanged(it) },
                    label = {
                        Text(
                            text = context.getString(R.string.subject_id),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .padding(0.dp, 72.dp, 0.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(36.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            viewModel.nextButtonClicked()
                        },
                        enabled = viewModel.state.value.enableNextButton(),
                    ) {
                        Text(
                            text = context.getString(R.string.next),
                            color = MaterialTheme.colors.secondary,
                        )
                    }
                }
            }
        }
    }
}