package com.jeudry.rosafiesta.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pestañas de navegación de la App Personal (`TabBar`, Sección 1 y 3 de `entrega-de-diseno.md`):
 * - Modo cliente: `Inicio · Catálogo · Rosa · Eventos · Favoritos`
 * - Modo invitado: `Inicio · Catálogo · Rosa · Paquetes · Entrar`
 */
public enum class PersonalTab(
    public val key: String,
    public val label: String,
    public val icon: RfIconKind,
) {
    INICIO("inicio", "Inicio", RfIconKind.HOME),
    CATALOGO("catalogo", "Catálogo", RfIconKind.CATALOG),
    ROSA("rosa", "Rosa", RfIconKind.SPARK),
    EVENTOS("eventos", "Eventos", RfIconKind.CALENDAR),
    FAVORITOS("favoritos", "Favoritos", RfIconKind.HEART),
    PAQUETES("paquetes", "Paquetes", RfIconKind.ORDERS),
    ENTRAR("entrar", "Entrar", RfIconKind.USER),
    ;

    public companion object {
        public val HOME: PersonalTab = INICIO
        public val CATALOG: PersonalTab = CATALOGO
        public val ROSA_IA: PersonalTab = ROSA
        public val EVENTS: PersonalTab = EVENTOS
        public val FAVORITES: PersonalTab = FAVORITOS
        public val PROFILE: PersonalTab = FAVORITOS

        public val standardTabs: List<PersonalTab> = listOf(INICIO, CATALOGO, ROSA, EVENTOS, FAVORITOS)
        public val guestTabs: List<PersonalTab> = listOf(INICIO, CATALOGO, ROSA, PAQUETES, ENTRAR)
    }
}

/**
 * Componente `TabBar` de la App Personal (Sección 2.5 y 3):
 * - Alto 96 dp (`RfDimens.BottomBarHeight`), con `Rosa` de 56 dp en el centro sobresaliendo 26 dp (`RfDimens.RosaCenterProtrusion`).
 * - Pestaña activa: píldora con el degradado `hero` (`linear-gradient(135deg,#E04FAF 0%,#945AE1 55%,#6664E9 100%)`).
 * - Soporta modo invitado (`Inicio · Catálogo · Rosa · Paquetes · Entrar`) y modo oscuro (`dark`).
 */
