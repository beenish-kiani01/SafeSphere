package com.example.safesphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safesphere.ui.theme.BackgroundGradient
import com.example.safesphere.ui.theme.SAfeSphereTheme
import kotlinx.coroutines.delay

import java.util.Locale

class FakeCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAfeSphereTheme {
                FakeCallManager(onExit = { finish() })
            }
        }
    }
}

enum class CallState { RINGING, ACTIVE, ENDED }

@Composable
fun FakeCallManager(onExit: () -> Unit) {
    var callState by remember { mutableStateOf(CallState.RINGING) }
    var secondsPassed by remember { mutableStateOf(0) }

    LaunchedEffect(callState) {
        if (callState == CallState.ACTIVE) {
            while (true) {
                delay(1000)
                secondsPassed++
            }
        }
    }

    when (callState) {
        CallState.RINGING -> {
            IncomingCallScreen(
                onAccept = { callState = CallState.ACTIVE },
                onDecline = onExit
            )
        }
        CallState.ACTIVE -> {
            ActiveCallScreen(
                seconds = secondsPassed,
                onHangUp = onExit
            )
        }
        CallState.ENDED -> onExit()
    }
}

@Composable
fun IncomingCallScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Profile Picture Placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Ammi",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Incoming Call...",
                fontSize = 18.sp,
                color = Color.LightGray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallActionCircle(
                    icon = Icons.Default.CallEnd,
                    color = Color(0xFFFF5252),
                    label = "Decline",
                    onClick = onDecline
                )
                
                CallActionCircle(
                    icon = Icons.Default.Call,
                    color = Color(0xFF4CAF50),
                    label = "Accept",
                    onClick = onAccept
                )
            }
        }
    }
}

@Composable
fun ActiveCallScreen(seconds: Int, onHangUp: () -> Unit) {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Solid dark for active call
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "Ammi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = timeString,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Realistic Call Grid
            Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CallOptionItem(Icons.Default.MicOff, "Mute")
                    CallOptionItem(Icons.Default.Dialpad, "Keypad")
                    CallOptionItem(Icons.Default.VolumeUp, "Speaker")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CallOptionItem(Icons.Default.Add, "Add Call")
                    CallOptionItem(Icons.Default.VideoCall, "Video")
                    CallOptionItem(Icons.Default.Contacts, "Contacts")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CallActionCircle(
                icon = Icons.Default.CallEnd,
                color = Color(0xFFFF5252),
                label = "Hang Up",
                onClick = onHangUp,
                size = 76.dp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun CallActionCircle(
    icon: ImageVector,
    color: Color,
    label: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 72.dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(size)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(size * 0.5f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun CallOptionItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}
