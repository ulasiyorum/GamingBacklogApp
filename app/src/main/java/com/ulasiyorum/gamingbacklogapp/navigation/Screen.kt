package com.ulasiyorum.gamingbacklogapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Keşfet", Icons.Default.Home)
    object Backlog : Screen("backlog", "Kütüphanem", Icons.Default.List) // Yeni eklendi (Icons.Default.List import et)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
}