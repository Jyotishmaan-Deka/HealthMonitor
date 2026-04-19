package com.healthmonitor.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // Logo scale-in
    val logoScale = remember { Animatable(0.3f) }
    // Content alpha fade-in
    val contentAlpha = remember { Animatable(0f) }
    // Tagline slides up alpha
    val taglineAlpha = remember { Animatable(0f) }

    // Continuous heartbeat pulse on the icon ring
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        // Logo bounce in
        logoScale.animateTo(1f, animationSpec = tween(600, easing = EaseOutBack))
        // Fade in the text content
        contentAlpha.animateTo(1f, animationSpec = tween(400))
        delay(200)
        taglineAlpha.animateTo(1f, animationSpec = tween(400))
        // Hold for a moment then navigate
        delay(1000)
        onSplashComplete()
    }

    val gradientStart = MaterialTheme.colorScheme.primary
    val gradientEnd   = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientStart, gradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Pulsing ring behind icon ───────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Outer pulse ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .alpha(0.25f)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                // Icon container
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(logoScale.value)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector    = Icons.Default.Favorite,
                        contentDescription = "Health Monitor",
                        tint           = MaterialTheme.colorScheme.onPrimary,
                        modifier       = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── App name ──────────────────────────────────────────────────
            Text(
                text      = "Health Monitor",
                style     = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color     = MaterialTheme.colorScheme.onPrimary,
                modifier  = Modifier.alpha(contentAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Text(
                text      = "Your personal health companion",
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(taglineAlpha.value)
            )
        }

        // ── Version label at bottom ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text   = "v1.0.0",
                style  = MaterialTheme.typography.labelMedium,
                color  = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.alpha(contentAlpha.value)
            )
        }
    }
}
