package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.ReviewRecord
import kotlin.math.roundToInt

private fun mergeCompanyAndDomainReviews(
    domainReviews: List<ReviewRecord>,
    uiReviews: List<CompanyReviewUiModel>,
): List<CompanyReviewUiModel> {
    val avatarPalettes = listOf(
        0xFFFDE6F2L to 0xFFC2338FL,
        0xFFEFE8FDL to 0xFF7A4FD8L,
        0xFFDDF4F3L to 0xFF1F8A84L,
        0xFFEEF6D6L to 0xFF4E6A10L,
    )
    val mappedDomain = domainReviews.mapIndexed { index, record ->
        val avatarColors = avatarPalettes[index % avatarPalettes.size]
        val authorTrimmed = record.authorName.trim().ifBlank { "Cliente" }
        val initialLetter = authorTrimmed.take(1).uppercase()
        CompanyReviewUiModel(
            id = record.id,
            author = authorTrimmed,
            initial = initialLetter,
            dateLabel = record.dateLabel,
            rating = record.rating.coerceIn(1, 5),
            comment = record.comment,
            isVerified = record.verified,
            avatarBgHex = avatarColors.first,
            avatarInkHex = avatarColors.second,
        )
    }
    val seenIds = mappedDomain.map { reviewItem -> reviewItem.id }.toMutableSet()
    val seenComments = mappedDomain.map { reviewItem -> reviewItem.comment.trim().lowercase() }.toMutableSet()
    val additionalUi = uiReviews.filter { uiItem ->
        val hasNewId = uiItem.id !in seenIds
        val normalizedComment = uiItem.comment.trim().lowercase()
        val hasNewComment = normalizedComment !in seenComments
        val shouldInclude = if (hasNewId) hasNewComment else false
        if (shouldInclude) {
            seenIds.add(uiItem.id)
            seenComments.add(normalizedComment)
        }
        shouldInclude
    }
    return mappedDomain + additionalUi
}

private fun computeAverageRatingValue(reviews: List<CompanyReviewUiModel>): Double {
    if (reviews.isEmpty()) return 5.0
    val sumStars = reviews.sumOf { reviewItem -> reviewItem.rating.coerceIn(1, 5) }
    return sumStars.toDouble() / reviews.size.toDouble()
}

private fun formatAverageRatingTenths(averageValue: Double): String {
    val tenths = (averageValue * 10.0).roundToInt().coerceIn(10, 50)
    val wholePart = tenths / 10
    val decimalPart = tenths % 10
    return "$wholePart.$decimalPart"
}

private fun photoAssetToRfPhotoKey(photoAsset: PhotoAsset): RfPhotoKey = when (photoAsset) {
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
 * Renders the company profile, legal RNC, live rating/review metrics, and native contact actions (`s16`).
 */
@Composable
internal fun RenderAboutScreenS16(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val settings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val domainReviews = viewModel?.reviews?.collectAsState()?.value ?: CanonicalSampleData.reviews
    val uiReviews = viewModel?.companyReviews?.collectAsState()?.value.orEmpty()
    val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val feedbackText = viewModel?.formFeedback?.collectAsState()?.value

    val mergedReviews = remember(domainReviews, uiReviews) {
        mergeCompanyAndDomainReviews(domainReviews, uiReviews)
    }
    val avgRatingText = remember(mergedReviews, uiReviews) {
        if (mergedReviews.isNotEmpty()) {
            formatAverageRatingTenths(computeAverageRatingValue(mergedReviews))
        } else {
            viewModel?.companyAverageRatingFormatted() ?: "5.0"
        }
    }
    val instagramHandle = remember(settings.instagramUrl) {
        settings.instagramUrl
            .trimEnd('/')
            .substringAfterLast("/")
            .removePrefix("@")
            .ifBlank { "rosafiesta.do" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .drawBehind {
                if (!palette.isDark) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                            center = Offset(0f, 0f),
                            radius = size.width * 1.1f,
                        ),
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                            center = Offset(size.width, size.height * 0.15f),
                            radius = size.width * 0.8f,
                        ),
                    )
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Acerca de",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp,
                        color = palette.ink,
                    ),
                )
            }

            if (!feedbackText.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEF6D6))
                        .border(1.dp, Color(0xFFCDE298), RoundedCornerShape(14.dp))
                        .clickable { viewModel.clearFormFeedback() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 16.dp)
                    BasicText(
                        text = feedbackText,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3F560C)),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, Color(0x24EA4EA9), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfLogo(customSize = 84.dp, insetPadding = false)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val nameAnnotated = buildAnnotatedString {
                        append("Rosa ")
                        withStyle(SpanStyle(color = Color(0xFFD63F98))) {
                            append("Fiesta")
                        }
                    }
                    BasicText(
                        text = nameAnnotated,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.56).sp,
                            lineHeight = 28.sp,
                            color = palette.ink,
                        ),
                    )
                    BasicText(
                        text = settings.businessName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typo.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                    )
                    BasicText(
                        text = "RNC ${settings.rnc} · ${settings.businessHours}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typo.body.copy(fontSize = 12.5.sp, color = palette.muted),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .clickable { onSelectScreenCode("s17") }
                    .padding(vertical = 16.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BasicText(
                            text = "${mergedReviews.size}",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = (-0.52).sp,
                                color = palette.ink,
                            ),
                        )
                        BasicText("reseñas", style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(palette.line),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BasicText(
                            text = "${eventsList.size}",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = (-0.52).sp,
                                color = palette.ink,
                            ),
                        )
                        BasicText("eventos activos", style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(palette.line),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(
                                text = avgRatingText,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.52).sp,
                                    color = palette.ink,
                                ),
                            )
                            CustomSvgIcon(
                                pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                size = 18.dp,
                                tint = Color(0xFFF7B500),
                                filled = true,
                            )
                        }
                        BasicText("calificación", style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .padding(vertical = 6.dp),
            ) {
                Column {
                    BasicText(
                        text = "CONTACTO",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = palette.muted,
                        ),
                        modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 4.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel?.dialPhoneNumber(settings.phone) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M5 4h3.5l1.5 4.5-2.2 1.3a11 11 0 0 0 6.4 6.4l1.3-2.2L20 15.5V19a1.5 1.5 0 0 1-1.6 1.5C10.6 20 4 13.4 3.5 5.6A1.5 1.5 0 0 1 5 4z",
                                size = 20.dp,
                                tint = Color(0xFFC2338F),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Teléfono", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                            BasicText(
                                text = settings.phone,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66865DEC), ambientColor = Color.Transparent)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                .clickable { viewModel?.dialPhoneNumber(settings.phone) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Llamar", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.openWhatsAppChat(
                                    rawPhone = settings.whatsapp,
                                    message = "Hola RosaFiesta, quisiera cotizar un evento",
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M4 20l1.3-4A8.5 8.5 0 1 1 8 18.8zM9 8.5c0 3.2 3.2 6.5 6.5 6.5l1-1.6-2-1.1-1 .8c-1-.4-2.1-1.5-2.5-2.5l.8-1-1.1-2z",
                                size = 20.dp,
                                tint = Color(0xFF4E6A10),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("WhatsApp", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                            BasicText(
                                text = "Escríbele a María · ${settings.whatsapp}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                            )
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = palette.muted,
                        )
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.openExternalUrl("https://instagram.com/$instagramHandle")
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFE8FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M3 8.5A1.5 1.5 0 0 1 4.5 7H7l1.5-2.5h7L17 7h2.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0",
                                size = 20.dp,
                                tint = Color(0xFF7A4FD8),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Instagram", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                            BasicText(
                                text = "@$instagramHandle · ${settings.email}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                            )
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = palette.muted,
                        )
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.openExternalUrl("https://facebook.com/rosafiestard")
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CustomSvgIcon(
                                pathData = "M5 8a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0M2 20c.6-3.2 3.3-5.5 6.5-5.5S14.4 16.8 15 20M15.5 4.6a3.5 3.5 0 0 1 0 6.8M18 14.8c2 .7 3.5 2.6 4 5.2",
                                size = 20.dp,
                                tint = Color(0xFF1F8A84),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Facebook", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                            BasicText("Únete a la comunidad", style = typo.body.copy(fontSize = 14.sp, color = palette.muted))
                        }
                        CustomSvgIcon(
                            pathData = "M9 18l6-6-6-6",
                            size = 20.dp,
                            tint = palette.muted,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp)),
            ) {
                Column {
                    MapIllustrationS16(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { viewModel?.openMapsSearch(settings.address) },
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            BasicText(
                                text = settings.address,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                            )
                            BasicText(
                                text = "RNC ${settings.rnc}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 12.5.sp, color = palette.muted),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(palette.surface)
                                .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                                .clickable { viewModel?.openMapsSearch(settings.address) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Cómo llegar", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink))
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
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s17") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFF7B500), size = 16.dp, filled = true)
                        BasicText(
                            text = "Reseñas (${mergedReviews.size})",
                            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66865DEC), ambientColor = Color.Transparent)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { onSelectScreenCode("s42") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color.White, size = 16.dp)
                        BasicText(
                            text = "Hablar en la app",
                            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MapIllustrationS16(modifier: Modifier = Modifier) {
    val land1 = remember { PathParser().parsePathString("M0 110C40 96 70 120 110 104S170 70 210 84 270 120 300 100V160H0Z").toPath() }
    val park1 = remember { PathParser().parsePathString("M180 10C210 0 250 14 256 40S230 76 204 70 168 40 180 10Z").toPath() }
    val park2 = remember { PathParser().parsePathString("M20 30C40 26 60 40 54 56S24 66 16 52 8 34 20 30Z").toPath() }
    val roadMain = remember { PathParser().parsePathString("M-10 128C60 112 120 140 190 120S280 96 310 104").toPath() }
    val pinPath = remember { PathParser().parsePathString("M150 95s-17-14-17-27a17 17 0 0 1 34 0c0 13-17 27-17 27z").toPath() }

    Canvas(modifier = modifier) {
        val scaleFactor = size.width / 300f
        drawRect(Color(0xFFEEF6FB))

        scale(scaleFactor, scaleFactor, Offset.Zero) {
            drawPath(land1, Color(0xFFDFF0FB))
            drawPath(park1, Color(0xFFD7F2E6))
            drawPath(park2, Color(0xFFD7F2E6))
            drawPath(roadMain, Color(0xFFBFE1F5), style = Stroke(width = 12f))

            drawLine(Color.White, Offset(0f, 74f), Offset(300f, 74f), strokeWidth = 8f)
            drawLine(Color.White, Offset(110f, 0f), Offset(110f, 160f), strokeWidth = 8f)
            drawLine(Color.White, Offset(228f, 0f), Offset(196f, 160f), strokeWidth = 8f)
            drawLine(Color.White, Offset(0f, 20f), Offset(300f, 60f), strokeWidth = 8f)

            drawLine(Color.White, Offset(60f, 0f), Offset(60f, 160f), strokeWidth = 3.5f)
            drawLine(Color.White, Offset(0f, 140f), Offset(300f, 140f), strokeWidth = 3.5f)
            drawLine(Color.White, Offset(160f, 0f), Offset(170f, 160f), strokeWidth = 3.5f)

            drawOval(Color(0x262B1730), topLeft = Offset(138f, 92.5f), size = Size(24f, 7f))

            drawPath(pinPath, Color(0xFFD12F7C))
            drawCircle(Color.White, radius = 6.5f, center = Offset(150f, 68f))
        }
    }
}

/**
 * Renders the public company reviews feed (`s17`) with live verified filtering and validated submission.
 */
@Composable
internal fun RenderCompanyReviewsS17(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var selectedFilter by remember { mutableStateOf(0) }
    var showWriteReviewModal by remember { mutableStateOf(false) }
    var draftStars by remember { mutableStateOf(5) }
    var draftComment by remember { mutableStateOf("") }
    var localReviewError by remember { mutableStateOf<String?>(null) }

    val domainReviews = viewModel?.reviews?.collectAsState()?.value ?: CanonicalSampleData.reviews
    val uiReviews = viewModel?.companyReviews?.collectAsState()?.value.orEmpty()
    val reviews = remember(domainReviews, uiReviews) {
        mergeCompanyAndDomainReviews(domainReviews, uiReviews)
    }
    val averageRatingValue = remember(reviews) {
        computeAverageRatingValue(reviews)
    }
    val avgRatingText = remember(reviews, uiReviews) {
        if (reviews.isNotEmpty()) {
            formatAverageRatingTenths(averageRatingValue)
        } else {
            viewModel?.companyAverageRatingFormatted() ?: "5.0"
        }
    }
    val filledSummaryStars = remember(averageRatingValue) {
        averageRatingValue.roundToInt().coerceIn(1, 5)
    }
    val verifiedCount = remember(reviews) { reviews.count { reviewItem -> reviewItem.isVerified } }
    val filteredReviews = remember(reviews, selectedFilter) {
        if (selectedFilter == 1) {
            reviews.filter { reviewItem -> reviewItem.isVerified }
        } else {
            reviews
        }
    }
    val totalCount = reviews.size.coerceAtLeast(1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .drawBehind {
                if (!palette.isDark) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                            center = Offset(0f, 0f),
                            radius = size.width * 1.1f,
                        ),
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                            center = Offset(size.width, size.height * 0.15f),
                            radius = size.width * 0.8f,
                        ),
                    )
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s16") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Reseñas",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp,
                        color = palette.ink,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = avgRatingText,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 48.sp,
                                letterSpacing = (-1).sp,
                                lineHeight = 44.sp,
                                color = palette.ink,
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (starNumber in 1..5) {
                                val isStarFilled = starNumber <= filledSummaryStars
                                CustomSvgIcon(
                                    pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                    size = 16.dp,
                                    tint = if (isStarFilled) Color(0xFFF7B500) else palette.muted,
                                    filled = isStarFilled,
                                )
                            }
                        }
                        BasicText(
                            text = "${reviews.size} reseñas",
                            style = typo.body.copy(fontSize = 13.sp, color = palette.muted),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (starLevel in 5 downTo 1) {
                            val starCount = reviews.count { reviewItem -> reviewItem.rating == starLevel }
                            val progress = starCount.toFloat() / totalCount.toFloat()
                            RatingBarRow(star = starLevel.toString(), progress = progress)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (selectedFilter == 0) {
                                Modifier.shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x73865DEC), ambientColor = Color.Transparent)
                            } else {
                                Modifier
                            },
                        )
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selectedFilter == 0) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                Brush.linearGradient(listOf(palette.surface, palette.surface))
                            },
                        )
                        .then(
                            if (selectedFilter != 0) Modifier.border(1.dp, palette.line, RoundedCornerShape(999.dp)) else Modifier,
                        )
                        .clickable { selectedFilter = 0 }
                        .padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Todas · ${reviews.size}",
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedFilter == 0) Color.White else palette.ink,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .then(
                            if (selectedFilter == 1) {
                                Modifier.shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x73865DEC), ambientColor = Color.Transparent)
                            } else {
                                Modifier
                            },
                        )
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selectedFilter == 1) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                Brush.linearGradient(listOf(palette.surface, palette.surface))
                            },
                        )
                        .then(
                            if (selectedFilter != 1) Modifier.border(1.dp, palette.line, RoundedCornerShape(999.dp)) else Modifier,
                        )
                        .clickable { selectedFilter = 1 }
                        .padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Verificadas · $verifiedCount",
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedFilter == 1) Color.White else palette.ink,
                        ),
                    )
                }
            }

            filteredReviews.forEach { reviewItem ->
                ReviewCardItem(
                    avatarBg = Color(reviewItem.avatarBgHex),
                    avatarTextColor = Color(reviewItem.avatarInkHex),
                    initial = reviewItem.initial,
                    author = reviewItem.author,
                    date = reviewItem.dateLabel,
                    comment = reviewItem.comment,
                    rating = reviewItem.rating,
                    isVerified = reviewItem.isVerified,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, palette.bg, palette.bg),
                    ),
                )
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp, top = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable {
                        localReviewError = null
                        showWriteReviewModal = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CustomSvgIcon(
                        pathData = "M4 20h4L19 9l-4-4L4 16zM13.5 6.5l4 4",
                        size = 20.dp,
                        tint = Color.White,
                    )
                    BasicText(
                        text = "Escribir una reseña",
                        style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        if (showWriteReviewModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88141024))
                    .clickable { showWriteReviewModal = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(palette.surface)
                        .clickable(enabled = false) {}
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 34.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "Califica tu experiencia",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(palette.chipBg)
                                .clickable { showWriteReviewModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 18.dp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (starValue in 1..5) {
                            val isStarSelected = starValue <= draftStars
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isStarSelected) Color(0xFFFFF2C9) else palette.chipBg)
                                    .clickable { draftStars = starValue },
                                contentAlignment = Alignment.Center,
                            ) {
                                CustomSvgIcon(
                                    pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                    size = 24.dp,
                                    tint = if (isStarSelected) Color(0xFFF7B500) else palette.muted,
                                    filled = isStarSelected,
                                )
                            }
                        }
                    }

                    val commentBorderColor = if (localReviewError != null) Color(0xFFB42345) else palette.line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.chipBg)
                            .border(1.dp, commentBorderColor, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        if (draftComment.isEmpty()) {
                            BasicText(
                                text = "Cuéntanos cómo quedó tu montaje o atención con Rosa Fiesta…",
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                            )
                        }
                        BasicTextField(
                            value = draftComment,
                            onValueChange = { updatedText ->
                                draftComment = updatedText
                                if (localReviewError != null) {
                                    localReviewError = null
                                }
                            },
                            textStyle = typo.body.copy(fontSize = 14.5.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    val validationMessage = localReviewError
                    if (validationMessage != null) {
                        BasicText(
                            text = validationMessage,
                            style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                            .clickable {
                                val trimmedComment = draftComment.trim()
                                val selectedRating = draftStars.coerceIn(1, 5)
                                if (trimmedComment.length < 6) {
                                    localReviewError = "Escribe al menos 6 caracteres para publicar tu reseña."
                                    viewModel?.postError("Escribe al menos 6 caracteres para publicar tu reseña.")
                                } else {
                                    val published = viewModel?.submitCompanyReview(
                                        comment = trimmedComment,
                                        rating = selectedRating,
                                    ) ?: true
                                    if (published) {
                                        draftComment = ""
                                        draftStars = 5
                                        localReviewError = null
                                        showWriteReviewModal = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Publicar reseña ($draftStars ★)",
                            style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(star: String, progress: Float) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicText(star, style = typo.body.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted), modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(RfThemeState.currentPalette.chipBg),
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF7B500)),
                )
            }
        }
    }
}

@Composable
private fun ReviewCardItem(
    avatarBg: Color,
    avatarTextColor: Color,
    initial: String,
    author: String,
    date: String,
    comment: String,
    rating: Int = 5,
    isVerified: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val clampedRating = rating.coerceIn(1, 5)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = initial,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = avatarTextColor,
                        ),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = author,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        )
                        if (isVerified) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFEEF6D6))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                BasicText(
                                    text = "Verificada",
                                    style = typo.body.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (starIndex in 1..5) {
                            val isFilled = starIndex <= clampedRating
                            CustomSvgIcon(
                                pathData = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
                                size = 13.dp,
                                tint = if (isFilled) Color(0xFFF7B500) else palette.muted,
                                filled = isFilled,
                            )
                        }
                    }
                }

                BasicText(date, style = typo.body.copy(fontSize = 13.sp, color = palette.muted))
            }

            BasicText(
                text = comment,
                style = typo.body.copy(fontSize = 15.sp, lineHeight = 22.sp, color = palette.ink),
            )
        }
    }
}

