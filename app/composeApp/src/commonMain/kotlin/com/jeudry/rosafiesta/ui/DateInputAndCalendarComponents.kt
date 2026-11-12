package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.data.createPlatformActions
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfBitmapPhoto
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.rememberDecodedImageBitmap
import kotlinx.coroutines.launch

internal fun computeDaysInGregorianMonth(year: Int, month: Int): Int = when (month) {
    2 -> {
        val isDivisibleBy4 = year % 4 == 0
        val isNotCentury = year % 100 != 0
        val isDivisibleBy400 = year % 400 == 0
        val isCenturyOrFourYear = if (isNotCentury) true else isDivisibleBy400
        val isLeapYear = if (isDivisibleBy4) isCenturyOrFourYear else false
        if (isLeapYear) 29 else 28
    }

    4, 6, 9, 11 -> 30

    else -> 31
}

internal fun computeGregorianMondayFirstOffset(year: Int, month: Int): Int {
    val monthOffsets = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val safeMonth = month.coerceIn(1, 12)
    val adjustedYear = if (safeMonth < 3) year - 1 else year
    val sundayZeroWeekday = (
        adjustedYear +
            adjustedYear / 4 -
            adjustedYear / 100 +
            adjustedYear / 400 +
            monthOffsets[safeMonth - 1] +
            1
        ) % 7
    val nonNegativeWeekday = (sundayZeroWeekday + 7) % 7
    return (nonNegativeWeekday + 6) % 7
}

/**
 * Formats raw digit input into `DD/MM/YYYY` automatically inserting `/` separators
 * so the user never needs to type `/` manually, while handling backspace cleanly.
 */
public fun formatDateDigitsWithSlashes(
    previousFormatted: String,
    newRawText: String,
): String {
    val isDeletingTrailingSlash = if (previousFormatted.endsWith("/")) {
        newRawText == previousFormatted.dropLast(1)
    } else {
        false
    }
    val effectiveSource = if (isDeletingTrailingSlash) {
        newRawText.dropLast(1)
    } else {
        newRawText
    }
    val digitsOnly = effectiveSource.filter { character -> character.isDigit() }.take(8)
    val isTypingForward = newRawText.length > previousFormatted.length

    return when (digitsOnly.length) {
        0 -> ""

        1 -> digitsOnly

        2 -> if (isTypingForward) "$digitsOnly/" else digitsOnly

        3 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"

        4 -> {
            val dayPart = digitsOnly.substring(0, 2)
            val monthPart = digitsOnly.substring(2, 4)
            if (isTypingForward) "$dayPart/$monthPart/" else "$dayPart/$monthPart"
        }

        else -> {
            val dayPart = digitsOnly.substring(0, 2)
            val monthPart = digitsOnly.substring(2, 4)
            val yearPart = digitsOnly.substring(4)
            "$dayPart/$monthPart/$yearPart"
        }
    }
}

/**
 * Reusable date input box with automatic `DD/MM/AAAA` slash masking and a calendar button
 * that opens [RfInteractiveCalendarModal].
 */
@Composable
internal fun RfMaskedDateField(
    value: String,
    onValueChange: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    placeholder: String = "DD/MM/AAAA",
    hasError: Boolean = false,
    isAdmin: Boolean = false,
    compactInline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val textFieldValue = remember(value) {
        TextFieldValue(
            text = value,
            selection = TextRange(value.length),
        )
    }
    val accentColor = if (isAdmin) Color(0xFF4B47D6) else Color(0xFFC2338F)
    val borderColor = when {
        hasError -> RfColors.Error
        isAdmin -> Color(0xFFD5D7EC)
        else -> Color(0xFFD9B8EC)
    }
    val containerBg = if (compactInline) {
        if (palette.isDark) Color(0xFF26243D) else Color(0xFFFBF8FD)
    } else {
        palette.surface
    }
    val fieldHeight = if (compactInline) 40.dp else 52.dp
    val cornerRadius = if (compactInline) 12.dp else 16.dp

    Row(
        modifier = modifier
            .height(fieldHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!compactInline) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onOpenCalendar() },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(
                    kind = RfIconKind.CALENDAR,
                    tint = if (hasError) RfColors.Error else accentColor,
                    size = 18.dp,
                )
            }
        }
        BasicTextField(
            value = textFieldValue,
            onValueChange = { updatedFieldValue ->
                val formattedText = formatDateDigitsWithSlashes(
                    previousFormatted = value,
                    newRawText = updatedFieldValue.text,
                )
                onValueChange(formattedText)
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = if (compactInline) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = if (compactInline) 14.sp else 15.sp,
                color = palette.ink,
            ),
            cursorBrush = SolidColor(accentColor),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = if (compactInline) 13.5.sp else 14.5.sp,
                            color = palette.muted,
                        ),
                    )
                }
                innerTextField()
            },
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (compactInline) 30.dp else 36.dp)
                .clip(CircleShape)
                .background(
                    if (isAdmin) {
                        Color(0xFFECEBFF)
                    } else if (palette.isDark) {
                        Color(0xFF3A1F3D)
                    } else {
                        Color(0xFFFDE6F2)
                    },
                )
                .clickable { onOpenCalendar() },
        ) {
            RfIcon(
                kind = RfIconKind.CALENDAR,
                tint = accentColor,
                size = if (compactInline) 15.dp else 17.dp,
            )
        }
    }
}

