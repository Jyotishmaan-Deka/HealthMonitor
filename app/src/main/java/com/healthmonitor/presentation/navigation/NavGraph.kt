package com.healthmonitor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.healthmonitor.presentation.ui.ai.AISuggestionsScreen
import com.healthmonitor.presentation.ui.alerts.AlertsScreen
import com.healthmonitor.presentation.ui.auth.AuthScreen
import com.healthmonitor.presentation.ui.auth.AuthViewModel
import com.healthmonitor.presentation.ui.ble.BleScannerScreen
import com.healthmonitor.presentation.ui.dashboard.DashboardScreen
import com.healthmonitor.presentation.ui.history.HistoryScreen
import com.healthmonitor.presentation.ui.profile.ProfileScreen
import com.healthmonitor.presentation.ui.MainScaffold

sealed class Screen(val route: String) {
    object Auth       : Screen("auth")
    object Dashboard  : Screen("dashboard")
    object History    : Screen("history")
    object Alerts     : Screen("alerts")
    object AI         : Screen("ai_suggestions")
    object Profile    : Screen("profile")
    object BleScanner : Screen("ble_scanner")
}

@Composable
fun HealthNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    val startDest = if (isLoggedIn) Screen.Dashboard.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDest) {

        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            MainScaffold(navController = navController) {
                DashboardScreen(
                    onNavigateToHistory    = { navController.navigate(Screen.History.route) },
                    onNavigateToAI         = { navController.navigate(Screen.AI.route) },
                    onNavigateToBleScanner = { navController.navigate(Screen.BleScanner.route) }
                )
            }
        }

        composable(Screen.History.route) {
            MainScaffold(navController = navController) { HistoryScreen() }
        }

        composable(Screen.Alerts.route) {
            MainScaffold(navController = navController) { AlertsScreen() }
        }

        composable(Screen.AI.route) {
            MainScaffold(navController = navController) { AISuggestionsScreen() }
        }

        composable(Screen.BleScanner.route) {
            MainScaffold(navController = navController) {
                BleScannerScreen(
                    onDeviceSelected = { address ->
                        // Navigate back to dashboard; address used by DashboardViewModel
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("ble_address", address)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Profile.route) {
            MainScaffold(navController = navController) {
                ProfileScreen(
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
