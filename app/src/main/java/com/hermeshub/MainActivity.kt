package com.hermeshub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hermeshub.data.model.HermesConnection
import com.hermeshub.ui.navigation.Screen
import com.hermeshub.ui.screens.addconnection.AddConnectionScreen
import com.hermeshub.ui.screens.chat.ChatScreen
import com.hermeshub.ui.screens.connectionlist.ConnectionListScreen
import com.hermeshub.ui.theme.DarkBackground
import com.hermeshub.viewmodel.HermesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    background = DarkBackground
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    HermesHubNavigation()
                }
            }
        }
    }
}

@Composable
fun HermesHubNavigation() {
    val navController = rememberNavController()
    val viewModel: HermesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.ConnectionList.route
    ) {
        // Connection List
        composable(Screen.ConnectionList.route) {
            ConnectionListScreen(
                viewModel = viewModel,
                onAddConnection = {
                    navController.navigate(Screen.AddConnection.route)
                },
                onOpenChat = { connection ->
                    viewModel.selectConnection(connection)
                    navController.navigate(
                        Screen.Chat.createRoute(connection.id, connection.name)
                    )
                }
            )
        }

        // Add Connection
        composable(Screen.AddConnection.route) {
            AddConnectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Chat
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("connectionId") { type = NavType.LongType },
                navArgument("connectionName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val connectionName = backStackEntry.arguments?.getString("connectionName") ?: "Hermes"
            ChatScreen(
                viewModel = viewModel,
                connectionName = connectionName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