/**
 * Interactive calendar modal for selecting any date (birth date or event date) across the app.
 */
@Composable
internal fun RfInteractiveCalendarModal(
    title: String,
    subtitle: String = "Selecciona día, mes y año",
    currentDateText: String,
    minYear: Int = 1925,
    maxYear: Int = 2030,
    defaultYear: Int = 1996,
    quickYears: List<Int> = listOf(1985, 1990, 1995, 1996, 2000, 2004, 2008),
    isAdmin: Boolean = false,
    onSelectDate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val initialParts = currentDateText.split("/")
    val parsedDay = initialParts.getOrNull(0)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(1, 31) ?: 22
    val parsedMonth = initialParts.getOrNull(1)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(1, 12) ?: 10
    val parsedYear = initialParts.getOrNull(2)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(minYear, maxYear) ?: defaultYear

    var selectedDay by remember(currentDateText) { mutableStateOf(parsedDay) }
    var selectedMonth by remember(currentDateText) { mutableStateOf(parsedMonth) }
    var selectedYear by remember(currentDateText) { mutableStateOf(parsedYear) }

    val monthNames = remember {
        listOf(
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre",
        )
    }

    val daysInSelectedMonth = computeDaysInGregorianMonth(selectedYear, selectedMonth)
    val leadingWeekdayOffset = computeGregorianMondayFirstOffset(selectedYear, selectedMonth)

    val clampedSelectedDay = selectedDay.coerceAtMost(daysInSelectedMonth)
    val primaryBrush = if (isAdmin) {
        Brush.horizontalGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)))
    }
    val activeChipColor = if (isAdmin) Color(0xFF5B6CF0) else Color(0xFF865DEC)
    val selectorBoxBg = if (palette.isDark) Color(0xFF26243D) else Color(0xFFF8F5FB)
    val dayCellBg = if (palette.isDark) Color(0xFF26243D) else Color(0xFFFAFAFC)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151729).copy(alpha = 0.55f))
            .clickable { onDismiss() }
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x66151729))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    BasicText(
                        text = title,
                        style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                    )
                    BasicText(
                        text = subtitle,
                        style = typo.body.copy(fontSize = 13.sp, color = palette.muted),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(selectorBoxBg)
                        .clickable { onDismiss() },
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 18.dp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectorBoxBg)
                        .padding(horizontal = 6.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                val nextMonth = if (selectedMonth == 1) 12 else selectedMonth - 1
                                selectedMonth = nextMonth
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(selectedYear, nextMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 14.dp)
                    }
                    BasicText(
                        text = monthNames[selectedMonth - 1],
                        maxLines = 1,
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                val nextMonth = if (selectedMonth == 12) 1 else selectedMonth + 1
                                selectedMonth = nextMonth
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(selectedYear, nextMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.ink, size = 14.dp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectorBoxBg)
                        .padding(horizontal = 6.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                val nextYear = (selectedYear - 1).coerceAtLeast(minYear)
                                selectedYear = nextYear
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(nextYear, selectedMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 14.dp)
                    }
                    BasicText(
                        text = selectedYear.toString(),
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = activeChipColor),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                val nextYear = (selectedYear + 1).coerceAtMost(maxYear)
                                selectedYear = nextYear
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(nextYear, selectedMonth))
                            },
                    ) {
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.ink, size = 14.dp)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                quickYears.forEach { yearOption ->
                    val isSelectedYear = yearOption == selectedYear
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelectedYear) activeChipColor else selectorBoxBg)
                            .clickable {
                                selectedYear = yearOption
                                selectedDay = selectedDay.coerceAtMost(computeDaysInGregorianMonth(yearOption, selectedMonth))
                            }
                            .padding(horizontal = 10.dp),
                    ) {
                        BasicText(
                            text = yearOption.toString(),
                            style = typo.body.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelectedYear) Color.White else palette.muted,
                            ),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val weekHeaders = listOf("L", "M", "M", "J", "V", "S", "D")
                weekHeaders.forEach { dayHeader ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp),
                    ) {
                        BasicText(
                            text = dayHeader,
                            style = typo.body.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.muted),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val leadingEmptyCells = List<Int?>(leadingWeekdayOffset) { null }
                val monthDayCells = (1..daysInSelectedMonth).map { dayNumber -> dayNumber as Int? }
                val calendarCells = leadingEmptyCells + monthDayCells
                calendarCells.chunked(7).forEach { weekDays ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        for (slotIndex in 0 until 7) {
                            val dayValue = weekDays.getOrNull(slotIndex)
                            if (dayValue != null) {
                                val isDaySelected = dayValue == clampedSelectedDay
                                val dayBgBrush = if (isDaySelected) {
                                    primaryBrush
                                } else {
                                    SolidColor(dayCellBg)
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(dayBgBrush)
                                        .clickable {
                                            selectedDay = dayValue
                                        },
                                ) {
                                    BasicText(
                                        text = dayValue.toString(),
                                        style = typo.body.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDaySelected) Color.White else palette.ink,
                                        ),
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            val formattedDay = clampedSelectedDay.toString().padStart(2, '0')
            val formattedMonth = selectedMonth.toString().padStart(2, '0')
            val formattedPreview = "$formattedDay/$formattedMonth/$selectedYear"

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x99C846B4))
                    .clip(RoundedCornerShape(14.dp))
                    .background(primaryBrush)
                    .clickable {
                        onSelectDate(formattedPreview)
                    },
            ) {
                BasicText(
                    text = "Confirmar $formattedPreview",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }
    }
}

