package com.jeudry.rosafiesta.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.resources.Res
import com.jeudry.rosafiesta.designsystem.resources.bricolage_grotesque_bold
import com.jeudry.rosafiesta.designsystem.resources.bricolage_grotesque_extrabold
import com.jeudry.rosafiesta.designsystem.resources.bricolage_grotesque_medium
import com.jeudry.rosafiesta.designsystem.resources.bricolage_grotesque_semibold
import com.jeudry.rosafiesta.designsystem.resources.figtree_bold
import com.jeudry.rosafiesta.designsystem.resources.figtree_medium
import com.jeudry.rosafiesta.designsystem.resources.figtree_regular
import com.jeudry.rosafiesta.designsystem.resources.figtree_semibold
import org.jetbrains.compose.resources.Font

/**
 * Escala tipográfica oficial de RosaFiesta (Sección 3.2: Bricolage Grotesque 500-800 + Figtree 400-700).
 */
@Immutable
public data class RfTypography(
    public val displayFamily: FontFamily,
    public val bodyFamily: FontFamily,
    public val heroTitle: TextStyle,
    public val screenTitle: TextStyle,
    public val sectionTitle: TextStyle,
    public val cardTitle: TextStyle,
    public val kpiPrice: TextStyle,
    public val body: TextStyle,
    public val bodyStrong: TextStyle,
    public val caption: TextStyle,
    public val chipLabel: TextStyle,
    public val tagLabel: TextStyle,
)

@Composable
public fun rememberBricolageGrotesqueFamily(): FontFamily {
    val medium = Font(Res.font.bricolage_grotesque_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.bricolage_grotesque_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.bricolage_grotesque_bold, FontWeight.Bold)
    val extraBold = Font(Res.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold)
    return remember(medium, semiBold, bold, extraBold) {
        FontFamily(medium, semiBold, bold, extraBold)
    }
}

@Composable
public fun rememberFigtreeFamily(): FontFamily {
    val regular = Font(Res.font.figtree_regular, FontWeight.Normal)
    val medium = Font(Res.font.figtree_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.figtree_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.figtree_bold, FontWeight.Bold)
    return remember(regular, medium, semiBold, bold) {
        FontFamily(regular, medium, semiBold, bold)
    }
}

@Composable
public fun rememberRfTypography(inkColor: Color, mutedColor: Color): RfTypography {
    val display = rememberBricolageGrotesqueFamily()
    val body = rememberFigtreeFamily()
    return remember(display, body, inkColor, mutedColor) {
        buildRfTypography(display, body, inkColor, mutedColor)
    }
}

internal fun buildRfTypography(
    display: FontFamily,
    body: FontFamily,
    inkColor: Color,
    mutedColor: Color,
): RfTypography = RfTypography(
    displayFamily = display,
    bodyFamily = body,
    heroTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.03).em,
        color = inkColor,
    ),
    screenTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.025).em,
        color = inkColor,
    ),
    sectionTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    ),
    cardTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        letterSpacing = (-0.01).em,
        color = inkColor,
    ),
    kpiPrice = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.03).em,
        color = inkColor,
    ),
    body = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        color = inkColor,
    ),
    bodyStrong = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        color = inkColor,
    ),
    caption = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        color = mutedColor,
    ),
    chipLabel = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        color = inkColor,
    ),
    tagLabel = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.02.em,
        color = inkColor,
    ),
)

public val LocalRfTypography: androidx.compose.runtime.ProvidableCompositionLocal<RfTypography> =
    staticCompositionLocalOf {
        buildRfTypography(
            display = FontFamily.Default,
            body = FontFamily.Default,
            inkColor = RfColors.PersonalInk,
            mutedColor = RfColors.PersonalMuted,
        )
    }
