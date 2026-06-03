package com.tildemark.alimango.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object ItemsBrowser : Screen("items_browser")
    object LessonSession : Screen("lesson_session")
    object ReviewSession : Screen("review_session")
    object ReviewSummary : Screen("review_summary/{correctCount}/{totalCount}") {
        fun createRoute(correctCount: Int, totalCount: Int): String {
            return "review_summary/$correctCount/$totalCount"
        }
    }
}
