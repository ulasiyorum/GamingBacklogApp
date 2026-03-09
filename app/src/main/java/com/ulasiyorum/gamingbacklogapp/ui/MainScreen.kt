package com.ulasiyorum.gamingbacklogapp.ui

import com.ulasiyorum.gamingbacklogapp.navigation.NavGraph
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ulasiyorum.gamingbacklogapp.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Backlog, Screen.Profile)

    Scaffold(
        bottomBar = {
            // Sadece Home ve Profile ekranlarındayken BottomBar görünsün
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Login ekranındaysak BottomBar'ı gizle
            if (currentDestination?.route != "login" && currentDestination?.route != "register" && currentDestination?.route != "welcome") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelMedium) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Navigasyon burada dönüyor
        NavGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}