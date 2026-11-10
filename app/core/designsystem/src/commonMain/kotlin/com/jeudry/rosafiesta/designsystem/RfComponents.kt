package com.jeudry.rosafiesta.designsystem

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    CATALOGO("catalogo", "Catálogo", RfIconKind.STORE),
    ROSA("rosa", "Rosa", RfIconKind.SPARK),
    EVENTOS("eventos", "Eventos", RfIconKind.CALENDAR),
    FAVORITOS("favoritos", "Favoritos", RfIconKind.HEART),
    MAS("mas", "Más", RfIconKind.MORE),
    PAQUETES("paquetes", "Paquetes", RfIconKind.GIFT),
    ENTRAR("entrar", "Entrar", RfIconKind.LOGIN),
    ;

    public companion object {
        public val HOME: PersonalTab = INICIO
        public val CATALOG: PersonalTab = CATALOGO
        public val ROSA_IA: PersonalTab = ROSA
        public val EVENTS: PersonalTab = EVENTOS
        public val FAVORITES: PersonalTab = FAVORITOS
        public val MORE: PersonalTab = MAS
        public val PROFILE: PersonalTab = MAS

        public val standardTabs: List<PersonalTab> = listOf(INICIO, CATALOGO, ROSA, EVENTOS, MAS)
        public val guestTabs: List<PersonalTab> = listOf(CATALOGO, FAVORITOS, ROSA, PAQUETES, ENTRAR)
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
    val typo = LocalRfTypography.current
    val tabs = if (guestMode) PersonalTab.guestTabs else PersonalTab.standardTabs
    val barBg = if (dark) Color(0xFF161624) else Color(0xFFFFFFFF)
    val barLine = if (dark) Color(0xFF2A2A40) else Color(0xFFF0EBF4)
    val inactiveColor = if (dark) Color(0xFFA6A3BA) else Color(0xFF62627A)
    val activePillBg = Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    val activeLabelColor = Color(0xFFA956D3)
    val rosaLabelColor = if (dark) Color(0xFFF2F0F8) else Color(0xFF1A1A2E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RfDimens.BottomBarHeight),
    ) {
        // Superficie principal de 96 dp con borde superior fino y sombra superior suave
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(barBg)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, if (dark) Color(0x28000000) else Color(0x0E1A1A2E)),
                            startY = -14f,
                            endY = 0f,
                        ),
                        topLeft = Offset(0f, -14f),
                        size = Size(size.width, 14f),
                    )
                    drawLine(
                        color = barLine,
                        start = Offset(0f, 0f),
                        end = Offset(drawContext.size.width, 0f),
                        strokeWidth = 1f,
                    )
                }
                .padding(top = 10.dp, start = 8.dp, end = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                tabs.forEach { tab ->
                    if (tab == PersonalTab.ROSA) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val selected = tab == activeTab
                        val pillProgress by animateFloatAsState(
                            targetValue = if (selected) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                            label = "personal-tab-pill-${tab.key}",
                        )
                        val iconTint by animateColorAsState(
                            targetValue = if (selected) Color.White else inactiveColor,
                            animationSpec = tween(durationMillis = 220),
                            label = "personal-tab-icon-${tab.key}",
                        )
                        val labelColor by animateColorAsState(
                            targetValue = if (selected) activeLabelColor else inactiveColor,
                            animationSpec = tween(durationMillis = 220),
                            label = "personal-tab-label-${tab.key}",
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .rfTactileClickable(pressedScale = 0.92f) { onSelectTab(tab) },
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(width = 52.dp, height = 30.dp),
                            ) {
                                if (pillProgress > 0.01f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = 0.45f + (0.55f * pillProgress)
                                                scaleY = 0.70f + (0.30f * pillProgress)
                                                alpha = pillProgress.coerceIn(0f, 1f)
                                            }
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(activePillBg),
                                    )
                                }
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        val iconScale = 1f + (0.08f * pillProgress)
                                        scaleX = iconScale
                                        scaleY = iconScale
                                        translationY = -1.5f * pillProgress
                                    },
                                ) {
                                    RfIcon(
                                        kind = tab.icon,
                                        tint = iconTint,
                                        size = 22.dp,
                                    )
                                }
                            }
                            BasicText(
                                text = tab.label,
                                style = typo.caption.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = labelColor,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Botón central Rosa sobresaliendo 26 dp respecto al contenedor de ítems (-16 dp del borde superior)
        val rosaSelected = activeTab == PersonalTab.ROSA
        val rosaScale by animateFloatAsState(
            targetValue = if (rosaSelected) 1.07f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "personal-rosa-scale",
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .graphicsLayer {
                    scaleX = rosaScale
                    scaleY = rosaScale
                }
                .rfTactileClickable(pressedScale = 0.92f) { onSelectTab(PersonalTab.ROSA) },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x70E04FAF),
                        ambientColor = Color(0x25E04FAF),
                    )
                    .clip(CircleShape)
                    .background(barBg)
                    .padding(4.dp),
            ) {
                RosaAvatar(
                    size = RosaAvatarSize.TAB_CENTER,
                    glow = rosaSelected,
                    forceAdminPalette = false,
                )
            }
            BasicText(
                text = "Rosa",
                style = typo.caption.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = rosaLabelColor,
                ),
                maxLines = 1,
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
    INVENTARIO("inventario", "Inventario", RfIconKind.BOX),
    MAS("mas", "Más", RfIconKind.MORE),
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
    showBadges: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val dark = palette.isDark
    val barBg = if (dark) palette.surface else Color(0xFFFFFFFF)
    val barLine = if (dark) palette.line else Color(0xFFECECF5)
    val inactiveColor = if (dark) palette.muted else Color(0xFF5F6377)
    val activePillBg = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    val activeLabelColor = if (dark) Color(0xFFA59BFF) else Color(0xFF4B47D6)
    val rosaLabelColor = if (dark) palette.ink else Color(0xFF151729)
    val topShadowColor = if (dark) Color(0x28000000) else Color(0x0E1A1A2E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RfDimens.BottomBarHeight),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(barBg)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, topShadowColor),
                            startY = -14f,
                            endY = 0f,
                        ),
                        topLeft = Offset(0f, -14f),
                        size = Size(size.width, 14f),
                    )
                    drawLine(
                        color = barLine,
                        start = Offset(0f, 0f),
                        end = Offset(drawContext.size.width, 0f),
                        strokeWidth = 1f,
                    )
                }
                .padding(top = 10.dp, start = 8.dp, end = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                AdminTab.entries.forEach { tab ->
                    if (tab == AdminTab.ROSA) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val selected = tab == activeTab
                        val pillProgress by animateFloatAsState(
                            targetValue = if (selected) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                            label = "admin-tab-pill-${tab.key}",
                        )
                        val iconTint by animateColorAsState(
                            targetValue = if (selected) Color.White else inactiveColor,
                            animationSpec = tween(durationMillis = 220),
                            label = "admin-tab-icon-${tab.key}",
                        )
                        val labelColor by animateColorAsState(
                            targetValue = if (selected) activeLabelColor else inactiveColor,
                            animationSpec = tween(durationMillis = 220),
                            label = "admin-tab-label-${tab.key}",
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .rfTactileClickable(pressedScale = 0.92f) { onSelectTab(tab) },
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(width = 52.dp, height = 30.dp),
                            ) {
                                if (pillProgress > 0.01f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = 0.45f + (0.55f * pillProgress)
                                                scaleY = 0.70f + (0.30f * pillProgress)
                                                alpha = pillProgress.coerceIn(0f, 1f)
                                            }
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(activePillBg),
                                    )
                                }
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        val iconScale = 1f + (0.08f * pillProgress)
                                        scaleX = iconScale
                                        scaleY = iconScale
                                        translationY = -1.5f * pillProgress
                                    },
                                ) {
                                    RfIcon(kind = tab.icon, tint = iconTint, size = 22.dp)
                                }
                                if (showBadges && tab.badgeCount > 0) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-2).dp)
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
                            BasicText(
                                text = tab.label,
                                style = typo.caption.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = labelColor,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        // Botón central Rosa de 56 dp con rosa-admin-bg sobresaliendo 26 dp respecto al contenedor de ítems (-16 dp del borde superior)
        val rosaSelected = activeTab == AdminTab.ROSA
        val rosaScale by animateFloatAsState(
            targetValue = if (rosaSelected) 1.07f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "admin-rosa-scale",
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .graphicsLayer {
                    scaleX = rosaScale
                    scaleY = rosaScale
                }
                .rfTactileClickable(pressedScale = 0.92f) { onSelectTab(AdminTab.ROSA) },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x707650E8),
                        ambientColor = Color(0x257650E8),
                    )
                    .clip(CircleShape)
                    .background(barBg)
                    .padding(4.dp),
            ) {
                RosaAvatar(
                    size = RosaAvatarSize.TAB_CENTER,
                    glow = rosaSelected,
                    forceAdminPalette = true,
                )
            }
            BasicText(
                text = "Rosa",
                style = typo.caption.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = rosaLabelColor,
                ),
                maxLines = 1,
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
 * Sombra oficial de tarjetas de RosaFiesta (`.rf .card`):
 * Personal: `box-shadow: 0 0 0 1px #F2EDF5, 0 12px 28px -20px rgba(200,80,170,.4);`
 * Admin: `box-shadow: 0 0 0 1px #ECECF5, 0 12px 28px -22px rgba(70,80,180,.45);`
 */
