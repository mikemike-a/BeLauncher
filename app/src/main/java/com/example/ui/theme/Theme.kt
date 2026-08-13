package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AkoShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun AkoTheme(
    themeMode: AkoThemeMode = AkoThemeMode.COTONOU,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AkoThemeMode.COTONOU -> if (darkTheme) darkColorScheme(
            primary = CotonouPrimaryDark,
            onPrimary = CotonouTextPrimaryDark,
            secondary = CotonouSecondaryDark,
            onSecondary = CotonouTextPrimaryDark,
            tertiary = CotonouAccent,
            onTertiary = CotonouTextPrimaryDark,
            background = CotonouBackgroundDark,
            onBackground = CotonouTextPrimaryDark,
            surface = CotonouSurfaceDark,
            onSurface = CotonouTextPrimaryDark,
            surfaceVariant = CotonouSurfaceDark.copy(alpha = 0.5f),
            onSurfaceVariant = CotonouTextSecondaryDark
        ) else lightColorScheme(
            primary = CotonouPrimary,
            onPrimary = Color.White,
            secondary = CotonouSecondary,
            onSecondary = Color.White,
            tertiary = CotonouAccent,
            onTertiary = CotonouTextPrimary,
            background = CotonouBackground,
            onBackground = CotonouTextPrimary,
            surface = CotonouSurface,
            onSurface = CotonouTextPrimary,
            surfaceVariant = Color(0xFFF3EADF),
            onSurfaceVariant = CotonouTextSecondary
        )
        AkoThemeMode.ABOMEY -> if (darkTheme) darkColorScheme(
            primary = AbomeyPrimaryDark,
            onPrimary = AbomeyTextPrimaryDark,
            secondary = AbomeySecondaryDark,
            onSecondary = AbomeyTextPrimaryDark,
            tertiary = AbomeyAccent,
            onTertiary = AbomeyTextPrimaryDark,
            background = AbomeyBackgroundDark,
            onBackground = AbomeyTextPrimaryDark,
            surface = AbomeySurfaceDark,
            onSurface = AbomeyTextPrimaryDark,
            surfaceVariant = AbomeySurfaceDark.copy(alpha = 0.5f),
            onSurfaceVariant = AbomeyTextSecondaryDark
        ) else lightColorScheme(
            primary = AbomeyPrimary,
            onPrimary = Color.White,
            secondary = AbomeySecondary,
            onSecondary = Color.White,
            tertiary = AbomeyAccent,
            onTertiary = AbomeyTextPrimary,
            background = AbomeyBackground,
            onBackground = AbomeyTextPrimary,
            surface = AbomeySurface,
            onSurface = AbomeyTextPrimary,
            surfaceVariant = Color(0xFFF2E7D8),
            onSurfaceVariant = AbomeyTextSecondary
        )
        AkoThemeMode.OUIDAH -> if (darkTheme) darkColorScheme(
            primary = OuidahPrimaryDark,
            onPrimary = OuidahTextPrimaryDark,
            secondary = OuidahSecondaryDark,
            onSecondary = OuidahTextPrimaryDark,
            tertiary = OuidahAccent,
            onTertiary = OuidahTextPrimaryDark,
            background = OuidahBackgroundDark,
            onBackground = OuidahTextPrimaryDark,
            surface = OuidahSurfaceDark,
            onSurface = OuidahTextPrimaryDark,
            surfaceVariant = OuidahSurfaceDark.copy(alpha = 0.5f),
            onSurfaceVariant = OuidahTextSecondaryDark
        ) else lightColorScheme(
            primary = OuidahPrimary,
            onPrimary = Color.White,
            secondary = OuidahSecondary,
            onSecondary = Color.White,
            tertiary = OuidahAccent,
            onTertiary = OuidahTextPrimary,
            background = OuidahBackground,
            onBackground = OuidahTextPrimary,
            surface = OuidahSurface,
            onSurface = OuidahTextPrimary,
            surfaceVariant = Color(0xFFEFE8DF),
            onSurfaceVariant = OuidahTextSecondary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AkoShapes,
        typography = Typography,
        content = content
    )
}
