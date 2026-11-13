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
                val rawText = updatedFieldValue.text
                val keepsRangeSeparator = rawText.contains(" - ")
                if (keepsRangeSeparator) {
                    onValueChange(rawText)
                } else if (value.contains(" - ")) {
                    onValueChange("")
                } else {
                    val formattedText = formatDateDigitsWithSlashes(
                        previousFormatted = value,
                        newRawText = rawText,
                    )
                    onValueChange(formattedText)
                }
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

private val spanishWeekdaysShort = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private val spanishMonthsShort = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

private val spanishMonthsFull = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

private data class CalendarDateParts(
    val day: Int,
    val month: Int,
    val year: Int,
) : Comparable<CalendarDateParts> {
    override fun compareTo(other: CalendarDateParts): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun formatSlash(): String {
        val dayText = day.toString().padStart(2, '0')
        val monthText = month.toString().padStart(2, '0')
        return "$dayText/$monthText/$year"
    }

    fun formatShortLetters(): String {
        val monthShort = spanishMonthsShort[month - 1]
        return "$day $monthShort $year"
    }

    fun formatReadableSpanish(): String {
        val firstDayOffset = computeGregorianMondayFirstOffset(year, month)
        val weekdayIndex = (firstDayOffset + (day - 1)) % 7
        val weekdayLabel = spanishWeekdaysShort[weekdayIndex]
        val monthFull = spanishMonthsFull[month - 1]
        return "$weekdayLabel, $day de $monthFull $year"
    }
}

private fun formatReadableSpanishDateRange(
    start: CalendarDateParts,
    end: CalendarDateParts,
): String {
    if (start == end) {
        return start.formatReadableSpanish()
    }
    val sameYear = start.year == end.year
    val sameMonth = start.month == end.month
    val sameMonthAndYear = if (sameYear) sameMonth else false
    if (sameMonthAndYear) {
        val monthFull = spanishMonthsFull[start.month - 1]
        return "Del ${start.day} al ${end.day} de $monthFull ${start.year}"
    }
    if (sameYear) {
        val startMonthShort = spanishMonthsShort[start.month - 1]
        val endMonthShort = spanishMonthsShort[end.month - 1]
        return "${start.day} $startMonthShort – ${end.day} $endMonthShort ${start.year}"
    }
    return "${start.formatShortLetters()} – ${end.formatShortLetters()}"
}

private fun matchSpanishMonthIndex(word: String): Int? {
    val clean = word.lowercase().trimEnd('.', ',')
    val fullIndex = spanishMonthsFull.indexOf(clean)
    if (fullIndex >= 0) {
        return fullIndex + 1
    }
    val shortIndex = spanishMonthsShort.indexOf(clean)
    if (shortIndex >= 0) {
        return shortIndex + 1
    }
    return null
}

private fun buildSafeDateParts(
    rawDay: Int,
    rawMonth: Int,
    rawYear: Int,
    minYear: Int,
    maxYear: Int,
): CalendarDateParts {
    val safeYear = rawYear.coerceIn(minYear, maxYear)
    val safeMonth = rawMonth.coerceIn(1, 12)
    val maxDayInMonth = computeDaysInGregorianMonth(safeYear, safeMonth)
    val safeDay = rawDay.coerceIn(1, maxDayInMonth)
    return CalendarDateParts(day = safeDay, month = safeMonth, year = safeYear)
}

private fun parseSpanishTextDateRange(
    dateText: String,
    minYear: Int,
    maxYear: Int,
    defaultYear: Int,
): Pair<CalendarDateParts, CalendarDateParts?> {
    val normalized = dateText.replace("–", " ").replace("-", " ")
    val rawTokens = normalized.split(" ").filter { token -> token.isNotBlank() }
    val numbers = mutableListOf<Int>()
    val months = mutableListOf<Int>()
    var hasSeenDayNumber = false

    for (token in rawTokens) {
        val cleanToken = token.trim(',', '.')
        val numericValue = cleanToken.toIntOrNull()
        if (numericValue != null) {
            numbers.add(numericValue)
            hasSeenDayNumber = true
        } else if (hasSeenDayNumber) {
            val matchedMonth = matchSpanishMonthIndex(cleanToken)
            if (matchedMonth != null) {
                months.add(matchedMonth)
            }
        }
    }

    val startMonth = months.getOrNull(0) ?: 10
    val firstNum = numbers.getOrNull(0) ?: 22
    val secondNum = numbers.getOrNull(1)
    val thirdNum = numbers.getOrNull(2)
    val fourthNum = numbers.getOrNull(3)

    val hasSeparateSecondDay = if (secondNum != null) secondNum <= 31 else false
    if (hasSeparateSecondDay) {
        val startYear = if (fourthNum != null) {
            secondNum ?: defaultYear
        } else {
            thirdNum ?: defaultYear
        }
        val endDay = if (fourthNum != null) {
            thirdNum ?: firstNum
        } else {
            secondNum ?: firstNum
        }
        val endMonth = months.getOrNull(1) ?: startMonth
        val endYear = fourthNum ?: startYear
        val startParts = buildSafeDateParts(firstNum, startMonth, startYear, minYear, maxYear)
        val endParts = buildSafeDateParts(endDay, endMonth, endYear, minYear, maxYear)
        return startParts to endParts
    }

    val singleYear = secondNum ?: defaultYear
    val singleParts = buildSafeDateParts(firstNum, startMonth, singleYear, minYear, maxYear)
    return singleParts to null
}

