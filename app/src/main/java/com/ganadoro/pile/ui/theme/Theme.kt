package com.ganadoro.pile.ui.theme


import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ExtendedColorScheme(
    val customColorList: List<ColorFamily> = emptyList()
)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

val extendedLight = ExtendedColorScheme(
    customColorList = listOf(

        ColorFamily(
            customColor1Light,
            onCustomColor1Light,
            customColor1ContainerLight,
            onCustomColor1ContainerLight,
        ),
        ColorFamily(
            customColor2Light,
            onCustomColor2Light,
            customColor2ContainerLight,
            onCustomColor2ContainerLight,
        ),
        ColorFamily(
            customColor3Light,
            onCustomColor3Light,
            customColor3ContainerLight,
            onCustomColor3ContainerLight,
        ),
        ColorFamily(
            customColor4Light,
            onCustomColor4Light,
            customColor4ContainerLight,
            onCustomColor4ContainerLight,
        ),
        ColorFamily(
            customColor5Light,
            onCustomColor5Light,
            customColor5ContainerLight,
            onCustomColor5ContainerLight,
        ),
        ColorFamily(
            customColor6Light,
            onCustomColor6Light,
            customColor6ContainerLight,
            onCustomColor6ContainerLight,
        ),
        ColorFamily(
            customColor7Light,
            onCustomColor7Light,
            customColor7ContainerLight,
            onCustomColor7ContainerLight,
        ),
        ColorFamily(
            customColor8Light,
            onCustomColor8Light,
            customColor8ContainerLight,
            onCustomColor8ContainerLight,
        ),
        ColorFamily(
            customColor9Light,
            onCustomColor9Light,
            customColor9ContainerLight,
            onCustomColor9ContainerLight,
        ),
        ColorFamily(
            customColor10Light,
            onCustomColor10Light,
            customColor10ContainerLight,
            onCustomColor10ContainerLight,
        ),
        ColorFamily(
            customColor11Light,
            onCustomColor11Light,
            customColor11ContainerLight,
            onCustomColor11ContainerLight,
        ),
        ColorFamily(
            customColor12Light,
            onCustomColor12Light,
            customColor12ContainerLight,
            onCustomColor12ContainerLight,
        ),
        ColorFamily(
            customColor13Light,
            onCustomColor13Light,
            customColor13ContainerLight,
            onCustomColor13ContainerLight,
        ),
        ColorFamily(
            customColor14Light,
            onCustomColor14Light,
            customColor14ContainerLight,
            onCustomColor14ContainerLight,
        ),
        ColorFamily(
            customColor15Light,
            onCustomColor15Light,
            customColor15ContainerLight,
            onCustomColor15ContainerLight,
        ),
        ColorFamily(
            customColor16Light,
            onCustomColor16Light,
            customColor16ContainerLight,
            onCustomColor16ContainerLight,
        ),
        ColorFamily(
            customColor17Light,
            onCustomColor17Light,
            customColor17ContainerLight,
            onCustomColor17ContainerLight,
        ),
        ColorFamily(
            customColor18Light,
            onCustomColor18Light,
            customColor18ContainerLight,
            onCustomColor18ContainerLight,
        ),
        ColorFamily(
            customColor19Light,
            onCustomColor19Light,
            customColor19ContainerLight,
            onCustomColor19ContainerLight,
        ),
        ColorFamily(
            customColor20Light,
            onCustomColor20Light,
            customColor20ContainerLight,
            onCustomColor20ContainerLight,
        ),
        ColorFamily(
            customColor21Light,
            onCustomColor21Light,
            customColor21ContainerLight,
            onCustomColor21ContainerLight,
        ),
        ColorFamily(
            customColor22Light,
            onCustomColor22Light,
            customColor22ContainerLight,
            onCustomColor22ContainerLight,
        ),
        ColorFamily(
            customColor23Light,
            onCustomColor23Light,
            customColor23ContainerLight,
            onCustomColor23ContainerLight,
        ),
        ColorFamily(
            customColor24Light,
            onCustomColor24Light,
            customColor24ContainerLight,
            onCustomColor24ContainerLight,
        ),
        ColorFamily(
            customColor25Light,
            onCustomColor25Light,
            customColor25ContainerLight,
            onCustomColor25ContainerLight,
        ),
        ColorFamily(
            customColor26Light,
            onCustomColor26Light,
            customColor26ContainerLight,
            onCustomColor26ContainerLight,
        ),
        ColorFamily(
            customColor27Light,
            onCustomColor27Light,
            customColor27ContainerLight,
            onCustomColor27ContainerLight,
        ),
        ColorFamily(
            customColor28Light,
            onCustomColor28Light,
            customColor28ContainerLight,
            onCustomColor28ContainerLight,
        ),
        ColorFamily(
            customColor29Light,
            onCustomColor29Light,
            customColor29ContainerLight,
            onCustomColor29ContainerLight,
        ),
        ColorFamily(
            customColor30Light,
            onCustomColor30Light,
            customColor30ContainerLight,
            onCustomColor30ContainerLight,
        )
    )
)

