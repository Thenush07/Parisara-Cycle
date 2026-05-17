package com.parisara.cycle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parisara.cycle.ui.components.ParisaraCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── 🌿 INTERACTIVE CARBON SAVINGS SIMULATOR ─────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoSimulatorScreen(onBack: () -> Unit) {
    var distanceKm by remember { mutableFloatStateOf(40f) }
    var selectedAlternative by remember { mutableStateOf("Petrol SUV") }

    val alternatives = listOf("Petrol SUV", "Diesel Sedan", "Public Bus", "Electric Vehicle")
    
    // Co2 coefficients (grams per km)
    val co2GramsPerKm = when (selectedAlternative) {
        "Petrol SUV" -> 220f
        "Diesel Sedan" -> 160f
        "Public Bus" -> 80f
        "Electric Vehicle" -> 50f
        else -> 150f
    }
    
    val savedCo2Kg = (distanceKm * co2GramsPerKm) / 1000f
    val treesSaved = savedCo2Kg / 0.06f // Approx 60g offset per tree-day
    val moneySaved = distanceKm * 8.5f // Commuting cost estimate per km

    // Smooth transition scale for visual badges
    val transitionScale by animateFloatAsState(
        targetValue = 1f + (distanceKm / 300f) * 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "transitionScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco-Savings Simulator") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ParisaraCard {
                    Text(
                        "Green Transportation Playground",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Simulate how much carbon you cut and money you save by choosing a cycle instead of motorized transit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                ParisaraCard {
                    Text(
                        "1. Weekly Commute Distance",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${distanceKm.toInt()} km",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Weekly",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = distanceKm,
                        onValueChange = { distanceKm = it },
                        valueRange = 0f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            item {
                ParisaraCard {
                    Text(
                        "2. Compare Cycling Against",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        alternatives.take(2).forEach { mode ->
                            val selected = selectedAlternative == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAlternative = mode }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = mode,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        alternatives.drop(2).forEach { mode ->
                            val selected = selectedAlternative == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAlternative = mode }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = mode,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "3. Simulated Environmental Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ParisaraCard {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .scale(transitionScale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Co2,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "${"%.1f".format(savedCo2Kg)} kg",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "CO₂ Saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ParisaraCard {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .scale(transitionScale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Park,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "${"%.0f".format(treesSaved)} Trees",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Daily Equiv.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                ParisaraCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Monetary Savings",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Based on fuel & maintenance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "₹${"%.0f".format(moneySaved)}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                ParisaraCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("💡", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Green Commuter Verdict",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val adviceText = when {
                                distanceKm == 0f -> "Increase the slider distance to test out potential eco savings!"
                                distanceKm <= 30f -> "Perfect local neighborhood riding! You are saving a healthy amount of carbon emissions daily."
                                distanceKm <= 100f -> "Phenomenal! Your active lifestyle completely mitigates your carbon footprint and offsets major travel costs."
                                else -> "Supreme Cyclist! Commuting this volume on a pedal cycle has the impact equivalent of a mini local forest. Truly exceptional!"
                            }
                            Text(
                                text = adviceText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 🚴 SPEEDOMETER / ACTIVE RIDE WORKOUT HUD Screen ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHudScreen(onBack: () -> Unit) {
    var isStarted by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var mockSpeed by remember { mutableFloatStateOf(0f) }
    var simulatedDistance by remember { mutableFloatStateOf(0f) }
    
    val scope = rememberCoroutineScope()

    // Realtime timer
    LaunchedEffect(isStarted) {
        if (isStarted) {
            while (true) {
                delay(1000)
                secondsElapsed++
                simulatedDistance += (mockSpeed / 3600f) // dynamic distance addition
                // Random walk speed simulation
                val delta = (-2..2).random().toFloat() * 0.8f
                mockSpeed = (mockSpeed + delta).coerceIn(12f, 29f)
            }
        } else {
            mockSpeed = 0f
        }
    }

    val simulatedCalories = (simulatedDistance * 32f).toInt()
    val co2PreventedG = (simulatedDistance * 165f).toInt()

    // Bouncy scale animation for starting/pausing HUD
    val scaleFactor by animateFloatAsState(
        targetValue = if (isStarted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "hudScale"
    )

    // Pulsing circle animation during cycling
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Ride Computer HUD") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speedometer Circle
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(220.dp)
                        .scale(if (isStarted) pulseRadius else 1.0f)
                ) {
                    // Outer Ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 6.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // Inner Circular Dial
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        Text(
                            text = if (isStarted) "${"%.1f".format(mockSpeed)}" else "0.0",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 48.sp
                        )
                        Text(
                            "SPEED km/h",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Status Grid
            item {
                ParisaraCard(modifier = Modifier.scale(scaleFactor)) {
                    Text(
                        "Live Workout Metrics",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HudStatBox("⏳ Elapsed Time", formatTime(secondsElapsed))
                        HudStatBox("🏁 Distance", "${"%.2f".format(simulatedDistance)} km")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HudStatBox("🔥 Calories", "$simulatedCalories kcal")
                        HudStatBox("🌱 CO₂ Saved", "$co2PreventedG g")
                    }
                }
            }

            // Custom Interactive Controllers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isStarted) {
                        Button(
                            onClick = {
                                isStarted = true
                                mockSpeed = 16.5f
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Commute", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = { isStarted = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Pause, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pause Ride", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                isStarted = false
                                mockSpeed = 0f
                                simulatedDistance = 0f
                                secondsElapsed = 0
                            },
                            modifier = Modifier
                                .weight(0.5f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }
            }

            // Styled mock GPX Route Trail Map representer
            item {
                ParisaraCard {
                    Text(
                        "📍 Active Routing GPX Trace",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DirectionsBike,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (isStarted) "GPX Simulator active... recording GPS path." else "HUD ready. Click Start to track.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudStatBox(label: String, value: String) {
    Column(modifier = Modifier.padding(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
