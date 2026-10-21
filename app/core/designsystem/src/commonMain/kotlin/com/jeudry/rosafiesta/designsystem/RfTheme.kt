package com.jeudry.rosafiesta.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Variante visual activa: App Personal clara, App Personal oscura (`s15`) o App Administrativa (`theme.admin`).
 */
public enum class RfThemeVariant {
    PERSONAL,
    PERSONAL_DARK,
    ADMIN,
}

/**
 * Paleta resuelta para la variante activa (`theme.personal` vs `theme.admin`).
 */
@Immutable
public data class RfPalette(
    public val variant: RfThemeVariant,
    public val ink: Color,
    public val muted: Color,
    public val bg: Color,
    public val surface: Color,
    public val line: Color,
    public val chipBg: Color,
    public val primaryAccent: Color,
    public val primaryGradient: Brush,
    public val heroGradient: Brush,
    public val rosaAvatarGradient: Brush,
    public val rosaAssistantBgGradient: Brush,
    public val toastBg: Color,
) {
    public val isAdmin: Boolean
        get() = variant == RfThemeVariant.ADMIN

    public val isDark: Boolean
        get() = variant == RfThemeVariant.PERSONAL_DARK
}

public fun paletteForVariant(variant: RfThemeVariant): RfPalette = when (variant) {
    RfThemeVariant.PERSONAL -> RfPalette(
        variant = RfThemeVariant.PERSONAL,
        ink = RfColors.PersonalInk,
        muted = RfColors.PersonalMuted,
        bg = RfColors.PersonalBg,
        surface = Color.White,
        line = RfColors.PersonalLine,
        chipBg = RfColors.PersonalChipBg,
        primaryAccent = RfColors.RosaBrand,
        primaryGradient = RfColors.PersonalPrimaryGradient,
        heroGradient = RfColors.PersonalHeroGradient,
        rosaAvatarGradient = RfColors.PersonalHeroGradient,
        rosaAssistantBgGradient = RfColors.PersonalAssistantBgGradient,
        toastBg = RfColors.PersonalInk,
    )

    RfThemeVariant.PERSONAL_DARK -> RfPalette(
        variant = RfThemeVariant.PERSONAL_DARK,
        ink = RfColors.PersonalDarkInk,
        muted = RfColors.PersonalDarkMuted,
        bg = RfColors.PersonalDarkBg,
        surface = RfColors.PersonalDarkSurface,
        line = RfColors.PersonalDarkLine,
        chipBg = RfColors.PersonalDarkChipBg,
        primaryAccent = RfColors.RosaBrand,
        primaryGradient = RfColors.PersonalPrimaryGradient,
        heroGradient = RfColors.PersonalHeroGradient,
        rosaAvatarGradient = RfColors.PersonalHeroGradient,
        rosaAssistantBgGradient = Brush.verticalGradient(
            listOf(Color(0xFF1E1A36), Color(0xFF151428)),
        ),
        toastBg = Color(0xFF282644),
    )

    RfThemeVariant.ADMIN -> RfPalette(
        variant = RfThemeVariant.ADMIN,
        ink = RfColors.AdminInk,
        muted = RfColors.AdminMuted,
        bg = RfColors.AdminBg,
        surface = Color.White,
        line = RfColors.AdminLine,
        chipBg = RfColors.AdminChipBg,
        primaryAccent = RfColors.IndigoBrand,
        primaryGradient = RfColors.AdminPrimaryGradient,
        heroGradient = RfColors.AdminHeroAndRosaGradient,
        rosaAvatarGradient = RfColors.AdminHeroAndRosaGradient,
        rosaAssistantBgGradient = RfColors.AdminAssistantBgGradient,
        toastBg = RfColors.AdminInk,
    )
}

public val LocalRfPalette: androidx.compose.runtime.ProvidableCompositionLocal<RfPalette> =
    staticCompositionLocalOf { paletteForVariant(RfThemeVariant.PERSONAL) }

/**
 * Proveedor raíz del sistema de diseño de RosaFiesta.
 */
@Composable
public fun RfTheme(
    variant: RfThemeVariant = RfThemeVariant.PERSONAL,
    content: @Composable () -> Unit,
) {
    val palette = paletteForVariant(variant)
    val typography = rememberRfTypography(palette.ink, palette.muted)
    CompositionLocalProvider(
        LocalRfPalette provides palette,
        LocalRfTypography provides typography,
        content = content,
    )
}
