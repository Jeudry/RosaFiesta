package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color exactos de RosaFiesta (Sección 3.1 de `entrega-de-diseno.md`).
 */
public object RfColors {
    public val Sol: Color = Color(0xFFFFD45C)
    public val SolInk: Color = Color(0xFF2E1A47)
    public val SolTint: Color = Color(0xFFFFF2C9)
    public val SolTintInk: Color = Color(0xFF7A5700)

    public val Lima: Color = Color(0xFFB9D84A)
    public val LimaTint: Color = Color(0xFFEEF6D6)
    public val LimaTintInk: Color = Color(0xFF4E6A10)

    public val Teal: Color = Color(0xFF2FB8C8)
    public val TealTint: Color = Color(0xFFDDF4F3)
    public val TealTintInk: Color = Color(0xFF1F8A84)

    public val RosaBrand: Color = Color(0xFFEA4EA9)
    public val RosaTint: Color = Color(0xFFFDE6F2)
    public val RosaTintInk: Color = Color(0xFFC2338F)

    public val VioletaBrand: Color = Color(0xFF865DEC)
    public val VioletaTint: Color = Color(0xFFEFE8FD)
    public val VioletaTintInk: Color = Color(0xFF7A4FD8)

    public val IndigoBrand: Color = Color(0xFF7C5CF0)
    public val IndigoSecondary: Color = Color(0xFF5B6CF0)
    public val IndigoTint: Color = Color(0xFFECEBFF)
    public val IndigoTintInk: Color = Color(0xFF4B47D6)

    public val Error: Color = Color(0xFFB42345)
    public val ErrorTint: Color = Color(0xFFFCE3E8)
    public val Scrim: Color = Color(0x61141223)

    public val PersonalInk: Color = Color(0xFF1A1A2E)
    public val PersonalMuted: Color = Color(0xFF62627A)
    public val PersonalBg: Color = Color(0xFFFAFAFC)
    public val PersonalRadialRose: Color = Color(0xFFF9E3F1)
    public val PersonalRadialViolet: Color = Color(0xFFEEE7FB)
    public val PersonalLine: Color = Color(0xFFF2EDF5)
    public val PersonalLineSoft: Color = Color(0xFFF2F0F5)
    public val PersonalChipBg: Color = Color(0xFFF1EFF5)

    public val PersonalPrimaryGradient: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
    )
    public val PersonalHeroGradient: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
    )
    public val PersonalAssistantBgGradient: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFDED5F7), Color(0xFFF2EAF7), Color(0xFFF5EEF7), Color(0xFFD9D0F2)),
    )

    public val PersonalDarkInk: Color = Color(0xFFF7F5FC)
    public val PersonalDarkMuted: Color = Color(0xFFA7A5C2)
    public val PersonalDarkBg: Color = Color(0xFF121222)
    public val PersonalDarkSurface: Color = Color(0xFF1D1D33)
    public val PersonalDarkLine: Color = Color(0xFF2E2C4A)
    public val PersonalDarkChipBg: Color = Color(0xFF282642)

    public val AdminInk: Color = Color(0xFF151729)
    public val AdminMuted: Color = Color(0xFF5F6377)
    public val AdminBg: Color = Color(0xFFF6F6FB)
    public val AdminRadialIndigo: Color = Color(0xFFE6E7FF)
    public val AdminRadialLavender: Color = Color(0xFFF1E9FB)
    public val AdminLine: Color = Color(0xFFECECF5)
    public val AdminLineStrong: Color = Color(0xFFE3E4EF)
    public val AdminLineSoft: Color = Color(0xFFF0F0F6)
    public val AdminChipBg: Color = Color(0xFFF2F2F8)

    public val AdminPrimaryGradient: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
    )
    public val AdminHeroAndRosaGradient: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
    )
    public val AdminAssistantBgGradient: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3E1FB), Color(0xFFF1EEFA), Color(0xFFF4F1FA), Color(0xFFDCDDF8)),
    )
}
