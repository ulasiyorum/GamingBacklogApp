package com.ulasiyorum.gamingbacklogapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import com.ulasiyorum.gamingbacklogapp.ui.screens.EditGameScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.GameDetailScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.HomeScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.LoginScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.MyBacklogScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.ProfileScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.RegisterScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.WelcomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(
                onContinueLogin = { navController.navigate("login") },
                onContinueWithoutLogin = {
                    SessionManager.startGuestSession()
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(onGameClick = { gameId -> navController.navigate("detail/$gameId") })
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") },
                onLoggedOut = {
                    navController.navigate("welcome") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "detail/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            GameDetailScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("backlog") {
            MyBacklogScreen(
                onEditClick = { userGameId -> navController.navigate("edit_game/$userGameId") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable(
            route = "edit_game/{userGameId}",
            arguments = listOf(navArgument("userGameId") { type = NavType.StringType })
        ) {
            EditGameScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
    }
}
