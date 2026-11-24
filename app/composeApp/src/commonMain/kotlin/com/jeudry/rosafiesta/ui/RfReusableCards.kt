package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.rememberDecodedImageBitmap
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.PhotoAsset

internal fun PhotoAsset.toPhotoKey(): RfPhotoKey = when (this) {
    PhotoAsset.SILLA_TIFFANY -> RfPhotoKey.SILLA_TIFFANY
    PhotoAsset.MESA_PLEGABLE -> RfPhotoKey.MESA_PLEGABLE
    PhotoAsset.MESA_REDONDA -> RfPhotoKey.MESA_REDONDA
    PhotoAsset.CENTRO_FLORAL -> RfPhotoKey.CENTRO_FLORAL
    PhotoAsset.ARCO_ROSA -> RfPhotoKey.ARCO_ROSA
    PhotoAsset.MESA_HELADOS -> RfPhotoKey.MESA_HELADOS
    PhotoAsset.BODA -> RfPhotoKey.BODA
    PhotoAsset.QUINCEANERA -> RfPhotoKey.QUINCEANERA
    PhotoAsset.CUMPLE_INFANTIL -> RfPhotoKey.CUMPLE_INFANTIL
    PhotoAsset.BABY_SHOWER -> RfPhotoKey.BABY_SHOWER
    PhotoAsset.GRADUACION -> RfPhotoKey.GRADUACION
    PhotoAsset.CORPORATIVO -> RfPhotoKey.CORPORATIVO
}

/**
 * Fila reutilizable de etiqueta + interruptor (`RfSwitch`).
 */
@Composable
internal fun RfPreferenceToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        BasicText(label, style = typo.bodyStrong, modifier = Modifier.weight(1f))
        RfSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Fila reutilizable de resumen clave-valor para cotizaciones, contratos y finanzas.
 */
@Composable
internal fun RfKeyValueSummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        BasicText(label, style = typo.body, modifier = Modifier.weight(1f))
        BasicText(value, style = typo.bodyStrong)
    }
}

/**
 * Selector de cantidad reutilizable con botones rápidos `-10` / `+10`.
 */
@Composable
internal fun RfQuantityStepperRow(
    label: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementLabel: String = "-10",
    incrementLabel: String = "+10",
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicText(label, style = typo.bodyStrong)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RfChip(decrementLabel, selected = false, onClick = onDecrement)
            RfChip(incrementLabel, selected = true, onClick = onIncrement)
        }
    }
}

/**
 * Tarjeta reutilizable para secciones legales, privacidad, facturación fiscal y licencias (`s87`).
 */
@Composable
internal fun RfLegalClauseCard(
    badgeText: String,
    tone: RfTagTone,
    title: String,
    bullets: List<String>,
) {
    val typo = LocalRfTypography.current
    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(title, style = typo.cardTitle, modifier = Modifier.weight(1f))
            RfTag(text = badgeText, tone = tone)
        }
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            bullets.forEach { line ->
                BasicText("• $line", style = typo.body)
            }
        }
    }
}

/**
 * Renders uploaded remote media when [photoUrl] is present and cached/fetched via [viewModel],
 * falling back to bundled [RfPhoto] using [fallbackPhotoKey].
 */
@Composable
internal fun RfUploadedOrCatalogPhoto(
    photoUrl: String,
    fallbackPhotoKey: RfPhotoKey,
    contentDescription: String,
    viewModel: RosaFiestaViewModel?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    bottomScrim: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val cleanUrl = photoUrl.trim()
    LaunchedEffect(cleanUrl, viewModel) {
        if (cleanUrl.isNotBlank()) {
            viewModel?.ensureMediaLoaded(cleanUrl)
        }
    }
    val cacheMap = viewModel?.mediaBytesCache?.collectAsState()?.value.orEmpty()
    val rawBytes = if (cleanUrl.isNotBlank()) cacheMap[cleanUrl] else null
    val decodedBitmap = rememberDecodedImageBitmap(rawBytes)
    if (decodedBitmap != null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color(0xFFF3F3F8)),
        ) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        RfPhoto(
            photoKey = fallbackPhotoKey,
            contentDescription = contentDescription,
            modifier = modifier,
            cornerRadius = cornerRadius,
            bottomScrim = bottomScrim,
        )
    }
}

