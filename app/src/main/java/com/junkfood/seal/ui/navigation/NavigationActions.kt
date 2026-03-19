package com.junkfood.seal.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.junkfood.seal.ui.common.Route

class NavigationActions(private val navController: NavController) {
    val onBack: () -> Unit = {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.popBackStack()
        }
    }

    fun navigateTo(route: String) {
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(Route.HOME) { saveState = true }
            }
        }
    }
}