val extendedDark = ExtendedColorScheme(
    customColorList = listOf(
        ColorFamily(
            customColor1Dark,
            onCustomColor1Dark,
            customColor1ContainerDark,
            onCustomColor1ContainerDark
        ),
        ColorFamily(
            customColor2Dark,
            onCustomColor2Dark,
            customColor2ContainerDark,
            onCustomColor2ContainerDark
        ),
        ColorFamily(
            customColor3Dark,
            onCustomColor3Dark,
            customColor3ContainerDark,
            onCustomColor3ContainerDark,
        ),
        ColorFamily(
            customColor4Dark,
            onCustomColor4Dark,
            customColor4ContainerDark,
            onCustomColor4ContainerDark,
        ),
        ColorFamily(
            customColor5Dark,
            onCustomColor5Dark,
            customColor5ContainerDark,
            onCustomColor5ContainerDark,
        ),
        ColorFamily(
            customColor6Dark,
            onCustomColor6Dark,
            customColor6ContainerDark,
            onCustomColor6ContainerDark,
        ),
        ColorFamily(
            customColor7Dark,
            onCustomColor7Dark,
            customColor7ContainerDark,
            onCustomColor7ContainerDark,
        ),
        ColorFamily(
            customColor8Dark,
            onCustomColor8Dark,
            customColor8ContainerDark,
            onCustomColor8ContainerDark,
        ),
        ColorFamily(
            customColor9Dark,
            onCustomColor9Dark,
            customColor9ContainerDark,
            onCustomColor9ContainerDark,
        ),
        ColorFamily(
            customColor10Dark,
            onCustomColor10Dark,
            customColor10ContainerDark,
            onCustomColor10ContainerDark,
        ),
        ColorFamily(
            customColor11Dark,
            onCustomColor11Dark,
            customColor11ContainerDark,
            onCustomColor11ContainerDark,
        ),
        ColorFamily(
            customColor12Dark,
            onCustomColor12Dark,
            customColor12ContainerDark,
            onCustomColor12ContainerDark,
        ),
        ColorFamily(
            customColor13Dark,
            onCustomColor13Dark,
            customColor13ContainerDark,
            onCustomColor13ContainerDark,
        ),
        ColorFamily(
            customColor14Dark,
            onCustomColor14Dark,
            customColor14ContainerDark,
            onCustomColor14ContainerDark,
        ),
        ColorFamily(
            customColor15Dark,
            onCustomColor15Dark,
            customColor15ContainerDark,
            onCustomColor15ContainerDark,
        ),
        ColorFamily(
            customColor16Dark,
            onCustomColor16Dark,
            customColor16ContainerDark,
            onCustomColor16ContainerDark,
        ),
        ColorFamily(
            customColor17Dark,
            onCustomColor17Dark,
            customColor17ContainerDark,
            onCustomColor17ContainerDark,
        ),
        ColorFamily(
            customColor18Dark,
            onCustomColor18Dark,
            customColor18ContainerDark,
            onCustomColor18ContainerDark,
        ),
        ColorFamily(
            customColor19Dark,
            onCustomColor19Dark,
            customColor19ContainerDark,
            onCustomColor19ContainerDark,
        ),
        ColorFamily(
            customColor20Dark,
            onCustomColor20Dark,
            customColor20ContainerDark,
            onCustomColor20ContainerDark,
        ),
        ColorFamily(
            customColor21Dark,
            onCustomColor21Dark,
            customColor21ContainerDark,
            onCustomColor21ContainerDark,
        ),
        ColorFamily(
            customColor22Dark,
            onCustomColor22Dark,
            customColor22ContainerDark,
            onCustomColor22ContainerDark,
        ),
        ColorFamily(
            customColor23Dark,
            onCustomColor23Dark,
            customColor23ContainerDark,
            onCustomColor23ContainerDark,
        ),
        ColorFamily(
            customColor24Dark,
            onCustomColor24Dark,
            customColor24ContainerDark,
            onCustomColor24ContainerDark,
        ),
        ColorFamily(
            customColor25Dark,
            onCustomColor25Dark,
            customColor25ContainerDark,
            onCustomColor25ContainerDark,
        ),
        ColorFamily(
            customColor26Dark,
            onCustomColor26Dark,
            customColor26ContainerDark,
            onCustomColor26ContainerDark,
        ),
        ColorFamily(
            customColor27Dark,
            onCustomColor27Dark,
            customColor27ContainerDark,
            onCustomColor27ContainerDark,
        ),
        ColorFamily(
            customColor28Dark,
            onCustomColor28Dark,
            customColor28ContainerDark,
            onCustomColor28ContainerDark,
        ),
        ColorFamily(
            customColor29Dark,
            onCustomColor29Dark,
            customColor29ContainerDark,
            onCustomColor29ContainerDark,
        ),
        ColorFamily(
            customColor30Dark,
            onCustomColor30Dark,
            customColor30ContainerDark,
            onCustomColor30ContainerDark,
        )
    )
)

