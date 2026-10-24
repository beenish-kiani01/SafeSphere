package com.example.safesphere

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safesphere.ui.home.HomeFeature
import com.example.safesphere.ui.home.HomeViewModel
import com.example.safesphere.ui.reports.ReportsScreen
import com.example.safesphere.ui.theme.*
import com.example.safesphere.utils.NetworkUtils
import com.example.safesphere.utils.SecurityUtils
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
            val isDarkMode = remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }
            
            SAfeSphereTheme(darkTheme = isDarkMode.value) {
                MainContainer(homeViewModel, isDarkMode)
            }
        }
    }
}

@Composable
fun MainContainer(homeViewModel: HomeViewModel, isDarkMode: MutableState<Boolean>) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAppOffline = remember { mutableStateOf(!NetworkUtils.isInternetAvailable(context)) }

    val LightBackgroundGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE8EAF6))
    )

    Scaffold(
        bottomBar = {
            ModernBottomNav(selectedTab) { selectedTab = it }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isAppOffline.value) {
                Surface(
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You are offline. Limited features available.",
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode.value) BackgroundGradient else LightBackgroundGradient)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(homeViewModel)
                    1 -> ReportsScreen()
                    2 -> ProfileScreen()
                    3 -> SettingsScreen(isDarkMode)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val features by viewModel.features.collectAsState()
    var showCustomizer by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Left Name & Logo Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SafeSphere",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Features List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(features.filter { it.isVisible }) { feature ->
                PremiumFeatureCard(
                    feature = feature,
                    subtitle = viewModel.getSubtitle(feature.id),
                    onClick = {
                        when (feature.id) {
                            "sos" -> context.startActivity(Intent(context, SOSActivity::class.java))
                            "contacts" -> context.startActivity(Intent(context, EmergencyContactsActivity::class.java))
                            "route" -> context.startActivity(Intent(context, SafeRouteActivity::class.java))
                            "report" -> context.startActivity(Intent(context, ReportActivity::class.java))
                            "fake_call" -> context.startActivity(Intent(context, FakeCallActivity::class.java))
                            "share" -> context.startActivity(Intent(context, ShareLocationActivity::class.java))
                        }
                    }
                )
            }

            item {
                CustomizeButton { showCustomizer = true }
            }
        }
    }

    if (showCustomizer) {
        CustomizerDialog(
            features = features,
            onDismiss = { showCustomizer = false },
            onTogglePin = { viewModel.togglePin(it) },
            onToggleVisibility = { viewModel.toggleVisibility(it) }
        )
    }
}