public fun Modifier.rfCardShadow(
    shape: Shape = RoundedCornerShape(RfDimens.CardRadius),
    isAdmin: Boolean = false,
): Modifier = this.shadow(
    elevation = 6.dp,
    shape = shape,
    spotColor = if (isAdmin) Color(0x304650B4) else Color(0x35C850AA),
    ambientColor = if (isAdmin) Color(0x124650B4) else Color(0x15C850AA),
)

/**
 * Sombra oficial de campos de entrada (`.rf .fld`):
 * Personal: `box-shadow: 0 0 0 1px #E8E1EE, 0 8px 20px -16px rgba(180,80,180,.35);`
 * Admin: `box-shadow: 0 0 0 1px #E3E4EF, 0 8px 20px -16px rgba(70,80,180,.3);`
 */
public fun Modifier.rfInputShadow(
    shape: Shape = RoundedCornerShape(RfDimens.InputRadius),
    isAdmin: Boolean = false,
    isActive: Boolean = false,
): Modifier = this.shadow(
    elevation = if (isActive) 6.dp else 4.dp,
    shape = shape,
    spotColor = if (isAdmin) {
        Color(0x254650B4)
    } else if (isActive) {
        Color(0x35B450B4)
    } else {
        Color(0x25B450B4)
    },
    ambientColor = if (isAdmin) Color(0x104650B4) else Color(0x10B450B4),
)