/**
 * Direct client chat screens (`s41` event tab chat and `s42` full-screen direct chat).
 */

@Composable
private fun CustomSvgIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = Color(0xFF1A1A2E),
    strokeWidth: Float = 1.8f,
    filled: Boolean = false,
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier = modifier.size(size)) {
        val scaleFactor = this.size.width / 24f
        scale(scaleFactor, scaleFactor, Offset.Zero) {
            if (filled) {
                drawPath(
                    path = path,
                    color = tint,
                )
            } else {
                drawPath(
                    path = path,
                    color = tint,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}

@Composable
private fun ChatDateBadge(
    label: String = "Ayer",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(RfThemeState.currentPalette.chipBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RfThemeState.currentPalette.muted,
            ),
        )
    }
}

@Composable
private fun ChatMessageBubble(
    text: String,
    time: String,
    attachmentName: String = "",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (attachmentName.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CustomSvgIcon(
                            pathData = "M20 11.5l-8.2 8.2a5 5 0 0 1-7-7L13 4.5a3.3 3.3 0 0 1 4.7 4.7l-8.2 8.2a1.7 1.7 0 0 1-2.4-2.4L14.5 7.5",
                            size = 13.dp,
                            tint = Color.White,
                            strokeWidth = 2f,
                        )
                        BasicText(
                            text = attachmentName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                    }
                }
                BasicText(
                    text = text,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = Color.White,
                    ),
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText(
                        text = time,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color(0xFFFFD6E8),
                        ),
                    )
                    CustomSvgIcon(
                        pathData = "M5 12.5l4.5 4.5L19 7.5",
                        size = 14.dp,
                        tint = Color(0xFFFFD6E8),
                        strokeWidth = 2.2f,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMariaIncomingBubble(
    author: String,
    text: String,
    time: String,
    attachmentName: String = "",
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        RfLogo(customSize = 28.dp, insetPadding = false)
        Box(
            modifier = Modifier
                .widthIn(max = 275.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                .background(palette.surface)
                .border(1.dp, palette.line, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(
                    text = author,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD63F98),
                    ),
                )
                if (attachmentName.isNotBlank()) {
                    BasicText(
                        text = "Adjunto: $attachmentName",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD63F98),
                        ),
                    )
                }
                BasicText(
                    text = text,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        color = palette.ink,
                    ),
                )
                BasicText(
                    text = time,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.sp,
                        color = palette.muted,
                    ),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun PickedChatAttachmentBar(
    fileName: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFDE6F2))
            .border(1.dp, Color(0xFFF5B7DA), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CustomSvgIcon(
            pathData = "M20 11.5l-8.2 8.2a5 5 0 0 1-7-7L13 4.5a3.3 3.3 0 0 1 4.7 4.7l-8.2 8.2a1.7 1.7 0 0 1-2.4-2.4L14.5 7.5",
            size = 16.dp,
            tint = Color(0xFFC2338F),
        )
        BasicText(
            text = fileName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = palette.ink,
            ),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .clickable { onClear() }
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            BasicText(
                text = "Quitar",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC2338F),
                ),
            )
        }
    }
}

@Composable
internal fun RenderEventChatTabS41(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var draftInputS41 by remember { mutableStateOf("") }
    var localPickedFileName by remember { mutableStateOf("") }
    val observedEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val observedSelectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val activeEvent: ClientEvent = remember(observedEvents, observedSelectedEventId, viewModel) {
        viewModel?.activeClientEvent() ?: observedEvents.first()
    }
    val observedPayments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val latestPaymentForEvent = remember(observedPayments, activeEvent.id) {
        observedPayments.firstOrNull { paymentItem -> paymentItem.eventId == activeEvent.id }
    }
    val pickedFile = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    val activeAttachmentName = pickedFile?.fileName ?: localPickedFileName
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value

    var localFallbackMessages by remember {
        mutableStateOf(
            listOf(
                DirectChatMessageUiModel(
                    id = "loc-1",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Hola! Soy María de Rosa Fiesta 🌸 Estoy conectada para ayudarte con la cotización, el mobiliario o cualquier detalle de tu evento.",
                    timeLabel = "7:35 p. m.",
                    isFromClient = false,
                ),
                DirectChatMessageUiModel(
                    id = "loc-2",
                    senderName = "Cliente",
                    text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                    timeLabel = "7:38 p. m.",
                    isFromClient = true,
                ),
                DirectChatMessageUiModel(
                    id = "loc-3",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Claro que sí! Tenemos disponibilidad inmediata y las acabo de reservar en tu lista ✨",
                    timeLabel = "7:40 p. m.",
                    isFromClient = false,
                ),
            ),
        )
    }
    val vmMessages = viewModel?.directChatMessages?.collectAsState()?.value
    val activeMessages = vmMessages ?: localFallbackMessages
    val scrollState = rememberScrollState()

    val sendChatPayload: (String, String) -> Unit = { rawText, attachmentFileName ->
        val trimmedText = rawText.trim()
        val trimmedAttachment = attachmentFileName.trim()
        val hasMessageText = trimmedText.isNotEmpty()
        val hasAttachmentFile = trimmedAttachment.isNotEmpty()
        val shouldDispatch = if (hasMessageText) true else hasAttachmentFile
        if (shouldDispatch) {
            if (viewModel != null) {
                viewModel.sendDirectChatMessage(trimmedText, trimmedAttachment)
                viewModel.clearPickedAttachment()
            } else {
                val resolvedText = if (hasMessageText) trimmedText else "Archivo adjunto: $trimmedAttachment"
                localFallbackMessages = localFallbackMessages + listOf(
                    DirectChatMessageUiModel(
                        id = "loc-${localFallbackMessages.size + 1}",
                        senderName = "Cliente",
                        text = resolvedText,
                        timeLabel = "Ahora",
                        isFromClient = true,
                        attachmentName = trimmedAttachment,
                    ),
                    DirectChatMessageUiModel(
                        id = "loc-${localFallbackMessages.size + 2}",
                        senderName = "María · Rosa Fiesta",
                        text = "¡Recibido, muchas gracias! Ya lo anoté en tu expediente de ${activeEvent.title} 🌸",
                        timeLabel = "Ahora",
                        isFromClient = false,
                    ),
                )
            }
            localPickedFileName = ""
        }
    }

    val submitMessageS41 = {
        val trimmedDraft = draftInputS41.trim()
        val hasTextToSubmit = trimmedDraft.isNotEmpty()
        val hasAttachmentToSubmit = activeAttachmentName.isNotEmpty()
        val canSubmitNow = if (hasTextToSubmit) true else hasAttachmentToSubmit
        if (canSubmitNow) {
            sendChatPayload(trimmedDraft, activeAttachmentName)
            draftInputS41 = ""
        }
    }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 164.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M19 12H5M11 18l-6-6 6-6",
                        size = 22.dp,
                        tint = palette.ink,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = activeEvent.title,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = "${activeEvent.dateLabel} · ${activeEvent.venue}",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = palette.muted,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            viewModel?.exportReceiptDocument(
                                event = activeEvent,
                                payment = latestPaymentForEvent,
                                shareInsteadOfSave = false,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M12 4v11M7 10.5l5 5 5-5M5 20h14",
                        size = 20.dp,
                        tint = palette.ink,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            val shareSummaryText = "${activeEvent.title} · ${activeEvent.dateLabel} · ${activeEvent.venue}"
                            viewModel?.sharePlainText(activeEvent.title, shareSummaryText)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M15 6a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M8.6 10.6l6.8-3.6M8.6 13.4l6.8 3.6",
                        size = 20.dp,
                        tint = palette.ink,
                    )
                }
            }

            if (!feedbackMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEF6D6))
                        .clickable { viewModel.clearFormFeedback() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = feedbackMessage,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3F560C),
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { onSelectScreenCode("s40") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Detalles",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.muted,
                            ),
                        )
                    }

                    Column(
                        modifier = Modifier.height(44.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Chat",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.ink,
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(Color(0xFFD63F98)),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(palette.line),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfLogo(
                    customSize = 36.dp,
                    insetPadding = false,
                )
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "María · Equipo Rosa Fiesta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink,
                        ),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8DB52C)),
                        )
                        BasicText(
                            text = "${activeEvent.guests} invitados · ${activeEvent.status.label}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ChatDateBadge("Conversación activa · ${activeEvent.code}")
                }

                activeMessages.forEach { messageItem ->
                    if (messageItem.isFromClient) {
                        ChatMessageBubble(
                            text = messageItem.text,
                            time = messageItem.timeLabel,
                            attachmentName = messageItem.attachmentName,
                        )
                    } else {
                        ChatMariaIncomingBubble(
                            author = messageItem.senderName,
                            text = messageItem.text,
                            time = messageItem.timeLabel,
                            attachmentName = messageItem.attachmentName,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line),
        ) {
            if (activeAttachmentName.isNotEmpty()) {
                PickedChatAttachmentBar(
                    fileName = activeAttachmentName,
                    onClear = {
                        viewModel?.clearPickedAttachment()
                        localPickedFileName = ""
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val quickPrompts = listOf(
                    "Cotizar sillas Tiffany",
                    "Consultar fecha",
                    "Formas de pago",
                    "Visitar el local",
                )
                quickPrompts.forEach { promptChip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.chipBg)
                            .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                            .clickable { sendChatPayload(promptChip, "") }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = promptChip,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD63F98),
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .clickable {
                            viewModel?.pickMediaAttachment(
                                allowedExtensions = listOf("jpg", "jpeg", "png", "pdf"),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M20 11.5l-8.2 8.2a5 5 0 0 1-7-7L13 4.5a3.3 3.3 0 0 1 4.7 4.7l-8.2 8.2a1.7 1.7 0 0 1-2.4-2.4L14.5 7.5",
                        size = 22.dp,
                        tint = if (activeAttachmentName.isNotEmpty()) Color(0xFFD63F98) else palette.muted,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draftInputS41.isEmpty()) {
                        BasicText(
                            text = "Escríbele a María…",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = palette.muted,
                            ),
                        )
                    }
                    BasicTextField(
                        value = draftInputS41,
                        onValueChange = { updatedText -> draftInputS41 = updatedText },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitMessageS41() }),
                        textStyle = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = palette.ink,
                        ),
                        cursorBrush = SolidColor(Color(0xFFD63F98)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { submitMessageS41() },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M21 3L10 14M21 3l-6.5 18-4.5-7-7-4.5z",
                        size = 20.dp,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderEventChatS42(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var draftInputS42 by remember { mutableStateOf("") }
    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showEmojiPickerRow by remember { mutableStateOf(false) }

    val observedEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val observedSelectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
    val activeEvent: ClientEvent = remember(observedEvents, observedSelectedEventId, viewModel) {
        viewModel?.activeClientEvent() ?: observedEvents.first()
    }
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value ?: CanonicalSampleData.businessSettings
    val pickedFile = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    val activeAttachmentName = pickedFile?.fileName.orEmpty()
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value

    var localFallbackMessages by remember {
        mutableStateOf(
            listOf(
                DirectChatMessageUiModel(
                    id = "loc42-1",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Hola! Soy María de Rosa Fiesta 🌸 Estoy conectada para ayudarte con la cotización, el mobiliario o cualquier detalle de tu evento.",
                    timeLabel = "7:35 p. m.",
                    isFromClient = false,
                ),
                DirectChatMessageUiModel(
                    id = "loc42-2",
                    senderName = "Cliente",
                    text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                    timeLabel = "7:38 p. m.",
                    isFromClient = true,
                ),
                DirectChatMessageUiModel(
                    id = "loc42-3",
                    senderName = "María · Rosa Fiesta",
                    text = "¡Claro que sí! Tenemos disponibilidad inmediata y las acabo de reservar en tu lista ✨",
                    timeLabel = "7:40 p. m.",
                    isFromClient = false,
                ),
            ),
        )
    }
    val vmMessages = viewModel?.directChatMessages?.collectAsState()?.value
    val activeMessages = vmMessages ?: localFallbackMessages
    val scrollState = rememberScrollState()

    val sendChatPayload: (String, String) -> Unit = { rawText, attachmentFileName ->
        val trimmedText = rawText.trim()
        val trimmedAttachment = attachmentFileName.trim()
        val hasMessageText = trimmedText.isNotEmpty()
        val hasAttachmentFile = trimmedAttachment.isNotEmpty()
        val shouldDispatch = if (hasMessageText) true else hasAttachmentFile
        if (shouldDispatch) {
            if (viewModel != null) {
                viewModel.sendDirectChatMessage(trimmedText, trimmedAttachment)
                viewModel.clearPickedAttachment()
            } else {
                val resolvedText = if (hasMessageText) trimmedText else "Archivo adjunto: $trimmedAttachment"
                localFallbackMessages = localFallbackMessages + listOf(
                    DirectChatMessageUiModel(
                        id = "loc42-${localFallbackMessages.size + 1}",
                        senderName = "Cliente",
                        text = resolvedText,
                        timeLabel = "Ahora",
                        isFromClient = true,
                        attachmentName = trimmedAttachment,
                    ),
                    DirectChatMessageUiModel(
                        id = "loc42-${localFallbackMessages.size + 2}",
                        senderName = "María · Rosa Fiesta",
                        text = "¡Recibido, muchas gracias! Ya lo anoté en tu expediente de ${activeEvent.title} 🌸",
                        timeLabel = "Ahora",
                        isFromClient = false,
                    ),
                )
            }
        }
    }

    val submitMessageS42 = {
        val trimmedDraft = draftInputS42.trim()
        val hasTextToSubmit = trimmedDraft.isNotEmpty()
        val hasAttachmentToSubmit = activeAttachmentName.isNotEmpty()
        val canSubmitNow = if (hasTextToSubmit) true else hasAttachmentToSubmit
        if (canSubmitNow) {
            sendChatPayload(trimmedDraft, activeAttachmentName)
            draftInputS42 = ""
            showEmojiPickerRow = false
        }
    }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val eventPhotoKey = remember(activeEvent.photo) { photoAssetToRfPhotoKey(activeEvent.photo) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 84.dp, bottom = 160.dp)
                .verticalScroll(scrollState)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(18.dp))
                    .clickable { onSelectScreenCode("s40") }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RfPhoto(
                    photoKey = eventPhotoKey,
                    contentDescription = activeEvent.title,
                    cornerRadius = 12.dp,
                    modifier = Modifier.size(48.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = activeEvent.title,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = "${activeEvent.dateLabel} · ${activeEvent.guests} invitados · ${activeEvent.venue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = palette.muted,
                        ),
                    )
                }

                BasicText(
                    text = "Ver",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFC2338F),
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }

            if (!feedbackMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEF6D6))
                        .clickable { viewModel.clearFormFeedback() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = feedbackMessage,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3F560C),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                ChatDateBadge("Chat directo · ${activeEvent.code}")
            }

            activeMessages.forEach { messageItem ->
                if (messageItem.isFromClient) {
                    ChatMessageBubble(
                        text = messageItem.text,
                        time = messageItem.timeLabel,
                        attachmentName = messageItem.attachmentName,
                    )
                } else {
                    ChatMariaIncomingBubble(
                        author = messageItem.senderName,
                        text = messageItem.text,
                        time = messageItem.timeLabel,
                        attachmentName = messageItem.attachmentName,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line)
                .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onSelectScreenCode("BACK:s37") },
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M19 12H5M11 18l-6-6 6-6",
                    size = 22.dp,
                    tint = palette.ink,
                )
            }

            RfLogo(
                customSize = 42.dp,
                insetPadding = false,
            )

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "María · ${businessSettings.businessName}",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8DB52C)),
                    )
                    BasicText(
                        text = "En línea · ${activeEvent.title}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF4E6A10),
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { showMoreMenuSheet = !showMoreMenuSheet },
                contentAlignment = Alignment.Center,
            ) {
                CustomSvgIcon(
                    pathData = "M12 5h.01M12 12h.01M12 19h.01",
                    size = 22.dp,
                    tint = palette.ink,
                    strokeWidth = 2.6f,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(palette.surface)
                .border(1.dp, palette.line),
        ) {
            if (activeAttachmentName.isNotEmpty()) {
                PickedChatAttachmentBar(
                    fileName = activeAttachmentName,
                    onClear = { viewModel?.clearPickedAttachment() },
                )
            }

            if (showEmojiPickerRow) {
                val quickEmojis = listOf("✨", "🎉", "🌸", "💐", "🥂", "❤️")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.chipBg)
                        .border(1.dp, palette.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    quickEmojis.forEach { emojiOption ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(palette.surface)
                                .clickable {
                                    val prefixSeparator = if (draftInputS42.isEmpty()) "" else " "
                                    draftInputS42 = "$draftInputS42$prefixSeparator$emojiOption"
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = emojiOption,
                                style = TextStyle(fontSize = 19.sp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val quickPrompts = listOf(
                    "Cotizar sillas Tiffany",
                    "Consultar fecha",
                    "Formas de pago",
                    "Visitar el local",
                )
                quickPrompts.forEach { promptChip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.chipBg)
                            .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                            .clickable { sendChatPayload(promptChip, "") }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = promptChip,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD63F98),
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(if (showEmojiPickerRow) Color(0xFFFDE6F2) else palette.chipBg)
                        .clickable { showEmojiPickerRow = !showEmojiPickerRow },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M8.5 14.5c1.8 2 5.2 2 7 0M9 9.5h.01M15 9.5h.01",
                        size = 22.dp,
                        tint = if (showEmojiPickerRow) Color(0xFFD63F98) else palette.muted,
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(palette.chipBg)
                        .padding(start = 16.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (draftInputS42.isEmpty()) {
                            BasicText(
                                text = "Escríbele a María…",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    color = palette.muted,
                                ),
                            )
                        }
                        BasicTextField(
                            value = draftInputS42,
                            onValueChange = { updatedText -> draftInputS42 = updatedText },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitMessageS42() }),
                            textStyle = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = palette.ink,
                            ),
                            cursorBrush = SolidColor(Color(0xFFD63F98)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                viewModel?.pickMediaAttachment(
                                    allowedExtensions = listOf("jpg", "jpeg", "png", "pdf"),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        CustomSvgIcon(
                            pathData = "M3 8.5A1.5 1.5 0 0 1 4.5 7H7l1.5-2.5h7L17 7h2.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0",
                            size = 20.dp,
                            tint = if (activeAttachmentName.isNotEmpty()) Color(0xFFD63F98) else palette.muted,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                        .clickable { submitMessageS42() },
                    contentAlignment = Alignment.Center,
                ) {
                    CustomSvgIcon(
                        pathData = "M21 3L10 14M21 3l-6.5 18-4.5-7-7-4.5z",
                        size = 20.dp,
                        tint = Color.White,
                    )
                }
            }
        }

        if (showMoreMenuSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66151729))
                    .clickable { showMoreMenuSheet = false },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(24.dp))
                        .clickable { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        text = "Opciones · ${activeEvent.title}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = palette.ink,
                        ),
                    )
                    ChatActionSheetRow(
                        label = "Ver detalle del evento",
                        subtitle = "${activeEvent.dateLabel} · ${activeEvent.venue}",
                        onClick = {
                            showMoreMenuSheet = false
                            onSelectScreenCode("s40")
                        },
                    )
                    ChatActionSheetRow(
                        label = "Compartir resumen",
                        subtitle = "Enviar resumen de cotización y fecha",
                        onClick = {
                            showMoreMenuSheet = false
                            val summaryPayload = "${activeEvent.title} · ${activeEvent.dateLabel} · ${activeEvent.venue}"
                            viewModel?.sharePlainText(activeEvent.title, summaryPayload)
                        },
                    )
                    ChatActionSheetRow(
                        label = "Llamar a RosaFiesta",
                        subtitle = businessSettings.phone,
                        onClick = {
                            showMoreMenuSheet = false
                            viewModel?.dialPhoneNumber(businessSettings.phone)
                        },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.chipBg)
                            .clickable { showMoreMenuSheet = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cerrar",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.ink,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatActionSheetRow(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.chipBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = palette.ink,
            ),
        )
        BasicText(
            text = subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 12.5.sp,
                color = palette.muted,
            ),
        )
    }
}

