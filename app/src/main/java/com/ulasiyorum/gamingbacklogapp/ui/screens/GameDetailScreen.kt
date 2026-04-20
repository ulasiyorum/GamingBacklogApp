package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.ui.viewmodel.GameDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: GameDetailViewModel = viewModel()
) {
    val game by viewModel.game.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentGame = game
    val userGame = currentUser?.userGames.orEmpty().firstOrNull { backlogGame -> backlogGame.gameId == currentGame?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGame?.name ?: "Oyun Detayi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (currentGame == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Oyun detayi bulunamadi.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            GameHeader(game = currentGame)

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = currentGame.name ?: "Adsiz Oyun",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f / 5.0", currentGame.rating),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        text = currentGame.gameCategories.orEmpty()
                            .mapNotNull { category -> category.category?.name }
                            .distinct()
                            .joinToString(" • ")
                            .ifBlank { "Kategori bilgisi yok" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "Oyun Hakkinda",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = currentGame.description.orEmpty().ifBlank { "Bu oyun icin aciklama eklenmemis." },
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Button(
                    onClick = {
                        when {
                            currentUser == null -> onNavigateToLogin()
                            userGame == null -> viewModel.addToBacklog()
                            else -> Unit
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSubmitting && userGame == null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    when {
                        uiState.isSubmitting -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        }

                        currentUser == null -> Text("Backlog icin giris yap", color = Color.Black)
                        userGame != null -> Text("Bu oyun backlog'unda", color = Color.Black)
                        else -> Text("Backlog'uma Ekle", color = Color.Black)
                    }
                }

                uiState.infoMessage?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.primary)
                }

                uiState.errorMessage?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }

                if (currentUser == null) {
                    Text(
                        text = "Backlog islevleri icin once giris yapman gerekiyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameHeader(game: Game) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp)
    ) {
        Text(
            text = game.name ?: "Adsiz Oyun",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