/**
 * Sombra oficial de botones circulares de acción (`.rf .ib`):
 * Personal: `box-shadow: 0 0 0 1px #F0EBF4, 0 8px 20px -14px rgba(180,80,180,.4);`
 * Admin: `box-shadow: 0 0 0 1px #ECECF5, 0 8px 20px -14px rgba(70,80,180,.45);`
 */
public fun Modifier.rfIconButtonShadow(
    shape: Shape = CircleShape,
    isAdmin: Boolean = false,
): Modifier = this.shadow(
    elevation = 4.dp,
    shape = shape,
    spotColor = if (isAdmin) Color(0x254650B4) else Color(0x25B450B4),
    ambientColor = if (isAdmin) Color(0x104650B4) else Color(0x10B450B4),
)

/**
 * Sombra oficial de botones primarios (`.rf .bp`):
 * Personal: `box-shadow: 0 12px 24px -12px rgba(234,72,153,.7);`
 * Admin: `box-shadow: 0 12px 24px -12px rgba(91,108,240,.75);`
 */
public fun Modifier.rfPrimaryButtonShadow(
    shape: Shape = RoundedCornerShape(RfDimens.ButtonRadius),
    isAdmin: Boolean = false,
): Modifier = this.shadow(
    elevation = 8.dp,
    shape = shape,
    spotColor = if (isAdmin) Color(0x505B6CF0) else Color(0x50EA4899),
    ambientColor = if (isAdmin) Color(0x205B6CF0) else Color(0x20EA4899),
)

