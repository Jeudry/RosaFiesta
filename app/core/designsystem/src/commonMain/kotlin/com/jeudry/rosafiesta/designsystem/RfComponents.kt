package com.jeudry.rosafiesta.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pestañas de navegación de la App Personal (`TabBar`, Sección 4.3).
 */
public enum class PersonalTab(public val label: String, public val icon: RfIconKind) {
    HOME("Inicio", RfIconKind.HOME),
    CATALOG("Catálogo", RfIconKind.CATALOG),
    ROSA_IA("Rosa IA", RfIconKind.SPARK),
    EVENTS("Eventos", RfIconKind.CALENDAR),
    PROFILE("Perfil", RfIconKind.USER),
}

/**
 * Componente `TabBar` de la App Personal (Sección 4.3):
 * 5 ítems con botón central elevado `Rosa IA` de 56 dp.
 */
@Composable
public fun PersonalTabBar(
    activeTab: PersonalTab,
    onSelectTab: (PersonalTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface.copy(alpha = 0.96f))
            .border(1.dp, palette.line)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        PersonalTab.entries.forEach { tab ->
            val selected = tab == activeTab
            if (tab == PersonalTab.ROSA_IA) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = (-10).dp)
                        .clickable { onSelectTab(tab) },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(RfColors.PersonalHeroGradient)
                            .border(2.dp, palette.surface, CircleShape),
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 24.dp)
                    }
                    Spacer(Modifier.height(2.dp))
                    BasicText(
                        text = tab.label,
                        style = typo.caption.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) RfColors.RosaBrand else palette.muted,
                        ),
                    )
                }
            } else {
                val color = if (selected) RfColors.RosaBrand else palette.muted
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    RfIcon(kind = tab.icon, tint = color, size = 20.dp)
                    Spacer(Modifier.height(3.dp))
                    BasicText(
                        text = tab.label,
                        style = typo.caption.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = color,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Pestañas de navegación de la App Administrativa (`AdminBar`, Sección 4.4).
 */
public enum class AdminTab(public val label: String, public val icon: RfIconKind, public val badgeCount: Int = 0) {
    DASHBOARD("Panel", RfIconKind.HOME),
    AGENDA("Agenda", RfIconKind.CALENDAR),
    ORDERS("Pedidos", RfIconKind.ORDERS, badgeCount = 2),
    CATALOG("Catálogo", RfIconKind.CATALOG),
    SETTINGS("Ajustes", RfIconKind.SETTINGS),
}

/**
 * Componente `AdminBar` de la App Administrativa (Sección 4.4):
 * 5 pestañas uniformes con indicador activo `#7C5CF0` y badge en `Pedidos`.
 */
@Composable
public fun AdminTabBar(
    activeTab: AdminTab,
    onSelectTab: (AdminTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface)
            .border(1.dp, RfColors.AdminLine)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        AdminTab.entries.forEach { tab ->
            val selected = tab == activeTab
            val color = if (selected) RfColors.IndigoBrand else RfColors.AdminMuted
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Box {
                    RfIcon(kind = tab.icon, tint = color, size = 20.dp)
                    if (tab.badgeCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(RfColors.Error),
                        ) {
                            BasicText(
                                text = tab.badgeCount.toString(),
                                style = typo.tagLabel.copy(color = Color.White, fontSize = 9.sp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                BasicText(
                    text = tab.label,
                    style = typo.caption.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = color,
                    ),
                )
            }
        }
    }
}

/**
 * Componente `Tarjeta` estándar (Sección 4.8): radio 18 dp, borde sutil y relleno interno.
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
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

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
 * Componente `Tarjeta` hero con degradado de marca (`theme.personal` o `theme.admin`, radio 22 dp).
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
 * Variantes de botón (`Sección 4.8`).
 */
public enum class RfButtonStyle {
    PRIMARY,
    SECONDARY,
    SOL,
    DANGER,
}

/**
 * Botón de acción de RosaFiesta (alto 54 dp, radio 16 dp, opacidad 35 % cuando está deshabilitado en `s88`).
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
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
    ) {
        BasicText(
            text = label,
            style = typo.bodyStrong.copy(color = textColor, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
    }
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
            .clickable(onClick = onClick)
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
 * Tintes semánticos para el componente `Tag` (Sección 2.1).
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
 * Componente `Campo` (Sección 2.5): campo de formulario con etiqueta 13/700, alto 50–52 dp y radio 14–16 dp.
 */
@Composable
public fun RfField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    helperOrError: String? = null,
    isError: Boolean = false,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val borderColor = if (isError) RfColors.Error else palette.line
    val shape = RoundedCornerShape(RfDimens.InputRadius)

    Column(modifier = modifier.fillMaxWidth()) {
        BasicText(
            text = label,
            style = typo.caption.copy(color = palette.ink, fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(5.dp))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(shape)
                .background(palette.surface)
                .border(1.2.dp, borderColor, shape)
                .padding(horizontal = 13.dp),
        ) {
            BasicText(
                text = value,
                style = typo.body.copy(color = palette.ink),
                maxLines = 1,
            )
        }
        if (!helperOrError.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = helperOrError,
                style = typo.caption.copy(
                    color = if (isError) RfColors.Error else palette.muted,
                ),
            )
        }
    }
}

/**
 * Componente `Segmentado` (Sección 2.3 y 3): control de selección entre 2 a 4 vistas.
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
    val outerShape = RoundedCornerShape(13.dp)

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
 * Componente `Toast` (Sección 2.1 y 3): fondo `toast` (`#1A1A2E` personal / `#151729` admin), icono lima con ✓ y acción en `#FFD45C`.
 */
@Composable
public fun RfToast(
    message: String,
    actionLabel: String? = null,
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
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            RfIcon(kind = RfIconKind.CHECK, tint = RfColors.Lima, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = message,
                style = typo.bodyStrong.copy(color = Color.White, fontSize = 12.5.sp),
                maxLines = 1,
            )
        }
        if (actionLabel != null) {
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = actionLabel,
                style = typo.tagLabel.copy(color = RfColors.Sol),
                maxLines = 1,
            )
        }
    }
}

