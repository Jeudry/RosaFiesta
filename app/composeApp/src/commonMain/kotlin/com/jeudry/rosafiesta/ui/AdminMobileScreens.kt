package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.core.engine.UserRole
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconButton
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.designsystem.rfTrackValidationFocus
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.validation.InputValidator
import com.jeudry.rosafiesta.domain.validation.MaskedTextCursorResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private val AdminBackgroundGradient = Brush.radialGradient(
    0.0f to Color(0xFFE6E7FF),
    0.6f to Color(0x00E6E7FF),
    center = Offset(400f, 0f),
    radius = 450f,
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

private enum class AdminQuoteSourceKind {
    NONE,
    QUOTE,
    LEAD,
}

private data class AdminQuoteDraftState(
    val sourceKey: String = "",
    val sourceKind: AdminQuoteSourceKind = AdminQuoteSourceKind.NONE,
    val clientName: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val eventTitle: String = "",
    val dateLabel: String = "",
    val guestsCount: Int = 40,
    val venue: String = "",
    val lines: List<EventLineItem> = emptyList(),
    val deliveryFeeText: String = "350",
    val discountText: String = "0",
    val notes: String = "Incluye montaje 2 horas antes y coordinación logística en salón.",
)

private val adminQuoteDraftFlow = MutableStateFlow(AdminQuoteDraftState())

private fun bothConditions(first: Boolean, second: Boolean): Boolean = if (first) second else false

private fun eitherCondition(first: Boolean, second: Boolean): Boolean = if (first) true else second

/**
 * Consumes pointer taps on modal surfaces without displaying a material ripple.
 */
@Composable
private fun Modifier.rfSwallowTapWithoutRipple(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {},
    )
}

/**
 * Displays a centered admin modal dialog with fade-and-scale transitions and tap-swallowing surface.
 */
@Composable
private fun AdminCenteredDialogOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    verticalSpacing: Dp = 10.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(120)),
        modifier = modifier.fillMaxSize(),
    ) {
        val scrimInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73151729))
                .onPreviewKeyEvent { keyEvent ->
                    val isKeyDown = keyEvent.type == KeyEventType.KeyDown
                    val isEscape = keyEvent.key == Key.Escape
                    val shouldDismiss = if (isKeyDown) isEscape else false
                    if (shouldDismiss) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                }
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .rfSwallowTapWithoutRipple()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                content = content,
            )
        }
    }
}

/**
 * Displays a bottom-anchored admin modal sheet with slide-up motion and no-ripple tap swallowing.
 */
@Composable
internal fun AdminBottomSheetOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    outerPadding: Dp = 0.dp,
    bottomClearance: Dp = 0.dp,
    shape: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    innerHorizontalPadding: Dp = 20.dp,
    innerVerticalPadding: Dp = 24.dp,
    verticalSpacing: Dp = 12.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
        modifier = modifier.fillMaxSize(),
    ) {
        val scrimInteractionSource = remember { MutableInteractionSource() }
        val sheetInteractionSource = remember { MutableInteractionSource() }
        val sheetSlideModifier = rememberModalSheetSlideModifier()
        val hasBottomClearance = bottomClearance > 0.dp
        val isDefaultOuterPadding = outerPadding == 0.dp
        val useFloatingSideMargin = if (hasBottomClearance) isDefaultOuterPadding else false
        val resolvedHorizontalPadding = if (useFloatingSideMargin) 16.dp else outerPadding
        val resolvedBottomPadding = if (outerPadding > bottomClearance) outerPadding else bottomClearance
        val resolvedShape = if (hasBottomClearance) RoundedCornerShape(24.dp) else shape
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .onPreviewKeyEvent { keyEvent ->
                    val isKeyDown = keyEvent.type == KeyEventType.KeyDown
                    val isEscape = keyEvent.key == Key.Escape
                    val shouldDismiss = if (isKeyDown) isEscape else false
                    if (shouldDismiss) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                }
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(
                    start = resolvedHorizontalPadding,
                    top = outerPadding,
                    end = resolvedHorizontalPadding,
                    bottom = resolvedBottomPadding,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetSlideModifier)
                    .clip(resolvedShape)
                    .background(RfThemeState.currentPalette.surface)
                    .clickable(
                        interactionSource = sheetInteractionSource,
                        indication = null,
                        onClick = {},
                    )
                    .padding(horizontal = innerHorizontalPadding, vertical = innerVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                content = content,
            )
        }
    }
}

/**
 * Returns the border color for an admin input field reflecting active validation state.
 */
private fun adminFieldBorderColor(hasError: Boolean): Color = if (hasError) RfColors.Error else RfThemeState.currentPalette.fieldBorder

/**
 * Renders a compact admin validation banner for form-level or submission errors.
 */
@Composable
internal fun AdminFormErrorBanner(errorMessage: String?) {
    if (errorMessage.isNullOrBlank()) return
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val bgColor = if (palette.isDark) Color(0xFF3D1924) else rfAdminDangerPillBg()
    val inkColor = if (palette.isDark) Color(0xFFFF7A96) else rfAdminDangerPillInk()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        BasicText(
            text = errorMessage,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = inkColor,
            ),
        )
    }
}

/**
 * Renders an admin text field with optional label, prefix/suffix, input masking,
 * error border highlight, and 2000ms idle debounced validation feedback.
 */
@Composable
internal fun AdminDebouncedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: (String) -> String?,
    hasInteracted: Boolean,
    onInteracted: () -> Unit,
    forceShowError: Boolean,
    label: String? = null,
    placeholder: String = "",
    prefixText: String? = null,
    suffixText: String? = null,
    leadingIcon: RfIconKind? = null,
    inputFilter: ((String) -> String)? = null,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    useInputShadow: Boolean = false,
    containerColor: Color = RfThemeState.currentPalette.surface,
    cornerRadius: Dp = 14.dp,
    minHeight: Dp = 48.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 10.dp,
    textFontSize: TextUnit = 14.sp,
    textFontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = RfThemeState.currentPalette.ink,
    labelFontSize: TextUnit = 12.sp,
    labelColor: Color = RfThemeState.currentPalette.muted,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val blurState = rememberFieldBlurState()
    LaunchedEffect(hasInteracted) {
        if (!hasInteracted) {
            blurState.reset()
        }
    }
    val visibleError = rememberDebouncedValidationError(
        value = value,
        hasInteracted = hasInteracted,
        forceShowError = forceShowError,
        hasBlurred = blurState.hasBlurred,
        validator = validator,
    )
    val hasVisibleError = visibleError != null
    val shape = RoundedCornerShape(cornerRadius)
    val borderWidth = if (hasVisibleError) 1.5.dp else 1.dp
    val shadowModifier = if (useInputShadow) Modifier.rfInputShadow(shape, isAdmin = true) else Modifier
    val sizeModifier = if (singleLine) Modifier.height(minHeight) else Modifier.defaultMinSize(minHeight = minHeight)
    val boxAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            BasicText(
                text = label,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = labelColor,
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(sizeModifier)
                .then(shadowModifier)
                .clip(shape)
                .background(containerColor)
                .border(borderWidth, adminFieldBorderColor(hasVisibleError), shape)
                .padding(horizontal = horizontalPadding, vertical = if (singleLine) 0.dp else verticalPadding),
            contentAlignment = boxAlignment,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (leadingIcon != null) {
                    RfIcon(
                        kind = leadingIcon,
                        tint = if (hasVisibleError) RfColors.Error else rfAdminIndigoPillInk(),
                        size = 16.dp,
                    )
                }
                if (!prefixText.isNullOrBlank()) {
                    BasicText(
                        text = prefixText,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = textFontSize,
                            fontWeight = FontWeight.Normal,
                            color = textColor,
                        ),
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = boxAlignment) {
                    val showPlaceholder = bothConditions(value.isEmpty(), placeholder.isNotEmpty())
                    if (showPlaceholder) {
                        BasicText(
                            text = placeholder,
                            maxLines = if (singleLine) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = textFontSize,
                                color = Color(0xFF8A8FA6),
                            ),
                        )
                    }
                    if (cursorMask != null) {
                        RfMaskedDigitBasicTextField(
                            value = value,
                            onValueChange = { masked ->
                                onInteracted()
                                onValueChange(masked)
                            },
                            maskTransform = cursorMask,
                            singleLine = singleLine,
                            keyboardOptions = keyboardOptions,
                            textStyle = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = textFontSize,
                                fontWeight = textFontWeight,
                                color = textColor,
                            ),
                            cursorBrush = SolidColor(rfAdminIndigoPillInk()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfTrackValidationFocus(focusKey = blurState) { isFocused ->
                                    blurState.onFocusStateChanged(isFocused, onBlurred = onInteracted)
                                },
                        )
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = { rawInput ->
                                onInteracted()
                                val filtered = if (inputFilter != null) inputFilter.invoke(rawInput) else rawInput
                                onValueChange(filtered)
                            },
                            singleLine = singleLine,
                            keyboardOptions = keyboardOptions,
                            cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                            textStyle = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = textFontSize,
                                fontWeight = textFontWeight,
                                color = textColor,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfTrackValidationFocus(focusKey = blurState) { isFocused ->
                                    blurState.onFocusStateChanged(isFocused, onBlurred = onInteracted)
                                },
                        )
                    }
                }
                if (!suffixText.isNullOrBlank()) {
                    BasicText(
                        text = suffixText,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = textFontSize,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )
                }
            }
        }
        RfValidationErrorText(errorMessage = visibleError)
    }
}

/**
 * Convenience overload of [AdminDebouncedInputField] backed by an [RfDebouncedFieldController].
 */
@Composable
internal fun AdminDebouncedInputField(
    controller: RfDebouncedFieldController,
    validator: (String) -> String?,
    label: String? = null,
    placeholder: String = "",
    prefixText: String? = null,
    suffixText: String? = null,
    leadingIcon: RfIconKind? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    useInputShadow: Boolean = false,
    containerColor: Color = RfThemeState.currentPalette.surface,
    cornerRadius: Dp = 14.dp,
    minHeight: Dp = 48.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 10.dp,
    textFontSize: TextUnit = 14.sp,
    textFontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = RfThemeState.currentPalette.ink,
    labelFontSize: TextUnit = 12.sp,
    labelColor: Color = RfThemeState.currentPalette.muted,
    modifier: Modifier = Modifier,
) {
    AdminDebouncedInputField(
        value = controller.value,
        onValueChange = { nextText -> controller.onValueChange(nextText) },
        validator = validator,
        hasInteracted = controller.hasInteracted,
        onInteracted = {},
        forceShowError = controller.forceShowError,
        label = label,
        placeholder = placeholder,
        prefixText = prefixText,
        suffixText = suffixText,
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        useInputShadow = useInputShadow,
        containerColor = containerColor,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        textFontSize = textFontSize,
        textFontWeight = textFontWeight,
        textColor = textColor,
        labelFontSize = labelFontSize,
        labelColor = labelColor,
        modifier = modifier,
    )
}

/**
 * Filters a bank reference string to allowed alphanumeric and punctuation tokens within max length.
 */
private fun filterBankReferenceInput(rawValue: String): String {
    val allowedPunctuation = setOf('-', '_', '.', '/', ' ')
    val builder = StringBuilder()
    for (character in rawValue) {
        if (builder.length >= InputValidator.MAX_BANK_REFERENCE_LENGTH) break
        val isPunctuation = character in allowedPunctuation
        val isAllowed = eitherCondition(character.isLetterOrDigit(), isPunctuation)
        if (isAllowed) {
            builder.append(character)
        }
    }
    return builder.toString()
}

/**
 * Filters a catalog SKU input to uppercase alphanumeric characters and hyphens.
 */
private fun filterArticleSkuInput(rawValue: String): String {
    val builder = StringBuilder()
    for (character in rawValue) {
        if (builder.length >= 20) break
        val isDashOrUnderscore = eitherCondition(character == '-', character == '_')
        val isAllowed = eitherCondition(character.isLetterOrDigit(), isDashOrUnderscore)
        if (isAllowed) {
            builder.append(character.uppercaseChar())
        }
    }
    return builder.toString()
}

/**
 * Validates an article SKU for length, allowed characters, and uniqueness across existing catalog items.
 */
private fun validateArticleSku(
    rawCode: String,
    allArticles: List<CatalogArticle>,
    editingArticleId: String?,
): String? {
    val cleanCode = rawCode.trim()
    val entityError = InputValidator.validateEntityName(
        rawValue = cleanCode,
        fieldLabel = "El código SKU",
        minLength = 3,
        maxLength = 20,
        isRequired = true,
    )
    if (entityError != null) return entityError
    val hasInvalidChar = cleanCode.any { character ->
        val isDashOrUnderscore = eitherCondition(character == '-', character == '_')
        val isAllowed = eitherCondition(character.isLetterOrDigit(), isDashOrUnderscore)
        !isAllowed
    }
    if (hasInvalidChar) {
        return "El código SKU solo admite letras, números y guiones."
    }
    val hasDuplicate = allArticles.any { articleItem ->
        val isSameCode = articleItem.code.equals(cleanCode, ignoreCase = true)
        val isDifferentRecord = articleItem.id != editingArticleId
        bothConditions(isSameCode, isDifferentRecord)
    }
    if (hasDuplicate) {
        return "Ya existe un artículo con el código SKU $cleanCode."
    }
    return null
}

/**
 * Normalizes a Spanish short date string such as `10 nov 2026` into `DD/MM/YYYY` for [RfMaskedDateField].
 */
private fun normalizePaymentDateToSlash(rawDate: String): String {
    val trimmed = rawDate.trim()
    if (trimmed.isEmpty()) return ""
    val slashParts = trimmed.split("/")
    if (slashParts.size == 3) return trimmed
    val monthLookup = mapOf(
        "ene" to "01",
        "feb" to "02",
        "mar" to "03",
        "abr" to "04",
        "may" to "05",
        "jun" to "06",
        "jul" to "07",
        "ago" to "08",
        "sep" to "09",
        "oct" to "10",
        "nov" to "11",
        "dic" to "12",
    )
    val tokens = trimmed.split(" ").filter { token -> token.isNotBlank() }
    if (tokens.size >= 3) {
        val dayNum = tokens[0].toIntOrNull()
        val monthCode = monthLookup[tokens[1].lowercase().trimEnd('.')]
        val yearNum = tokens[2].toIntOrNull()
        val hasDayAndMonth = bothConditions(dayNum != null, monthCode != null)
        if (hasDayAndMonth) {
            if (yearNum != null) {
                val paddedDay = dayNum.toString().padStart(2, '0')
                return "$paddedDay/$monthCode/$yearNum"
            }
        }
    }
    return trimmed
}

private fun photoKeyForAsset(photo: PhotoAsset): RfPhotoKey = when (photo) {
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

private fun photoKeyForEventType(eventType: String): RfPhotoKey {
    val normalized = eventType.lowercase()
    return when {
        normalized.contains("boda") -> RfPhotoKey.BODA
        normalized.contains("quince") -> RfPhotoKey.QUINCEANERA
        normalized.contains("baby") -> RfPhotoKey.BABY_SHOWER
        normalized.contains("grad") -> RfPhotoKey.GRADUACION
        normalized.contains("corp") -> RfPhotoKey.CORPORATIVO
        else -> RfPhotoKey.CUMPLE_INFANTIL
    }
}

private fun isExpiredRequestStatus(rawStatus: String): Boolean {
    val normalized = rawStatus.trim().uppercase()
    val isEnglishExpired = normalized == "EXPIRED"
    val isSpanishExpired = normalized == "VENCIDA"
    return eitherCondition(isEnglishExpired, isSpanishExpired)
}

private fun isQuotedRequestStatus(rawStatus: String): Boolean {
    val normalized = rawStatus.trim().uppercase()
    val isSentOrQuoted = eitherCondition(normalized == "SENT", normalized == "QUOTED")
    val isSpanishQuoted = eitherCondition(normalized == "COTIZADA", normalized.contains("COTIZACI"))
    val isApproved = normalized == "APPROVED"
    val matchesSentGroup = eitherCondition(isSentOrQuoted, isSpanishQuoted)
    return eitherCondition(matchesSentGroup, isApproved)
}

private fun buildStarterLinesForLead(
    lead: LeadRecord,
    catalog: List<CatalogArticle>,
): List<EventLineItem> {
    if (catalog.isEmpty()) {
        return emptyList()
    }
    val chairArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-silla-tiffany" }
        ?: catalog.first()
    val tableArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-mesa-redonda" }
        ?: catalog.getOrNull(1)
        ?: chairArticle
    val guestChairs = lead.guests.coerceAtLeast(20)
    val tableCount = ((guestChairs + 7) / 8).coerceAtLeast(2)
    val baseLines = mutableListOf(
        EventLineItem(
            articleCode = chairArticle.code,
            name = chairArticle.name,
            quantity = guestChairs,
            unitPriceDop = chairArticle.pricePerDayDop,
            photo = chairArticle.photo,
        ),
    )
    if (tableArticle.id != chairArticle.id) {
        baseLines.add(
            EventLineItem(
                articleCode = tableArticle.code,
                name = tableArticle.name,
                quantity = tableCount,
                unitPriceDop = tableArticle.pricePerDayDop,
                photo = tableArticle.photo,
            ),
        )
    }
    val archArticle = catalog.firstOrNull { articleItem -> articleItem.id == "art-arco-rosa" }
    if (archArticle != null) {
        baseLines.add(
            EventLineItem(
                articleCode = archArticle.code,
                name = archArticle.name,
                quantity = 1,
                unitPriceDop = archArticle.pricePerDayDop,
                photo = archArticle.photo,
            ),
        )
    }
    return baseLines
}

private fun primeQuoteDraftFromQuote(
    quote: QuoteRecord,
    fallbackVenue: String,
    phone: String = "",
) {
    val resolvedVenue = quote.venue.trim().ifBlank { fallbackVenue }
    adminQuoteDraftFlow.value = AdminQuoteDraftState(
        sourceKey = "QUOTE:${quote.id}",
        sourceKind = AdminQuoteSourceKind.QUOTE,
        clientName = quote.clientName,
        clientEmail = quote.clientEmail,
        clientPhone = phone,
        eventTitle = quote.eventTitle,
        dateLabel = quote.dateLabel,
        guestsCount = quote.guests,
        venue = resolvedVenue,
        lines = quote.lines,
        deliveryFeeText = quote.deliveryFeeDop.toString(),
        discountText = quote.discountDop.toString(),
        notes = quote.notes,
    )
}

private fun primeQuoteDraftFromLead(
    lead: LeadRecord,
    fallbackVenue: String,
    catalog: List<CatalogArticle>,
) {
    val starterLines = buildStarterLinesForLead(lead, catalog)
    val noteText = lead.followUpNote.trim().ifBlank {
        "Propuesta comercial para ${lead.eventType} (${lead.guests} invitados)."
    }
    adminQuoteDraftFlow.value = AdminQuoteDraftState(
        sourceKey = "LEAD:${lead.id}",
        sourceKind = AdminQuoteSourceKind.LEAD,
        clientName = lead.clientName,
        clientEmail = lead.email,
        clientPhone = lead.phone,
        eventTitle = "${lead.eventType} · ${lead.clientName.substringBefore(" ")}",
        dateLabel = lead.dateLabel,
        guestsCount = lead.guests,
        venue = fallbackVenue,
        lines = starterLines,
        deliveryFeeText = "350",
        discountText = "0",
        notes = noteText,
    )
}

private fun appendCatalogArticleToAdminQuoteDraft(article: CatalogArticle) {
    adminQuoteDraftFlow.update { currentDraft ->
        val existingIndex = currentDraft.lines.indexOfFirst { lineItem ->
            eitherCondition(
                lineItem.articleCode.equals(article.code, ignoreCase = true),
                lineItem.name.equals(article.name, ignoreCase = true),
            )
        }
        val nextLines = if (existingIndex >= 0) {
            currentDraft.lines.mapIndexed { lineIndex, lineItem ->
                if (lineIndex == existingIndex) {
                    lineItem.copy(quantity = lineItem.quantity + 1)
                } else {
                    lineItem
                }
            }
        } else {
            currentDraft.lines + EventLineItem(
                articleCode = article.code,
                name = article.name,
                quantity = 1,
                unitPriceDop = article.pricePerDayDop,
                photo = article.photo,
            )
        }
        currentDraft.copy(lines = nextLines)
    }
}

// ==============================================================================
// A01 · INICIO (ADMIN DASHBOARD MÓVIL)
// ==============================================================================
@Composable
private fun AdminHeroWeddingRingsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Circle 1: cx="49" cy="70" r="21" stroke="#D9A441" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFD9A441),
                radius = 21f,
                center = Offset(49f, 70f),
                style = Stroke(width = 6.5f),
            )
            // Circle 2: cx="72" cy="72" r="21" stroke="#F0CC7A" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFF0CC7A),
                radius = 21f,
                center = Offset(72f, 72f),
                style = Stroke(width = 6.5f),
            )
            // Overlapping arc: M63.5 52.6A21 21 0 0 1 69 62
            val arcPath = PathParser().parsePathString("M63.5 52.6A21 21 0 0 1 69 62").toPath()
            drawPath(arcPath, color = Color(0xFFD9A441), style = Stroke(width = 6.5f, cap = StrokeCap.Round))

            // Diamond on ring: M42 44L49 37L56 44L49 53Z
            val diamondPath = PathParser().parsePathString("M42 44L49 37L56 44L49 53Z").toPath()
            drawPath(diamondPath, color = Color(0xFFA8D8F5))
            drawPath(diamondPath, color = Color.White, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

            // Star 1: M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z
            val star1 = PathParser().parsePathString("M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z").toPath()
            drawPath(star1, color = Color(0xFFEC4F97))

            // Star 2: M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z
            val star2 = PathParser().parsePathString("M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z").toPath()
            drawPath(star2, color = Color(0xFF7650E8))

            // Sparkle: M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z
            val sparkle = PathParser().parsePathString("M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z").toPath()
            drawPath(sparkle, color = Color(0xFFF9B6D3))
        }
    }
}

private data class AdminHomeAlertRow(
    val title: String,
    val subtitle: String,
    val trailingBadge: String,
    val isBadgePill: Boolean,
    val iconKind: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val onTap: () -> Unit,
)

internal fun resolveAdminFirstName(rawProfileName: String): String {
    val trimmed = rawProfileName.trim()
    val isBlankName = trimmed.isEmpty()
    val isGuestLabel = trimmed.startsWith("Invitado", ignoreCase = true)
    val shouldFallbackToMaria = eitherCondition(isBlankName, isGuestLabel)
    if (shouldFallbackToMaria) {
        return "María"
    }
    return trimmed.substringBefore(" ")
}

private fun buildHeroTimingBadge(eventItem: ClientEvent): String {
    val readableDate = formatReadableSpanishDateText(eventItem.dateLabel)
    return when {
        eventItem.daysUntilEvent == 0 -> "Hoy · $readableDate"
        eventItem.daysUntilEvent == 1 -> "Mañana · $readableDate"
        eventItem.daysUntilEvent > 1 -> "$readableDate · en ${eventItem.daysUntilEvent} días"
        else -> readableDate
    }
}

