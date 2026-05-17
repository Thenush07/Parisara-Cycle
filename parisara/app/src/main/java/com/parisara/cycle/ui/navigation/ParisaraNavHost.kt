package com.parisara.cycle.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.screens.*
import com.parisara.cycle.ui.viewmodel.AuthViewModel
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory

@Composable
fun ParisaraNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val authState by authVm.state.collectAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val uid = firebaseUser?.uid ?: ""
    val userName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.email?.substringBefore("@")
        ?: "Rider"

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        enterTransition = {
            slideInHorizontally(animationSpec = spring()) { it / 3 } + 
            scaleIn(initialScale = 0.94f, animationSpec = springSpec) + 
            fadeIn(animationSpec = springSpec)
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = spring()) { -it / 3 } + 
            scaleOut(targetScale = 0.94f, animationSpec = springSpec) + 
            fadeOut(animationSpec = springSpec)
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = spring()) { -it / 3 } + 
            scaleIn(initialScale = 0.94f, animationSpec = springSpec) + 
            fadeIn(animationSpec = springSpec)
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = spring()) { it / 3 } + 
            scaleOut(targetScale = 0.94f, animationSpec = springSpec) + 
            fadeOut(animationSpec = springSpec)
        }
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen {
                navController.navigate(
                    if (authState.isLoggedIn) NavRoutes.HOME else NavRoutes.LOGIN
                ) { popUpTo(NavRoutes.SPLASH) { inclusive = true } }
            }
        }
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                container = container,
                onNavigateRegister = { navController.navigate(NavRoutes.REGISTER) },
                onNavigateForgot = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                container = container,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(container, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.HOME) {
            HomeScreen(container, uid) { route -> navController.navigate(route) }
        }
        composable(NavRoutes.MAP_ROUTE) {
            MapRouteScreen(container, uid, userName, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ECO_STATS) {
            EcoStatsScreen(container, uid, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.PIT_STOPS) {
            PitStopScreen(container, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.BUDDY) {
            BuddyScreen(container, uid, userName, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.REPORT) {
            ReportScreen(container, uid, userName, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                container, uid,
                onBack = { navController.popBackStack() },
                onLogout = {
                    container.authRepository.logout()
                    navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(NavRoutes.RIDE_HISTORY) {
            RideHistoryScreen(container, uid, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SAFETY_TIPS) {
            SafetyTipsScreen(container, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(container, uid, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(container, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ADMIN) {
            AdminScreen(container, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.WEATHER) {
            WeatherScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.LEADERBOARD) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.CHALLENGES) {
            ChallengesScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.EDIT_PROFILE) {
            EditProfileScreen(container, uid, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ECO_SIMULATOR) {
            EcoSimulatorScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.RIDE_HUD) {
            RideHudScreen(onBack = { navController.popBackStack() })
        }
    }
}