package com.parisara.cycle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.parisara.cycle.data.repository.SampleData
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.viewmodel.AppContainerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ParisaraApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // Initialize App Check for Debugging
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        val container = AppContainer(this)
        AppContainerHolder.container = container
        createNotificationChannel()
        
        appScope.launch {
            try {
                container.pitStopRepository.seedSampleData()
                SampleData.dangerZones.forEach { zone ->
                    container.routeRepository.addDangerZone(zone)
                }
            } catch (e: Exception) {
                // Silently fail if network/permissions are not ready yet
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel),
                "Parisara Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Route alerts, buddy requests, and achievements"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
