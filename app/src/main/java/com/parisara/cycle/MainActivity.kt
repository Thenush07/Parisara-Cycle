package com.parisara.cycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.parisara.cycle.ui.navigation.ParisaraNavHost
import com.parisara.cycle.ui.theme.ParisaraTheme
import com.parisara.cycle.ui.viewmodel.AppContainerHolder

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.parisara.cycle.data.repository.ThemeOption

/**
 * MainActivity serves as the main entry point for the Parisara-Cycle application.
 * It sets up the Compose navigation host and applies the user's preferred theme.
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Called when the activity is starting.
     * Initializes the edge-to-edge display and sets up the root Compose view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = AppContainerHolder.container
        setContent {
            val themeOption by container.userPreferencesRepository.themeOption.collectAsState(initial = ThemeOption.SYSTEM)
            val isDarkTheme = when (themeOption) {
                ThemeOption.DARK -> true
                ThemeOption.LIGHT -> false
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            ParisaraTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(), 
                    color = MaterialTheme.colorScheme.background
                ) {
                    ParisaraNavHost(container)
                }
            }
        }
    }
}
