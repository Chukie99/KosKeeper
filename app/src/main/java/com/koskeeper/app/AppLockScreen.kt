package com.koskeeper.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLockScreen(
    onUnlock: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val correctPin = "1234" // Default PIN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Masukkan PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "PIN default: 1234",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PIN dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pin.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        if (error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "PIN salah!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Number pad
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberButton("1") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("2") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("3") { pin = if (pin.length < 4) pin + it else pin }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberButton("4") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("5") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("6") { pin = if (pin.length < 4) pin + it else pin }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberButton("7") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("8") { pin = if (pin.length < 4) pin + it else pin }
                NumberButton("9") { pin = if (pin.length < 4) pin + it else pin }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(72.dp))
                NumberButton("0") { pin = if (pin.length < 4) pin + it else pin }
                IconButton(
                    onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Backspace, "Hapus", modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        Button(
            onClick = {
                if (pin == correctPin) {
                    onUnlock()
                } else {
                    error = true
                    pin = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = pin.length == 4,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Buka", fontSize = 18.sp)
        }
    }
}

@Composable
fun NumberButton(number: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