/**
 * Administrative financial overview dashboard (`s59`).
 */
@Composable
internal fun RenderFinanceDashboardS59(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFinanceTab by remember { mutableStateOf(0) }
    val paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val suppliersList = viewModel?.suppliers?.collectAsState()?.value ?: CanonicalSampleData.suppliers
    val metricsSnapshot = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics

    val totalCollectedDop = remember(paymentsList) {
        paymentsList.sumOf { paymentItem -> paymentItem.amountDop }
    }
    val formattedCollected = remember(totalCollectedDop) {
        RosaFiestaEngine.formatDop(totalCollectedDop)
    }
    val formattedReceivable = remember(metricsSnapshot.accountsReceivableDop) {
        RosaFiestaEngine.formatDop(metricsSnapshot.accountsReceivableDop)
    }

    AdminPanelScaffold(activeCode = "s59", onSelectScreenCode = onSelectScreenCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEDEDF5))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PanelSegTab59("Movimientos", selected = selectedFinanceTab == 0, onClick = { selectedFinanceTab = 0 }, modifier = Modifier.weight(1f))
            PanelSegTab59("Facturas", selected = selectedFinanceTab == 1, onClick = { selectedFinanceTab = 1 }, modifier = Modifier.weight(1f))
            PanelSegTab59("Proveedores", selected = selectedFinanceTab == 2, onClick = { selectedFinanceTab = 2 }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                BasicText("Entradas (${paymentsList.size})", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF4E6A10)))
                BasicText(formattedCollected, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, letterSpacing = (-0.44).sp, color = Color(0xFF3F560C)))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                BasicText("Por cobrar (${quotesList.size})", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF5F6377)))
                BasicText(formattedReceivable, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, letterSpacing = (-0.44).sp, color = Color(0xFF151729)))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            when (selectedFinanceTab) {
                0 -> {
                    paymentsList.forEachIndexed { paymentIndex, paymentItem ->
                        val receiptStatus = if (paymentItem.verified) {
                            "${paymentItem.receiptNumber} · Verificado"
                        } else {
                            "${paymentItem.receiptNumber} · En revisión"
                        }
                        val formattedAmount = RosaFiestaEngine.formatDop(paymentItem.amountDop)
                        PanelMovementRow59(
                            title = "${paymentItem.kindLabel} · ${paymentItem.eventTitle}",
                            subtitle = "${paymentItem.methodLabel} · ${paymentItem.dateLabel} · $receiptStatus",
                            amount = "+$formattedAmount",
                            icon = RfIconKind.TREND_UP,
                            iconBackground = Color(0xFFEEF6D6),
                            iconTint = Color(0xFF4E6A10),
                            amountColor = Color(0xFF4E6A10),
                            divider = paymentIndex > 0,
                            onClick = {
                                viewModel?.selectEvent(paymentItem.eventId)
                                onSelectScreenCode("s83")
                            },
                        )
                    }
                }

                1 -> {
                    quotesList.forEachIndexed { quoteIndex, quoteItem ->
                        val formattedQuoteTotal = RosaFiestaEngine.formatDop(quoteItem.totalDop)
                        PanelMovementRow59(
                            title = "${quoteItem.clientName} · ${quoteItem.eventTitle}",
                            subtitle = "${quoteItem.code} · ${quoteItem.status} · ${quoteItem.dateLabel}",
                            amount = formattedQuoteTotal,
                            icon = RfIconKind.NOTE,
                            iconBackground = Color(0xFFEEE8FD),
                            iconTint = Color(0xFF5D3FD3),
                            amountColor = Color(0xFF151729),
                            divider = quoteIndex > 0,
                            onClick = {
                                viewModel?.selectQuote(quoteItem.id)
                                onSelectScreenCode("A03")
                            },
                        )
                    }
                }

                else -> {
                    suppliersList.forEachIndexed { supplierIndex, supplierItem ->
                        PanelMovementRow59(
                            title = supplierItem.name,
                            subtitle = "${supplierItem.category} · ${supplierItem.phone}",
                            amount = supplierItem.statusLabel,
                            icon = RfIconKind.TRUCK,
                            iconBackground = Color(0xFFFFF2C9),
                            iconTint = Color(0xFF7A5700),
                            amountColor = Color(0xFF865DEC),
                            divider = supplierIndex > 0,
                            onClick = { onSelectScreenCode("s55") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelSegTab59(label: String, selected: Boolean, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val typo = LocalRfTypography.current

    Box(
        modifier = modifier
            .height(38.dp)
            .then(
                if (selected) {
                    Modifier.shadow(6.dp, RoundedCornerShape(11.dp), spotColor = Color(0x334650B4))
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color(0xFF151729) else Color(0xFF5F6377),
            ),
        )
    }
}

@Composable
private fun PanelMovementRow59(
    title: String,
    subtitle: String,
    amount: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    amountColor: Color,
    divider: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            BasicText(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
            )
        }
        BasicText(
            amount,
            maxLines = 1,
            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = amountColor),
        )
    }
}

/**
 * Administrative analytics, warehouse availability calendar, commercial leads, and exportable reports (`s63`–`s70`).
 */

private fun spanishMonthName(monthOneBased: Int): String = when (monthOneBased) {
    1 -> "Enero"
    2 -> "Febrero"
    3 -> "Marzo"
    4 -> "Abril"
    5 -> "Mayo"
    6 -> "Junio"
    7 -> "Julio"
    8 -> "Agosto"
    9 -> "Septiembre"
    10 -> "Octubre"
    11 -> "Noviembre"
    else -> "Diciembre"
}

private fun normalizeLeadStageBucket(rawStage: String): String {
    val upperStage = rawStage.trim().uppercase()
    if (upperStage.contains("NUEV")) return "NUEVO"
    if (upperStage.contains("GANAD")) return "GANADO"
    if (upperStage.contains("APROBAD")) return "GANADO"
    if (upperStage.contains("CERRAD")) return "GANADO"
    if (upperStage.contains("PERDID")) return "PERDIDO"
    if (upperStage.contains("DESCART")) return "PERDIDO"
    if (upperStage.contains("COTIZ")) return "COTIZADO"
    if (upperStage.contains("PROPUEST")) return "COTIZADO"
    if (upperStage.contains("CONTACT")) return "CONTACTADO"
    if (upperStage.contains("NEGOCI")) return "CONTACTADO"
    if (upperStage.contains("CALIFIC")) return "CONTACTADO"
    return "CONTACTADO"
}

private fun nextLeadStageValue(rawStage: String): String = when (normalizeLeadStageBucket(rawStage)) {
    "NUEVO" -> "CONTACTADO"
    "CONTACTADO" -> "COTIZADO"
    "COTIZADO" -> "GANADO"
    else -> "GANADO"
}

private val ReportPeriodOptions = listOf(
    "Ene–Jun 2026" to "2026-06",
    "Jul–Dic 2026" to "2026-10",
    "Anual 2026" to "2026-12",
)

private fun nextReportPeriodIndex(currentIndex: Int): Int = (currentIndex + 1) % ReportPeriodOptions.size

private val StockHighColor63 = Color(0xFF8DB52C)
private val StockMediumColor63 = Color(0xFFF7B500)
private val StockSoldOutColor63 = Color(0xFFD9385A)

@Composable
internal fun RenderInventoryCalendarS63(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val vmSelectedArticleId = viewModel?.selectedArticleId?.collectAsState()?.value
    var localSelectedArticleId by remember { mutableStateOf(articlesList.first().id) }
    val resolvedArticleId = vmSelectedArticleId ?: localSelectedArticleId
    val activeArticle: CatalogArticle = remember(articlesList, resolvedArticleId) {
        articlesList.firstOrNull { articleItem -> articleItem.id == resolvedArticleId } ?: articlesList.first()
    }

    var calendarYear by remember { mutableStateOf(2026) }
    var calendarMonthOneBased by remember { mutableStateOf(10) }
    var selectedDay63 by remember { mutableStateOf(20) }

    val daysInMonth = remember(calendarYear, calendarMonthOneBased) {
        computeDaysInGregorianMonth(calendarYear, calendarMonthOneBased)
    }
    val mondayFirstOffset = remember(calendarYear, calendarMonthOneBased) {
        computeGregorianMondayFirstOffset(calendarYear, calendarMonthOneBased)
    }
    val paddedMonth = remember(calendarMonthOneBased) {
        calendarMonthOneBased.toString().padStart(2, '0')
    }
    val monthIso = "$calendarYear-$paddedMonth"
    val currentMonthLabel = "${spanishMonthName(calendarMonthOneBased)} $calendarYear"

    var availability by remember { mutableStateOf<ArticleAvailabilityCalendar?>(null) }
    LaunchedEffect(activeArticle.id, monthIso) {
        availability = viewModel?.queryWarehouseAvailability(activeArticle.id, monthIso)
    }

    val clampedSelectedDay = selectedDay63.coerceIn(1, daysInMonth)
    val resolvedTotalStock = (availability?.totalStock ?: activeArticle.totalStock).coerceAtLeast(1)
    val halfStockThreshold = (resolvedTotalStock + 1) / 2

    val dayRecordsByNumber = remember(availability, activeArticle, calendarYear, calendarMonthOneBased, daysInMonth, resolvedTotalStock) {
        val mapped = mutableMapOf<Int, Pair<Int, Int>>()
        val existingDays = availability?.days.orEmpty()
        for (dayNumber in 1..daysInMonth) {
            val paddedDay = dayNumber.toString().padStart(2, '0')
            val targetIso = "$monthIso-$paddedDay"
            val matchedDay = existingDays.firstOrNull { dayEntry -> dayEntry.dateIso == targetIso }
            if (matchedDay != null) {
                mapped[dayNumber] = matchedDay.availableUnits to matchedDay.reservedUnits
            } else {
                val simulatedReserved = when {
                    dayNumber == 23 -> (resolvedTotalStock - activeArticle.availableOct23).coerceAtLeast(0)
                    dayNumber % 9 == 0 -> (resolvedTotalStock * 6) / 10
                    dayNumber % 6 == 0 -> (resolvedTotalStock * 3) / 10
                    else -> 0
                }
                val simulatedAvailable = (resolvedTotalStock - simulatedReserved).coerceAtLeast(0)
                mapped[dayNumber] = simulatedAvailable to simulatedReserved
            }
        }
        mapped
    }

    val calendarCells = remember(mondayFirstOffset, daysInMonth, dayRecordsByNumber, halfStockThreshold) {
        val cellsList = mutableListOf<Triple<Int, Color, Int>?>()
        for (offsetIndex in 0 until mondayFirstOffset) {
            cellsList.add(null)
        }
        for (dayNumber in 1..daysInMonth) {
            val unitsPair = dayRecordsByNumber[dayNumber] ?: (resolvedTotalStock to 0)
            val availableUnits = unitsPair.first
            val trafficColor = when {
                availableUnits <= 0 -> StockSoldOutColor63
                availableUnits < halfStockThreshold -> StockMediumColor63
                else -> StockHighColor63
            }
            cellsList.add(Triple(dayNumber, trafficColor, availableUnits))
        }
        cellsList
    }

    val selectedUnitsPair = dayRecordsByNumber[clampedSelectedDay] ?: (resolvedTotalStock to 0)
    val selectedAvailableUnits = selectedUnitsPair.first
    val selectedReservedUnits = selectedUnitsPair.second
    val selectedTrafficStatus = when {
        selectedAvailableUnits <= 0 -> "Agotado"
        selectedAvailableUnits < halfStockThreshold -> "Stock medio / alerta"
        else -> "Disponibilidad alta"
    }
    val detailBannerBg = when {
        selectedAvailableUnits <= 0 -> Color(0xFFFDE3EA)
        selectedAvailableUnits < halfStockThreshold -> Color(0xFFFFF2C9)
        else -> Color(0xFFEEF6D6)
    }
    val detailBannerTint = when {
        selectedAvailableUnits <= 0 -> Color(0xFFB42345)
        selectedAvailableUnits < halfStockThreshold -> Color(0xFF7A5700)
        else -> Color(0xFF4E6A10)
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        "Disponibilidad",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                    )
                    BasicText(
                        "${activeArticle.code} · Stock total: $resolvedTotalStock uds",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                articlesList.forEach { articleItem ->
                    val isSelectedChip = articleItem.id == activeArticle.id
                    LeadChip64(
                        label = articleItem.name,
                        selected = isSelectedChip,
                        onClick = {
                            localSelectedArticleId = articleItem.id
                            viewModel?.selectArticle(articleItem.id)
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                        .padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).clickable {
                                if (calendarMonthOneBased == 1) {
                                    calendarYear -= 1
                                    calendarMonthOneBased = 12
                                } else {
                                    calendarMonthOneBased -= 1
                                }
                                val updatedMaxDay = computeDaysInGregorianMonth(calendarYear, calendarMonthOneBased)
                                selectedDay63 = selectedDay63.coerceIn(1, updatedMaxDay)
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHEVRON_LEFT, tint = Color(0xFF151729), size = 20.dp)
                        }
                        BasicText(
                            currentMonthLabel,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, textAlign = TextAlign.Center, color = Color(0xFF151729)),
                        )
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).clickable {
                                if (calendarMonthOneBased == 12) {
                                    calendarYear += 1
                                    calendarMonthOneBased = 1
                                } else {
                                    calendarMonthOneBased += 1
                                }
                                val updatedMaxDay = computeDaysInGregorianMonth(calendarYear, calendarMonthOneBased)
                                selectedDay63 = selectedDay63.coerceIn(1, updatedMaxDay)
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF151729), size = 20.dp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { dayHeader ->
                            BasicText(
                                dayHeader,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color(0xFF5F6377)),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        calendarCells.chunked(7).forEach { weekCells ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                weekCells.forEach { dayCell ->
                                    if (dayCell == null) {
                                        Spacer(Modifier.weight(1f))
                                    } else {
                                        CalendarDay63(
                                            day = dayCell.first,
                                            status = dayCell.second,
                                            availableUnits = dayCell.third,
                                            modifier = Modifier.weight(1f),
                                            selected = dayCell.first == clampedSelectedDay,
                                            onClick = { selectedDay63 = dayCell.first },
                                        )
                                    }
                                }
                                val trailingSlots = 7 - weekCells.size
                                for (slotIndex in 0 until trailingSlots) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarLegend63("Disponible", StockHighColor63)
                CalendarLegend63("< 50% stock", StockMediumColor63)
                CalendarLegend63("Agotado (0)", StockSoldOutColor63)
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(detailBannerBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RfIcon(kind = RfIconKind.BOX, tint = detailBannerTint, size = 24.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    BasicText(
                        text = "${activeArticle.name} · Día $clampedSelectedDay ($selectedTrafficStatus)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Disponibles: $selectedAvailableUnits de $resolvedTotalStock uds · Reservadas: $selectedReservedUnits uds",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = detailBannerTint),
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CalendarDay63(
    day: Int,
    status: Color,
    availableUnits: Int,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFECEBFF) else RfThemeState.currentPalette.bg)
            .then(if (selected) Modifier.border(2.dp, Color(0xFFD63F98), RoundedCornerShape(12.dp)) else Modifier)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            day.toString(),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)),
        )
        BasicText(
            availableUnits.toString(),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, color = Color(0xFF5F6377)),
        )
        Spacer(Modifier.height(2.dp))
        Box(modifier = Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(status))
    }
}

