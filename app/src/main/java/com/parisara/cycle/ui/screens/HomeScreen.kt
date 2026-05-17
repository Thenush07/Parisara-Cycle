package com.parisara.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parisara.cycle.data.model.EcoCalculator
import com.parisara.cycle.data.repository.ThemeOption
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.DashboardActionCard
import com.parisara.cycle.ui.components.ParisaraCard
import com.parisara.cycle.ui.components.ParisaraLoadingIndicator
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.navigation.NavRoutes
import com.parisara.cycle.ui.viewmodel.HomeViewModel
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    uid: String,
    onNavigate: (String) -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid))
    val state by vm.state.collectAsState()
    val settingsVm: SettingsViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val themeOption by settingsVm.themeOption.collectAsState()

    val isDark = when (themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parisara-Cycle", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Theme toggle button
                    IconButton(onClick = {
                        val newTheme = if (isDark) ThemeOption.LIGHT else ThemeOption.DARK
                        settingsVm.setThemeOption(newTheme)
                    }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    BadgedBox(
                        badge = {
                            if (state.unreadNotifications > 0) {
                                Badge { Text("${state.unreadNotifications}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { onNavigate(NavRoutes.NOTIFICATIONS) }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    IconButton(onClick = { onNavigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true, 
                    onClick = {}, 
                    icon = { Icon(Icons.Default.Home, null) }, 
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(selected = false, onClick = { onNavigate(NavRoutes.MAP_ROUTE) }, icon = { Icon(Icons.Default.Map, null) }, label = { Text("Route") })
                NavigationBarItem(selected = false, onClick = { onNavigate(NavRoutes.ECO_STATS) }, icon = { Icon(Icons.Default.Eco, null) }, label = { Text("Eco") })
                NavigationBarItem(selected = false, onClick = { onNavigate(NavRoutes.PROFILE) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        if (state.isLoading) {
            ParisaraLoadingIndicator(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ParisaraCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    state.profile?.name?.firstOrNull()?.uppercase() ?: "U", 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Welcome, ${state.profile?.name?.split(" ")?.firstOrNull() ?: "Rider"}! 🌿", 
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    state.profile?.city ?: "", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatChip("Distance", "${"%.1f".format(state.profile?.totalDistanceKm ?: 0.0)} km")
                            StatChip("Rides", "${state.profile?.totalRides ?: 0}")
                            StatChip("CO₂ Saved", EcoCalculator.formatCo2(state.profile?.totalCo2SavedGrams ?: 0.0))
                        }
                    }
                }
                item {
                    ParisaraCard {
                        Text(
                            "🌍 Eco Impact Today", 
                            fontWeight = FontWeight.SemiBold, 
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.ecoMessage.ifBlank { "You saved ${EcoCalculator.formatCo2(state.ecoStats.dailyCo2Grams)} CO₂ today" },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (state.ecoStats.dailyDistanceKm / 10.0).coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            "Daily: ${"%.1f".format(state.ecoStats.dailyDistanceKm)} km", 
                            style = MaterialTheme.typography.bodySmall, 
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item { SectionTitle("Interactive Playgrounds 🎮") }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardActionCard("Go Premium HUD", "Realtime ride computer", Icons.Default.Speed, Modifier.weight(1f), onClick = { onNavigate(NavRoutes.RIDE_HUD) })
                        DashboardActionCard("Eco Simulator", "Playground carbon estimate", Icons.Default.Park, Modifier.weight(1f), onClick = { onNavigate(NavRoutes.ECO_SIMULATOR) })
                    }
                }
                item { SectionTitle("Quick Actions") }
                item {
                    DashboardActionCard("Start Safe Route", "Plan a bicycle-friendly path", Icons.Default.Route, onClick = { onNavigate(NavRoutes.MAP_ROUTE) })
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardActionCard("CO₂ Saved Today", EcoCalculator.formatCo2(state.ecoStats.dailyCo2Grams), Icons.Default.Eco, Modifier.weight(1f), onClick = { onNavigate(NavRoutes.ECO_STATS) })
                        DashboardActionCard("Monthly Eco Stats", "${"%.1f".format(state.ecoStats.monthlyDistanceKm)} km", Icons.Default.BarChart, Modifier.weight(1f), onClick = { onNavigate(NavRoutes.ECO_STATS) })
                    }
                }
                item { DashboardActionCard("Pit-Stop Finder", "Repair, water, rest & first-aid", Icons.Default.Place, onClick = { onNavigate(NavRoutes.PIT_STOPS) }) }
                item { DashboardActionCard("Buddy System", "Ride together for better safety", Icons.Default.Group, onClick = { onNavigate(NavRoutes.BUDDY) }) }
                item { DashboardActionCard("Report Road Issue", "Help others avoid hazards", Icons.Default.Report, onClick = { onNavigate(NavRoutes.REPORT) }) }
                item { DashboardActionCard("Safety Tips", "Helmet, rules & visibility", Icons.Default.HealthAndSafety, onClick = { onNavigate(NavRoutes.SAFETY_TIPS) }) }
                item { DashboardActionCard("Ride History", "View past rides & impact", Icons.Default.History, onClick = { onNavigate(NavRoutes.RIDE_HISTORY) }) }

                item { SectionTitle("Explore More") }
                item { DashboardActionCard("Weather for Cycling", "Check today's cycling conditions", Icons.Default.Cloud, onClick = { onNavigate(NavRoutes.WEATHER) }) }
                item { DashboardActionCard("Community Leaderboard", "See top eco riders nearby", Icons.Default.Leaderboard, onClick = { onNavigate(NavRoutes.LEADERBOARD) }) }
                item { DashboardActionCard("Cycling Challenges", "Join weekly green challenges", Icons.Default.EmojiEvents, onClick = { onNavigate(NavRoutes.CHALLENGES) }) }

                if (state.profile?.isAdmin == true) {
                    item { DashboardActionCard("Admin Panel", "Manage reports & zones", Icons.Default.AdminPanelSettings, onClick = { onNavigate(NavRoutes.ADMIN) }) }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title, 
        style = MaterialTheme.typography.titleMedium, 
        fontWeight = FontWeight.SemiBold, 
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}
