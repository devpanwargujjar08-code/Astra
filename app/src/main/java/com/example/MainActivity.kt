package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AstraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AstraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AstraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AstraApp(viewModel: AstraViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // BackHandler: Navigate back to Chat tab if on another screen
    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat"
                        )
                    },
                    label = { Text("Astra Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AstraIndigoLight,
                        selectedTextColor = AstraIndigoLight,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_chat")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Default.Explore else Icons.Outlined.Explore,
                            contentDescription = "Toolkits"
                        )
                    },
                    label = { Text("Toolkits") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AstraCyanLight,
                        selectedTextColor = AstraCyanLight,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_toolkits")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 2) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Saved"
                        )
                    },
                    label = { Text("Saved") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AstraIndigoLight,
                        selectedTextColor = AstraIndigoLight,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_saved")
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AstraCyanLight,
                        selectedTextColor = AstraCyanLight,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            0 -> ChatScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            1 -> HubScreen(
                viewModel = viewModel,
                onNavigateToChat = { viewModel.setTab(0) },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> BookmarksScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            3 -> SettingsScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
    }
}