/**
 * Renders the user's circular avatar (photo or monogram) and displays a prominent
 * camera badge + interactive ring when [isEditing] is true.
 */
@Composable
internal fun RfEditableProfileAvatar(
    initials: String,
    avatarKey: String?,
    customImageBytes: ByteArray? = null,
    size: Dp = 84.dp,
    isEditing: Boolean = false,
    onClickEditPhoto: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val matchedPhotoKey = RfPhotoKey.entries.firstOrNull { entry -> entry.name == avatarKey }
    val isCustomDeviceAvatar = avatarKey?.startsWith("CUSTOM:") == true
    val customAvatarFileName = if (isCustomDeviceAvatar) {
        avatarKey.orEmpty().removePrefix("CUSTOM:").trim()
    } else {
        ""
    }
    val decodedCustomBitmap = rememberDecodedImageBitmap(
        bytes = if (isCustomDeviceAvatar) customImageBytes else null,
    )

    val innerBgColor = when (avatarKey) {
        "MONO_VIOLET" -> Color(0xFFEFE8FD)
        "MONO_GOLD" -> Color(0xFFFFF2C9)
        "MONO_TEAL" -> Color(0xFFDDF4F3)
        else -> Color(0xFFFDE6F2)
    }
    val innerTextColor = when (avatarKey) {
        "MONO_VIOLET" -> Color(0xFF7A4FD8)
        "MONO_GOLD" -> Color(0xFF7A5700)
        "MONO_TEAL" -> Color(0xFF1F8A84)
        else -> Color(0xFFC2338F)
    }
    val ringBrush = if (isEditing) {
        Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    } else {
        SolidColor(Color(0xFFF9B6D3))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .then(if (isEditing) Modifier.clickable { onClickEditPhoto() } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ringBrush)
                    .padding(3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(palette.surface)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (matchedPhotoKey != null) {
                        RfPhoto(
                            photoKey = matchedPhotoKey,
                            contentDescription = "Foto de perfil",
                            cornerRadius = size / 2,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else if (decodedCustomBitmap != null) {
                        RfBitmapPhoto(
                            bitmap = decodedCustomBitmap,
                            contentDescription = "Foto de perfil",
                            cornerRadius = size / 2,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else if (isCustomDeviceAvatar) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFDE6F2), Color(0xFFEAE6FD)),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 6.dp),
                            ) {
                                RfIcon(
                                    kind = RfIconKind.CAMERA,
                                    tint = Color(0xFFC2338F),
                                    size = (size.value * 0.24f).dp,
                                )
                                val badgeLabel = customAvatarFileName.ifBlank { initials }
                                BasicText(
                                    text = badgeLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (size.value * 0.14f).coerceAtLeast(9f).sp,
                                        color = Color(0xFF7A4FD8),
                                    ),
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(innerBgColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = initials,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (size.value * 0.33f).sp,
                                    color = innerTextColor,
                                ),
                            )
                        }
                    }
                }
            }

            if (isEditing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(30.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x80EA4EA9))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .border(2.dp, palette.surface, CircleShape)
                        .clickable { onClickEditPhoto() },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color.White, size = 15.dp)
                }
            }
        }

        if (isEditing) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (palette.isDark) Color(0xFF3A1F3D) else Color(0xFFFDE6F2))
                    .border(1.dp, Color(0xFFEA4EA9).copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                    .clickable { onClickEditPhoto() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFFC2338F), size = 12.dp)
                    BasicText(
                        text = "Cambiar foto",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (palette.isDark) Color(0xFFFF9BD6) else Color(0xFFC2338F),
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Modal dialog allowing the user to select a profile photo or monogram style in `s54`.
 */
@Composable
internal fun RfProfilePhotoPickerModal(
    initials: String,
    currentAvatarKey: String?,
    onSelectAvatarKey: (String?) -> Unit,
    onDismiss: () -> Unit,
    viewModel: RosaFiestaViewModel? = null,
    customImageBytes: ByteArray? = null,
    onPickDevicePhoto: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val coroutineScope = rememberCoroutineScope()
    val viewModelAvatarBytes = viewModel?.profileAvatarBytes?.collectAsState()?.value
    var previewKey by remember(currentAvatarKey) { mutableStateOf(currentAvatarKey) }
    var previewBytes by remember(customImageBytes, viewModelAvatarBytes) {
        mutableStateOf(customImageBytes ?: viewModelAvatarBytes)
    }
    val isCustomPreviewSelected = previewKey?.startsWith("CUSTOM:") == true
    val customPreviewFileName = if (isCustomPreviewSelected) {
        previewKey.orEmpty().removePrefix("CUSTOM:").trim()
    } else {
        ""
    }

    val photoOptions = remember {
        listOf(
            RfPhotoKey.BODA to "Gala Rosa",
            RfPhotoKey.QUINCEANERA to "Crystal",
            RfPhotoKey.CENTRO_FLORAL to "Floral",
            RfPhotoKey.ARCO_ROSA to "Arco Rosa",
            RfPhotoKey.BABY_SHOWER to "Pastel",
            RfPhotoKey.GRADUACION to "Dorado",
            RfPhotoKey.CUMPLE_INFANTIL to "Fiesta",
            RfPhotoKey.CORPORATIVO to "Ejecutivo",
        )
    }

    val monogramOptions = remember {
        listOf(
            null to ("Rosa clásico" to Color(0xFFFDE6F2)),
            "MONO_VIOLET" to ("Violeta" to Color(0xFFEFE8FD)),
            "MONO_GOLD" to ("Sol dorado" to Color(0xFFFFF2C9)),
            "MONO_TEAL" to ("Turquesa" to Color(0xFFDDF4F3)),
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151729).copy(alpha = 0.55f))
            .clickable { onDismiss() }
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x66151729))
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    BasicText(
                        text = "Foto de perfil",
                        style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                    )
                    BasicText(
                        text = "Elige una fotografía o estilo de avatar",
                        style = typo.body.copy(fontSize = 13.sp, color = palette.muted),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable { onDismiss() },
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 18.dp)
                }
            }

            RfEditableProfileAvatar(
                initials = initials,
                avatarKey = previewKey,
                customImageBytes = previewBytes,
                size = 88.dp,
                isEditing = false,
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isCustomPreviewSelected) Color(0xFFFDE6F2) else palette.chipBg)
                    .border(
                        width = if (isCustomPreviewSelected) 1.5.dp else 1.dp,
                        color = if (isCustomPreviewSelected) Color(0xFFEA4EA9) else palette.line,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable {
                        if (onPickDevicePhoto != null) {
                            onPickDevicePhoto()
                        } else if (viewModel != null) {
                            viewModel.pickProfileAvatarFromDevice { pickedFile ->
                                val customKey = "CUSTOM:${pickedFile.fileName}"
                                previewKey = customKey
                                previewBytes = pickedFile.bytes
                                onSelectAvatarKey(customKey)
                            }
                        } else {
                            coroutineScope.launch {
                                val platformActions = createPlatformActions()
                                val pickedFile = platformActions.pickMediaFile(listOf("jpg", "jpeg", "png"))
                                if (pickedFile != null) {
                                    val customKey = "CUSTOM:${pickedFile.fileName}"
                                    previewKey = customKey
                                    previewBytes = pickedFile.bytes
                                    onSelectAvatarKey(customKey)
                                }
                            }
                        }
                    }
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFFC2338F), size = 18.dp)
                    val uploadButtonText = if (customPreviewFileName.isNotEmpty()) {
                        "Subir foto desde dispositivo ($customPreviewFileName)"
                    } else {
                        "Subir foto desde dispositivo"
                    }
                    BasicText(
                        text = uploadButtonText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFFC2338F),
                        ),
                    )
                }
            }

            BasicText(
                text = "FOTOGRAFÍAS DE PERFIL",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.8.sp,
                    color = palette.muted,
                ),
            )

            photoOptions.chunked(4).forEach { rowPhotos ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowPhotos.forEach { (photoKey, label) ->
                        val isSelected = previewKey == photoKey.name
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFFDE6F2) else palette.chipBg)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFEA4EA9) else palette.line,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable { previewKey = photoKey.name }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RfPhoto(
                                photoKey = photoKey,
                                contentDescription = label,
                                cornerRadius = 22.dp,
                                modifier = Modifier.size(44.dp).clip(CircleShape),
                            )
                            BasicText(
                                text = label,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFFC2338F) else palette.ink,
                                ),
                            )
                        }
                    }
                }
            }

            BasicText(
                text = "INICIALES CON COLOR",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.8.sp,
                    color = palette.muted,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                monogramOptions.forEach { (monoKey, labelAndBg) ->
                    val (monoLabel, monoBg) = labelAndBg
                    val isSelected = previewKey == monoKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(monoBg)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFEA4EA9) else Color(0xFFE5DCEB),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { previewKey = monoKey },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = monoLabel,
                            maxLines = 1,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x99C846B4))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable {
                        onSelectAvatarKey(previewKey)
                    },
            ) {
                BasicText(
                    text = "Guardar foto de perfil",
                    style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }
    }
}