@Composable
public fun PersonalTabBar(
    activeTab: PersonalTab,
    onSelectTab: (PersonalTab) -> Unit,
    guestMode: Boolean = false,
    dark: Boolean = LocalRfPalette.current.isDark,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val tabs = if (guestMode) PersonalTab.guestTabs else PersonalTab.standardTabs
    val barBg = if (dark) RfColors.PersonalDarkSurface else palette.surface.copy(alpha = 0.98f)
    val barLine = if (dark) RfColors.PersonalDarkLine else palette.line
    val inactiveColor = if (dark) RfColors.PersonalDarkMuted else palette.muted

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .height(RfDimens.BottomBarHeight),
    ) {
        // Superficie principal de la barra inferior (70 dp base + 26 dp de espacio superior para Rosa sobresaliendo)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .height(RfDimens.BottomBarHeight - RfDimens.RosaCenterProtrusion)
                .background(barBg)
                .border(1.dp, barLine)
                .padding(horizontal = 8.dp),
        ) {
            tabs.forEach { tab ->
                val selected = tab == activeTab
                if (tab == PersonalTab.ROSA) {
                    Spacer(Modifier.width(64.dp))
                } else {
                    val pillShape = RoundedCornerShape(999.dp)
                    val fgColor = if (selected) Color.White else inactiveColor
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(pillShape)
                            .then(
                                if (selected) {
                                    Modifier.background(RfColors.PersonalHeroGradient, pillShape)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        RfIcon(kind = tab.icon, tint = fgColor, size = 18.dp)
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            text = tab.label,
                            style = typo.caption.copy(
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = fgColor,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Botón central Rosa de 56 dp sobresaliendo 26 dp
        val rosaSelected = activeTab == PersonalTab.ROSA
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable { onSelectTab(PersonalTab.ROSA) },
        ) {
            RosaAvatar(
                size = RosaAvatarSize.TAB_CENTER,
                glow = rosaSelected,
                forceAdminPalette = false,
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = "Rosa",
                style = typo.caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rosaSelected) RfColors.RosaBrand else inactiveColor,
                ),
            )
        }
    }
}

/**
 * Pestañas de navegación de la App Administrativa (`AdminBar`, Sección 1 y 3 de `entrega-de-diseno.md`):
 * `Inicio · Eventos · Rosa · Inventario · Más`
 */
public enum class AdminTab(
    public val key: String,
    public val label: String,
    public val icon: RfIconKind,
    public val badgeCount: Int = 0,
) {
    INICIO("inicio", "Inicio", RfIconKind.HOME),
    EVENTOS("eventos", "Eventos", RfIconKind.CALENDAR, badgeCount = 2),
    ROSA("rosa", "Rosa", RfIconKind.SPARK),
    INVENTARIO("inventario", "Inventario", RfIconKind.CATALOG),
    MAS("mas", "Más", RfIconKind.SETTINGS),
    ;

    public companion object {
        public val DASHBOARD: AdminTab = INICIO
        public val AGENDA: AdminTab = EVENTOS
        public val ORDERS: AdminTab = EVENTOS
        public val CATALOG: AdminTab = INVENTARIO
        public val SETTINGS: AdminTab = MAS
    }
}

/**
 * Componente `AdminBar` de la App Administrativa (Sección 2.5 y 3):
 * - `Inicio · Eventos · Rosa · Inventario · Más`
 * - Alto 96 dp (`RfDimens.BottomBarHeight`), con `Rosa` de 56 dp en el centro usando `rosa-admin-bg` (índigo → turquesa) sobresaliendo 26 dp.
 * - Pestaña activa: píldora con `primary` (`linear-gradient(135deg,#7C5CF0 0%,#5B6CF0 100%)`).
 */
@Composable
public fun AdminTabBar(
    activeTab: AdminTab,
    onSelectTab: (AdminTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .height(RfDimens.BottomBarHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .height(RfDimens.BottomBarHeight - RfDimens.RosaCenterProtrusion)
                .background(palette.surface)
                .border(1.dp, RfColors.AdminLine)
                .padding(horizontal = 8.dp),
        ) {
            AdminTab.entries.forEach { tab ->
                val selected = tab == activeTab
                if (tab == AdminTab.ROSA) {
                    Spacer(Modifier.width(64.dp))
                } else {
                    val pillShape = RoundedCornerShape(999.dp)
                    val fgColor = if (selected) Color.White else RfColors.AdminMuted
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(pillShape)
                            .then(
                                if (selected) {
                                    Modifier.background(RfColors.AdminPrimaryGradient, pillShape)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Box {
                            RfIcon(kind = tab.icon, tint = fgColor, size = 18.dp)
                            if (tab.badgeCount > 0) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-4).dp)
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(RfColors.Sol),
                                ) {
                                    BasicText(
                                        text = tab.badgeCount.toString(),
                                        style = typo.tagLabel.copy(color = RfColors.SolInk, fontSize = 9.sp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            text = tab.label,
                            style = typo.caption.copy(
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = fgColor,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Botón central Rosa de 56 dp con rosa-admin-bg sobresaliendo 26 dp
        val rosaSelected = activeTab == AdminTab.ROSA
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable { onSelectTab(AdminTab.ROSA) },
        ) {
            RosaAvatar(
                size = RosaAvatarSize.TAB_CENTER,
                glow = rosaSelected,
                forceAdminPalette = true,
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = "Rosa",
                style = typo.caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rosaSelected) RfColors.IndigoBrand else RfColors.AdminMuted,
                ),
            )
        }
    }
}

@Composable
private fun Modifier.rfTactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.975f,
    onClick: () -> Unit,
): Modifier {
    if (!enabled) return this
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "rf-tactile-press",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
}

/**
 * Componente `Tarjeta` estándar (Sección 2.5): radio 20 dp, borde `#F2EDF5` (personal) o `#ECECF5` (admin).
 */
@Composable
public fun RfCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalRfPalette.current
    val shape = RoundedCornerShape(RfDimens.CardRadius)
    val clickableModifier = if (onClick != null) Modifier.rfTactileClickable(pressedScale = 0.985f, onClick = onClick) else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.surface)
            .border(1.dp, palette.line, shape)
            .then(clickableModifier)
            .padding(padding),
        content = content,
    )
}

/**
 * Componente `Tarjeta` hero con degradado de marca (`hero`, radio 20 dp).
 */
@Composable
public fun RfHeroCard(
    modifier: Modifier = Modifier,
    useAdminGradient: Boolean = LocalRfPalette.current.isAdmin,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(RfDimens.HeroCardRadius)
    val gradient = if (useAdminGradient) RfColors.AdminHeroAndRosaGradient else RfColors.PersonalHeroGradient
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .padding(16.dp),
        content = content,
    )
}

