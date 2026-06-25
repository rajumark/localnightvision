package com.localnightimage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localnightimage.platform.rememberCameraPermissionRequester

@Composable
fun PermissionScreen(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val requestPermission = rememberCameraPermissionRequester(
        onGranted = onGranted,
        onDenied = onDenied
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Local Night Vision",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Camera permission required\nto enhance low-light images",
            fontSize = 16.sp,
            color = Color(0xAAFFFFFF),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        FilledIconButton(
            onClick = { requestPermission() },
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF6C63FF)
            )
        ) {
            Text("📷", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap to enable camera",
            fontSize = 14.sp,
            color = Color(0xFF6C63FF),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your photos are processed entirely on-device.\nNo data leaves your phone.",
            fontSize = 12.sp,
            color = Color(0x66FFFFFF),
            textAlign = TextAlign.Center
        )
    }
}