internal fun isCustomMediaIconKey(rawValue: String): Boolean {
    val trimmed = rawValue.trim().lowercase()
    if (trimmed.isEmpty()) return false
    if (trimmed.startsWith("/v1/media/")) return true
    if (trimmed.startsWith("http://")) return true
    return trimmed.startsWith("https://")
}

/**
 * Renders a category's user-uploaded custom SVG/image from [iconUrlOrKey], or a neutral fallback [RfIcon].
 */
@Composable
internal fun RfCategoryCustomOrFallbackIcon(
    iconUrlOrKey: String,
    contentDescription: String,
    viewModel: RosaFiestaViewModel?,
    size: Dp = 24.dp,
    fallbackIcon: RfIconKind = RfIconKind.CATALOG,
    fallbackTint: Color = Color(0xFF4B47D6),
    modifier: Modifier = Modifier,
) {
    val cleanValue = iconUrlOrKey.trim()
    val hasCustomUrl = isCustomMediaIconKey(cleanValue)
    LaunchedEffect(cleanValue, viewModel) {
        if (hasCustomUrl) {
            viewModel?.ensureMediaLoaded(cleanValue)
        }
    }
    val cacheMap = viewModel?.mediaBytesCache?.collectAsState()?.value.orEmpty()
    val rawBytes = if (hasCustomUrl) cacheMap[cleanValue] else null
    val decodedBitmap = rememberDecodedImageBitmap(rawBytes)
    if (decodedBitmap != null) {
        Image(
            bitmap = decodedBitmap,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(size),
        )
    } else {
        RfIcon(
            kind = fallbackIcon,
            tint = fallbackTint,
            size = size,
            modifier = modifier,
        )
    }
}

/**
 * Unified gradient pill button for the active event draft/cart shared across Home, Catalog, and Events.
 */
@Composable
internal fun RfDraftCartPillButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonHeight: Dp = RfDimens.IconButtonSize,
) {
    val typo = LocalRfTypography.current
    val horizontalStartPadding = if (count > 0) 11.dp else 0.dp
    val horizontalEndPadding = if (count > 0) 6.dp else 0.dp
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (count > 0) Arrangement.spacedBy(6.dp) else Arrangement.Center,
        modifier = modifier
            .widthIn(min = buttonHeight)
            .height(buttonHeight)
            .rfPrimaryButtonShadow(CircleShape, isAdmin = false)
            .clip(CircleShape)
            .background(gradient)
            .semantics { contentDescription = "Borrador de evento" }
            .clickable(onClick = onClick)
            .padding(start = horizontalStartPadding, end = horizontalEndPadding),
    ) {
        RfIcon(kind = RfIconKind.PARTY, tint = Color.White, size = 19.dp)
        if (count > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(22.dp)
                    .widthIn(min = 24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD45C))
                    .padding(horizontal = 6.dp),
            ) {
                BasicText(
                    text = "$count",
                    maxLines = 1,
                    softWrap = false,
                    style = typo.caption.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E1A47),
                    ),
                )
            }
        }
    }
}

/**
 * Unified circular gradient action button for header primary actions in Personal and Admin screens.
 */
@Composable
internal fun RfRoundGradientIconButton(
    icon: RfIconKind,
    accessibilityLabel: String,
    isAdmin: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = if (isAdmin) {
        Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(RfDimens.IconButtonSize)
            .rfPrimaryButtonShadow(CircleShape, isAdmin = isAdmin)
            .clip(CircleShape)
            .background(gradient)
            .semantics { contentDescription = accessibilityLabel }
            .clickable(onClick = onClick),
    ) {
        RfIcon(kind = icon, tint = Color.White, size = 19.dp)
    }
}

/**
 * Full-screen photo viewer overlay with close action and gallery navigation.
 */
