package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.models.UserGame
import com.ulasiyorum.gamingbacklogapp.data.models.stateEnum
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBacklogScreen(
    onEditClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val currentUser by SessionManager.currentUser.collectAsState()
    val userGames = currentUser?.userGames.orEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kutuphanem", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            currentUser == null -> {
                AuthRequiredState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    title = "Backlog'un icin giris yap",
                    description = "Kayitli oyunlarini gormek ve guncellemek icin bir hesapla devam etmelisin.",
                    actionLabel = "Giris Yap",
                    onAction = onNavigateToLogin
                )
            }

            userGames.isEmpty() -> {
                AuthRequiredState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    title = "Backlog'un henuz bos",
                    description = "Kesfet ekranindan oyun secip backlog'una ekleyebilirsin.",
                    actionLabel = "Tamam",
                    onAction = {}
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(userGames, key = { userGame -> userGame.id }) { item ->
                        BacklogItemCard(item = item, onEditClick = { onEditClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BacklogItemCard(item: UserGame, onEditClick: () -> Unit) {
    val status = item.stateEnum()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.game?.name ?: "Adsiz Oyun",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = status)
            }

            Text(
                text = item.game?.gameCategories.orEmpty()
                    .mapNotNull { category -> category.category?.name }
                    .distinct()
                    .joinToString(" • ")
                    .ifBlank { "Kategori bilgisi yok" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "  ${item.notes.orEmpty().ifBlank { "Henüz not eklenmemis." }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: BacklogState) {
    val color = backlogColor(status)
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AuthRequiredState(
    modifier: Modifier,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

private fun backlogColor(state: BacklogState): Color = when (state) {
    BacklogState.PLANNED -> Color(0xFFFFA500)
    BacklogState.PLAYING -> Color(0xFF64B5F6)
    BacklogState.COMPLETED -> Color(0xFF81C784)
}
