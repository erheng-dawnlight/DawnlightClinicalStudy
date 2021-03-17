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
import androidx.compose.ui.unit.dp

@Composable
fun BottomButton(
    onButtonClicked: () -> Unit,
    enabled: Boolean,
    text: String,
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
                text = text,
                color = MaterialTheme.colors.secondary,
            )
        }
    }
}