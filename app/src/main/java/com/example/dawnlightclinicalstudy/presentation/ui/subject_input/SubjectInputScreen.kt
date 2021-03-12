package com.example.dawnlightclinicalstudy.presentation.ui.subject_input

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.presentation.theme.AppTheme

@ExperimentalMaterialApi
@Composable
fun SubjectInputScreen(
    viewModel: SubjectInputViewModel,
    context: Context,
) {
    AppTheme(
        darkTheme = false,
    ) {
        Scaffold {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (title, textField, button, hotspotButton) = createRefs()
                Text(
                    text = context.getString(R.string.setting_wifi_hotspot_instruction),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(16.dp, 24.dp, 16.dp, 0.dp)
                        .constrainAs(title) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
                TextField(
                    value = viewModel.state.value.subjectId,
                    onValueChange = { viewModel.subjectIdTextChanged(it) },
                    label = { Text(text = context.getString(R.string.subject_id)) },
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .constrainAs(textField) {
                            top.linkTo(title.bottom, margin = 40.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                )
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    },
                    modifier = Modifier.constrainAs(hotspotButton) {
                        top.linkTo(textField.bottom, margin = 40.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                ) {
                    Text(context.getString(R.string.go_to_hotspot_setting))
                }
                Button(
                    onClick = { /* Do something! */ },
                    modifier = Modifier.constrainAs(button) {
                        bottom.linkTo(parent.bottom, margin = 32.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                ) {
                    Text(viewModel.state.value.buttonText)
                }
            }
        }
    }
}