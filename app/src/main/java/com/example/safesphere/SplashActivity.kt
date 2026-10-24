package com.example.safesphere

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safesphere.ui.theme.BackgroundGradient
import com.example.safesphere.ui.theme.SAfeSphereTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAfeSphereTheme {
                SplashScreenContent {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val isAppLockEnabled = com.example.safesphere.utils.SecurityUtils.isAppLockEnabled(this@SplashActivity)
                    
                    val nextActivity = when {
                        currentUser == null -> LoginActivity::class.java
                        isAppLockEnabled -> LockActivity::class.java
                        else -> MainActivity::class.java
                    }
                    
                    startActivity(Intent(this@SplashActivity, nextActivity))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreenContent(onTimeout: () -> Unit) {
    val girlAlpha = remember { Animatable(0f) }
    val handsOffset = remember { Animatable(80f) } 
    val pulseScale = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Girl silhouette fades in first
        girlAlpha.animateTo(1f, animationSpec = tween(1200, easing = LinearOutSlowInEasing))
        
        // 2. Protective hands move inward calmly
        handsOffset.animateTo(0f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
        
        // 3. Subtle neon pulse and glow appear
        glowAlpha.animateTo(0.3f, animationSpec = tween(800))
        
        pulseScale.animateTo(1.03f, animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ))
    }

    LaunchedEffect(Unit) {
        delay(1800) // Wait for protective logo to form
        textAlpha.animateTo(1f, animationSpec = tween(1000))
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 60.dp) // Lift logo slightly above center
        ) {
            // Animated Logo Container
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale.value),
                contentAlignment = Alignment.Center
            ) {
                // Compatible back-glow using radial gradient
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(glowAlpha.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Girl Silhouette (Center)
                Image(
                    painter = painterResource(id = R.drawable.girl_silhouette),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(girlAlpha.value)
                )
                
                // Left Protective Hand
                Image(
                    painter = painterResource(id = R.drawable.left_hand),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = (-handsOffset.value).dp)
                        .alpha(if (handsOffset.value < 80f) 1f else 0f)
                )
                
                // Right Protective Hand
                Image(
                    painter = painterResource(id = R.drawable.right_hand),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = handsOffset.value.dp)
                        .alpha(if (handsOffset.value < 80f) 1f else 0f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "SafeSphere",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Not every route is safe.\nChoose wisely",
                    fontSize = 17.sp,
                    color = Color.LightGray.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