/**
 * Formats a date string (`DD/MM/YYYY` or `YYYY-MM-DD`) in natural Spanish prose (`15 de agosto de 1998`).
 */
internal fun formatSpanishDateWithWords(rawDate: String): String {
    val cleanDate = rawDate.trim()
    if (cleanDate.isEmpty()) return "Agregar"

    val parsedParts = parseDayMonthYearParts(cleanDate) ?: return cleanDate
    val day = parsedParts.first
    val month = parsedParts.second
    val year = parsedParts.third
    val isValidDay = day in 1..31
    val isValidMonth = month in 1..12
    val isValidYear = year in 1900..2100
    val isDayAndMonthValid = if (isValidDay) isValidMonth else false
    val isAllValid = if (isDayAndMonthValid) isValidYear else false
    if (!isAllValid) return cleanDate

    val monthName = spanishMonthNameForProfile(month)
    return "$day de $monthName de $year"
}

private fun parseDayMonthYearParts(cleanDate: String): Triple<Int, Int, Int>? {
    val slashParts = cleanDate.split("/")
    if (slashParts.size == 3) {
        val day = slashParts[0].trim().toIntOrNull()
        val month = slashParts[1].trim().toIntOrNull()
        val year = slashParts[2].trim().toIntOrNull()
        if (day != null) {
            if (month != null) {
                if (year != null) {
                    return Triple(day, month, year)
                }
            }
        }
    }
    val isoToken = cleanDate.substringBefore("T").trim()
    val dashParts = isoToken.split("-")
    if (dashParts.size == 3) {
        val year = dashParts[0].trim().toIntOrNull()
        val month = dashParts[1].trim().toIntOrNull()
        val day = dashParts[2].trim().toIntOrNull()
        if (day != null) {
            if (month != null) {
                if (year != null) {
                    return Triple(day, month, year)
                }
            }
        }
    }
    return null
}

private fun spanishMonthNameForProfile(monthNumber: Int): String = when (monthNumber) {
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
