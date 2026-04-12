package com.healthmonitor.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.healthmonitor.presentation.navigation.Screen
import com.healthmonitor.presentation.ui.alerts.AlertsViewModel

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun MainScaffold(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val alertsVm: AlertsViewModel = hiltViewModel()
    val unreadCount by alertsVm.unreadCount.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
        NavItem(Screen.History,   "History",   Icons.Default.Analytics),
        NavItem(Screen.Alerts,    "Alerts",    Icons.Default.Notifications, badgeCount = unreadCount),
        NavItem(Screen.AI,        "AI",        Icons.Default.Psychology),
        NavItem(Screen.Profile,   "Profile",   Icons.Default.Person)
    )

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    val selected = currentDest?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (item.badgeCount > 0) {
                                BadgedBox(badge = {
                                    Badge { Text("${item.badgeCount}") }
                                }) {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
