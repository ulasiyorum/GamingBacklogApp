package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 1. OYUN DURUMLARI (State) TANIMI
enum class GameStatus(val label: String, val color: Color) {
    PLANNED("Planlandı", Color(0xFFFFA500)),   // Turuncu
    PLAYING("Oynanıyor", Color(0xFF64B5F6)),  // Mavi
    COMPLETED("Bitti", Color(0xFF81C784))     // Yeşil
}

// Veri Modeli
data class BacklogItem(
    val id: Int,
    val gameName: String,
    val note: String,
    val status: GameStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBacklogScreen(onEditClick: () -> Unit) { // Navigasyon parametresi eklendi
    // Örnek Veri Seti (UI Simülasyonu için)
    val myGames = listOf(
        BacklogItem(1, "Elden Ring", "Boss savaşları zor ama atmosfer harika.", GameStatus.PLAYING),
        BacklogItem(2, "The Witcher 3", "Hikaye bitti, yan görevleri temizliyorum.", GameStatus.COMPLETED),
        BacklogItem(3, "Hades II", "Erken erişim çıktığı an başlayacağım.", GameStatus.PLANNED),
        BacklogItem(4, "Cyberpunk 2077", "Phantom Liberty DLC'si bekleniyor.", GameStatus.PLANNED)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kütüphanem", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(myGames) { item ->
                // Her karta tıklama özelliğini ve navigasyonu aktarıyoruz
                BacklogItemCard(item, onEditClick)
            }
        }
    }
}

@Composable
fun BacklogItemCard(item: BacklogItem, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }, // Karta tıklandığında düzenleme ekranına gider
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Başlık ve Durum Rozeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.gameName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Durum Rozeti (Badge)
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Not Alanı Görünümü
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: GameStatus) {
    Surface(
        color = status.color.copy(alpha = 0.15f), // Arka plana çok hafif renk verir
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, status.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = status.color,
            fontWeight = FontWeight.Bold
        )
    }
}