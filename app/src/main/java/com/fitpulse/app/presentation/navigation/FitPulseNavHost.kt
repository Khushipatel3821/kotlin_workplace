package com.fitpulse.app.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitpulse.app.presentation.analytics.AnalyticsScreen
import com.fitpulse.app.presentation.analytics.AnalyticsViewModel
import com.fitpulse.app.presentation.dashboard.DashboardScreen
import com.fitpulse.app.presentation.dashboard.DashboardViewModel
import com.fitpulse.app.presentation.gym.ActiveWorkoutScreen
import com.fitpulse.app.presentation.gym.ActiveWorkoutViewModel
import com.fitpulse.app.presentation.gym.GymTrackerScreen
import com.fitpulse.app.presentation.settings.SettingsScreen
import com.fitpulse.app.presentation.settings.SettingsViewModel
import com.fitpulse.app.presentation.theme.NeonLime
import com.fitpulse.app.presentation.yoga.YogaStudioScreen
import com.fitpulse.app.presentation.yoga.YogaStudioViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun FitPulseNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = remember {
        listOf(
            BottomNavItem(Screen.Dashboard.route, "Home", Icons.Default.Home),
            BottomNavItem(Screen.Gym.route, "Gym", Icons.Default.FitnessCenter),
            BottomNavItem(Screen.Yoga.route, "Yoga", Icons.Default.SelfImprovement),
            BottomNavItem(Screen.Analytics.route, "Analytics", Icons.Default.Assessment),
            BottomNavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
        )
    }

    val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NeonLime,
                                indicatorColor = NeonLime,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onStartWorkout = { routineId, title ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(routineId, title))
                    },
                    onOpenYoga = {
                        navController.navigate(Screen.Yoga.route)
                    },
                    onOpenAnalytics = {
                        navController.navigate(Screen.Analytics.route)
                    }
                )
            }

            // Gym Tracker
            composable(Screen.Gym.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                val activeWorkoutViewModel: ActiveWorkoutViewModel = hiltViewModel()
                val activeState by activeWorkoutViewModel.uiState.collectAsStateWithLifecycle()

                GymTrackerScreen(
                    exercises = activeState.availableExercises,
                    routines = emptyList(),
                    onStartRoutine = { routineId, title ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(routineId, title))
                    },
                    onStartEmptyWorkout = {
                        navController.navigate(Screen.ActiveWorkout.createRoute(null, "Quick Workout"))
                    }
                )
            }

            // Active Workout Session
            composable(
                route = Screen.ActiveWorkout.route,
                arguments = listOf(
                    navArgument("routineId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("title") { type = NavType.StringType; defaultValue = "Quick Workout" }
                )
            ) { backStackEntry ->
                val routineIdArg = backStackEntry.arguments?.getLong("routineId")?.takeIf { it > 0 }
                val titleArg = backStackEntry.arguments?.getString("title") ?: "Quick Workout"
                val viewModel: ActiveWorkoutViewModel = hiltViewModel()

                ActiveWorkoutScreen(
                    viewModel = viewModel,
                    routineId = routineIdArg,
                    title = titleArg,
                    onFinish = {
                        navController.popBackStack()
                    }
                )
            }

            // Yoga Studio
            composable(Screen.Yoga.route) {
                val viewModel: YogaStudioViewModel = hiltViewModel()
                YogaStudioScreen(
                    viewModel = viewModel,
                    onSessionCompleted = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }

            // Analytics & History
            composable(Screen.Analytics.route) {
                val viewModel: AnalyticsViewModel = hiltViewModel()
                AnalyticsScreen(viewModel = viewModel)
            }

            // Settings & Export
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
