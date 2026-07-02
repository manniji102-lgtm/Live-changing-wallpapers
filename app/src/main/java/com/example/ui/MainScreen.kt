package com.example.ui

import android.app.WallpaperInfo
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NotificationSound
import com.example.data.WallpaperVideo
import com.example.service.NotificationSoundService
import com.example.service.VideoWallpaperService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val sounds by viewModel.sounds.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Wallpapers, 1 = Sounds, 2 = System Status

    // Dynamic Permission Statuses
    var isWallpaperActive by remember { mutableStateOf(false) }
    var isNotificationAccessGranted by remember { mutableStateOf(false) }
    var isWriteSettingsGranted by remember { mutableStateOf(false) }

    // Periodic check for permission statuses
    LaunchedEffect(Unit) {
        while (true) {
            isWallpaperActive = checkWallpaperServiceActive(context)
            isNotificationAccessGranted = checkNotificationAccessGranted(context)
            isWriteSettingsGranted = checkWriteSettingsGranted(context)
            kotlinx.coroutines.delay(2000)
        }
    }

    // SAF Document Pickers
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("MainScreen", "Failed to persist URI permission", e)
            }
            viewModel.addWallpaper(it)
        }
    }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("MainScreen", "Failed to persist URI permission", e)
            }
            viewModel.addSound(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFD0BCFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Vibe Engine",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFE6E1E5)
                            )
                        }

                        // Optimized badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF49454F))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF4ADE80)) // green-400
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OPTIMIZED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF2B2930),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(if (activeTab == 0) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary, contentDescription = "Wallpapers") },
                    label = { Text("Wallpapers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFC9C5D0),
                        unselectedTextColor = Color(0xFFC9C5D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(if (activeTab == 1) Icons.Filled.Audiotrack else Icons.Outlined.Audiotrack, contentDescription = "Sounds") },
                    label = { Text("Sounds") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFC9C5D0),
                        unselectedTextColor = Color(0xFFC9C5D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(if (activeTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Engine") },
                    label = { Text("Engine") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFC9C5D0),
                        unselectedTextColor = Color(0xFFC9C5D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1C1B1F))
        ) {
            when (activeTab) {
                0 -> WallpaperTabContent(
                    wallpapers = wallpapers,
                    isActiveService = isWallpaperActive,
                    onAddClick = {
                        wallpaperPickerLauncher.launch(arrayOf("video/*"))
                    },
                    onToggleActive = { id, active -> viewModel.toggleWallpaperActive(id, active) },
                    onDelete = { id -> viewModel.deleteWallpaper(id) },
                    onSetupWallpaperClick = { triggerWallpaperSetup(context) }
                )
                1 -> SoundTabContent(
                    sounds = sounds,
                    isListenerGranted = isNotificationAccessGranted,
                    isWriteGranted = isWriteSettingsGranted,
                    onAddClick = {
                        soundPickerLauncher.launch(arrayOf("audio/*"))
                    },
                    onToggleActive = { id, active -> viewModel.toggleSoundActive(id, active) },
                    onDelete = { id -> viewModel.deleteSound(id) },
                    onGrantListenerClick = { triggerNotificationAccessSetup(context) },
                    onGrantWriteClick = { triggerWriteSettingsSetup(context) }
                )
                2 -> EngineSettingsContent(
                    isWallpaperActive = isWallpaperActive,
                    isNotificationAccessGranted = isNotificationAccessGranted,
                    isWriteSettingsGranted = isWriteSettingsGranted,
                    wallpaperCount = wallpapers.filter { it.isActive }.size,
                    soundCount = sounds.filter { it.isActive }.size,
                    onSetupWallpaperClick = { triggerWallpaperSetup(context) },
                    onGrantListenerClick = { triggerNotificationAccessSetup(context) },
                    onGrantWriteClick = { triggerWriteSettingsSetup(context) }
                )
            }
        }
    }
}

@Composable
fun WallpaperTabContent(
    wallpapers: List<WallpaperVideo>,
    isActiveService: Boolean,
    onAddClick: () -> Unit,
    onToggleActive: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onSetupWallpaperClick: () -> Unit
) {
    val activeCount = wallpapers.count { it.isActive }
    val meetsRequirement = wallpapers.size >= 5

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headers matching the layout: "Video Wallpaper Playlist" / "Swaps on Power On"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "VIDEO WALLPAPER PLAYLIST",
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Swaps on Power On",
                    color = Color(0xFF938F99),
                    fontSize = 11.sp
                )
            }
        }

        // Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActiveService) Color(0xFF2B2930) else Color(0x33F44336)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isActiveService) Color(0xFF49454F) else Color(0x66F44336), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isActiveService) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isActiveService) Color(0xFF4ADE80) else Color(0xFFE57373),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isActiveService) "Video Wallpaper is Active" else "Wallpaper Service Inactive",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = if (isActiveService) "Active & cycling videos on each power-on event." else "Tap activate to setup the system engine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF938F99)
                        )
                    }
                    if (!isActiveService) {
                        Button(
                            onClick = onSetupWallpaperClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373),
                                contentColor = Color(0xFF1C1B1F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wallpaper_setup_btn")
                        ) {
                            Text("Activate", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Requirements Indicator
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wallpaper List (${wallpapers.size}/5 Required)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE6E1E5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add at least 5 videos from your gallery. Active videos will cycle consecutively when you power on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF938F99)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (wallpapers.size.toFloat() / 5f).coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (meetsRequirement) Color(0xFFD0BCFF) else Color(0xFF49454F),
                        trackColor = Color(0xFF1C1B1F)
                    )
                }
            }
        }

        // Add Button (Styled like the dashed border button / high fidelity action button)
        item {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFD0BCFF)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFF381E72), Color(0xFFD0BCFF))), RoundedCornerShape(20.dp))
                    .testTag("add_wallpaper_btn"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFFD0BCFF))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Add Video from Gallery", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        if (wallpapers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No videos added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF938F99)
                        )
                    }
                }
            }
        } else {
            items(wallpapers, key = { it.id }) { wallpaper ->
                WallpaperItemRow(
                    wallpaper = wallpaper,
                    onToggleActive = { active -> onToggleActive(wallpaper.id, active) },
                    onDelete = { onDelete(wallpaper.id) }
                )
            }
        }
    }
}