val extendedLightMediumContrast = ExtendedColorScheme(
    customColorList = listOf(
    ColorFamily(
        customColor1LightMediumContrast,
        onCustomColor1LightMediumContrast,
        customColor1ContainerLightMediumContrast,
        onCustomColor1ContainerLightMediumContrast
    ),
    ColorFamily(
        customColor2LightMediumContrast,
        onCustomColor2LightMediumContrast,
        customColor2ContainerLightMediumContrast,
        onCustomColor2ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor3LightMediumContrast,
        onCustomColor3LightMediumContrast,
        customColor3ContainerLightMediumContrast,
        onCustomColor3ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor4LightMediumContrast,
        onCustomColor4LightMediumContrast,
        customColor4ContainerLightMediumContrast,
        onCustomColor4ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor5LightMediumContrast,
        onCustomColor5LightMediumContrast,
        customColor5ContainerLightMediumContrast,
        onCustomColor5ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor6LightMediumContrast,
        onCustomColor6LightMediumContrast,
        customColor6ContainerLightMediumContrast,
        onCustomColor6ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor7LightMediumContrast,
        onCustomColor7LightMediumContrast,
        customColor7ContainerLightMediumContrast,
        onCustomColor7ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor8LightMediumContrast,
        onCustomColor8LightMediumContrast,
        customColor8ContainerLightMediumContrast,
        onCustomColor8ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor9LightMediumContrast,
        onCustomColor9LightMediumContrast,
        customColor9ContainerLightMediumContrast,
        onCustomColor9ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor10LightMediumContrast,
        onCustomColor10LightMediumContrast,
        customColor10ContainerLightMediumContrast,
        onCustomColor10ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor11LightMediumContrast,
        onCustomColor11LightMediumContrast,
        customColor11ContainerLightMediumContrast,
        onCustomColor11ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor12LightMediumContrast,
        onCustomColor12LightMediumContrast,
        customColor12ContainerLightMediumContrast,
        onCustomColor12ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor13LightMediumContrast,
        onCustomColor13LightMediumContrast,
        customColor13ContainerLightMediumContrast,
        onCustomColor13ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor14LightMediumContrast,
        onCustomColor14LightMediumContrast,
        customColor14ContainerLightMediumContrast,
        onCustomColor14ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor15LightMediumContrast,
        onCustomColor15LightMediumContrast,
        customColor15ContainerLightMediumContrast,
        onCustomColor15ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor16LightMediumContrast,
        onCustomColor16LightMediumContrast,
        customColor16ContainerLightMediumContrast,
        onCustomColor16ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor17LightMediumContrast,
        onCustomColor17LightMediumContrast,
        customColor17ContainerLightMediumContrast,
        onCustomColor17ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor18LightMediumContrast,
        onCustomColor18LightMediumContrast,
        customColor18ContainerLightMediumContrast,
        onCustomColor18ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor19LightMediumContrast,
        onCustomColor19LightMediumContrast,
        customColor19ContainerLightMediumContrast,
        onCustomColor19ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor20LightMediumContrast,
        onCustomColor20LightMediumContrast,
        customColor20ContainerLightMediumContrast,
        onCustomColor20ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor21LightMediumContrast,
        onCustomColor21LightMediumContrast,
        customColor21ContainerLightMediumContrast,
        onCustomColor21ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor22LightMediumContrast,
        onCustomColor22LightMediumContrast,
        customColor22ContainerLightMediumContrast,
        onCustomColor22ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor23LightMediumContrast,
        onCustomColor23LightMediumContrast,
        customColor23ContainerLightMediumContrast,
        onCustomColor23ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor24LightMediumContrast,
        onCustomColor24LightMediumContrast,
        customColor24ContainerLightMediumContrast,
        onCustomColor24ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor25LightMediumContrast,
        onCustomColor25LightMediumContrast,
        customColor25ContainerLightMediumContrast,
        onCustomColor25ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor26LightMediumContrast,
        onCustomColor26LightMediumContrast,
        customColor26ContainerLightMediumContrast,
        onCustomColor26ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor27LightMediumContrast,
        onCustomColor27LightMediumContrast,
        customColor27ContainerLightMediumContrast,
        onCustomColor27ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor28LightMediumContrast,
        onCustomColor28LightMediumContrast,
        customColor28ContainerLightMediumContrast,
        onCustomColor28ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor29LightMediumContrast,
        onCustomColor29LightMediumContrast,
        customColor29ContainerLightMediumContrast,
        onCustomColor29ContainerLightMediumContrast,
    ),
    ColorFamily(
        customColor30LightMediumContrast,
        onCustomColor30LightMediumContrast,
        customColor30ContainerLightMediumContrast,
        onCustomColor30ContainerLightMediumContrast,
    ))
)

