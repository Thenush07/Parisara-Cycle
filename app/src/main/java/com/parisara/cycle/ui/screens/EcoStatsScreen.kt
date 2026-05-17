package com.parisara.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parisara.cycle.data.model.ACHIEVEMENTS
import com.parisara.cycle.data.model.EcoCalculator
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.EcoBarChart
import com.parisara.cycle.ui.components.ParisaraCard
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.ui.viewmodel.RideHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoStatsScreen(container: AppContainer, uid: String, onBack: () -> Unit) {
    val vm: RideHistoryViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid))
    val stats by vm.ecoStats.collectAsState()
    val profileVm: com.parisara.cycle.ui.viewmodel.ProfileViewModel =
        viewModel(factory = ParisaraViewModelFactory(container, uid))
    val profile by profileVm.profile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eco Stats") },
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
                    Text(
                        "Distance Overview", 
                        fontWeight = FontWeight.SemiBold, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    EcoBarChart(stats.dailyDistanceKm, stats.weeklyDistanceKm, stats.monthlyDistanceKm)
                }
            }
            item {
                ParisaraCard {
                    Text(
                        "🌱 Environmental Impact", 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    EcoStatRow("Today", stats.dailyDistanceKm, stats.dailyCo2Grams)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    EcoStatRow("This Week", stats.weeklyDistanceKm, stats.weeklyCo2Grams)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    EcoStatRow("This Month", stats.monthlyDistanceKm, stats.monthlyCo2Grams)
                }
            }
            item {
                ParisaraCard {
                    Text(
                        "Motivation", 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    val messages = listOf(
                        "You saved ${EcoCalculator.formatCo2(stats.dailyCo2Grams)} CO₂ today",
                        "You prevented ${EcoCalculator.formatCo2(stats.weeklyCo2Grams)} of carbon emissions this week",
                        "Monthly impact: ${EcoCalculator.formatCo2(stats.monthlyCo2Grams)} CO₂ avoided"
                    )
                    messages.forEach { msg -> 
                        Text(
                            "• $msg", 
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    }
                }
            }
            item { 
                Text(
                    "Achievements", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            }
            items(ACHIEVEMENTS.size) { i ->
                val ach = ACHIEVEMENTS[i]
                val unlocked = profile?.achievements?.contains(ach.id) == true
                ParisaraCard {
                    Row {
                        Text(ach.icon, modifier = Modifier.padding(end = 12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                ach.title, 
                                fontWeight = FontWeight.SemiBold, 
                                color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                ach.description, 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (unlocked) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EcoStatRow(period: String, km: Double, co2: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(
                period, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${"%.1f".format(km)} km cycled", 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            EcoCalculator.formatCo2(co2), 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary
        )
    }
}
