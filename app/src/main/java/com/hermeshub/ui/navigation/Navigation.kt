package com.hermeshub.ui.navigation

sealed class Screen(val route: String) {
    object ConnectionList : Screen("connection_list")
    object AddConnection : Screen("add_connection")
    object Chat : Screen("chat/{connectionId}/{connectionName}") {
        fun createRoute(connectionId: Long, connectionName: String): String {
            return "chat/$connectionId/$connectionName"
        }
    }
}