/**
 * Componente `Banner sin conexión` (Sección 2.1, 3 y 5): franja oscura (`toastBg`) con icono wifi tachado y acción "Reintentar".
 */
@Composable
public fun RfOfflineBanner(
    pendingChangesCount: Int = 3,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.toastBg)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            RfIcon(kind = RfIconKind.WIFI_OFF, tint = RfColors.Sol, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = if (isAdmin) {
                    "Sin conexión · $pendingChangesCount cambios guardados localmente"
                } else {
                    "Sin conexión · Guardado local activo"
                },
                style = typo.caption.copy(color = Color.White, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        BasicText(
            text = "Reintentar",
            style = typo.tagLabel.copy(color = RfColors.Sol),
            maxLines = 1,
        )
    }
}

/**
 * Marco móvil de referencia (390x820 dp, radio exterior 44 dp, interior 34 dp) o escritorio (1440x900 dp).
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

        Column(
            modifier = modifier
                .width(RfDimens.PhoneFrameWidth)
                .height(RfDimens.PhoneFrameHeight)
                .clip(RoundedCornerShape(RfDimens.PhoneInnerRadius))
                .background(palette.bg)
                .border(1.5.dp, palette.line, RoundedCornerShape(RfDimens.PhoneInnerRadius)),
        ) {
            // Barra de estado iOS/Android
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                BasicText(
                    text = "9:41",
                    style = typo.caption.copy(color = palette.ink, fontWeight = FontWeight.Bold),
                )
                if (screenCode.isNotEmpty()) {
                    RfTag(
                        text = "$screenCode · $screenTitle".trimEnd(' ', '·'),
                        tone = if (palette.isAdmin) RfTagTone.INDIGO else RfTagTone.ROSA,
                    )
                }
                BasicText(
                    text = "5G · 100%",
                    style = typo.caption.copy(color = palette.ink, fontWeight = FontWeight.SemiBold),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                content = content,
            )

            if (bottomBar != null) {
                bottomBar()
            }
        }
    }
}
