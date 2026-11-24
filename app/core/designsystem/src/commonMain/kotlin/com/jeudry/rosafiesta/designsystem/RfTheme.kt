package com.jeudry.rosafiesta.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

/**
 * Variante visual activa: App Personal clara (`s14`), App Personal oscura (`s15`),
 * App Administrativa clara (`theme.admin`) o App Administrativa oscura.
 */
public enum class RfThemeVariant {
    PERSONAL,
    PERSONAL_DARK,
    ADMIN,
    ADMIN_DARK,
}

/**
 * Paleta resuelta para la variante activa (`theme.personal` vs `theme.admin`, Sección 2 de `entrega-de-diseno.md`).
 */
@Immutable
public data class RfPalette(
    public val variant: RfThemeVariant,
    public val ink: Color,
    public val muted: Color,
    public val bg: Color,
    public val radialPrimary: Color,
    public val radialSecondary: Color,
    public val surface: Color,
    public val line: Color,
    public val lineSoft: Color,
    public val fieldBorder: Color,
    public val chipBg: Color,
    public val primaryAccent: Color,
    public val primaryGradient: Brush,
    public val heroGradient: Brush,
    public val priceTextGradient: Brush,
    public val rosaAvatarGradient: Brush,
    public val rosaAssistantBgGradient: Brush,
    public val toastBg: Color,
    public val cardShadow: Color,
) {
    /**
     * Indica si la paleta pertenece a la App Administrativa (clara u oscura).
     */
    public val isAdmin: Boolean
        get() = when (variant) {
            RfThemeVariant.ADMIN, RfThemeVariant.ADMIN_DARK -> true
            RfThemeVariant.PERSONAL, RfThemeVariant.PERSONAL_DARK -> false
        }

    /**
     * Indica si la paleta corresponde a modo oscuro (Personal o Admin).
     */
    public val isDark: Boolean
        get() = when (variant) {
            RfThemeVariant.PERSONAL_DARK, RfThemeVariant.ADMIN_DARK -> true
            RfThemeVariant.PERSONAL, RfThemeVariant.ADMIN -> false
        }
}

/**
 * Paleta resuelta para la variante activa (`theme.personal` vs `theme.admin`, Sección 2 de `entrega-de-diseno.md`).
 */
public fun paletteForVariant(variant: RfThemeVariant): RfPalette = when (variant) {
    RfThemeVariant.PERSONAL -> RfPalette(
        variant = RfThemeVariant.PERSONAL,
        ink = RfColors.PersonalInk,
        muted = RfColors.PersonalMuted,
        bg = RfColors.PersonalBg,
        radialPrimary = RfColors.PersonalRadialRose,
        radialSecondary = RfColors.PersonalRadialViolet,
        surface = Color.White,
        line = RfColors.PersonalLine,
        lineSoft = RfColors.PersonalLineSoft,
        fieldBorder = RfColors.PersonalLine,
        chipBg = RfColors.PersonalChipBg,
        primaryAccent = RfColors.RosaBrand,
        primaryGradient = RfColors.PersonalPrimaryGradient,
        heroGradient = RfColors.PersonalHeroGradient,
        priceTextGradient = RfColors.PersonalTextGradient,
        rosaAvatarGradient = RfColors.PersonalHeroGradient,
        rosaAssistantBgGradient = RfColors.PersonalAssistantBgGradient,
        toastBg = RfColors.ToastPersonal,
        cardShadow = RfColors.PersonalCardShadow,
    )

    RfThemeVariant.PERSONAL_DARK -> RfPalette(
        variant = RfThemeVariant.PERSONAL_DARK,
        ink = RfColors.PersonalDarkInk,
        muted = RfColors.PersonalDarkMuted,
        bg = RfColors.PersonalDarkBg,
        radialPrimary = Color(0xFF2B1C38),
        radialSecondary = Color(0xFF1E1B3A),
        surface = RfColors.PersonalDarkSurface,
        line = RfColors.PersonalDarkLine,
        lineSoft = RfColors.PersonalDarkLine,
        fieldBorder = RfColors.PersonalDarkLine,
        chipBg = RfColors.PersonalDarkChipBg,
        primaryAccent = RfColors.RosaBrand,
        primaryGradient = RfColors.PersonalPrimaryGradient,
        heroGradient = RfColors.PersonalHeroGradient,
        priceTextGradient = RfColors.PersonalTextGradient,
        rosaAvatarGradient = RfColors.PersonalHeroGradient,
        rosaAssistantBgGradient = Brush.verticalGradient(
            listOf(Color(0xFF1E1A36), Color(0xFF151428)),
        ),
        toastBg = Color(0xFF282644),
        cardShadow = Color(0x40000000),
    )

    RfThemeVariant.ADMIN -> RfPalette(
        variant = RfThemeVariant.ADMIN,
        ink = RfColors.AdminInk,
        muted = RfColors.AdminMuted,
        bg = RfColors.AdminBg,
        radialPrimary = RfColors.AdminRadialIndigo,
        radialSecondary = RfColors.AdminRadialLavender,
        surface = Color.White,
        line = RfColors.AdminLine,
        lineSoft = RfColors.AdminLineSoft,
        fieldBorder = RfColors.AdminLineStrong,
        chipBg = RfColors.AdminChipBg,
        primaryAccent = RfColors.IndigoBrand,
        primaryGradient = RfColors.AdminPrimaryGradient,
        heroGradient = RfColors.AdminHeroAndRosaGradient,
        priceTextGradient = RfColors.AdminPrimaryGradient,
        rosaAvatarGradient = RfColors.AdminHeroAndRosaGradient,
        rosaAssistantBgGradient = RfColors.AdminAssistantBgGradient,
        toastBg = RfColors.ToastAdmin,
        cardShadow = RfColors.AdminCardShadow,
    )

    RfThemeVariant.ADMIN_DARK -> RfPalette(
        variant = RfThemeVariant.ADMIN_DARK,
        ink = RfColors.AdminDarkInk,
        muted = RfColors.AdminDarkMuted,
        bg = RfColors.AdminDarkBg,
        radialPrimary = Color(0xFF202247),
        radialSecondary = Color(0xFF18223D),
        surface = RfColors.AdminDarkSurface,
        line = RfColors.AdminDarkLine,
        lineSoft = RfColors.AdminDarkLine,
        fieldBorder = RfColors.AdminDarkLineStrong,
        chipBg = RfColors.AdminDarkChipBg,
        primaryAccent = RfColors.IndigoBrand,
        primaryGradient = RfColors.AdminPrimaryGradient,
        heroGradient = RfColors.AdminHeroAndRosaGradient,
        priceTextGradient = RfColors.AdminPrimaryGradient,
        rosaAvatarGradient = RfColors.AdminHeroAndRosaGradient,
        rosaAssistantBgGradient = Brush.verticalGradient(
            listOf(Color(0xFF1A1C3A), Color(0xFF121426)),
        ),
        toastBg = Color(0xFF222742),
        cardShadow = Color(0x40000000),
    )
}

