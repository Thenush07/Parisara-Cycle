package com.parisara.cycle.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.parisara.cycle.data.repository.SampleData
import kotlinx.coroutines.tasks.await

/**
 * Observes the device's current location for map and safety features.
 * Falls back to Bengaluru demo coordinates when GPS is unavailable.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberDeviceLocation(enabled: Boolean = true): LatLng {
    val context = LocalContext.current
    var location by remember { mutableStateOf(SampleData.center) }

    LaunchedEffect(enabled, context) {
        if (!enabled) return@LaunchedEffect
        location = fetchLocation(context) ?: SampleData.center
    }
    return location
}

@SuppressLint("MissingPermission")
suspend fun fetchLocation(context: Context): LatLng? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val current = client.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).await()
        current?.let { LatLng(it.latitude, it.longitude) }
            ?: client.lastLocation.await()?.let { LatLng(it.latitude, it.longitude) }
    } catch (_: Exception) {
        null
    }
}