private fun parseCalendarDateOrDefault(
    dateText: String,
    minYear: Int,
    maxYear: Int,
    defaultYear: Int,
    defaultMonth: Int = 10,
    defaultDay: Int = 22,
): CalendarDateParts {
    val parts = dateText.trim().split("/")
    val parsedDay = parts.getOrNull(0)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(1, 31) ?: defaultDay
    val parsedMonth = parts.getOrNull(1)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(1, 12) ?: defaultMonth
    val parsedYear = parts.getOrNull(2)?.filter { charItem -> charItem.isDigit() }?.toIntOrNull()?.coerceIn(minYear, maxYear) ?: defaultYear
    return buildSafeDateParts(parsedDay, parsedMonth, parsedYear, minYear, maxYear)
}

private fun parseCalendarRangeOrDefault(
    dateText: String,
    minYear: Int,
    maxYear: Int,
    defaultYear: Int,
): Triple<CalendarDateParts, CalendarDateParts, Boolean> {
    val trimmed = dateText.trim()
    if (trimmed.contains("/")) {
        val dateSegments = trimmed.split(" - ")
        val startSegment = dateSegments.getOrNull(0).orEmpty()
        val endSegment = dateSegments.getOrNull(1).orEmpty()
        val startParts = parseCalendarDateOrDefault(startSegment, minYear, maxYear, defaultYear)
        val hasRangeSegment = endSegment.isNotBlank()
        val endParts = if (hasRangeSegment) {
            parseCalendarDateOrDefault(endSegment, minYear, maxYear, startParts.year, startParts.month, startParts.day)
        } else {
            startParts
        }
        return Triple(startParts, endParts, hasRangeSegment)
    }
    val hasLetterMonth = trimmed.any { character -> character.isLetter() }
    if (hasLetterMonth) {
        val (startParts, maybeEndParts) = parseSpanishTextDateRange(trimmed, minYear, maxYear, defaultYear)
        val resolvedEnd = maybeEndParts ?: startParts
        return Triple(startParts, resolvedEnd, maybeEndParts != null)
    }
    val fallback = buildSafeDateParts(22, 10, defaultYear, minYear, maxYear)
    return Triple(fallback, fallback, false)
}

internal fun formatReadableSpanishDateText(rawDateText: String): String {
    val trimmed = rawDateText.trim()
    if (trimmed.isEmpty()) return ""
    val isSlashFormat = trimmed.contains("/")
    if (!isSlashFormat) {
        return trimmed
    }
    val dateSegments = trimmed.split(" - ")
    val startSegment = dateSegments.getOrNull(0).orEmpty()
    val endSegment = dateSegments.getOrNull(1).orEmpty()
    val startParts = parseCalendarDateOrDefault(startSegment, 1925, 2035, 2026)
    if (endSegment.isBlank()) {
        return startParts.formatReadableSpanish()
    }
    val endParts = parseCalendarDateOrDefault(endSegment, 1925, 2035, startParts.year, startParts.month, startParts.day)
    return formatReadableSpanishDateRange(startParts, endParts)
}

