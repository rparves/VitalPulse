@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vitapulse.app.health.HealthRepo
import com.vitapulse.app.ui.screens.*

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("today", "Today", Icons.Rounded.Home),
    Tab("food", "Food", Icons.Rounded.Restaurant),
    Tab("workouts", "Workouts", Icons.Rounded.FitnessCenter),
    Tab("trends", "Trends", Icons.Rounded.Insights),
    Tab("profile", "Profile", Icons.Rounded.Person),
)

@Composable
fun VitaApp(vm: AppViewModel = viewModel()) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.onResume() }

    val state = loaded ?: return
    val p = state.profile
    if (p == null || !p.onboarded) {
        OnboardingScreen(vm)
        return
    }

    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val hcLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { vm.refreshHealth() }
    val connectHealth: () -> Unit = {
        if (vm.health.isAvailable()) hcLauncher.launch(HealthRepo.ALL)
        else Toast.makeText(context, "Health Connect isn't available on this device", Toast.LENGTH_LONG).show()
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val goTab: (String) -> Unit = { r ->
        nav.navigate(r) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.label) },
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "today", modifier = Modifier.padding(pad)) {
            composable("today") {
                TodayScreen(vm, onConnectHealth = connectHealth,
                    onOpenFood = { goTab("food") }, onOpenTrends = { goTab("trends") })
            }
            composable("food") { FoodScreen(vm, onScan = { nav.navigate("scan") }) }
            composable("workouts") {
                WorkoutsScreen(onOpen = { id -> nav.navigate("exercise/" + android.net.Uri.encode(id)) })
            }
            composable("exercise/{id}") { entry ->
                ExerciseDetailScreen(entry.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable("trends") { TrendsScreen(vm) }
            composable("profile") {
                ProfileScreen(vm, onEdit = { nav.navigate("edit") }, onConnectHealth = connectHealth)
            }
            composable("scan") { ScanScreen(vm, onClose = { nav.popBackStack() }) }
            composable("edit") { EditProfileScreen(vm, onDone = { nav.popBackStack() }) }
        }
    }
}
