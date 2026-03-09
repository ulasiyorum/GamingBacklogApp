package com.ulasiyorum.gamingbacklogapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onContinueLogin: () -> Unit,
    onContinueWithoutLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Gaming Backlog", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onContinueLogin) {
            Text("Giriş Yap")
        }

        TextButton(onClick = onContinueWithoutLogin) {
            Text("Giriş Yapmadan Devam Et")
        }
    }
}