package com.parisara.cycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.Manifest
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.util.rememberDeviceLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.parisara.cycle.ui.components.EmptyState
import com.parisara.cycle.ui.components.ParisaraCard
import com.parisara.cycle.ui.components.ParisaraLoadingIndicator
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.ui.viewmodel.PitStopViewModel
import com.parisara.cycle.util.LocationUtils.distanceKm
import com.parisara.cycle.util.toLatLng

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PitStopScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: PitStopViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val state by vm.state.collectAsState()
    val categories = listOf("All", "Repair Shop", "Water Station", "Rest Area", "First Aid", "Utility")
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    LaunchedEffect(Unit) { if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest() }
    val deviceLoc = rememberDeviceLocation(permissions.allPermissionsGranted)
    val userLoc = state.userLocation ?: deviceLoc

    LaunchedEffect(deviceLoc) { vm.setUserLocation(deviceLoc) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLoc, 13f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pit-Stop Finder") },
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { vm.setCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, 
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                if (state.isLoading) ParisaraLoadingIndicator()
                else if (state.filtered.isEmpty()) {
                    EmptyState(Icons.Default.Place, "No Pit-Stops Found", "Try a different filter or check back later.")
                } else {
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraState) {
                        state.filtered.forEach { stop ->
                            stop.location?.let { gp ->
                                val latLng = gp.toLatLng()
                                Marker(
                                    state = MarkerState(latLng),
                                    title = stop.name,
                                    snippet = "${stop.category} • ${stop.operatingHours}",
                                    onClick = { vm.selectStop(stop); false }
                                )
                            }
                        }
                    }
                }
                state.selectedStop?.let { stop ->
                    ParisaraCard(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                        Text(
                            stop.name, 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stop.category, 
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stop.address, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Hours: ${stop.operatingHours}", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        stop.location?.toLatLng()?.let { loc ->
                            val dist = distanceKm(userLoc, loc)
                            Text(
                                "${"%.1f".format(dist)} km away", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { vm.selectStop(null) }) { 
                            Text("Close", color = MaterialTheme.colorScheme.primary) 
                        }
                    }
                }
            }
        }
    }
}