public val LocalRfPalette: androidx.compose.runtime.ProvidableCompositionLocal<RfPalette> =
    staticCompositionLocalOf { paletteForVariant(RfThemeVariant.PERSONAL) }

/**
 * Estado reactivo centralizado del tema Personal (Claro `s14` vs Oscuro `s15`),
 * accesible tanto desde funciones `@Composable` como desde bloques `DrawScope` (`Canvas` / `drawBehind`).
 */
public object RfThemeState {
    public var isPersonalDark: Boolean by mutableStateOf(false)
    public var activeVariant: RfThemeVariant by mutableStateOf(RfThemeVariant.PERSONAL)

    public val currentPalette: RfPalette
        get() = paletteForVariant(activeVariant)
}

/**
 * Indicación táctil contextual para todos los elementos interactivos de RosaFiesta.
 * Elimina el recuadro gris rectangular por defecto de Compose y aplica un efecto elástico suave
 * de escala alrededor del centro del propio elemento (respetando círculos, píldoras, tarjetas e iconos).
 */
private object RfContextualIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = RfContextualIndicationNode(interactionSource)

    override fun hashCode(): Int = -1948271635

    override fun equals(other: Any?): Boolean = other === this
}

private class RfContextualIndicationNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(),
    DrawModifierNode {
    private val scale = Animatable(1f)

    override fun onAttach() {
        coroutineScope.launch {
            var isPressed = false
            var isHovered = false
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
                    is HoverInteraction.Enter -> isHovered = true
                    is HoverInteraction.Exit -> isHovered = false
                }
                val targetScale = when {
                    isPressed -> 0.955f
                    isHovered -> 1.025f
                    else -> 1f
                }
                launch {
                    scale.animateTo(
                        targetValue = targetScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val currentScale = scale.value
        if (currentScale != 1f) {
            withTransform({
                scale(scaleX = currentScale, scaleY = currentScale, pivot = center)
            }) {
                this@draw.drawContent()
            }
        } else {
            drawContent()
        }
    }
}

/**
 * Proveedor raíz del sistema de diseño de RosaFiesta.
 */
@Composable
public fun RfTheme(
    variant: RfThemeVariant = RfThemeVariant.PERSONAL,
    content: @Composable () -> Unit,
) {
    val effectiveVariant = when (variant) {
        RfThemeVariant.PERSONAL -> {
            if (RfThemeState.isPersonalDark) RfThemeVariant.PERSONAL_DARK else RfThemeVariant.PERSONAL
        }

        RfThemeVariant.ADMIN -> {
            if (RfThemeState.isPersonalDark) RfThemeVariant.ADMIN_DARK else RfThemeVariant.ADMIN
        }

        RfThemeVariant.PERSONAL_DARK, RfThemeVariant.ADMIN_DARK -> variant
    }
    RfThemeState.activeVariant = effectiveVariant
    val palette = paletteForVariant(effectiveVariant)
    val typography = rememberRfTypography(palette.ink, palette.muted)
    CompositionLocalProvider(
        LocalRfPalette provides palette,
        LocalRfTypography provides typography,
        LocalIndication provides RfContextualIndication,
        content = content,
    )
}
