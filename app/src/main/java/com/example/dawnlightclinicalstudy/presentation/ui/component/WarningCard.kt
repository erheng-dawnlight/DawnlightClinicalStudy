package com.example.dawnlightclinicalstudy.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dawnlightclinicalstudy.presentation.theme.RedErrorDark

@Composable
fun WarningCard(
    text: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = RedErrorDark,
        modifier = Modifier
            .fillMaxWidth(.8f)
            .clickable { onClick.invoke() },
        elevation = 4.dp,
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
        )
    }
}