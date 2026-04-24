package com.edoreczenia.feature.inbox.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.edoreczenia.R

@Composable
fun InboxBottomNavBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.Mail, contentDescription = null) },
            label = { Text(stringResource(R.string.inbox_bottom_nav_inbox)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.Outbox, contentDescription = null) },
            label = { Text(stringResource(R.string.inbox_bottom_nav_outbox)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.ManageSearch, contentDescription = null) },
            label = { Text(stringResource(R.string.inbox_bottom_nav_ade)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.inbox_bottom_nav_settings)) }
        )
    }
}

