package com.parisara.cycle.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.ParisaraCard
import kotlinx.coroutines.launch

// ─── Weather for Cycling ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(onBack: () -> Unit) {
    val weatherData = remember {
        listOf(
            WeatherItem("Now", "☀️ Sunny", "32°C", "Wind: 12 km/h NW", "Great for cycling!"),
            WeatherItem("12 PM", "⛅ Partly Cloudy", "34°C", "Wind: 15 km/h NW", "Good conditions"),
            WeatherItem("3 PM", "🌤️ Mostly Sunny", "33°C", "Wind: 10 km/h W", "Ideal ride time"),
            WeatherItem("6 PM", "🌅 Clear Evening", "29°C", "Wind: 8 km/h SW", "Pleasant evening ride"),
            WeatherItem("9 PM", "🌙 Clear Night", "26°C", "Wind: 5 km/h S", "Cool night ride")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather for Cycling") },
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
                        Text("☀️", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "32°C",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Bengaluru • Sunny",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        WeatherStat("💧 Humidity", "45%")
                        WeatherStat("🌬️ Wind", "12 km/h")
                        WeatherStat("☁️ UV Index", "6 (High)")
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsBike, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Cycling Score: 9/10 — Perfect conditions!",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "Today's Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(weatherData) { item ->
                ParisaraCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${item.time} — ${item.condition}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(item.wind, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            item.temp,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item {
                ParisaraCard {
                    Text("🌿 Eco Tip", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Today's low humidity and moderate wind make it ideal for longer rides. Consider extending your route to save more CO₂!",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class WeatherItem(val time: String, val condition: String, val temp: String, val wind: String, val tip: String)

// ─── Community Leaderboard ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val leaders = remember {
        listOf(
            LeaderEntry("Arjun K.", 156.2, 18744.0, 42),
            LeaderEntry("Priya S.", 132.8, 15936.0, 38),
            LeaderEntry("Rahul M.", 98.5, 11820.0, 29),
            LeaderEntry("Sneha R.", 87.3, 10476.0, 25),
            LeaderEntry("Kiran D.", 76.1, 9132.0, 21),
            LeaderEntry("Meera V.", 65.4, 7848.0, 18),
            LeaderEntry("Aditya P.", 54.2, 6504.0, 15),
            LeaderEntry("Divya N.", 43.7, 5244.0, 12),
            LeaderEntry("Vikram B.", 32.1, 3852.0, 9),
            LeaderEntry("Ananya G.", 21.5, 2580.0, 6)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Leaderboard") },
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
                    Text("🏆 This Month's Top Riders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Compete with fellow cyclists to save maximum CO₂!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(leaders.size) { index ->
                val leader = leaders[index]
                val rank = index + 1
                val medal = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$rank" }
                ParisaraCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            medal,
                            style = if (rank <= 3) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(48.dp)
                        )
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                leader.name.first().toString(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(leader.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${leader.rides} rides • ${"%.1f".format(leader.distanceKm)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${"%.1f".format(leader.co2Grams / 1000)} kg",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("CO₂ saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private data class LeaderEntry(val name: String, val distanceKm: Double, val co2Grams: Double, val rides: Int)

// ─── Cycling Challenges ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(onBack: () -> Unit) {
    val challenges = remember {
        listOf(
            ChallengeItem("🚴 Weekend Warrior", "Ride 20 km this weekend", 20.0, 8.5, true),
            ChallengeItem("🌱 Carbon Cutter", "Save 5 kg CO₂ this month", 5000.0, 2100.0, true),
            ChallengeItem("🔥 7-Day Streak", "Ride every day for a week", 7.0, 3.0, true),
            ChallengeItem("🏔️ Distance Master", "Complete 100 km total", 100.0, 45.0, true),
            ChallengeItem("👥 Social Rider", "Ride with 5 buddies", 5.0, 2.0, true),
            ChallengeItem("🌅 Early Bird", "Complete 3 rides before 7 AM", 3.0, 0.0, false),
            ChallengeItem("🗺️ Explorer", "Discover 10 new routes", 10.0, 4.0, true),
            ChallengeItem("⭐ Safety Star", "Report 5 road issues", 5.0, 1.0, true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cycling Challenges") },
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
                        Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Active Challenges", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Complete challenges to earn badges and level up!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Text("In Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            items(challenges.filter { it.joined }) { challenge ->
                val progress = (challenge.current / challenge.target).coerceIn(0.0, 1.0)
                ParisaraCard {
                    Text(challenge.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(challenge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${"%.0f".format(challenge.current)} / ${"%.0f".format(challenge.target)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${"%.0f".format(progress * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item {
                Text("Available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            items(challenges.filter { !it.joined }) { challenge ->
                ParisaraCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(challenge.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(challenge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { /* Join challenge */ },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Join") }
                    }
                }
            }
        }
    }
}

private data class ChallengeItem(val title: String, val description: String, val target: Double, val current: Double, val joined: Boolean)

// ─── Edit Profile ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(container: AppContainer, uid: String, onBack: () -> Unit) {
    val profileVm: com.parisara.cycle.ui.viewmodel.ProfileViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory(container, uid)
        )
    val profile by profileVm.profile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var city by remember(profile) { mutableStateOf(profile?.city ?: "") }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        profile?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
            item {
                Text("Personal Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            item {
                ParisaraCard {
                    com.parisara.cycle.ui.components.ParisaraTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(Modifier.height(12.dp))
                    com.parisara.cycle.ui.components.ParisaraTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone
                    )
                    Spacer(Modifier.height(12.dp))
                    com.parisara.cycle.ui.components.ParisaraTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City",
                        leadingIcon = Icons.Default.LocationCity
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        // Save profile updates to Firestore
                        scope.launch {
                            val updatedProfile = profile?.copy(name = name, phone = phone, city = city)
                            if (updatedProfile != null) {
                                container.userRepository.createUser(updatedProfile)
                                saved = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
            if (saved) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Profile updated successfully!", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            item {
                Text("Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ParisaraCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(profile?.email ?: "—", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Member Since", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("May 2026", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