@Composable
fun PremiumFeatureCard(feature: HomeFeature, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (feature.isPinned) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (feature.id == "sos") Color.Red else MaterialTheme.colorScheme.primary,
                spotColor = if (feature.id == "sos") Color.Red else MaterialTheme.colorScheme.primary
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feature.id == "sos") Color.Transparent else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (feature.id == "sos") Modifier.background(SOSGradient)
                    else Modifier.background(MaterialTheme.colorScheme.surface)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (feature.id == "sos") Color.White.copy(alpha = 0.2f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            tint = if (feature.id == "sos") Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = feature.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = if (feature.id == "sos") Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (feature.id == "sos") Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryPurple.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = PrimaryPurple)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(user?.displayName ?: "User Name", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(user?.email ?: "email@example.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
        ) {
            Text("Edit Profile", color = PrimaryPurple)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        ProfileOptionItem(Icons.Default.VerifiedUser, "Safety Verification") {
            context.startActivity(Intent(context, SafetyVerificationActivity::class.java))
        }
        ProfileOptionItem(Icons.Default.Settings, "Account Settings") {
            context.startActivity(Intent(context, AccountSettingsActivity::class.java))
        }
        ProfileOptionItem(Icons.Default.HelpCenter, "Help & Support") {}
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                auth.signOut()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f), contentColor = ErrorRed)
        ) {
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(isDarkMode: MutableState<Boolean>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    
    var notifications by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var sound by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var lockscreenSOS by remember { mutableStateOf(prefs.getBoolean("lockscreen_sos", false)) }
    
    // Security States
    var appLockEnabled by remember { mutableStateOf(com.example.safesphere.utils.SecurityUtils.isAppLockEnabled(context)) }
    var lockType by remember { mutableStateOf(com.example.safesphere.utils.SecurityUtils.getLockType(context)) }
    var showPinDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        
        // ... (Theme section remains same)
        
        Text("General", color = PrimaryPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Theme", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkMode.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkMode.value,
                    onCheckedChange = { 
                        isDarkMode.value = it
                        prefs.edit().putBoolean("dark_mode", it).apply()
                    }
                )
            }
        }

        SettingToggle("Notifications", notifications) { 
            notifications = it
            prefs.edit().putBoolean("notifications", it).apply()
        }
        SettingToggle("Emergency Sound", sound) { 
            sound = it
            prefs.edit().putBoolean("sound", it).apply()
        }
        SettingToggle("Lockscreen SOS Shortcut", lockscreenSOS) { 
            lockscreenSOS = it
            prefs.edit().putBoolean("lockscreen_sos", it).apply()
            
            val intent = Intent(context, com.example.safesphere.services.SOSNotificationService::class.java)
            if (it) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Security", color = PrimaryPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("App Lock", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            Switch(
                checked = appLockEnabled,
                onCheckedChange = { 
                    if (it && com.example.safesphere.utils.SecurityUtils.getUserPin(context) == null) {
                        showPinDialog = true
                    } else {
                        appLockEnabled = it
                        com.example.safesphere.utils.SecurityUtils.setAppLockEnabled(context, it)
                    }
                }
            )
        }

        if (appLockEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lock Type", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Row {
                    FilterChip(
                        selected = lockType == "PIN",
                        onClick = { 
                            lockType = "PIN"
                            com.example.safesphere.utils.SecurityUtils.setLockType(context, "PIN")
                        },
                        label = { Text("PIN") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = lockType == "BIOMETRIC",
                        onClick = { 
                            if (com.example.safesphere.utils.SecurityUtils.isBiometricAvailable(context)) {
                                lockType = "BIOMETRIC"
                                com.example.safesphere.utils.SecurityUtils.setLockType(context, "BIOMETRIC")
                            } else {
                                Toast.makeText(context, "Biometrics not available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text("Biometric") }
                    )
                }
            }

            TextButton(onClick = { showPinDialog = true }) {
                Text("Change App PIN", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("App Information", color = PrimaryPurple, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Version", color = MaterialTheme.colorScheme.onBackground)
            Text("1.0.0-beta", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }

        if (showPinDialog) {
            SetPinDialog(
                onDismiss = { showPinDialog = false },
                onPinSet = { pin ->
                    com.example.safesphere.utils.SecurityUtils.setUserPin(context, pin)
                    com.example.safesphere.utils.SecurityUtils.setAppLockEnabled(context, true)
                    appLockEnabled = true
                    showPinDialog = false
                    Toast.makeText(context, "PIN set successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun SetPinDialog(onDismiss: () -> Unit, onPinSet: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set 4-Digit PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("Enter PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (pin.length == 4 && pin == confirmPin) onPinSet(pin)
                else if (pin != confirmPin) { /* Show error */ }
            }) {
                Text("Set PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomizeButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Icon(Icons.Default.Tune, contentDescription = null, tint = Color.LightGray)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Customize Layout", color = Color.LightGray)
    }
}

@Composable
fun ModernBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = SurfaceGrey.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, 0),
            Triple("Reports", Icons.Default.Security, 1),
            Triple("Profile", Icons.Default.Person, 2),
            Triple("Settings", Icons.Default.Settings, 3)
        )

        items.forEach { (label, icon, index) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    selectedTextColor = PrimaryPurple,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = PrimaryPurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun CustomizerDialog(
    features: List<HomeFeature>,
    onDismiss: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleVisibility: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Layout Settings") },
        text = {
            LazyColumn {
                items(features) { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(feature.icon, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(feature.title, modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = { onTogglePin(feature.id) }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                tint = if (feature.isPinned) PrimaryPurple else Color.Gray
                            )
                        }
                        
                        Switch(
                            checked = feature.isVisible,
                            onCheckedChange = { onToggleVisibility(feature.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Save") }
        },
        containerColor = SurfaceGrey,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
