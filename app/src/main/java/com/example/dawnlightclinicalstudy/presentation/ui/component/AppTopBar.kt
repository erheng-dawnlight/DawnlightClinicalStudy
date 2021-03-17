package com.example.dawnlightclinicalstudy.presentation.ui.component

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dawnlightclinicalstudy.domain.StringWrapper

@Composable
fun AppTopBar(
    titleString: StringWrapper,
    icon: ImageVector?,
    onIconClicked: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = titleString.getText(LocalContext.current),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onIconClicked,
            ) {
                icon?.let { Icon(it, "") }
            }
        },
        contentColor = MaterialTheme.colors.secondary,
        elevation = 12.dp,
    )
}