/**
 * Sombra oficial de banners Hero destacados:
 * Personal: `box-shadow: 0 18px 34px -20px rgba(234,78,169,.8);`
 * Admin: `box-shadow: 0 18px 34px -20px rgba(80,110,230,.8);`
 */
public fun Modifier.rfHeroBannerShadow(
    shape: Shape = RoundedCornerShape(22.dp),
    isAdmin: Boolean = true,
): Modifier = this.shadow(
    elevation = 10.dp,
    shape = shape,
    spotColor = if (isAdmin) Color(0x40506EE6) else Color(0x45EA4899),
    ambientColor = if (isAdmin) Color(0x15506EE6) else Color(0x18EA4899),
)

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
            .rfCardShadow(shape, isAdmin = palette.isAdmin)
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
            .rfHeroBannerShadow(shape, isAdmin = useAdminGradient)
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
                .rfPrimaryButtonShadow(shape, isAdmin = palette.isAdmin)
                .clip(shape)
                .background(palette.primaryGradient)

        RfButtonStyle.SECONDARY ->
            modifier
                .then(widthModifier)
                .then(alphaModifier)
                .height(RfDimens.ButtonHeight)
                .rfInputShadow(shape, isAdmin = palette.isAdmin)
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
            .rfIconButtonShadow(CircleShape, isAdmin = palette.isAdmin)
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
                .rfInputShadow(shape, isAdmin = palette.isAdmin)
                .clip(shape)
                .background(palette.surface)
                .border(1.2.dp, borderColor, shape)
                .padding(horizontal = 14.dp),
        ) {
            if (onValueChange != null) {
                val showPlaceholder = if (value.isEmpty()) placeholder.isNotEmpty() else false
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
                            if (showPlaceholder) {
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
                val usePlaceholder = if (value.isEmpty()) placeholder.isNotEmpty() else false
                val displayText = if (usePlaceholder) placeholder else value
                val displayColor = if (usePlaceholder) palette.muted else palette.ink
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
 * Theme-aware search field reading surface, border, and text tokens from [LocalRfPalette].
 */
@Composable
public fun RfSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar artículos, paquetes o eventos...",
    onClear: (() -> Unit)? = null,
    trailingIcon: RfIconKind? = null,
    trailingAccessibilityLabel: String = "",
    onTrailingClick: (() -> Unit)? = null,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val shape = RoundedCornerShape(RfDimens.InputRadius)
    val showPlaceholder = if (query.isEmpty()) placeholder.isNotEmpty() else false

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(RfDimens.FieldHeight)
            .rfInputShadow(shape, isAdmin = palette.isAdmin, isActive = query.isNotEmpty())
            .clip(shape)
            .background(palette.surface)
            .border(1.2.dp, palette.fieldBorder, shape)
            .padding(horizontal = 14.dp),
    ) {
        RfIcon(kind = RfIconKind.SEARCH, tint = palette.muted, size = 18.dp)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typo.body.copy(color = palette.ink),
            cursorBrush = SolidColor(palette.primaryAccent),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (showPlaceholder) {
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
        if (query.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(palette.chipBg)
                    .clickable {
                        if (onClear != null) {
                            onClear()
                        } else {
                            onQueryChange("")
                        }
                    },
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 13.dp)
            }
        }
        if (trailingIcon != null) {
            val resolvedLabel = trailingAccessibilityLabel.ifEmpty { trailingIcon.name }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .semantics { contentDescription = resolvedLabel }
                    .then(
                        if (onTrailingClick != null) {
                            Modifier.rfTactileClickable(pressedScale = 0.92f, onClick = onTrailingClick)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                RfIcon(kind = trailingIcon, tint = palette.primaryAccent, size = 18.dp)
            }
        }
    }
}

/**
 * Theme-aware empty state card reading surface, line, ink, and muted tokens from [LocalRfPalette].
 */
@Composable
public fun RfEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: RfIconKind = RfIconKind.SPARK,
    tone: RfTagTone = RfTagTone.ROSA,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    val shape = RoundedCornerShape(RfDimens.CardRadius)
    val hasAction = if (!actionLabel.isNullOrBlank()) onActionClick != null else false

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(shape, isAdmin = palette.isAdmin)
            .clip(shape)
            .background(palette.surface)
            .border(1.dp, palette.line, shape)
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        RfIconTile(icon = icon, tone = tone)
        Spacer(Modifier.height(2.dp))
        BasicText(
            text = title,
            style = typo.sheetOrEmptyTitle.copy(
                color = palette.ink,
                textAlign = TextAlign.Center,
            ),
        )
        BasicText(
            text = description,
            style = typo.secondary.copy(
                color = palette.muted,
                textAlign = TextAlign.Center,
            ),
        )
        if (hasAction) {
            Spacer(Modifier.height(6.dp))
            RfButton(
                label = actionLabel.orEmpty(),
                onClick = { onActionClick?.invoke() },
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
 * Resolves the base skeleton shimmer color for light and dark theme variants while keeping light-mode gallery parity.
 */
public fun resolveRfSkeletonBaseColor(isAdmin: Boolean, isDark: Boolean): Color {
    if (isDark) {
        return if (isAdmin) Color(0xFF23253A) else Color(0xFF262136)
    }
    return if (isAdmin) Color(0xFFE4E5F0) else Color(0xFFEDE5F1)
}

/**
 * Resolves the highlight skeleton shimmer color for light and dark theme variants.
 */
public fun resolveRfSkeletonHighlightColor(isAdmin: Boolean, isDark: Boolean): Color {
    if (isDark) {
        return if (isAdmin) Color(0xFF343857) else Color(0xFF393150)
    }
    return if (isAdmin) Color(0xFFF7F8FD) else Color(0xFFFAF7FC)
}

/**
 * Official shimmer brush for skeleton placeholders with dark-mode support and deterministic static capture mode.
 */
@Composable
public fun rememberRfSkeletonBrush(
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    animate: Boolean = true,
): Brush {
    val palette = LocalRfPalette.current
    val baseColor = resolveRfSkeletonBaseColor(isAdmin = isAdmin, isDark = palette.isDark)
    val highlightColor = resolveRfSkeletonHighlightColor(isAdmin = isAdmin, isDark = palette.isDark)
    return if (animate) {
        val transition = rememberInfiniteTransition(label = "rf-skeleton-shimmer")
        val progress by transition.animateFloat(
            initialValue = -300f,
            targetValue = 1300f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmer-x",
        )
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(progress, 0f),
            end = Offset(progress + 320f, 60f),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(0f, 0f),
            end = Offset(320f, 60f),
        )
    }
}

/**
 * Modificador para aplicar el fondo de esqueleto oficial a cualquier elemento.
 */
@Composable
public fun Modifier.rfSkeleton(
    shape: Shape = RoundedCornerShape(10.dp),
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    animate: Boolean = true,
): Modifier {
    val brush = rememberRfSkeletonBrush(isAdmin = isAdmin, animate = animate)
    return this.clip(shape).background(brush)
}

/**
 * Animated shimmer skeleton card translating a linear highlight gradient across the card surface,
 * while remaining deterministic when [animate] is `false` for static screenshot capture.
 */
@Composable
public fun RfShimmerCard(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    animate: Boolean = true,
) {
    val palette = LocalRfPalette.current
    val shimmerBrush = rememberRfSkeletonBrush(isAdmin = isAdmin, animate = animate)
    val baseColor = resolveRfSkeletonBaseColor(isAdmin = isAdmin, isDark = palette.isDark)
    val cardSurface = if (palette.isDark) palette.surface else Color.White
    val cardBorder = if (palette.isDark) palette.line else baseColor
    val shape = RoundedCornerShape(RfDimens.CardRadius)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(shape, isAdmin = isAdmin)
            .clip(shape)
            .background(cardSurface)
            .border(1.dp, cardBorder, shape)
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

/**
 * Componente `Esqueleto` de carga (`s90`, `A36`, Sección 5):
 * Misma estructura que la pantalla y un brillo de izquierda a derecha de 1.2 s (`1200` ms).
 * Nada de spinners a pantalla completa.
 */
@Composable
public fun RfSkeleton(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    animate: Boolean = true,
) {
    val shimmerBrush = rememberRfSkeletonBrush(isAdmin = isAdmin, animate = animate)

    Column(
        verticalArrangement = Arrangement.spacedBy(RfDimens.BlockSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(RfDimens.CardRadius))
                .background(shimmerBrush),
        )
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
        repeat(3) {
            RfShimmerCard(isAdmin = isAdmin, animate = animate)
        }
    }
}

/**
 * Estructuras oficiales de esqueleto de carga según el tipo de pantalla (Sección 5).
 */
public enum class RfSkeletonLayout {
    CATALOG,
    DASHBOARD,
    EVENTS_LIST,
    ANALYTICS,
    DETAIL_FORM,
    GENERIC,
}

/**
 * Pantalla completa de estado de carga en esqueleto (`s90`, `A36`, Sección 5).
 * Adapta su estructura según el tipo de pantalla ([layout]) con brillo de 1.2 s de izquierda a derecha.
 */
@Composable
public fun RfSkeletonScreen(
    layout: RfSkeletonLayout = RfSkeletonLayout.GENERIC,
    isAdmin: Boolean = LocalRfPalette.current.isAdmin,
    animate: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val shimmerBrush = rememberRfSkeletonBrush(isAdmin = isAdmin, animate = animate)
    val baseColor = resolveRfSkeletonBaseColor(isAdmin = isAdmin, isDark = palette.isDark)
    val cardSurface = if (palette.isDark) palette.surface else Color.White
    val cardBorder = if (palette.isDark) palette.line else baseColor

    RfRadialBackgroundBox(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (layout) {
                RfSkeletonLayout.CATALOG -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(shimmerBrush))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.width(96.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.width(110.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.width(80.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        RfSkeletonProductCard(shimmerBrush = shimmerBrush, isAdmin = isAdmin, modifier = Modifier.weight(1f))
                        RfSkeletonProductCard(shimmerBrush = shimmerBrush, isAdmin = isAdmin, modifier = Modifier.weight(1f))
                    }
                }

                RfSkeletonLayout.DASHBOARD -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                            Box(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                        }
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(24.dp)).background(shimmerBrush))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth(0.35f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                    }
                }

                RfSkeletonLayout.EVENTS_LIST -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.width(100.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.width(90.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.width(110.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                    }
                    repeat(3) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(RoundedCornerShape(RfDimens.CardRadius), isAdmin = isAdmin)
                                .clip(RoundedCornerShape(RfDimens.CardRadius))
                                .background(cardSurface)
                                .border(1.dp, cardBorder, RoundedCornerShape(RfDimens.CardRadius))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(shimmerBrush))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.fillMaxWidth(0.65f).height(16.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                                    Box(modifier = Modifier.fillMaxWidth(0.40f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                                }
                                Box(modifier = Modifier.width(60.dp).height(24.dp).clip(RoundedCornerShape(999.dp)).background(shimmerBrush))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(cardBorder))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                                Box(modifier = Modifier.width(70.dp).height(14.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                            }
                        }
                    }
                }

                RfSkeletonLayout.ANALYTICS -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.width(90.dp).height(38.dp).clip(RoundedCornerShape(12.dp)).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(22.dp)).background(shimmerBrush))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(RfDimens.CardRadius), isAdmin = isAdmin)
                            .clip(RoundedCornerShape(RfDimens.CardRadius))
                            .background(cardSurface)
                            .border(1.dp, cardBorder, RoundedCornerShape(RfDimens.CardRadius))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).background(shimmerBrush))
                    }
                }

                RfSkeletonLayout.DETAIL_FORM -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                        Box(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)).background(shimmerBrush))
                    repeat(3) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                            Box(modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(shimmerBrush))
                        }
                    }
                }

                RfSkeletonLayout.GENERIC -> {
                    RfSkeleton(isAdmin = isAdmin, animate = animate)
                }
            }
        }
    }
}