val extendedLightHighContrast = ExtendedColorScheme(
    customColorList = listOf(
    ColorFamily(
        customColor1LightHighContrast,
        onCustomColor1LightHighContrast,
        customColor1ContainerLightHighContrast,
        onCustomColor1ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor2LightHighContrast,
        onCustomColor2LightHighContrast,
        customColor2ContainerLightHighContrast,
        onCustomColor2ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor3LightHighContrast,
        onCustomColor3LightHighContrast,
        customColor3ContainerLightHighContrast,
        onCustomColor3ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor4LightHighContrast,
        onCustomColor4LightHighContrast,
        customColor4ContainerLightHighContrast,
        onCustomColor4ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor5LightHighContrast,
        onCustomColor5LightHighContrast,
        customColor5ContainerLightHighContrast,
        onCustomColor5ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor6LightHighContrast,
        onCustomColor6LightHighContrast,
        customColor6ContainerLightHighContrast,
        onCustomColor6ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor7LightHighContrast,
        onCustomColor7LightHighContrast,
        customColor7ContainerLightHighContrast,
        onCustomColor7ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor8LightHighContrast,
        onCustomColor8LightHighContrast,
        customColor8ContainerLightHighContrast,
        onCustomColor8ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor9LightHighContrast,
        onCustomColor9LightHighContrast,
        customColor9ContainerLightHighContrast,
        onCustomColor9ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor10LightHighContrast,
        onCustomColor10LightHighContrast,
        customColor10ContainerLightHighContrast,
        onCustomColor10ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor11LightHighContrast,
        onCustomColor11LightHighContrast,
        customColor11ContainerLightHighContrast,
        onCustomColor11ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor12LightHighContrast,
        onCustomColor12LightHighContrast,
        customColor12ContainerLightHighContrast,
        onCustomColor12ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor13LightHighContrast,
        onCustomColor13LightHighContrast,
        customColor13ContainerLightHighContrast,
        onCustomColor13ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor14LightHighContrast,
        onCustomColor14LightHighContrast,
        customColor14ContainerLightHighContrast,
        onCustomColor14ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor15LightHighContrast,
        onCustomColor15LightHighContrast,
        customColor15ContainerLightHighContrast,
        onCustomColor15ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor16LightHighContrast,
        onCustomColor16LightHighContrast,
        customColor16ContainerLightHighContrast,
        onCustomColor16ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor17LightHighContrast,
        onCustomColor17LightHighContrast,
        customColor17ContainerLightHighContrast,
        onCustomColor17ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor18LightHighContrast,
        onCustomColor18LightHighContrast,
        customColor18ContainerLightHighContrast,
        onCustomColor18ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor19LightHighContrast,
        onCustomColor19LightHighContrast,
        customColor19ContainerLightHighContrast,
        onCustomColor19ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor20LightHighContrast,
        onCustomColor20LightHighContrast,
        customColor20ContainerLightHighContrast,
        onCustomColor20ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor21LightHighContrast,
        onCustomColor21LightHighContrast,
        customColor21ContainerLightHighContrast,
        onCustomColor21ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor22LightHighContrast,
        onCustomColor22LightHighContrast,
        customColor22ContainerLightHighContrast,
        onCustomColor22ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor23LightHighContrast,
        onCustomColor23LightHighContrast,
        customColor23ContainerLightHighContrast,
        onCustomColor23ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor24LightHighContrast,
        onCustomColor24LightHighContrast,
        customColor24ContainerLightHighContrast,
        onCustomColor24ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor25LightHighContrast,
        onCustomColor25LightHighContrast,
        customColor25ContainerLightHighContrast,
        onCustomColor25ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor26LightHighContrast,
        onCustomColor26LightHighContrast,
        customColor26ContainerLightHighContrast,
        onCustomColor26ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor27LightHighContrast,
        onCustomColor27LightHighContrast,
        customColor27ContainerLightHighContrast,
        onCustomColor27ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor28LightHighContrast,
        onCustomColor28LightHighContrast,
        customColor28ContainerLightHighContrast,
        onCustomColor28ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor29LightHighContrast,
        onCustomColor29LightHighContrast,
        customColor29ContainerLightHighContrast,
        onCustomColor29ContainerLightHighContrast,
    ),
    ColorFamily(
        customColor30LightHighContrast,
        onCustomColor30LightHighContrast,
        customColor30ContainerLightHighContrast,
        onCustomColor30ContainerLightHighContrast,
    ))
)