@Composable
private fun CalendarLegend63(label: String, color: Color) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF151729)))
    }
}

@Composable
internal fun RenderLeadsPipelineS64(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var selectedStageFilter by remember { mutableStateOf("TODOS") }
    var showCreateLeadModal by remember { mutableStateOf(false) }

    val filteredLeads = remember(leadsList, selectedStageFilter) {
        when (selectedStageFilter) {
            "NUEVO" -> leadsList.filter { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "NUEVO" }
            "CONTACTADO" -> leadsList.filter { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "CONTACTADO" }
            "COTIZADO" -> leadsList.filter { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "COTIZADO" }
            "GANADO" -> leadsList.filter { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "GANADO" }
            else -> leadsList
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LeadsScaffold(
            activeCode = "s64",
            onSelectScreenCode = onSelectScreenCode,
            onRefresh = {
                viewModel?.postFeedback("Pipeline sincronizado (${leadsList.size} prospectos · ${quotesList.size} cotizaciones).")
                viewModel?.syncWithServer()
            },
            onOpenCreateLead = { showCreateLeadModal = true },
            feedbackBanner = feedbackMessage,
            onDismissFeedback = { viewModel?.clearFormFeedback() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val filterEntries = listOf(
                    "TODOS" to "Todos · ${leadsList.size}",
                    "NUEVO" to "Nuevos",
                    "CONTACTADO" to "Contactados",
                    "COTIZADO" to "Cotizados",
                    "GANADO" to "Ganados",
                )
                filterEntries.forEach { filterPair ->
                    LeadChip64(
                        label = filterPair.second,
                        selected = selectedStageFilter == filterPair.first,
                        onClick = { selectedStageFilter = filterPair.first },
                    )
                }
            }
            Spacer(Modifier.height(2.dp))

            if (filteredLeads.isEmpty()) {
                ReportCard(contentPadding = PaddingValues(16.dp)) {
                    BasicText(
                        "No hay prospectos en esta etapa todavía. Usa el botón + para registrar uno nuevo.",
                        style = TextStyle(fontFamily = LocalRfTypography.current.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                filteredLeads.forEach { leadItem ->
                    val normalizedStage = normalizeLeadStageBucket(leadItem.stage)
                    val statusBadgeBg = when (normalizedStage) {
                        "NUEVO" -> Color(0xFFDDF4F3)
                        "COTIZADO" -> Color(0xFFEFE8FD)
                        "GANADO" -> Color(0xFFEEF6D6)
                        else -> Color(0xFFFFF2C9)
                    }
                    val statusBadgeFg = when (normalizedStage) {
                        "NUEVO" -> Color(0xFF1F8A84)
                        "COTIZADO" -> Color(0xFF7A4FD8)
                        "GANADO" -> Color(0xFF4E6A10)
                        else -> Color(0xFF7A5700)
                    }
                    val formattedBudget = RosaFiestaEngine.formatDop(leadItem.estimatedDop)
                    val nextStage = nextLeadStageValue(leadItem.stage)
                    LeadPipelineCard64(
                        name = leadItem.clientName,
                        status = leadItem.stage,
                        statusBackground = statusBadgeBg,
                        statusColor = statusBadgeFg,
                        event = "${leadItem.eventType} · ${leadItem.guests} inv · ${leadItem.dateLabel}",
                        budget = "$formattedBudget · ${leadItem.channel}",
                        priority = if (normalizedStage == "GANADO") "Cerrado" else "Avanzar a $nextStage",
                        priorityColor = if (normalizedStage == "GANADO") Color(0xFF4E6A10) else Color(0xFF4B47D6),
                        priorityDot = if (normalizedStage == "GANADO") Color(0xFF8DB52C) else Color(0xFF5B6CF0),
                        onClick = {
                            viewModel?.selectLead(leadItem.id)
                            onSelectScreenCode("A03")
                        },
                        onAdvanceStage = {
                            viewModel?.selectLead(leadItem.id)
                            viewModel?.updateCommercialLead(
                                leadId = leadItem.id,
                                stage = nextStage,
                                followUpNote = leadItem.followUpNote,
                            )
                            viewModel?.postFeedback("${leadItem.clientName} actualizado a etapa $nextStage.")
                        },
                    )
                }
            }
        }

        if (showCreateLeadModal) {
            NewCommercialLeadModal(
                onDismiss = { showCreateLeadModal = false },
                onSaveLead = { clientName, phone, eventType, guests, budgetDop, followUpNote ->
                    viewModel?.createCommercialLead(
                        clientName = clientName,
                        eventType = eventType,
                        dateLabel = "Por confirmar",
                        guests = guests,
                        estimatedDop = budgetDop,
                        channel = "WhatsApp",
                        followUpNote = followUpNote,
                        phone = phone,
                    )
                    showCreateLeadModal = false
                },
            )
        }
    }
}

@Composable
private fun LeadsScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenCreateLead: () -> Unit,
    feedbackBanner: String? = null,
    onDismissFeedback: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Leads",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                ReportIconButton(RfIconKind.REFRESH) { onRefresh() }
                BotAddButton72 { onOpenCreateLead() }
            }
            if (!feedbackBanner.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEF6D6))
                        .clickable { onDismissFeedback() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    BasicText(
                        text = feedbackBanner,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf("Pipeline" to "s64", "Seguimientos" to "s65", "Estadísticas" to "s66"),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun NewCommercialLeadModal(
    onDismiss: () -> Unit,
    onSaveLead: (clientName: String, phone: String, eventType: String, guests: Int, budgetDop: Int, followUpNote: String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var clientNameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var eventTypeInput by remember { mutableStateOf("Boda") }
    var guestsInput by remember { mutableStateOf("80") }
    var budgetDopInput by remember { mutableStateOf("4500") }
    var followUpNoteInput by remember { mutableStateOf("") }
    var localErrorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77151729))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(24.dp))
                .clickable { }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicText(
                "Nuevo prospecto / seguimiento",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF151729)),
            )
            AdminModalLabeledField(label = "Nombre del cliente", value = clientNameInput, placeholder = "Ej. Lucía Fernández") { updatedValue ->
                clientNameInput = updatedValue
                localErrorText = null
            }
            AdminModalLabeledField(label = "Teléfono / WhatsApp", value = phoneInput, placeholder = "+18095550199") { updatedValue ->
                phoneInput = updatedValue
                localErrorText = null
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1.2f)) {
                    AdminModalLabeledField(label = "Tipo de evento", value = eventTypeInput, placeholder = "Boda / Corporativo") { updatedValue ->
                        eventTypeInput = updatedValue
                    }
                }
                Box(modifier = Modifier.weight(0.8f)) {
                    AdminModalLabeledField(label = "Invitados", value = guestsInput, placeholder = "80") { updatedValue ->
                        guestsInput = updatedValue
                        localErrorText = null
                    }
                }
            }
            AdminModalLabeledField(label = "Presupuesto estimado (RD$)", value = budgetDopInput, placeholder = "4500") { updatedValue ->
                budgetDopInput = updatedValue
                localErrorText = null
            }
            AdminModalLabeledField(label = "Nota de seguimiento", value = followUpNoteInput, placeholder = "Enviar catálogo y confirmar visita") { updatedValue ->
                followUpNoteInput = updatedValue
            }

            if (localErrorText != null) {
                BasicText(
                    text = localErrorText.orEmpty(),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFFB42345)),
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEDEDF5))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .clickable {
                            val trimmedClient = clientNameInput.trim()
                            val trimmedPhone = phoneInput.trim()
                            val trimmedEvent = eventTypeInput.trim().ifBlank { "Evento social" }
                            val parsedGuests = guestsInput.trim().toIntOrNull() ?: 0
                            val parsedBudget = budgetDopInput.trim().toIntOrNull() ?: 0
                            val trimmedFollowUp = followUpNoteInput.trim()
                            when {
                                trimmedClient.length < 2 -> {
                                    localErrorText = "Ingresa el nombre completo del prospecto."
                                }

                                trimmedPhone.length < 7 -> {
                                    localErrorText = "Ingresa un teléfono válido de al menos 7 dígitos."
                                }

                                parsedGuests < 10 -> {
                                    localErrorText = "Indica al menos 10 invitados estimados."
                                }

                                parsedBudget < 300 -> {
                                    localErrorText = "El presupuesto estimado mínimo es RD$ 300."
                                }

                                else -> {
                                    onSaveLead(trimmedClient, trimmedPhone, trimmedEvent, parsedGuests, parsedBudget, trimmedFollowUp)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable
internal fun AdminModalLabeledField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF5F6377)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF6F6FB))
                .border(1.dp, Color(0xFFE3E3EF), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                BasicText(
                    text = placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF8C90A4)),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LeadChip64(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .height(38.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
    }
}

@Composable
private fun LeadPipelineCard64(
    name: String,
    status: String,
    statusBackground: Color,
    statusColor: Color,
    event: String,
    budget: String,
    priority: String,
    priorityColor: Color,
    priorityDot: Color,
    onClick: () -> Unit = {},
    onAdvanceStage: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(
                name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF151729)),
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusBackground)
                    .padding(horizontal = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF5F6377), size = 14.dp)
            BasicText(event, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(
                budget,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            val stageClickModifier = if (onAdvanceStage != null) {
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF3F3FA))
                    .clickable { onAdvanceStage() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            } else {
                Modifier
            }
            Row(
                modifier = stageClickModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(priorityDot))
                BasicText(priority, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = priorityColor))
            }
        }
    }
}

