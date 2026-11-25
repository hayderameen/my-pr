package com.hayderameen.mypr.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hayderameen.mypr.ui.components.MainTab
import com.hayderameen.mypr.ui.screens.DayExercisesScreen
import com.hayderameen.mypr.ui.screens.DaysScreen
import com.hayderameen.mypr.ui.screens.ExercisePickerScreen
import com.hayderameen.mypr.ui.screens.LogSetScreen
import com.hayderameen.mypr.ui.screens.OnboardingScreen
import com.hayderameen.mypr.ui.screens.PlatesScreen
import com.hayderameen.mypr.ui.screens.PrHistoryScreen
import com.hayderameen.mypr.ui.screens.PrRevealScreen
import com.hayderameen.mypr.ui.theme.Motion

object Routes {
    const val ONBOARDING = "onboarding"
    const val WORKOUTS = "workouts"
    const val HISTORY = "history"
    const val DAY = "day/{dayId}"
    const val PICKER = "day/{dayId}/add"
    const val LOG = "log/{dayId}/{exerciseId}"
    const val REVEAL = "pr/{exerciseId}"
    const val PLATES = "plates"

    fun day(id: String) = "day/$id"
    fun picker(id: String) = "day/$id/add"
    fun log(dayId: String, exerciseId: String) = "log/$dayId/$exerciseId"
    fun reveal(exerciseId: String) = "pr/$exerciseId"
}

private fun NavHostController.goToTab(tab: MainTab) {
    val route = if (tab == MainTab.WORKOUTS) Routes.WORKOUTS else Routes.HISTORY
    navigate(route) {
        popUpTo(Routes.WORKOUTS) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MyPrNavHost(startDestination: String, nav: NavHostController = rememberNavController()) {
    val enterSpec = tween<androidx.compose.ui.unit.IntOffset>(Motion.ENTER_MS, easing = Motion.enterEasing)
    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(enterSpec) { it / 8 } + fadeIn(tween(Motion.ENTER_MS)) },
        exitTransition = { fadeOut(tween(Motion.STATE_MS)) },
        popEnterTransition = { fadeIn(tween(Motion.ENTER_MS)) },
        popExitTransition = { slideOutHorizontally(enterSpec) { it / 8 } + fadeOut(tween(Motion.STATE_MS)) },
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                nav.navigate(Routes.WORKOUTS) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }
        composable(Routes.WORKOUTS) {
            DaysScreen(onOpenDay = { nav.navigate(Routes.day(it)) }, onTab = nav::goToTab)
        }
        composable(Routes.HISTORY) {
            PrHistoryScreen(onOpen = { nav.navigate(Routes.reveal(it)) }, onTab = nav::goToTab)
        }
        composable(Routes.DAY) { entry ->
            val dayId = entry.arguments?.getString("dayId").orEmpty()
            DayExercisesScreen(
                dayId = dayId,
                onBack = { nav.popBackStack() },
                onLog = { nav.navigate(Routes.log(dayId, it)) },
                onAddExercises = { nav.navigate(Routes.picker(dayId)) },
                onWorkoutEnded = { nav.popBackStack(Routes.WORKOUTS, inclusive = false) },
            )
        }
        composable(Routes.PICKER) { entry ->
            ExercisePickerScreen(dayId = entry.arguments?.getString("dayId").orEmpty(), onBack = { nav.popBackStack() })
        }
        composable(Routes.LOG) { entry ->
            LogSetScreen(
                dayId = entry.arguments?.getString("dayId").orEmpty(),
                exerciseId = entry.arguments?.getString("exerciseId").orEmpty(),
                onBack = { nav.popBackStack() },
                onDone = { nav.popBackStack() },
                onPlates = { nav.navigate(Routes.PLATES) },
            )
        }
        composable(Routes.REVEAL) { entry ->
            PrRevealScreen(
                exerciseId = entry.arguments?.getString("exerciseId").orEmpty(),
                onBack = { nav.popBackStack() },
                onTab = nav::goToTab,
            )
        }
        composable(Routes.PLATES) {
            PlatesScreen(onBack = { nav.popBackStack() })
        }
    }
}