@Composable
internal fun RfFullScreenPhotoViewer(
    mediaItems: List<Pair<String, RfPhotoKey>>,
    currentIndex: Int,
    title: String,
    viewModel: RosaFiestaViewModel?,
    onSelectIndex: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    if (mediaItems.isEmpty()) return
    val safeIndex = currentIndex.coerceIn(0, mediaItems.lastIndex)
    val activeItem = mediaItems[safeIndex]
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEB0E0F1C))
            .clickable(onClick = onDismiss),
    ) {
        RfUploadedOrCatalogPhoto(
            photoUrl = activeItem.first,
            fallbackPhotoKey = activeItem.second,
            contentDescription = title,
            viewModel = viewModel,
            cornerRadius = 0.dp,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 72.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            BasicText(
                text = if (mediaItems.size > 1) "$title · ${safeIndex + 1}/${mediaItems.size}" else title,
                style = typo.bodyStrong.copy(color = Color.White, fontSize = 14.sp),
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(RfDimens.IconButtonSize)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .semantics { contentDescription = "Cerrar vista completa" }
                    .clickable(onClick = onDismiss),
            ) {
                RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 18.dp)
            }
        }

        if (mediaItems.size > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(RfDimens.IconButtonSize)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .semantics { contentDescription = "Foto anterior" }
                        .clickable {
                            val prevIndex = if (safeIndex == 0) mediaItems.lastIndex else safeIndex - 1
                            onSelectIndex(prevIndex)
                        },
                ) {
                    RfIcon(kind = RfIconKind.CHEVRON_LEFT, tint = Color.White, size = 18.dp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(RfDimens.IconButtonSize)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .semantics { contentDescription = "Foto siguiente" }
                        .clickable {
                            val nextIndex = (safeIndex + 1) % mediaItems.size
                            onSelectIndex(nextIndex)
                        },
                ) {
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color.White, size = 18.dp)
                }
            }
        }
    }
}

/**
 * Illustrated empty state card with icon badge, concise copy, and optional gradient CTA button.
 */
@Composable
internal fun RfEmptyStateCard(
    icon: RfIconKind,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    isAdmin: Boolean = false,
    compact: Boolean = false,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val cardBg = palette.surface
    val cardBorder = palette.line
    val titleColor = palette.ink
    val subtitleColor = palette.muted
    val iconBadgeBg = when {
        isAdmin -> if (palette.isDark) Color(0xFF282B52) else Color(0xFFECEBFF)
        palette.isDark -> Color(0xFF33265A)
        else -> Color(0xFFFDE6F2)
    }
    val iconTint = when {
        isAdmin -> if (palette.isDark) Color(0xFFA7A5FF) else Color(0xFF4B47D6)
        palette.isDark -> Color(0xFFF48FB1)
        else -> Color(0xFFC2338F)
    }
    val ctaBrush = if (isAdmin) {
        Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    }
    val hasActionLabel = !actionLabel.isNullOrBlank()
    val hasAction = if (hasActionLabel) onAction != null else false

    Box(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = isAdmin)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = if (compact) 16.dp else 22.dp),
    ) {
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBadgeBg),
                    ) {
                        RfIcon(kind = icon, tint = iconTint, size = 22.dp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        BasicText(
                            text = title,
                            style = typo.bodyStrong.copy(fontSize = 15.sp, color = titleColor),
                        )
                        BasicText(
                            text = subtitle,
                            style = typo.secondary.copy(fontSize = 13.sp, lineHeight = 18.sp, color = subtitleColor),
                        )
                    }
                }
                if (hasAction) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = isAdmin)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ctaBrush)
                            .clickable(onClick = onAction!!),
                    ) {
                        BasicText(
                            text = actionLabel!!,
                            style = typo.buttonLabel.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 16.dp)
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBadgeBg),
                ) {
                    RfIcon(kind = icon, tint = iconTint, size = 26.dp)
                }
                BasicText(
                    text = title,
                    style = typo.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = titleColor, textAlign = TextAlign.Center),
                )
                BasicText(
                    text = subtitle,
                    style = typo.secondary.copy(fontSize = 13.5.sp, lineHeight = 19.sp, color = subtitleColor, textAlign = TextAlign.Center),
                )
                if (hasAction) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = isAdmin)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ctaBrush)
                            .clickable(onClick = onAction!!),
                    ) {
                        BasicText(
                            text = actionLabel!!,
                            style = typo.buttonLabel.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact white floating preview card showing the article saved to the event draft.
 */
@Composable
internal fun RfSavedCartPreviewToast(
    preview: SavedCartItemPreview,
    viewModel: RosaFiestaViewModel,
    onOpenCart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val subtotalDop = preview.quantity * preview.unitPriceDop

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .widthIn(min = 210.dp, max = 276.dp)
            .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = false)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenCart)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        RfUploadedOrCatalogPhoto(
            photoUrl = preview.photoUrl,
            fallbackPhotoKey = preview.photo.toPhotoKey(),
            contentDescription = preview.title,
            viewModel = viewModel,
            cornerRadius = 10.dp,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(36.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF228B4E), size = 12.dp)
                BasicText(
                    text = "Guardado en Mi evento",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typo.caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF228B4E),
                    ),
                )
            }
            BasicText(
                text = "${preview.title} · ${preview.quantity} u.",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typo.bodyStrong.copy(
                    fontSize = 13.sp,
                    color = palette.ink,
                ),
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            BasicText(
                text = "RD$ $subtotalDop",
                style = typo.bodyStrong.copy(
                    fontSize = 12.5.sp,
                    color = Color(0xFFC2338F),
                ),
            )
            BasicText(
                text = "Ver",
                style = typo.caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A4FD8),
                ),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(palette.chipBg)
                .clickable(onClick = onDismiss),
        ) {
            RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 12.dp)
        }
    }
}

