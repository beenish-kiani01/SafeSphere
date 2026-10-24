package com.example.safesphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.safesphere.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

class SafetyVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
            val isDarkMode = prefs.getBoolean("dark_mode", true)
            
            SAfeSphereTheme(darkTheme = isDarkMode) {
                SafetyVerificationScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun SafetyVerificationScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text("Safety Verification", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Trust Score Section
            TrustScoreCard(score = if (user?.isEmailVerified == true) 75 else 40)

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Verification Checklist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    VerificationItem(
                        icon = Icons.Default.Email,
                        title = "Email Verification",
                        subtitle = if (user?.isEmailVerified == true) "Verified" else "Not Verified",
                        isVerified = user?.isEmailVerified ?: false,
                        onClick = {
                            if (user?.isEmailVerified == false) {
                                user.sendEmailVerification()
                            }
                        }
                    )
                }
                item {
                    VerificationItem(
                        icon = Icons.Default.People,
                        title = "Trusted Contacts",
                        subtitle = "Add at least 2 emergency contacts",
                        isVerified = false // Placeholder for actual check
                    )
                }
                item {
                    VerificationItem(
                        icon = Icons.Default.VpnKey,
                        title = "Secure App PIN",
                        subtitle = "Enable PIN lock in settings",
                        isVerified = com.example.safesphere.utils.SecurityUtils.isAppLockEnabled(androidx.compose.ui.platform.LocalContext.current)
                    )
                }
            }
        }
    }
}

@Composable
fun TrustScoreCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your Trust Score", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("$score%", fontSize = 48.sp, fontWeight = FontWeight.Black, color = if (score > 70) SafeGreen else AlertYellow)
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                color = if (score > 70) SafeGreen else AlertYellow,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "A higher score means your safety network is more reliable.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun VerificationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isVerified: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isVerified) SafeGreen else Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}
