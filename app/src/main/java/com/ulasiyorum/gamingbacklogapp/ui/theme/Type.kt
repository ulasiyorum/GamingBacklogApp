package com.ulasiyorum.gamingbacklogapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ulasiyorum.gamingbacklogapp.R

val Alexandria = FontFamily(
    Font(R.font.alexandria, FontWeight.Normal),
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Alexandria,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Alexandria,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Alexandria,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )
)