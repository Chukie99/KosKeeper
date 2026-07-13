package com.koskeeper.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1500)
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF059669)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .size(120.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "KosKeeper",
            modifier = Modifier
                .alpha(alpha.value),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Sistem Manajemen Kos",
            modifier = Modifier
                .alpha(alpha.value),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