/**
 * Centered confirmation modal guarding destructive delete actions across Personal and Admin screens.
 */
@Composable
internal fun RfDeleteConfirmationDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Eliminar",
    cancelLabel: String = "Cancelar",
    errorMessage: String? = null,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(120)),
        modifier = Modifier.fillMaxSize(),
    ) {
        val scrimInteraction = remember { MutableInteractionSource() }
        val cardInteraction = remember { MutableInteractionSource() }
        val dangerBg = Color(0xFFB42345)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable(
                    interactionSource = scrimInteraction,
                    indication = null,
                    onClick = {
                        if (!isSubmitting) {
                            onDismiss()
                        }
                    },
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = cardInteraction,
                        indication = null,
                        onClick = {},
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BasicText(
                    text = title,
                    style = typo.sheetOrEmptyTitle.copy(color = palette.ink),
                )
                BasicText(
                    text = message,
                    style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                )
                if (!errorMessage.isNullOrBlank()) {
                    BasicText(
                        text = errorMessage,
                        style = typo.caption.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (palette.isDark) Color(0xFFFF7A96) else dangerBg,
                        ),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.chipBg)
                            .clickable(enabled = !isSubmitting, onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = cancelLabel,
                            style = typo.buttonLabel.copy(fontSize = 14.sp, color = palette.muted),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(dangerBg)
                            .clickable(enabled = !isSubmitting, onClick = onConfirm),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (isSubmitting) "Eliminando..." else confirmLabel,
                            style = typo.buttonLabel.copy(fontSize = 14.sp, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

internal fun rfAdminIndigoPillBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF)

internal fun rfAdminIndigoPillInk(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)

internal fun rfAdminSuccessPillBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF253516) else Color(0xFFEEF6D6)

internal fun rfAdminSuccessPillInk(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFFAEDB5C) else Color(0xFF4E6A10)

internal fun rfAdminWarnPillBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF3A2F16) else Color(0xFFFFF2C9)

internal fun rfAdminWarnPillInk(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFFF5C954) else Color(0xFF7A5700)

internal fun rfAdminTealPillBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF173636) else Color(0xFFDDF4F3)

internal fun rfAdminTealPillInk(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF5CD6CE) else Color(0xFF1F8A84)

internal fun rfAdminDangerPillBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF3D1924) else Color(0xFFFCE3E8)

internal fun rfAdminDangerPillInk(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFFFF7A96) else Color(0xFFB42345)

internal fun rfAdminSoftTintBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF262348) else Color(0xFFF7F6FF)

internal fun rfAdminNoteAmberBg(): Color = if (RfThemeState.currentPalette.isDark) Color(0xFF332A16) else Color(0xFFFFF8E5)

/**
 * Full-screen Personal empty-state scaffold shown when the client has no active events yet.
 */
@Composable
internal fun PersonalEmptyEventStateScaffold(
    headerTitle: String,
    backTargetCode: String,
    message: String,
    onSelectScreenCode: (String) -> Unit,
    actionLabel: String = "Crear mi evento",
    actionTargetCode: String = "s38",
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current
    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode(backTargetCode) },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                BasicText(
                    text = headerTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typo.screenTitle.copy(color = palette.ink),
                    modifier = Modifier.weight(1f),
                )
            }
            RfEmptyStateCard(
                icon = RfIconKind.CALENDAR,
                title = "Aún no tienes eventos",
                subtitle = message,
                actionLabel = actionLabel,
                isAdmin = false,
                onAction = { onSelectScreenCode(actionTargetCode) },
            )
        }
    }
}
