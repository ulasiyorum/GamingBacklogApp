package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.models.stateEnum
import com.ulasiyorum.gamingbacklogapp.ui.viewmodel.EditGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGameScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: EditGameViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val userGame = currentUser?.userGames.orEmpty().firstOrNull { item -> item.id == viewModel.userGameId }

    var note by remember(userGame?.id) { mutableStateOf(userGame?.notes.orEmpty()) }
    var selectedStatus by remember(userGame?.id) {
        mutableStateOf(userGame?.stateEnum() ?: BacklogState.PLANNED)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oyunu Duzenle", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
        if (userGame == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Duzenlenecek backlog kaydi bulunamadi.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = userGame.game?.name ?: "Adsiz Oyun",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = userGame.game?.gameCategories.orEmpty()
                    .mapNotNull { category -> category.category?.name }
                    .distinct()
                    .joinToString(" • ")
                    .ifBlank { "Kategori bilgisi yok" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )

            Text("Oyun Durumu", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BacklogState.entries.forEach { status ->
                    StatusSelectChip(
                        status = status,
                        isSelected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("Notlarin", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    viewModel.clearError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Oyun hakkinda bir seyler yaz...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveChanges(selectedStatus, note, onSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving && !uiState.isRemoving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Degisiklikleri Kaydet", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                }
            }

            OutlinedButton(
                onClick = { viewModel.removeGame(onSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving && !uiState.isRemoving
            ) {
                if (uiState.isRemoving) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Backlog'dan Kaldir")
                }
            }
        }
    }
}

@Composable
private fun StatusSelectChip(
    status: BacklogState,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (status) {
        BacklogState.PLANNED -> Color(0xFFFFA500)
        BacklogState.PLAYING -> Color(0xFF64B5F6)
        BacklogState.COMPLETED -> Color(0xFF81C784)
    }

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(2.dp, accentColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = if (isSelected) FontWeight.Bold else null
            )
        }
    }
}
