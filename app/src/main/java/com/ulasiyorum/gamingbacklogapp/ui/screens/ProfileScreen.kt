package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.models.stateEnum
import com.ulasiyorum.gamingbacklogapp.ui.viewmodel.ProfileViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isGuestSession by viewModel.isGuestSession.collectAsState()

    LaunchedEffect(user?.id) {
        if (user != null && !isGuestSession) {
            viewModel.refreshProfile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    if (user != null && !isGuestSession) {
                        IconButton(onClick = viewModel::refreshProfile, enabled = !uiState.isRefreshing) {
                            Text("Yenile", color = MaterialTheme.colorScheme.primary)
                        }
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
        if (user == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Profil icin giris yap", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Kayitli oyun istatistiklerini ve profil bilgilerini gormek icin hesabina giris yapmalisin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                Button(onClick = onNavigateToLogin) {
                    Text("Giris Yap")
                }
            }
            return@Scaffold
        }

        val userGames = user?.userGames.orEmpty()
        val completedCount = userGames.count { game -> game.stateEnum() == BacklogState.COMPLETED }
        val playingCount = userGames.count { game -> game.stateEnum() == BacklogState.PLAYING }
        val plannedCount = userGames.count { game -> game.stateEnum() == BacklogState.PLANNED }
        val categoryCounts = userGames
            .flatMap { backlogGame -> backlogGame.game?.gameCategories.orEmpty() }
            .mapNotNull { category -> category.category?.name }
            .groupingBy { name -> name }
            .eachCount()
            .toList()
            .sortedByDescending { (_, count) -> count }
            .take(3)
        val maxCategoryCount = categoryCounts.maxOfOrNull { (_, count) -> count } ?: 1

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.name.orEmpty().ifBlank { if (isGuestSession) "Misafir Oyuncu" else "Adsiz Kullanici" },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isGuestSession) {
                        "Yerel profil • veriler bu cihazda tutuluyor"
                    } else {
                        user?.email.orEmpty().ifBlank { "E-posta bilgisi yok" }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard("Tamamlanan", completedCount.toString(), Modifier.weight(1f))
                StatCard("Oynanan", playingCount.toString(), Modifier.weight(1f))
                StatCard("Planlanan", plannedCount.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Oyun Tercihleri",
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryCounts.isEmpty()) {
                Text(
                    "Tercih dagilimi icin henuz yeterli veri yok.",
                    modifier = Modifier.align(Alignment.Start),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            } else {
                categoryCounts.forEach { (name, count) ->
                    PreferenceBar(
                        label = name,
                        progress = count.toFloat() / maxCategoryCount.toFloat()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isGuestSession) {
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hesap Olustur")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mevcut Hesapla Giris Yap")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isGuestSession) "Misafir Verisini Temizle" else "Cikis Yap")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PreferenceBar(label: String, progress: Float) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}