/**
 * Variantes de botón (Sección 2.2, 2.3 y 5).
 */
public enum class RfButtonStyle {
    PRIMARY,
    SECONDARY,
    SOL,
    DANGER,
}

/**
 * Botón de acción de RosaFiesta (Sección 2.5 y 5: alto 54 dp, radio 16 dp, texto 16/700;
 * deshabilitado al 35 % de opacidad en `s88`).
 */
@Composable
public fun RfButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: RfButtonStyle = RfButtonStyle.PRIMARY,
    fullWidth: Boolean = true,
    enabled: Boolean = true,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val shape = RoundedCornerShape(RfDimens.ButtonRadius)
    val widthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val alphaModifier = if (enabled) Modifier else Modifier.alpha(0.35f)

    val textColor = when (style) {
        RfButtonStyle.PRIMARY -> Color.White
        RfButtonStyle.SECONDARY -> palette.ink
        RfButtonStyle.SOL -> RfColors.SolInk
        RfButtonStyle.DANGER -> Color.White
    }

    val boxModifier = when (style) {
        RfButtonStyle.PRIMARY ->
            modifier
                .then(widthModifier)
                .then(alphaModifier)
                .height(RfDimens.ButtonHeight)
                .clip(shape)
                .background(palette.primaryGradient)

        RfButtonStyle.SECONDARY ->
            modifier
                .then(widthModifier)
                .then(alphaModifier)
                .height(RfDimens.ButtonHeight)
                .clip(shape)
                .background(palette.chipBg)
                .border(1.dp, palette.line, shape)

        RfButtonStyle.SOL ->
            modifier
                .then(widthModifier)
                .then(alphaModifier)
                .height(RfDimens.ButtonHeight)
                .clip(shape)
                .background(RfColors.Sol)

        RfButtonStyle.DANGER ->
            modifier
                .then(widthModifier)
                .then(alphaModifier)
                .height(RfDimens.ButtonHeight)
                .clip(shape)
                .background(RfColors.Error)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = boxModifier
            .rfTactileClickable(enabled = enabled, pressedScale = 0.97f, onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        BasicText(
            text = label,
            style = typo.buttonLabel.copy(color = textColor),
            maxLines = 1,
        )
    }
}

/**
 * Botón icono (`ib`, Sección 2.5 y 7.4): círculo de 44 dp, fondo blanco y `accessibilityLabel`.
 */
@Composable
public fun RfIconButton(
    icon: RfIconKind,
    accessibilityLabel: String = "",
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    badgeDot: Boolean = false,
) {
    val palette = LocalRfPalette.current
    val bg = if (palette.isDark) palette.surface else Color.White
    val resolvedLabel = accessibilityLabel.ifEmpty { contentDescription ?: icon.name }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(RfDimens.IconButtonSize)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, palette.line, CircleShape)
            .semantics { this.contentDescription = resolvedLabel }
            .rfTactileClickable(pressedScale = 0.92f, onClick = onClick),
    ) {
        RfIcon(kind = icon, tint = palette.ink, size = 20.dp)
        if (badgeDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RfColors.Sol),
            )
        }
    }
}

