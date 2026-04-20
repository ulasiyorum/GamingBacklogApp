package com.ulasiyorum.gamingbacklogapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ulasiyorum.gamingbacklogapp.navigation.NavGraph
import com.ulasiyorum.gamingbacklogapp.navigation.Screen
import com.ulasiyorum.gamingbacklogapp.ui.viewmodel.AppViewModel

@Composable
fun MainScreen(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Backlog, Screen.Profile)
    val isRestored by appViewModel.isRestored.collectAsState()
    val currentUser by appViewModel.currentUser.collectAsState()

    if (!isRestored) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

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
        NavGraph(
            navController = navController,
            startDestination = if (currentUser != null) "home" else "welcome",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