val extendedDarkMediumContrast = ExtendedColorScheme(
    customColorList = listOf(
    ColorFamily(
        customColor1DarkMediumContrast,
        onCustomColor1DarkMediumContrast,
        customColor1ContainerDarkMediumContrast,
        onCustomColor1ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor2DarkMediumContrast,
        onCustomColor2DarkMediumContrast,
        customColor2ContainerDarkMediumContrast,
        onCustomColor2ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor3DarkMediumContrast,
        onCustomColor3DarkMediumContrast,
        customColor3ContainerDarkMediumContrast,
        onCustomColor3ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor4DarkMediumContrast,
        onCustomColor4DarkMediumContrast,
        customColor4ContainerDarkMediumContrast,
        onCustomColor4ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor5DarkMediumContrast,
        onCustomColor5DarkMediumContrast,
        customColor5ContainerDarkMediumContrast,
        onCustomColor5ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor6DarkMediumContrast,
        onCustomColor6DarkMediumContrast,
        customColor6ContainerDarkMediumContrast,
        onCustomColor6ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor7DarkMediumContrast,
        onCustomColor7DarkMediumContrast,
        customColor7ContainerDarkMediumContrast,
        onCustomColor7ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor8DarkMediumContrast,
        onCustomColor8DarkMediumContrast,
        customColor8ContainerDarkMediumContrast,
        onCustomColor8ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor9DarkMediumContrast,
        onCustomColor9DarkMediumContrast,
        customColor9ContainerDarkMediumContrast,
        onCustomColor9ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor10DarkMediumContrast,
        onCustomColor10DarkMediumContrast,
        customColor10ContainerDarkMediumContrast,
        onCustomColor10ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor11DarkMediumContrast,
        onCustomColor11DarkMediumContrast,
        customColor11ContainerDarkMediumContrast,
        onCustomColor11ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor12DarkMediumContrast,
        onCustomColor12DarkMediumContrast,
        customColor12ContainerDarkMediumContrast,
        onCustomColor12ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor13DarkMediumContrast,
        onCustomColor13DarkMediumContrast,
        customColor13ContainerDarkMediumContrast,
        onCustomColor13ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor14DarkMediumContrast,
        onCustomColor14DarkMediumContrast,
        customColor14ContainerDarkMediumContrast,
        onCustomColor14ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor15DarkMediumContrast,
        onCustomColor15DarkMediumContrast,
        customColor15ContainerDarkMediumContrast,
        onCustomColor15ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor16DarkMediumContrast,
        onCustomColor16DarkMediumContrast,
        customColor16ContainerDarkMediumContrast,
        onCustomColor16ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor17DarkMediumContrast,
        onCustomColor17DarkMediumContrast,
        customColor17ContainerDarkMediumContrast,
        onCustomColor17ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor18DarkMediumContrast,
        onCustomColor18DarkMediumContrast,
        customColor18ContainerDarkMediumContrast,
        onCustomColor18ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor19DarkMediumContrast,
        onCustomColor19DarkMediumContrast,
        customColor19ContainerDarkMediumContrast,
        onCustomColor19ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor20DarkMediumContrast,
        onCustomColor20DarkMediumContrast,
        customColor20ContainerDarkMediumContrast,
        onCustomColor20ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor21DarkMediumContrast,
        onCustomColor21DarkMediumContrast,
        customColor21ContainerDarkMediumContrast,
        onCustomColor21ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor22DarkMediumContrast,
        onCustomColor22DarkMediumContrast,
        customColor22ContainerDarkMediumContrast,
        onCustomColor22ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor23DarkMediumContrast,
        onCustomColor23DarkMediumContrast,
        customColor23ContainerDarkMediumContrast,
        onCustomColor23ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor24DarkMediumContrast,
        onCustomColor24DarkMediumContrast,
        customColor24ContainerDarkMediumContrast,
        onCustomColor24ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor25DarkMediumContrast,
        onCustomColor25DarkMediumContrast,
        customColor25ContainerDarkMediumContrast,
        onCustomColor25ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor26DarkMediumContrast,
        onCustomColor26DarkMediumContrast,
        customColor26ContainerDarkMediumContrast,
        onCustomColor26ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor27DarkMediumContrast,
        onCustomColor27DarkMediumContrast,
        customColor27ContainerDarkMediumContrast,
        onCustomColor27ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor28DarkMediumContrast,
        onCustomColor28DarkMediumContrast,
        customColor28ContainerDarkMediumContrast,
        onCustomColor28ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor29DarkMediumContrast,
        onCustomColor29DarkMediumContrast,
        customColor29ContainerDarkMediumContrast,
        onCustomColor29ContainerDarkMediumContrast,
    ),
    ColorFamily(
        customColor30DarkMediumContrast,
        onCustomColor30DarkMediumContrast,
        customColor30ContainerDarkMediumContrast,
        onCustomColor30ContainerDarkMediumContrast,
    ))
)