@Composable
internal fun RenderLeadsFollowupS65(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var showCreateLeadModal by remember { mutableStateOf(false) }

    val pendingLeadsCount = remember(leadsList) {
        leadsList.count { leadItem -> normalizeLeadStageBucket(leadItem.stage) != "GANADO" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LeadsScaffold(
            activeCode = "s65",
            onSelectScreenCode = onSelectScreenCode,
            onRefresh = {
                viewModel?.postFeedback("Seguimientos sincronizados ($pendingLeadsCount pendientes · ${quotesList.size} cotizaciones).")
                viewModel?.syncWithServer()
            },
            onOpenCreateLead = { showCreateLeadModal = true },
            feedbackBanner = feedbackMessage,
            onDismissFeedback = { viewModel?.clearFormFeedback() },
        ) {
            BasicText(
                buildAnnotatedString {
                    append("Pendientes ")
                    withStyle(SpanStyle(color = Color(0xFF5F6377), fontWeight = FontWeight.Medium)) { append("· $pendingLeadsCount") }
                },
                modifier = Modifier.padding(top = 6.dp),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )

            leadsList.forEach { leadItem ->
                val isCompletedLead = normalizeLeadStageBucket(leadItem.stage) == "GANADO"
                val taskDescription = leadItem.followUpNote.ifBlank {
                    "Dar seguimiento a cotización de ${leadItem.eventType} (${leadItem.guests} invitados)."
                }
                LeadFollowupCard65(
                    leadItem = leadItem,
                    task = "${leadItem.clientName} · $taskDescription",
                    due = "${leadItem.stage} · ${leadItem.dateLabel}",
                    isCompleted = isCompletedLead,
                    onToggleFollowUp = {
                        viewModel?.selectLead(leadItem.id)
                        val targetStage = if (isCompletedLead) "CONTACTADO" else nextLeadStageValue(leadItem.stage)
                        val updatedNote = if (isCompletedLead) {
                            leadItem.followUpNote
                        } else {
                            "${leadItem.followUpNote.ifBlank { "Seguimiento comercial" }} · Actualizado"
                        }
                        viewModel?.updateCommercialLead(
                            leadId = leadItem.id,
                            stage = targetStage,
                            followUpNote = updatedNote,
                        )
                        viewModel?.postFeedback("Seguimiento de ${leadItem.clientName} pasado a $targetStage.")
                    },
                    onOpenQuote = {
                        viewModel?.selectLead(leadItem.id)
                        onSelectScreenCode("A03")
                    },
                )
            }
        }

        if (showCreateLeadModal) {
            NewCommercialLeadModal(
                onDismiss = { showCreateLeadModal = false },
                onSaveLead = { clientName, phone, eventType, guests, budgetDop, followUpNote ->
                    viewModel?.createCommercialLead(
                        clientName = clientName,
                        eventType = eventType,
                        dateLabel = "Por confirmar",
                        guests = guests,
                        estimatedDop = budgetDop,
                        channel = "WhatsApp",
                        followUpNote = followUpNote,
                        phone = phone,
                    )
                    showCreateLeadModal = false
                },
            )
        }
    }
}

@Composable
private fun LeadFollowupCard65(
    leadItem: LeadRecord,
    task: String,
    due: String,
    isCompleted: Boolean,
    onToggleFollowUp: () -> Unit,
    onOpenQuote: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val formattedLeadDop = remember(leadItem.estimatedDop) {
        RosaFiestaEngine.formatDop(leadItem.estimatedDop)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .clickable { onToggleFollowUp() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF8DB52C) else Color.Transparent)
                .border(2.dp, if (isCompleted) Color(0xFF8DB52C) else Color(0xFFCDBFCB), CircleShape)
                .clickable { onToggleFollowUp() },
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicText(
                task,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isCompleted) Color(0xFFEEF6D6) else Color(0xFFFDE3EA))
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    RfIcon(kind = if (isCompleted) RfIconKind.CHECK else RfIconKind.WARNING, tint = if (isCompleted) Color(0xFF4E6A10) else Color(0xFFB42345), size = 13.dp)
                    BasicText(
                        if (isCompleted) "Ganado · $formattedLeadDop" else due,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (isCompleted) Color(0xFF4E6A10) else Color(0xFFB42345)),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEE8FD))
                        .clickable { onOpenQuote() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        "Cotizar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF5D3FD3)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderLeadsStatsS66(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value

    val totalLeadsCount = leadsList.size.coerceAtLeast(1)
    val nuevosCount = leadsList.count { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "NUEVO" }
    val contactadosCount = leadsList.count { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "CONTACTADO" }
    val cotizadosLeadsCount = leadsList.count { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "COTIZADO" }
    val explicitWonCount = leadsList.count { leadItem -> normalizeLeadStageBucket(leadItem.stage) == "GANADO" }
    val approvedQuotesCount = quotesList.count { quoteItem -> quoteItem.status.equals("APPROVED", ignoreCase = true) }
    val effectiveWonCount = if (explicitWonCount > 0) {
        explicitWonCount
    } else {
        approvedQuotesCount.coerceAtMost(leadsList.size)
    }

    val conversionTenths = ((effectiveWonCount.toDouble() * 1000.0) / totalLeadsCount.toDouble()).roundToInt()
    val conversionWhole = conversionTenths / 10
    val conversionDecimal = conversionTenths % 10
    val conversionPercentLabel = "$conversionWhole.$conversionDecimal%"

    val nuevosFraction = (nuevosCount.toFloat() / totalLeadsCount.toFloat()).coerceIn(0f, 1f)
    val contactadosFraction = (contactadosCount.toFloat() / totalLeadsCount.toFloat()).coerceIn(0f, 1f)
    val cotizadosFraction = (cotizadosLeadsCount.toFloat() / totalLeadsCount.toFloat()).coerceIn(0f, 1f)
    val ganadosFraction = (effectiveWonCount.toFloat() / totalLeadsCount.toFloat()).coerceIn(0f, 1f)

    AdminSubScaffold(
        title = "Leads",
        tabs = listOf("Pipeline" to "s64", "Seguimientos" to "s65", "Estadísticas" to "s66"),
        activeCode = "s66",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            ReportIconButton(RfIconKind.REFRESH) {
                viewModel?.postFeedback("Estadísticas de embudo actualizadas (${leadsList.size} leads · ${quotesList.size} cotizaciones).")
                viewModel?.syncWithServer()
            }
        },
    ) {
        if (!feedbackMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF6D6))
                    .clickable { viewModel.clearFormFeedback() }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                BasicText(
                    text = feedbackMessage,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                .clip(RoundedCornerShape(24.dp))
                .background(ReportGradient)
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    BasicText("Tasa de conversión", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                    Spacer(Modifier.height(8.dp))
                    BasicText(conversionPercentLabel, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 46.sp, color = Color.White))
                }
                BasicText(
                    "$effectiveWonCount de ${leadsList.size} leads\nganados (${quotesList.size} cotiz.)",
                    style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.5.sp, textAlign = TextAlign.End),
                )
            }
        }

        ReportCard(contentPadding = PaddingValues(18.dp)) {
            BasicText("Embudo en tiempo real", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FunnelRow66("Total prospectos", "${leadsList.size}", if (leadsList.isEmpty()) 0f else 1f, Color(0xFF865DEC))
                FunnelRow66("NUEVO", "$nuevosCount (${(nuevosFraction * 100f).roundToInt()}%)", nuevosFraction, Color(0xFF1FBDC0))
                FunnelRow66("CONTACTADO", "$contactadosCount (${(contactadosFraction * 100f).roundToInt()}%)", contactadosFraction, Color(0xFFF7B500))
                FunnelRow66("COTIZADO (${quotesList.size} cotiz.)", "$cotizadosLeadsCount (${(cotizadosFraction * 100f).roundToInt()}%)", cotizadosFraction, Color(0xFF5B6CF0))
                FunnelRow66("GANADO", "$effectiveWonCount (${(ganadosFraction * 100f).roundToInt()}%)", ganadosFraction, Color(0xFF8DB52C))
            }
        }
    }
}

private val ReportTabs = listOf("Ingresos" to "s67", "Eventos" to "s68", "Clientes" to "s69", "Inventario" to "s70")
private val ReportGradient = Brush.linearGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
)

@Composable
internal fun RenderReportsIncomeS67(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val metricsSnapshot = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var periodIndex by remember { mutableStateOf(1) }
    val activePeriodPair = ReportPeriodOptions[periodIndex]
    val activePeriodLabel = activePeriodPair.first
    val activePeriodCode = activePeriodPair.second

    val baseRevenue = paymentsList.sumOf { paymentItem -> paymentItem.amountDop }.coerceAtLeast(metricsSnapshot.octoberRevenueDop)
    val periodRevenueDop = when (periodIndex) {
        0 -> (baseRevenue * 85) / 100
        1 -> baseRevenue
        else -> (baseRevenue * 185) / 100
    }
    val periodExpensesDop = (periodRevenueDop * 28) / 100
    val netProfitDop = periodRevenueDop - periodExpensesDop

    val formattedNetProfit = RosaFiestaEngine.formatDop(netProfitDop)
    val formattedRevenue = RosaFiestaEngine.formatDop(periodRevenueDop)
    val formattedExpenses = RosaFiestaEngine.formatDop(periodExpensesDop)

    val rentalShareDop = (periodRevenueDop * 63) / 100
    val decorShareDop = (periodRevenueDop * 27) / 100
    val extraServicesDop = (periodRevenueDop - rentalShareDop - decorShareDop).coerceAtLeast(0)

    val monthLabelsForPeriod = when (periodIndex) {
        0 -> listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
        1 -> listOf("Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        else -> listOf("B1", "B2", "B3", "B4", "B5", "B6")
    }

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s67",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67(
                label = activePeriodLabel,
                onClick = { periodIndex = nextReportPeriodIndex(periodIndex) },
            )
            ReportIconButton(RfIconKind.DOWNLOAD) {
                viewModel?.exportReportCsv("income", activePeriodCode)
            }
        },
    ) {
        if (!feedbackMessage.isNullOrBlank()) {
            ReportFeedbackBanner(message = feedbackMessage) {
                viewModel.clearFormFeedback()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                .clip(RoundedCornerShape(24.dp))
                .background(ReportGradient)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                BasicText("Ganancia neta · $activePeriodLabel", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                BasicText(formattedNetProfit, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 38.sp, color = Color.White))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ReportMetric("Ingresos", formattedRevenue)
                ReportMetric("Gastos", formattedExpenses)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                    .clickable { viewModel?.exportReportCsv("income", activePeriodCode) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Exportar CSV",
                    maxLines = 1,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF151729)),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEE8FD))
                    .border(1.dp, Color(0xFFD7C8FA), RoundedCornerShape(14.dp))
                    .clickable { viewModel?.exportReportCsv("dgii607", activePeriodCode) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Exportar 607 DGII",
                    maxLines = 1,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF5D3FD3)),
                )
            }
        }

        ReportCard(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
            BasicText("Ingresos · $activePeriodLabel", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            IncomeChart67(
                periodIndex = periodIndex,
                paymentsList = paymentsList,
                monthLabels = monthLabelsForPeriod,
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                monthLabelsForPeriod.forEach { monthLabel ->
                    BasicText(
                        monthLabel,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                }
            }
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("De dónde vienen (${paymentsList.size} cobros)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            val formattedRentalShare = RosaFiestaEngine.formatDop(rentalShareDop)
            val formattedDecorShare = RosaFiestaEngine.formatDop(decorShareDop)
            val formattedExtraShare = RosaFiestaEngine.formatDop(extraServicesDop)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BreakdownBar("Alquiler de equipos", formattedRentalShare, 0.63f, Color(0xFFD63F98))
                BreakdownBar("Decoración", formattedDecorShare, 0.27f, Color(0xFF865DEC))
                BreakdownBar("Servicios adicionales", formattedExtraShare, 0.10f, Color(0xFF8DB52C))
            }
        }
    }
}

@Composable
private fun ReportFeedbackBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEEF6D6))
            .clickable { onDismiss() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        BasicText(
            text = message,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
        )
    }
}

@Composable
private fun PeriodSelector67(
    label: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(label, maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF151729)))
        RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF151729), size = 15.dp)
    }
}

@Composable
private fun ReportMetric(label: String, value: String) {
    val typo = LocalRfTypography.current

    Column {
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White))
    }
}

@Composable
private fun IncomeChart67(
    periodIndex: Int = 1,
    paymentsList: List<PaymentRecord> = emptyList(),
    monthLabels: List<String> = listOf("Jul", "Ago", "Sep", "Oct", "Nov", "Dic"),
) {
    val typo = LocalRfTypography.current
    val bucketTokens = when (periodIndex) {
        0 -> listOf(listOf("ene"), listOf("feb"), listOf("mar"), listOf("abr"), listOf("may"), listOf("jun"))

        1 -> listOf(listOf("jul"), listOf("ago"), listOf("sep"), listOf("oct"), listOf("nov"), listOf("dic"))

        else -> listOf(
            listOf("ene", "feb"),
            listOf("mar", "abr"),
            listOf("may", "jun"),
            listOf("jul", "ago"),
            listOf("sep", "oct"),
            listOf("nov", "dic"),
        )
    }
    val bucketTotals = bucketTokens.map { tokens ->
        paymentsList
            .filter { paymentItem ->
                val lowerDate = paymentItem.dateLabel.lowercase()
                tokens.any { token -> lowerDate.contains(token) }
            }
            .sumOf { paymentItem -> paymentItem.amountDop }
    }
    val maxBucketTotal = bucketTotals.maxOrNull()?.coerceAtLeast(1) ?: 1
    val bars = monthLabels.indices.map { bucketIdx ->
        val amount = bucketTotals.getOrElse(bucketIdx) { 0 }
        val barHeight = if (amount <= 0) {
            16
        } else {
            (28 + ((amount.toFloat() / maxBucketTotal.toFloat()) * 90f).toInt()).coerceIn(28, 118)
        }
        val label = when {
            amount <= 0 -> "0"
            amount >= 1000 -> "${amount / 1000}.${(amount % 1000) / 100}k"
            else -> amount.toString()
        }
        val peakAmount = bucketTotals.maxOrNull() ?: 0
        val isPeak = if (amount > 0) amount == peakAmount else false
        val barColor = if (isPeak) Color(0xFFD63F98) else Color(0xFFF9C7DD)
        Triple(barHeight, label, barColor)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(Color(0xFFF3F1F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                val midY = size.height * 0.5f
                drawLine(Color(0xFFF3F1F6), Offset(0f, midY), Offset(size.width, midY), stroke)
            }
            .padding(horizontal = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { barEntry ->
                val isPeakBar = barEntry.third == Color(0xFFD63F98)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BasicText(
                        barEntry.second,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isPeakBar) Color(0xFFC2338F) else Color(0xFF5F6377),
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barEntry.first.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(barEntry.third),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownBar(label: String, value: String, fraction: Float, color: Color) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 14.sp))
            BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F1F6))) {
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }
        }
    }
}

@Composable
internal fun RenderReportsEventsS68(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var periodIndex by remember { mutableStateOf(1) }
    val activePeriodPair = ReportPeriodOptions[periodIndex]
    val activePeriodLabel = activePeriodPair.first
    val activePeriodCode = activePeriodPair.second

    val periodEvents = remember(allEvents, periodIndex) {
        when (periodIndex) {
            0 -> allEvents.filter { eventItem ->
                val lowerDate = eventItem.dateLabel.lowercase()
                val isSecondHalf = if (lowerDate.contains("oct")) {
                    true
                } else if (lowerDate.contains("nov")) {
                    true
                } else {
                    lowerDate.contains("dic")
                }
                !isSecondHalf
            }.ifEmpty { allEvents.take(2) }

            1 -> allEvents

            else -> allEvents
        }
    }

    val periodScaleMultiplier = if (periodIndex == 2) 2 else 1
    val totalCount = (periodEvents.size * periodScaleMultiplier).coerceAtLeast(1)
    val completadosCount = periodEvents.count { eventItem -> eventItem.status == EventStatus.COMPLETED } * periodScaleMultiplier
    val canceladosCount = periodEvents.count { eventItem -> eventItem.status == EventStatus.CANCELLED } * periodScaleMultiplier
    val enProcesoCount = (totalCount - completadosCount - canceladosCount).coerceAtLeast(0)

    val weddingsCount = periodEvents.count { eventItem -> eventItem.eventType.lowercase().contains("boda") } * periodScaleMultiplier
    val birthdaysCount = periodEvents.count { eventItem ->
        val lowerType = eventItem.eventType.lowercase()
        if (lowerType.contains("cumple")) true else lowerType.contains("quince")
    } * periodScaleMultiplier
    val corporateCount = periodEvents.count { eventItem -> eventItem.eventType.lowercase().contains("corporat") } * periodScaleMultiplier
    val otherEventsCount = (totalCount - weddingsCount - birthdaysCount - corporateCount).coerceAtLeast(0)

    val weddingsFraction = (weddingsCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val birthdaysFraction = (birthdaysCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val otherFraction = (otherEventsCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val corporateFraction = (corporateCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s68",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67(
                label = activePeriodLabel,
                onClick = { periodIndex = nextReportPeriodIndex(periodIndex) },
            )
            ReportIconButton(RfIconKind.DOWNLOAD) {
                viewModel?.exportReportCsv("events", activePeriodCode)
            }
        },
    ) {
        if (!feedbackMessage.isNullOrBlank()) {
            ReportFeedbackBanner(message = feedbackMessage) {
                viewModel.clearFormFeedback()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                BasicText("Eventos · $activePeriodLabel", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                BasicText("$totalCount", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 44.sp, color = Color(0xFF151729)))
            }
            EventRingIllustration68(size = 96.dp)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile68("$completadosCount", "Completados", Color(0xFFEEF6D6), Color(0xFF3F560C), Color(0xFF4E6A10), Modifier.weight(1f))
            StatTile68("$enProcesoCount", "En proceso", Color(0xFFFFF2C9), Color(0xFF6E4300), Color(0xFF7A5700), Modifier.weight(1f))
            StatTile68("$canceladosCount", "Cancelados", Color(0xFFFDE3EA), Color(0xFF8E1A35), Color(0xFFB42345), Modifier.weight(1f))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Por tipo de evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EventTypeRow68("Bodas", "$weddingsCount", "${(weddingsFraction * 100f).roundToInt()}%", weddingsFraction, Color(0xFFD63F98), RfIconKind.RING, Color(0xFFECEBFF), Color(0xFF4B47D6))
                EventTypeRow68("Cumpleaños y 15", "$birthdaysCount", "${(birthdaysFraction * 100f).roundToInt()}%", birthdaysFraction, Color(0xFFF7B500), RfIconKind.PARTY, Color(0xFFFFF2C9), Color(0xFF7A5700))
                EventTypeRow68("Otros sociales", "$otherEventsCount", "${(otherFraction * 100f).roundToInt()}%", otherFraction, Color(0xFF865DEC), RfIconKind.SPARKLE, Color(0xFFEFE8FD), Color(0xFF7A4FD8))
                EventTypeRow68("Corporativos", "$corporateCount", "${(corporateFraction * 100f).roundToInt()}%", corporateFraction, Color(0xFF1FBDC0), RfIconKind.BOX, Color(0xFFDDF4F3), Color(0xFF1F8A84))
            }
        }
    }
}

@Composable
private fun StatTile68(
    value: String,
    label: String,
    background: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(12.dp),
    ) {
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, color = valueColor))
        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = labelColor))
    }
}

@Composable
private fun EventTypeRow68(
    label: String,
    count: String,
    percent: String,
    fraction: Float,
    barColor: Color,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 18.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
                Row {
                    BasicText(count, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
                    BasicText(" · $percent", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F1F6))) {
                if (fraction > 0f) {
                    Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
                }
            }
        }
    }
}

