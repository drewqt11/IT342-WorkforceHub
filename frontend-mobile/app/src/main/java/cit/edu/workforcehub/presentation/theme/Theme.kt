package cit.edu.workforcehub.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Our color scheme using AppColors
private val LightColorScheme = lightColorScheme(
    primary = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
    onPrimary = cit.edu.workforcehub.presentation.theme.AppColors.white,
    primaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.blue100,
    onPrimaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.blue900,
    secondary = cit.edu.workforcehub.presentation.theme.AppColors.teal500,
    onSecondary = cit.edu.workforcehub.presentation.theme.AppColors.white,
    secondaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.teal100,
    onSecondaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.teal700,
    tertiary = cit.edu.workforcehub.presentation.theme.AppColors.blue700,
    background = cit.edu.workforcehub.presentation.theme.AppColors.gray50,
    surface = cit.edu.workforcehub.presentation.theme.AppColors.white,
    onSurface = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
    surfaceVariant = cit.edu.workforcehub.presentation.theme.AppColors.gray100,
    outline = cit.edu.workforcehub.presentation.theme.AppColors.gray200,
)

private val DarkColorScheme = darkColorScheme(
    primary = cit.edu.workforcehub.presentation.theme.AppColors.blue300,
    onPrimary = cit.edu.workforcehub.presentation.theme.AppColors.white,
    primaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.blue900,
    onPrimaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.blue100,
    secondary = cit.edu.workforcehub.presentation.theme.AppColors.teal300,
    onSecondary = cit.edu.workforcehub.presentation.theme.AppColors.white,
    secondaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.teal900,
    onSecondaryContainer = cit.edu.workforcehub.presentation.theme.AppColors.teal100,
    tertiary = cit.edu.workforcehub.presentation.theme.AppColors.blue500,
    background = cit.edu.workforcehub.presentation.theme.AppColors.gray800,
    surface = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
    onSurface = cit.edu.workforcehub.presentation.theme.AppColors.white,
    surfaceVariant = cit.edu.workforcehub.presentation.theme.AppColors.gray700,
    outline = cit.edu.workforcehub.presentation.theme.AppColors.gray500,
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 