@Composable
private fun RfSkeletonProductCard(
    shimmerBrush: Brush,
    isAdmin: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val cardSurface = if (palette.isDark) palette.surface else Color.White
    val cardBorder = if (palette.isDark) palette.line else Color(0xFFF2EDF5)

    Column(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(RfDimens.CardRadius), isAdmin = isAdmin)
            .clip(RoundedCornerShape(RfDimens.CardRadius))
            .background(cardSurface)
            .border(1.dp, cardBorder, RoundedCornerShape(RfDimens.CardRadius)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(shimmerBrush),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBrush),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBrush),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush),
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush),
                )
            }
        }
    }
}

/**
 * Fondo con los dos degradados radiales superiores definidos en la Sección 2.2 (Personal) y 2.3 (Admin).
 */
@Composable
public fun RfRadialBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalRfPalette.current
    Box(modifier = modifier.background(palette.bg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            fun ellipse(centerX: Float, centerY: Float, rx: Float, ry: Float, color: Color) {
                val isValidRadius = if (rx > 0f) ry > 0f else false
                if (!isValidRadius) return
                withTransform({
                    scale(scaleX = 1f, scaleY = ry / rx, pivot = Offset(centerX, centerY))
                }) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(0f to color, 0.6f to color.copy(alpha = 0f)),
                            center = Offset(centerX, centerY),
                            radius = rx,
                        ),
                        radius = rx,
                        center = Offset(centerX, centerY),
                    )
                }
            }
            if (palette.isAdmin) {
                ellipse(centerX = w, centerY = 0f, rx = w * 1.1f, ry = h * 0.4f, color = palette.radialPrimary)
                ellipse(centerX = 0f, centerY = h * 0.12f, rx = w * 0.8f, ry = h * 0.35f, color = palette.radialSecondary)
            } else {
                ellipse(centerX = 0f, centerY = 0f, rx = w * 1.1f, ry = h * 0.4f, color = palette.radialPrimary)
                ellipse(centerX = w, centerY = h * 0.15f, rx = w * 0.8f, ry = h * 0.35f, color = palette.radialSecondary)
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
    showMockStatusBar: Boolean = false,
    wrapInPhoneBorder: Boolean = false,
    fillAvailable: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RfTheme(variant = variant) {
        val frameModifier = if (wrapInPhoneBorder) {
            modifier
                .width(RfDimens.PhoneFrameWidth)
                .height(RfDimens.PhoneFrameHeight)
                .clip(RoundedCornerShape(RfDimens.PhoneInnerRadius))
                .border(1.5.dp, LocalRfPalette.current.line, RoundedCornerShape(RfDimens.PhoneInnerRadius))
        } else if (fillAvailable) {
            modifier.fillMaxSize()
        } else {
            modifier
                .width(RfDimens.PhoneFrameWidth)
                .height(RfDimens.PhoneFrameHeight)
        }

        RfRadialBackgroundBox(
            modifier = frameModifier,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    content = content,
                )
                if (bottomBar != null) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        bottomBar()
                    }
                }
            }
        }
    }
}
