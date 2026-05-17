package com.parisara.cycle.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.util.rememberDeviceLocation
import com.parisara.cycle.util.toGeoPoint
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.parisara.cycle.ui.components.LoadingOverlay
import com.parisara.cycle.ui.components.ParisaraTextField
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory
import com.parisara.cycle.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportScreen(container: AppContainer, uid: String, userName: String, onBack: () -> Unit) {
    val vm: ReportViewModel = viewModel(factory = ParisaraViewModelFactory(container, uid, userName))
    val state by vm.state.collectAsState()
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    LaunchedEffect(Unit) { if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest() }
    val deviceLoc = rememberDeviceLocation(permissions.allPermissionsGranted)
    var issueType by remember { mutableStateOf("Pothole") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val issueTypes = listOf("Pothole", "Road Blockage", "Water Logging", "Poor Lighting", "Unsafe Intersection", "Heavy Traffic", "Damaged Cycle Path", "Construction")

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.reset()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Route Problem") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Location auto-captured from your current position",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded, 
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = issueType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Issue Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded, 
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    issueTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = MaterialTheme.colorScheme.onSurface) }, 
                            onClick = { issueType = type; expanded = false }
                        )
                    }
                }
            }
            
            ParisaraTextField(description, { description = it }, "Description", singleLine = false)
            
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(Modifier.width(8.dp))
                Text(if (imageUri != null) "Image Selected" else "Add Photo (Optional)")
            }
            
            Button(
                onClick = {
                    vm.submit(issueType, description, deviceLoc.toGeoPoint(), imageUri)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = description.isNotBlank()
            ) { 
                Text("Submit Report", color = MaterialTheme.colorScheme.onPrimary) 
            }
            
            state.error?.let { 
                Text(
                    it, 
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                ) 
            }
        }
    }
    if (state.isLoading) LoadingOverlay("Uploading report...")
}