@Composable
internal fun RenderAdminHomeA01(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val rawProfileName = viewModel?.profileName?.collectAsState()?.value ?: "María"
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val allPayments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val allQuotes = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val allLeads = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val allNotifications = viewModel?.notifications?.collectAsState()?.value ?: emptyList()
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings

    val adminFirstName = resolveAdminFirstName(rawProfileName)
    val unreadNotificationsCount = if (viewModel != null) {
        allNotifications.count { notificationItem -> !notificationItem.isRead }
    } else {
        3
    }

    val activeEvents = allEvents.filter { eventItem ->
        val notCompleted = eventItem.status != EventStatus.COMPLETED
        val notCancelled = eventItem.status != EventStatus.CANCELLED
        bothConditions(notCompleted, notCancelled)
    }
    val heroEvent = activeEvents.firstOrNull() ?: allEvents.firstOrNull()

    val collectedPaymentsDop = allPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val totalRevenueDop = if (collectedPaymentsDop > 0) {
        collectedPaymentsDop
    } else {
        metrics.octoberRevenueDop
    }
    val activeEventsCount = if (allEvents.isNotEmpty()) {
        activeEvents.size
    } else {
        metrics.activeEventsCount
    }

    val pendingQuotes = allQuotes.filter { quoteItem ->
        quoteItem.status.equals("PENDING", ignoreCase = true)
    }
    val unquotedLeads = allLeads.filter { leadItem ->
        val notQuoted = !isQuotedRequestStatus(leadItem.stage)
        val notExpired = !isExpiredRequestStatus(leadItem.stage)
        bothConditions(notQuoted, notExpired)
    }
    val quotedClientNames = pendingQuotes.map { quoteItem -> quoteItem.clientName.lowercase() }.toSet()
    val distinctNewLeads = unquotedLeads.filter { leadItem ->
        leadItem.clientName.lowercase() !in quotedClientNames
    }
    val pendingRequestsCount = pendingQuotes.size + distinctNewLeads.size
    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"

    val alertRows = remember(allEvents, pendingQuotes, distinctNewLeads, allArticles, fallbackVenue) {
        val builtRows = mutableListOf<AdminHomeAlertRow>()
        val unpaidEvents = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
        val topUnpaidEvent = unpaidEvents.firstOrNull()
        if (topUnpaidEvent != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Pago restante · ${topUnpaidEvent.title}",
                    subtitle = "Vence ${topUnpaidEvent.balanceDueDateLabel} · ${topUnpaidEvent.clientName}",
                    trailingBadge = formatAdminDop(topUnpaidEvent.balanceDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.WALLET,
                    iconBg = rfAdminWarnPillBg(),
                    iconTint = rfAdminWarnPillInk(),
                    onTap = {
                        viewModel?.selectEvent(topUnpaidEvent.id)
                        onSelectScreenCode("A21")
                    },
                ),
            )
        }
        val topPendingQuote = pendingQuotes.firstOrNull()
        if (topPendingQuote != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Cotización · ${topPendingQuote.eventTitle}",
                    subtitle = "${topPendingQuote.clientName} · ${formatReadableSpanishDateText(topPendingQuote.dateLabel)}",
                    trailingBadge = formatAdminDop(topPendingQuote.totalDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.ORDERS,
                    iconBg = rfAdminIndigoPillBg(),
                    iconTint = rfAdminIndigoPillInk(),
                    onTap = {
                        primeQuoteDraftFromQuote(topPendingQuote, fallbackVenue)
                        viewModel?.selectQuote(topPendingQuote.id)
                        onSelectScreenCode("A03")
                    },
                ),
            )
        }
        val topNewLead = distinctNewLeads.firstOrNull()
        if (topNewLead != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Solicitud nueva · ${topNewLead.clientName}",
                    subtitle = "${topNewLead.eventType} · ${formatReadableSpanishDateText(topNewLead.dateLabel)} · ${topNewLead.channel}",
                    trailingBadge = "Cotizar",
                    isBadgePill = true,
                    iconKind = RfIconKind.SPARK,
                    iconBg = RfColors.VioletaTint,
                    iconTint = RfColors.VioletaTintInk,
                    onTap = {
                        primeQuoteDraftFromLead(topNewLead, fallbackVenue, allArticles)
                        viewModel?.selectLead(topNewLead.id)
                        onSelectScreenCode("A03")
                    },
                ),
            )
        }
        val secondUnpaidEvent = unpaidEvents.drop(1).firstOrNull()
        if (secondUnpaidEvent != null) {
            builtRows.add(
                AdminHomeAlertRow(
                    title = "Expediente · ${secondUnpaidEvent.title}",
                    subtitle = "${secondUnpaidEvent.clientName} · ${formatReadableSpanishDateText(secondUnpaidEvent.dateLabel)}",
                    trailingBadge = formatAdminDop(secondUnpaidEvent.balanceDop),
                    isBadgePill = false,
                    iconKind = RfIconKind.CALENDAR,
                    iconBg = rfAdminSuccessPillBg(),
                    iconTint = rfAdminSuccessPillInk(),
                    onTap = {
                        viewModel?.selectEvent(secondUnpaidEvent.id)
                        onSelectScreenCode("A13")
                    },
                ),
            )
        }
        builtRows.take(3)
    }

    val adminHomeTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = adminHomeTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (includeHeader) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectScreenCode("s54") },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfColors.IndigoTint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfLogo(size = RfLogoSize.MEDIUM)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BasicText(
                                text = "Rosa Fiesta",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                            )
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .background(rfAdminIndigoPillBg(), CircleShape)
                                    .padding(horizontal = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Admin",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                                )
                            }
                        }
                        BasicText(
                            text = "Hola, $adminFirstName",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 24.sp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onSelectScreenCode("A29") },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BELL, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-3).dp)
                                .defaultMinSize(minWidth = 20.dp)
                                .height(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "$unreadNotificationsCount",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                            )
                        }
                    }
                }
            }
        }

        val isHeroMissing = heroEvent == null
        val isEmptyLiveHero = if (isHeroMissing) viewModel != null else false
        val heroTitle = when {
            heroEvent != null -> heroEvent.title
            isEmptyLiveHero -> "Sin eventos programados"
            else -> "Boda Real Rosa & Dorado"
        }
        val heroBadge = when {
            heroEvent != null -> buildHeroTimingBadge(heroEvent)
            isEmptyLiveHero -> "Agenda disponible"
            else -> "Este viernes · en 3 días"
        }
        val heroSubtitle = when {
            heroEvent != null -> "${heroEvent.clientName} · ${heroEvent.venue}"
            isEmptyLiveHero -> "Toca para crear tu primer evento o cotización"
            else -> "Montaje 2:00 p. m. · Jardín Botánico"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = true)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        0.0f to Color(0xFF7C5CF0),
                        0.55f to Color(0xFF5B6CF0),
                        1.0f to Color(0xFF2FB8C8),
                    ),
                )
                .clickable {
                    val targetHeroId = heroEvent?.id
                    if (targetHeroId != null) {
                        viewModel?.selectEvent(targetHeroId)
                        onSelectScreenCode("A13")
                    } else if (viewModel != null) {
                        onSelectScreenCode("A04")
                    } else {
                        onSelectScreenCode("A13")
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleFactor = size.width / 350f
                scale(scaleFactor, scaleFactor, Offset.Zero) {
                    drawCircle(color = Color.White.copy(alpha = 0.1f), radius = 70f, center = Offset(300f, 20f))
                    drawCircle(color = Color(0xFFD6EA8C), radius = 4f, center = Offset(190f, 52f))
                    val starPath = PathParser().parsePathString("M0 -10L2.35 -3.24L9.5 -3.1L3.8 1.24L5.9 8.1L0 4L-5.9 8.1L-3.8 1.24L-9.5 -3.1L-2.35 -3.24Z").toPath()
                    translate(214f, 30f) {
                        scale(0.8f, 0.8f, Offset.Zero) {
                            drawPath(starPath, color = Color(0xFFFFD45C))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 18.dp)
                    .size(110.dp),
            ) {
                AdminHeroWeddingRingsIllustration(modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = heroBadge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A3464)),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BasicText(
                    text = heroTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 210.dp),
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 23.sp),
                )

                Spacer(modifier = Modifier.height(2.dp))

                BasicText(
                    text = heroSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFE9EEFF)),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A20") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Ingresos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.muted))
                    BasicText(
                        text = formatAdminDop(totalRevenueDop),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A11") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Eventos activos", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.muted))
                    BasicText("$activeEventsCount", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(rfAdminWarnPillBg())
                    .clickable { onSelectScreenCode("A02") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Solicitudes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminWarnPillInk()))
                    BasicText("$pendingRequestsCount nuevas", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rfAdminWarnPillInk()))
                }
            }
        }

        BasicText(
            text = "CREAR",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = RfThemeState.currentPalette.muted),
            modifier = Modifier.padding(top = 2.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().offset(y = (-6).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val rosaCreateGradient = if (RfThemeState.currentPalette.isDark) {
                    Brush.linearGradient(listOf(rfAdminIndigoPillBg(), rfAdminTealPillBg()))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(rosaCreateGradient)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A08") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 36.dp, forceAdminPalette = true)
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear con Rosa IA", maxLines = 1, softWrap = false, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                            BasicText("Arma el borrador", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.startNewManualEvent()
                            onSelectScreenCode("A05")
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(rfAdminIndigoPillBg()),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = rfAdminIndigoPillInk(), size = 20.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear evento", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink, lineHeight = 17.sp))
                            BasicText("Paso a paso", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.startEditingArticle(null)
                            onSelectScreenCode("A10")
                        }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(rfAdminSuccessPillBg()),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = rfAdminSuccessPillInk(), size = 20.dp)
                        }
                        BasicText("Agregar\nartículo", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink, lineHeight = 17.sp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A40") }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(rfAdminTealPillBg()),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CATALOG, tint = rfAdminTealPillInk(), size = 20.dp)
                        }
                        BasicText("Crear\ncategoría", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink, lineHeight = 17.sp))
                    }
                }
            }
        }

        BasicText(
            text = "POR ATENDER",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = RfThemeState.currentPalette.muted),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-6).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (alertRows.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = rfAdminSuccessPillInk(), size = 20.dp)
                        BasicText(
                            text = "Todo al día. No hay alertas pendientes.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                } else {
                    alertRows.forEachIndexed { alertIndex, alertItem ->
                        if (alertIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { alertItem.onTap() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(alertItem.iconBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = alertItem.iconKind, tint = alertItem.iconTint, size = 20.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = alertItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = alertItem.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            if (alertItem.isBadgePill) {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(alertItem.iconBg)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = alertItem.trailingBadge,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = alertItem.iconTint),
                                    )
                                }
                            } else {
                                BasicText(
                                    text = alertItem.trailingBadge,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AdminRequestCardModel(
    val key: String,
    val title: String,
    val clientName: String,
    val dateLabel: String,
    val guests: Int,
    val articlesCountLabel: String,
    val estimatedDop: Int,
    val channelBadge: String,
    val rawStatus: String,
    val bucket: String,
    val phone: String,
    val photoKey: RfPhotoKey,
    val quoteRecord: QuoteRecord?,
    val leadRecord: LeadRecord?,
)

@Composable
internal fun RenderAdminRequestsA02(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"

    val requestCards = remember(quotesList, leadsList) {
        val unifiedList = mutableListOf<AdminRequestCardModel>()
        val coveredClients = mutableSetOf<String>()

        for (quoteItem in quotesList) {
            val matchedLead = leadsList.firstOrNull { leadItem ->
                leadItem.clientName.equals(quoteItem.clientName, ignoreCase = true)
            }
            coveredClients.add(quoteItem.clientName.trim().lowercase())
            val isLeadQuotedStage = if (matchedLead != null) {
                isQuotedRequestStatus(matchedLead.stage)
            } else {
                false
            }
            val effectiveRawStatus = if (isExpiredRequestStatus(quoteItem.status)) {
                quoteItem.status
            } else if (isLeadQuotedStage) {
                matchedLead?.stage ?: quoteItem.status
            } else {
                quoteItem.status
            }
            val bucket = when {
                isExpiredRequestStatus(effectiveRawStatus) -> "EXPIRED"
                isQuotedRequestStatus(effectiveRawStatus) -> "QUOTED"
                else -> "NEW"
            }
            val firstLinePhoto = quoteItem.lines.firstOrNull()?.photo
            val resolvedPhoto = if (firstLinePhoto != null) {
                photoKeyForAsset(firstLinePhoto)
            } else {
                photoKeyForEventType(quoteItem.eventTitle)
            }
            val channelText = matchedLead?.channel?.let { channelName -> "Vía $channelName" } ?: "Con Rosa IA"
            unifiedList.add(
                AdminRequestCardModel(
                    key = "quote-${quoteItem.id}",
                    title = quoteItem.eventTitle,
                    clientName = quoteItem.clientName,
                    dateLabel = quoteItem.dateLabel,
                    guests = quoteItem.guests,
                    articlesCountLabel = "${quoteItem.lines.size} artículos",
                    estimatedDop = quoteItem.totalDop,
                    channelBadge = channelText,
                    rawStatus = effectiveRawStatus,
                    bucket = bucket,
                    phone = matchedLead?.phone.orEmpty(),
                    photoKey = resolvedPhoto,
                    quoteRecord = quoteItem,
                    leadRecord = matchedLead,
                ),
            )
        }

        for (leadItem in leadsList) {
            val normalizedClient = leadItem.clientName.trim().lowercase()
            if (normalizedClient !in coveredClients) {
                val bucket = when {
                    isExpiredRequestStatus(leadItem.stage) -> "EXPIRED"
                    isQuotedRequestStatus(leadItem.stage) -> "QUOTED"
                    else -> "NEW"
                }
                unifiedList.add(
                    AdminRequestCardModel(
                        key = "lead-${leadItem.id}",
                        title = "${leadItem.eventType} · ${leadItem.clientName.substringBefore(" ")}",
                        clientName = leadItem.clientName,
                        dateLabel = leadItem.dateLabel,
                        guests = leadItem.guests,
                        articlesCountLabel = leadItem.stage,
                        estimatedDop = leadItem.estimatedDop,
                        channelBadge = "Vía ${leadItem.channel}",
                        rawStatus = leadItem.stage,
                        bucket = bucket,
                        phone = leadItem.phone,
                        photoKey = photoKeyForEventType(leadItem.eventType),
                        quoteRecord = null,
                        leadRecord = leadItem,
                    ),
                )
            }
        }
        unifiedList
    }

    val totalCount = requestCards.size
    val newCount = requestCards.count { cardItem -> cardItem.bucket == "NEW" }
    val quotedCount = requestCards.count { cardItem -> cardItem.bucket == "QUOTED" }
    val expiredCount = requestCards.count { cardItem -> isExpiredRequestStatus(cardItem.rawStatus) }

    var selectedRequestTab by remember { mutableStateOf(0) }
    val requestTabs = listOf(
        "Todas ($totalCount)",
        "Nuevas ($newCount)",
        "Cotizadas ($quotedCount)",
        "Vencidas ($expiredCount)",
    )

    val filteredCards = when (selectedRequestTab) {
        1 -> requestCards.filter { cardItem -> cardItem.bucket == "NEW" }
        2 -> requestCards.filter { cardItem -> cardItem.bucket == "QUOTED" }
        3 -> requestCards.filter { cardItem -> isExpiredRequestStatus(cardItem.rawStatus) }
        else -> requestCards
    }

    val requestsTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = requestsTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (includeHeader) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Solicitudes",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "Cotizaciones y prospectos entrantes",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            requestTabs.forEachIndexed { tabIndex, tabTitle ->
                val isSelectedTab = selectedRequestTab == tabIndex
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (isSelectedTab) {
                                Modifier
                                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4), ambientColor = Color.Transparent)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.surface)
                            } else {
                                Modifier.clip(RoundedCornerShape(12.dp))
                            },
                        )
                        .clickable { selectedRequestTab = tabIndex }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = tabTitle,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSelectedTab) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelectedTab) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                        ),
                    )
                }
            }
        }

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = rfAdminIndigoPillInk(), size = 24.dp)
                    BasicText(
                        text = if (selectedRequestTab == 3) "No hay solicitudes vencidas" else "Sin solicitudes en este filtro",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = "Todas las cotizaciones activas están vigentes.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                    )
                }
            }
        } else {
            filteredCards.forEach { requestItem ->
                val openQuoteBuilder: () -> Unit = {
                    val linkedQuote = requestItem.quoteRecord
                    val linkedLead = requestItem.leadRecord
                    if (linkedQuote != null) {
                        primeQuoteDraftFromQuote(linkedQuote, fallbackVenue, requestItem.phone)
                        viewModel?.selectQuote(linkedQuote.id)
                    } else if (linkedLead != null) {
                        primeQuoteDraftFromLead(linkedLead, fallbackVenue, articlesList)
                        viewModel?.selectLead(linkedLead.id)
                    }
                    onSelectScreenCode("A03")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable { openQuoteBuilder() },
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                            ) {
                                RfPhoto(photoKey = requestItem.photoKey, contentDescription = requestItem.title, modifier = Modifier.fillMaxSize())
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(
                                    text = requestItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = "${requestItem.clientName} · ${formatReadableSpanishDateText(requestItem.dateLabel)}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .background(RfColors.VioletaTint, CircleShape)
                                            .padding(horizontal = 9.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = requestItem.channelBadge,
                                            maxLines = 1,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RfColors.VioletaTintInk),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .background(RfThemeState.currentPalette.chipBg, CircleShape)
                                            .padding(horizontal = 9.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "${requestItem.guests} invitados",
                                            maxLines = 1,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = requestItem.articlesCountLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                )
                                Row {
                                    BasicText(
                                        "est. ",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                        modifier = Modifier.alignByBaseline(),
                                    )
                                    BasicText(
                                        text = formatAdminDop(requestItem.estimatedDop),
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                                        modifier = Modifier.alignByBaseline(),
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(rfAdminSuccessPillBg())
                                    .border(1.dp, Color(0xFFE0EDBF), CircleShape)
                                    .clickable {
                                        viewModel?.selectChatThreadForContact(requestItem.clientName)
                                        if (requestItem.phone.isNotBlank()) {
                                            viewModel?.openWhatsAppChat(
                                                rawPhone = requestItem.phone,
                                                message = "Hola ${requestItem.clientName}, revisamos tu solicitud de ${requestItem.title}.",
                                            )
                                        }
                                        onSelectScreenCode("A15")
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.WHATSAPP, tint = rfAdminSuccessPillInk(), size = 18.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                    .clickable { openQuoteBuilder() }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    BasicText("Cotizar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 16.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RfIcon(kind = RfIconKind.INFO, tint = RfThemeState.currentPalette.muted, size = 16.dp)
            BasicText(
                text = "Las solicitudes llegan desde Rosa IA, Instagram, Web o WhatsApp.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
            )
        }
    }
}

private fun formatAdminDop(amount: Int): String {
    if (amount < 1000) {
        return "RD$ $amount"
    }
    val thousands = amount / 1000
    val remainder = (amount % 1000).toString().padStart(3, '0')
    return "RD$ $thousands,$remainder"
}

@Composable
internal fun RenderAdminQuoteBuilderA03(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val selectedQuoteId = viewModel?.selectedQuoteId?.collectAsState()?.value.orEmpty()
    val selectedLeadId = viewModel?.selectedLeadId?.collectAsState()?.value.orEmpty()
    val formError = viewModel?.formError?.collectAsState()?.value
    val draftState by adminQuoteDraftFlow.collectAsState()

    val fallbackVenue = businessSettings.zones.firstOrNull()?.name ?: "Jardín Los Rosales"
    val explicitQuote = quotesList.firstOrNull { quoteItem -> quoteItem.id == selectedQuoteId }
    val explicitLead = leadsList.firstOrNull { leadItem -> leadItem.id == selectedLeadId }
    val resolvedQuote = viewModel?.selectedQuote() ?: if (viewModel == null) CanonicalSampleData.quotes[1] else quotesList.firstOrNull()
    val resolvedLead = viewModel?.selectedLead()

    LaunchedEffect(selectedQuoteId, selectedLeadId) {
        val currentKey = adminQuoteDraftFlow.value.sourceKey
        val expectedQuoteKey = if (explicitQuote != null) "QUOTE:${explicitQuote.id}" else ""
        val expectedLeadKey = if (explicitLead != null) "LEAD:${explicitLead.id}" else ""
        val alreadyMatchesQuote = bothConditions(expectedQuoteKey.isNotEmpty(), currentKey == expectedQuoteKey)
        val alreadyMatchesLead = bothConditions(expectedLeadKey.isNotEmpty(), currentKey == expectedLeadKey)
        val alreadyInitializedForSelection = eitherCondition(alreadyMatchesQuote, alreadyMatchesLead)
        if (!alreadyInitializedForSelection) {
            if (explicitQuote != null) {
                primeQuoteDraftFromQuote(explicitQuote, fallbackVenue)
            } else if (explicitLead != null) {
                primeQuoteDraftFromLead(explicitLead, fallbackVenue, articlesList)
            } else if (currentKey.isEmpty()) {
                val selectedQuoteOrNull = viewModel?.selectedQuote()
                if (selectedQuoteOrNull != null) {
                    primeQuoteDraftFromQuote(selectedQuoteOrNull, fallbackVenue)
                } else if (resolvedLead != null) {
                    primeQuoteDraftFromLead(resolvedLead, fallbackVenue, articlesList)
                } else if (resolvedQuote != null) {
                    primeQuoteDraftFromQuote(resolvedQuote, fallbackVenue)
                }
            }
        }
    }

    var localValidationError by remember { mutableStateOf<String?>(null) }
    var showQuickCatalogSheet by remember { mutableStateOf(false) }
    var hasVenueInteracted by remember(draftState.sourceKey) { mutableStateOf(false) }
    var hasDeliveryInteracted by remember(draftState.sourceKey) { mutableStateOf(false) }
    var hasNotesInteracted by remember(draftState.sourceKey) { mutableStateOf(false) }
    var forceQuoteValidation by remember(draftState.sourceKey) { mutableStateOf(false) }

    val validateQuoteVenue: (String) -> String? = { rawValue ->
        InputValidator.validateAddress(
            rawValue = rawValue,
            fieldLabel = "El lugar del evento",
            isRequired = true,
        )
    }
    val validateQuoteDelivery: (String) -> String? = { rawValue ->
        InputValidator.validateMoneyAmount(
            rawValue = rawValue,
            fieldLabel = "El transporte e instalación",
            isRequired = true,
            minAmount = 0.0,
            maxAmount = 500_000.0,
        )
    }
    val validateQuoteNotes: (String) -> String? = { rawValue ->
        InputValidator.validateNotes(
            rawValue = rawValue,
            fieldLabel = "Las notas",
            isRequired = false,
            minLength = 0,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    val activeLines = draftState.lines
    val articlesSubtotal = activeLines.sumOf { lineItem -> lineItem.subtotalDop }
    val parsedTransportDop = draftState.deliveryFeeText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val parsedDiscountDop = draftState.discountText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val totalQuoteDop = (articlesSubtotal + parsedTransportDop - parsedDiscountDop).coerceAtLeast(0)
    val activeArticleLinesCount = activeLines.size
    val displayClientName = draftState.clientName.ifBlank { resolvedQuote?.clientName.orEmpty() }
    val displayEventTitle = draftState.eventTitle.ifBlank { resolvedQuote?.eventTitle.orEmpty() }
    val displayDateLabel = draftState.dateLabel.ifBlank { resolvedQuote?.dateLabel.orEmpty() }
    val formattedDateLabel = formatReadableSpanishDateText(displayDateLabel)
    val displayVenue = draftState.venue.ifBlank { fallbackVenue }
    val clientInitial = InputValidator.extractInitials(displayClientName, fallback = "C", maxInitials = 1)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A02") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cotizar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "$displayEventTitle · $formattedDateLabel",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RfColors.VioletaTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RfColors.VioletaTintInk))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = displayClientName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = "${draftState.guestsCount} invitados · $displayVenue",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                                .clickable {
                                    viewModel?.selectChatThreadForContact(displayClientName)
                                    onSelectScreenCode("A15")
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = rfAdminSuccessPillInk(), size = 20.dp)
                        }
                    }

                    AdminDebouncedInputField(
                        value = draftState.venue,
                        onValueChange = { nextVenue ->
                            adminQuoteDraftFlow.update { stateItem -> stateItem.copy(venue = nextVenue) }
                            localValidationError = null
                        },
                        validator = validateQuoteVenue,
                        hasInteracted = hasVenueInteracted,
                        onInteracted = { hasVenueInteracted = true },
                        forceShowError = forceQuoteValidation,
                        placeholder = fallbackVenue,
                        leadingIcon = RfIconKind.LOCATION,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        containerColor = RfThemeState.currentPalette.chipBg,
                        cornerRadius = 12.dp,
                        minHeight = 42.dp,
                        textFontSize = 13.sp,
                        textColor = RfThemeState.currentPalette.ink,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "ARTÍCULOS",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = RfThemeState.currentPalette.muted),
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(rfAdminIndigoPillBg())
                        .clickable { showQuickCatalogSheet = !showQuickCatalogSheet }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = if (showQuickCatalogSheet) "Cerrar lista rápida" else "+ Catálogo rápido",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                    )
                }
            }

            AnimatedVisibility(visible = showQuickCatalogSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF9F9FE))
                        .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            text = "Toca un artículo para sumarlo a la cotización:",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                        )
                        articlesList.take(6).forEach { catalogItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RfThemeState.currentPalette.surface)
                                    .clickable {
                                        appendCatalogArticleToAdminQuoteDraft(catalogItem)
                                        localValidationError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = catalogItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "${formatAdminDop(catalogItem.pricePerDayDop)}/día · ${catalogItem.availableOct23} disp.",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                                RfIcon(kind = RfIconKind.PLUS, tint = rfAdminIndigoPillInk(), size = 18.dp)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
            ) {
                Column {
                    if (activeLines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Sin artículos seleccionados. Agrega piezas del inventario.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
                            )
                        }
                    } else {
                        activeLines.forEachIndexed { lineIndex, lineItem ->
                            if (lineIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                ) {
                                    RfPhoto(
                                        photoKey = photoKeyForAsset(lineItem.photo),
                                        contentDescription = lineItem.name,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    BasicText(
                                        text = lineItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, RfThemeState.currentPalette.fieldBorder, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = formatAdminDop(lineItem.unitPriceDop),
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.ink),
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .height(22.dp)
                                                .background(rfAdminSuccessPillBg(), CircleShape)
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = formatAdminDop(lineItem.subtotalDop),
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = rfAdminSuccessPillInk()),
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                adminQuoteDraftFlow.update { currentDraft ->
                                                    val nextQty = lineItem.quantity - 1
                                                    val updatedLines = if (nextQty <= 0) {
                                                        currentDraft.lines.filterIndexed { itemIndex, _ -> itemIndex != lineIndex }
                                                    } else {
                                                        currentDraft.lines.mapIndexed { itemIndex, existingLine ->
                                                            if (itemIndex == lineIndex) existingLine.copy(quantity = nextQty) else existingLine
                                                        }
                                                    }
                                                    currentDraft.copy(lines = updatedLines)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                    BasicText(
                                        text = "${lineItem.quantity}",
                                        modifier = Modifier.defaultMinSize(minWidth = 24.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                adminQuoteDraftFlow.update { currentDraft ->
                                                    val updatedLines = currentDraft.lines.mapIndexed { itemIndex, existingLine ->
                                                        if (itemIndex == lineIndex) existingLine.copy(quantity = existingLine.quantity + 1) else existingLine
                                                    }
                                                    currentDraft.copy(lines = updatedLines)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clickable {
                                viewModel?.setInventoryPickerForQuote(true)
                                onSelectScreenCode("A09")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = rfAdminIndigoPillInk(), size = 16.dp)
                        Spacer(Modifier.width(6.dp))
                        BasicText("Agregar del inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                    }
                }
            }

            val deliveryFeeError = rememberDebouncedValidationError(
                value = draftState.deliveryFeeText,
                hasInteracted = hasDeliveryInteracted,
                forceShowError = forceQuoteValidation,
                validator = validateQuoteDelivery,
            )
            val hasDeliveryError = deliveryFeeError != null
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(
                            width = if (hasDeliveryError) 1.5.dp else 1.dp,
                            color = adminFieldBorderColor(hasDeliveryError),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink))
                            BasicTextField(
                                value = draftState.deliveryFeeText,
                                onValueChange = { updatedText ->
                                    hasDeliveryInteracted = true
                                    val cleanDigits = InputValidator.filterDigitsOnly(updatedText, 7)
                                    adminQuoteDraftFlow.update { stateItem -> stateItem.copy(deliveryFeeText = cleanDigits) }
                                    localValidationError = null
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.widthIn(min = 32.dp, max = 76.dp),
                            )
                        }
                    }
                }
                RfValidationErrorText(errorMessage = deliveryFeeError)
            }

            AdminDebouncedInputField(
                value = draftState.notes,
                onValueChange = { updatedText ->
                    adminQuoteDraftFlow.update { stateItem -> stateItem.copy(notes = updatedText) }
                    localValidationError = null
                },
                validator = validateQuoteNotes,
                hasInteracted = hasNotesInteracted,
                onInteracted = { hasNotesInteracted = true },
                forceShowError = forceQuoteValidation,
                placeholder = "Notas operativas de montaje y logística",
                singleLine = false,
                useInputShadow = true,
                cornerRadius = 14.dp,
                minHeight = 70.dp,
                horizontalPadding = 14.dp,
                verticalPadding = 12.dp,
                textFontSize = 14.sp,
                textColor = RfThemeState.currentPalette.ink,
            )

            val displayedError = localValidationError ?: formError
            AdminFormErrorBanner(errorMessage = displayedError)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    BasicText("Total · $activeArticleLinesCount artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                    BasicText(formatAdminDop(totalQuoteDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable {
                            forceQuoteValidation = true
                            val venueError = validateQuoteVenue(draftState.venue)
                            val deliveryError = validateQuoteDelivery(draftState.deliveryFeeText)
                            val notesError = validateQuoteNotes(draftState.notes)
                            val linesError = if (activeLines.isEmpty()) {
                                "Agrega al menos un artículo antes de enviar la cotización."
                            } else {
                                null
                            }
                            val firstError = venueError ?: deliveryError ?: notesError ?: linesError
                            if (firstError != null) {
                                localValidationError = firstError
                            } else {
                                localValidationError = null
                                val isUpdatingQuote = draftState.sourceKind == AdminQuoteSourceKind.QUOTE
                                val existingQuoteId = if (isUpdatingQuote) {
                                    draftState.sourceKey.removePrefix("QUOTE:").ifBlank { null }
                                } else {
                                    null
                                }
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    val createdQuote = activeVm.createFormalQuoteFromAdmin(
                                        eventTitle = displayEventTitle,
                                        clientName = displayClientName,
                                        clientEmail = draftState.clientEmail,
                                        venue = draftState.venue.trim(),
                                        dateLabel = displayDateLabel,
                                        guests = draftState.guestsCount,
                                        lines = activeLines,
                                        deliveryFeeDop = parsedTransportDop,
                                        discountDop = parsedDiscountDop,
                                        notes = draftState.notes.trim(),
                                        existingQuoteId = existingQuoteId,
                                    )
                                    val matchingLead = leadsList.firstOrNull { leadItem ->
                                        leadItem.clientName.equals(displayClientName, ignoreCase = true)
                                    }
                                    if (matchingLead != null) {
                                        activeVm.updateCommercialLead(
                                            leadId = matchingLead.id,
                                            stage = "Cotización enviada",
                                            followUpNote = draftState.notes.trim(),
                                        )
                                    }
                                    if (createdQuote != null) {
                                        onSelectScreenCode("A22")
                                    }
                                } else {
                                    onSelectScreenCode("A22")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val isApprovingExisting = draftState.sourceKind == AdminQuoteSourceKind.QUOTE
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = if (isApprovingExisting) "Aprobar y enviar" else "Enviar cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

private fun matchesInventoryCategoryChip(articleItem: CatalogArticle, selectedCategory: String): Boolean {
    if (selectedCategory == "Todos") return true
    if (articleItem.category.equals(selectedCategory, ignoreCase = true)) return true
    if (articleItem.category.contains(selectedCategory, ignoreCase = true)) return true
    return when (selectedCategory) {
        "Mobiliario" -> {
            val inFurniture = articleItem.category.contains("Mobiliario", ignoreCase = true)
            val inChairsOrTables = eitherCondition(
                articleItem.category.contains("Sillas", ignoreCase = true),
                articleItem.category.contains("Mesas", ignoreCase = true),
            )
            eitherCondition(inFurniture, inChairsOrTables)
        }

        "Floral" -> eitherCondition(
            articleItem.category.contains("Flor", ignoreCase = true),
            articleItem.name.contains("floral", ignoreCase = true),
        )

        "Globos" -> {
            val inBalloonCat = articleItem.category.contains("Globo", ignoreCase = true)
            val inBalloonName = eitherCondition(
                articleItem.name.contains("globo", ignoreCase = true),
                articleItem.name.contains("arco", ignoreCase = true),
            )
            eitherCondition(inBalloonCat, inBalloonName)
        }

        "Mesa dulce" -> {
            val inSweetCat = eitherCondition(
                articleItem.category.contains("Dulce", ignoreCase = true),
                articleItem.category.contains("Estaciones", ignoreCase = true),
            )
            eitherCondition(inSweetCat, articleItem.name.contains("helado", ignoreCase = true))
        }

        else -> false
    }
}

private fun isLowStockArticle(articleItem: CatalogArticle): Boolean = eitherCondition(
    articleItem.availableOct23 in 1..5,
    articleItem.totalStock <= 5,
)

@Composable
internal fun AdminDeleteArticleConfirmSheet(
    targetArticle: CatalogArticle?,
    errorMessage: String?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (CatalogArticle) -> Unit,
    bottomClearance: Dp = 0.dp,
) {
    val typo = LocalRfTypography.current
    AdminBottomSheetOverlay(
        visible = targetArticle != null,
        onDismiss = onDismiss,
        bottomClearance = bottomClearance,
        verticalSpacing = 12.dp,
    ) {
        if (targetArticle != null) {
            BasicText(
                text = "Eliminar artículo",
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
            )
            BasicText(
                text = "¿Seguro que deseas eliminar \"${targetArticle.name}\" (SKU ${targetArticle.code})? Esta acción lo quitará del catálogo e inventario.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted, lineHeight = 19.sp),
            )
            AdminFormErrorBanner(errorMessage = errorMessage)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cerrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.muted))
                }
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("confirm-delete-article")
                        .clip(RoundedCornerShape(14.dp))
                        .background(rfAdminDangerPillInk())
                        .clickable(enabled = !isSubmitting) { onConfirmDelete(targetArticle) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (isSubmitting) "Eliminando..." else "Eliminar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminInventoryA09(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
    val isQuotePickerMode = viewModel?.inventoryPickerForQuote?.collectAsState()?.value ?: false
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    var searchQuery by remember { mutableStateOf("") }
    var hasSearchInteracted by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedStockSortOption by remember { mutableStateOf("Todos") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var pendingDeleteArticle by remember { mutableStateOf<CatalogArticle?>(null) }
    var deleteLocalError by remember { mutableStateOf<String?>(null) }
    val searchError = rememberDebouncedValidationError(
        value = searchQuery,
        hasInteracted = hasSearchInteracted,
        validator = InputValidator::validateSearchQuery,
    )

    val dynamicCategoryNames = remember(vmCategories, articles) {
        if (vmCategories != null) {
            val fromCatalog = vmCategories.map { category -> category.name.trim() }.filter { name -> name.isNotEmpty() }
            val fromArticles = articles.map { articleItem -> articleItem.category.trim() }.filter { name -> name.isNotEmpty() }
            (fromCatalog + fromArticles).distinctBy { categoryName -> categoryName.lowercase() }
        } else {
            articles.map { articleItem -> articleItem.category.trim() }
                .filter { name -> name.isNotEmpty() }
                .distinctBy { categoryName -> categoryName.lowercase() }
        }
    }
    val categoryChips = remember(dynamicCategoryNames) { listOf("Todos") + dynamicCategoryNames }
    val filterSortOptions = remember {
        listOf("Todos", "Stock bajo", "Ocultos en catálogo", "Mayor precio", "Menor precio")
    }

    val distinctCategoriesCount = dynamicCategoryNames.size

    val baseFilteredArticles = articles.filter { articleItem ->
        val matchesCategory = matchesInventoryCategoryChip(articleItem, selectedCategory)
        val trimmedSearch = searchQuery.trim()
        val matchesNameOrCat = eitherCondition(
            articleItem.name.contains(trimmedSearch, ignoreCase = true),
            articleItem.category.contains(trimmedSearch, ignoreCase = true),
        )
        val matchesCode = articleItem.code.contains(trimmedSearch, ignoreCase = true)
        val matchesSearch = if (trimmedSearch.isEmpty()) {
            true
        } else {
            eitherCondition(matchesNameOrCat, matchesCode)
        }
        val matchesVisibilityOrStock = when (selectedStockSortOption) {
            "Stock bajo" -> isLowStockArticle(articleItem)
            "Ocultos en catálogo" -> !articleItem.visibleInCatalog
            else -> true
        }
        val matchesPrimary = bothConditions(matchesCategory, matchesSearch)
        bothConditions(matchesPrimary, matchesVisibilityOrStock)
    }

    val filteredArticles = when (selectedStockSortOption) {
        "Mayor precio" -> baseFilteredArticles.sortedByDescending { articleItem -> articleItem.pricePerDayDop }
        "Menor precio" -> baseFilteredArticles.sortedBy { articleItem -> articleItem.pricePerDayDop }
        else -> baseFilteredArticles
    }

    val disponiblesCount = articles.count { articleItem -> articleItem.availableOct23 > 5 }
    val reservadosCount = articles.count { articleItem -> articleItem.totalStock - articleItem.availableOct23 > 0 }
    val stockBajoCount = articles.count { articleItem -> isLowStockArticle(articleItem) }
    val articleWord = if (articles.size == 1) "artículo" else "artículos"
    val categoryWord = if (distinctCategoriesCount == 1) "categoría" else "categorías"

    val inventoryTopPadding = if (includeHeader) 16.dp else 6.dp
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = inventoryTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isQuotePickerMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Modo selección para cotización",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                            BasicText(
                                text = "Toca un artículo para sumarlo al borrador",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFFE9EEFF)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable {
                                    viewModel.setInventoryPickerForQuote(false)
                                    onSelectScreenCode("BACK:A03")
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Volver",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }

            if (includeHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Inventario",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "${articles.size} $articleWord · $distinctCategoriesCount $categoryWord",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("A40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CATALOG, tint = rfAdminIndigoPillInk(), size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("A41") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFFC2338F), size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("s63") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CALENDAR, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .clickable {
                                viewModel?.startEditingArticle(null)
                                onSelectScreenCode("A10")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                    }
                }
            }

            val hasSearchError = searchError != null
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(
                            width = if (hasSearchError) 1.5.dp else 1.dp,
                            color = adminFieldBorderColor(hasSearchError),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(start = 14.dp, end = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = if (hasSearchError) RfColors.Error else RfThemeState.currentPalette.muted, size = 18.dp)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                BasicText(
                                    text = "Buscar artículo",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { updatedText ->
                                    hasSearchInteracted = true
                                    searchQuery = updatedText
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (showFilterSheet) Color(0xFF5B6CF0) else rfAdminIndigoPillBg())
                                .clickable { showFilterSheet = !showFilterSheet },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(
                                kind = RfIconKind.FILTER,
                                tint = if (showFilterSheet) Color.White else rfAdminIndigoPillInk(),
                                size = 18.dp,
                            )
                        }
                    }
                }
                RfValidationErrorText(errorMessage = searchError)
            }

            AnimatedVisibility(visible = showFilterSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(
                                text = "Filtrar y ordenar inventario",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                            )
                            RfIcon(
                                kind = RfIconKind.CLOSE,
                                tint = RfThemeState.currentPalette.muted,
                                size = 16.dp,
                                modifier = Modifier.clickable { showFilterSheet = false },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            filterSortOptions.forEach { optionLabel ->
                                val isOptionActive = selectedStockSortOption == optionLabel
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isOptionActive) rfAdminIndigoPillInk() else RfThemeState.currentPalette.chipBg)
                                        .clickable {
                                            selectedStockSortOption = optionLabel
                                            showFilterSheet = false
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = optionLabel,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isOptionActive) Color.White else RfThemeState.currentPalette.ink,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categoryChips.forEach { categoryLabel ->
                    val isCategorySelected = selectedCategory == categoryLabel
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .then(
                                if (isCategorySelected) {
                                    Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                } else {
                                    Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                                },
                            )
                            .clickable { selectedCategory = categoryLabel }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            categoryLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isCategorySelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCategorySelected) Color.White else RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(rfAdminSuccessPillBg())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Column {
                        BasicText(disponiblesCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C)))
                        BasicText("Disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk()))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(rfAdminIndigoPillBg())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Column {
                        BasicText(reservadosCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminIndigoPillInk()))
                        BasicText("Reservados hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(rfAdminDangerPillBg())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Column {
                        BasicText(stockBajoCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2B48)))
                        BasicText("Stock bajo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
            ) {
                Column {
                    if (filteredArticles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "No se encontraron artículos con los filtros activos.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                    } else {
                        filteredArticles.forEachIndexed { itemIndex, articleItem ->
                            if (itemIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                            }
                            val photoKey = photoKeyForAsset(articleItem.photo)
                            val isLowStock = isLowStockArticle(articleItem)
                            val tagText = when {
                                !articleItem.visibleInCatalog -> "Oculto"
                                isLowStock -> "Stock bajo"
                                else -> "Activo"
                            }
                            val tagBg = when {
                                !articleItem.visibleInCatalog -> RfThemeState.currentPalette.chipBg
                                isLowStock -> rfAdminDangerPillBg()
                                else -> rfAdminSuccessPillBg()
                            }
                            val tagColor = when {
                                !articleItem.visibleInCatalog -> RfThemeState.currentPalette.muted
                                isLowStock -> Color(0xFFDC2B48)
                                else -> rfAdminSuccessPillInk()
                            }

                            val handleArticleTap: () -> Unit = {
                                viewModel?.soundPlayer?.playTap()
                                if (isQuotePickerMode) {
                                    appendCatalogArticleToAdminQuoteDraft(articleItem)
                                    viewModel.setInventoryPickerForQuote(false)
                                    onSelectScreenCode("BACK:A03")
                                } else {
                                    viewModel?.selectArticle(articleItem.id)
                                    onSelectScreenCode("A23")
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { handleArticleTap() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                ) {
                                    RfUploadedOrCatalogPhoto(
                                        photoUrl = articleItem.photoUrl,
                                        fallbackPhotoKey = photoKey,
                                        contentDescription = articleItem.name,
                                        modifier = Modifier.fillMaxSize(),
                                        viewModel = viewModel,
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    BasicText(
                                        text = articleItem.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "${articleItem.totalStock} unid. · RD$ ${articleItem.pricePerDayDop}/día",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }

                                if (isQuotePickerMode) {
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .clip(CircleShape)
                                            .background(rfAdminIndigoPillBg())
                                            .clickable { handleArticleTap() }
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            RfIcon(kind = RfIconKind.PLUS, tint = rfAdminIndigoPillInk(), size = 14.dp)
                                            BasicText(
                                                text = "Agregar",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .background(tagBg, CircleShape)
                                                .padding(horizontal = 9.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tagColor))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("delete-article-${articleItem.id}")
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(rfAdminDangerPillBg())
                                                .clickable {
                                                    deleteLocalError = null
                                                    viewModel?.clearFormError()
                                                    pendingDeleteArticle = articleItem
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.TRASH, tint = rfAdminDangerPillInk(), size = 16.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AdminDeleteArticleConfirmSheet(
            targetArticle = pendingDeleteArticle,
            errorMessage = deleteLocalError,
            isSubmitting = isSubmitting,
            onDismiss = {
                pendingDeleteArticle = null
                deleteLocalError = null
            },
            onConfirmDelete = { target ->
                viewModel?.deleteArticle(target.id) { outcome ->
                    if (outcome.isSuccess) {
                        pendingDeleteArticle = null
                        deleteLocalError = null
                    } else {
                        deleteLocalError = outcome.exceptionOrNull()?.message ?: "No se pudo eliminar el artículo."
                    }
                }
            },
            bottomClearance = RfDimens.BottomBarContentClearance,
        )
    }
}

private fun extractDayNumberTokens(dateLabel: String): Set<String> {
    val tokens = mutableSetOf<String>()
    var activeRun = ""
    for (charIndex in 0 until dateLabel.length) {
        val currentChar = dateLabel[charIndex]
        if (currentChar.isDigit()) {
            activeRun += currentChar
        } else {
            if (activeRun.isNotEmpty()) {
                val parsed = activeRun.toIntOrNull()
                if (parsed != null) {
                    tokens.add(parsed.toString())
                }
                activeRun = ""
            }
        }
    }
    if (activeRun.isNotEmpty()) {
        val parsed = activeRun.toIntOrNull()
        if (parsed != null) {
            tokens.add(parsed.toString())
        }
    }
    return tokens
}

private fun statusTagStyleForEvent(eventItem: ClientEvent): Triple<String, Color, Color> = when (eventItem.status) {
    EventStatus.CONFIRMED -> Triple("Confirmado", rfAdminSuccessPillBg(), rfAdminSuccessPillInk())
    EventStatus.IN_SETUP -> Triple("En montaje", rfAdminTealPillBg(), rfAdminTealPillInk())
    EventStatus.PENDING_QUOTE -> Triple("En revisión", rfAdminWarnPillBg(), rfAdminWarnPillInk())
    EventStatus.COMPLETED -> Triple("Cerrado", rfAdminSuccessPillBg(), rfAdminSuccessPillInk())
    EventStatus.CANCELLED -> Triple("Cancelado", rfAdminDangerPillBg(), rfAdminDangerPillInk())
}

@Composable
internal fun RenderAdminEventsA11(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics

    var selectedDayFilter by remember { mutableStateOf("Todos") }
    var selectedEventSegment by remember { mutableStateOf(0) }

    val upcomingEvents = allEvents.filter { eventItem ->
        val notDone = eventItem.status != EventStatus.COMPLETED
        val notCancelled = eventItem.status != EventStatus.CANCELLED
        bothConditions(notDone, notCancelled)
    }
    val receivableEvents = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
    val pastEvents = allEvents.filter { eventItem ->
        val isCompletedOrCancelled = eitherCondition(
            eventItem.status == EventStatus.COMPLETED,
            eventItem.status == EventStatus.CANCELLED,
        )
        eitherCondition(isCompletedOrCancelled, eventItem.daysUntilEvent < 0)
    }

    val segmentedEvents = when (selectedEventSegment) {
        1 -> receivableEvents
        2 -> pastEvents
        else -> upcomingEvents
    }

    val displayedEvents = if (selectedDayFilter == "Todos") {
        segmentedEvents
    } else {
        segmentedEvents.filter { eventItem ->
            selectedDayFilter in extractDayNumberTokens(eventItem.dateLabel)
        }
    }

    val dayNumbersWithEvents = remember(allEvents) {
        allEvents.flatMap { eventItem -> extractDayNumberTokens(eventItem.dateLabel) }.toSet()
    }

    val adminEventsTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = adminEventsTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (includeHeader) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Eventos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = "${metrics.monthLabel} · ${upcomingEvents.size} próximos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                RfIconButton(
                    icon = RfIconKind.CALENDAR,
                    accessibilityLabel = "Calendario",
                    onClick = { onSelectScreenCode("s63") },
                )

                RfRoundGradientIconButton(
                    icon = RfIconKind.PLUS,
                    accessibilityLabel = "Crear evento",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("A04") },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val isAllSelected = selectedDayFilter == "Todos"
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isAllSelected) {
                            Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        } else {
                            Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        },
                    )
                    .clickable { selectedDayFilter = "Todos" }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    BasicText(
                        text = "Ver",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAllSelected) RfThemeState.currentPalette.surface.copy(alpha = 0.88f) else RfThemeState.currentPalette.muted),
                    )
                    BasicText(
                        text = "Todos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isAllSelected) Color.White else RfThemeState.currentPalette.ink),
                    )
                }
            }

            val weekDays = listOf(
                "L" to "19",
                "M" to "20",
                "X" to "21",
                "J" to "22",
                "V" to "23",
                "S" to "24",
                "D" to "25",
            )

            weekDays.forEach { dayPair ->
                val dayLetter = dayPair.first
                val dayNumber = dayPair.second
                val isSelected = selectedDayFilter == dayNumber
                val hasMatchingEvent = dayNumber in dayNumbersWithEvents
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            },
                        )
                        .clickable {
                            selectedDayFilter = if (isSelected) "Todos" else dayNumber
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            text = dayLetter,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White.copy(alpha = 0.8f) else RfThemeState.currentPalette.muted),
                        )
                        BasicText(
                            text = dayNumber,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else RfThemeState.currentPalette.ink),
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (hasMatchingEvent) Color(0xFFFFD45C) else Color.Transparent),
                        )
                    }
                }
            }
        }

        val eventSegments = listOf(
            "Próximos · ${upcomingEvents.size}",
            "Por cobrar · ${receivableEvents.size}",
            "Pasados · ${pastEvents.size}",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            eventSegments.forEachIndexed { segmentIndex, segmentTitle ->
                val isSegmentSelected = selectedEventSegment == segmentIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSegmentSelected) Color.White else Color.Transparent)
                        .clickable { selectedEventSegment = segmentIndex },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = segmentTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSegmentSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSegmentSelected) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                        ),
                    )
                }
            }
        }

        if (displayedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        text = "No hay eventos programados el día $selectedDayFilter",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(rfAdminIndigoPillBg())
                            .clickable { selectedDayFilter = "Todos" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "Mostrar todos los eventos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedEvents.forEach { eventItem ->
                    val tagTriple = statusTagStyleForEvent(eventItem)
                    val paymentProgress = if (eventItem.totalDop > 0) {
                        (eventItem.paidDepositDop.toFloat() / eventItem.totalDop.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel?.selectEvent(eventItem.id)
                                onSelectScreenCode("A13")
                            }
                            .padding(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                            ) {
                                RfPhoto(
                                    photoKey = photoKeyForAsset(eventItem.photo),
                                    contentDescription = eventItem.title,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(
                                    text = eventItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = "${formatReadableSpanishDateText(eventItem.dateLabel)} · ${eventItem.clientName} · ${eventItem.venue}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(22.dp)
                                            .background(tagTriple.second, CircleShape)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = tagTriple.first,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tagTriple.third),
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(RfThemeState.currentPalette.line),
                                    ) {
                                        if (paymentProgress > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(paymentProgress)
                                                    .height(5.dp)
                                                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))),
                                            )
                                        }
                                    }

                                    BasicText(
                                        text = "${(paymentProgress * 100).toInt()}%",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminPaymentsA20(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    val pendingVerificationPayments = paymentsList.filter { paymentItem ->
        bothConditions(!paymentItem.verified, paymentItem.rejectionReason.isBlank())
    }
    val receivableEvents = eventsList.filter { eventItem -> eventItem.balanceDop > 0 }
    val collectedMonthDop = if (paymentsList.isNotEmpty()) {
        paymentsList.sumOf { paymentItem -> paymentItem.amountDop }
    } else {
        metrics.octoberRevenueDop
    }
    val receivableTotalDop = receivableEvents.sumOf { eventItem -> eventItem.balanceDop }
    val canManageFinances = viewModel?.canCurrentUserManageFinances() ?: true

    var selectedPaymentTab by remember { mutableStateOf(0) }
    var remindedEventIds by remember { mutableStateOf(emptySet<String>()) }
    var rejectingPaymentId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var rejectionLocalError by remember { mutableStateOf<String?>(null) }

    val paymentTabs = listOf(
        "Todos",
        "Verificar (${pendingVerificationPayments.size})",
        "Cobrado (${paymentsList.size})",
        "Por cobrar (${receivableEvents.size})",
    )

    val showPendingVerificationSection = eitherCondition(selectedPaymentTab == 0, selectedPaymentTab == 1)
    val showCollectedSection = eitherCondition(selectedPaymentTab == 0, selectedPaymentTab == 2)
    val showReceivablesSection = eitherCondition(selectedPaymentTab == 0, selectedPaymentTab == 3)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A12") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Pagos", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        BasicText(metrics.monthLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(rfAdminIndigoPillBg())
                            .border(1.dp, Color(0xFFD5D4F7), CircleShape)
                            .clickable { viewModel?.exportReportCsv(reportType = "dgii607") }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            RfIcon(kind = RfIconKind.DOWNLOAD, tint = rfAdminIndigoPillInk(), size = 15.dp)
                            BasicText(
                                text = "Exportar 607 DGII",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                            )
                        }
                    }
                }

                AdminFormErrorBanner(errorMessage = formError)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                            .clickable { selectedPaymentTab = 2 }
                            .padding(14.dp),
                    ) {
                        Column {
                            BasicText("Cobrado este mes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE9EEFF)))
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = formatAdminDop(collectedMonthDop),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(rfAdminWarnPillBg())
                            .clickable { selectedPaymentTab = 3 }
                            .padding(14.dp),
                    ) {
                        Column {
                            BasicText("Por cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminWarnPillInk()))
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = formatAdminDop(receivableTotalDop),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminWarnPillInk()),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.line)
                        .padding(4.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        paymentTabs.forEachIndexed { tabIndex, tabLabel ->
                            val isSelected = selectedPaymentTab == tabIndex
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedPaymentTab = tabIndex }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = tabLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                                    ),
                                )
                            }
                        }
                    }
                }

                if (showReceivablesSection) {
                    BasicText("CUENTAS POR COBRAR", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, letterSpacing = 1.sp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
                    ) {
                        Column {
                            if (receivableEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "Todos los eventos tienen su saldo cubierto.",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            } else {
                                receivableEvents.forEachIndexed { rowIndex, receivableEvent ->
                                    val hasPersistedReminder = receivableEvent.lastPaymentReminderAt.isNotBlank()
                                    val isLocallyReminded = receivableEvent.id in remindedEventIds
                                    val isReminded = if (hasPersistedReminder) true else isLocallyReminded
                                    val clientInitial = InputValidator.extractInitials(receivableEvent.clientName, fallback = "C", maxInitials = 1)
                                    val isUrgentDue = receivableEvent.daysUntilEvent <= 5
                                    if (rowIndex > 0) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel?.selectEvent(receivableEvent.id)
                                                onSelectScreenCode("A21")
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(rfAdminIndigoPillBg()),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminIndigoPillInk()))
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            BasicText(
                                                text = "${receivableEvent.title} · ${receivableEvent.clientName}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                            )
                                            BasicText(
                                                text = "Vence ${receivableEvent.balanceDueDateLabel}",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 12.sp,
                                                    color = if (isUrgentDue) rfAdminDangerPillInk() else rfAdminWarnPillInk(),
                                                ),
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            BasicText(
                                                text = formatAdminDop(receivableEvent.balanceDop),
                                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                                            )
                                            Row(
                                                modifier = Modifier.clickable(enabled = !isSubmitting) {
                                                    if (viewModel == null) {
                                                        remindedEventIds = if (isReminded) {
                                                            remindedEventIds - receivableEvent.id
                                                        } else {
                                                            remindedEventIds + receivableEvent.id
                                                        }
                                                    } else {
                                                        viewModel.sendEventPaymentReminder(
                                                            eventId = receivableEvent.id,
                                                            channel = "whatsapp",
                                                        ) { sentOk, _ ->
                                                            if (sentOk) {
                                                                remindedEventIds = remindedEventIds + receivableEvent.id
                                                            }
                                                        }
                                                    }
                                                },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                RfIcon(
                                                    kind = if (isReminded) RfIconKind.CHECK else RfIconKind.WHATSAPP,
                                                    tint = if (isReminded) rfAdminSuccessPillInk() else rfAdminIndigoPillInk(),
                                                    size = 13.dp,
                                                )
                                                BasicText(
                                                    text = if (isReminded) "Recordado" else "Recordar",
                                                    style = TextStyle(
                                                        fontFamily = typo.bodyFamily,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isReminded) rfAdminSuccessPillInk() else rfAdminIndigoPillInk(),
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showPendingVerificationSection) {
                    BasicText("POR VERIFICAR", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, letterSpacing = 1.sp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
                    ) {
                        Column {
                            if (pendingVerificationPayments.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "No hay transferencias o comprobantes por verificar.",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            } else {
                                pendingVerificationPayments.forEachIndexed { pendingIndex, pendingPayment ->
                                    if (pendingIndex > 0) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                                    }
                                    AdminPendingPaymentVerificationRow(
                                        payment = pendingPayment,
                                        canManageFinances = canManageFinances,
                                        viewModel = viewModel,
                                        onApprove = { viewModel?.verifyPendingPayment(pendingPayment.id) },
                                        onRequestReject = {
                                            rejectingPaymentId = pendingPayment.id
                                            rejectionReasonInput = ""
                                            rejectionLocalError = null
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                if (showCollectedSection) {
                    BasicText("COBROS REGISTRADOS", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, letterSpacing = 1.sp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
                    ) {
                        Column {
                            paymentsList.forEachIndexed { paymentIndex, paymentItem ->
                                if (paymentIndex > 0) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                                }
                                val isRejectedPayment = paymentItem.rejectionReason.isNotBlank()
                                val isPendingPayment = bothConditions(!paymentItem.verified, !isRejectedPayment)
                                val statusBgColor = when {
                                    paymentItem.verified -> rfAdminSuccessPillBg()
                                    isRejectedPayment -> rfAdminDangerPillBg()
                                    else -> rfAdminWarnPillBg()
                                }
                                val statusIconKind = when {
                                    paymentItem.verified -> RfIconKind.CHECK
                                    isRejectedPayment -> RfIconKind.CLOSE
                                    else -> RfIconKind.CLOCK
                                }
                                val statusTintColor = when {
                                    paymentItem.verified -> rfAdminSuccessPillInk()
                                    isRejectedPayment -> rfAdminDangerPillInk()
                                    else -> rfAdminWarnPillInk()
                                }
                                val ncfSubtitle = if (paymentItem.ncfNumber.isNotBlank()) {
                                    " · ${paymentItem.ncfType} ${paymentItem.ncfNumber}"
                                } else {
                                    " · ${paymentItem.ncfType}"
                                }
                                val amountTintColor = if (isRejectedPayment) {
                                    rfAdminDangerPillInk()
                                } else if (isPendingPayment) {
                                    rfAdminWarnPillInk()
                                } else {
                                    rfAdminSuccessPillInk()
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val linkedEvent = eventsList.firstOrNull { eventItem -> eventItem.id == paymentItem.eventId }
                                            if (linkedEvent != null) {
                                                viewModel?.exportReceiptDocument(event = linkedEvent, payment = paymentItem)
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(statusBgColor),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(
                                            kind = statusIconKind,
                                            tint = statusTintColor,
                                            size = 18.dp,
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = "${paymentItem.clientName} · ${paymentItem.kindLabel}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                                        )
                                        BasicText(
                                            text = "${paymentItem.eventTitle} · ${paymentItem.methodLabel}$ncfSubtitle",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                        )
                                        if (paymentItem.rejectionReason.isNotBlank()) {
                                            BasicText(
                                                text = "Motivo: ${paymentItem.rejectionReason}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = rfAdminDangerPillInk()),
                                            )
                                        }
                                    }

                                    BasicText(
                                        text = "+${formatAdminDop(paymentItem.amountDop)}",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = amountTintColor),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            val firstUnpaid = receivableEvents.firstOrNull()
                            if (firstUnpaid != null) {
                                viewModel?.selectEvent(firstUnpaid.id)
                            }
                            onSelectScreenCode("A21")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                        BasicText("Registrar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        val activeRejectPaymentId = rejectingPaymentId
        if (activeRejectPaymentId != null) {
            AdminRejectPaymentDialog(
                reasonValue = rejectionReasonInput,
                errorText = rejectionLocalError,
                isSubmitting = isSubmitting,
                onReasonChange = { nextReason ->
                    rejectionReasonInput = nextReason
                    rejectionLocalError = null
                },
                onCancel = { rejectingPaymentId = null },
                onConfirm = {
                    val trimmedReason = rejectionReasonInput.trim()
                    if (trimmedReason.length < 4) {
                        rejectionLocalError = "Indica un motivo de al menos 4 caracteres."
                    } else {
                        viewModel?.rejectPendingPayment(activeRejectPaymentId, trimmedReason)
                        rejectingPaymentId = null
                    }
                },
            )
        }
    }
}

@Composable
private fun AdminPendingPaymentVerificationRow(
    payment: PaymentRecord,
    canManageFinances: Boolean,
    viewModel: RosaFiestaViewModel?,
    onApprove: () -> Unit,
    onRequestReject: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val rncSuffix = if (payment.clientRnc.isNotBlank()) " · RNC ${payment.clientRnc}" else ""
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (payment.voucherUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rfAdminIndigoPillBg()),
                ) {
                    RfUploadedOrCatalogPhoto(
                        photoUrl = payment.voucherUrl,
                        fallbackPhotoKey = RfPhotoKey.ARCO_ROSA,
                        contentDescription = "Comprobante ${payment.referenceCode}",
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(rfAdminWarnPillBg()),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOCK, tint = rfAdminWarnPillInk(), size = 18.dp)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    text = "${payment.clientName} · +${formatAdminDop(payment.amountDop)}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    text = "${payment.eventTitle} · ${payment.methodLabel} · Ref ${payment.referenceCode}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                )
                BasicText(
                    text = "NCF ${payment.ncfType}$rncSuffix",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                )
            }
        }

        if (canManageFinances) {
            val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rfAdminDangerPillBg())
                        .border(1.dp, Color(0xFFF5C2CB), RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSubmitting) { onRequestReject() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Rechazar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminDangerPillInk()))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AdminButtonGradient)
                        .clickable(enabled = !isSubmitting) { onApprove() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Verificar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        } else {
            BasicText(
                text = "Requiere permiso de Finanzas para verificar",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = RfThemeState.currentPalette.muted),
            )
        }
    }
}

@Composable
private fun AdminRejectPaymentDialog(
    reasonValue: String,
    errorText: String?,
    isSubmitting: Boolean = false,
    onReasonChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66151729))
            .onPreviewKeyEvent { keyEvent ->
                val isKeyDown = keyEvent.type == KeyEventType.KeyDown
                val isEscape = keyEvent.key == Key.Escape
                val shouldCancel = if (isKeyDown) {
                    if (isEscape) !isSubmitting else false
                } else {
                    false
                }
                if (shouldCancel) {
                    onCancel()
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCancel() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RfThemeState.currentPalette.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {}
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText("Rechazar comprobante", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
            BasicText("Explica al cliente qué debe corregir para reenviar su pago.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
            BasicTextField(
                value = reasonValue,
                onValueChange = onReasonChange,
                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                cursorBrush = SolidColor(rfAdminIndigoPillInk()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RfThemeState.currentPalette.chipBg)
                    .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                decorationBox = { innerField ->
                    if (reasonValue.isEmpty()) {
                        BasicText("Ej. Referencia no coincide con depósito", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }
                    innerField()
                },
            )
            if (errorText != null) {
                BasicText(errorText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = rfAdminDangerPillInk()))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(rfAdminDangerPillInk())
                        .clickable(enabled = !isSubmitting) { onConfirm() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Confirmar rechazo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

/**
 * Renders the originating admin screen and optional bottom tab bar behind the payment modal scrim.
 */
@Composable
private fun RenderUnderlyingScreenForRecordPaymentA21(
    originScreenCode: String,
    viewModel: RosaFiestaViewModel?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (originScreenCode) {
            "A01" -> RenderAdminHomeA01(viewModel = viewModel, onSelectScreenCode = {})
            "A02" -> RenderAdminRequestsA02(viewModel = viewModel, onSelectScreenCode = {})
            "A09" -> RenderAdminInventoryA09(viewModel = viewModel, onSelectScreenCode = {})
            "A11" -> RenderAdminEventsA11(viewModel = viewModel, onSelectScreenCode = {})
            "A12" -> RenderAdminMoreA12(viewModel = viewModel, onSelectScreenCode = {})
            "A13" -> RenderAdminEventDetailA13(viewModel = viewModel, onSelectScreenCode = {})
            "A22" -> RenderAdminQuotesA22(viewModel = viewModel, onSelectScreenCode = {})
            else -> RenderAdminPaymentsA20(viewModel = viewModel, onSelectScreenCode = {})
        }
        val originHasBottomBar = shouldShowBottomBar(originScreenCode)
        if (originHasBottomBar) {
            RenderAppBottomBar(
                spec = ScreenCatalog.findByCode(originScreenCode),
                onSelectScreenCode = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun RenderAdminRecordPaymentA21(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
) {
    val typo = LocalRfTypography.current
    val originScreenCode = remember(underlyingScreenCode) {
        underlyingScreenCode?.trim()?.ifEmpty { "A20" } ?: "A20"
    }
    val dismissRoute = "BACK:$originScreenCode"
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value.orEmpty()
    val pickedAttachment = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    val vmVoucherUrl = viewModel?.paymentVoucherUrl?.collectAsState()?.value.orEmpty()
    val vmNcfType = viewModel?.paymentNcfType?.collectAsState()?.value ?: "B02"
    val vmClientRnc = viewModel?.paymentClientRnc?.collectAsState()?.value.orEmpty()
    val formError = viewModel?.formError?.collectAsState()?.value

    val selectableEvents = remember(allEvents) {
        val unpaidFirst = allEvents.filter { eventItem -> eventItem.balanceDop > 0 }
        if (unpaidFirst.isNotEmpty()) unpaidFirst else allEvents
    }
    val initialSelectedEvent = selectableEvents.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: allEvents.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: selectableEvents.firstOrNull()
    if (initialSelectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Registrar pago",
            emptyTitle = "Sin eventos para cobrar",
            emptyMessage = "Crea un evento o aprueba una cotización antes de registrar anticipos o saldos.",
            backTargetCode = dismissRoute,
            iconKind = RfIconKind.WALLET,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }

    var chosenEventId by remember(selectedEventId) { mutableStateOf(initialSelectedEvent.id) }
    val chosenEvent = allEvents.firstOrNull { eventItem -> eventItem.id == chosenEventId } ?: initialSelectedEvent

    var showEventDropdown by remember { mutableStateOf(false) }
    var showPaymentDatePicker by remember { mutableStateOf(false) }
    var amountDigitsText by remember(chosenEvent.id) {
        val prefillDop = if (chosenEvent.balanceDop > 0) chosenEvent.balanceDop else chosenEvent.totalDop
        mutableStateOf(prefillDop.toString())
    }
    var selectedMethod by remember { mutableStateOf("Transferencia") }
    var selectedBank by remember { mutableStateOf("Banreservas") }
    var referenceText by remember(chosenEvent.id) {
        mutableStateOf("BR-${chosenEvent.code.takeLast(4)}-01")
    }
    var paymentDate by remember {
        mutableStateOf(normalizePaymentDateToSlash(DomainClockFormatter.currentFullDateLabel()))
    }
    var localNcfType by remember(chosenEvent.id) { mutableStateOf("B02") }
    var localClientRnc by remember(chosenEvent.id) { mutableStateOf("") }
    var localVoucherUrl by remember(chosenEvent.id) { mutableStateOf("") }
    var sendWhatsAppReceipt by remember { mutableStateOf(true) }
    val activeNcfType = if (viewModel != null) vmNcfType else localNcfType
    val activeClientRnc = if (viewModel != null) vmClientRnc else localClientRnc
    val activeVoucherUrl = if (viewModel != null) vmVoucherUrl else localVoucherUrl
    var hasAmountInteracted by remember(chosenEvent.id) { mutableStateOf(false) }
    var hasRefInteracted by remember(chosenEvent.id) { mutableStateOf(false) }
    var hasDateInteracted by remember(chosenEvent.id) { mutableStateOf(false) }
    var hasRncInteracted by remember(chosenEvent.id) { mutableStateOf(false) }
    var forcePaymentValidation by remember(chosenEvent.id) { mutableStateOf(false) }
    var localErrorText by remember(chosenEvent.id) { mutableStateOf<String?>(null) }

    val validateAmount: (String) -> String? = { rawValue ->
        InputValidator.validateMoneyAmount(
            rawValue = rawValue,
            fieldLabel = "El monto",
            isRequired = true,
            minAmount = 1.0,
            maxAmount = InputValidator.MAX_MONEY_AMOUNT,
        )
    }
    val validateReference: (String) -> String? = { rawValue ->
        val isReferenceRequired = selectedMethod != "Efectivo"
        InputValidator.validateBankReference(
            rawValue = rawValue,
            isRequired = isReferenceRequired,
        )
    }
    val validatePaymentDate: (String) -> String? = { rawValue ->
        InputValidator.validatePaymentDate(
            rawValue = rawValue,
            isRequired = true,
            minYear = 2024,
        )
    }
    val validateTaxIdForB01: (String) -> String? = { rawValue ->
        val isB01 = activeNcfType == "B01"
        if (!isB01) {
            null
        } else {
            val isValidRnc = viewModel?.isValidDominicanTaxId(rawValue) ?: (InputValidator.validateRnc(rawValue, isRequired = true) == null)
            if (isValidRnc) null else "Ingresa un RNC (9 dígitos) o Cédula (11 dígitos) válido para NCF B01."
        }
    }

    val amountVisibleError = rememberDebouncedValidationError(
        value = amountDigitsText,
        hasInteracted = hasAmountInteracted,
        forceShowError = forcePaymentValidation,
        validator = validateAmount,
    )
    val paymentDateVisibleError = rememberDebouncedValidationError(
        value = paymentDate,
        hasInteracted = hasDateInteracted,
        forceShowError = forcePaymentValidation,
        validator = validatePaymentDate,
    )
    val rncVisibleError = rememberDebouncedValidationError(
        value = activeClientRnc,
        hasInteracted = hasRncInteracted,
        forceShowError = forcePaymentValidation,
        validator = validateTaxIdForB01,
    )

    val parsedAmountDop = amountDigitsText.filter { digitChar -> digitChar.isDigit() }.toIntOrNull() ?: 0
    val clientFirstName = chosenEvent.clientName.trim().substringBefore(" ").ifBlank { "Cliente" }
    val clientInitial = InputValidator.extractInitials(clientFirstName, fallback = "C", maxInitials = 1)

    Box(modifier = Modifier.fillMaxSize()) {
        RenderUnderlyingScreenForRecordPaymentA21(
            originScreenCode = originScreenCode,
            viewModel = viewModel,
        )

        val scrimInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                    onClick = { onSelectScreenCode(dismissRoute) },
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(RfThemeState.currentPalette.fieldBorder),
                )

                BasicText(
                    text = "Registrar pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.fieldBorder, RoundedCornerShape(14.dp))
                        .clickable { showEventDropdown = !showEventDropdown }
                        .padding(start = 8.dp, end = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RfColors.VioletaTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = RfColors.VioletaTintInk))
                        }

                        BasicText(
                            text = "${chosenEvent.title} · ${chosenEvent.clientName}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink),
                        )

                        RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    }
                }

                AnimatedVisibility(visible = showEventDropdown) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8F8FC))
                            .border(1.dp, RfThemeState.currentPalette.fieldBorder, RoundedCornerShape(14.dp))
                            .padding(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            selectableEvents.forEach { optionEvent ->
                                val isChosen = optionEvent.id == chosenEvent.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isChosen) rfAdminIndigoPillBg() else RfThemeState.currentPalette.surface)
                                        .clickable {
                                            chosenEventId = optionEvent.id
                                            viewModel?.selectEvent(optionEvent.id)
                                            localErrorText = null
                                            showEventDropdown = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    BasicText(
                                        text = "${optionEvent.title} · ${optionEvent.clientName}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = formatAdminDop(optionEvent.balanceDop),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                                    )
                                }
                            }
                        }
                    }
                }

                val hasAmountError = amountVisibleError != null
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText("Monto (RD$)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .border(
                                width = if (hasAmountError) 1.5.dp else 1.dp,
                                color = adminFieldBorderColor(hasAmountError),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BasicText(
                                text = "RD$",
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted),
                            )
                            BasicTextField(
                                value = amountDigitsText,
                                onValueChange = { updatedText ->
                                    hasAmountInteracted = true
                                    amountDigitsText = InputValidator.filterDigitsOnly(updatedText, 7)
                                    localErrorText = null
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RfThemeState.currentPalette.ink,
                                    textAlign = TextAlign.Center,
                                ),
                                modifier = Modifier.widthIn(min = 80.dp, max = 180.dp),
                            )
                        }
                    }
                    RfValidationErrorText(errorMessage = amountVisibleError)
                    val balanceHint = if (parsedAmountDop >= chosenEvent.balanceDop) {
                        "Salda el balance pendiente (${formatAdminDop(chosenEvent.balanceDop)})"
                    } else {
                        "Abono parcial · restarán ${formatAdminDop((chosenEvent.balanceDop - parsedAmountDop).coerceAtLeast(0))}"
                    }
                    BasicText(balanceHint, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminSuccessPillInk()))
                }

                val paymentMethods = listOf("Transferencia", "Efectivo", "Tarjeta")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { methodLabel ->
                        val isMethodSelected = selectedMethod == methodLabel
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isMethodSelected) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                                    },
                                )
                                .clickable {
                                    selectedMethod = methodLabel
                                    localErrorText = null
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                methodLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMethodSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMethodSelected) Color.White else RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }

                if (selectedMethod == "Transferencia") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Banreservas", "Banco Popular", "BHD").forEach { bankChoice ->
                            val isBankChosen = selectedBank == bankChoice
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isBankChosen) rfAdminIndigoPillBg() else RfThemeState.currentPalette.surface)
                                    .border(
                                        1.dp,
                                        if (isBankChosen) Color(0xFF5B6CF0) else RfThemeState.currentPalette.fieldBorder,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable { selectedBank = bankChoice },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = bankChoice,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isBankChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isBankChosen) rfAdminIndigoPillInk() else RfThemeState.currentPalette.ink,
                                    ),
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminDebouncedInputField(
                        value = referenceText,
                        onValueChange = { updatedText ->
                            referenceText = updatedText
                            localErrorText = null
                        },
                        validator = validateReference,
                        hasInteracted = hasRefInteracted,
                        onInteracted = { hasRefInteracted = true },
                        forceShowError = forcePaymentValidation,
                        placeholder = if (selectedMethod == "Efectivo") "Referencia (opcional)" else "Referencia / Banco",
                        inputFilter = ::filterBankReferenceInput,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next,
                        ),
                        cornerRadius = 14.dp,
                        minHeight = 50.dp,
                        horizontalPadding = 14.dp,
                        textFontSize = 14.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )

                    val hasDateError = paymentDateVisibleError != null
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(
                                    width = if (hasDateError) 1.5.dp else 1.dp,
                                    color = adminFieldBorderColor(hasDateError),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .padding(start = 10.dp, end = 6.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            RfMaskedDateField(
                                value = paymentDate,
                                onValueChange = { updatedText ->
                                    hasDateInteracted = true
                                    paymentDate = updatedText
                                    localErrorText = null
                                },
                                onOpenCalendar = { showPaymentDatePicker = true },
                                placeholder = "Elegir fecha",
                                hasError = hasDateError,
                                isAdmin = true,
                                compactInline = true,
                                embeddedInContainer = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        RfValidationErrorText(errorMessage = paymentDateVisibleError)
                    }
                }

                val ncfOptions = listOf(
                    "B02" to "Consumo (B02)",
                    "B01" to "Crédito Fiscal (B01)",
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ncfOptions.forEach { ncfPair ->
                        val isNcfSelected = activeNcfType == ncfPair.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNcfSelected) rfAdminIndigoPillInk() else RfThemeState.currentPalette.surface)
                                .border(1.dp, if (isNcfSelected) rfAdminIndigoPillInk() else RfThemeState.currentPalette.fieldBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (viewModel != null) {
                                        viewModel.setPaymentNcfType(ncfPair.first)
                                    } else {
                                        localNcfType = ncfPair.first
                                    }
                                    localErrorText = null
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = ncfPair.second,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isNcfSelected) Color.White else RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }

                if (activeNcfType == "B01") {
                    val hasRncError = rncVisibleError != null
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("RNC o Cédula fiscal (B01)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                        RfMaskedDigitBasicTextField(
                            value = activeClientRnc,
                            onValueChange = { cleanDigits ->
                                hasRncInteracted = true
                                if (viewModel != null) {
                                    viewModel.setPaymentClientRnc(cleanDigits)
                                } else {
                                    localClientRnc = cleanDigits
                                }
                                localErrorText = null
                            },
                            maskTransform = { prev, next, sel ->
                                InputValidator.filterDigitsOnlyWithCursor(prev, next, sel, 11)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                            cursorBrush = SolidColor(rfAdminIndigoPillInk()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("a21-rnc-input")
                                .clip(RoundedCornerShape(12.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(
                                    width = if (hasRncError) 1.5.dp else 1.dp,
                                    color = adminFieldBorderColor(hasRncError),
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            decorationBox = { innerField ->
                                if (activeClientRnc.isEmpty()) {
                                    BasicText("9 u 11 dígitos (sin guiones)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                                }
                                innerField()
                            },
                        )
                        RfValidationErrorText(errorMessage = rncVisibleError)
                    }
                }

                val attachedFileName = pickedAttachment?.fileName
                val hasVoucherUrl = activeVoucherUrl.isNotBlank()
                val hasAttachment = eitherCondition(!attachedFileName.isNullOrBlank(), hasVoucherUrl)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (hasAttachment) rfAdminIndigoPillBg() else Color.Transparent)
                        .border(2.dp, if (hasAttachment) Color(0xFF5B6CF0) else Color(0xFFC9CAE6), RoundedCornerShape(14.dp))
                        .clickable {
                            if (viewModel != null) {
                                viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png", "pdf")) { pickedFile ->
                                    viewModel.uploadAndAttachMedia("voucher", pickedFile) { uploadedUrl ->
                                        viewModel.setPaymentVoucherUrl(uploadedUrl)
                                    }
                                }
                            } else {
                                localVoucherUrl = "comprobante-admin.jpg"
                            }
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (hasVoucherUrl) {
                        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))) {
                            RfUploadedOrCatalogPhoto(
                                photoUrl = activeVoucherUrl,
                                fallbackPhotoKey = RfPhotoKey.ARCO_ROSA,
                                contentDescription = "Comprobante",
                                modifier = Modifier.fillMaxSize(),
                                viewModel = viewModel,
                            )
                        }
                    } else {
                        RfIcon(kind = RfIconKind.CAMERA, tint = rfAdminIndigoPillInk(), size = 18.dp)
                    }
                    BasicText(
                        text = if (hasAttachment) "Comprobante adjunto: ${attachedFileName ?: "subido"}" else "Adjuntar comprobante",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendWhatsAppReceipt = !sendWhatsAppReceipt }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Enviar recibo a $clientFirstName ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                        BasicText("por WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(if (sendWhatsAppReceipt) AdminButtonGradient else SolidColor(RfThemeState.currentPalette.fieldBorder))
                            .padding(3.dp),
                        contentAlignment = if (sendWhatsAppReceipt) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                    }
                }

                val activeError = localErrorText ?: formError
                AdminFormErrorBanner(errorMessage = activeError)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            forcePaymentValidation = true
                            val amountError = validateAmount(amountDigitsText)
                            val refError = validateReference(referenceText)
                            val dateError = validatePaymentDate(paymentDate)
                            val requiresRnc = activeNcfType == "B01"
                            val hasValidRnc = viewModel?.isValidDominicanTaxId(activeClientRnc) ?: true
                            val rncError = if (bothConditions(requiresRnc, !hasValidRnc)) {
                                "Ingresa un RNC (9 dígitos) o Cédula (11 dígitos) válido para NCF B01."
                            } else {
                                null
                            }
                            val firstError = amountError ?: refError ?: dateError ?: rncError
                            if (firstError != null) {
                                localErrorText = firstError
                            } else {
                                localErrorText = null
                                val cleanRef = referenceText.trim()
                                val readablePaymentDate = InputValidator.formatSpanishDateLabel(paymentDate)
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    val resolvedKind = if (parsedAmountDop < chosenEvent.balanceDop) "Abono" else "Saldo"
                                    val resolvedBank = if (selectedMethod == "Transferencia") selectedBank else selectedMethod
                                    val recorded = activeVm.submitDetailedPaymentFlow(
                                        eventId = chosenEvent.id,
                                        amountDop = parsedAmountDop,
                                        methodLabel = "$selectedMethod · $readablePaymentDate",
                                        bankName = resolvedBank,
                                        referenceCode = cleanRef.ifBlank { "EFE-CAJA" },
                                        voucherUrl = activeVoucherUrl,
                                        ncfType = activeNcfType,
                                        clientRnc = activeClientRnc.trim(),
                                        kindLabel = resolvedKind,
                                    )
                                    if (recorded != null) {
                                        if (sendWhatsAppReceipt) {
                                            activeVm.exportReceiptDocument(
                                                event = chosenEvent,
                                                payment = recorded,
                                                shareInsteadOfSave = true,
                                            )
                                        }
                                        onSelectScreenCode(dismissRoute)
                                    }
                                } else {
                                    onSelectScreenCode(dismissRoute)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        if (showPaymentDatePicker) {
            RfInteractiveCalendarModal(
                title = "Fecha del pago",
                currentDateText = paymentDate,
                minYear = 2024,
                isAdmin = true,
                dateMode = RfCalendarDateMode.PAYMENT_PAST_OR_TODAY,
                onDismiss = { showPaymentDatePicker = false },
                onSelectDate = { formattedDate ->
                    hasDateInteracted = true
                    paymentDate = formattedDate
                    localErrorText = null
                    showPaymentDatePicker = false
                },
            )
        }
    }
}