/**
 * Mosaico de icono (`ti`, Sección 2.5): 40 × 40 dp, radio 12 dp, fondo de color suave.
 */
@Composable
public fun RfIconTile(
    icon: RfIconKind,
    tone: RfTagTone = RfTagTone.ROSA,
    backgroundColor: Color? = null,
    iconTint: Color? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RfDimens.IconTileRadius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(RfDimens.IconTileSize)
            .clip(shape)
            .background(backgroundColor ?: tone.bg),
    ) {
        RfIcon(kind = icon, tint = iconTint ?: tone.fg, size = 20.dp)
    }
}

/**
 * Interruptor (`Sección 2.5`): 46 × 28 dp.
 */
@Composable
public fun RfSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    accessibilityLabel: String = "Interruptor",
) {
    val palette = LocalRfPalette.current
    val shape = RoundedCornerShape(999.dp)
    val activeBrush = if (palette.isAdmin) RfColors.AdminPrimaryGradient else RfColors.PersonalHeroGradient

    Box(
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = modifier
            .width(RfDimens.SwitchWidth)
            .height(RfDimens.SwitchHeight)
            .clip(shape)
            .then(
                if (checked) {
                    Modifier.background(activeBrush)
                } else {
                    Modifier.background(palette.chipBg).border(1.dp, palette.line, shape)
                },
            )
            .semantics { contentDescription = accessibilityLabel }
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/**
 * Texto con `texto-gradiente` (Sección 2.2: `linear-gradient(90deg,#EA4EA9,#865DEC)` recortado al texto para precios en Personal).
 */
@Composable
public fun RfGradientPriceText(
    text: String,
    style: TextStyle = LocalRfTypography.current.cardTitle,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val effectiveStyle = if (palette.isAdmin) {
        style.copy(color = RfColors.IndigoBrand)
    } else {
        style.copy(brush = RfColors.PersonalTextGradient)
    }
    BasicText(
        text = text,
        style = effectiveStyle,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * Componente `Chip` (Sección 2.5 y 3): alto 36 dp, radio completo; el chip activo usa `hero` (personal) o `primary` (admin).
 */
@Composable
public fun RfChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val shape = RoundedCornerShape(999.dp)
    val activeBrush = if (palette.isAdmin) RfColors.AdminPrimaryGradient else RfColors.PersonalHeroGradient
    val bgBrush = if (selected) activeBrush else null
    val bgColor = if (selected) Color.Transparent else palette.chipBg
    val textColor = if (selected) Color.White else palette.ink

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(RfDimens.ChipHeight)
            .clip(shape)
            .then(
                if (bgBrush != null) {
                    Modifier.background(bgBrush)
                } else {
                    Modifier.background(bgColor).border(1.dp, palette.line, shape)
                },
            )
            .rfTactileClickable(pressedScale = 0.95f, onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        BasicText(
            text = label,
            style = typo.chipLabel.copy(color = textColor),
            maxLines = 1,
        )
    }
}

/**
 * Tintes semánticos para el componente `Tag` y `RfIconTile` (Sección 2.1).
 */
public enum class RfTagTone(public val bg: Color, public val fg: Color) {
    ROSA(RfColors.RosaTint, RfColors.RosaTintInk),
    VIOLETA(RfColors.VioletaTint, RfColors.VioletaTintInk),
    SOL(RfColors.SolTint, RfColors.SolTintInk),
    LIMA(RfColors.LimaTint, RfColors.LimaTintInk),
    TEAL(RfColors.TealTint, RfColors.TealTintInk),
    INDIGO(RfColors.IndigoTint, RfColors.IndigoTintInk),
    ERROR(RfColors.ErrorTint, RfColors.Error),
}

/**
 * Componente `Tag` (Sección 2.5): alto 24 dp, radio completo, 12 / 700.
 */
@Composable
public fun RfTag(
    text: String,
    tone: RfTagTone = RfTagTone.ROSA,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(RfDimens.TagHeight)
            .clip(RoundedCornerShape(999.dp))
            .background(tone.bg)
            .padding(horizontal = 10.dp),
    ) {
        BasicText(
            text = text,
            style = typo.tagLabel.copy(color = tone.fg),
            maxLines = 1,
        )
    }
}

/**
 * Componente `Campo` (Sección 2.4 y 2.5): etiqueta `lbl` 13/700, alto 51 dp (50–52) y radio 15 dp (14–16).
 */
@Composable
public fun RfField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    helperOrError: String? = null,
    isError: Boolean = false,
    placeholder: String = "",
    onValueChange: ((String) -> Unit)? = null,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val borderColor = if (isError) RfColors.Error else palette.fieldBorder
    val shape = RoundedCornerShape(RfDimens.InputRadius)

    Column(modifier = modifier.fillMaxWidth()) {
        BasicText(
            text = label,
            style = typo.fieldLabel.copy(color = palette.ink),
        )
        Spacer(Modifier.height(5.dp))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(RfDimens.FieldHeight)
                .clip(shape)
                .background(palette.surface)
                .border(1.2.dp, borderColor, shape)
                .padding(horizontal = 14.dp),
        ) {
            if (onValueChange != null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = typo.body.copy(color = palette.ink),
                    cursorBrush = SolidColor(palette.primaryAccent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (value.isEmpty() && placeholder.isNotEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    style = typo.body.copy(color = palette.muted),
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            } else {
                val displayText = if (value.isEmpty() && placeholder.isNotEmpty()) placeholder else value
                val displayColor = if (value.isEmpty() && placeholder.isNotEmpty()) palette.muted else palette.ink
                BasicText(
                    text = displayText,
                    style = typo.body.copy(color = displayColor),
                    maxLines = 1,
                )
            }
        }
        if (!helperOrError.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = helperOrError,
                style = typo.secondary.copy(
                    color = if (isError) RfColors.Error else palette.muted,
                ),
            )
        }
    }
}

/**
 * Componente `Segmentado` (Sección 2.3 y 3): control de selección sobre `chip` / `segmentado`.
 */
@Composable
public fun RfSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val outerShape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(palette.chipBg)
            .padding(4.dp),
    ) {
        options.forEachIndexed { idx, label ->
            val active = idx == selectedIndex
            val itemShape = RoundedCornerShape(10.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .background(if (active) palette.surface else Color.Transparent)
                    .clickable { onSelect(idx) }
                    .padding(vertical = 7.dp, horizontal = 6.dp),
            ) {
                BasicText(
                    text = label,
                    style = typo.chipLabel.copy(
                        color = if (active) palette.ink else palette.muted,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Componente `Toast` (Sección 2.1, 3 y 5):
 * Fondo `toast` (`#1A1A2E` personal / `#151729` admin), círculo `lima` (`#B9D84A`) con ✓ y acción en `#FFD45C`.
 * En personal va sobre la barra inferior (`s92`); en admin va arriba (`A38`).
 */
@Composable
public fun RfToast(
    message: String,
    actionLabel: String? = "Ver",
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val bg = if (isAdmin) RfColors.AdminInk else palette.toastBg

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(RfColors.Lima),
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = RfColors.PersonalInk, size = 13.dp, strokeWidthFactor = 0.14f)
            }
            Spacer(Modifier.width(10.dp))
            BasicText(
                text = message,
                style = typo.bodyStrong.copy(color = Color.White, fontSize = 13.5.sp),
                maxLines = 1,
            )
        }
        if (actionLabel != null) {
            Spacer(Modifier.width(10.dp))
            BasicText(
                text = actionLabel,
                style = typo.tagLabel.copy(color = RfColors.Sol),
                maxLines = 1,
                modifier = if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier,
            )
        }
    }
}

/**
 * Componente `Banner sin conexión` (Sección 2.1, 3 y 5):
 * Franja oscura sobre la barra de estado con icono wifi tachado, "Reintentar" y guardado local (`s91`, `A37`).
 */
@Composable
public fun RfOfflineBanner(
    pendingChangesCount: Int = 3,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.toastBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            RfIcon(kind = RfIconKind.WIFI_OFF, tint = RfColors.Sol, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = if (isAdmin) {
                    "Sin conexión · $pendingChangesCount cambios locales"
                } else {
                    "Sin conexión · Guardado local"
                },
                style = typo.secondary.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        BasicText(
            text = "Reintentar",
            style = typo.tagLabel.copy(color = RfColors.Sol),
            maxLines = 1,
            modifier = if (onRetry != null) Modifier.clickable(onClick = onRetry) else Modifier,
        )
    }
}

/**
 * Componente `Hoja inferior` (`RfBottomSheet`, Sección 2.5 y 7.6):
 * - Radio 28 dp arriba (`RfDimens.BottomSheetRadius`), asa de 40 × 5 dp, fondo atenuado `rgba(30,20,50,.45)` (`RfColors.SheetScrim`).
 * - Se cierra deslizando hacia abajo o tocando el fondo atenuado.
 */
@Composable
public fun RfBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val sheetShape = RoundedCornerShape(
        topStart = RfDimens.BottomSheetRadius,
        topEnd = RfDimens.BottomSheetRadius,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(RfColors.SheetScrim),
    ) {
        // Zona superior atenuada: tocar el fondo cierra la hoja (Sección 7.6)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clickable(onClick = onDismiss),
        ) {
            BasicText(
                text = "Toca el fondo o desliza hacia abajo para cerrar",
                style = typo.secondary.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 11.5.sp),
            )
        }

        // Cuerpo de la hoja inferior con radio superior 28 dp y asa de 40 × 5 dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(sheetShape)
                .background(palette.surface)
                .pointerInput(onDismiss) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 16f) {
                            onDismiss()
                        }
                    }
                }
                .padding(horizontal = RfDimens.ScreenHorizontalPadding, vertical = 14.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(RfDimens.BottomSheetHandleWidth)
                    .height(RfDimens.BottomSheetHandleHeight)
                    .clip(RoundedCornerShape(999.dp))
                    .background(palette.line),
            )
            Spacer(Modifier.height(12.dp))
            BasicText(
                text = title,
                style = typo.sheetOrEmptyTitle,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = subtitle,
                    style = typo.secondary,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Componente `Esqueleto` de carga (`s90`, `A36`, Sección 5):
 * Misma estructura que la pantalla y un brillo de izquierda a derecha de 1.2 s (`1200` ms).
 * Nada de spinners a pantalla completa.
 */
@Composable
public fun RfSkeleton(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
) {
    val transition = rememberInfiniteTransition(label = "rf-skeleton-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-x",
    )

    val baseColor = if (isAdmin) Color(0xFFE7E8F3) else Color(0xFFEEEAF4)
    val highlightColor = Color.White.copy(alpha = 0.85f)
    val offsetStart = (progress * 500f) - 180f
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(offsetStart, 0f),
        end = Offset(offsetStart + 200f, 40f),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(RfDimens.BlockSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        // Esqueleto de tarjeta hero / resumen superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(RfDimens.CardRadius))
                .background(shimmerBrush),
        )
        // Chips de filtro en esqueleto
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(RfDimens.ChipHeight)
                        .clip(RoundedCornerShape(999.dp))
                        .background(shimmerBrush),
                )
            }
        }
        // 3 tarjetas con la misma estructura de la pantalla
        repeat(3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(RfDimens.CardRadius))
                    .background(Color.White)
                    .border(1.dp, baseColor, RoundedCornerShape(RfDimens.CardRadius))
                    .padding(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(shimmerBrush),
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerBrush),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerBrush),
                    )
                }
            }
        }
    }
}

