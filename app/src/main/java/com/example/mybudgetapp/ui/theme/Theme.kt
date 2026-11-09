package com.example.mybudgetapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Black,
    secondary = SoftGold,
    onSecondary = Black,
    tertiary = SoftGold,
    background = Black,
    onBackground = Ivory,
    surface = SurfaceBlack,
    onSurface = Ivory,
    surfaceVariant = ElevatedBlack,
    onSurfaceVariant = MutedIvory,
    outline = Bronze,
    outlineVariant = Bronze.copy(alpha = 0.55f),
    error = MutedRed,
    onError = Black
)

private val LuxeShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun MyBudgetAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = LuxeShapes,
        typography = Typography,
        content = content
    )
}
