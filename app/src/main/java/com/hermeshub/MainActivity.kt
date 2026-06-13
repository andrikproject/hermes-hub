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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hermeshub.ui.components.BottomNavBar
import com.hermeshub.ui.navigation.BottomNavItem
import com.hermeshub.ui.screens.addconnection.AddConnectionScreen
import com.hermeshub.ui.screens.chat.ChatScreen
import com.hermeshub.ui.screens.connectionlist.ConnectionListScreen
import com.hermeshub.ui.screens.profile.ProfileScreen
import com.hermeshub.ui.theme.*
import com.hermeshub.viewmodel.HermesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    background = DarkBackground,
                    surface = DarkSurface,
                    surfaceVariant = DarkSurfaceVariant
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    HermesHubMain()
                }
            }
        }
    }
}

@Composable
fun HermesHubMain() {
    val navController = rememberNavController()
    val viewModel: HermesViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar only on main screens
    val bottomNavScreens = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Profile.route
    )
    val showBottomBar = currentDestination?.route in bottomNavScreens

    androidx.compose.material3.Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onTabSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // === HOME / CONNECTION LIST ===
            composable(BottomNavItem.Home.route) {
                ConnectionListScreen(
                    viewModel = viewModel,
                    onAddConnection = {
                        navController.navigate(BottomNavItem.AddConnection.route)
                    },
                    onOpenChat = { connection ->
                        viewModel.selectConnection(connection)
                        navController.navigate(
                            "chat/${connection.id}/${connection.name}"
                        )
                    }
                )
            }

            // === ADD CONNECTION ===
            composable(BottomNavItem.AddConnection.route) {
                AddConnectionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }

            // === CHAT ===
            composable(
                route = "chat/{connectionId}/{connectionName}",
                arguments = listOf(
                    navArgument("connectionId") { type = NavType.LongType },
                    navArgument("connectionName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val connectionName = backStackEntry.arguments?.getString("connectionName") ?: "Hermes"
                val connectionId = backStackEntry.arguments?.getLong("connectionId") ?: 0L
                ChatScreen(
                    viewModel = viewModel,
                    connectionName = connectionName,
                    connectionId = connectionId,
                    onBack = { navController.popBackStack() }
                )
            }

            // === PROFILE ===
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