/**
 * Alias de [RfSkeleton] para pantallas de estado de carga (`s90`, `A36`).
 */
@Composable
public fun RfSkeletonScreen(
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    modifier: Modifier = Modifier,
) {
    RfSkeleton(modifier = modifier, isAdmin = isAdmin)
}

/**
 * Fondo con los dos degradados radiales superiores definidos en la Sección 2.2 (Personal) y 2.3 (Admin).
 */
@Composable
public fun RfRadialBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = LocalRfPalette.current
    Box(modifier = modifier.background(palette.bg)) {
        if (!palette.isDark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (palette.isAdmin) {
                    // Admin (2.3): radial-gradient(110% 40% at 100% 0%,#E6E7FF 0%,transparent 60%), radial-gradient(80% 35% at 0% 12%,#F1E9FB 0%,transparent 60%)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.radialPrimary, Color.Transparent),
                            center = Offset(w, 0f),
                            radius = w * 0.78f,
                        ),
                        radius = w * 0.78f,
                        center = Offset(w, 0f),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.radialSecondary, Color.Transparent),
                            center = Offset(0f, h * 0.12f),
                            radius = w * 0.65f,
                        ),
                        radius = w * 0.65f,
                        center = Offset(0f, h * 0.12f),
                    )
                } else {
                    // Personal (2.2): radial-gradient(110% 40% at 0% 0%,#F9E3F1 0%,transparent 60%), radial-gradient(80% 35% at 100% 15%,#EEE7FB 0%,transparent 60%)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.radialPrimary, Color.Transparent),
                            center = Offset.Zero,
                            radius = w * 0.78f,
                        ),
                        radius = w * 0.78f,
                        center = Offset.Zero,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.radialSecondary, Color.Transparent),
                            center = Offset(w, h * 0.15f),
                            radius = w * 0.65f,
                        ),
                        radius = w * 0.65f,
                        center = Offset(w, h * 0.15f),
                    )
                }
            }
        }
        content()
    }
}