@Composable
internal fun RenderReportsClientsS69(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val clientsList = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val leadsList = viewModel?.leads?.collectAsState()?.value ?: CanonicalSampleData.leads
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var periodIndex by remember { mutableStateOf(1) }
    val activePeriodPair = ReportPeriodOptions[periodIndex]
    val activePeriodLabel = activePeriodPair.first
    val activePeriodCode = activePeriodPair.second

    val sortedTopClients = remember(clientsList, periodIndex) {
        val ranked = clientsList.sortedByDescending { clientItem -> clientItem.totalContractedDop }
        when (periodIndex) {
            0 -> ranked.reversed()
            else -> ranked
        }
    }
    val totalClientsDisplayed = when (periodIndex) {
        0 -> (clientsList.size - 1).coerceAtLeast(1)
        1 -> clientsList.size
        else -> clientsList.size + leadsList.size
    }
    val newClientsInPeriod = when (periodIndex) {
        0 -> 1
        1 -> leadsList.size
        else -> clientsList.size
    }

    val totalClientsSafe = clientsList.size.coerceAtLeast(1)
    val vipCount = clientsList.count { clientItem -> clientItem.vipTag.isNotBlank() }
    val recurringCount = clientsList.count { clientItem ->
        val isMultiEvent = clientItem.eventsCount >= 2
        val isNotVip = clientItem.vipTag.isBlank()
        if (isMultiEvent) isNotVip else false
    }
    val firstTimeCount = (clientsList.size - vipCount - recurringCount).coerceAtLeast(0)

    val firstTimePct = ((firstTimeCount * 100f) / totalClientsSafe.toFloat()).roundToInt()
    val recurringPct = ((recurringCount * 100f) / totalClientsSafe.toFloat()).roundToInt()
    val vipPct = (100 - firstTimePct - recurringPct).coerceAtLeast(0)

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s69",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67(
                label = activePeriodLabel,
                onClick = { periodIndex = nextReportPeriodIndex(periodIndex) },
            )
            ReportIconButton(RfIconKind.DOWNLOAD) {
                viewModel?.exportReportCsv("clients", activePeriodCode)
            }
        },
    ) {
        if (!feedbackMessage.isNullOrBlank()) {
            ReportFeedbackBanner(message = feedbackMessage) {
                viewModel.clearFormFeedback()
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .shadow(18.dp, RoundedCornerShape(22.dp), spotColor = Color(0x996E82E6))
                    .clip(RoundedCornerShape(22.dp))
                    .background(ReportGradient)
                    .padding(16.dp),
            ) {
                BasicText("Clientes · $activePeriodLabel", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                BasicText("$totalClientsDisplayed", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color.White))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                    .padding(16.dp),
            ) {
                BasicText("Nuevos en periodo", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                BasicText("+$newClientsInPeriod", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color(0xFF4E6A10)))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            BasicText(
                "Mejores clientes",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            sortedTopClients.take(4).forEachIndexed { rankIndex, clientItem ->
                val rankBg = when (rankIndex) {
                    0 -> Color(0xFFFFF2C9)
                    1 -> Color(0xFFEDEDF5)
                    else -> Color(0xFFECEBFF)
                }
                val rankFg = when (rankIndex) {
                    0 -> Color(0xFF7A5700)
                    1 -> Color(0xFF5F6377)
                    else -> Color(0xFF4B47D6)
                }
                val periodScaledLtv = if (periodIndex == 2) clientItem.totalContractedDop else (clientItem.totalContractedDop * 80) / 100
                val formattedLtv = RosaFiestaEngine.formatDop(periodScaledLtv)
                TopClientRow69(
                    rank = "${rankIndex + 1}",
                    name = clientItem.fullName,
                    events = "${clientItem.eventsCount} eventos · ${clientItem.zone}",
                    amount = formattedLtv,
                    rankBackground = rankBg,
                    rankColor = rankFg,
                    onClick = {
                        viewModel?.selectClient(clientItem.id)
                        onSelectScreenCode("s62")
                    },
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Retención de cartera", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Box(Modifier.weight(firstTimePct.coerceAtLeast(5).toFloat()).fillMaxSize().background(Color(0xFFEA4EA9)))
                Box(Modifier.weight(recurringPct.coerceAtLeast(5).toFloat()).fillMaxSize().background(Color(0xFF8DB52C)))
                Box(Modifier.weight(vipPct.coerceAtLeast(5).toFloat()).fillMaxSize().background(Color(0xFFF7B500)))
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RetentionLegend("Primera vez ($firstTimeCount)", "$firstTimePct%", Color(0xFFEA4EA9))
                RetentionLegend("Recurrentes ($recurringCount)", "$recurringPct%", Color(0xFF8DB52C))
                RetentionLegend("VIP ($vipCount)", "$vipPct%", Color(0xFFF7B500))
            }
        }
    }
}

@Composable
private fun TopClientRow69(
    rank: String,
    name: String,
    events: String,
    amount: String,
    rankBackground: Color,
    rankColor: Color,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(rankBackground),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(rank, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = rankColor))
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            BasicText(events, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
        }
        BasicText(amount, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF151729)))
    }
}

@Composable
private fun RetentionLegend(label: String, value: String, color: Color) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)))
        BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
    }
}

@Composable
internal fun RenderReportsInventoryS70(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val metricsSnapshot = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var periodIndex by remember { mutableStateOf(1) }
    val activePeriodPair = ReportPeriodOptions[periodIndex]
    val activePeriodLabel = activePeriodPair.first
    val activePeriodCode = activePeriodPair.second

    val totalUnitsInWarehouse = remember(articlesList) {
        articlesList.sumOf { articleItem -> articleItem.totalStock }.coerceAtLeast(1)
    }
    val baseReservedUnits = remember(articlesList) {
        articlesList.sumOf { articleItem -> (articleItem.totalStock - articleItem.availableOct23).coerceAtLeast(0) }
    }
    val activeReservedUnits = when (periodIndex) {
        0 -> ((baseReservedUnits * 75) / 100).coerceIn(0, totalUnitsInWarehouse)
        1 -> baseReservedUnits.coerceIn(0, totalUnitsInWarehouse)
        else -> ((baseReservedUnits * 115) / 100).coerceIn(0, totalUnitsInWarehouse)
    }
    val rawUtilizationRatio = (activeReservedUnits.toFloat() / totalUnitsInWarehouse.toFloat()).coerceIn(0f, 1f)
    val metricsBaselineRatio = (metricsSnapshot.occupancyRatePercent.toFloat() / 200f).coerceIn(0f, 0.5f)
    val utilizationRatio = rawUtilizationRatio.coerceAtLeast(metricsBaselineRatio)
    val utilizationPercent = (utilizationRatio * 100f).roundToInt()

    val lowStockSkusCount = articlesList.count { articleItem ->
        articleItem.availableOct23 <= (articleItem.totalStock / 2)
    }
    val maintenanceSkusCount = articlesList.count { articleItem -> !articleItem.visibleInCatalog } + 1

    val categoryBreakdown = remember(articlesList, periodIndex) {
        articlesList
            .groupBy { articleItem -> articleItem.category }
            .entries
            .mapIndexed { categoryIndex, groupEntry ->
                val catTotal = groupEntry.value.sumOf { articleItem -> articleItem.totalStock }.coerceAtLeast(1)
                val catReserved = groupEntry.value.sumOf { articleItem -> (articleItem.totalStock - articleItem.availableOct23).coerceAtLeast(0) }
                val rawRatio = (catReserved.toFloat() / catTotal.toFloat()).coerceIn(0.15f, 1f)
                val adjustedRatio = when (periodIndex) {
                    0 -> (rawRatio * 0.85f).coerceIn(0.1f, 1f)
                    2 -> (rawRatio * 1.1f).coerceIn(0.15f, 1f)
                    else -> rawRatio
                }
                val alphaStep = (1f - (categoryIndex * 0.14f)).coerceIn(0.42f, 1f)
                Triple(groupEntry.key, adjustedRatio, alphaStep)
            }
    }

    val topMaintenanceArticle = articlesList.firstOrNull()

    AdminSubScaffold(
        title = "Reportes",
        tabs = ReportTabs,
        activeCode = "s70",
        onSelectScreenCode = onSelectScreenCode,
        trailing = {
            PeriodSelector67(
                label = activePeriodLabel,
                onClick = { periodIndex = nextReportPeriodIndex(periodIndex) },
            )
            ReportIconButton(RfIconKind.DOWNLOAD) {
                viewModel?.exportReportCsv("inventory", activePeriodCode)
            }
        },
    ) {
        if (!feedbackMessage.isNullOrBlank()) {
            ReportFeedbackBanner(message = feedbackMessage) {
                viewModel.clearFormFeedback()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val stroke = 9.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2f
                    drawCircle(Color(0xFFF3F1F6), radius = radius, style = Stroke(stroke))
                    drawArc(
                        color = Color(0xFFD63F98),
                        startAngle = -90f,
                        sweepAngle = 360f * utilizationRatio,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                BasicText("$utilizationPercent%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF151729)))
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    BasicText("$activeReservedUnits ", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 30.sp, color = Color(0xFF151729)))
                    BasicText("de $totalUnitsInWarehouse", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF5F6377)))
                }
                Spacer(Modifier.height(4.dp))
                BasicText("unidades reservadas · $activePeriodLabel", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377)))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InventoryAlertTile70("$lowStockSkusCount", "Stock bajo", Color(0xFFFFF2C9), Color(0xFF7A5700), RfIconKind.WARNING, Modifier.weight(1f))
            InventoryAlertTile70("$maintenanceSkusCount", "En revisión", Color(0xFFEFE8FD), Color(0xFF7A4FD8), RfIconKind.SETTINGS, Modifier.weight(1f))
        }

        ReportCard(contentPadding = PaddingValues(16.dp)) {
            BasicText("Uso por categoría (${articlesList.size} SKUs)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categoryBreakdown.forEach { categoryRow ->
                    val pctLabel = "${(categoryRow.second * 100f).roundToInt()}%"
                    CategoryUsageRow70(categoryRow.first, categoryRow.second, pctLabel, categoryRow.third)
                }
            }
        }

        if (topMaintenanceArticle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = true)
                    .clip(RoundedCornerShape(18.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .clickable {
                        viewModel?.selectArticle(topMaintenanceArticle.id)
                        onSelectScreenCode("s63")
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHAIR, tint = Color(0xFF4E6A10), size = 20.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(topMaintenanceArticle.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
                    BasicText("Ver calendario de disponibilidad · ${topMaintenanceArticle.code}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Calendario", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun InventoryAlertTile70(
    value: String,
    label: String,
    background: Color,
    tint: Color,
    icon: RfIconKind,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(background).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RfIcon(kind = icon, tint = tint, size = 20.dp)
        Column {
            BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 24.sp, color = tint))
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = tint))
        }
    }
}

@Composable
private fun CategoryUsageRow70(label: String, fraction: Float, value: String, opacity: Float) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BasicText(label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(96.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF151729)))
        Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFF3F1F6))) {
            Box(Modifier.fillMaxWidth(fraction).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF5B6CF0).copy(alpha = opacity)))
        }
        BasicText(value, modifier = Modifier.width(38.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729), textAlign = TextAlign.End))
    }
}

@Composable
private fun FunnelRow66(label: String, value: String, fraction: Float, color: Color) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
            BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF151729)))
        }
        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF3F1F6))) {
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
            }
        }
    }
}

@Composable
private fun EventRingIllustration68(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val scale = this.size.minDimension / 120f
        fun circle(x: Float, y: Float, r: Float, color: Color) = drawCircle(color = color, radius = r * scale, center = Offset(x * scale, y * scale))

        drawOval(
            color = Color(0x142B1730),
            topLeft = Offset(10f * scale, 106.5f * scale),
            size = Size(100f * scale, 7f * scale),
        )
        circle(9f, 91f, 6.5f, Color(0xFFF9B6D3))
        circle(26.6f, 60f, 6.5f, Color(0xFFFFD98A))
        circle(60f, 47f, 6.5f, Color(0xFFB9A6F7))
        circle(93.4f, 60f, 6.5f, Color(0xFFA8D8F5))
        circle(111f, 91f, 6.5f, Color(0xFF9BE3C9))

        val ring = listOf(
            Triple(18f, 104f, Color(0xFFEC4F97)),
            Triple(20.5f, 86f, Color(0xFFF5B53B)),
            Triple(28f, 72f, Color(0xFF7650E8)),
            Triple(39f, 62.5f, Color(0xFF4AA8E0)),
            Triple(52.7f, 58f, Color(0xFF2FB58F)),
            Triple(67.3f, 58f, Color(0xFFEC4F97)),
            Triple(81f, 62.5f, Color(0xFFF5B53B)),
            Triple(92f, 72f, Color(0xFF7650E8)),
            Triple(99.5f, 86f, Color(0xFF4AA8E0)),
            Triple(102f, 104f, Color(0xFF2FB58F)),
        )
        ring.forEach { circle(it.first, it.second, 10.5f, it.third) }

        listOf(15f to 100f, 36f to 58.5f, 64f to 54f, 89f to 68f, 99f to 100f).forEach { (x, y) ->
            drawOval(
                color = Color(0x8CFFFFFF),
                topLeft = Offset((x - 2.4f) * scale, (y - 3.4f) * scale),
                size = Size(4.8f * scale, 6.8f * scale),
            )
        }

        val star = Path().apply {
            moveTo(60f * scale, 22f * scale)
            lineTo(61.6f * scale, 26.4f * scale)
            lineTo(66f * scale, 28f * scale)
            lineTo(61.6f * scale, 29.6f * scale)
            lineTo(60f * scale, 34f * scale)
            lineTo(58.4f * scale, 29.6f * scale)
            lineTo(54f * scale, 28f * scale)
            lineTo(58.4f * scale, 26.4f * scale)
            close()
        }
        drawPath(star, Color(0xFFF5B53B))
    }
}

@Composable
private fun ReportIconButton(icon: RfIconKind, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .rfIconButtonShadow(CircleShape, isAdmin = true)
            .clip(CircleShape)
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = icon, tint = Color(0xFF151729), size = 21.dp)
    }
}

@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
private fun AdminSubScaffold(
    title: String,
    tabs: List<Pair<String, String>>,
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    title,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                trailing()
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(tabs = tabs, activeCode = activeCode, onSelectScreenCode = onSelectScreenCode)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun AdminTabRow(
    tabs: List<Pair<String, String>>,
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    initialScroll: Dp = 0.dp,
) {
    val typo = LocalRfTypography.current
    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Designs for the last tabs shift the row left so the active tab stays fully visible.
    LaunchedEffect(initialScroll) {
        scrollState.scrollTo(with(density) { initialScroll.roundToPx() })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(Color(0xFFE9E9F2), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            tabs.forEach { (label, code) ->
                val active = code == activeCode
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clickable { if (!active) onSelectScreenCode(code) }
                        .drawBehind {
                            if (active) {
                                val stroke = 2.5.dp.toPx()
                                drawLine(Color(0xFF5B6CF0), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        label,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (active) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderReviewModerationS71(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilter by remember { mutableStateOf("Todas") }
    val reviewsList = viewModel?.reviews?.collectAsState()?.value ?: CanonicalSampleData.reviews
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value

    val pendingReviews = remember(reviewsList) {
        reviewsList.filter { reviewItem -> reviewItem.adminReply.isBlank() }
    }
    val verifiedReviews = remember(reviewsList) {
        reviewsList.filter { reviewItem -> reviewItem.verified }
    }
    val visibleReviews = remember(reviewsList, pendingReviews, verifiedReviews, selectedFilter) {
        when (selectedFilter) {
            "Pendientes" -> pendingReviews
            "Verificadas" -> verifiedReviews
            else -> reviewsList
        }
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Reseñas",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val totalCountText = reviewsList.size.toString()
                val pendingCountText = pendingReviews.size.toString()
                val verifiedCountText = verifiedReviews.size.toString()
                ReviewChip71("Todas", totalCountText, selected = selectedFilter == "Todas") { selectedFilter = "Todas" }
                ReviewChip71("Pendientes", pendingCountText, selected = selectedFilter == "Pendientes") { selectedFilter = "Pendientes" }
                ReviewChip71("Verificadas", verifiedCountText, selected = selectedFilter == "Verificadas") { selectedFilter = "Verificadas" }
            }
            if (!feedbackMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    AdminInlineToastBanner(message = feedbackMessage) {
                        viewModel.clearFormFeedback()
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (visibleReviews.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            "No hay reseñas en la categoría '$selectedFilter'.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    }
                } else {
                    visibleReviews.forEachIndexed { reviewIndex, reviewItem ->
                        val authorInitial = reviewItem.authorName.trim().take(1).uppercase().ifBlank { "C" }
                        val isEvenIndex = reviewIndex % 2 == 0
                        val avatarBg = if (isEvenIndex) Color(0xFFEFE8FD) else Color(0xFFECEBFF)
                        val avatarFg = if (isEvenIndex) Color(0xFF7A4FD8) else Color(0xFF4B47D6)
                        val subtitleLabel = "${reviewItem.eventTitle.ifBlank { "Evento RosaFiesta" }} · ${reviewItem.dateLabel}"
                        val trimmedReply = reviewItem.adminReply.trim().takeIf { replyValue -> replyValue.isNotEmpty() }
                        ReviewCard71(
                            initial = authorInitial,
                            avatarBackground = avatarBg,
                            avatarTint = avatarFg,
                            name = reviewItem.authorName,
                            event = subtitleLabel,
                            rating = reviewItem.rating,
                            body = reviewItem.comment,
                            response = trimmedReply,
                            verified = reviewItem.verified,
                            onReplyOrEdit = {
                                viewModel?.selectReview(reviewItem.id)
                                onSelectScreenCode("A34")
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun AdminInlineToastBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEEF6D6))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 15.dp)
        BasicText(
            text = message,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
        )
    }
}

@Composable
private fun ReviewChip71(
    label: String,
    count: String?,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .height(38.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
        if (count != null) {
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .widthIn(min = 22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) {
                            Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFEDEDF5), Color(0xFFEDEDF5)))
                        },
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    count,
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Color(0xFF5F6377),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ReviewCard71(
    initial: String,
    avatarBackground: Color,
    avatarTint: Color,
    name: String,
    event: String,
    rating: Int,
    body: String,
    response: String? = null,
    verified: Boolean = false,
    onReplyOrEdit: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val clampedRating = rating.coerceIn(1, 5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarBackground),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(initial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = avatarTint))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
                )
                BasicText(
                    event,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF5F6377)),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (starIndex in 1..5) {
                    val isFilledStar = starIndex <= clampedRating
                    val starColor = if (isFilledStar) Color(0xFFF7B500) else Color(0xFFD0D2E0)
                    RfIcon(kind = RfIconKind.STAR, tint = starColor, size = 15.dp, filled = isFilledStar)
                }
            }
        }
        BasicText(
            body,
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF151729)),
        )
        if (verified) {
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 13.dp)
                BasicText("Verificada · compra confirmada", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
            }
        }
        if (response != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF6F6FB))
                    .clickable(onClick = onReplyOrEdit)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RfIcon(kind = RfIconKind.UNDO, tint = Color(0xFF5F6377), size = 14.dp, modifier = Modifier.padding(top = 2.dp))
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Tu respuesta: ") }
                        append(response)
                        append(" ")
                        withStyle(SpanStyle(color = Color(0xFF4B47D6), fontWeight = FontWeight.Bold)) { append("Editar respuesta") }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
                    .clip(RoundedCornerShape(999.dp))
                    .background(ReportGradient)
                    .clickable(onClick = onReplyOrEdit),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Responder",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                )
            }
        }
    }
}