@Composable
fun WallpaperItemRow(
    wallpaper: WallpaperVideo,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2930)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1B1F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallpaper.displayName,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6E1E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (wallpaper.isActive) "Active in cycle" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (wallpaper.isActive) Color(0xFF4ADE80) else Color(0xFF938F99)
                )
            }
            Switch(
                checked = wallpaper.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFD0BCFF),
                    checkedTrackColor = Color(0xFF381E72),
                    uncheckedThumbColor = Color(0xFF938F99),
                    uncheckedTrackColor = Color(0xFF1C1B1F)
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete wallpaper",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun SoundTabContent(
    sounds: List<NotificationSound>,
    isListenerGranted: Boolean,
    isWriteGranted: Boolean,
    onAddClick: () -> Unit,
    onToggleActive: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onGrantListenerClick: () -> Unit,
    onGrantWriteClick: () -> Unit
) {
    val meetsRequirement = sounds.size >= 3

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headers matching the design layout: "Smart Audio Rotation" / "Sounds Configured"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "SMART AUDIO ROTATION",
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${sounds.size} Sounds Configured",
                    color = Color(0xFF938F99),
                    fontSize = 11.sp
                )
            }
        }

        // Notification Listener Alert
        if (!isListenerGranted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x22F44336)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x55F44336), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NotificationImportant,
                                contentDescription = null,
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Notification Access Required",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To change the sound whenever a new notification arrives, AuraWallpaper needs permission to detect notifications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF938F99)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onGrantListenerClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373),
                                contentColor = Color(0xFF1C1B1F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("permission_notification_btn")
                        ) {
                            Text("Grant Access", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Settings Write Alert
        if (!isWriteGranted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2B2930)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SettingsSuggest,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Modify Settings (Optional)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To dynamically swap the global system notification ringtone, please allow modifying system settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF938F99)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onGrantWriteClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A4458),
                                contentColor = Color(0xFFD0BCFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("permission_settings_btn")
                        ) {
                            Text("Allow Modifying Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Requirements Indicator
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notification Sounds (${sounds.size}/3 Required)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE6E1E5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add at least 3 audio tracks. Active sounds will cycle sequentially on every incoming notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF938F99)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (sounds.size.toFloat() / 3f).coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (meetsRequirement) Color(0xFFD0BCFF) else Color(0xFF49454F),
                        trackColor = Color(0xFF1C1B1F)
                    )
                }
            }
        }

        // Add Button
        item {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFD0BCFF)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFF381E72), Color(0xFFD0BCFF))), RoundedCornerShape(20.dp))
                    .testTag("add_sound_btn"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFFD0BCFF))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Select Sound from Files", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        if (sounds.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Audiotrack,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notification sounds added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF938F99)
                        )
                    }
                }
            }
        } else {
            items(sounds, key = { it.id }) { sound ->
                SoundItemRow(
                    sound = sound,
                    onToggleActive = { active -> onToggleActive(sound.id, active) },
                    onDelete = { onDelete(sound.id) }
                )
            }
        }
    }
}

