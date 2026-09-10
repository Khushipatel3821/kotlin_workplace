package com.fitpulse.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Gym : Screen("gym")
    data object ActiveWorkout : Screen("active_workout/{routineId}/{title}") {
        fun createRoute(routineId: Long? = null, title: String = "Quick Workout"): String {
            return "active_workout/${routineId ?: -1L}/$title"
        }
    }
    data object Yoga : Screen("yoga")
    data object Analytics : Screen("analytics")
    data object Settings : Screen("settings")
}
