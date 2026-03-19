package com.junkfood.seal.ui.navigation.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.animatedComposableVariant
import com.junkfood.seal.ui.common.arg
import com.junkfood.seal.ui.common.id
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.page.command.TaskListPage
import com.junkfood.seal.ui.page.command.TaskLogPage
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.home.NewHomePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.videolist.VideoListPage

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    dialogViewModel: DownloadDialogViewModel,
    cookiesViewModel: CookiesViewModel,
    onMenuOpen: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.HOME,
        modifier = modifier
    ) {
        animatedComposable(Route.HOME) {
            NewHomePage(
                dialogViewModel = dialogViewModel,
                onMenuOpen = onMenuOpen,
                onNavigateToDownloads = {
                    navController.navigate(Route.DOWNLOADS) { launchSingleTop = true }
                },
                onNavigateToSupport = {
                    navController.navigate(Route.DONATE) { launchSingleTop = true }
                }
            )
        }

        animatedComposable(Route.DOWNLOADS) {
            VideoListPage { onNavigateBack() }
        }

        animatedComposableVariant(Route.TASK_LIST) {
            TaskListPage(
                onNavigateBack = onNavigateBack,
                onNavigateToDetail = { navController.navigate(Route.TASK_LOG id it) },
            )
        }

        slideInVerticallyComposable(
            Route.TASK_LOG arg Route.TASK_HASHCODE,
            arguments = listOf(navArgument(Route.TASK_HASHCODE) { type = NavType.IntType }),
        ) {
            TaskLogPage(
                onNavigateBack = onNavigateBack,
                taskHashCode = it.arguments?.getInt(Route.TASK_HASHCODE) ?: -1,
            )
        }

        // Подключаем граф настроек
        settingsGraph(
            onNavigateBack = onNavigateBack,
            onNavigateTo = { route ->
                navController.navigate(route) { launchSingleTop = true }
            },
            cookiesViewModel = cookiesViewModel,
        )
    }
}