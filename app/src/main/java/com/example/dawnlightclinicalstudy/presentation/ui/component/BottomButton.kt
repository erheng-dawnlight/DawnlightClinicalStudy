package com.example.dawnlightclinicalstudy.presentation.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dawnlightclinicalstudy.R

@Composable
fun BottomButton(
    onButtonClicked: () -> Unit,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 0.dp, 32.dp),
    ) {
        Button(
            onClick = { onButtonClicked() },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth(.8f)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = LocalContext.current.getString(R.string.next),
                color = MaterialTheme.colors.secondary,
            )
        }
    }
}