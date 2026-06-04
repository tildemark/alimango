package com.tildemark.alimango.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tildemark.alimango.ui.dashboard.DashboardScreen
import com.tildemark.alimango.ui.dashboard.DashboardViewModel
import com.tildemark.alimango.ui.onboarding.OnboardingScreen
import com.tildemark.alimango.ui.onboarding.OnboardingViewModel
import com.tildemark.alimango.ui.review.ReviewScreen
import com.tildemark.alimango.ui.review.ReviewSummaryScreen
import com.tildemark.alimango.ui.review.ReviewViewModel
import com.tildemark.alimango.ui.browser.ItemsBrowserScreen
import com.tildemark.alimango.ui.browser.ItemsBrowserViewModel
import com.tildemark.alimango.ui.lesson.LessonScreen
import com.tildemark.alimango.ui.lesson.LessonViewModel

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun AlimangoNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onStartReviews = {
                    navController.navigate(Screen.ReviewSession.route)
                },
                onStartLessons = { subjectIds ->
                    if (subjectIds.isNullOrEmpty()) {
                        navController.navigate("lesson_session")
                    } else {
                        navController.navigate(Screen.LessonSession.createRoute(subjectIds))
                    }
                },
                onBrowseItems = {
                    navController.navigate(Screen.ItemsBrowser.route)
                }
            )
        }

        composable(
            route = Screen.LessonSession.route,
            arguments = listOf(
                navArgument("subjectIds") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: LessonViewModel = hiltViewModel()
            LessonScreen(
                viewModel = viewModel,
                onBackToDashboard = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ItemsBrowser.route) {
            val viewModel: ItemsBrowserViewModel = hiltViewModel()
            ItemsBrowserScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ReviewSession.route) {
            val viewModel: ReviewViewModel = hiltViewModel()
            ReviewScreen(
                viewModel = viewModel,
                onSessionFinished = { correct, total ->
                    navController.navigate(Screen.ReviewSummary.createRoute(correct, total)) {
                        popUpTo(Screen.ReviewSession.route) { inclusive = true }
                    }
                },
                onBackToDashboard = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ReviewSummary.route,
            arguments = listOf(
                navArgument("correctCount") { type = NavType.IntType },
                navArgument("totalCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val correctCount = backStackEntry.arguments?.getInt("correctCount") ?: 0
            val totalCount = backStackEntry.arguments?.getInt("totalCount") ?: 0
            ReviewSummaryScreen(
                correctCount = correctCount,
                totalCount = totalCount,
                onBackToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ReviewSummary.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
