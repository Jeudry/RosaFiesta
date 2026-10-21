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
 * Escala tipográfica oficial de RosaFiesta (Sección 2.4 de `entrega-de-diseno.md`):
 * - Bricolage Grotesque (500–800) con `letter-spacing: -0.02em`: títulos, montos y KPIs.
 * - Figtree (400–700): todo el texto.
 */
@Immutable
public data class RfTypography(
    public val displayFamily: FontFamily,
    public val bodyFamily: FontFamily,
    /** Título de pestaña raíz (Catálogo, Mis eventos): 26 / 800 */
    public val rootTabTitle: TextStyle,
    /** Título de pantalla con “volver”: 20 / 800 */
    public val backScreenTitle: TextStyle,
    /** Título de diálogo u hoja, estado vacío: 22 / 800 */
    public val sheetOrEmptyTitle: TextStyle,
    /** Monto grande (recibo, reembolso): 32–34 / 800 */
    public val bigAmount: TextStyle,
    /** KPI (web): 30 / 800 */
    public val webKpi: TextStyle,
    /** Cuerpo: 15 / 400 */
    public val body: TextStyle,
    /** Cuerpo seminegrita: 15 / 600 */
    public val bodyStrong: TextStyle,
    /** Secundario: 13–14 / 400 en `muted` */
    public val secondary: TextStyle,
    /** Etiqueta de sección (`cap`): 12 / 700, mayúsculas, `letter-spacing: .08em` */
    public val sectionCap: TextStyle,
    /** Etiqueta de campo (`lbl`): 13 / 700 */
    public val fieldLabel: TextStyle,
    /** Texto de botón: 16 / 700 */
    public val buttonLabel: TextStyle,
    // Alias semánticos usados en tarjetas y encabezados
    public val heroTitle: TextStyle,
    public val screenTitle: TextStyle,
    public val sectionTitle: TextStyle,
    public val cardTitle: TextStyle,
    public val kpiPrice: TextStyle,
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

public fun buildRfTypography(
    display: FontFamily,
    body: FontFamily,
    inkColor: Color,
    mutedColor: Color,
): RfTypography {
    val rootTabTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    )
    val backScreenTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    )
    val sheetOrEmptyTitle = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    )
    val bigAmount = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    )
    val webKpi = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em,
        color = inkColor,
    )
    val bodyStyle = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = inkColor,
    )
    val bodyStrongStyle = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = inkColor,
    )
    val secondaryStyle = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
        color = mutedColor,
    )
    val sectionCap = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
        color = mutedColor,
    )
    val fieldLabel = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        color = inkColor,
    )
    val buttonLabel = TextStyle(
        fontFamily = body,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = Color.White,
    )

    return RfTypography(
        displayFamily = display,
        bodyFamily = body,
        rootTabTitle = rootTabTitle,
        backScreenTitle = backScreenTitle,
        sheetOrEmptyTitle = sheetOrEmptyTitle,
        bigAmount = bigAmount,
        webKpi = webKpi,
        body = bodyStyle,
        bodyStrong = bodyStrongStyle,
        secondary = secondaryStyle,
        sectionCap = sectionCap,
        fieldLabel = fieldLabel,
        buttonLabel = buttonLabel,
        heroTitle = rootTabTitle,
        screenTitle = backScreenTitle,
        sectionTitle = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.02).em,
            color = inkColor,
        ),
        cardTitle = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.02).em,
            color = inkColor,
        ),
        kpiPrice = webKpi,
        caption = secondaryStyle,
        chipLabel = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            color = inkColor,
        ),
        tagLabel = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = inkColor,
        ),
    )
}

public val LocalRfTypography: androidx.compose.runtime.ProvidableCompositionLocal<RfTypography> =
    staticCompositionLocalOf {
        buildRfTypography(
            display = FontFamily.Default,
            body = FontFamily.Default,
            inkColor = RfColors.PersonalInk,
            mutedColor = RfColors.PersonalMuted,
        )
    }