// ==============================================================================
// A13 · DETALLE DEL EVENTO (ADMIN)
// ==============================================================================
@Composable
internal fun RenderAdminEventDetailA13(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    if (viewModel != null) {
        if (eventsList.isEmpty()) {
            RenderAdminEmptyFeatureState(
                headerTitle = "Detalle del evento",
                emptyTitle = "Aún no tienes eventos",
                emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
                backTargetCode = "BACK:A11",
                iconKind = RfIconKind.CALENDAR,
                onSelectScreenCode = onSelectScreenCode,
                ctaLabel = "Crear evento",
                ctaTargetCode = "A04",
            )
            return
        }
    }
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = resolveAdminEventOrNull(
        selectedEventId = selectedEventId,
        activeEvent = viewModel?.activeClientEvent(),
        eventsList = eventsList,
        isPreviewMode = viewModel == null,
    )
    if (selectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Detalle del evento",
            emptyTitle = "Aún no tienes eventos",
            emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
            backTargetCode = "BACK:A11",
            iconKind = RfIconKind.CALENDAR,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }
    val checklistList = viewModel?.checklist?.collectAsState()?.value ?: emptyList()
    val eventTasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value

    val fallbackEmptyClientLabel = if (viewModel == null) "Camila Rodríguez" else "Sin cliente"
    val clientName = selectedEvent.clientName.ifBlank { fallbackEmptyClientLabel }
    val clientInitial = InputValidator.extractInitials(clientName, fallback = "C", maxInitials = 1)
    val paidDop = selectedEvent.paidDepositDop
    val remainingDop = (selectedEvent.totalDop - paidDop).coerceAtLeast(0)
    val safeTotalForProgress = selectedEvent.totalDop.coerceAtLeast(1)
    val paidFraction = (paidDop.toFloat() / safeTotalForProgress.toFloat()).coerceIn(0.05f, 1f)

    val rentedItems = if (viewModel == null) {
        selectedEvent.items.ifEmpty { CanonicalSampleData.events.first().items }
    } else {
        selectedEvent.items
    }
    val itemTypesCount = rentedItems.size
    val totalRentedUnits = rentedItems.sumOf { lineItem -> lineItem.quantity }

    val matchingChecklist = checklistList.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = matchingChecklist.ifEmpty { checklistList }
    val doneStepsCount = activeChecklist.count { stepItem -> stepItem.completed } + eventTasks.count { taskItem -> taskItem.completed }
    val rawTotalSteps = activeChecklist.size + eventTasks.size
    val totalStepsCount = if (viewModel == null) rawTotalSteps.coerceAtLeast(6) else rawTotalSteps
    val tasksProgressSubtitle = "$doneStepsCount de $totalStepsCount hechas"

    var showMoreOptionsModal by remember { mutableStateOf(false) }
    var showRentedItemsModal by remember { mutableStateOf(false) }

    val heroPhotoKey = photoKeyForEventType(selectedEvent.title)
    val daysBadgeText = when {
        selectedEvent.daysUntilEvent == 0 -> "Hoy"
        selectedEvent.daysUntilEvent == 1 -> "Mañana"
        selectedEvent.daysUntilEvent > 1 -> "En ${selectedEvent.daysUntilEvent} días"
        else -> formatReadableSpanishDateText(selectedEvent.dateLabel)
    }
    val executionCtaBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))

    val openClientChat: () -> Unit = {
        viewModel?.selectChatThreadForContact(clientName, selectedEvent.id)
        onSelectScreenCode("A15")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A11") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = selectedEvent.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "${formatReadableSpanishDateText(selectedEvent.dateLabel)} · ${selectedEvent.venue}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { showMoreOptionsModal = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }
                }

                if (!formFeedback.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rfAdminSuccessPillBg())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = formFeedback,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk()),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(20.dp)),
                ) {
                    RfPhoto(photoKey = heroPhotoKey, contentDescription = selectedEvent.title, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(0.3f to Color.Transparent, 1.0f to Color(0x8C151729))),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(daysBadgeText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E1A47)))
                        }

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(rfAdminTealPillBg())
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(selectedEvent.statusLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminTealPillInk()))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RfColors.VioletaTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = RfColors.VioletaTintInk))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = clientName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = "${selectedEvent.guests} invitados · ${selectedEvent.venue}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }

                        if (selectedEvent.clientName.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.surface)
                                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                                    .clickable { openClientChat() },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHAT, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Pagos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink))
                            Row(verticalAlignment = Alignment.Bottom) {
                                BasicText("Total ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                                BasicText(formatAdminDop(selectedEvent.totalDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RfThemeState.currentPalette.line),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(paidFraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AdminButtonGradient),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Row {
                                    BasicText("Pagado ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                                    BasicText(formatAdminDop(paidDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink))
                                }
                                BasicText("Faltan ${formatAdminDop(remainingDop)}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = rfAdminWarnPillInk()))
                            }

                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .background(AdminButtonGradient)
                                    .clickable {
                                        viewModel?.selectEvent(selectedEvent.id)
                                        onSelectScreenCode("A21")
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 14.dp)
                                    BasicText("Pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .clickable { showRentedItemsModal = true }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminSuccessPillBg()),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.BOX, tint = rfAdminSuccessPillInk(), size = 18.dp)
                            }
                            Column {
                                BasicText("Artículos alquilados", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText("$itemTypesCount tipos · $totalRentedUnits unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel?.selectEvent(selectedEvent.id)
                                onSelectScreenCode("A17")
                            }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminIndigoPillBg()),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECKLIST, tint = rfAdminIndigoPillInk(), size = 18.dp)
                            }
                            Column {
                                BasicText("Tareas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText(tasksProgressSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel?.selectEvent(selectedEvent.id)
                                onSelectScreenCode("A18")
                            }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminWarnPillBg()),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.COPY, tint = rfAdminWarnPillInk(), size = 18.dp)
                            }
                            Column {
                                BasicText("Boceto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText("Ver plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .clickable { openClientChat() }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminTealPillBg()),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHAT, tint = rfAdminTealPillInk(), size = 18.dp)
                            }
                            Column {
                                BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText(clientName.substringBefore(" "), maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
                        .clickable {
                            viewModel?.selectEvent(selectedEvent.id)
                            onSelectScreenCode("A19")
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RfColors.VioletaTint),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = RfColors.VioletaTintInk, size = 18.dp)
                            }
                            Column {
                                BasicText("Retorno e inspección final", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText("Revisar devolución de mobiliario y cierre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(executionCtaBrush)
                        .clickable {
                            viewModel?.selectEvent(selectedEvent.id)
                            onSelectScreenCode("A16")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.PLAY, tint = Color.White, size = 20.dp, filled = true)
                        BasicText("Modo ejecución", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        AdminBottomSheetOverlay(
            visible = showMoreOptionsModal,
            onDismiss = { showMoreOptionsModal = false },
            outerPadding = 20.dp,
            shape = RoundedCornerShape(24.dp),
            innerHorizontalPadding = 18.dp,
            innerVerticalPadding = 18.dp,
            verticalSpacing = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Opciones del evento", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showMoreOptionsModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            val optionEntries = listOf(
                Triple("Duplicar evento", "Crear copia editable en agenda", RfIconKind.COPY) to {
                    val duplicatedEvent = viewModel?.duplicateExistingEvent(selectedEvent.id)
                    if (duplicatedEvent != null) {
                        viewModel.selectEvent(duplicatedEvent.id)
                    }
                    showMoreOptionsModal = false
                    onSelectScreenCode("A11")
                },
                Triple("Descargar orden PDF", "Guardar orden operativa del evento", RfIconKind.SEND) to {
                    val generatedFile = viewModel?.exportReceiptDocument(selectedEvent, null, shareInsteadOfSave = false)
                    if (generatedFile != null) {
                        viewModel.postFeedback("Orden PDF descargada: $generatedFile")
                    }
                    showMoreOptionsModal = false
                },
                Triple("Registrar pago", "Abonar anticipo o saldo pendiente", RfIconKind.PLUS) to {
                    viewModel?.selectEvent(selectedEvent.id)
                    showMoreOptionsModal = false
                    onSelectScreenCode("A21")
                },
                Triple("Inspección y cierre", "Revisar devolución y daños", RfIconKind.CHECKLIST) to {
                    viewModel?.selectEvent(selectedEvent.id)
                    showMoreOptionsModal = false
                    onSelectScreenCode("A19")
                },
            )

            optionEntries.forEach { optionPair ->
                val meta = optionPair.first
                val actionHandler = optionPair.second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { actionHandler() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = meta.third, tint = rfAdminIndigoPillInk(), size = 18.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(meta.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                        BasicText(meta.second, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }
        }

        AdminBottomSheetOverlay(
            visible = showRentedItemsModal,
            onDismiss = { showRentedItemsModal = false },
            outerPadding = 20.dp,
            shape = RoundedCornerShape(24.dp),
            innerHorizontalPadding = 18.dp,
            innerVerticalPadding = 18.dp,
            verticalSpacing = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Artículos alquilados", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                    BasicText("$itemTypesCount líneas · $totalRentedUnits unidades reservadas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showRentedItemsModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rentedItems.forEach { rentedLine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfPhoto(
                            photoKey = photoKeyForAsset(rentedLine.photo),
                            contentDescription = rentedLine.name,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = rentedLine.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = "${rentedLine.articleCode} · ${rentedLine.quantity} unid.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        BasicText(
                            text = formatAdminDop(rentedLine.subtotalDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AdminButtonGradient)
                    .clickable { showRentedItemsModal = false },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Cerrar detalle", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

private data class ExecutionStageRow(
    val rowKey: String,
    val domainStepId: String?,
    val stageIndex: Int,
    val done: Boolean,
    val title: String,
    val subtitle: String,
    val avatarLetter: String,
    val avatarColors: Pair<Color, Color>,
)

private fun classifyChecklistStageIndex(rawStageLabel: String): Int {
    val normalized = rawStageLabel.trim().lowercase()
    val isLoadKeyword = eitherCondition(normalized.contains("carga"), normalized.contains("camión"))
    val isWarehouseKeyword = eitherCondition(normalized.contains("almac"), normalized.contains("prepar"))
    val isPreKeyword = eitherCondition(normalized.contains("logíst"), normalized.contains("prev"))
    val matchesLoadGroup = eitherCondition(eitherCondition(isLoadKeyword, isWarehouseKeyword), isPreKeyword)
    val isTeardownKeyword = eitherCondition(normalized.contains("desmont"), normalized.contains("recog"))
    val isCloseoutKeyword = eitherCondition(normalized.contains("retir"), normalized.contains("cierre"))
    val matchesTeardownGroup = eitherCondition(isTeardownKeyword, isCloseoutKeyword)
    return when {
        matchesLoadGroup -> 0
        matchesTeardownGroup -> 2
        else -> 1
    }
}

private fun resolveAssigneeAvatarPalette(rawAssignee: String): Pair<String, Pair<Color, Color>> {
    val cleanName = rawAssignee.trim()
    val initial = InputValidator.extractInitials(cleanName, fallback = "M", maxInitials = 1)
    val normalized = cleanName.lowercase()
    val colors = when {
        normalized.startsWith("p") -> Pair(rfAdminTealPillBg(), rfAdminTealPillInk())
        normalized.startsWith("y") -> Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)
        normalized.startsWith("a") -> Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())
        else -> Pair(rfAdminIndigoPillBg(), rfAdminIndigoPillInk())
    }
    return Pair(initial, colors)
}

@Composable
internal fun RenderAdminExecutionA16(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    if (viewModel != null) {
        if (eventsList.isEmpty()) {
            RenderAdminEmptyFeatureState(
                headerTitle = "Modo ejecución",
                emptyTitle = "Aún no tienes eventos",
                emptyMessage = "Crea un evento para controlar carga, checklist en sitio y entregas.",
                backTargetCode = "BACK:A11",
                iconKind = RfIconKind.CALENDAR,
                onSelectScreenCode = onSelectScreenCode,
                ctaLabel = "Crear evento",
                ctaTargetCode = "A04",
            )
            return
        }
    }
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = resolveAdminEventOrNull(
        selectedEventId = selectedEventId,
        activeEvent = viewModel?.activeClientEvent(),
        eventsList = eventsList,
        isPreviewMode = viewModel == null,
    )
    if (selectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Detalle del evento",
            emptyTitle = "Aún no tienes eventos",
            emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
            backTargetCode = "BACK:A11",
            iconKind = RfIconKind.CALENDAR,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }
    val deliveryStops = viewModel?.deliveryStops?.collectAsState()?.value ?: emptyList()
    val allChecklist = viewModel?.checklist?.collectAsState()?.value ?: emptyList()

    val matchingStop = deliveryStops.firstOrNull { stopItem ->
        val matchesId = bothConditions(stopItem.eventId.isNotBlank(), stopItem.eventId == selectedEvent.id)
        val matchesTitle = stopItem.eventTitle.equals(selectedEvent.title, ignoreCase = true)
        eitherCondition(matchesId, matchesTitle)
    } ?: deliveryStops.firstOrNull()

    val eventSpecificChecklist = allChecklist.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = eventSpecificChecklist.ifEmpty { allChecklist }

    var selectedPhaseIndex by remember { mutableStateOf(1) }
    var fallbackDoneByKey by remember(selectedEvent.id) {
        mutableStateOf(
            mapOf(
                "load-0" to true,
                "load-1" to true,
                "load-2" to true,
                "montage-0" to true,
                "montage-1" to true,
                "montage-2" to false,
                "montage-3" to false,
                "teardown-0" to false,
                "teardown-1" to false,
                "teardown-2" to false,
            ),
        )
    }
    var uploadedPhotos by remember(selectedEvent.id, selectedEvent.setupPhotoUrls) {
        mutableStateOf(selectedEvent.setupPhotoUrls)
    }

    val domainStageRows = activeChecklist.map { stepItem ->
        val stageIdx = classifyChecklistStageIndex(stepItem.stageLabel)
        val assigneeLabel = stepItem.completedBy.ifBlank { "Equipo" }
        val avatarInfo = resolveAssigneeAvatarPalette(assigneeLabel)
        ExecutionStageRow(
            rowKey = "domain-${stepItem.id}",
            domainStepId = stepItem.id,
            stageIndex = stageIdx,
            done = stepItem.completed,
            title = stepItem.title,
            subtitle = "${stepItem.stageLabel} · ${stepItem.timeLabel}",
            avatarLetter = avatarInfo.first,
            avatarColors = avatarInfo.second,
        )
    }

    val hasRealEventSelected = selectedEvent.id.isNotBlank()
    val usePreviewExecutionFallback = if (viewModel == null) true else hasRealEventSelected
    val loadRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 0 }.let { filtered ->
        val showFallback = bothConditions(filtered.isEmpty(), usePreviewExecutionFallback)
        if (showFallback) {
            listOf(
                ExecutionStageRow("load-0", null, 0, fallbackDoneByKey["load-0"] ?: true, "Verificar orden de carga en almacén", "Conteo de sillas, mesas y mantelería", "A", Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())),
                ExecutionStageRow("load-1", null, 0, fallbackDoneByKey["load-1"] ?: true, "Proteger estructuras y bases florales", "Embalaje acolchado en camión", "P", Pair(rfAdminTealPillBg(), rfAdminTealPillInk())),
                ExecutionStageRow("load-2", null, 0, fallbackDoneByKey["load-2"] ?: true, "Salida de ruta hacia el salón", matchingStop?.vehicleAndCrew ?: "Camión principal · cuadrilla lista", "A", Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())),
            )
        } else {
            filtered
        }
    }

    val montageRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 1 }.let { filtered ->
        val showFallback = bothConditions(filtered.isEmpty(), usePreviewExecutionFallback)
        if (showFallback) {
            listOf(
                ExecutionStageRow("montage-0", null, 1, fallbackDoneByKey["montage-0"] ?: true, "Colocar mesas según plano aprobado", "${selectedEvent.guests} invitados · distribución validada", "P", Pair(rfAdminTealPillBg(), rfAdminTealPillInk())),
                ExecutionStageRow("montage-1", null, 1, fallbackDoneByKey["montage-1"] ?: true, "Vestir mesas y sillas Tiffany", "Mantelería blanca y lazos alineados", "Y", Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)),
                ExecutionStageRow("montage-2", null, 1, fallbackDoneByKey["montage-2"] ?: false, "Montar arco principal y fondo", "Entrada principal · nivelación segura", "Y", Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)),
                ExecutionStageRow("montage-3", null, 1, fallbackDoneByKey["montage-3"] ?: false, "Ubicar centros de mesa florales", "Revisión final de iluminación y detalles", "M", Pair(rfAdminIndigoPillBg(), rfAdminIndigoPillInk())),
            )
        } else {
            filtered
        }
    }

    val teardownRows = domainStageRows.filter { rowItem -> rowItem.stageIndex == 2 }.let { filtered ->
        val showFallback = bothConditions(filtered.isEmpty(), usePreviewExecutionFallback)
        if (showFallback) {
            listOf(
                ExecutionStageRow("teardown-0", null, 2, fallbackDoneByKey["teardown-0"] ?: false, "Recoger bases florales y cristalería", "Separar piezas frágiles en cajas", "Y", Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)),
                ExecutionStageRow("teardown-1", null, 2, fallbackDoneByKey["teardown-1"] ?: false, "Plegar y contar sillas y mesas", "Verificar estado antes de subir", "P", Pair(rfAdminTealPillBg(), rfAdminTealPillInk())),
                ExecutionStageRow("teardown-2", null, 2, fallbackDoneByKey["teardown-2"] ?: false, "Retorno a almacén y cierre de ruta", "Entrega de llaves y acta firmada", "A", Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())),
            )
        } else {
            filtered
        }
    }

    val allStageRows = loadRows + montageRows + teardownRows
    val totalCompletedCount = allStageRows.count { rowItem -> rowItem.done }
    val totalRowCount = allStageRows.size.coerceAtLeast(1)
    val loadDoneCount = loadRows.count { rowItem -> rowItem.done }
    val montageDoneCount = montageRows.count { rowItem -> rowItem.done }
    val teardownDoneCount = teardownRows.count { rowItem -> rowItem.done }

    val displayedStageRows = when (selectedPhaseIndex) {
        0 -> loadRows
        2 -> teardownRows
        else -> montageRows
    }
    val activePhaseBadgeLabel = when (selectedPhaseIndex) {
        0 -> "Hoy · Carga"
        2 -> "Hoy · Desmontaje"
        else -> "Hoy · Montaje"
    }
    val venueTitle = matchingStop?.venue?.ifBlank { selectedEvent.venue } ?: selectedEvent.venue
    val venueSubtitle = if (matchingStop != null) {
        "${matchingStop.windowLabel} · ${matchingStop.vehicleAndCrew} · ${selectedEvent.clientName}"
    } else {
        "${formatReadableSpanishDateText(selectedEvent.dateLabel)} · Contacto: ${selectedEvent.clientName}"
    }
    val headerHeroBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(headerHeroBrush)
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 22.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                                    .clip(CircleShape)
                                    .background(Color(0x2EFFFFFF))
                                    .clickable { onSelectScreenCode("BACK:A13") },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD45C))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF2E1A47)))
                                    BasicText(activePhaseBadgeLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(78.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0x38FFFFFF), style = Stroke(width = 8.dp.toPx()))
                                    drawArc(
                                        color = Color(0xFFFFD45C),
                                        startAngle = -90f,
                                        sweepAngle = (totalCompletedCount.toFloat() / totalRowCount.toFloat()) * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                                    )
                                }
                                BasicText("$totalCompletedCount/$totalRowCount", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = selectedEvent.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                BasicText(
                                    text = "${matchingStop?.windowLabel ?: formatReadableSpanishDateText(selectedEvent.dateLabel)} · $venueTitle",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)),
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.line)
                            .padding(4.dp),
                    ) {
                        val phaseLabels = listOf(
                            "Carga · $loadDoneCount/${loadRows.size}",
                            "Montaje · $montageDoneCount/${montageRows.size}",
                            "Desmontaje · $teardownDoneCount/${teardownRows.size}",
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            phaseLabels.forEachIndexed { phaseIndex, phaseLabel ->
                                val isSelectedPhase = selectedPhaseIndex == phaseIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .then(
                                            if (isSelectedPhase) {
                                                Modifier.clip(RoundedCornerShape(12.dp)).background(RfThemeState.currentPalette.surface)
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .clickable { selectedPhaseIndex = phaseIndex },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = phaseLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelectedPhase) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelectedPhase) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminTealPillBg()),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.LOCATION, tint = rfAdminTealPillInk(), size = 20.dp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = venueTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                )
                                BasicText(
                                    text = venueSubtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(rfAdminIndigoPillBg())
                                    .clickable { viewModel?.openMapsSearch(venueTitle) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("Mapa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                            }
                        }
                    }

                    displayedStageRows.forEach { stageRow ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (stageRow.done) RfThemeState.currentPalette.chipBg else RfThemeState.currentPalette.surface)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                                .clickable {
                                    val stepId = stageRow.domainStepId
                                    if (stepId != null) {
                                        viewModel?.toggleChecklistStep(stepId)
                                    } else {
                                        val updatedMap = fallbackDoneByKey.toMutableMap()
                                        val currentFlag = updatedMap[stageRow.rowKey] ?: stageRow.done
                                        updatedMap[stageRow.rowKey] = !currentFlag
                                        fallbackDoneByKey = updatedMap
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (stageRow.done) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF8DB52C)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(RfThemeState.currentPalette.surface)
                                            .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(10.dp)),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = stageRow.title,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 15.sp,
                                            fontWeight = if (stageRow.done) FontWeight.Normal else FontWeight.Medium,
                                            color = if (stageRow.done) RfThemeState.currentPalette.muted else RfThemeState.currentPalette.ink,
                                            textDecoration = if (stageRow.done) TextDecoration.LineThrough else null,
                                        ),
                                    )
                                    BasicText(
                                        text = stageRow.subtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(stageRow.avatarColors.first),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(stageRow.avatarLetter, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = stageRow.avatarColors.second))
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                            .clip(RoundedCornerShape(18.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(18.dp))
                            .clickable {
                                if (viewModel != null) {
                                    viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                                        val fallbackLabel = pickedFile.fileName.ifBlank { "montaje-${uploadedPhotos.size + 1}.jpg" }
                                        viewModel.attachSetupPhotosToEvent(
                                            eventId = selectedEvent.id,
                                            files = listOf(pickedFile),
                                        ) { savedOk, persistedUrls ->
                                            val hasPersisted = persistedUrls.isNotEmpty()
                                            val shouldReplace = if (savedOk) hasPersisted else false
                                            if (shouldReplace) {
                                                uploadedPhotos = persistedUrls
                                            } else {
                                                val appended = if (hasPersisted) persistedUrls else listOf(fallbackLabel)
                                                uploadedPhotos = (uploadedPhotos + appended).distinct()
                                            }
                                        }
                                    }
                                } else {
                                    val nextDemoPhoto = "montaje-${uploadedPhotos.size + 1}.jpg"
                                    uploadedPhotos = (uploadedPhotos + nextDemoPhoto).distinct()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(rfAdminIndigoPillBg()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CAMERA, tint = rfAdminIndigoPillInk(), size = 18.dp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText("Subir fotos del montaje", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                    BasicText(
                                        text = if (uploadedPhotos.isEmpty()) "Adjuntar evidencia visual de la etapa" else "${uploadedPhotos.size} foto(s) adjuntada(s)",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            }

                            if (uploadedPhotos.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    uploadedPhotos.forEach { photoName ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(rfAdminSuccessPillBg())
                                                .clickable {
                                                    val remainingPhotos = uploadedPhotos.filterNot { itemPhoto -> itemPhoto == photoName }
                                                    uploadedPhotos = remainingPhotos
                                                    viewModel?.saveEventSetupPhotoUrls(
                                                        eventId = selectedEvent.id,
                                                        photoUrls = remainingPhotos,
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                        ) {
                                            BasicText(
                                                text = "$photoName ×",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk()),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            viewModel?.postFeedback("Avance operativo actualizado ($totalCompletedCount/$totalRowCount).")
                            onSelectScreenCode("A13")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                        BasicText("Guardar avance y volver", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

private data class TeamTaskDisplayEntry(
    val entryKey: String,
    val checklistStepId: String?,
    val eventTaskId: String?,
    val groupHeader: String,
    val title: String,
    val subtitle: String,
    val assigneeName: String,
    val completed: Boolean,
)

private fun resolveStaffFirstNamesForTasks(staffMembers: List<StaffMember>): List<String> {
    val defaultNames = listOf("María", "Pedro", "Yuli", "Andrés")
    val dynamicNames = staffMembers.map { staffItem ->
        staffItem.name.trim().substringBefore(" ")
    }.filter { firstName -> firstName.isNotEmpty() }
    val combined = defaultNames + dynamicNames
    val uniqueList = mutableListOf<String>()
    combined.forEach { candidateName ->
        val alreadyExists = uniqueList.any { existingName -> existingName.equals(candidateName, ignoreCase = true) }
        if (!alreadyExists) {
            uniqueList.add(candidateName)
        }
    }
    return uniqueList
}

@Composable
internal fun RenderAdminTasksA17(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    if (viewModel != null) {
        if (eventsList.isEmpty()) {
            RenderAdminEmptyFeatureState(
                headerTitle = "Tareas del evento",
                emptyTitle = "Aún no tienes eventos",
                emptyMessage = "Crea un evento para asignar responsables de montaje, logística y decoración.",
                backTargetCode = "BACK:A11",
                iconKind = RfIconKind.CALENDAR,
                onSelectScreenCode = onSelectScreenCode,
                ctaLabel = "Crear evento",
                ctaTargetCode = "A04",
            )
            return
        }
    }
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = resolveAdminEventOrNull(
        selectedEventId = selectedEventId,
        activeEvent = viewModel?.activeClientEvent(),
        eventsList = eventsList,
        isPreviewMode = viewModel == null,
    )
    if (selectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Detalle del evento",
            emptyTitle = "Aún no tienes eventos",
            emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
            backTargetCode = "BACK:A11",
            iconKind = RfIconKind.CALENDAR,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }
    val allChecklist = viewModel?.checklist?.collectAsState()?.value ?: emptyList()
    val eventTasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val staffMembers = viewModel?.staffMembers?.collectAsState()?.value ?: emptyList()
    LaunchedEffect(viewModel, selectedEvent.id) {
        viewModel?.loadTasksForActiveEvent()
    }

    val assigneeOptions = resolveStaffFirstNamesForTasks(staffMembers)

    var selectedMemberChip by remember { mutableStateOf("Todas") }
    var showNewTaskModal by remember { mutableStateOf(false) }
    var pendingDeleteTask by remember { mutableStateOf<TeamTaskDisplayEntry?>(null) }
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    val taskTitleValidator: (String) -> String? = { rawTitle ->
        InputValidator.validateEntityName(
            rawValue = rawTitle,
            fieldLabel = "El título de la tarea",
            minLength = 3,
            maxLength = InputValidator.MAX_EVENT_NAME_LENGTH,
            isRequired = true,
        )
    }
    val taskTitleField = rememberDebouncedFieldController(
        initialValue = "",
        validator = taskTitleValidator,
    )
    var selectedTaskAssignee by remember(assigneeOptions) {
        mutableStateOf(assigneeOptions.firstOrNull() ?: "María")
    }

    var localFallbackTasks by remember(selectedEvent.id) {
        mutableStateOf(emptyList<TeamTaskDisplayEntry>())
    }

    val matchingChecklist = allChecklist.filter { stepItem -> stepItem.eventId == selectedEvent.id }
    val activeChecklist = matchingChecklist.ifEmpty { allChecklist }

    val domainChecklistEntries = activeChecklist.mapIndexed { stepIdx, stepItem ->
        val fallbackAssignee = assigneeOptions[stepIdx % assigneeOptions.size.coerceAtLeast(1)]
        val resolvedAssignee = stepItem.completedBy.trim().ifBlank { fallbackAssignee }.substringBefore(" ")
        TeamTaskDisplayEntry(
            entryKey = "chk-${stepItem.id}",
            checklistStepId = stepItem.id,
            eventTaskId = null,
            groupHeader = stepItem.stageLabel.uppercase(),
            title = stepItem.title,
            subtitle = stepItem.timeLabel,
            assigneeName = resolvedAssignee,
            completed = stepItem.completed,
        )
    }

    val customTaskEntries = eventTasks.map { taskItem ->
        val rawBadge = taskItem.badgeText.trim()
        val matchesKnownStaff = assigneeOptions.firstOrNull { staffName ->
            rawBadge.contains(staffName, ignoreCase = true)
        }
        val resolvedAssignee = matchesKnownStaff ?: assigneeOptions.firstOrNull() ?: "María"
        TeamTaskDisplayEntry(
            entryKey = "evt-${taskItem.id}",
            checklistStepId = null,
            eventTaskId = taskItem.id,
            groupHeader = "TAREAS DEL EQUIPO",
            title = taskItem.title,
            subtitle = "Asignada a $resolvedAssignee · ${rawBadge.ifBlank { "Pendiente" }}",
            assigneeName = resolvedAssignee,
            completed = taskItem.completed,
        )
    }

    val useDomainChecklist = eitherCondition(domainChecklistEntries.isNotEmpty(), viewModel != null)
    val baseEntries = if (useDomainChecklist) domainChecklistEntries else localFallbackTasks
    val combinedEntries = customTaskEntries + baseEntries
    val filteredEntries = combinedEntries.filter { entryItem ->
        val showAll = selectedMemberChip == "Todas"
        val matchesAssignee = entryItem.assigneeName.equals(selectedMemberChip, ignoreCase = true)
        eitherCondition(showAll, matchesAssignee)
    }
    val groupedEntries = filteredEntries.groupBy { entryItem -> entryItem.groupHeader }
    val activeChipGradient = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Tareas del equipo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                    )
                    BasicText(
                        text = selectedEvent.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                        .clip(CircleShape)
                        .background(activeChipGradient)
                        .clickable {
                            taskTitleField.reset("")
                            showNewTaskModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val isAllSelected = selectedMemberChip == "Todas"
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isAllSelected) {
                                Modifier.background(activeChipGradient)
                            } else {
                                Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                            },
                        )
                        .clickable { selectedMemberChip = "Todas" }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Todas",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAllSelected) Color.White else RfThemeState.currentPalette.ink,
                        ),
                    )
                }

                assigneeOptions.forEach { memberName ->
                    val palette = resolveAssigneeAvatarPalette(memberName)
                    val isMemberSelected = selectedMemberChip.equals(memberName, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .then(
                                if (isMemberSelected) {
                                    Modifier.background(activeChipGradient)
                                } else {
                                    Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                                },
                            )
                            .clickable { selectedMemberChip = memberName }
                            .padding(start = 5.dp, end = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(palette.second.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(palette.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = palette.second.second))
                            }
                            BasicText(
                                text = memberName,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMemberSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isMemberSelected) Color.White else RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }
            }

            if (groupedEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Sin tareas asignadas a $selectedMemberChip en este evento.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                    )
                }
            }

            groupedEntries.forEach { groupPair ->
                val sectionTitle = groupPair.key
                val sectionTasks = groupPair.value

                BasicText(
                    text = sectionTitle,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                    modifier = Modifier.padding(top = 2.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        sectionTasks.forEachIndexed { taskIndex, taskEntry ->
                            if (taskIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                            }
                            val avatarMeta = resolveAssigneeAvatarPalette(taskEntry.assigneeName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val stepId = taskEntry.checklistStepId
                                        val evtTaskId = taskEntry.eventTaskId
                                        if (stepId != null) {
                                            viewModel?.toggleChecklistStep(stepId)
                                        } else if (evtTaskId != null) {
                                            viewModel?.toggleEventTask(evtTaskId)
                                        } else {
                                            localFallbackTasks = localFallbackTasks.map { existingRow ->
                                                if (existingRow.entryKey == taskEntry.entryKey) {
                                                    existingRow.copy(completed = !existingRow.completed)
                                                } else {
                                                    existingRow
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (taskEntry.completed) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8DB52C)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = taskEntry.title,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (taskEntry.completed) RfThemeState.currentPalette.muted else RfThemeState.currentPalette.ink,
                                            textDecoration = if (taskEntry.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        ),
                                    )
                                    BasicText(
                                        text = taskEntry.subtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(avatarMeta.second.first),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(avatarMeta.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = avatarMeta.second.second))
                                    }
                                    val evtTaskId = taskEntry.eventTaskId
                                    if (viewModel != null) {
                                        if (evtTaskId != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(rfAdminDangerPillBg())
                                                    .clickable { pendingDeleteTask = taskEntry },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                RfIcon(kind = RfIconKind.CLOSE, tint = rfAdminDangerPillInk(), size = 13.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AdminCenteredDialogOverlay(
            visible = showNewTaskModal,
            onDismiss = { showNewTaskModal = false },
            verticalSpacing = 12.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Nueva tarea de equipo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showNewTaskModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            AdminDebouncedInputField(
                controller = taskTitleField,
                validator = taskTitleValidator,
                label = "Título de la tarea",
                placeholder = "Ej. Verificar mantelería dorada",
                containerColor = RfThemeState.currentPalette.chipBg,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                labelFontSize = 13.sp,
                labelColor = RfThemeState.currentPalette.ink,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Responsable", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    assigneeOptions.forEach { assigneeOption ->
                        val isAssigneeSelected = selectedTaskAssignee.equals(assigneeOption, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isAssigneeSelected) {
                                        Modifier.background(activeChipGradient)
                                    } else {
                                        Modifier.background(RfThemeState.currentPalette.chipBg)
                                    },
                                )
                                .clickable { selectedTaskAssignee = assigneeOption }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = assigneeOption,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAssigneeSelected) Color.White else RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeChipGradient)
                    .clickable {
                        val titleError = taskTitleField.currentImmediateError()
                        if (titleError != null) {
                            taskTitleField.markSubmitted()
                        } else {
                            val cleanTitle = taskTitleField.value.trim()
                            if (viewModel != null) {
                                viewModel.addEventTask(cleanTitle, selectedTaskAssignee)
                            } else {
                                val createdKey = "fb-new-${localFallbackTasks.size + 1}"
                                val addedEntry = TeamTaskDisplayEntry(
                                    entryKey = createdKey,
                                    checklistStepId = null,
                                    eventTaskId = null,
                                    groupHeader = "TAREAS DEL EQUIPO",
                                    title = cleanTitle,
                                    subtitle = "Asignada a $selectedTaskAssignee",
                                    assigneeName = selectedTaskAssignee,
                                    completed = false,
                                )
                                localFallbackTasks = listOf(addedEntry) + localFallbackTasks
                            }
                            taskTitleField.reset("")
                            showNewTaskModal = false
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Crear tarea", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }

        val taskToDelete = pendingDeleteTask
        val taskIdToDelete = taskToDelete?.eventTaskId
        RfDeleteConfirmationDialog(
            visible = taskIdToDelete != null,
            title = "Eliminar tarea",
            message = if (taskToDelete != null) {
                "¿Seguro que deseas eliminar \"${taskToDelete.title}\"? Esta acción no se puede deshacer."
            } else {
                ""
            },
            errorMessage = formError,
            isSubmitting = isSubmitting,
            onDismiss = { pendingDeleteTask = null },
            onConfirm = {
                if (taskIdToDelete != null) {
                    viewModel?.deleteEventTask(taskIdToDelete)
                    pendingDeleteTask = null
                }
            },
        )
    }
}

// ==============================================================================
// A18 · PLANO Y DISTRIBUCIÓN
// ==============================================================================
private fun parseTablesCountFromBlueprintJson(rawSketchJson: String): Int? {
    val trimmed = rawSketchJson.trim()
    if (!trimmed.startsWith("{")) return null
    val marker = "\"tables\":"
    val markerIndex = trimmed.indexOf(marker)
    if (markerIndex < 0) return null
    val digits = trimmed
        .substring(markerIndex + marker.length)
        .trimStart()
        .takeWhile { character -> character.isDigit() }
    return digits.toIntOrNull()?.coerceIn(1, 16)
}

private fun mergeAdminTableCountIntoSketchElements(
    existingElements: List<SketchCanvasElement>,
    targetTableCount: Int,
): List<SketchCanvasElement> {
    val existingTables = existingElements.filter { element -> isTableElementKind(element.kind) }
    if (existingTables.size == targetTableCount) {
        return existingElements
    }
    val nonTableElements = existingElements.filterNot { element -> isTableElementKind(element.kind) }
    val retainedTables = existingTables.take(targetTableCount)
    val missingTablesCount = (targetTableCount - retainedTables.size).coerceAtLeast(0)
    if (missingTablesCount == 0) {
        return nonTableElements + retainedTables
    }
    val candidateOffsets = buildTableCanvasOffsets(targetTableCount)
    val appendedTables = (retainedTables.size until targetTableCount).map { tableIdx ->
        val rawOffset = candidateOffsets[tableIdx.coerceAtMost(candidateOffsets.lastIndex)]
        SketchCanvasElement(
            id = "el-admin-tbl-${tableIdx + 1}",
            kind = SketchElementKind.ROUND_TABLE,
            normX = (rawOffset.x / 358f).coerceIn(0.08f, 0.92f),
            normY = (rawOffset.y / 440f).coerceIn(0.10f, 0.90f),
            colorHex = 0xFF5B6CF0,
            label = "Mesa ${tableIdx + 1}",
        )
    }
    return nonTableElements + retainedTables + appendedTables
}

private fun buildSketchBlueprintJson(
    eventId: String,
    tableCount: Int,
    guestsCount: Int,
    viewMode: String,
): String {
    val chairsCount = tableCount * 10
    return "{\"eventId\":\"$eventId\",\"mode\":\"$viewMode\",\"roomMeters\":\"12.0x8.0\",\"corridorMeters\":1.8,\"powerOutlets\":\"110V\",\"tables\":$tableCount,\"chairs\":$chairsCount,\"guests\":$guestsCount}"
}

private fun buildTableCanvasOffsets(tableCount: Int): List<Offset> {
    val candidateSlots = listOf(
        Offset(82f, 150f),
        Offset(179f, 150f),
        Offset(276f, 150f),
        Offset(74f, 232f),
        Offset(284f, 232f),
        Offset(74f, 314f),
        Offset(284f, 314f),
        Offset(125f, 376f),
        Offset(233f, 376f),
        Offset(125f, 206f),
        Offset(233f, 206f),
        Offset(179f, 376f),
        Offset(56f, 108f),
        Offset(302f, 108f),
        Offset(56f, 376f),
        Offset(302f, 376f),
    )
    val clampedCount = tableCount.coerceIn(1, candidateSlots.size)
    return candidateSlots.take(clampedCount)
}

@Composable
internal fun RenderAdminSketchA18(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    if (viewModel != null) {
        if (eventsList.isEmpty()) {
            RenderAdminEmptyFeatureState(
                headerTitle = "Boceto y plano",
                emptyTitle = "Aún no tienes eventos",
                emptyMessage = "Crea un evento para editar planos técnicos y puntos de montaje.",
                backTargetCode = "BACK:A11",
                iconKind = RfIconKind.CALENDAR,
                onSelectScreenCode = onSelectScreenCode,
                ctaLabel = "Crear evento",
                ctaTargetCode = "A04",
            )
            return
        }
    }
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = resolveAdminEventOrNull(
        selectedEventId = selectedEventId,
        activeEvent = viewModel?.activeClientEvent(),
        eventsList = eventsList,
        isPreviewMode = viewModel == null,
    )
    if (selectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Detalle del evento",
            emptyTitle = "Aún no tienes eventos",
            emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
            backTargetCode = "BACK:A11",
            iconKind = RfIconKind.CALENDAR,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }
    val parsedClientElements = remember(selectedEvent.id, selectedEvent.sketchJson) {
        parseSketchElements(selectedEvent.sketchJson)
    }
    val clientElementTableCount = remember(parsedClientElements) {
        parsedClientElements.count { element -> isTableElementKind(element.kind) }
    }
    val initialSketchTableCount = remember(selectedEvent.id, selectedEvent.sketchJson, selectedEvent.guests) {
        val suggestedTables = ((selectedEvent.guests + 9) / 10).coerceIn(4, 16)
        val jsonTables = parseTablesCountFromBlueprintJson(selectedEvent.sketchJson)
        when {
            clientElementTableCount > 0 -> clientElementTableCount.coerceIn(1, 16)
            jsonTables != null -> jsonTables
            else -> suggestedTables
        }
    }
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value

    var activeTableCount by remember(selectedEvent.id, selectedEvent.sketchJson) {
        mutableStateOf(initialSketchTableCount)
    }
    var selectedViewMode by remember(selectedEvent.id) {
        mutableStateOf(if (parsedClientElements.isNotEmpty()) "Boceto original" else "Plano")
    }
    var localSaveNotice by remember { mutableStateOf<String?>(null) }

    val calculatedSeats = activeTableCount * 10
    val clientFirstName = selectedEvent.clientName.trim().substringBefore(" ").ifBlank { "Cliente" }
    val isOriginalSelected = selectedViewMode == "Boceto original"
    val isPlanoSelected = selectedViewMode == "Plano"
    val primaryActionBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    val adjustTablesByDelta: (Int) -> Unit = { delta ->
        localSaveNotice = null
        val updatedCount = (activeTableCount + delta).coerceIn(1, 16)
        activeTableCount = updatedCount
        viewModel?.setSketchTableCount(updatedCount)
    }

    val saveCurrentBlueprint: () -> Unit = {
        val payloadJson = if (parsedClientElements.isNotEmpty()) {
            val mergedElements = mergeAdminTableCountIntoSketchElements(
                existingElements = parsedClientElements,
                targetTableCount = activeTableCount,
            )
            serializeSketchElements(mergedElements)
        } else {
            buildSketchBlueprintJson(
                eventId = selectedEvent.id,
                tableCount = activeTableCount,
                guestsCount = selectedEvent.guests,
                viewMode = selectedViewMode,
            )
        }
        if (viewModel != null) {
            viewModel.saveSketchLayoutJson(payloadJson, selectedEvent.id)
            viewModel.postFeedback("Plano guardado: $activeTableCount mesas ($calculatedSeats sillas).")
        } else {
            localSaveNotice = "Plano guardado: $activeTableCount mesas ($calculatedSeats sillas)."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A13") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Boceto de $clientFirstName",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                        )
                        BasicText(
                            text = "${selectedEvent.title} · ${selectedEvent.venue}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.line)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(
                                if (isOriginalSelected) {
                                    Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(RfThemeState.currentPalette.surface)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { selectedViewMode = "Boceto original" },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Boceto original",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isOriginalSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isOriginalSelected) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(
                                if (isPlanoSelected) {
                                    Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(RfThemeState.currentPalette.surface)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { selectedViewMode = "Plano" },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Plano",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isPlanoSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isPlanoSelected) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "$activeTableCount mesas · $calculatedSeats sillas",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "Meta del evento: ${selectedEvent.guests} invitados",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable { adjustTablesByDelta(-1) },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("−", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                        }
                        BasicText(
                            text = "$activeTableCount",
                            modifier = Modifier.widthIn(min = 24.dp),
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.Center),
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(primaryActionBrush)
                                .clickable { adjustTablesByDelta(1) },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("+", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }

                if (isOriginalSelected) {
                    val palette = RfThemeState.currentPalette
                    val originalCanvasGradient = if (palette.isDark) {
                        Brush.verticalGradient(listOf(palette.surface, palette.chipBg))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFFFF8FB), Color(0xFFF7F5FF)))
                    }
                    val originalCanvasBorder = if (palette.isDark) palette.line else Color(0xFFF3DCEB)
                    val displayedClientElements = remember(parsedClientElements, activeTableCount) {
                        if (parsedClientElements.isEmpty()) {
                            emptyList()
                        } else {
                            mergeAdminTableCountIntoSketchElements(
                                existingElements = parsedClientElements,
                                targetTableCount = activeTableCount,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(originalCanvasGradient)
                            .border(1.dp, originalCanvasBorder, RoundedCornerShape(20.dp)),
                    ) {
                        if (displayedClientElements.isNotEmpty()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                displayedClientElements.forEach { element ->
                                    drawSingleSketchElement(
                                        element = element,
                                        isThreeDimensionalPreview = false,
                                    )
                                }
                            }
                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                Row(
                                    modifier = Modifier.align(Alignment.BottomStart),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(palette.surface)
                                            .border(1.dp, palette.line, CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText(
                                            text = "Boceto del cliente · ${displayedClientElements.size} elementos",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(palette.surface)
                                            .border(1.dp, palette.fieldBorder, CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText("${selectedEvent.guests} invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.ink))
                                    }
                                }
                            }
                        } else {
                            val tablePositions = buildTableCanvasOffsets(activeTableCount)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val scaleX = size.width / 358f
                                val scaleY = size.height / 440f
                                scale(scaleX, scaleY, Offset.Zero) {
                                    drawArc(
                                        color = rfAdminIndigoPillBg(),
                                        startAngle = 180f,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        topLeft = Offset(86f, 22f),
                                        size = Size(186f, 92f),
                                    )
                                    drawArc(
                                        color = rfAdminIndigoPillInk(),
                                        startAngle = 180f,
                                        sweepAngle = 180f,
                                        useCenter = false,
                                        topLeft = Offset(86f, 22f),
                                        size = Size(186f, 92f),
                                        style = Stroke(width = 3f, cap = StrokeCap.Round),
                                    )

                                    drawRoundRect(
                                        color = rfAdminWarnPillBg(),
                                        topLeft = Offset(246f, 34f),
                                        size = Size(92f, 44f),
                                        cornerRadius = CornerRadius(12f, 12f),
                                    )
                                    drawRoundRect(
                                        color = Color(0xFFD9A441),
                                        topLeft = Offset(246f, 34f),
                                        size = Size(92f, 44f),
                                        cornerRadius = CornerRadius(12f, 12f),
                                        style = Stroke(width = 2f),
                                    )

                                    drawRoundRect(
                                        color = rfAdminSuccessPillBg(),
                                        topLeft = Offset(128f, 236f),
                                        size = Size(102f, 88f),
                                        cornerRadius = CornerRadius(16f, 16f),
                                    )

                                    tablePositions.forEach { tableCenter ->
                                        drawCircle(color = rfAdminIndigoPillBg(), radius = 21f, center = tableCenter)
                                        drawCircle(color = rfAdminIndigoPillInk(), radius = 21f, center = tableCenter, style = Stroke(width = 2f))
                                        for (chairIndex in 0 until 10) {
                                            val angle = chairIndex * (kotlin.math.PI * 2 / 10)
                                            val chairX = tableCenter.x + kotlin.math.cos(angle).toFloat() * 29f
                                            val chairY = tableCenter.y + kotlin.math.sin(angle).toFloat() * 29f
                                            drawCircle(color = Color(0xFFFFD45C), radius = 3.5f, center = Offset(chairX, chairY))
                                        }
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = 14.dp)
                                        .clip(CircleShape)
                                        .background(palette.surface.copy(alpha = 0.92f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    BasicText("Arco floral principal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(y = 20.dp)
                                        .clip(CircleShape)
                                        .background(rfAdminNoteAmberBg())
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    BasicText("Estación de postres", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = rfAdminWarnPillInk()))
                                }
                                BasicText(
                                    text = "Pista central",
                                    modifier = Modifier.align(Alignment.Center).offset(y = 32.dp),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminSuccessPillInk()),
                                )
                                Row(
                                    modifier = Modifier.align(Alignment.BottomStart),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(palette.surface)
                                            .border(1.dp, palette.line, CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText("Vista conceptual decorativa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(palette.surface)
                                            .border(1.dp, palette.fieldBorder, CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText("${selectedEvent.guests} invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.ink))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val palette = RfThemeState.currentPalette
                    val blueprintBg = if (palette.isDark) palette.surface else Color(0xFFF4F7FF)
                    val blueprintBorder = if (palette.isDark) palette.fieldBorder else Color(0xFFBBC6F2)
                    val blueprintGridColor = if (palette.isDark) palette.line else Color(0xFFDCE3FA)
                    val blueprintLineColor = if (palette.isDark) Color(0xFF8B98F8) else Color(0xFF3949AB)
                    val blueprintStageFill = if (palette.isDark) rfAdminIndigoPillBg() else Color(0xFFE3E8FF)
                    val blueprintTableFill = if (palette.isDark) palette.chipBg else Color.White
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(blueprintBg)
                            .border(1.5.dp, blueprintBorder, RoundedCornerShape(20.dp)),
                    ) {
                        val tablePositions = buildTableCanvasOffsets(activeTableCount)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val scaleX = size.width / 358f
                            val scaleY = size.height / 440f
                            scale(scaleX, scaleY, Offset.Zero) {
                                for (gridX in 1..8) {
                                    val xCoord = gridX * 40f
                                    drawLine(
                                        color = blueprintGridColor,
                                        start = Offset(xCoord, 20f),
                                        end = Offset(xCoord, 420f),
                                        strokeWidth = 1f,
                                    )
                                }
                                for (gridY in 1..10) {
                                    val yCoord = gridY * 40f
                                    drawLine(
                                        color = blueprintGridColor,
                                        start = Offset(18f, yCoord),
                                        end = Offset(340f, yCoord),
                                        strokeWidth = 1f,
                                    )
                                }

                                drawRect(
                                    color = blueprintLineColor,
                                    topLeft = Offset(18f, 24f),
                                    size = Size(322f, 392f),
                                    style = Stroke(width = 2f),
                                )

                                drawLine(
                                    color = rfAdminTealPillInk(),
                                    start = Offset(118f, 92f),
                                    end = Offset(118f, 404f),
                                    strokeWidth = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                                )
                                drawLine(
                                    color = rfAdminTealPillInk(),
                                    start = Offset(240f, 92f),
                                    end = Offset(240f, 404f),
                                    strokeWidth = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                                )

                                drawRoundRect(
                                    color = blueprintStageFill,
                                    topLeft = Offset(76f, 34f),
                                    size = Size(206f, 44f),
                                    cornerRadius = CornerRadius(6f, 6f),
                                )
                                drawRoundRect(
                                    color = blueprintLineColor,
                                    topLeft = Offset(76f, 34f),
                                    size = Size(206f, 44f),
                                    cornerRadius = CornerRadius(6f, 6f),
                                    style = Stroke(width = 1.8f),
                                )

                                tablePositions.forEach { tableCenter ->
                                    drawCircle(color = blueprintTableFill, radius = 19f, center = tableCenter)
                                    drawCircle(color = blueprintLineColor, radius = 19f, center = tableCenter, style = Stroke(width = 1.8f))
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RfThemeState.currentPalette.ink)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                BasicText("12.0 m × 8.0 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFD45C))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                BasicText("110V · 2 tomas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rfAdminTealPillBg())
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                BasicText("Pasillos 1.8 m libres", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rfAdminTealPillInk()))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rfAdminDangerPillBg())
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                BasicText("Salida de emergencia", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rfAdminDangerPillInk()))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("LEYENDA TÉCNICA DEL PLANO", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = RfThemeState.currentPalette.muted))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BasicText("Salón: 12.0 m × 8.0 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText("Pasillo servicio: 1.8 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminTealPillInk()))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BasicText("Energía: circuitos 110V", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminWarnPillInk()))
                                BasicText("Ruta evacuación: despejada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminDangerPillInk()))
                            }
                        }
                    }
                }

                val feedbackText = localSaveNotice ?: formFeedback
                if (!feedbackText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rfAdminSuccessPillBg())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = feedbackText,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk()),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryActionBrush)
                            .clickable { saveCurrentBlueprint() },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Guardar plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Box(
                        modifier = Modifier
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(rfAdminIndigoPillBg())
                            .border(1.dp, Color(0xFFD5D4F7), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel?.syncDraftQuantitiesToSketch(selectedEvent.id)
                                localSaveNotice = "Artículos sincronizados con el plano actual."
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Sincronizar artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                    }

                    Box(
                        modifier = Modifier
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFD9DAF0), RoundedCornerShape(16.dp))
                            .clickable {
                                saveCurrentBlueprint()
                                viewModel?.selectChatThreadForContact(selectedEvent.clientName, selectedEvent.id)
                                onSelectScreenCode("A15")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.SEND, tint = rfAdminIndigoPillInk(), size = 18.dp)
                            BasicText("Enviar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminCloseoutA19(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    if (viewModel != null) {
        if (eventsList.isEmpty()) {
            RenderAdminEmptyFeatureState(
                headerTitle = "Cierre e inspección",
                emptyTitle = "Aún no tienes eventos",
                emptyMessage = "Crea y completa un evento para registrar devolución de mobiliario e inspección final.",
                backTargetCode = "BACK:A11",
                iconKind = RfIconKind.CALENDAR,
                onSelectScreenCode = onSelectScreenCode,
                ctaLabel = "Crear evento",
                ctaTargetCode = "A04",
            )
            return
        }
    }
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val selectedEvent = resolveAdminEventOrNull(
        selectedEventId = selectedEventId,
        activeEvent = viewModel?.activeClientEvent(),
        eventsList = eventsList,
        isPreviewMode = viewModel == null,
    )
    if (selectedEvent == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Detalle del evento",
            emptyTitle = "Aún no tienes eventos",
            emptyMessage = "Crea tu primer evento o aprueba una solicitud para gestionar cliente, pagos, chat, boceto y montaje.",
            backTargetCode = "BACK:A11",
            iconKind = RfIconKind.CALENDAR,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Crear evento",
            ctaTargetCode = "A04",
        )
        return
    }

    val inspectionItems = if (viewModel == null) {
        selectedEvent.items.ifEmpty { CanonicalSampleData.events.first().items }
    } else {
        selectedEvent.items
    }

    var damagedUnitsByCode by remember(selectedEvent.id) {
        mutableStateOf(mapOf<String, Int>())
    }
    val damageNotesValidator: (String) -> String? = { rawNotes ->
        InputValidator.validateNotes(
            rawValue = rawNotes,
            fieldLabel = "Las notas de inspección",
            isRequired = false,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }
    val damageNotesField = rememberDebouncedFieldController(
        initialValue = "",
        validator = damageNotesValidator,
    )
    LaunchedEffect(selectedEvent.id) {
        damageNotesField.reset("")
    }
    var attachedEvidenceName by remember(selectedEvent.id) { mutableStateOf<String?>(null) }
    var attachedEvidenceUrl by remember(selectedEvent.id) { mutableStateOf("") }
    val allDamageIncidents = viewModel?.damageIncidents?.collectAsState()?.value ?: emptyList()
    val eventOpenIncidents = allDamageIncidents.filter { incident ->
        bothConditions(incident.eventId == selectedEvent.id, incident.resolutionNotes.isBlank())
    }

    val totalDamagedUnits = inspectionItems.sumOf { lineItem ->
        (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
    }
    val totalDamageChargeDop = inspectionItems.sumOf { lineItem ->
        val damagedCount = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
        val unitReplacementDop = lineItem.unitPriceDop.coerceAtLeast(25)
        damagedCount * unitReplacementDop
    }

    val hasDamagedItems = totalDamagedUnits > 0
    val damageSummaryBadgeText = if (hasDamagedItems) {
        "$totalDamagedUnits dañadas · +${formatAdminDop(totalDamageChargeDop)}"
    } else {
        "0 dañadas · RD$ 0"
    }
    val primaryCtaLabel = if (hasDamagedItems) {
        "Cobrar y cerrar evento"
    } else {
        "Cerrar evento sin cargos"
    }

    val remainingContractDop = (selectedEvent.totalDop - selectedEvent.paidDepositDop).coerceAtLeast(0)
    val finalBalanceDop = remainingContractDop + totalDamageChargeDop
    val closeoutButtonBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A13") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Cierre del evento",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                        )
                        BasicText(
                            text = "${selectedEvent.title} · ${formatReadableSpanishDateText(selectedEvent.dateLabel)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "DEVOLUCIÓN DE ARTÍCULOS",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (hasDamagedItems) rfAdminDangerPillBg() else rfAdminSuccessPillBg())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        BasicText(
                            text = damageSummaryBadgeText,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasDamagedItems) rfAdminDangerPillInk() else rfAdminSuccessPillInk(),
                            ),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        inspectionItems.forEachIndexed { itemIndex, lineItem ->
                            if (itemIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                            }
                            val damagedQty = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
                            val goodQty = (lineItem.quantity - damagedQty).coerceAtLeast(0)
                            val itemHasDamage = damagedQty > 0
                            val unitFeeDop = lineItem.unitPriceDop.coerceAtLeast(25)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (itemHasDamage) rfAdminNoteAmberBg() else RfThemeState.currentPalette.surface)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    RfPhoto(
                                        photoKey = photoKeyForAsset(lineItem.photo),
                                        contentDescription = lineItem.name,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = lineItem.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                        )
                                        BasicText(
                                            text = "$goodQty/${lineItem.quantity} en buen estado · Reposición ${formatAdminDop(unitFeeDop)} c/u",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (itemHasDamage) rfAdminDangerPillBg() else rfAdminSuccessPillBg())
                                            .clickable {
                                                val updatedMap = damagedUnitsByCode.toMutableMap()
                                                updatedMap[lineItem.articleCode] = if (itemHasDamage) 0 else 1.coerceAtMost(lineItem.quantity)
                                                damagedUnitsByCode = updatedMap
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        BasicText(
                                            text = if (itemHasDamage) "Con daño" else "Buen estado",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (itemHasDamage) rfAdminDangerPillInk() else rfAdminSuccessPillInk(),
                                            ),
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    BasicText(
                                        text = if (itemHasDamage) "$damagedQty dañada(s) · +${formatAdminDop(damagedQty * unitFeeDop)}" else "Sin unidades dañadas",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (itemHasDamage) rfAdminDangerPillInk() else RfThemeState.currentPalette.muted,
                                        ),
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(RfThemeState.currentPalette.chipBg)
                                                .clickable {
                                                    val updatedMap = damagedUnitsByCode.toMutableMap()
                                                    updatedMap[lineItem.articleCode] = (damagedQty - 1).coerceAtLeast(0)
                                                    damagedUnitsByCode = updatedMap
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("−", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                        }
                                        BasicText(
                                            text = "$damagedQty",
                                            modifier = Modifier.widthIn(min = 22.dp),
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.Center),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(RfThemeState.currentPalette.chipBg)
                                                .clickable {
                                                    val updatedMap = damagedUnitsByCode.toMutableMap()
                                                    updatedMap[lineItem.articleCode] = (damagedQty + 1).coerceAtMost(lineItem.quantity)
                                                    damagedUnitsByCode = updatedMap
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("+", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (hasDamagedItems) {
                    BasicText(
                        text = "EVIDENCIA Y OBSERVACIONES",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (viewModel != null) {
                                            viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                                                attachedEvidenceName = pickedFile.fileName.ifBlank { "evidencia-cierre.jpg" }
                                                viewModel.uploadAndAttachMedia("damage", pickedFile) { uploadedUrl ->
                                                    attachedEvidenceUrl = uploadedUrl
                                                }
                                            }
                                        } else {
                                            attachedEvidenceName = "evidencia-cierre.jpg"
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val hasEvidenceUrl = attachedEvidenceUrl.isNotBlank()
                                val hasPhoto = eitherCondition(!attachedEvidenceName.isNullOrBlank(), hasEvidenceUrl)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, if (hasPhoto) Color(0xFF8DB52C) else Color(0xFFC9CAE6), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (hasEvidenceUrl) {
                                        RfUploadedOrCatalogPhoto(
                                            photoUrl = attachedEvidenceUrl,
                                            fallbackPhotoKey = RfPhotoKey.ARCO_ROSA,
                                            contentDescription = "Evidencia de daño",
                                            modifier = Modifier.fillMaxSize(),
                                            viewModel = viewModel,
                                        )
                                    } else if (hasPhoto) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 20.dp)
                                    } else {
                                        RfIcon(kind = RfIconKind.CAMERA, tint = rfAdminIndigoPillInk(), size = 20.dp)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = if (hasPhoto) "Evidencia: $attachedEvidenceName" else "Adjuntar foto del daño",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "Queda registrada en el acta de inspección",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }
                            }

                            AdminDebouncedInputField(
                                controller = damageNotesField,
                                validator = damageNotesValidator,
                                placeholder = "Notas de inspección (ej. base fisurada)",
                                containerColor = RfThemeState.currentPalette.chipBg,
                                cornerRadius = 12.dp,
                                minHeight = 46.dp,
                                horizontalPadding = 12.dp,
                                verticalPadding = 8.dp,
                                textFontSize = 13.sp,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done,
                                ),
                            )
                        }
                    }
                }

                if (eventOpenIncidents.isNotEmpty()) {
                    BasicText(
                        text = "INCIDENCIAS EN CUARENTENA",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            eventOpenIncidents.forEach { incident ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    if (incident.photoUrl.isNotBlank()) {
                                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))) {
                                            RfUploadedOrCatalogPhoto(
                                                photoUrl = incident.photoUrl,
                                                fallbackPhotoKey = RfPhotoKey.ARCO_ROSA,
                                                contentDescription = incident.articleName,
                                                modifier = Modifier.fillMaxSize(),
                                                viewModel = viewModel,
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = "${incident.articleName} (${incident.damagedUnits} dañadas)",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                                        )
                                        BasicText(
                                            text = "${formatAdminDop(incident.replacementDop)} · ${incident.notes}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = RfThemeState.currentPalette.muted),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .clip(CircleShape)
                                            .background(rfAdminIndigoPillBg())
                                            .clickable { viewModel?.resolveDamageIncidentById(incident.id) }
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("Liberar cuarentena", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                    }
                                }
                            }
                        }
                    }
                }

                BasicText(
                    text = "RESUMEN DE LIQUIDACIÓN",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Total del evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(formatAdminDop(selectedEvent.totalDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(formatAdminDop(selectedEvent.paidDepositDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminSuccessPillInk()))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Cargo por daño", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(formatAdminDop(totalDamageChargeDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (hasDamagedItems) rfAdminDangerPillInk() else RfThemeState.currentPalette.ink))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            BasicText("Saldo a cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText(formatAdminDop(finalBalanceDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                        }
                        if (finalBalanceDop > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rfAdminIndigoPillBg())
                                    .clickable {
                                        viewModel?.selectEvent(selectedEvent.id)
                                        onSelectScreenCode("A21")
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Registrar cobro de saldo (${formatAdminDop(finalBalanceDop)})",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()),
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(closeoutButtonBrush)
                        .clickable {
                            val notesError = damageNotesField.currentImmediateError()
                            if (notesError != null) {
                                damageNotesField.markSubmitted()
                            } else {
                                if (viewModel != null) {
                                    if (hasDamagedItems) {
                                        inspectionItems.forEach { lineItem ->
                                            val damagedQty = (damagedUnitsByCode[lineItem.articleCode] ?: 0).coerceIn(0, lineItem.quantity)
                                            if (damagedQty > 0) {
                                                val unitFeeDop = lineItem.unitPriceDop.coerceAtLeast(25)
                                                val resolvedNotes = damageNotesField.value.trim().ifBlank {
                                                    "Inspección de cierre: $damagedQty unid. dañadas"
                                                }
                                                viewModel.reportEventDamageWithPhoto(
                                                    eventId = selectedEvent.id,
                                                    articleCode = lineItem.articleCode,
                                                    articleName = lineItem.name,
                                                    damagedUnits = damagedQty,
                                                    missingUnits = 0,
                                                    replacementDop = damagedQty * unitFeeDop,
                                                    notes = resolvedNotes,
                                                    photoUrl = attachedEvidenceUrl,
                                                )
                                            }
                                        }
                                    }
                                    viewModel.completeCloseoutWithServer(
                                        eventId = selectedEvent.id,
                                        notes = damageNotesField.value.trim(),
                                    )
                                }
                                onSelectScreenCode("A13")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                        BasicText(primaryCtaLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

// ==============================================================================
// A10 · NUEVO ARTÍCULO ADMIN
// ==============================================================================
private fun findNextNonCollidingSku(existingArticles: List<CatalogArticle>): String {
    for (candidateNumber in 7..999) {
        val padded = candidateNumber.toString().padStart(3, '0')
        val candidateCode = "RF-$padded"
        val collides = existingArticles.any { articleItem ->
            articleItem.code.equals(candidateCode, ignoreCase = true)
        }
        if (!collides) {
            return candidateCode
        }
    }
    return "RF-999"
}

private fun shiftYearMonthIso(currentMonthIso: String, deltaMonths: Int): String {
    val parts = currentMonthIso.split("-")
    val (fallbackYear, fallbackMonth, _) = DomainClockFormatter.currentCivilDateTriple()
    val baseYear = parts.getOrNull(0)?.toIntOrNull() ?: fallbackYear
    val baseMonth = parts.getOrNull(1)?.toIntOrNull() ?: fallbackMonth
    val zeroBasedTotal = (baseYear * 12) + (baseMonth - 1) + deltaMonths
    val targetYear = zeroBasedTotal / 12
    val targetMonth = (zeroBasedTotal % 12) + 1
    val paddedMonth = targetMonth.toString().padStart(2, '0')
    return "$targetYear-$paddedMonth"
}

private fun formatYearMonthSpanish(monthIso: String): String {
    val parts = monthIso.split("-")
    val (fallbackYear, fallbackMonth, _) = DomainClockFormatter.currentCivilDateTriple()
    val yearPart = parts.getOrNull(0) ?: fallbackYear.toString()
    val monthNumber = parts.getOrNull(1)?.toIntOrNull() ?: fallbackMonth
    val monthName = when (monthNumber) {
        1 -> "enero"
        2 -> "febrero"
        3 -> "marzo"
        4 -> "abril"
        5 -> "mayo"
        6 -> "junio"
        7 -> "julio"
        8 -> "agosto"
        9 -> "septiembre"
        10 -> "octubre"
        11 -> "noviembre"
        else -> "diciembre"
    }
    return "$monthName $yearPart"
}

@Composable
internal fun RenderAdminNewArticleA10(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
    val editingArticleId = viewModel?.editingArticleId?.collectAsState()?.value
    val articleCode = viewModel?.newArticleCode?.collectAsState()?.value.orEmpty()
    val articleName = viewModel?.newArticleName?.collectAsState()?.value.orEmpty()
    val articleCat = viewModel?.newArticleCategory?.collectAsState()?.value.orEmpty()
    val articlePrice = viewModel?.newArticlePrice?.collectAsState()?.value.orEmpty()
    val articleStock = viewModel?.newArticleStock?.collectAsState()?.value.orEmpty()
    val vmArticlePhotoUrl = viewModel?.newArticlePhotoUrl?.collectAsState()?.value.orEmpty()
    val formError = viewModel?.formError?.collectAsState()?.value

    var articleDescription by remember { mutableStateOf("") }
    var articleDimensions by remember { mutableStateOf("") }
    var articleMaterial by remember { mutableStateOf("") }
    var visibleInCatalog by remember { mutableStateOf(true) }
    var pickedPhotoName by remember { mutableStateOf<String?>(null) }
    var clearedPreviewPhoto by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var hasCodeInteracted by remember { mutableStateOf(false) }
    var hasNameInteracted by remember { mutableStateOf(false) }
    var hasPriceInteracted by remember { mutableStateOf(false) }
    var hasStockInteracted by remember { mutableStateOf(false) }
    var hasDimensionsInteracted by remember { mutableStateOf(false) }
    var hasMaterialInteracted by remember { mutableStateOf(false) }
    var hasDescriptionInteracted by remember { mutableStateOf(false) }
    var forceArticleValidation by remember { mutableStateOf(false) }

    val codeValidator: (String) -> String? = { rawCode ->
        validateArticleSku(rawCode, allArticles, editingArticleId)
    }
    val nameValidator: (String) -> String? = { rawName ->
        InputValidator.validateArticleName(rawName)
    }
    val priceValidator: (String) -> String? = { rawPrice ->
        InputValidator.validateMoneyAmount(
            rawValue = rawPrice,
            fieldLabel = "El precio por día",
            minAmount = 1.0,
            maxAmount = 1_000_000.0,
        )
    }
    val stockValidator: (String) -> String? = { rawStock ->
        InputValidator.validateStockCount(rawStock)
    }
    val dimensionsValidator: (String) -> String? = { rawDimensions ->
        InputValidator.validateNotes(
            rawValue = rawDimensions,
            fieldLabel = "Las dimensiones",
            isRequired = false,
            maxLength = 60,
        )
    }
    val materialValidator: (String) -> String? = { rawMaterial ->
        InputValidator.validateNotes(
            rawValue = rawMaterial,
            fieldLabel = "El material",
            isRequired = false,
            maxLength = 60,
        )
    }
    val descriptionValidator: (String) -> String? = { rawDescription ->
        InputValidator.validateDescription(
            rawValue = rawDescription,
            fieldLabel = "La descripción",
            isRequired = false,
            minLength = 0,
            maxLength = InputValidator.MAX_DESCRIPTION_LENGTH,
        )
    }

    val availableCategories = remember(vmCategories, allArticles, articleCat) {
        val baseNames = if (vmCategories != null) {
            vmCategories.map { category -> category.name.trim() }.filter { name -> name.isNotEmpty() }
        } else {
            allArticles.map { articleItem -> articleItem.category.trim() }.filter { name -> name.isNotEmpty() }
        }
        val extraCurrent = articleCat.trim().takeIf { name -> name.isNotEmpty() }
        val combined = if (extraCurrent != null) baseNames + extraCurrent else baseNames
        combined.distinctBy { name -> name.lowercase() }
    }

    LaunchedEffect(availableCategories, editingArticleId) {
        if (editingArticleId == null) {
            if (articleCat.isBlank()) {
                val firstCat = availableCategories.firstOrNull()
                if (firstCat != null) {
                    viewModel?.setNewArticleCategory(firstCat)
                }
            }
        }
    }

    LaunchedEffect(editingArticleId) {
        viewModel?.clearFormError()
        localValidationError = null
        clearedPreviewPhoto = false
        hasCodeInteracted = false
        hasNameInteracted = false
        hasPriceInteracted = false
        hasStockInteracted = false
        hasDimensionsInteracted = false
        hasMaterialInteracted = false
        hasDescriptionInteracted = false
        forceArticleValidation = false
        if (editingArticleId != null) {
            val matchedArticle = allArticles.firstOrNull { articleItem -> articleItem.id == editingArticleId }
            if (matchedArticle != null) {
                articleDescription = matchedArticle.description
                articleDimensions = matchedArticle.dimensions
                articleMaterial = matchedArticle.material
                visibleInCatalog = matchedArticle.visibleInCatalog
            }
        } else {
            articleDescription = ""
            articleDimensions = ""
            articleMaterial = ""
            visibleInCatalog = true
            pickedPhotoName = null
            val cleanCode = articleCode.trim()
            val codeCollides = allArticles.any { articleItem ->
                articleItem.code.equals(cleanCode, ignoreCase = true)
            }
            val needsFreshCode = eitherCondition(cleanCode.isEmpty(), codeCollides)
            if (needsFreshCode) {
                viewModel?.startEditingArticle(null)
                val refreshedCode = viewModel?.newArticleCode?.value?.trim().orEmpty()
                val refreshedStillCollides = allArticles.any { articleItem ->
                    articleItem.code.equals(refreshedCode, ignoreCase = true)
                }
                val mustAssignFallbackSku = eitherCondition(refreshedCode.isEmpty(), refreshedStillCollides)
                if (mustAssignFallbackSku) {
                    val uniqueSku = findNextNonCollidingSku(allArticles)
                    viewModel?.setNewArticleCode(uniqueSku)
                }
            }
        }
    }

    val isEditMode = editingArticleId != null
    val headerTitle = if (isEditMode) "Editar artículo" else "Nuevo artículo"
    val editingArticleObj = allArticles.firstOrNull { articleItem -> articleItem.id == editingArticleId }
    val previewPhotoKey = editingArticleObj?.let { articleObj -> photoKeyForAsset(articleObj.photo) }
    val hasExplicitPhotoUrl = vmArticlePhotoUrl.isNotBlank()
    val hasExistingAssetPhoto = if (clearedPreviewPhoto) false else previewPhotoKey != null
    val hasPreviewImage = eitherCondition(hasExplicitPhotoUrl, hasExistingAssetPhoto)
    val hasPickedPhoto = !pickedPhotoName.isNullOrBlank()
    val hasAnyCoverPhoto = eitherCondition(hasPreviewImage, hasPickedPhoto)
    val primarySaveBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    var pendingDeleteArticle by remember { mutableStateOf<CatalogArticle?>(null) }
    var deleteLocalError by remember { mutableStateOf<String?>(null) }
    val pickArticleCoverPhoto: () -> Unit = {
        clearedPreviewPhoto = false
        if (viewModel != null) {
            viewModel.pickMediaAttachment(listOf("jpg", "jpeg", "png")) { pickedFile ->
                pickedPhotoName = pickedFile.fileName.ifBlank { "articulo-foto.jpg" }
                viewModel.uploadAndAttachMedia("catalog", pickedFile) { uploadedUrl ->
                    viewModel.setNewArticlePhotoUrl(uploadedUrl)
                }
            }
        } else {
            pickedPhotoName = "articulo-foto.jpg"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A09") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = headerTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                        )
                        BasicText(
                            text = "SKU $articleCode · Catálogo e inventario",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                    if (editingArticleObj != null) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(rfAdminDangerPillBg())
                                .border(1.dp, Color(0xFFF5C2CB), CircleShape)
                                .clickable {
                                    deleteLocalError = null
                                    viewModel?.clearFormError()
                                    pendingDeleteArticle = editingArticleObj
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.TRASH, tint = rfAdminDangerPillInk(), size = 20.dp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .border(
                                width = if (hasPreviewImage) 1.dp else 2.dp,
                                color = if (hasPreviewImage) Color(0xFF8DB52C) else Color(0xFFC9CAE6),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable { pickArticleCoverPhoto() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (hasPreviewImage) {
                            RfUploadedOrCatalogPhoto(
                                photoUrl = vmArticlePhotoUrl,
                                fallbackPhotoKey = previewPhotoKey ?: RfPhotoKey.MESA_PLEGABLE,
                                contentDescription = articleName,
                                modifier = Modifier.fillMaxSize(),
                                viewModel = viewModel,
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xE6FFFFFF))
                                    .border(1.dp, Color(0xFFDCE0EE), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CAMERA, tint = rfAdminIndigoPillInk(), size = 14.dp)
                            }
                        } else if (hasPickedPhoto) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(8.dp),
                            ) {
                                RfIcon(kind = RfIconKind.CLOCK, tint = rfAdminIndigoPillInk(), size = 20.dp)
                                BasicText(
                                    text = "Subiendo...",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = rfAdminIndigoPillInk()),
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(8.dp),
                            ) {
                                RfIcon(kind = RfIconKind.CAMERA, tint = rfAdminIndigoPillInk(), size = 22.dp)
                                BasicText(
                                    text = "Agregar foto",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk(), textAlign = TextAlign.Center),
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = if (hasPreviewImage) "Foto de portada" else "Sin foto seleccionada",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = pickedPhotoName ?: if (hasPreviewImage) "Imagen lista para guardar" else "Toca el recuadro para subir una foto (JPG o PNG).",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RfThemeState.currentPalette.surface)
                                    .border(1.dp, Color(0xFFD6D8EC), RoundedCornerShape(10.dp))
                                    .clickable { pickArticleCoverPhoto() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                BasicText(
                                    text = if (hasAnyCoverPhoto) "Cambiar" else "Subir foto",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                                )
                            }
                            if (hasAnyCoverPhoto) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(rfAdminDangerPillBg())
                                        .border(1.dp, Color(0xFFF3C6CF), RoundedCornerShape(10.dp))
                                        .clickable {
                                            pickedPhotoName = null
                                            clearedPreviewPhoto = true
                                            viewModel?.setNewArticlePhotoUrl("")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    BasicText(
                                        text = "Quitar",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = rfAdminDangerPillInk()),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AdminDebouncedInputField(
                        value = articleCode,
                        onValueChange = { updatedCode -> viewModel?.setNewArticleCode(updatedCode) },
                        validator = codeValidator,
                        hasInteracted = hasCodeInteracted,
                        onInteracted = { hasCodeInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Código SKU",
                        placeholder = "RF-007",
                        inputFilter = ::filterArticleSkuInput,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next,
                        ),
                        useInputShadow = true,
                        minHeight = 50.dp,
                        horizontalPadding = 12.dp,
                        textFontSize = 14.sp,
                        textFontWeight = FontWeight.SemiBold,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.width(116.dp),
                    )

                    AdminDebouncedInputField(
                        value = articleName,
                        onValueChange = { updatedName -> viewModel?.setNewArticleName(updatedName) },
                        validator = nameValidator,
                        hasInteracted = hasNameInteracted,
                        onInteracted = { hasNameInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Nombre",
                        placeholder = "Ej. Mesa plegable 1.8 m",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        useInputShadow = true,
                        minHeight = 50.dp,
                        horizontalPadding = 14.dp,
                        textFontSize = 15.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                        BasicText(
                            text = "Gestionar categorías →",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectScreenCode("A40") }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                        )
                    }
                    if (availableCategories.isEmpty()) {
                        RfEmptyStateCard(
                            icon = RfIconKind.CATALOG,
                            title = "No hay categorías creadas todavía",
                            subtitle = "Crea tu primera categoría para clasificar el inventario.",
                            actionLabel = "Crear categoría",
                            isAdmin = true,
                            compact = true,
                            onAction = { onSelectScreenCode("A40") },
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            availableCategories.forEach { categoryLabel ->
                                val isSelected = categoryLabel.equals(articleCat, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .shadow(if (isSelected) 6.dp else 4.dp, CircleShape, spotColor = if (isSelected) Color(0xC05B6CF0) else Color(0x604650B4), ambientColor = if (isSelected) Color(0x405B6CF0) else Color(0x204650B4))
                                        .clip(CircleShape)
                                        .background(if (isSelected) primarySaveBrush else SolidColor(RfThemeState.currentPalette.surface))
                                        .then(if (!isSelected) Modifier.border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape) else Modifier)
                                        .clickable { viewModel?.setNewArticleCategory(categoryLabel) }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = categoryLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else RfThemeState.currentPalette.ink,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AdminDebouncedInputField(
                        value = articlePrice,
                        onValueChange = { updatedPrice -> viewModel?.setNewArticlePrice(updatedPrice) },
                        validator = priceValidator,
                        hasInteracted = hasPriceInteracted,
                        onInteracted = { hasPriceInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Precio por día",
                        placeholder = "Ej. 250",
                        prefixText = "RD$",
                        inputFilter = { rawPrice -> InputValidator.filterDigitsOnly(rawPrice, maxDigits = 7) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        useInputShadow = true,
                        minHeight = 50.dp,
                        horizontalPadding = 14.dp,
                        textFontSize = 15.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )

                    AdminDebouncedInputField(
                        value = articleStock,
                        onValueChange = { updatedStock -> viewModel?.setNewArticleStock(updatedStock) },
                        validator = stockValidator,
                        hasInteracted = hasStockInteracted,
                        onInteracted = { hasStockInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Cantidad",
                        placeholder = "Ej. 30",
                        suffixText = "unid.",
                        inputFilter = { rawStock -> InputValidator.filterDigitsOnly(rawStock, maxDigits = 6) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        useInputShadow = true,
                        minHeight = 50.dp,
                        horizontalPadding = 14.dp,
                        textFontSize = 15.sp,
                        textFontWeight = FontWeight.SemiBold,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AdminDebouncedInputField(
                        value = articleDimensions,
                        onValueChange = { updatedDimensions -> articleDimensions = updatedDimensions },
                        validator = dimensionsValidator,
                        hasInteracted = hasDimensionsInteracted,
                        onInteracted = { hasDimensionsInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Dimensiones",
                        placeholder = "Ej. 1.80 m × 0.75 m",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        minHeight = 46.dp,
                        horizontalPadding = 12.dp,
                        textFontSize = 14.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )

                    AdminDebouncedInputField(
                        value = articleMaterial,
                        onValueChange = { updatedMaterial -> articleMaterial = updatedMaterial },
                        validator = materialValidator,
                        hasInteracted = hasMaterialInteracted,
                        onInteracted = { hasMaterialInteracted = true },
                        forceShowError = forceArticleValidation,
                        label = "Material",
                        placeholder = "Ej. Resina y metal",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        minHeight = 46.dp,
                        horizontalPadding = 12.dp,
                        textFontSize = 14.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 13.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )
                }

                AdminDebouncedInputField(
                    value = articleDescription,
                    onValueChange = { updatedDescription -> articleDescription = updatedDescription },
                    validator = descriptionValidator,
                    hasInteracted = hasDescriptionInteracted,
                    onInteracted = { hasDescriptionInteracted = true },
                    forceShowError = forceArticleValidation,
                    label = "Descripción",
                    placeholder = "Detalles de uso, acabados o recomendaciones...",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = false,
                    useInputShadow = true,
                    minHeight = 70.dp,
                    horizontalPadding = 14.dp,
                    verticalPadding = 12.dp,
                    textFontSize = 15.sp,
                    textColor = RfThemeState.currentPalette.ink,
                    labelFontSize = 13.sp,
                    labelColor = RfThemeState.currentPalette.ink,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.fieldBorder, RoundedCornerShape(16.dp))
                        .clickable { visibleInCatalog = !visibleInCatalog }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                            BasicText("Los clientes lo verán y podrán pedirlo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                        }

                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(30.dp)
                                .clip(CircleShape)
                                .background(if (visibleInCatalog) primarySaveBrush else SolidColor(RfThemeState.currentPalette.fieldBorder))
                                .padding(3.dp),
                            contentAlignment = if (visibleInCatalog) Alignment.CenterEnd else Alignment.CenterStart,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                            )
                        }
                    }
                }

                AdminFormErrorBanner(errorMessage = localValidationError ?: formError)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(0.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primarySaveBrush)
                        .clickable {
                            forceArticleValidation = true
                            val categoryError = if (articleCat.isBlank()) {
                                "Crea y selecciona una categoría antes de guardar."
                            } else {
                                null
                            }
                            val firstValidationError = listOfNotNull(
                                categoryError,
                                codeValidator(articleCode),
                                nameValidator(articleName),
                                priceValidator(articlePrice),
                                stockValidator(articleStock),
                                dimensionsValidator(articleDimensions),
                                materialValidator(articleMaterial),
                                descriptionValidator(articleDescription),
                            ).firstOrNull()
                            if (firstValidationError != null) {
                                localValidationError = firstValidationError
                            } else {
                                localValidationError = null
                                val saved = viewModel?.submitNewArticle(
                                    description = articleDescription.trim(),
                                    dimensions = articleDimensions.trim(),
                                    material = articleMaterial.trim(),
                                    visibleInCatalog = visibleInCatalog,
                                ) ?: true
                                if (saved) {
                                    onSelectScreenCode("A09")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        AdminDeleteArticleConfirmSheet(
            targetArticle = pendingDeleteArticle,
            errorMessage = deleteLocalError,
            isSubmitting = isSubmitting,
            onDismiss = {
                pendingDeleteArticle = null
                deleteLocalError = null
            },
            onConfirmDelete = { target ->
                viewModel?.deleteArticle(target.id) { outcome ->
                    if (outcome.isSuccess) {
                        pendingDeleteArticle = null
                        deleteLocalError = null
                        onSelectScreenCode("A09")
                    } else {
                        deleteLocalError = outcome.exceptionOrNull()?.message ?: "No se pudo eliminar el artículo."
                    }
                }
            },
        )
    }
}

@Composable
internal fun RenderAdminArticleDetailA23(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val articleId = viewModel?.selectedArticleId?.collectAsState()?.value ?: "art-silla-tiffany"
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val selectedArticle = allArticles.firstOrNull { articleItem -> articleItem.id == articleId }
        ?: allArticles.firstOrNull()
        ?: if (viewModel == null) CanonicalSampleData.articles.first() else null
    if (selectedArticle == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RfThemeState.currentPalette.chipBg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("BACK:A09") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }
            RfEmptyStateCard(
                icon = RfIconKind.CATALOG,
                title = "Sin artículos en inventario",
                subtitle = "Agrega piezas del catálogo para controlar existencias y fechas disponibles.",
                actionLabel = "Agregar artículo",
                isAdmin = true,
                onAction = { onSelectScreenCode("A24") },
            )
        }
        return
    }

    var selectedMonthIso by remember(selectedArticle.id) {
        mutableStateOf(DomainClockFormatter.currentYearMonthIso())
    }
    var availabilityCalendar by remember(selectedArticle.id) {
        mutableStateOf<ArticleAvailabilityCalendar?>(null)
    }
    var selectedDayNumber by remember(selectedArticle.id, selectedMonthIso) {
        mutableStateOf<Int?>(null)
    }
    var pendingDeleteArticle by remember(selectedArticle.id) {
        mutableStateOf<CatalogArticle?>(null)
    }
    var deleteLocalError by remember(selectedArticle.id) {
        mutableStateOf<String?>(null)
    }
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    LaunchedEffect(selectedArticle.id, selectedMonthIso) {
        availabilityCalendar = viewModel?.queryWarehouseAvailability(selectedArticle.id, selectedMonthIso)
    }

    val photoKey = photoKeyForAsset(selectedArticle.photo)
    val calendarDays = availabilityCalendar?.days ?: emptyList()
    val peakReservedInMonth = calendarDays.maxOfOrNull { dayEntry -> dayEntry.reservedUnits }
        ?: (selectedArticle.totalStock - selectedArticle.availableOct23).coerceAtLeast(0)
    val currentAvailableUnits = (selectedArticle.totalStock - peakReservedInMonth).coerceAtLeast(0)
    val reservedDatesCount = calendarDays.count { dayEntry -> dayEntry.reservedUnits > 0 }
    val monthTitleLabel = "Reservas · ${formatYearMonthSpanish(selectedMonthIso)}"
    val visibilityStatusLabel = if (selectedArticle.visibleInCatalog) {
        "Visible en el catálogo"
    } else {
        "Oculto en el catálogo"
    }

    var showFullScreenPhoto by remember(selectedArticle.id) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable { showFullScreenPhoto = true },
        ) {
            RfUploadedOrCatalogPhoto(
                photoUrl = selectedArticle.photoUrl,
                fallbackPhotoKey = photoKey,
                contentDescription = selectedArticle.name,
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.0f to Color(0x4D151729), 0.4f to Color.Transparent)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x88151729))
                    .clickable { showFullScreenPhoto = true }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                BasicText(
                    text = "Ver foto",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                    .clickable { onSelectScreenCode("BACK:A09") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(rfAdminDangerPillBg())
                        .border(1.dp, Color(0xFFF5C2CB), CircleShape)
                        .clickable {
                            deleteLocalError = null
                            viewModel?.clearFormError()
                            pendingDeleteArticle = selectedArticle
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.TRASH, tint = rfAdminDangerPillInk(), size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable {
                            viewModel?.startEditingArticle(selectedArticle.id)
                            onSelectScreenCode("A10")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 226.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.chipBg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = selectedArticle.name,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "${selectedArticle.code} · ${selectedArticle.category} · $visibilityStatusLabel",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        BasicText(
                            text = formatAdminDop(selectedArticle.pricePerDayDop),
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText("por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                    }
                }

                BasicText(
                    text = "${selectedArticle.description} (${selectedArticle.dimensions} · ${selectedArticle.material})",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted, lineHeight = 18.sp),
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText("${selectedArticle.totalStock}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                            BasicText("En total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .clickable { viewModel?.adjustArticleStock(selectedArticle.id, -1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("−", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(rfAdminIndigoPillBg())
                                        .clickable { viewModel?.adjustArticleStock(selectedArticle.id, 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("+", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("$currentAvailableUnits", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminSuccessPillInk()))
                            BasicText("Disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("$peakReservedInMonth", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminWarnPillInk()))
                            BasicText("En reserva", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                        }
                    }
                }

                if (selectedArticle.damagedStock > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rfAdminDangerPillBg())
                            .border(1.dp, Color(0xFFF5C2CB), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        BasicText(
                            text = "${selectedArticle.damagedStock} unidades en cuarentena por daño (no disponibles para renta)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminDangerPillInk()),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(monthTitleLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                                BasicText("$reservedDatesCount día(s) con reservas activas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .clickable { selectedMonthIso = shiftYearMonthIso(selectedMonthIso, -1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("‹", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .clickable { selectedMonthIso = shiftYearMonthIso(selectedMonthIso, 1) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("›", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                                }
                            }
                        }

                        val totalDaysInGrid = calendarDays.size.takeIf { dayCount -> dayCount > 0 } ?: 30
                        val dayNumbers = (1..totalDaysInGrid).toList()
                        val rows = dayNumbers.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEach { rowDays ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rowDays.forEach { dayNumber ->
                                        val matchingDayInfo = calendarDays.getOrNull(dayNumber - 1)
                                        val reservedUnitsOnDay = matchingDayInfo?.reservedUnits ?: if (dayNumber == 23) peakReservedInMonth else 0
                                        val availableUnitsOnDay = matchingDayInfo?.availableUnits ?: (selectedArticle.totalStock - reservedUnitsOnDay).coerceAtLeast(0)
                                        val isFull = bothConditions(reservedUnitsOnDay > 0, availableUnitsOnDay <= 0)
                                        val isPartial = bothConditions(reservedUnitsOnDay > 0, availableUnitsOnDay > 0)
                                        val isSelectedDay = selectedDayNumber == dayNumber

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .then(
                                                    if (isFull) {
                                                        Modifier.background(AdminButtonGradient)
                                                    } else if (isPartial) {
                                                        Modifier.background(rfAdminIndigoPillBg())
                                                    } else if (isSelectedDay) {
                                                        Modifier.border(1.5.dp, Color(0xFF5B6CF0), RoundedCornerShape(9.dp))
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .clickable { selectedDayNumber = dayNumber },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = "$dayNumber",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = when {
                                                        isFull -> Color.White
                                                        isPartial -> rfAdminIndigoPillInk()
                                                        else -> RfThemeState.currentPalette.ink
                                                    },
                                                ),
                                            )
                                        }
                                    }
                                    if (rowDays.size < 7) {
                                        repeat(7 - rowDays.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedDayNumber != null) {
                            val chosenIndex = (selectedDayNumber ?: 1) - 1
                            val chosenDayInfo = calendarDays.getOrNull(chosenIndex)
                            val dayReserved = chosenDayInfo?.reservedUnits ?: 0
                            val dayAvailable = chosenDayInfo?.availableUnits ?: (selectedArticle.totalStock - dayReserved).coerceAtLeast(0)
                            BasicText(
                                text = "Día $selectedDayNumber: $dayAvailable disponibles · $dayReserved reservadas",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF5B6CF0)))
                                BasicText("Todo reservado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(rfAdminIndigoPillBg()))
                                BasicText("Parcial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }
                }
            }
        }

        AdminDeleteArticleConfirmSheet(
            targetArticle = pendingDeleteArticle,
            errorMessage = deleteLocalError,
            isSubmitting = isSubmitting,
            onDismiss = {
                pendingDeleteArticle = null
                deleteLocalError = null
            },
            onConfirmDelete = { target ->
                viewModel?.deleteArticle(target.id) { outcome ->
                    if (outcome.isSuccess) {
                        pendingDeleteArticle = null
                        deleteLocalError = null
                        onSelectScreenCode("BACK:A09")
                    } else {
                        deleteLocalError = outcome.exceptionOrNull()?.message ?: "No se pudo eliminar el artículo."
                    }
                }
            },
        )

        if (showFullScreenPhoto) {
            RfFullScreenPhotoViewer(
                mediaItems = listOf(selectedArticle.photoUrl.trim() to photoKey),
                currentIndex = 0,
                title = selectedArticle.name,
                viewModel = viewModel,
                onSelectIndex = {},
                onDismiss = { showFullScreenPhoto = false },
            )
        }
    }
}

private data class PackageDraftLineItem(
    val articleId: String,
    val name: String,
    val unitPriceDop: Int,
    val quantity: Int,
    val photoKey: RfPhotoKey,
)

private fun buildCanonicalBodasRosaLines(): List<PackageDraftLineItem> = listOf(
    PackageDraftLineItem("art-arco-rosa", "Arco de globos rosa", 95, 1, RfPhotoKey.ARCO_ROSA),
    PackageDraftLineItem("art-centro-floral", "Centro de mesa floral", 35, 10, RfPhotoKey.CENTRO_FLORAL),
    PackageDraftLineItem("art-silla-tiffany", "Silla Tiffany Cristal", 4, 120, RfPhotoKey.SILLA_TIFFANY),
    PackageDraftLineItem("art-mesa-redonda", "Mesa redonda", 25, 3, RfPhotoKey.MESA_REDONDA),
)

private fun buildFallbackAdminPackages(): List<EventPackage> = listOf(
    EventPackage(
        id = "pkg-bodas-rosa",
        name = "Bodas Rosa",
        subtitle = "Ceremonia y recepción romántica",
        suggestedGuests = 120,
        priceDop = 850,
        savingsDop = 150,
        photo = PhotoAsset.BODA,
        includedItems = listOf("1x Arco de globos rosa", "10x Centro de mesa floral", "120x Silla Tiffany Cristal", "3x Mesa redonda"),
        eventType = "Boda",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-quince-premium",
        name = "Quinceañera Premium",
        subtitle = "Salón de gala en cristal y flores",
        suggestedGuests = 100,
        priceDop = 1200,
        savingsDop = 165,
        photo = PhotoAsset.QUINCEANERA,
        includedItems = listOf("1x Arco de globos rosa", "12x Centro de mesa floral", "100x Silla Tiffany Cristal", "10x Mesa redonda"),
        eventType = "Quinceañera",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-cumple-infantil",
        name = "Cumpleaños Infantil",
        subtitle = "Estación dulce y montaje colorido",
        suggestedGuests = 50,
        priceDop = 450,
        savingsDop = 50,
        photo = PhotoAsset.CUMPLE_INFANTIL,
        includedItems = listOf("1x Mesa de helados", "1x Arco de globos rosa", "40x Silla Tiffany Cristal"),
        eventType = "Cumpleaños",
        visibleInCatalog = true,
    ),
    EventPackage(
        id = "pkg-corporativo",
        name = "Evento Corporativo",
        subtitle = "Mobiliario ejecutivo y sonido base",
        suggestedGuests = 80,
        priceDop = 600,
        savingsDop = 55,
        photo = PhotoAsset.CORPORATIVO,
        includedItems = listOf("80x Silla Tiffany Cristal", "8x Mesa plegable 1.8 m", "8x Centro de mesa floral"),
        eventType = "Corporativo",
        visibleInCatalog = true,
    ),
)

private fun parseIncludedItemsIntoDraftLines(
    includedItems: List<String>,
    catalogArticles: List<CatalogArticle>,
): List<PackageDraftLineItem> {
    if (includedItems.isEmpty()) {
        return buildCanonicalBodasRosaLines()
    }
    return includedItems.mapIndexed { itemIndex, rawEntry ->
        val trimmed = rawEntry.trim()
        val leadingDigits = trimmed.takeWhile { charItem -> charItem.isDigit() }
        val parsedQty = leadingDigits.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val remainder = trimmed.drop(leadingDigits.length).trimStart('x', 'X', ' ', '·', '-')
        val labelName = remainder.ifBlank { trimmed }
        val matchedCatalog = catalogArticles.firstOrNull { articleItem ->
            val nameContainsLabel = articleItem.name.contains(labelName, ignoreCase = true)
            val labelContainsName = labelName.contains(articleItem.name, ignoreCase = true)
            eitherCondition(nameContainsLabel, labelContainsName)
        }
        if (matchedCatalog != null) {
            PackageDraftLineItem(
                articleId = matchedCatalog.id,
                name = matchedCatalog.name,
                unitPriceDop = matchedCatalog.pricePerDayDop.coerceAtLeast(1),
                quantity = parsedQty,
                photoKey = photoKeyForAsset(matchedCatalog.photo),
            )
        } else {
            PackageDraftLineItem(
                articleId = "custom-$itemIndex",
                name = labelName,
                unitPriceDop = 35,
                quantity = parsedQty,
                photoKey = RfPhotoKey.CENTRO_FLORAL,
            )
        }
    }
}

@Composable
internal fun RenderAdminPackagesA24(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val packagesList = viewModel?.packages?.collectAsState()?.value ?: buildFallbackAdminPackages()
    var fallbackVisibilityById by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var showHiddenOnly by remember { mutableStateOf(false) }

    val activePackageCount = packagesList.count { packageItem ->
        fallbackVisibilityById[packageItem.id] ?: packageItem.visibleInCatalog
    }
    val displayedPackages = remember(packagesList, fallbackVisibilityById, showHiddenOnly) {
        if (!showHiddenOnly) {
            packagesList
        } else {
            packagesList.filter { pkg -> !(fallbackVisibilityById[pkg.id] ?: pkg.visibleInCatalog) }
        }
    }
    val primaryPillGradient = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    val adminPackagesTopPadding = if (includeHeader) 16.dp else 6.dp
    var pendingDeletePackage by remember { mutableStateOf<EventPackage?>(null) }
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = adminPackagesTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (includeHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:A12") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Paquetes",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                        )
                        BasicText(
                            text = "$activePackageCount activos en el catálogo",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                            .clip(CircleShape)
                            .background(primaryPillGradient)
                            .clickable {
                                viewModel?.startEditingPackage("NEW")
                                onSelectScreenCode("A25")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("+ Nuevo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(false to "Todos (${packagesList.size})", true to "Ocultos").forEach { (isHiddenTab, tabText) ->
                    val selectedTab = showHiddenOnly == isHiddenTab
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(if (selectedTab) rfAdminIndigoPillBg() else RfThemeState.currentPalette.surface)
                            .border(1.dp, if (selectedTab) Color(0xFF5B6CF0) else RfThemeState.currentPalette.line, CircleShape)
                            .clickable { showHiddenOnly = isHiddenTab }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = tabText,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab) rfAdminIndigoPillInk() else RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                }
            }

            displayedPackages.forEach { packageItem ->
                val isPackageActive = fallbackVisibilityById[packageItem.id] ?: packageItem.visibleInCatalog
                val normalValue = (packageItem.priceDop + packageItem.savingsDop).coerceAtLeast(1)
                val discountPct = ((packageItem.savingsDop * 100) / normalValue).coerceAtLeast(0)
                val discountBadge = if (discountPct > 0) "$discountPct% desc." else "${packageItem.suggestedGuests} inv."

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.selectPackage(packageItem.id)
                            viewModel?.startEditingPackage(packageItem.id)
                            onSelectScreenCode("A25")
                        }
                        .padding(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfPhoto(
                            photoKey = photoKeyForAsset(packageItem.photo),
                            contentDescription = packageItem.name,
                            modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(
                                text = packageItem.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicText(
                                    text = "${packageItem.includedItems.size} artículos · ",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                                )
                                BasicText(
                                    text = discountBadge,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rfAdminSuccessPillInk()),
                                )
                            }
                            BasicText(
                                text = formatAdminDop(packageItem.priceDop),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPackageActive) primaryPillGradient else SolidColor(RfThemeState.currentPalette.fieldBorder))
                                    .clickable {
                                        val nextVisible = !isPackageActive
                                        val updatedMap = fallbackVisibilityById.toMutableMap()
                                        updatedMap[packageItem.id] = nextVisible
                                        fallbackVisibilityById = updatedMap
                                        if (viewModel != null) {
                                            viewModel.startEditingPackage(packageItem.id)
                                            viewModel.saveAdminPackage(
                                                name = packageItem.name,
                                                subtitle = packageItem.subtitle,
                                                eventType = packageItem.eventType,
                                                suggestedGuests = packageItem.suggestedGuests,
                                                priceDop = packageItem.priceDop,
                                                includedItems = packageItem.includedItems,
                                                visibleInCatalog = nextVisible,
                                            )
                                            viewModel.startEditingPackage(null)
                                        }
                                    }
                                    .padding(3.dp),
                                contentAlignment = if (isPackageActive) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                )
                            }
                            if (viewModel != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(rfAdminDangerPillBg())
                                        .clickable { pendingDeletePackage = packageItem },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CLOSE, tint = rfAdminDangerPillInk(), size = 14.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        val packageToDelete = pendingDeletePackage
        RfDeleteConfirmationDialog(
            visible = packageToDelete != null,
            title = "Eliminar paquete",
            message = if (packageToDelete != null) {
                "¿Seguro que deseas eliminar \"${packageToDelete.name}\" del catálogo? Esta acción no se puede deshacer."
            } else {
                ""
            },
            errorMessage = formError,
            isSubmitting = isSubmitting,
            onDismiss = { pendingDeletePackage = null },
            onConfirm = {
                if (packageToDelete != null) {
                    viewModel?.deletePackageById(packageToDelete.id)
                    pendingDeletePackage = null
                }
            },
        )
    }
}

@Composable
internal fun RenderAdminPackageEditA25(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val packagesList = viewModel?.packages?.collectAsState()?.value ?: buildFallbackAdminPackages()
    val editingPackageId = viewModel?.editingPackageId?.collectAsState()?.value
    val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val formError = viewModel?.formError?.collectAsState()?.value

    val matchedExistingPackage = if (editingPackageId != null) {
        packagesList.firstOrNull { packageItem -> packageItem.id == editingPackageId }
    } else {
        null
    }
    val isCreatingNewPackage = editingPackageId == "NEW"

    var packageName by remember(editingPackageId) {
        val initialName = when {
            isCreatingNewPackage -> ""
            matchedExistingPackage != null -> matchedExistingPackage.name
            else -> "Bodas Rosa"
        }
        mutableStateOf(initialName)
    }
    var packageSubtitle by remember(editingPackageId) {
        val initialSub = when {
            isCreatingNewPackage -> ""
            matchedExistingPackage != null -> matchedExistingPackage.subtitle
            else -> "Ceremonia y recepción romántica"
        }
        mutableStateOf(initialSub)
    }
    var packageEventType by remember(editingPackageId) {
        val initialType = when {
            isCreatingNewPackage -> "Boda"
            matchedExistingPackage != null -> matchedExistingPackage.eventType
            else -> "Boda"
        }
        mutableStateOf(initialType)
    }
    var suggestedGuestsText by remember(editingPackageId) {
        val initialGuests = when {
            isCreatingNewPackage -> "50"
            matchedExistingPackage != null -> matchedExistingPackage.suggestedGuests.toString()
            else -> "120"
        }
        mutableStateOf(initialGuests)
    }
    var visibleInCatalog by remember(editingPackageId) {
        val initialVisible = matchedExistingPackage?.visibleInCatalog ?: true
        mutableStateOf(initialVisible)
    }
    var packageLines by remember(editingPackageId) {
        val initialLines = when {
            isCreatingNewPackage -> emptyList()
            matchedExistingPackage != null -> parseIncludedItemsIntoDraftLines(matchedExistingPackage.includedItems, catalogArticles)
            else -> buildCanonicalBodasRosaLines()
        }
        mutableStateOf(initialLines)
    }
    var discountPercent by remember(editingPackageId) {
        val initialDiscount = when {
            isCreatingNewPackage -> 10

            matchedExistingPackage != null -> {
                val baseTotal = (matchedExistingPackage.priceDop + matchedExistingPackage.savingsDop).coerceAtLeast(1)
                ((matchedExistingPackage.savingsDop * 100) / baseTotal).coerceIn(5, 40)
            }

            else -> 15
        }
        mutableStateOf(initialDiscount)
    }

    var showCatalogPickerModal by remember { mutableStateOf(false) }
    var showDeletePackageConfirm by remember { mutableStateOf(false) }
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var hasPackageNameInteracted by remember(editingPackageId) { mutableStateOf(false) }
    var hasSubtitleInteracted by remember(editingPackageId) { mutableStateOf(false) }
    var hasGuestsInteracted by remember(editingPackageId) { mutableStateOf(false) }
    var forcePackageValidation by remember(editingPackageId) { mutableStateOf(false) }

    val packageNameValidator: (String) -> String? = { rawName ->
        InputValidator.validateEntityName(
            rawValue = rawName,
            fieldLabel = "El nombre del paquete",
            minLength = 3,
            maxLength = InputValidator.MAX_PACKAGE_NAME_LENGTH,
            isRequired = true,
        )
    }
    val packageSubtitleValidator: (String) -> String? = { rawSubtitle ->
        InputValidator.validateNotes(
            rawValue = rawSubtitle,
            fieldLabel = "El subtítulo",
            isRequired = false,
            maxLength = 120,
        )
    }
    val suggestedGuestsValidator: (String) -> String? = { rawGuests ->
        InputValidator.validateIntegerCount(
            rawValue = rawGuests,
            fieldLabel = "La cantidad de invitados",
            minValue = 10,
            maxValue = InputValidator.MAX_GUEST_COUNT,
            isRequired = true,
        )
    }

    val normalPriceDop = packageLines.sumOf { lineEntry -> lineEntry.unitPriceDop * lineEntry.quantity }
    val discountedPriceDop = normalPriceDop * (100 - discountPercent) / 100
    val headerTitle = if (isCreatingNewPackage) "Nuevo paquete" else "Editar paquete"
    val previewPhoto = matchedExistingPackage?.let { existingPkg -> photoKeyForAsset(existingPkg.photo) }
        ?: photoKeyForEventType(packageEventType)
    val eventTypeOptions = listOf("Boda", "Quinceañera", "Cumpleaños", "Corporativo")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable {
                                viewModel?.startEditingPackage(null)
                                onSelectScreenCode("BACK:A24")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(headerTitle, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        BasicText(
                            text = packageName.ifBlank { "Configura artículos y descuento" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    ) {
                        RfPhoto(photoKey = previewPhoto, contentDescription = packageName.ifBlank { "Paquete" }, modifier = Modifier.fillMaxSize())
                    }

                    AdminDebouncedInputField(
                        value = packageName,
                        onValueChange = { updatedText -> packageName = updatedText },
                        validator = packageNameValidator,
                        hasInteracted = hasPackageNameInteracted,
                        onInteracted = { hasPackageNameInteracted = true },
                        forceShowError = forcePackageValidation,
                        label = "Nombre",
                        placeholder = "Ej. Paquete Jardín Imperial",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        cornerRadius = 12.dp,
                        minHeight = 42.dp,
                        horizontalPadding = 12.dp,
                        textFontSize = 14.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 12.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AdminDebouncedInputField(
                        value = packageSubtitle,
                        onValueChange = { updatedSubtitle -> packageSubtitle = updatedSubtitle },
                        validator = packageSubtitleValidator,
                        hasInteracted = hasSubtitleInteracted,
                        onInteracted = { hasSubtitleInteracted = true },
                        forceShowError = forcePackageValidation,
                        label = "Subtítulo",
                        placeholder = "Descripción breve",
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        cornerRadius = 12.dp,
                        minHeight = 38.dp,
                        horizontalPadding = 10.dp,
                        textFontSize = 13.sp,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 12.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.weight(1f),
                    )

                    AdminDebouncedInputField(
                        value = suggestedGuestsText,
                        onValueChange = { updatedGuests -> suggestedGuestsText = updatedGuests },
                        validator = suggestedGuestsValidator,
                        hasInteracted = hasGuestsInteracted,
                        onInteracted = { hasGuestsInteracted = true },
                        forceShowError = forcePackageValidation,
                        label = "Invitados",
                        placeholder = "50",
                        inputFilter = { rawGuests -> InputValidator.filterDigitsOnly(rawGuests, maxDigits = 5) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        cornerRadius = 12.dp,
                        minHeight = 38.dp,
                        horizontalPadding = 10.dp,
                        textFontSize = 13.sp,
                        textFontWeight = FontWeight.SemiBold,
                        textColor = RfThemeState.currentPalette.ink,
                        labelFontSize = 12.sp,
                        labelColor = RfThemeState.currentPalette.ink,
                        modifier = Modifier.width(98.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    eventTypeOptions.forEach { typeOption ->
                        val isTypeSelected = packageEventType.equals(typeOption, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isTypeSelected) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                                    },
                                )
                                .clickable { packageEventType = typeOption }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = typeOption,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isTypeSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isTypeSelected) Color.White else RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                }

                BasicText("INCLUYE", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted, letterSpacing = 1.sp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp)),
                ) {
                    Column {
                        if (packageLines.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Agrega artículos del catálogo para armar este paquete.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                        }

                        packageLines.forEachIndexed { lineIndex, lineEntry ->
                            if (lineIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                ) {
                                    RfPhoto(photoKey = lineEntry.photoKey, contentDescription = lineEntry.name, modifier = Modifier.fillMaxSize())
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = lineEntry.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink),
                                    )
                                    BasicText(
                                        text = "RD$ ${lineEntry.unitPriceDop} c/u",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = RfThemeState.currentPalette.muted),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(CircleShape)
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 3.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    val nextQty = lineEntry.quantity - 1
                                                    packageLines = if (nextQty <= 0) {
                                                        packageLines.filterIndexed { currentIdx, _ -> currentIdx != lineIndex }
                                                    } else {
                                                        packageLines.mapIndexed { currentIdx, currentItem ->
                                                            if (currentIdx == lineIndex) currentItem.copy(quantity = nextQty) else currentItem
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("−", style = TextStyle(fontSize = 15.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                        }
                                        BasicText(
                                            text = "${lineEntry.quantity}",
                                            modifier = Modifier.widthIn(min = 24.dp),
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.Center),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    packageLines = packageLines.mapIndexed { currentIdx, currentItem ->
                                                        if (currentIdx == lineIndex) currentItem.copy(quantity = currentItem.quantity + 1) else currentItem
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText("+", style = TextStyle(fontSize = 15.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clickable { showCatalogPickerModal = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RfIcon(kind = RfIconKind.PLUS, tint = rfAdminIndigoPillInk(), size = 16.dp)
                                BasicText("Agregar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                        .clip(RoundedCornerShape(18.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
                        .clickable {
                            discountPercent = when (discountPercent) {
                                15 -> 20
                                20 -> 10
                                else -> 15
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Descuento del paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                            BasicText("$discountPercent%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = rfAdminIndigoPillInk()))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RfThemeState.currentPalette.line),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((discountPercent / 50f).coerceIn(0.1f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AdminButtonGradient),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                BasicText("Precio normal ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                                BasicText(
                                    text = formatAdminDop(normalPriceDop),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted, textDecoration = TextDecoration.LineThrough),
                                )
                            }
                            BasicText(
                                text = formatAdminDop(discountedPriceDop),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                            )
                        }
                    }
                }

                AdminFormErrorBanner(errorMessage = localValidationError ?: formError)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (viewModel != null) {
                    if (editingPackageId != null) {
                        if (!isCreatingNewPackage) {
                            Box(
                                modifier = Modifier
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(rfAdminDangerPillBg())
                                    .border(1.dp, Color(0xFFF5C2CD), RoundedCornerShape(16.dp))
                                    .clickable(enabled = !isSubmitting) { showDeletePackageConfirm = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("Eliminar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rfAdminDangerPillInk()))
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable(enabled = !isSubmitting) {
                            forcePackageValidation = true
                            val firstValidationError = listOfNotNull(
                                packageNameValidator(packageName),
                                packageSubtitleValidator(packageSubtitle),
                                suggestedGuestsValidator(suggestedGuestsText),
                                if (packageLines.isEmpty()) "Agrega al menos un artículo al paquete." else null,
                            ).firstOrNull()
                            if (firstValidationError != null) {
                                localValidationError = firstValidationError
                            } else {
                                localValidationError = null
                                val trimmedName = packageName.trim()
                                val activeVm = viewModel
                                if (activeVm != null) {
                                    if (isCreatingNewPackage) {
                                        activeVm.startEditingPackage(null)
                                    }
                                    val parsedGuests = suggestedGuestsText.toIntOrNull()
                                    if (parsedGuests == null) {
                                        localValidationError = "Ingresa una cantidad de invitados válida."
                                    } else {
                                        val formattedIncluded = packageLines.map { lineEntry ->
                                            "${lineEntry.quantity}x ${lineEntry.name}"
                                        }
                                        val resolvedSub = packageSubtitle.trim().ifBlank {
                                            "Paquete $packageEventType · ${packageLines.size} artículos"
                                        }
                                        val savedPackage = activeVm.saveAdminPackage(
                                            name = trimmedName,
                                            subtitle = resolvedSub,
                                            eventType = packageEventType,
                                            suggestedGuests = parsedGuests,
                                            priceDop = discountedPriceDop.coerceAtLeast(1),
                                            includedItems = formattedIncluded,
                                            visibleInCatalog = visibleInCatalog,
                                        )
                                        if (savedPackage != null) {
                                            activeVm.startEditingPackage(null)
                                            onSelectScreenCode("A24")
                                        }
                                    }
                                } else {
                                    onSelectScreenCode("A24")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        AdminCenteredDialogOverlay(
            visible = showCatalogPickerModal,
            onDismiss = { showCatalogPickerModal = false },
            verticalSpacing = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Agregar artículo al paquete", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showCatalogPickerModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                catalogArticles.forEach { catalogItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .clickable {
                                val existingIdx = packageLines.indexOfFirst { lineEntry ->
                                    eitherCondition(
                                        lineEntry.articleId == catalogItem.id,
                                        lineEntry.name.equals(catalogItem.name, ignoreCase = true),
                                    )
                                }
                                packageLines = if (existingIdx >= 0) {
                                    packageLines.mapIndexed { idx, lineEntry ->
                                        if (idx == existingIdx) lineEntry.copy(quantity = lineEntry.quantity + 1) else lineEntry
                                    }
                                } else {
                                    packageLines + PackageDraftLineItem(
                                        articleId = catalogItem.id,
                                        name = catalogItem.name,
                                        unitPriceDop = catalogItem.pricePerDayDop.coerceAtLeast(1),
                                        quantity = 1,
                                        photoKey = photoKeyForAsset(catalogItem.photo),
                                    )
                                }
                                showCatalogPickerModal = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfPhoto(
                            photoKey = photoKeyForAsset(catalogItem.photo),
                            contentDescription = catalogItem.name,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = catalogItem.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                text = "${catalogItem.code} · RD$ ${catalogItem.pricePerDayDop}/día",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                            )
                        }
                        RfIcon(kind = RfIconKind.PLUS, tint = rfAdminIndigoPillInk(), size = 18.dp)
                    }
                }
            }
        }

        RfDeleteConfirmationDialog(
            visible = showDeletePackageConfirm,
            title = "Eliminar paquete",
            message = "¿Seguro que deseas eliminar \"${packageName.ifBlank { "este paquete" }}\" del catálogo? Esta acción no se puede deshacer.",
            errorMessage = formError,
            isSubmitting = isSubmitting,
            onDismiss = { showDeletePackageConfirm = false },
            onConfirm = {
                val targetId = editingPackageId
                if (targetId != null) {
                    if (viewModel.deletePackageById(targetId)) {
                        showDeletePackageConfirm = false
                        viewModel.startEditingPackage(null)
                        onSelectScreenCode("A24")
                    }
                }
            },
        )
    }
}

// ==============================================================================
// A26 · DIRECTORIO DE CLIENTES
// ==============================================================================
@Composable
internal fun RenderAdminClientsA26(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val searchValidator: (String) -> String? = { rawQuery ->
        InputValidator.validateSearchQuery(rawQuery)
    }
    val searchField = rememberDebouncedFieldController(
        initialValue = "",
        validator = searchValidator,
    )
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val repositoryClients = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val registeredAccounts = viewModel?.registeredAccounts?.collectAsState()?.value ?: emptyList()

    val existingEmails = repositoryClients.map { profile -> profile.email.trim().lowercase() }.toSet()
    val extraClients = registeredAccounts
        .filter { account -> account.email.trim().lowercase() !in existingEmails }
        .map { account ->
            ClientProfile(
                id = "cli-reg-${account.username}",
                fullName = account.fullName,
                email = account.email,
                phone = account.phone.ifBlank { "Sin teléfono" },
                zone = "Santo Domingo",
                eventsCount = 0,
                totalContractedDop = 0,
                pendingBalanceDop = 0,
                vipTag = "Registrado",
            )
        }
    val allClients = repositoryClients + extraClients

    val vipClientsCount = allClients.count { profile -> isVipClientProfile(profile) }
    val balanceClientsCount = allClients.count { profile -> profile.pendingBalanceDop > 0 }
    val newClientsCount = allClients.count { profile -> isNewClientProfile(profile) }

    val filteredClients = allClients.filter { profile ->
        val matchesChip = when (selectedFilterIndex) {
            1 -> isVipClientProfile(profile)
            2 -> profile.pendingBalanceDop > 0
            3 -> isNewClientProfile(profile)
            else -> true
        }
        val trimmedQuery = searchField.value.trim()
        val combinedText = "${profile.fullName} ${profile.email} ${profile.phone} ${profile.zone} ${profile.vipTag}"
        val matchesQuery = if (trimmedQuery.isEmpty()) {
            true
        } else {
            combinedText.contains(trimmedQuery, ignoreCase = true)
        }
        bothConditions(matchesChip, matchesQuery)
    }

    val adminClientsTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = adminClientsTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (includeHeader) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Clientes",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "${filteredClients.size} clientes",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                    )
                }
            }
        }

        AdminDebouncedInputField(
            controller = searchField,
            validator = searchValidator,
            placeholder = "Buscar por nombre, correo o teléfono",
            leadingIcon = RfIconKind.SEARCH,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            containerColor = RfThemeState.currentPalette.surface,
            minHeight = 50.dp,
            horizontalPadding = 14.dp,
            textFontSize = 15.sp,
            textColor = RfThemeState.currentPalette.ink,
        )

        val filterChips = listOf(
            "Todos · ${allClients.size}",
            "VIP · $vipClientsCount",
            "Con saldo · $balanceClientsCount",
            "Nuevos · $newClientsCount",
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterChips.forEachIndexed { chipIndex, chipLabel ->
                val isSelected = selectedFilterIndex == chipIndex
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(RfThemeState.currentPalette.surface).border(1.dp, RfThemeState.currentPalette.fieldBorder, CircleShape)
                            },
                        )
                        .clickable { selectedFilterIndex = chipIndex }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        chipLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else RfThemeState.currentPalette.ink,
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (filteredClients.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "No se encontraron clientes con ese criterio.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }
                filteredClients.forEachIndexed { clientIndex, profile ->
                    if (clientIndex > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                    }
                    val avatarColors = clientAvatarColors(clientIndex)
                    val tagColors = clientTagColors(profile)
                    val initialChar = InputValidator.extractInitials(profile.fullName, fallback = "C", maxInitials = 1)
                    val formattedPhone = if (profile.phone.isBlank()) {
                        "Sin teléfono"
                    } else {
                        InputValidator.formatPhoneDisplay(profile.phone)
                    }
                    val summaryLine = if (profile.pendingBalanceDop > 0) {
                        "${profile.eventsCount} eventos · saldo RD$ ${profile.pendingBalanceDop}"
                    } else if (profile.totalContractedDop > 0) {
                        "${profile.eventsCount} eventos · pagó RD$ ${profile.totalContractedDop}"
                    } else {
                        "${profile.email} · $formattedPhone"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.selectClient(profile.id)
                                onSelectScreenCode("A27")
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(avatarColors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(initialChar, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = avatarColors.second))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(profile.fullName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                            BasicText(summaryLine, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted))
                        }
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(tagColors.first)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(profile.vipTag.ifBlank { "Cliente" }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagColors.second))
                        }
                    }
                }
            }
        }
    }
}

private fun isVipClientProfile(profile: ClientProfile): Boolean {
    val tag = profile.vipTag.lowercase()
    val hasVipTag = eitherCondition(tag.contains("vip"), tag.contains("frecuente"))
    val highSpend = profile.totalContractedDop >= 5000
    return eitherCondition(hasVipTag, highSpend)
}

private fun isNewClientProfile(profile: ClientProfile): Boolean {
    val tag = profile.vipTag.lowercase()
    val hasNewTag = eitherCondition(tag.contains("nuev"), tag.contains("registrad"))
    return eitherCondition(hasNewTag, profile.eventsCount <= 1)
}

private fun clientAvatarColors(index: Int): Pair<Color, Color> = when (index % 4) {
    0 -> Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)
    1 -> Pair(rfAdminSuccessPillBg(), rfAdminSuccessPillInk())
    2 -> Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())
    else -> Pair(rfAdminIndigoPillBg(), rfAdminIndigoPillInk())
}

private fun clientTagColors(profile: ClientProfile): Pair<Color, Color> = when {
    profile.pendingBalanceDop > 0 -> Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())
    isVipClientProfile(profile) -> Pair(rfAdminIndigoPillBg(), rfAdminIndigoPillInk())
    else -> Pair(rfAdminSuccessPillBg(), rfAdminSuccessPillInk())
}

private fun resolveAdminEventOrNull(
    selectedEventId: String,
    activeEvent: ClientEvent?,
    eventsList: List<ClientEvent>,
    isPreviewMode: Boolean,
): ClientEvent? {
    val bySelected = eventsList.firstOrNull { eventItem -> eventItem.id == selectedEventId }
    if (bySelected != null) return bySelected
    if (activeEvent != null) return activeEvent
    val firstAvailable = eventsList.firstOrNull()
    if (firstAvailable != null) return firstAvailable
    return if (isPreviewMode) CanonicalSampleData.events.first() else null
}

private fun matchesClientRecord(eventItem: ClientEvent, client: ClientProfile): Boolean {
    val cleanEventEmail = eventItem.clientEmail.trim()
    val cleanClientEmail = client.email.trim()
    val hasEmails = bothConditions(cleanEventEmail.isNotEmpty(), cleanClientEmail.isNotEmpty())
    val emailMatches = if (hasEmails) cleanEventEmail.equals(cleanClientEmail, ignoreCase = true) else false
    if (emailMatches) {
        return true
    }
    return eventItem.clientName.trim().equals(client.fullName.trim(), ignoreCase = true)
}

@Composable
internal fun RenderAdminClientProfileA27(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    val selectedClientId = viewModel?.selectedClientId?.collectAsState()?.value
        ?: if (viewModel == null) CanonicalSampleData.clients.first().id else ""
    val clientsList = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val resolvedClient = clientsList.firstOrNull { profile -> profile.id == selectedClientId }
        ?: viewModel?.selectedClient()
        ?: clientsList.firstOrNull()
        ?: if (viewModel == null) CanonicalSampleData.clients.first() else null
    if (resolvedClient == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Perfil de cliente",
            emptyTitle = "Aún no tienes clientes",
            emptyMessage = "Los perfiles de tus clientes aparecerán automáticamente al crear eventos o cotizaciones.",
            backTargetCode = "BACK:A26",
            iconKind = RfIconKind.USER,
            onSelectScreenCode = onSelectScreenCode,
            ctaLabel = "Ir a clientes",
            ctaTargetCode = "A26",
        )
        return
    }
    val activeClient = resolvedClient

    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments

    val clientEvents = eventsState.filter { eventItem -> matchesClientRecord(eventItem, activeClient) }
    val clientPayments = paymentsState.filter { paymentItem ->
        paymentItem.clientName.trim().equals(activeClient.fullName.trim(), ignoreCase = true)
    }
    val paidFromPayments = clientPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val paidFromEvents = clientEvents.sumOf { eventItem -> eventItem.paidDepositDop }
    val paidFallback = (activeClient.totalContractedDop - activeClient.pendingBalanceDop).coerceAtLeast(0)
    val totalPaidDop = when {
        paidFromPayments > 0 -> paidFromPayments
        paidFromEvents > 0 -> paidFromEvents
        else -> paidFallback
    }
    val displayedEventsCount = maxOf(clientEvents.size, activeClient.eventsCount)

    val defaultClientNote = activeClient.notes.ifBlank {
        "Nota interna de ${activeClient.fullName}: zona ${activeClient.zone} · seguimiento personalizado."
    }
    var internalNote by remember(activeClient.id, activeClient.notes) { mutableStateOf(defaultClientNote) }
    var hasInternalNoteInteracted by remember(activeClient.id, activeClient.notes) { mutableStateOf(false) }
    var forceInternalNoteValidation by remember(activeClient.id, activeClient.notes) { mutableStateOf(false) }
    var showEditClientModal by remember { mutableStateOf(false) }
    var editName by remember(activeClient.id) { mutableStateOf(activeClient.fullName) }
    var editEmail by remember(activeClient.id) { mutableStateOf(activeClient.email) }
    var editPhone by remember(activeClient.id) { mutableStateOf(InputValidator.filterPhoneInput(activeClient.phone)) }
    var editNotes by remember(activeClient.id) { mutableStateOf(internalNote) }
    var editValidationError by remember { mutableStateOf<String?>(null) }
    var hasEditNameInteracted by remember(activeClient.id) { mutableStateOf(false) }
    var hasEditEmailInteracted by remember(activeClient.id) { mutableStateOf(false) }
    var hasEditPhoneInteracted by remember(activeClient.id) { mutableStateOf(false) }
    var hasEditNotesInteracted by remember(activeClient.id) { mutableStateOf(false) }
    var forceEditValidation by remember(activeClient.id) { mutableStateOf(false) }

    val internalNoteValidator: (String) -> String? = { rawNote ->
        InputValidator.validateNotes(
            rawValue = rawNote,
            fieldLabel = "La nota interna",
            isRequired = false,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }
    val editNameValidator: (String) -> String? = { rawName ->
        InputValidator.validateFullName(rawName)
    }
    val editEmailValidator: (String) -> String? = { rawEmail ->
        InputValidator.validateEmail(rawEmail)
    }
    val editPhoneValidator: (String) -> String? = { rawPhone ->
        InputValidator.validateDominicanPhone(
            rawValue = rawPhone,
            isRequired = true,
            allowInternational = true,
        )
    }
    val editNotesValidator: (String) -> String? = { rawNotes ->
        InputValidator.validateNotes(
            rawValue = rawNotes,
            fieldLabel = "Las notas CRM",
            isRequired = false,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    val clientInitial = InputValidator.extractInitials(activeClient.fullName, fallback = "C", maxInitials = 1)

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A26") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Cliente",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable {
                            editName = activeClient.fullName
                            editEmail = activeClient.email
                            editPhone = InputValidator.filterPhoneInput(activeClient.phone)
                            editNotes = internalNote
                            editValidationError = null
                            hasEditNameInteracted = false
                            hasEditEmailInteracted = false
                            hasEditPhoneInteracted = false
                            hasEditNotesInteracted = false
                            forceEditValidation = false
                            showEditClientModal = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }
            }

            AdminFormErrorBanner(errorMessage = formError)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(RfColors.VioletaTint),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = RfColors.VioletaTintInk, fontSize = 28.sp))
                }
                val formattedClientPhone = if (activeClient.phone.isBlank()) {
                    "Sin teléfono"
                } else {
                    InputValidator.formatPhoneDisplay(activeClient.phone)
                }
                BasicText(activeClient.fullName, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink, fontSize = 22.sp))
                BasicText("${activeClient.email} · $formattedClientPhone", style = TextStyle(fontFamily = typo.bodyFamily, color = RfThemeState.currentPalette.muted, fontSize = 13.sp))
                BasicText("${activeClient.zone} · ${activeClient.vipTag}", style = TextStyle(fontFamily = typo.bodyFamily, color = rfAdminIndigoPillInk(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable { viewModel?.dialPhoneNumber(activeClient.phone) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.PHONE, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                        BasicText("Llamar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.openWhatsAppChat(
                                activeClient.phone,
                                "Hola ${activeClient.fullName}, te escribimos de Rosa Fiesta sobre tus eventos.",
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = rfAdminSuccessPillInk(), size = 18.dp)
                        BasicText("WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk()))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.selectChatThreadForContact(activeClient.fullName)
                            onSelectScreenCode("A15")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = rfAdminIndigoPillInk(), size = 18.dp)
                        BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk()))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(rfAdminIndigoPillBg())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("$displayedEventsCount", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = rfAdminIndigoPillInk(), fontSize = 20.sp))
                        BasicText("Eventos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = rfAdminIndigoPillInk(), fontSize = 12.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(rfAdminSuccessPillBg())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("RD$ $totalPaidDop", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C), fontSize = 20.sp))
                        BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = rfAdminSuccessPillInk(), fontSize = 12.sp))
                    }
                }
            }

            BasicText("EVENTOS", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = RfThemeState.currentPalette.muted))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
            ) {
                Column {
                    if (clientEvents.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(
                                text = "Sin eventos registrados aún",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(rfAdminIndigoPillBg())
                                    .clickable {
                                        viewModel?.setNewEventClient(activeClient.fullName)
                                        onSelectScreenCode("A05")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                BasicText("Crear evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rfAdminIndigoPillInk()))
                            }
                        }
                    }

                    clientEvents.forEachIndexed { eventIndex, clientEvent ->
                        if (eventIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel?.selectEvent(clientEvent.id)
                                    onSelectScreenCode("A13")
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = photoKeyForAsset(clientEvent.photo), contentDescription = clientEvent.title, modifier = Modifier.fillMaxSize())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(clientEvent.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                                BasicText("${formatReadableSpanishDateText(clientEvent.dateLabel)} · ${clientEvent.statusLabel}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                        }
                    }
                }
            }

            val internalNoteError = rememberDebouncedValidationError(
                value = internalNote,
                hasInteracted = hasInternalNoteInteracted,
                forceShowError = forceInternalNoteValidation,
                validator = internalNoteValidator,
            )
            val hasNoteError = internalNoteError != null
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(rfAdminNoteAmberBg())
                        .then(
                            if (hasNoteError) {
                                Modifier.border(1.5.dp, RfColors.Error, RoundedCornerShape(18.dp))
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.EDIT, tint = if (hasNoteError) RfColors.Error else rfAdminWarnPillInk(), size = 18.dp)
                            BasicTextField(
                                value = internalNote,
                                onValueChange = { updatedNote ->
                                    hasInternalNoteInteracted = true
                                    internalNote = updatedNote
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done,
                                ),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 19.sp, color = RfThemeState.currentPalette.ink),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(rfAdminWarnPillInk())
                                    .clickable(enabled = !isSubmitting) {
                                        forceInternalNoteValidation = true
                                        val noteError = internalNoteValidator(internalNote)
                                        if (noteError == null) {
                                            viewModel?.updateSelectedClientProfile(
                                                fullName = activeClient.fullName,
                                                email = activeClient.email,
                                                phone = activeClient.phone,
                                                notes = internalNote.trim(),
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                BasicText("Guardar nota", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }
                RfValidationErrorText(errorMessage = internalNoteError)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        AdminCenteredDialogOverlay(
            visible = showEditClientModal,
            onDismiss = { showEditClientModal = false },
            verticalSpacing = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Editar datos del cliente", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showEditClientModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            AdminDebouncedInputField(
                value = editName,
                onValueChange = { nextName -> editName = nextName },
                validator = editNameValidator,
                hasInteracted = hasEditNameInteracted,
                onInteracted = { hasEditNameInteracted = true },
                forceShowError = forceEditValidation,
                label = "Nombre completo",
                placeholder = "Ej. Laura Méndez",
                inputFilter = { rawName -> InputValidator.filterPersonNameInput(rawName, InputValidator.MAX_FULL_NAME_LENGTH) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = editEmail,
                onValueChange = { nextEmail -> editEmail = nextEmail },
                validator = editEmailValidator,
                hasInteracted = hasEditEmailInteracted,
                onInteracted = { hasEditEmailInteracted = true },
                forceShowError = forceEditValidation,
                label = "Correo electrónico",
                placeholder = "cliente@correo.com",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = editPhone,
                onValueChange = { nextPhone -> editPhone = nextPhone },
                validator = editPhoneValidator,
                hasInteracted = hasEditPhoneInteracted,
                onInteracted = { hasEditPhoneInteracted = true },
                forceShowError = forceEditValidation,
                label = "Teléfono",
                placeholder = "(809) 555-0142",
                cursorMask = InputValidator::filterPhoneInputWithCursor,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = editNotes,
                onValueChange = { nextNotes -> editNotes = nextNotes },
                validator = editNotesValidator,
                hasInteracted = hasEditNotesInteracted,
                onInteracted = { hasEditNotesInteracted = true },
                forceShowError = forceEditValidation,
                label = "Notas CRM",
                placeholder = "Preferencias o notas de seguimiento",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                singleLine = false,
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 64.dp,
                horizontalPadding = 12.dp,
                verticalPadding = 10.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminFormErrorBanner(errorMessage = editValidationError ?: formError)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AdminButtonGradient)
                    .clickable(enabled = !isSubmitting) {
                        forceEditValidation = true
                        val firstValidationError = listOfNotNull(
                            editNameValidator(editName),
                            editEmailValidator(editEmail),
                            editPhoneValidator(editPhone),
                            editNotesValidator(editNotes),
                        ).firstOrNull()
                        if (firstValidationError != null) {
                            editValidationError = firstValidationError
                        } else {
                            editValidationError = null
                            val cleanName = editName.trim()
                            val cleanEmail = editEmail.trim()
                            val cleanPhone = InputValidator.formatPhoneDisplay(editPhone.trim())
                            val cleanNotes = editNotes.trim()
                            viewModel?.selectClient(activeClient.id)
                            viewModel?.updateSelectedClientProfile(
                                fullName = cleanName,
                                email = cleanEmail,
                                phone = cleanPhone,
                                notes = cleanNotes,
                            )
                            internalNote = cleanNotes
                            showEditClientModal = false
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar cambios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

private data class StaffPermissionOptionRow(
    val permission: StaffPermission,
    val label: String,
)

private fun resolveStaffAvatarPalette(memberName: String): Pair<Color, Color> {
    val clean = memberName.trim().lowercase()
    return when {
        clean.contains("mar") -> Pair(rfAdminIndigoPillBg(), rfAdminIndigoPillInk())
        clean.contains("pedro") -> Pair(rfAdminTealPillBg(), rfAdminTealPillInk())
        clean.contains("yuli") -> Pair(RfColors.VioletaTint, RfColors.VioletaTintInk)
        else -> Pair(rfAdminWarnPillBg(), rfAdminWarnPillInk())
    }
}

private fun resolveStaffRoleBadge(member: StaffMember): String = when {
    member.roleLabel.contains("Dueñ", ignoreCase = true) -> "Admin"
    member.roleLabel.contains("Admin", ignoreCase = true) -> "Admin"
    StaffPermission.EVENTS !in member.permissions -> "Limitado"
    else -> "Equipo"
}

private fun permissionsForInvitedRoleLabel(roleLabel: String): List<String> = when {
    roleLabel.contains("Admin", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.DECORATION.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
        StaffPermission.PAYMENTS.name,
        StaffPermission.REPORTS.name,
    )

    roleLabel.contains("Decor", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.DECORATION.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
    )

    roleLabel.contains("Log", ignoreCase = true) -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.TASKS.name,
        StaffPermission.INVENTORY.name,
    )

    else -> listOf(
        StaffPermission.EVENTS.name,
        StaffPermission.TASKS.name,
    )
}

@Composable
internal fun RenderAdminTeamA28(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val staffList = viewModel?.staffMembers?.collectAsState()?.value ?: CanonicalSampleData.staffMembers
    val formError = viewModel?.formError?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    var pendingDeleteStaffMember by remember { mutableStateOf<StaffMember?>(null) }

    val initialMemberId = staffList.firstOrNull { member -> member.name.equals("Yuli", ignoreCase = true) }?.id
        ?: staffList.first().id
    var selectedStaffId by remember { mutableStateOf(initialMemberId) }
    val activeMember = staffList.firstOrNull { member -> member.id == selectedStaffId } ?: staffList.first()

    var showInviteModal by remember { mutableStateOf(false) }
    var inviteName by remember { mutableStateOf("") }
    var inviteEmail by remember { mutableStateOf("") }
    var invitePhone by remember { mutableStateOf("") }
    var invitePassword by remember { mutableStateOf("") }
    var inviteRoleLabel by remember { mutableStateOf("Decoración y eventos") }
    var inviteLocalError by remember { mutableStateOf<String?>(null) }
    var hasInviteNameInteracted by remember { mutableStateOf(false) }
    var hasInviteEmailInteracted by remember { mutableStateOf(false) }
    var hasInvitePhoneInteracted by remember { mutableStateOf(false) }
    var hasInvitePasswordInteracted by remember { mutableStateOf(false) }
    var forceInviteValidation by remember { mutableStateOf(false) }

    val inviteNameValidator: (String) -> String? = { rawName ->
        InputValidator.validateFullName(rawName)
    }
    val inviteEmailValidator: (String) -> String? = { rawEmail ->
        InputValidator.validateEmail(rawEmail)
    }
    val invitePhoneValidator: (String) -> String? = { rawPhone ->
        InputValidator.validateDominicanPhone(
            rawValue = rawPhone,
            isRequired = false,
            allowInternational = true,
        )
    }
    val invitePasswordValidator: (String) -> String? = { rawPassword ->
        if (rawPassword.isBlank()) {
            null
        } else {
            InputValidator.validatePassword(rawPassword)
        }
    }

    val roleChoices = listOf("Decoración y eventos", "Logística y montaje", "Almacén y entregas", "Administración")
    val permissionRows = remember {
        listOf(
            StaffPermissionOptionRow(StaffPermission.EVENTS, "Ver y editar eventos"),
            StaffPermissionOptionRow(StaffPermission.INVENTORY, "Inventario y artículos"),
            StaffPermissionOptionRow(StaffPermission.PAYMENTS, "Pagos y cobros"),
            StaffPermissionOptionRow(StaffPermission.REPORTS, "Reportes del negocio"),
            StaffPermissionOptionRow(StaffPermission.DECORATION, "Montaje y decoración de eventos"),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Equipo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "${staffList.size} personas",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable {
                            inviteName = ""
                            inviteEmail = ""
                            invitePhone = ""
                            invitePassword = ""
                            inviteLocalError = null
                            hasInviteNameInteracted = false
                            hasInviteEmailInteracted = false
                            hasInvitePhoneInteracted = false
                            hasInvitePasswordInteracted = false
                            forceInviteValidation = false
                            viewModel?.clearFormError()
                            showInviteModal = true
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.USER, tint = Color.White, size = 18.dp)
                        BasicText("Invitar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
            ) {
                Column {
                    staffList.forEachIndexed { memberIndex, member ->
                        if (memberIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                        }
                        val isHighlighted = member.id == activeMember.id
                        val avatarPalette = resolveStaffAvatarPalette(member.name)
                        val roleBadge = resolveStaffRoleBadge(member)
                        val tagBg = if (roleBadge == "Admin") {
                            rfAdminIndigoPillBg()
                        } else if (roleBadge == "Equipo") {
                            rfAdminTealPillBg()
                        } else {
                            RfThemeState.currentPalette.chipBg
                        }
                        val tagInk = if (roleBadge == "Admin") {
                            rfAdminIndigoPillInk()
                        } else if (roleBadge == "Equipo") {
                            rfAdminTealPillInk()
                        } else {
                            RfThemeState.currentPalette.muted
                        }
                        val initialChar = InputValidator.extractInitials(member.name, fallback = "S", maxInitials = 1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isHighlighted) rfAdminSoftTintBg() else RfThemeState.currentPalette.surface)
                                .clickable { selectedStaffId = member.id }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(avatarPalette.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(initialChar, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = avatarPalette.second))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(member.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RfThemeState.currentPalette.ink))
                                BasicText(member.roleLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RfThemeState.currentPalette.muted))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(tagBg)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(roleBadge, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagInk))
                                }
                                if (viewModel != null) {
                                    if (member.role != UserRole.ADMIN_OWNER) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(rfAdminDangerPillBg())
                                                .clickable { pendingDeleteStaffMember = member },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.CLOSE, tint = rfAdminDangerPillInk(), size = 14.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BasicText(
                text = "PERMISOS DE ${activeMember.name.uppercase()}",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
            ) {
                Column {
                    permissionRows.forEachIndexed { permIndex, optionRow ->
                        val isPaymentsRow = optionRow.permission == StaffPermission.PAYMENTS
                        val inPermissionSet = optionRow.permission in activeMember.permissions
                        val isPermissionActive = if (isPaymentsRow) {
                            eitherCondition(activeMember.canViewPayments, inPermissionSet)
                        } else {
                            inPermissionSet
                        }

                        if (permIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.line))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = isPermissionActive,
                                    role = Role.Switch,
                                    onValueChange = { nextActive ->
                                        val nextSet = activeMember.permissions.toMutableSet()
                                        if (nextActive) {
                                            nextSet.add(optionRow.permission)
                                        } else {
                                            nextSet.remove(optionRow.permission)
                                        }
                                        val permissionNames = nextSet.map { perm -> perm.name }
                                        viewModel?.updateStaffPermissions(
                                            staffId = activeMember.id,
                                            permissions = permissionNames,
                                        )
                                    },
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(optionRow.label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.ink))
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPermissionActive) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(RfThemeState.currentPalette.fieldBorder))
                                    .padding(3.dp),
                                contentAlignment = if (isPermissionActive) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                )
                            }
                        }
                    }
                }
            }
        }

        AdminCenteredDialogOverlay(
            visible = showInviteModal,
            onDismiss = { showInviteModal = false },
            verticalSpacing = 10.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Invitar miembro al equipo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { showInviteModal = false },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                }
            }

            AdminDebouncedInputField(
                value = inviteName,
                onValueChange = { nextName -> inviteName = nextName },
                validator = inviteNameValidator,
                hasInteracted = hasInviteNameInteracted,
                onInteracted = { hasInviteNameInteracted = true },
                forceShowError = forceInviteValidation,
                label = "Nombre del colaborador",
                placeholder = "Ej. Camila Reyes",
                inputFilter = { rawName -> InputValidator.filterPersonNameInput(rawName, InputValidator.MAX_FULL_NAME_LENGTH) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = inviteEmail,
                onValueChange = { nextEmail -> inviteEmail = nextEmail },
                validator = inviteEmailValidator,
                hasInteracted = hasInviteEmailInteracted,
                onInteracted = { hasInviteEmailInteracted = true },
                forceShowError = forceInviteValidation,
                label = "Correo electrónico",
                placeholder = "colaborador@rosafiesta.do",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = invitePhone,
                onValueChange = { nextPhone -> invitePhone = nextPhone },
                validator = invitePhoneValidator,
                hasInteracted = hasInvitePhoneInteracted,
                onInteracted = { hasInvitePhoneInteracted = true },
                forceShowError = forceInviteValidation,
                label = "Teléfono (opcional)",
                placeholder = "(809) 555-0190",
                cursorMask = InputValidator::filterPhoneInputWithCursor,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            AdminDebouncedInputField(
                value = invitePassword,
                onValueChange = { nextPassword -> invitePassword = nextPassword },
                validator = invitePasswordValidator,
                hasInteracted = hasInvitePasswordInteracted,
                onInteracted = { hasInvitePasswordInteracted = true },
                forceShowError = forceInviteValidation,
                label = "Contraseña temporal (opcional, mín. 6)",
                placeholder = "Para acceso inmediato si no hay SMTP",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                containerColor = RfThemeState.currentPalette.chipBg,
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                horizontalPadding = 12.dp,
                textColor = RfThemeState.currentPalette.ink,
            )

            BasicText("Rol inicial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.muted))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                roleChoices.forEach { roleChoice ->
                    val selectedRole = inviteRoleLabel == roleChoice
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(if (selectedRole) rfAdminIndigoPillInk() else RfThemeState.currentPalette.chipBg)
                            .clickable { inviteRoleLabel = roleChoice }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = roleChoice,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedRole) Color.White else RfThemeState.currentPalette.ink,
                            ),
                        )
                    }
                }
            }

            AdminFormErrorBanner(errorMessage = inviteLocalError ?: formError)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AdminButtonGradient)
                    .clickable(enabled = !isSubmitting) {
                        forceInviteValidation = true
                        val firstValidationError = listOfNotNull(
                            inviteNameValidator(inviteName),
                            inviteEmailValidator(inviteEmail),
                            invitePhoneValidator(invitePhone),
                            invitePasswordValidator(invitePassword),
                        ).firstOrNull()
                        if (firstValidationError != null) {
                            inviteLocalError = firstValidationError
                        } else {
                            inviteLocalError = null
                            val cleanName = inviteName.trim()
                            val cleanEmail = inviteEmail.trim()
                            val cleanPhone = invitePhone.trim()
                            val cleanPassword = invitePassword.trim()
                            val perms = permissionsForInvitedRoleLabel(inviteRoleLabel)
                            val invitedMember = viewModel?.inviteNewStaffMember(
                                fullName = cleanName,
                                email = cleanEmail,
                                roleLabel = inviteRoleLabel,
                                phone = cleanPhone,
                                permissions = perms,
                                initialPassword = cleanPassword,
                            )
                            val canClose = if (viewModel == null) true else invitedMember != null
                            if (canClose) {
                                if (invitedMember != null) {
                                    selectedStaffId = invitedMember.id
                                }
                                showInviteModal = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Enviar invitación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }

        val staffToDelete = pendingDeleteStaffMember
        RfDeleteConfirmationDialog(
            visible = staffToDelete != null,
            title = "Eliminar del equipo",
            message = if (staffToDelete != null) {
                "¿Seguro que deseas quitar a ${staffToDelete.name} del equipo? Perderá acceso inmediato al panel."
            } else {
                ""
            },
            errorMessage = formError,
            isSubmitting = isSubmitting,
            onDismiss = { pendingDeleteStaffMember = null },
            onConfirm = {
                if (staffToDelete != null) {
                    viewModel?.deleteStaffMemberById(staffToDelete.id)
                    pendingDeleteStaffMember = null
                }
            },
        )
    }
}
