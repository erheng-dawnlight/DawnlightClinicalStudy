package com.example.dawnlightclinicalstudy.presentation.ui.hotspot_connection

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dawnlightclinicalstudy.R
import com.example.dawnlightclinicalstudy.presentation.theme.Blue400
import com.example.dawnlightclinicalstudy.presentation.ui.component.BottomButton
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@Composable
fun HotspotConnectionView(
    patchIdText: String,
    onNextButtonClicked: () -> Unit,
    enableNextButton: () -> Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val context = LocalContext.current

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
                (context as? Activity)?.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
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
            if (patchIdText.isNotEmpty()) {
                Text(
                    text = context.getString(
                        R.string.xxx_has_connected,
                        patchIdText,
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

        BottomButton(
            onButtonClicked = onNextButtonClicked,
            enabled = enableNextButton(),
            text = LocalContext.current.getString(R.string.next),
        )
    }
}