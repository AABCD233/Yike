package com.yike.focus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
fun MiuixTemplateTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec

    val miuixPaletteStyle = try {
        ThemePaletteStyle.valueOf(colorStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }

    val miuixColorSpec = if (colorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val controller = ThemeController(
        when (appSettings.colorMode) {
            ColorMode.SYSTEM -> ColorSchemeMode.System
            ColorMode.LIGHT -> ColorSchemeMode.Light
            ColorMode.DARK -> ColorSchemeMode.Dark
            ColorMode.MONET_SYSTEM -> ColorSchemeMode.MonetSystem
            ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
            ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> ColorSchemeMode.MonetDark
        },
        keyColor = if (appSettings.keyColor == 0) null else Color(appSettings.keyColor),
        isDark = darkTheme,
        paletteStyle = miuixPaletteStyle,
        colorSpec = miuixColorSpec,
    )

    MiuixTheme(
        controller = controller,
        content = {
            LaunchedEffect(darkTheme) {
                val window = (context as? Activity)?.window ?: return@LaunchedEffect
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            val miuixColors = MiuixTheme.colorScheme
            // Material3 组件（对话框、输入框、下拉菜单）也套一层同色系的 MaterialTheme，
            // 否则在 Miuix 模式下会掉回 Material 默认配色，看着很割裂。
            MaterialTheme(
                colorScheme = if (darkTheme) {
                    darkColorScheme(
                        primary = miuixColors.primary,
                        onPrimary = miuixColors.onPrimary,
                        primaryContainer = miuixColors.primaryContainer,
                        onPrimaryContainer = miuixColors.onPrimaryContainer,
                        secondaryContainer = miuixColors.secondaryContainer,
                        onSecondaryContainer = miuixColors.onSecondaryContainer,
                        background = miuixColors.background,
                        onBackground = miuixColors.onBackground,
                        surface = miuixColors.surface,
                        onSurface = miuixColors.onSurface,
                        surfaceVariant = miuixColors.surfaceContainer,
                        onSurfaceVariant = miuixColors.onSurfaceVariantSummary,
                        outline = miuixColors.outline,
                    )
                } else {
                    lightColorScheme(
                        primary = miuixColors.primary,
                        onPrimary = miuixColors.onPrimary,
                        primaryContainer = miuixColors.primaryContainer,
                        onPrimaryContainer = miuixColors.onPrimaryContainer,
                        secondaryContainer = miuixColors.secondaryContainer,
                        onSecondaryContainer = miuixColors.onSecondaryContainer,
                        background = miuixColors.background,
                        onBackground = miuixColors.onBackground,
                        surface = miuixColors.surface,
                        onSurface = miuixColors.onSurface,
                        surfaceVariant = miuixColors.surfaceContainer,
                        onSurfaceVariant = miuixColors.onSurfaceVariantSummary,
                        outline = miuixColors.outline,
                    )
                },
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides miuixColors.onBackground,
                ) {
                    content()
                }
            }
        }
    )
}
