package com.junkfood.seal.ui.navigation.graph

import android.webkit.CookieManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.arg
import com.junkfood.seal.ui.common.id
import com.junkfood.seal.ui.page.onboarding.OnboardingScreen
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.SupportDeveloperPage
import com.junkfood.seal.ui.page.settings.about.UpdatePage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.DarkThemePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.command.TemplateEditPage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.directory.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.format.SubtitlePreference
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.interaction.InteractionPreferencePage
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.ProxySettingsPage
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.settings.sealplus.SealPlusExtrasPage
import com.junkfood.seal.ui.page.settings.security.SecuritySettingsPage
import com.junkfood.seal.ui.page.settings.troubleshooting.TroubleShootingPage

fun NavGraphBuilder.settingsGraph(
    onNavigateBack: () -> Unit,
    onNavigateTo: (route: String) -> Unit,
    cookiesViewModel: CookiesViewModel,
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences(onNavigateBack)
        }
        animatedComposable(Route.SETTINGS_PAGE) {
            SettingsPage(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo)
        }
        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(onNavigateBack = { onNavigateBack() }) {
                onNavigateTo(Route.TEMPLATE)
            }
        }
        animatedComposable(Route.DOWNLOAD_FORMAT) {
            DownloadFormatPreferences(onNavigateBack = onNavigateBack) {
                onNavigateTo(Route.SUBTITLE_PREFERENCES)
            }
        }
        animatedComposable(Route.SUBTITLE_PREFERENCES) { SubtitlePreference { onNavigateBack() } }
        animatedComposable(Route.ABOUT) {
            AboutPage(
                onNavigateBack = onNavigateBack,
                onNavigateToCreditsPage = { onNavigateTo(Route.CREDITS) },
                onNavigateToUpdatePage = { onNavigateTo(Route.AUTO_UPDATE) },
                onNavigateToDonatePage = { onNavigateTo(Route.DONATE) },
                onNavigateToOnboarding = { onNavigateTo(Route.ONBOARDING) },
            )
        }
        animatedComposable(Route.DONATE) { SupportDeveloperPage(onNavigateBack = onNavigateBack) }
        animatedComposable(Route.CREDITS) { CreditsPage(onNavigateBack) }
        animatedComposable(Route.AUTO_UPDATE) { UpdatePage(onNavigateBack) }
        animatedComposable(Route.APPEARANCE) {
            AppearancePreferences(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo)
        }
        animatedComposable(Route.INTERACTION) { InteractionPreferencePage(onBack = onNavigateBack) }
        animatedComposable(Route.LANGUAGES) { LanguagePage { onNavigateBack() } }
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onNavigateBack() }
        }
        animatedComposable(Route.TEMPLATE) {
            TemplateListPage(onNavigateBack = onNavigateBack) {
                onNavigateTo(Route.TEMPLATE_EDIT id it)
            }
        }
        animatedComposable(
            Route.TEMPLATE_EDIT arg Route.TEMPLATE_ID,
            arguments = listOf(navArgument(Route.TEMPLATE_ID) { type = NavType.IntType }),
        ) {
            TemplateEditPage(onNavigateBack, it.arguments?.getInt(Route.TEMPLATE_ID) ?: -1)
        }
        animatedComposable(Route.DARK_THEME) { DarkThemePreferences { onNavigateBack() } }
        animatedComposable(Route.NETWORK_PREFERENCES) {
            NetworkPreferences(
                navigateToCookieProfilePage = { onNavigateTo(Route.COOKIE_PROFILE) }
            ) {
                onNavigateBack()
            }
        }
        animatedComposable(Route.COOKIE_PROFILE) {
            CookieProfilePage(
                cookiesViewModel = cookiesViewModel,
                navigateToCookieGeneratorPage = { onNavigateTo(Route.COOKIE_GENERATOR_WEBVIEW) },
            ) {
                onNavigateBack()
            }
        }
        animatedComposable(Route.COOKIE_GENERATOR_WEBVIEW) {
            WebViewPage(cookiesViewModel = cookiesViewModel) {
                onNavigateBack()
                CookieManager.getInstance().flush()
            }
        }
        animatedComposable(Route.SEALPLUS_EXTRAS) {
            SealPlusExtrasPage(
                onNavigateBack = onNavigateBack,
                onNavigateToSecurity = { onNavigateTo(Route.SECURITY_SETTINGS) },
                onNavigateToProxySettings = { onNavigateTo(Route.PROXY_SETTINGS) }
            )
        }
        animatedComposable(Route.SECURITY_SETTINGS) {
            SecuritySettingsPage(onBackPressed = onNavigateBack)
        }
        animatedComposable(Route.PROXY_SETTINGS) {
            ProxySettingsPage(onNavigateBack = onNavigateBack)
        }
        animatedComposable(Route.TROUBLESHOOTING) {
            TroubleShootingPage(onNavigateTo = onNavigateTo, onBack = onNavigateBack)
        }
        animatedComposable(Route.ONBOARDING) {
            OnboardingScreen(onFinish = onNavigateBack)
        }
    }
}
