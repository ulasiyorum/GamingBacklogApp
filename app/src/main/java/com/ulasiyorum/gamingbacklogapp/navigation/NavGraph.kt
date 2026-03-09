package com.ulasiyorum.gamingbacklogapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ulasiyorum.gamingbacklogapp.ui.screens.EditGameScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.GameDetailScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.HomeScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.LoginScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.MyBacklogScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.ProfileScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.RegisterScreen
import com.ulasiyorum.gamingbacklogapp.ui.screens.WelcomeScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier // Alt barın içeriği kapatmaması için önemli
    ) {
        composable("welcome") {
            WelcomeScreen(onContinueLogin = { navController.navigate("login") },
                onContinueWithoutLogin = { navController.navigate("home") })
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("home") },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(onGameClick = { navController.navigate("detail") })
        }
        composable("profile") {
            ProfileScreen()
        }
        composable("detail") {
            // Detay sayfasında alt barı gizlemek isteyebilirsin, şimdilik böyle kalsın
            GameDetailScreen(onBackClick = { navController.popBackStack() })
        }
        composable("backlog") {
            MyBacklogScreen(onEditClick = { navController.navigate("edit_game") })
        }
        composable("edit_game") {
            EditGameScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
    }
}