/**
 * Marco móvil oficial (Sección 1 y 2.5: 390 × 844 dp, márgenes laterales de 20 dp, contenido desde `y = 54 dp` debajo de la barra de estado).
 */
@Composable
public fun RfPhoneFrame(
    variant: RfThemeVariant = RfThemeVariant.PERSONAL,
    screenCode: String = "",
    screenTitle: String = "",
    bottomBar: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RfTheme(variant = variant) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current

        RfRadialBackgroundBox(
            modifier = modifier
                .width(RfDimens.PhoneFrameWidth)
                .height(RfDimens.PhoneFrameHeight)
                .clip(RoundedCornerShape(RfDimens.PhoneInnerRadius))
                .border(1.5.dp, palette.line, RoundedCornerShape(RfDimens.PhoneInnerRadius)),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de estado de 54 dp de alto (contenido empieza en y = 54 dp, Sección 2.5)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RfDimens.StatusBarHeight)
                        .padding(horizontal = RfDimens.ScreenHorizontalPadding),
                ) {
                    BasicText(
                        text = "9:41",
                        style = typo.fieldLabel.copy(color = palette.ink),
                    )
                    if (screenCode.isNotEmpty()) {
                        RfTag(
                            text = "$screenCode · $screenTitle".trimEnd(' ', '·'),
                            tone = if (palette.isAdmin) RfTagTone.INDIGO else RfTagTone.ROSA,
                        )
                    }
                    BasicText(
                        text = "5G · 100%",
                        style = typo.secondary.copy(color = palette.ink, fontWeight = FontWeight.SemiBold),
                    )
                }

                // Cuerpo con márgenes laterales de 20 dp (Sección 2.5)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = RfDimens.ScreenHorizontalPadding),
                    content = content,
                )

                if (bottomBar != null) {
                    bottomBar()
                }
            }
        }
    }
}