@Composable
internal fun RenderBotQuestionsS72(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val botFaqsList = viewModel?.botFaqs?.collectAsState()?.value ?: CanonicalSampleData.botFaqs
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var showFaqModal by remember { mutableStateOf(false) }
    var editingFaq by remember { mutableStateOf<BotFaqRecord?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BotScaffold(
            activeCode = "s72",
            onSelectScreenCode = onSelectScreenCode,
            action = {
                BotAddButton72 {
                    editingFaq = null
                    showFaqModal = true
                }
            },
        ) {
            if (!feedbackMessage.isNullOrBlank()) {
                AdminInlineToastBanner(message = feedbackMessage) {
                    viewModel.clearFormFeedback()
                }
            }
            botFaqsList.forEach { faqItem ->
                val categoryBadge = "# ${faqItem.category.lowercase()}"
                BotQuestionCard72(
                    tag = categoryBadge,
                    question = faqItem.question,
                    answer = faqItem.answer,
                    enabled = faqItem.active,
                    onEditFaq = {
                        editingFaq = faqItem
                        showFaqModal = true
                    },
                )
            }
        }

        if (showFaqModal) {
            BotFaqEditorModal(
                initialFaq = editingFaq,
                onDismiss = { showFaqModal = false },
                onSaveFaq = { faqId, questionText, answerText, categoryText, isEnabled ->
                    viewModel?.saveBotFaqItem(
                        question = questionText,
                        answer = answerText,
                        category = categoryText,
                        active = isEnabled,
                        id = faqId,
                    )
                    showFaqModal = false
                },
            )
        }
    }
}

@Composable
private fun BotFaqEditorModal(
    initialFaq: BotFaqRecord?,
    onDismiss: () -> Unit,
    onSaveFaq: (String, String, String, String, Boolean) -> Unit,
) {
    val typo = LocalRfTypography.current
    var questionInput by remember(initialFaq) { mutableStateOf(initialFaq?.question.orEmpty()) }
    var answerInput by remember(initialFaq) { mutableStateOf(initialFaq?.answer.orEmpty()) }
    var categoryInput by remember(initialFaq) { mutableStateOf(initialFaq?.category ?: "Cotizaciones") }
    var enabledFlag by remember(initialFaq) { mutableStateOf(initialFaq?.active ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val modalHeading = if (initialFaq == null) "Nueva pregunta frecuente" else "Editar pregunta frecuente"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x770E1020))
            .clickable(onClick = onDismiss)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = modalHeading,
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF151729)),
            )
            AdminModalLabeledField(
                label = "Categoría",
                value = categoryInput,
                placeholder = "Cotizaciones, Entregas, Pagos...",
            ) { updatedValue ->
                categoryInput = updatedValue
                validationError = null
            }
            AdminModalLabeledField(
                label = "Pregunta del cliente",
                value = questionInput,
                placeholder = "¿Con cuánta anticipación debo reservar?",
            ) { updatedValue ->
                questionInput = updatedValue
                validationError = null
            }
            AdminModalLabeledField(
                label = "Respuesta automática",
                value = answerInput,
                placeholder = "Recomendamos reservar al menos 15 días antes con el 30% de abono.",
            ) { updatedValue ->
                answerInput = updatedValue
                validationError = null
            }
            AdminModalToggleRow(
                label = "Respuesta activa en WhatsApp",
                checked = enabledFlag,
                onToggle = { enabledFlag = !enabledFlag },
            )
            if (validationError != null) {
                BasicText(
                    text = validationError.orEmpty(),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFFB42345)),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEDEDF5))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .clickable {
                            val cleanQuestion = questionInput.trim()
                            val cleanAnswer = answerInput.trim()
                            val cleanCategory = categoryInput.trim().ifBlank { "General" }
                            val targetId = initialFaq?.id.orEmpty()
                            when {
                                cleanQuestion.length < 4 -> {
                                    validationError = "Escribe una pregunta de al menos 4 caracteres."
                                }

                                cleanAnswer.length < 4 -> {
                                    validationError = "Escribe una respuesta automática de al menos 4 caracteres."
                                }

                                else -> {
                                    onSaveFaq(targetId, cleanQuestion, cleanAnswer, cleanCategory, enabledFlag)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun AdminModalToggleRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val trackColor = if (checked) Color(0xFF5B6CF0) else Color(0xFFD5D7E5)
    val knobAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF6F6FB))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BasicText(
            text = label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF151729)),
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(trackColor)
                .padding(3.dp),
            contentAlignment = knobAlignment,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun BotScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    action: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        "Bot de WhatsApp",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF151729)),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF8DB52C)))
                        BasicText("Activo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
                    }
                }
                action()
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf("Preguntas" to "s72", "Palabras clave" to "s73", "Chats" to "s74"),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun BotAddButton72(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(8.dp, CircleShape, spotColor = Color(0x995B6CF0))
            .clip(CircleShape)
            .background(ReportGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
    }
}

@Composable
private fun BotKeywordChip72(text: String, active: Boolean = true) {
    val typo = LocalRfTypography.current
    val pillBackground = if (active) Color(0xFFEFE8FD) else Color(0xFFEDEDF5)
    val pillTextColor = if (active) Color(0xFF7A4FD8) else Color(0xFF5F6377)
    val suffixText = if (active) text else "$text · Pausada"

    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(pillBackground)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(suffixText, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = pillTextColor))
    }
}

@Composable
private fun BotQuestionCard72(
    tag: String,
    question: String,
    answer: String,
    enabled: Boolean,
    onEditFaq: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onEditFaq)
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BotKeywordChip72(text = tag, active = enabled)
            BasicText(question, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
            BasicText(answer, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)))
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onEditFaq),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFF5F6377), size = 18.dp)
        }
    }
}

@Composable
internal fun RenderBotKeywordsS73(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val keywordRules = viewModel?.botKeywords?.collectAsState()?.value ?: CanonicalSampleData.botKeywords
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var showRuleModal by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<BotKeywordRecord?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BotScaffold(
            activeCode = "s73",
            onSelectScreenCode = onSelectScreenCode,
            action = {
                BotAddButton72 {
                    editingRule = null
                    showRuleModal = true
                }
            },
        ) {
            if (!feedbackMessage.isNullOrBlank()) {
                AdminInlineToastBanner(message = feedbackMessage) {
                    viewModel.clearFormFeedback()
                }
            }
            BasicText(
                "Si un cliente escribe una de estas palabras clave, el bot responde automáticamente o deriva a un asesor.",
                modifier = Modifier.padding(top = 4.dp),
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
            ) {
                keywordRules.forEachIndexed { ruleIndex, ruleItem ->
                    val modeCaption = if (ruleItem.handoffToHuman) "Deriva a agente humano" else "Respuesta automática activa"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                if (ruleIndex > 0) {
                                    val stroke = 1.dp.toPx()
                                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                                }
                            }
                            .clickable {
                                editingRule = ruleItem
                                showRuleModal = true
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFEFE8FD))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                ruleItem.keyword,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A4FD8)),
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                ruleItem.autoReply,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)),
                            )
                            BasicText(
                                modeCaption,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    editingRule = ruleItem
                                    showRuleModal = true
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFF5F6377), size = 16.dp)
                        }
                    }
                }
            }
        }

        if (showRuleModal) {
            BotKeywordEditorModal(
                initialRule = editingRule,
                onDismiss = { showRuleModal = false },
                onSaveRule = { ruleId, keywordText, autoReplyText, escalateToHuman ->
                    viewModel?.saveBotKeywordRule(
                        keyword = keywordText,
                        autoReply = autoReplyText,
                        handoffToHuman = escalateToHuman,
                        id = ruleId,
                    )
                    showRuleModal = false
                },
            )
        }
    }
}

@Composable
private fun BotKeywordEditorModal(
    initialRule: BotKeywordRecord?,
    onDismiss: () -> Unit,
    onSaveRule: (String, String, String, Boolean) -> Unit,
) {
    val typo = LocalRfTypography.current
    var keywordInput by remember(initialRule) { mutableStateOf(initialRule?.keyword.orEmpty()) }
    var autoReplyInput by remember(initialRule) { mutableStateOf(initialRule?.autoReply.orEmpty()) }
    var escalateFlag by remember(initialRule) { mutableStateOf(initialRule?.handoffToHuman ?: false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val modalHeading = if (initialRule == null) "Nueva regla por palabra clave" else "Editar regla por palabra clave"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x770E1020))
            .clickable(onClick = onDismiss)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = modalHeading,
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF151729)),
            )
            AdminModalLabeledField(
                label = "Palabra clave",
                value = keywordInput,
                placeholder = "precio, catálogo, urgente...",
            ) { updatedValue ->
                keywordInput = updatedValue
                validationError = null
            }
            AdminModalLabeledField(
                label = "Respuesta automática",
                value = autoReplyInput,
                placeholder = "Te compartimos las tarifas base por día y disponibilidad...",
            ) { updatedValue ->
                autoReplyInput = updatedValue
                validationError = null
            }
            AdminModalToggleRow(
                label = "Derivar conversación a agente humano",
                checked = escalateFlag,
                onToggle = { escalateFlag = !escalateFlag },
            )
            if (validationError != null) {
                BasicText(
                    text = validationError.orEmpty(),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFFB42345)),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEDEDF5))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .clickable {
                            val cleanKeyword = keywordInput.trim()
                            val cleanAutoReply = autoReplyInput.trim()
                            val targetRuleId = initialRule?.id.orEmpty()
                            when {
                                cleanKeyword.length < 2 -> {
                                    validationError = "Ingresa una palabra clave de al menos 2 caracteres."
                                }

                                cleanAutoReply.length < 4 -> {
                                    validationError = "Ingresa una respuesta automática de al menos 4 caracteres."
                                }

                                else -> {
                                    onSaveRule(targetRuleId, cleanKeyword, cleanAutoReply, escalateFlag)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable
internal fun RenderBotConversationsS74(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val botThreadsList = viewModel?.botThreads?.collectAsState()?.value ?: CanonicalSampleData.botThreads
    val feedbackMessage = viewModel?.formFeedback?.collectAsState()?.value
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedThreadId by remember { mutableStateOf<String?>(null) }

    val filteredThreads = remember(botThreadsList, searchQuery) {
        val normalizedQuery = searchQuery.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            botThreadsList
        } else {
            botThreadsList.filter { threadItem ->
                val matchesCustomer = threadItem.customerName.lowercase().contains(normalizedQuery)
                val matchesMessage = threadItem.lastMessage.lowercase().contains(normalizedQuery)
                val matchesPhone = threadItem.phone.lowercase().contains(normalizedQuery)
                when {
                    matchesCustomer -> true
                    matchesMessage -> true
                    else -> matchesPhone
                }
            }
        }
    }

    val activeHandoffThread = remember(botThreadsList, selectedThreadId) {
        if (selectedThreadId == null) {
            null
        } else {
            botThreadsList.firstOrNull { threadItem -> threadItem.id == selectedThreadId }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BotScaffold(
            activeCode = "s74",
            onSelectScreenCode = onSelectScreenCode,
            action = {
                ReportIconButton(RfIconKind.SEARCH) {
                    showSearchBar = !showSearchBar
                    if (!showSearchBar) {
                        searchQuery = ""
                    }
                }
            },
        ) {
            if (!feedbackMessage.isNullOrBlank()) {
                AdminInlineToastBanner(message = feedbackMessage) {
                    viewModel.clearFormFeedback()
                }
            }
            if (showSearchBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E3EF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 16.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            BasicText(
                                "Buscar contacto o mensaje...",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF8C90A4)),
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { updatedQuery -> searchQuery = updatedQuery },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 14.dp)
                        }
                    }
                }
            }

            if (filteredThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "No se encontraron conversaciones para '$searchQuery'.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
                ) {
                    filteredThreads.forEachIndexed { threadIndex, threadItem ->
                        val initialsLabel = buildContactInitials(threadItem.customerName)
                        val statusLabel = if (threadItem.botPaused) {
                            val agentName = threadItem.assignedAgent.ifBlank { "María" }
                            "Humano · $agentName"
                        } else {
                            "Bot activo"
                        }
                        val statusBg = if (threadItem.botPaused) Color(0xFFFFF2C9) else Color(0xFFEEF6D6)
                        val statusFg = if (threadItem.botPaused) Color(0xFF7A5700) else Color(0xFF4E6A10)
                        val colorSlot = threadIndex % 3
                        val avatarBg = when (colorSlot) {
                            0 -> Color(0xFFECEBFF)
                            1 -> Color(0xFFEFE8FD)
                            else -> Color(0xFFDDF4F3)
                        }
                        val avatarFg = when (colorSlot) {
                            0 -> Color(0xFF4B47D6)
                            1 -> Color(0xFF7A4FD8)
                            else -> Color(0xFF1F8A84)
                        }
                        BotConversationRow74(
                            initials = initialsLabel,
                            name = threadItem.customerName,
                            time = threadItem.updatedAtLabel,
                            preview = threadItem.lastMessage,
                            status = statusLabel,
                            statusBackground = statusBg,
                            statusColor = statusFg,
                            phone = threadItem.phone,
                            avatarBackground = avatarBg,
                            avatarTint = avatarFg,
                            divider = threadIndex > 0,
                            onClick = { selectedThreadId = threadItem.id },
                        )
                    }
                }
            }
        }

        if (activeHandoffThread != null) {
            BotConversationHandoffModal(
                threadItem = activeHandoffThread,
                onDismiss = { selectedThreadId = null },
                onToggleBotPause = {
                    val nextPausedState = !activeHandoffThread.botPaused
                    val nextAssignedAgent = if (nextPausedState) "María" else ""
                    viewModel?.toggleBotThreadPause(activeHandoffThread.id, nextPausedState, nextAssignedAgent)
                },
                onOpenAdminChat = {
                    viewModel?.selectChatThreadForContact(activeHandoffThread.customerName)
                    selectedThreadId = null
                    onSelectScreenCode("A15")
                },
                onOpenWhatsApp = {
                    val followUpGreeting = "Hola ${activeHandoffThread.customerName}, te atendemos desde RosaFiesta."
                    viewModel?.openWhatsAppChat(activeHandoffThread.phone, followUpGreeting)
                },
            )
        }
    }
}

private fun buildContactInitials(rawFullName: String): String {
    val tokens = rawFullName
        .trim()
        .split(" ")
        .filter { word -> word.isNotBlank() }
    return when {
        tokens.size >= 2 -> "${tokens[0].take(1)}${tokens[1].take(1)}".uppercase()
        tokens.size == 1 -> tokens[0].take(2).uppercase()
        else -> "RF"
    }
}

@Composable
private fun BotConversationHandoffModal(
    threadItem: BotThreadRecord,
    onDismiss: () -> Unit,
    onToggleBotPause: () -> Unit,
    onOpenAdminChat: () -> Unit,
    onOpenWhatsApp: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val statusDescription = if (threadItem.botPaused) {
        val agentLabel = threadItem.assignedAgent.ifBlank { "María" }
        "Pausado · Atendido por $agentLabel"
    } else {
        "Bot respondiendo automáticamente"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x770E1020))
            .clickable(onClick = onDismiss)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = threadItem.customerName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "${threadItem.phone} · ${threadItem.updatedAtLabel}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F3FA))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 15.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF6F6FB))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText(
                    text = "Último mensaje recibido",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                )
                BasicText(
                    text = threadItem.lastMessage,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
                )
                BasicText(
                    text = statusDescription,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF4B47D6)),
                )
            }

            AdminModalToggleRow(
                label = "Pausar bot / Derivar a humano",
                checked = threadItem.botPaused,
                onToggle = onToggleBotPause,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReportGradient)
                        .clickable(onClick = onOpenAdminChat),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Abrir chat Admin", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .clickable(onClick = onOpenWhatsApp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 16.dp)
                        BasicText("Abrir WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)))
                    }
                }
            }
        }
    }
}

