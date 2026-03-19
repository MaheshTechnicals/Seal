package com.junkfood.seal.ui.common

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.composable

// Тайминги: 300мс — идеальный баланс между скоростью и плавностью
const val DURATION_ENTER = 450
const val DURATION_EXIT = 350

// "Породистая" кривая: быстрый вход и очень мягкое затухание (стандарт Material 3)
private val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/**
 * Основная функция для навигации.
 * Красота: Эффект Z-оси (легкое масштабирование) + Fade.
 * Оптимизация: Масштаб меняется всего на 3%, что не перегружает GPU.
 */
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        // Вход: легкое увеличение из 97% в 100% + появление
        fadeIn(tween(DURATION_ENTER, easing = EmphasizedEasing)) +
                scaleIn(initialScale = 0.80f, animationSpec = tween(DURATION_ENTER, easing = EmphasizedEasing))
    },
    exitTransition = {
        // Выход: простое исчезновение (самое быстрое для процессора)
        fadeOut(tween(DURATION_EXIT))
    },
    popEnterTransition = {
        // Возврат: плавное появление
        fadeIn(tween(DURATION_ENTER))
    },
    popExitTransition = {
        // Уход назад: легкое уменьшение и затухание
        fadeOut(tween(DURATION_EXIT, easing = EmphasizedEasing)) +
                scaleOut(targetScale = 0.80f, animationSpec = tween(DURATION_EXIT, easing = EmphasizedEasing))
    },
    content = content,
)

/**
 * Вертикальная анимация (снизу вверх).
 * Красота: Используем частичное смещение (не на весь экран), чтобы не было "дерганий".
 */
fun NavGraphBuilder.slideInVerticallyComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        // Смещение на 1/6 экрана вверх — выглядит аккуратно
        slideInVertically(tween(DURATION_ENTER, easing = EmphasizedEasing)) { it / 6 } +
                fadeIn(tween(DURATION_ENTER))
    },
    exitTransition = {
        slideOutVertically(tween(DURATION_EXIT)) { it / 6 } +
                fadeOut(tween(DURATION_EXIT))
    },
    content = content,
)

/**
 * Вариант для вложенных настроек.
 * Только Crossfade — самый надежный вариант, если телефон за 500р совсем не тянет сдвиги.
 */
fun NavGraphBuilder.animatedComposableVariant(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { fadeIn(tween(DURATION_ENTER, easing = EmphasizedEasing)) },
    exitTransition = { fadeOut(tween(DURATION_EXIT)) },
    content = content
)