val extendedDarkHighContrast = ExtendedColorScheme(
    customColorList = listOf(
    ColorFamily(
        customColor1DarkHighContrast,
        onCustomColor1DarkHighContrast,
        customColor1ContainerDarkHighContrast,
        onCustomColor1ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor2DarkHighContrast,
        onCustomColor2DarkHighContrast,
        customColor2ContainerDarkHighContrast,
        onCustomColor2ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor3DarkHighContrast,
        onCustomColor3DarkHighContrast,
        customColor3ContainerDarkHighContrast,
        onCustomColor3ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor4DarkHighContrast,
        onCustomColor4DarkHighContrast,
        customColor4ContainerDarkHighContrast,
        onCustomColor4ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor5DarkHighContrast,
        onCustomColor5DarkHighContrast,
        customColor5ContainerDarkHighContrast,
        onCustomColor5ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor6DarkHighContrast,
        onCustomColor6DarkHighContrast,
        customColor6ContainerDarkHighContrast,
        onCustomColor6ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor7DarkHighContrast,
        onCustomColor7DarkHighContrast,
        customColor7ContainerDarkHighContrast,
        onCustomColor7ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor8DarkHighContrast,
        onCustomColor8DarkHighContrast,
        customColor8ContainerDarkHighContrast,
        onCustomColor8ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor9DarkHighContrast,
        onCustomColor9DarkHighContrast,
        customColor9ContainerDarkHighContrast,
        onCustomColor9ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor10DarkHighContrast,
        onCustomColor10DarkHighContrast,
        customColor10ContainerDarkHighContrast,
        onCustomColor10ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor11DarkHighContrast,
        onCustomColor11DarkHighContrast,
        customColor11ContainerDarkHighContrast,
        onCustomColor11ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor12DarkHighContrast,
        onCustomColor12DarkHighContrast,
        customColor12ContainerDarkHighContrast,
        onCustomColor12ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor13DarkHighContrast,
        onCustomColor13DarkHighContrast,
        customColor13ContainerDarkHighContrast,
        onCustomColor13ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor14DarkHighContrast,
        onCustomColor14DarkHighContrast,
        customColor14ContainerDarkHighContrast,
        onCustomColor14ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor15DarkHighContrast,
        onCustomColor15DarkHighContrast,
        customColor15ContainerDarkHighContrast,
        onCustomColor15ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor16DarkHighContrast,
        onCustomColor16DarkHighContrast,
        customColor16ContainerDarkHighContrast,
        onCustomColor16ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor17DarkHighContrast,
        onCustomColor17DarkHighContrast,
        customColor17ContainerDarkHighContrast,
        onCustomColor17ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor18DarkHighContrast,
        onCustomColor18DarkHighContrast,
        customColor18ContainerDarkHighContrast,
        onCustomColor18ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor19DarkHighContrast,
        onCustomColor19DarkHighContrast,
        customColor19ContainerDarkHighContrast,
        onCustomColor19ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor20DarkHighContrast,
        onCustomColor20DarkHighContrast,
        customColor20ContainerDarkHighContrast,
        onCustomColor20ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor21DarkHighContrast,
        onCustomColor21DarkHighContrast,
        customColor21ContainerDarkHighContrast,
        onCustomColor21ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor22DarkHighContrast,
        onCustomColor22DarkHighContrast,
        customColor22ContainerDarkHighContrast,
        onCustomColor22ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor23DarkHighContrast,
        onCustomColor23DarkHighContrast,
        customColor23ContainerDarkHighContrast,
        onCustomColor23ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor24DarkHighContrast,
        onCustomColor24DarkHighContrast,
        customColor24ContainerDarkHighContrast,
        onCustomColor24ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor25DarkHighContrast,
        onCustomColor25DarkHighContrast,
        customColor25ContainerDarkHighContrast,
        onCustomColor25ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor26DarkHighContrast,
        onCustomColor26DarkHighContrast,
        customColor26ContainerDarkHighContrast,
        onCustomColor26ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor27DarkHighContrast,
        onCustomColor27DarkHighContrast,
        customColor27ContainerDarkHighContrast,
        onCustomColor27ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor28DarkHighContrast,
        onCustomColor28DarkHighContrast,
        customColor28ContainerDarkHighContrast,
        onCustomColor28ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor29DarkHighContrast,
        onCustomColor29DarkHighContrast,
        customColor29ContainerDarkHighContrast,
        onCustomColor29ContainerDarkHighContrast,
    ),
    ColorFamily(
        customColor30DarkHighContrast,
        onCustomColor30DarkHighContrast,
        customColor30ContainerDarkHighContrast,
        onCustomColor30ContainerDarkHighContrast,
    )
)
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

private val LocalExtendedColorScheme = staticCompositionLocalOf<ExtendedColorScheme> {
    error("No ExtendedColorScheme provided")
}

@Composable
fun PileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val extendedColors = if (darkTheme) extendedDark else extendedLight

    CompositionLocalProvider(
        LocalExtendedColorScheme provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}


object ExtendedTheme {
    val colors: ExtendedColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColorScheme.current
}
