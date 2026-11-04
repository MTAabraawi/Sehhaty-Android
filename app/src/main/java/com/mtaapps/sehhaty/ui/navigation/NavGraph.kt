package com.mtaapps.sehhaty.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.mtaapps.sehhaty.ui.screens.*
import com.mtaapps.sehhaty.ui.theme.BrandTeal

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if user is authenticated; session is cached locally by Firebase Auth
    val isAuthenticated = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.DrugSearch.route,
        Screen.Supervisor.route,
        Screen.DoseHistory.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigate(Screen.Home.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.DrugSearch.route,
                        onClick = { navController.navigate(Screen.DrugSearch.route) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "البحث الذكي") },
                        label = { Text("البحث") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Supervisor.route,
                        onClick = { navController.navigate(Screen.Supervisor.route) },
                        icon = { Icon(Icons.Default.People, contentDescription = "مشرف الأسرة") },
                        label = { Text("المشرف") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.DoseHistory.route,
                        onClick = { navController.navigate(Screen.DoseHistory.route) },
                        icon = { Icon(Icons.Default.History, contentDescription = "السجل") },
                        label = { Text("السجل") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                        label = { Text("الإعدادات") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTeal,
                            selectedTextColor = BrandTeal,
                            indicatorColor = BrandTeal.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onContinueOffline = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddMedicationClick = { navController.navigate(Screen.AddMedication.route) },
                    onSearchClick = { navController.navigate(Screen.DrugSearch.route) },
                    onSupervisorClick = { navController.navigate(Screen.Supervisor.route) }
                )
            }
            composable(Screen.AddMedication.route) {
                AddEditMedicationScreen(onSaved = { navController.popBackStack() })
            }
            composable(Screen.DrugSearch.route) {
                DrugSearchScreen()
            }
            composable(Screen.DoseHistory.route) {
                DoseHistoryScreen()
            }
            composable(Screen.Supervisor.route) {
                SupervisorScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
