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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.Manifest
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.ParisaraCard
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.BuddyViewModel
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.ui.viewmodel.ProfileViewModel
import com.parisara.cycle.util.rememberDeviceLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BuddyScreen(
    container: AppContainer,
    uid: String,
    userName: String,
    onBack: () -> Unit
) {
    val profileVm: ProfileViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid))
    val profile by profileVm.profile.collectAsState()
    val profileImageUrl = profile?.profileImageUrl ?: ""
    val vm: BuddyViewModel = viewModel(
        factory = ParisaraViewModelFactory(container, uid, userName, profileImageUrl),
        key = "buddy_$profileImageUrl"
    )
    val state by vm.state.collectAsState()
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    LaunchedEffect(Unit) { if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest() }
    val location = rememberDeviceLocation(permissions.allPermissionsGranted)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buddy System") },
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ParisaraCard(modifier = Modifier.padding(16.dp)) {
                Text(
                    state.message, 
                    fontWeight = FontWeight.Medium, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Share location only with trusted users.", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Live Location Sharing", 
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = state.isSharing,
                        onCheckedChange = { vm.toggleSharing(it, location) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer, 
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            Box(Modifier.weight(0.5f).fillMaxWidth()) {
                val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(location, 13f) }
                GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camera) {
                    state.buddies.forEach { buddy ->
                        Marker(
                            state = MarkerState(LatLng(buddy.latitude, buddy.longitude)),
                            title = buddy.userName,
                            snippet = "Nearby cyclist"
                        )
                    }
                }
            }
            LazyColumn(Modifier.weight(0.5f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.requests.isNotEmpty()) {
                    item { 
                        Text(
                            "Buddy Requests", 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    }
                    items(state.requests) { req ->
                        ParisaraCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer), 
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        req.fromUserName.firstOrNull()?.toString() ?: "?",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    req.fromUserName, 
                                    Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { vm.respondRequest(req.id, true) }) { 
                                    Text("Accept", color = MaterialTheme.colorScheme.primary) 
                                }
                                TextButton(onClick = { vm.respondRequest(req.id, false) }) { 
                                    Text("Reject", color = MaterialTheme.colorScheme.error) 
                                }
                            }
                        }
                    }
                }
                item { 
                    Text(
                        "Nearby Cyclists (${state.buddies.size})", 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                }
                if (state.buddies.isEmpty()) {
                    item { 
                        Text(
                            "No buddies sharing nearby. Enable sharing to connect!", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    }
                }
                items(state.buddies) { buddy ->
                    ParisaraCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                buddy.userName, 
                                Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(
                                onClick = { vm.sendRequest(buddy.userId) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("Add Buddy") }
                        }
                    }
                }
            }
        }
    }
}
