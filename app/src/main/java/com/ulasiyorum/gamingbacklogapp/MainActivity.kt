package com.ulasiyorum.gamingbacklogapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ulasiyorum.gamingbacklogapp.ui.MainScreen
import com.ulasiyorum.gamingbacklogapp.ui.theme.GamingBacklogAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamingBacklogAppTheme {
                MainScreen()
            }
        }
    }
}