@Composable
private fun BotConversationRow74(
    initials: String,
    name: String,
    time: String,
    preview: String,
    status: String,
    statusBackground: Color,
    statusColor: Color,
    phone: String,
    avatarBackground: Color,
    avatarTint: Color,
    divider: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarBackground),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(initials, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = avatarTint))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(
                    name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
                )
                BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
            }
            BasicText(
                preview,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5F6377)),
            )
            Row(
                modifier = Modifier.padding(top = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBackground)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor))
                }
                BasicText(phone, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
            }
        }
    }
}

@Composable
private fun AdminPanelScaffold(
    activeCode: String,
    onSelectScreenCode: (String) -> Unit,
    tabScroll: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Panel",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF5F6377)))
            }
            Spacer(Modifier.height(12.dp))
            AdminTabRow(
                tabs = listOf(
                    "Resumen" to "s58",
                    "Finanzas" to "s59",
                    "Seguros" to "s60",
                    "Auditoría" to "s61",
                    "Clientes" to "s62",
                ),
                activeCode = activeCode,
                onSelectScreenCode = onSelectScreenCode,
                initialScroll = tabScroll,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
internal fun RenderInsurancePanelS60(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val policiesList = viewModel?.insurancePolicies?.collectAsState()?.value ?: CanonicalSampleData.insurancePolicies
    var selectedPolicy by remember { mutableStateOf<InsurancePolicyRecord?>(null) }

    val activePoliciesCount = remember(policiesList) {
        policiesList.count { policyItem ->
            val normalizedStatus = policyItem.status.trim().lowercase()
            normalizedStatus.contains("activ")
        }
    }
    val providerSummaryLabel = remember(policiesList) {
        val uniqueProviders = policiesList.map { policyItem -> policyItem.providerName }.distinct()
        when {
            uniqueProviders.isEmpty() -> "Cobertura corporativa RosaFiesta"
            uniqueProviders.size == 1 -> "Todas con ${uniqueProviders.first()}"
            else -> uniqueProviders.joinToString(" · ")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminPanelScaffold(activeCode = "s60", onSelectScreenCode = onSelectScreenCode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFDDF4F3))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF1F8A84), size = 22.dp)
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$activePoliciesCount pólizas activas\n") }
                        withStyle(SpanStyle(color = Color(0xFF5F6377))) { append(providerSummaryLabel) }
                    },
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 19.sp, color = Color(0xFF151729)),
                )
            }
            policiesList.forEach { policyItem ->
                val formattedCoverage = RosaFiestaEngine.formatDop(policyItem.maxCoverageDop)
                PolicyRow60(
                    code = policyItem.policyNumber,
                    coverageType = policyItem.coverageType,
                    maxCoverageLabel = formattedCoverage,
                    statusLabel = policyItem.status,
                    onClick = { selectedPolicy = policyItem },
                )
            }
        }

        val currentSelectedPolicy = selectedPolicy
        if (currentSelectedPolicy != null) {
            InsurancePolicyDetailModal(
                policyItem = currentSelectedPolicy,
                onDismiss = { selectedPolicy = null },
            )
        }
    }
}

@Composable
private fun PolicyRow60(
    code: String,
    coverageType: String,
    maxCoverageLabel: String,
    statusLabel: String,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            BasicText(
                code,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
            BasicText(
                coverageType,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
            )
            BasicText(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF5F6377))) { append("Cobertura máx. ") }
                    withStyle(SpanStyle(color = Color(0xFF151729), fontWeight = FontWeight.Bold)) { append(maxCoverageLabel) }
                },
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
            )
        }
        Box(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFEEF6D6))
                .padding(horizontal = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(statusLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
        }
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
    }
}

@Composable
private fun InsurancePolicyDetailModal(
    policyItem: InsurancePolicyRecord,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val maxCoverageFormatted = RosaFiestaEngine.formatDop(policyItem.maxCoverageDop)
    val deductibleDop = (policyItem.maxCoverageDop / 100).coerceAtLeast(5000)
    val deductibleFormatted = RosaFiestaEngine.formatDop(deductibleDop)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x770E1020))
            .clickable(onClick = onDismiss)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = policyItem.policyNumber,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = policyItem.providerName,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF5F6377)),
                    )
                }
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(policyItem.status, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6F6FB))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PolicyDetailFieldRow(label = "Aseguradora", value = policyItem.providerName)
                PolicyDetailFieldRow(label = "Número de póliza", value = policyItem.policyNumber)
                PolicyDetailFieldRow(label = "Tipo de cobertura", value = policyItem.coverageType)
                PolicyDetailFieldRow(label = "Cobertura máxima", value = maxCoverageFormatted)
                PolicyDetailFieldRow(label = "Deducible por siniestro", value = deductibleFormatted)
                PolicyDetailFieldRow(label = "Vigencia hasta", value = policyItem.validUntilLabel)
                PolicyDetailFieldRow(label = "Estado", value = policyItem.status)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ReportGradient)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Cerrar detalle", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
            }
        }
    }
}

@Composable
private fun PolicyDetailFieldRow(
    label: String,
    value: String,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        BasicText(
            text = label,
            modifier = Modifier.weight(0.45f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
        )
        BasicText(
            text = value,
            modifier = Modifier.weight(0.55f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF151729), textAlign = TextAlign.End),
        )
    }
}

@Composable
internal fun RenderAuditPanelS61(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val auditLogsList = viewModel?.auditLogs?.collectAsState()?.value ?: CanonicalSampleData.auditLogs
    var selectedAuditFilter by remember { mutableStateOf("Todos") }
    val filterOptions = remember { listOf("Todos", "Pagos", "Eventos", "Inventario", "Seguridad") }

    val filteredAuditLogs = remember(auditLogsList, selectedAuditFilter) {
        if (selectedAuditFilter == "Todos") {
            auditLogsList
        } else {
            auditLogsList.filter { auditItem ->
                classifyAuditBucket(auditItem) == selectedAuditFilter
            }
        }
    }

    AdminPanelScaffold(activeCode = "s61", onSelectScreenCode = onSelectScreenCode, tabScroll = 56.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filterOptions.forEach { optionLabel ->
                AuditChip61(
                    label = optionLabel,
                    selected = selectedAuditFilter == optionLabel,
                ) {
                    selectedAuditFilter = optionLabel
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (filteredAuditLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Sin eventos de auditoría en '$selectedAuditFilter'.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                val lastAuditIndex = filteredAuditLogs.lastIndex
                filteredAuditLogs.forEachIndexed { rowIndex, auditItem ->
                    val bucketName = classifyAuditBucket(auditItem)
                    val rowIcon = when (bucketName) {
                        "Pagos" -> RfIconKind.WALLET
                        "Eventos" -> RfIconKind.CALENDAR
                        "Inventario" -> RfIconKind.NOTE
                        "Seguridad" -> RfIconKind.SHIELD
                        else -> RfIconKind.CHECK
                    }
                    val rowIconBg = when (bucketName) {
                        "Pagos" -> Color(0xFFEEF6D6)
                        "Eventos" -> Color(0xFFECEBFF)
                        "Inventario" -> Color(0xFFEFE8FD)
                        "Seguridad" -> Color(0xFFDDF4F3)
                        else -> Color(0xFFF3F3FA)
                    }
                    val rowIconFg = when (bucketName) {
                        "Pagos" -> Color(0xFF4E6A10)
                        "Eventos" -> Color(0xFF4B47D6)
                        "Inventario" -> Color(0xFF7A4FD8)
                        "Seguridad" -> Color(0xFF1F8A84)
                        else -> Color(0xFF5F6377)
                    }
                    val actorDetail = "$bucketName · ${auditItem.actorName} (${auditItem.actorRole}) · ${auditItem.action}"
                    AuditTimelineRow61(
                        date = auditItem.createdAtLabel,
                        title = auditItem.summary,
                        category = actorDetail,
                        icon = rowIcon,
                        iconBackground = rowIconBg,
                        iconTint = rowIconFg,
                        isLast = rowIndex == lastAuditIndex,
                    )
                }
            }
        }
    }
}

private fun classifyAuditBucket(auditItem: AuditLogRecord): String {
    val combinedText = "${auditItem.entityType} ${auditItem.action} ${auditItem.summary}".lowercase()
    return when {
        combinedText.contains("payment") -> "Pagos"
        combinedText.contains("pago") -> "Pagos"
        combinedText.contains("abono") -> "Pagos"
        combinedText.contains("refund") -> "Pagos"
        combinedText.contains("reembolso") -> "Pagos"
        combinedText.contains("article") -> "Inventario"
        combinedText.contains("inventory") -> "Inventario"
        combinedText.contains("inventario") -> "Inventario"
        combinedText.contains("stock") -> "Inventario"
        combinedText.contains("damage") -> "Inventario"
        combinedText.contains("auth") -> "Seguridad"
        combinedText.contains("login") -> "Seguridad"
        combinedText.contains("staff") -> "Seguridad"
        combinedText.contains("permiso") -> "Seguridad"
        combinedText.contains("role") -> "Seguridad"
        combinedText.contains("security") -> "Seguridad"
        combinedText.contains("seguridad") -> "Seguridad"
        else -> "Eventos"
    }
}

@Composable
private fun AuditChip61(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .height(36.dp)
            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(999.dp), spotColor = Color(0x995B6CF0))
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ReportGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (selected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF151729),
            ),
        )
    }
}

@Composable
private fun AuditTimelineRow61(
    date: String,
    title: String,
    category: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    isLast: Boolean,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.width(40.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = icon, tint = iconTint, size = 18.dp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .padding(vertical = 6.dp)
                        .background(Color(0xFFE3E4EF)),
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(top = 2.dp, bottom = 22.dp)) {
            BasicText(
                date.uppercase(),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.7.sp, color = Color(0xFF5F6377)),
            )
            Spacer(Modifier.height(4.dp))
            BasicText(
                title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
            )
            Spacer(Modifier.height(2.dp))
            BasicText(category, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
        }
    }
}

@Composable
internal fun RenderClientsAnalyticsS62(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val clientsList = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
    val selectedClientIdValue = viewModel?.selectedClientId?.collectAsState()?.value.orEmpty()
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val allPayments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments

    val activeClient = remember(clientsList, selectedClientIdValue) {
        val matchedById = clientsList.firstOrNull { clientOption -> clientOption.id == selectedClientIdValue }
        matchedById ?: viewModel?.selectedClient() ?: clientsList.firstOrNull() ?: CanonicalSampleData.clients.first()
    }

    val clientEvents = remember(allEvents, activeClient) {
        allEvents.filter { eventItem ->
            val matchesFullName = eventItem.clientName.equals(activeClient.fullName, ignoreCase = true)
            val hasClientEmail = activeClient.email.isNotBlank()
            val matchesEmail = if (hasClientEmail) {
                eventItem.clientEmail.equals(activeClient.email, ignoreCase = true)
            } else {
                false
            }
            if (matchesFullName) true else matchesEmail
        }
    }

    val clientPayments = remember(allPayments, activeClient) {
        allPayments.filter { paymentItem ->
            paymentItem.clientName.equals(activeClient.fullName, ignoreCase = true)
        }
    }

    val computedEventsCount = if (clientEvents.isNotEmpty()) {
        clientEvents.size
    } else {
        activeClient.eventsCount
    }
    val eventsTotalDop = clientEvents.sumOf { eventItem -> eventItem.totalDop }
    val paymentsTotalDop = clientPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val computedLtvDop = when {
        eventsTotalDop > 0 -> eventsTotalDop.coerceAtLeast(activeClient.totalContractedDop)
        paymentsTotalDop > 0 -> paymentsTotalDop.coerceAtLeast(activeClient.totalContractedDop)
        else -> activeClient.totalContractedDop
    }
    val formattedLtvLabel = RosaFiestaEngine.formatDop(computedLtvDop)
    val formattedPendingLabel = RosaFiestaEngine.formatDop(activeClient.pendingBalanceDop)
    val clientInitial = buildContactInitials(activeClient.fullName)
    val nextClientEvent = clientEvents.firstOrNull()
    val whatsappGreeting = "Hola ${activeClient.fullName}, te escribimos de RosaFiesta"

    AdminPanelScaffold(activeCode = "s62", onSelectScreenCode = onSelectScreenCode, tabScroll = 56.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            clientsList.forEach { clientOption ->
                val isSelectedClient = clientOption.id == activeClient.id
                AuditChip61(
                    label = clientOption.fullName,
                    selected = isSelectedClient,
                ) {
                    viewModel?.selectClient(clientOption.id)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFECEBFF)),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(clientInitial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4B47D6)))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    activeClient.fullName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF151729)),
                )
                BasicText(
                    activeClient.email,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377)),
                )
                val zoneVipMeta = listOf(activeClient.zone, activeClient.vipTag)
                    .filter { metaPart -> metaPart.isNotBlank() }
                    .joinToString(" · ")
                if (zoneVipMeta.isNotEmpty()) {
                    BasicText(
                        zoneVipMeta,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF7A4FD8)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF6D6))
                    .clickable {
                        viewModel?.openWhatsAppChat(activeClient.phone, whatsappGreeting)
                    },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 20.dp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ClientMetricTile62(
                modifier = Modifier.weight(1f),
                label = "LTV contratado",
                value = formattedLtvLabel,
                accentColor = Color(0xFF4B47D6),
            )
            ClientMetricTile62(
                modifier = Modifier.weight(1f),
                label = "Eventos",
                value = "$computedEventsCount",
                accentColor = Color(0xFF7A4FD8),
            )
            ClientMetricTile62(
                modifier = Modifier.weight(1f),
                label = "Por cobrar",
                value = formattedPendingLabel,
                accentColor = Color(0xFF7A5700),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFEEF6D6))
                .clickable {
                    viewModel?.openWhatsAppChat(activeClient.phone, whatsappGreeting)
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                BasicText(
                    "Contactar por WhatsApp",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4E6A10)),
                )
            }
        }

        BasicText(
            "PRÓXIMO EVENTO",
            modifier = Modifier.padding(top = 6.dp),
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
        )

        val upcomingTitle = nextClientEvent?.title ?: "Historial de ${activeClient.fullName}"
        val upcomingSubtitle = nextClientEvent?.let { eventRecord ->
            "${eventRecord.dateLabel} · ${eventRecord.venue}"
        } ?: "$computedEventsCount eventos registrados · ${activeClient.zone}"
        val upcomingDayDigits = nextClientEvent?.dateLabel
            ?.trim()
            ?.takeWhile { character -> character.isDigit() }
            ?.ifBlank { "23" } ?: "—"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFF2C9))
                .clickable {
                    if (nextClientEvent != null) {
                        viewModel?.selectEvent(nextClientEvent.id)
                    }
                    onSelectScreenCode("A13")
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.width(52.dp).height(56.dp).clip(RoundedCornerShape(14.dp)).background(RfThemeState.currentPalette.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BasicText(upcomingDayDigits, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 21.sp, color = Color(0xFF151729)))
                BasicText("EVT", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.7.sp, color = Color(0xFF151729)))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    upcomingTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF151729)),
                )
                BasicText(
                    upcomingSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF7A5700)),
                )
            }
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF7A5700), size = 18.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
        ) {
            ClientActionRow62(
                label = "Eventos del cliente",
                icon = RfIconKind.CALENDAR,
                iconBackground = Color(0xFFEFE8FD),
                iconTint = Color(0xFF7A4FD8),
                badge = computedEventsCount.toString(),
                divider = false,
                onClick = { onSelectScreenCode("A11") },
            )
            val formattedPaymentsTotalLabel = RosaFiestaEngine.formatDop(paymentsTotalDop)
            ClientActionRow62(
                label = "Pagos (${clientPayments.size})",
                icon = RfIconKind.WALLET,
                iconBackground = Color(0xFFEEF6D6),
                iconTint = Color(0xFF4E6A10),
                badge = formattedPaymentsTotalLabel,
                divider = true,
                onClick = { onSelectScreenCode("s59") },
            )
            ClientActionRow62(
                label = "Ficha del cliente",
                icon = RfIconKind.BELL,
                iconBackground = Color(0xFFDDF4F3),
                iconTint = Color(0xFF1F8A84),
                divider = true,
                onClick = {
                    viewModel?.selectClient(activeClient.id)
                    onSelectScreenCode("A27")
                },
            )
        }
    }
}

@Composable
private fun ClientMetricTile62(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
        )
        BasicText(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = accentColor),
        )
    }
}

@Composable
private fun ClientActionRow62(
    label: String,
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    badge: String? = null,
    divider: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 18.dp)
        }
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF151729)),
        )
        if (badge != null) {
            BasicText(badge, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF5F6377)))
        }
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 16.dp)
    }
}