@Composable
fun SoundItemRow(
    sound: NotificationSound,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2930)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1B1F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sound.displayName,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6E1E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (sound.isActive) "Active in cycle" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sound.isActive) Color(0xFF4ADE80) else Color(0xFF938F99)
                )
            }
            Switch(
                checked = sound.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFD0BCFF),
                    checkedTrackColor = Color(0xFF381E72),
                    uncheckedThumbColor = Color(0xFF938F99),
                    uncheckedTrackColor = Color(0xFF1C1B1F)
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete sound",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun EngineSettingsContent(
    isWallpaperActive: Boolean,
    isNotificationAccessGranted: Boolean,
    isWriteSettingsGranted: Boolean,
    wallpaperCount: Int,
    soundCount: Int,
    onSetupWallpaperClick: () -> Unit,
    onGrantListenerClick: () -> Unit,
    onGrantWriteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Battery-Optimized Engine Status",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFE6E1E5),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Engine highlights card matching CSS bg-gradient-to-br from-[#381E72] to-[#4F378B]
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF381E72), Color(0xFF4F378B))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ENGINE EFFICIENCY",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "98.4% optimized",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "• Auto-Sleep: Video rendering pauses instantly when screen goes off or you enter other apps, consuming 0% background CPU.\n" +
                               "• Hardware Acceleration: Decodes straight through native Android framework pipeline for highest energy-efficiency.\n" +
                               "• Event-Driven Observer: Restricts wake-locks by listening strictly to system events without running constant awake threads.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Active Status Checklist in a clean outline container
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "System Integration Checklist",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE6E1E5),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    StatusRow(
                        label = "Video Wallpaper Service",
                        isActive = isWallpaperActive,
                        detail = "Triggers wallpaper swap on screen power on",
                        onActionClick = onSetupWallpaperClick,
                        actionLabel = "Configure"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color(0xFF49454F)
                    )

                    StatusRow(
                        label = "Notification Service",
                        isActive = isNotificationAccessGranted,
                        detail = "Cycles sound on every incoming notification",
                        onActionClick = onGrantListenerClick,
                        actionLabel = "Grant Access"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color(0xFF49454F)
                    )

                    StatusRow(
                        label = "System Settings Permission",
                        isActive = isWriteSettingsGranted,
                        detail = "Synchronizes default system notification sounds",
                        onActionClick = onGrantWriteClick,
                        actionLabel = "Allow Modify"
                    )
                }
            }
        }

        // Current Pool Statistics
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Media Statistics",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE6E1E5),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("• Wallpaper Videos Active: $wallpaperCount (Min 5 recommended)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF938F99))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Notification Sounds Active: $soundCount (Min 3 recommended)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF938F99))
                }
            }
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    isActive: Boolean,
    detail: String,
    onActionClick: () -> Unit,
    actionLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (isActive) Color(0xFF4ADE80) else Color(0xFFE57373),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE6E1E5)
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF938F99)
            )
        }
        if (!isActive) {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A4458),
                    contentColor = Color(0xFFD0BCFF)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Utility functions to check service/permission statuses
fun checkWallpaperServiceActive(context: Context): Boolean {
    val wallpaperManager = WallpaperManager.getInstance(context)
    val info: WallpaperInfo? = wallpaperManager.wallpaperInfo
    return info != null && info.packageName == context.packageName && info.serviceName == VideoWallpaperService::class.java.name
}

fun checkNotificationAccessGranted(context: Context): Boolean {
    val contentResolver = context.contentResolver
    val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return enabledListeners != null && enabledListeners.contains(context.packageName)
}

fun checkWriteSettingsGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.System.canWrite(context)
    } else {
        true
    }
}

// Utility functions to open settings
fun triggerWallpaperSetup(context: Context) {
    try {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, VideoWallpaperService::class.java)
            )
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to wallpaper selector
        try {
            val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            context.startActivity(intent)
        } catch (ex: Exception) {
            Log.e("MainScreen", "Failed to open wallpaper settings", ex)
        }
    }
}

fun triggerNotificationAccessSetup(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("MainScreen", "Failed to open notification settings", e)
    }
}

fun triggerWriteSettingsSetup(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainScreen", "Failed to open write settings permission screen", e)
        }
    }
}

