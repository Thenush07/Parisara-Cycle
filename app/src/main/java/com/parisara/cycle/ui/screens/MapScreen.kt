package com.parisara.cycle.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.parisara.cycle.data.model.ReportStatus
import com.parisara.cycle.data.repository.SampleData
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.*
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.MapViewModel
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.util.rememberDeviceLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapRouteScreen(
    container: AppContainer,
    uid: String,
    userName: String,
    onBack: () -> Unit
) {
    val vm: MapViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid, userName))
    val state by vm.state.collectAsState()
    val isDark = isSystemInDarkTheme()
    
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    LaunchedEffect(Unit) { if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest() }

    val deviceLoc = rememberDeviceLocation(permissions.allPermissionsGranted)
    val defaultLoc = deviceLoc
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.currentLocation ?: defaultLoc, 14f)
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safe Route Planner") },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back") 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissions.allPermissionsGranted,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true)
            ) {
                MapEffect(permissions.allPermissionsGranted) { map ->
                    map.isTrafficEnabled = false
                    try {
                        map.isBuildingsEnabled = true
                    } catch (_: Exception) { }
                }

                state.routeInfo?.let { route ->
                    Polyline(
                        points = route.polylinePoints,
                        color = if (route.safetyScore >= 80) RouteSafe else RouteCaution,
                        width = 12f
                    )
                }

                state.dangerZones.forEach { zone ->
                    zone.location?.let { gp ->
                        Marker(
                            state = MarkerState(LatLng(gp.latitude, gp.longitude)),
                            title = zone.title,
                            snippet = zone.description,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                }

                state.reports.forEach { report ->
                    report.location?.let { gp ->
                        Marker(
                            state = MarkerState(LatLng(gp.latitude, gp.longitude)),
                            title = "⚠ ${report.issueType}",
                            snippet = report.description,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                state.sourceLatLng?.let {
                    Marker(state = MarkerState(it), title = "Start", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                }
                state.destLatLng?.let {
                    Marker(state = MarkerState(it), title = "Destination", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParisaraCard {
                    ParisaraTextField(state.source, vm::setSource, "Source", leadingIcon = Icons.Default.MyLocation)
                    Spacer(Modifier.height(8.dp))
                    ParisaraTextField(state.destination, vm::setDestination, "Destination", leadingIcon = Icons.Default.Place)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { 
                                vm.setCurrentLocation(state.currentLocation ?: defaultLoc)
                                vm.setSource("Current Location", state.currentLocation ?: defaultLoc) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text("Use My Location") }
                        Button(
                            onClick = {
                                val dest = LatLng(defaultLoc.latitude + 0.02, defaultLoc.longitude + 0.02)
                                vm.setDestination("Sample Destination", dest)
                                vm.setSource("Current Location", state.currentLocation ?: defaultLoc)
                                vm.generateRoute()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Plan Route") }
                    }
                }

                state.routeInfo?.let { route ->
                    ParisaraCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    "${"%.1f".format(route.distanceKm)} km • ${route.durationMinutes} min", 
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Bicycle-friendly route • Avoids highways", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SafetyBadge(route.safetyScore)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (state.isRiding) {
                            Button(
                                onClick = { vm.completeRide() }, 
                                modifier = Modifier.fillMaxWidth(), 
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Complete Ride & Save")
                            }
                        }
                    }
                }
            }

            if (state.isLoading) LoadingOverlay("Planning safe route...")
            state.error?.let {
                Snackbar(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) { Text(it) }
            }
        }
    }

    LaunchedEffect(deviceLoc, permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) {
            vm.setCurrentLocation(deviceLoc)
        }
    }
}
