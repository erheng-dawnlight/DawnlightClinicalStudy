package com.example.dawnlightclinicalstudy.presentation.ui.component

import androidx.annotation.StringRes
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    @StringRes titleStringRes: Int,
    icon: ImageVector?,
    onIconClicked: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = LocalContext.current.getString(titleStringRes),
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