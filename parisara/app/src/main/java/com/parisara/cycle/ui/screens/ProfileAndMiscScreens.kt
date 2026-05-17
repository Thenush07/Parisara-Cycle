package com.parisara.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.parisara.cycle.data.model.ACHIEVEMENTS
import com.parisara.cycle.data.model.EcoCalculator
import com.parisara.cycle.data.model.ReportStatus
import com.parisara.cycle.data.repository.SampleData
import com.parisara.cycle.data.repository.ThemeOption
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.EmptyState
import com.parisara.cycle.ui.components.ParisaraCard
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.*
import com.parisara.cycle.ui.navigation.NavRoutes
import com.google.firebase.firestore.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(container: AppContainer, uid: String, onBack: () -> Unit, onLogout: () -> Unit, onNavigate: (String) -> Unit = {}) {
    val vm: ProfileViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid))
    val profile by vm.profile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ParisaraCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer), 
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile?.profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile?.profileImageUrl,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    profile?.name?.firstOrNull()?.uppercase() ?: "U", 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                profile?.name ?: "", 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                profile?.email ?: "", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${profile?.city ?: ""} • ${profile?.phone ?: ""}", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                ParisaraCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProfileStat("Rides", "${profile?.totalRides ?: 0}")
                        ProfileStat("Distance", "${"%.1f".format(profile?.totalDistanceKm ?: 0.0)} km")
                        ProfileStat("CO₂", EcoCalculator.formatCo2(profile?.totalCo2SavedGrams ?: 0.0))
                    }
                }
            }
            item { 
                Text(
                    "Achievements", 
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            }
            items(ACHIEVEMENTS) { ach ->
                val unlocked = profile?.achievements?.contains(ach.id) == true
                ParisaraCard {
                    Text(
                        "${ach.icon} ${ach.title}", 
                        color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Text(
                    "More Options", 
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item {
                ParisaraCard(onClick = { onNavigate(NavRoutes.EDIT_PROFILE) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Edit Profile", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                ParisaraCard(onClick = { onNavigate(NavRoutes.SETTINGS) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("App Settings & Theme", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = onLogout, 
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(container: AppContainer, uid: String, onBack: () -> Unit) {
    val vm: RideHistoryViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid))
    val rides by vm.rides.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride History") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (rides.isEmpty()) {
            EmptyState(Icons.Default.History, "No Rides Yet", "Complete a route to see your history here.", "Plan a Route", onBack)
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp), 
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rides) { ride ->
                    ParisaraCard {
                        Text(
                            "${ride.sourceName} → ${ride.destinationName}", 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${"%.1f".format(ride.distanceKm)} km • ${ride.durationMinutes} min • Safety ${ride.safetyScore}%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "CO₂ saved: ${EcoCalculator.formatCo2(ride.co2SavedGrams)}", 
                            color = MaterialTheme.colorScheme.primary, 
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            ride.completedAt?.toDate()?.toString()?.take(16) ?: "", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyTipsScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SafetyTipsViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val aiTip by vm.aiTip.collectAsState()
    val loading by vm.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Tips") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ParisaraCard {
                    Text("🤖 AI Safety Coach", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { vm.generatePersonalizedTip("urban cycling in India at night") }, 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Get Personalized Tips")
                    }
                    if (loading) CircularProgressIndicator(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary)
                    aiTip?.let { 
                        Text(
                            it, 
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    }
                }
            }
            items(SampleData.safetyTips) { tip ->
                ParisaraCard {
                    Text(
                        "${tip.icon} ${tip.title}", 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tip.content, 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        tip.category, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(container: AppContainer, uid: String, onBack: () -> Unit) {
    val notifs = remember { mutableStateListOf<com.parisara.cycle.data.model.AppNotification>() }
    LaunchedEffect(uid) {
        container.notificationRepository.observeNotifications(uid).collect { notifs.clear(); notifs.addAll(it) }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (notifs.isEmpty()) {
            EmptyState(Icons.Default.Notifications, "No Notifications", "Route alerts and buddy requests will appear here.")
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp), 
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifs) { n ->
                    ParisaraCard {
                        Text(
                            n.title, 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            n.message, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            n.type, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val notificationsEnabled by vm.notificationsEnabled.collectAsState()
    val themeOption by vm.themeOption.collectAsState()
    var expandedThemeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Preferences", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            item {
                ParisaraCard {
                    SettingsRow("Push Notifications", notificationsEnabled) { vm.setNotificationsEnabled(it) }
                }
            }
            item {
                Text("Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ParisaraCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Theme", color = MaterialTheme.colorScheme.onSurface)
                        Box {
                            TextButton(onClick = { expandedThemeDropdown = true }) {
                                Text(themeOption.name, color = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(
                                expanded = expandedThemeDropdown,
                                onDismissRequest = { expandedThemeDropdown = false }
                            ) {
                                ThemeOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.name) },
                                        onClick = { 
                                            vm.setThemeOption(option)
                                            expandedThemeDropdown = false 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Support & Legal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ParisaraCard(onClick = { /* Placeholder for Help */ }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Help & Support", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                ParisaraCard(onClick = { /* Placeholder for Privacy Policy */ }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Policy, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Privacy Policy", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                ParisaraCard(onClick = { /* Placeholder for Rate App */ }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Rate the App", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                ParisaraCard(onClick = { /* Placeholder for Share */ }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Share with Friends", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item {
                ParisaraCard {
                    Text(
                        "About Parisara-Cycle", 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "MindMatrix VTU Internship Project\nVersion 1.0.0\nGreen Commuter Guide for safe bicycle commuting.", 
                        style = MaterialTheme.typography.bodySmall, 
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked, 
            onCheckedChange = onChecked, 
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: AdminViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val reports by vm.reports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { 
                Text(
                    "Route Reports", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            }
            items(reports) { report ->
                ParisaraCard {
                    Text(
                        "${report.issueType} - ${report.status}", 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        report.description, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "By ${report.userName}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (report.status == ReportStatus.PENDING) {
                        Row {
                            TextButton(onClick = { vm.updateReportStatus(report.id, ReportStatus.VERIFIED) }) { 
                                Text("Verify", color = MaterialTheme.colorScheme.primary) 
                            }
                            TextButton(onClick = { vm.updateReportStatus(report.id, ReportStatus.RESOLVED) }) { 
                                Text("Resolve", color = MaterialTheme.colorScheme.primary) 
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        vm.addDangerZone("Admin Zone", "Manually added danger zone", "high", GeoPoint(12.97, 77.59))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Add Sample Danger Zone", color = MaterialTheme.colorScheme.onPrimary) }
            }
            item {
                Button(
                    onClick = {
                        vm.addPitStop(
                            com.parisara.cycle.data.model.PitStop(
                                name = "Admin Pit-Stop",
                                category = "Repair Shop",
                                address = "Added via Admin Panel",
                                location = GeoPoint(12.9720, 77.5950),
                                operatingHours = "9 AM - 7 PM"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Add Sample Pit-Stop", color = MaterialTheme.colorScheme.onSecondary) }
            }
        }
    }
}