/**
 * Interactive calendar modal for selecting any single date or optional date range across the app.
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
    allowRangeSelection: Boolean = false,
    preferReadableFormat: Boolean = false,
    isAdmin: Boolean = false,
    onSelectDate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val (initialStart, initialEnd, parsedHasRange) = parseCalendarRangeOrDefault(
        dateText = currentDateText,
        minYear = minYear,
        maxYear = maxYear,
        defaultYear = defaultYear,
    )
    val hasInitialRange = if (allowRangeSelection) parsedHasRange else false

    var isRangeMode by remember(currentDateText, allowRangeSelection) { mutableStateOf(hasInitialRange) }
    var editingEndDate by remember(currentDateText, allowRangeSelection) { mutableStateOf(false) }
    var startDate by remember(currentDateText) { mutableStateOf(initialStart) }
    var endDate by remember(currentDateText) { mutableStateOf(initialEnd) }
    var visibleMonth by remember(currentDateText) { mutableStateOf(initialStart.month) }
    var visibleYear by remember(currentDateText) { mutableStateOf(initialStart.year) }

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

    val daysInSelectedMonth = computeDaysInGregorianMonth(visibleYear, visibleMonth)
    val leadingWeekdayOffset = computeGregorianMondayFirstOffset(visibleYear, visibleMonth)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

            if (allowRangeSelection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectorBoxBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val isSingleActive = !isRangeMode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSingleActive) palette.surface else Color.Transparent)
                            .clickable {
                                isRangeMode = false
                                editingEndDate = false
                            },
                    ) {
                        BasicText(
                            text = "Fecha única",
                            style = typo.body.copy(
                                fontSize = 12.5.sp,
                                fontWeight = if (isSingleActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSingleActive) palette.ink else palette.muted,
                            ),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isRangeMode) palette.surface else Color.Transparent)
                            .clickable {
                                isRangeMode = true
                                editingEndDate = true
                                visibleMonth = endDate.month
                                visibleYear = endDate.year
                            },
                    ) {
                        BasicText(
                            text = "Rango de fechas",
                            style = typo.body.copy(
                                fontSize = 12.5.sp,
                                fontWeight = if (isRangeMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRangeMode) activeChipColor else palette.muted,
                            ),
                        )
                    }
                }

                if (isRangeMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val isStartFocused = !editingEndDate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isStartFocused) activeChipColor.copy(alpha = 0.12f) else selectorBoxBg)
                                .border(
                                    1.dp,
                                    if (isStartFocused) activeChipColor else palette.line,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    editingEndDate = false
                                    visibleMonth = startDate.month
                                    visibleYear = startDate.year
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Column {
                                BasicText(
                                    text = "Inicio",
                                    style = typo.body.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.muted),
                                )
                                BasicText(
                                    text = if (preferReadableFormat) startDate.formatShortLetters() else startDate.formatSlash(),
                                    style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (editingEndDate) activeChipColor.copy(alpha = 0.12f) else selectorBoxBg)
                                .border(
                                    1.dp,
                                    if (editingEndDate) activeChipColor else palette.line,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    editingEndDate = true
                                    visibleMonth = endDate.month
                                    visibleYear = endDate.year
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Column {
                                BasicText(
                                    text = "Fin",
                                    style = typo.body.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.muted),
                                )
                                BasicText(
                                    text = if (preferReadableFormat) endDate.formatShortLetters() else endDate.formatSlash(),
                                    style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                                )
                            }
                        }
                    }
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
                                val nextMonth = if (visibleMonth == 1) 12 else visibleMonth - 1
                                visibleMonth = nextMonth
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 14.dp)
                    }
                    BasicText(
                        text = monthNames[visibleMonth - 1],
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
                                val nextMonth = if (visibleMonth == 12) 1 else visibleMonth + 1
                                visibleMonth = nextMonth
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
                                visibleYear = (visibleYear - 1).coerceAtLeast(minYear)
                            },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 14.dp)
                    }
                    BasicText(
                        text = visibleYear.toString(),
                        style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = activeChipColor),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .clickable {
                                visibleYear = (visibleYear + 1).coerceAtMost(maxYear)
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
                    val isSelectedYear = yearOption == visibleYear
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelectedYear) activeChipColor else selectorBoxBg)
                            .clickable {
                                visibleYear = yearOption
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
                                val cellDate = CalendarDateParts(dayValue, visibleMonth, visibleYear)
                                val isStartMatch = cellDate == startDate
                                val isEndMatch = if (isRangeMode) cellDate == endDate else false
                                val isEndpointSelected = if (isStartMatch) true else isEndMatch
                                val isBetweenEndpoints = if (isRangeMode) {
                                    val isAfterStart = cellDate > startDate
                                    val isBeforeEnd = cellDate < endDate
                                    if (isAfterStart) isBeforeEnd else false
                                } else {
                                    false
                                }
                                val dayBgBrush = when {
                                    isEndpointSelected -> primaryBrush
                                    isBetweenEndpoints -> SolidColor(activeChipColor.copy(alpha = 0.16f))
                                    else -> SolidColor(dayCellBg)
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(dayBgBrush)
                                        .clickable {
                                            if (!isRangeMode) {
                                                startDate = cellDate
                                                endDate = cellDate
                                            } else if (!editingEndDate) {
                                                startDate = cellDate
                                                if (endDate < cellDate) {
                                                    endDate = cellDate
                                                }
                                                editingEndDate = true
                                            } else if (cellDate < startDate) {
                                                startDate = cellDate
                                            } else {
                                                endDate = cellDate
                                            }
                                        },
                                ) {
                                    BasicText(
                                        text = dayValue.toString(),
                                        style = typo.body.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isEndpointSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isEndpointSelected) Color.White else palette.ink,
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

            val startPreview = startDate.formatSlash()
            val endPreview = endDate.formatSlash()
            val slashPreview = if (isRangeMode) "$startPreview - $endPreview" else startPreview
            val readablePreview = if (isRangeMode) {
                formatReadableSpanishDateRange(startDate, endDate)
            } else {
                startDate.formatReadableSpanish()
            }
            val formattedPreview = if (preferReadableFormat) readablePreview else slashPreview

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
                    }
                    .padding(horizontal = 12.dp),
            ) {
                BasicText(
                    text = "Confirmar $formattedPreview",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typo.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
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
