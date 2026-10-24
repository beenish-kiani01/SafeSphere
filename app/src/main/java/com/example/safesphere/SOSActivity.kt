package com.example.safesphere

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.safesphere.data.ContactRepository
import com.example.safesphere.ui.theme.*
import com.example.safesphere.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SOSActivity : ComponentActivity() {
    private val contactRepository = ContactRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val isOffline = !NetworkUtils.isInternetAvailable(context)
            
            // Runtime Permission Launcher
            val smsPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "SMS Permission is required for emergency alerts", Toast.LENGTH_LONG).show()
                }
            }

            // Check and Request Permission on Start
            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                }
            }

            SAfeSphereTheme {
                SOSScreen(
                    isOffline = isOffline,
                    onCancel = { finish() },
                    onAlertSent = {
                        triggerVibration()
                        sendEmergencyAlerts()
                    }
                )
            }
        }
    }

    private fun sendEmergencyAlerts() {
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "A SafeSphere User"
        val message = "EMERGENCY: $userName may be in danger. Check their location immediately. (SafeSphere SOS)"
        
        lifecycleScope.launch {
            try {
                // 1. Check Permissions
                if (ContextCompat.checkSelfPermission(this@SOSActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@SOSActivity, "Cannot send SMS: Permission Denied", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 2. Fetch Contacts
                val contacts = contactRepository.getContacts().first()
                if (contacts.isEmpty()) {
                    Toast.makeText(this@SOSActivity, "No emergency contacts found!", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // 3. Robust SmsManager Initialization
                val smsManager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // API 31+ modern way
                    this@SOSActivity.getSystemService(SmsManager::class.java)
                } else {
                    // Legacy way (Safe for Android 6 to 11)
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                if (smsManager == null) {
                    Toast.makeText(this@SOSActivity, "SMS Service unavailable on this device", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 4. Send SMS to all contacts
                var sentCount = 0
                contacts.forEach { contact ->
                    if (contact.phone.isNotBlank()) {
                        try {
                            smsManager.sendTextMessage(contact.phone, null, message, null, null)
                            sentCount++
                        } catch (e: Exception) {
                            // Log specific contact error but continue loop
                        }
                    }
                }
                
                if (sentCount > 0) {
                    Toast.makeText(this@SOSActivity, "Danger alert sent to $sentCount contacts", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SOSActivity, "Failed to send alerts: Check SIM card", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SOSActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerVibration() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}

@Composable
fun SOSScreen(isOffline: Boolean, onCancel: () -> Unit, onAlertSent: () -> Unit) {
    var countdown by remember { mutableStateOf(5) }
    var isAlertActive by remember { mutableStateOf(false) }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        isAlertActive = true
        onAlertSent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isAlertActive) Color(0xFF420000) else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray.copy(alpha = 0.8f))
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                Text(
                    "OFFLINE MODE: SMS Backup Active",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            if (!isAlertActive) {
                Text(
                    text = "Emergency SOS Activated",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Notifying your emergency contacts in...",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(60.dp))
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(SOSGradient, CircleShape)
                ) {
                    Text(
                        text = countdown.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(80.dp))
                
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("CANCEL", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            } else {
                // Active Alert State
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(120.dp).scale(scale)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "DANGER ALERT SENT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                
                SOSQuickAction(Icons.Default.LocalPolice, "Call Police", Color(0xFFE53935)) { /* Simulate Call */ }
                Spacer(modifier = Modifier.height(16.dp))
                SOSQuickAction(Icons.Default.Phone, "Call Trusted Contact", Color(0xFF43A047)) { /* Call */ }
                Spacer(modifier = Modifier.height(16.dp))
                SOSQuickAction(Icons.Default.ShareLocation, "Share Live Location", Color(0xFF1E88E5)) { /* Share */ }
                
                Spacer(modifier = Modifier.height(60.dp))
                
                TextButton(onClick = onCancel) {
                    Text("I AM SAFE NOW", color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SOSQuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
