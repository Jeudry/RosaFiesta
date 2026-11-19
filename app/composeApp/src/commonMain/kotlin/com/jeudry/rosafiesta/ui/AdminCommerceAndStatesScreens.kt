package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.designsystem.rfTrackValidationFocus
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChatMessageRecord
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DeliveryZone
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.validation.InputValidator
import com.jeudry.rosafiesta.domain.validation.MaskedTextCursorResult
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

private val AdminHeroGradient = Brush.linearGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
)

private val AdminSoftBannerGradient = Brush.linearGradient(
    listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6)),
)

private val AdminCardShape20 = RoundedCornerShape(20.dp)
private val AdminHeroShape22 = RoundedCornerShape(22.dp)
private val AdminInputShape14 = RoundedCornerShape(14.dp)
private val AdminButtonShape16 = RoundedCornerShape(16.dp)
private val AdminSheetShape28 = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val AdminPillShape = RoundedCornerShape(999.dp)

private const val ADMIN_MAX_DELIVERY_FEE_DOP: Int = 50_000

private fun bothTrue(first: Boolean, second: Boolean): Boolean = if (first) second else false

private fun eitherTrue(first: Boolean, second: Boolean): Boolean = if (first) true else second

private fun allTrue(vararg flags: Boolean): Boolean {
    for (flag in flags) {
        if (!flag) {
            return false
        }
    }
    return true
}

private fun firstValidationError(vararg errors: String?): String? = errors.firstOrNull { error -> error != null }

private fun validateAdminEventDateText(rawDate: String): String? {
    val trimmed = rawDate.trim()
    if (trimmed.isEmpty()) return "La fecha del evento es obligatoria."
    val hasSlash = trimmed.contains('/')
    val allDigits = trimmed.all { character -> character.isDigit() }
    if (hasSlash) return InputValidator.validateEventDate(trimmed, isRequired = true)
    if (allDigits) return InputValidator.validateEventDate(trimmed, isRequired = true)
    val hasDigit = trimmed.any { character -> character.isDigit() }
    val hasLetter = trimmed.any { character -> character.isLetter() }
    val lowerText = trimmed.lowercase()
    val spanishMonths = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    val hasSpanishMonth = spanishMonths.any { token -> lowerText.contains(token) }
    val isReadableLabel = allTrue(trimmed.length in 4..40, hasDigit, hasLetter, hasSpanishMonth)
    if (isReadableLabel) return null
    return InputValidator.validateEventDate(trimmed, isRequired = true)
}

private fun toRfPhotoKey(asset: PhotoAsset): RfPhotoKey = when (asset) {
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

private data class DuplicateEventOption(
    val id: String,
    val title: String,
    val clientName: String,
    val dateLabel: String,
    val venue: String,
    val guests: Int,
)

private fun fallbackDuplicateEventOptions(): List<DuplicateEventOption> = listOf(
    DuplicateEventOption(
        id = "evt-boda-real",
        title = "Boda Real Rosa & Dorado",
        clientName = "Camila Rodríguez",
        dateLabel = "23/10/2026",
        venue = "Jardín Botánico",
        guests = 120,
    ),
    DuplicateEventOption(
        id = "evt-cumple-mateo",
        title = "Cumpleaños de Mateo",
        clientName = "Laura Peña",
        dateLabel = "07/11/2026",
        venue = "Terraza Los Prados",
        guests = 40,
    ),
    DuplicateEventOption(
        id = "evt-quince-valentina",
        title = "Quinceañera Valentina Crystal",
        clientName = "Ana Gómez",
        dateLabel = "02/11/2026",
        venue = "Hotel Embajador",
        guests = 85,
    ),
)

private fun fallbackClientsList(): List<ClientProfile> = listOf(
    ClientProfile(
        id = "cli-laura",
        fullName = "Laura Peña",
        email = "laura@rosafiesta.do",
        phone = "(809) 555-0142",
        zone = "Distrito Nacional",
        eventsCount = 2,
        totalContractedDop = 1850,
        pendingBalanceDop = 405,
        vipTag = "Frecuente",
    ),
    ClientProfile(
        id = "cli-camila",
        fullName = "Camila Rodríguez",
        email = "camila@rosafiesta.do",
        phone = "(809) 555-0188",
        zone = "Piantini",
        eventsCount = 3,
        totalContractedDop = 4500,
        pendingBalanceDop = 3150,
        vipTag = "VIP",
    ),
    ClientProfile(
        id = "cli-ana",
        fullName = "Ana Gómez",
        email = "ana@rosafiesta.do",
        phone = "(829) 555-0911",
        zone = "Naco",
        eventsCount = 1,
        totalContractedDop = 2300,
        pendingBalanceDop = 642,
        vipTag = "Corporativo",
    ),
)

private fun fallbackCatalogArticles(): List<CatalogArticle> = listOf(
    CatalogArticle(
        id = "art-silla-tiffany",
        code = "RF-001",
        name = "Silla Tiffany Cristal",
        category = "Mobiliario",
        description = "Silla en resina cristal con cojín marfil",
        pricePerDayDop = 4,
        totalStock = 120,
        availableOct23 = 80,
        photo = PhotoAsset.SILLA_TIFFANY,
        variants = listOf("Cristal", "Dorada"),
    ),
    CatalogArticle(
        id = "art-mesa-redonda",
        code = "RF-003",
        name = "Mesa redonda para 8",
        category = "Mesas",
        description = "Mesa plegable reforzada de 60 pulgadas",
        pricePerDayDop = 25,
        totalStock = 12,
        availableOct23 = 10,
        photo = PhotoAsset.MESA_REDONDA,
        variants = emptyList(),
    ),
    CatalogArticle(
        id = "art-arco-rosa",
        code = "RF-005",
        name = "Arco orgánico de globos",
        category = "Globos",
        description = "Estructura circular con globos biodegradables",
        pricePerDayDop = 95,
        totalStock = 3,
        availableOct23 = 2,
        photo = PhotoAsset.ARCO_ROSA,
        variants = listOf("Rosa pastel", "Dorado"),
    ),
    CatalogArticle(
        id = "art-mesa-dulce",
        code = "RF-006",
        name = "Mesa dulce temática",
        category = "Decoración",
        description = "Estación principal de postres y exhibidores",
        pricePerDayDop = 150,
        totalStock = 2,
        availableOct23 = 1,
        photo = PhotoAsset.MESA_HELADOS,
        variants = listOf("Blanco", "Madera"),
    ),
    CatalogArticle(
        id = "art-centro-floral",
        code = "RF-004",
        name = "Centro de mesa floral",
        category = "Decoración",
        description = "Arreglo con base dorada y follaje natural",
        pricePerDayDop = 35,
        totalStock = 24,
        availableOct23 = 20,
        photo = PhotoAsset.CENTRO_FLORAL,
        variants = listOf("Clásico"),
    ),
)

@Composable
private fun AdminInlineErrorBanner(message: String) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AdminInputShape14)
            .background(Color(0xFFFDE3EA))
            .border(1.dp, Color(0xFFF6B8C8), AdminInputShape14)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 16.dp)
            BasicText(
                text = message,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB42345),
                ),
            )
        }
    }
}

@Composable
private fun AdminWizardProgressHeader(
    activeStep: Int,
    onSelectStepOne: () -> Unit,
    onSelectStepTwo: () -> Unit,
    onSelectStepThree: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val stepTwoFilled = activeStep >= 2
    val stepThreeFilled = activeStep >= 3
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AdminButtonGradient)
                    .clickable(onClick = onSelectStepOne),
            )
            val stepTwoBg = if (stepTwoFilled) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepTwoBg)
                    .clickable(onClick = onSelectStepTwo),
            )
            val stepThreeBg = if (stepThreeFilled) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepThreeBg)
                    .clickable(onClick = onSelectStepThree),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            val isStepOne = activeStep == 1
            val isStepTwo = activeStep == 2
            val isStepThree = activeStep == 3
            BasicText(
                text = "Datos",
                modifier = Modifier.weight(1f).clickable(onClick = onSelectStepOne),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isStepOne) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isStepOne) Color(0xFF4B47D6) else Color(0xFF5F6377),
                ),
            )
            BasicText(
                text = "Artículos",
                modifier = Modifier.weight(1f).clickable(onClick = onSelectStepTwo),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isStepTwo) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isStepTwo) Color(0xFF4B47D6) else Color(0xFF5F6377),
                ),
            )
            BasicText(
                text = "Resumen",
                modifier = Modifier.weight(1f).clickable(onClick = onSelectStepThree),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isStepThree) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isStepThree) Color(0xFF4B47D6) else Color(0xFF5F6377),
                ),
            )
        }
    }
}

@Composable
private fun RenderUnderlyingScreenForCreateEventA04(
    originScreenCode: String,
    viewModel: RosaFiestaViewModel?,
) {
    val normalizedCode = originScreenCode.trim().uppercase()
    Box(modifier = Modifier.fillMaxSize()) {
        when (normalizedCode) {
            "A11" -> RenderAdminEventsA11(viewModel = viewModel, onSelectScreenCode = {})
            "A35" -> RenderAdminEmptyStateA35(viewModel = viewModel, onSelectScreenCode = {})
            "A02" -> RenderAdminRequestsA02(viewModel = viewModel, onSelectScreenCode = {})
            "A09" -> RenderAdminInventoryA09(viewModel = viewModel, onSelectScreenCode = {})
            "A12" -> RenderAdminMoreA12(viewModel = viewModel, onSelectScreenCode = {})
            else -> RenderAdminHomeA01(viewModel = viewModel, onSelectScreenCode = {})
        }
        if (shouldShowBottomBar(normalizedCode)) {
            RenderAppBottomBar(
                spec = ScreenCatalog.findByCode(normalizedCode),
                onSelectScreenCode = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun RenderAdminCreateEventA04(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    underlyingScreenCode: String? = null,
) {
    val typo = LocalRfTypography.current
    val originScreenCode = underlyingScreenCode?.trim()?.ifEmpty { "A01" } ?: "A01"
    var showDuplicateModal by remember { mutableStateOf(false) }
    val scrimInteractionSource = remember { MutableInteractionSource() }
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val vmEvents = viewModel?.events?.collectAsState()?.value
    val eventChoices = remember(vmEvents) {
        if (vmEvents != null) {
            vmEvents.map { eventItem ->
                DuplicateEventOption(
                    id = eventItem.id,
                    title = eventItem.title,
                    clientName = eventItem.clientName,
                    dateLabel = eventItem.dateLabel,
                    venue = eventItem.venue,
                    guests = eventItem.guests,
                )
            }
        } else {
            fallbackDuplicateEventOptions()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RenderUnderlyingScreenForCreateEventA04(
            originScreenCode = originScreenCode,
            viewModel = viewModel,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                ) {
                    if (showDuplicateModal) {
                        showDuplicateModal = false
                    } else {
                        onSelectScreenCode("BACK:$originScreenCode")
                    }
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .fillMaxWidth()
                .clip(AdminSheetShape28)
                .background(Color.White)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 34.dp),
        ) {
            if (!showDuplicateModal) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE3E4EF)),
                    )

                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        BasicText(
                            text = "Nuevo evento",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            text = "¿Cómo quieres crearlo?",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(AdminSoftBannerGradient)
                            .clickable { onSelectScreenCode("A08") }
                            .padding(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            RosaAvatar(size = RosaAvatarSize.LARGE, customSize = 48.dp, forceAdminPalette = true)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BasicText(
                                        text = "Con Rosa IA",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                    )
                                    Box(
                                        modifier = Modifier.height(20.dp).background(Color(0xFFFFD45C), CircleShape).padding(horizontal = 7.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "Más rápido",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E1A47)),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                BasicText(
                                    text = "Describe el evento y Rosa arma el borrador con disponibilidad real",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), AdminCardShape20)
                            .clickable {
                                viewModel?.startNewManualEvent()
                                onSelectScreenCode("A05")
                            }
                            .padding(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(AdminInputShape14).background(Color(0xFFECEBFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF4B47D6), size = 22.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = "Crear evento desde cero",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                                Spacer(Modifier.height(2.dp))
                                BasicText(
                                    text = "Paso a paso: cliente, fecha, lugar y artículos",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), AdminCardShape20)
                            .clickable { showDuplicateModal = true }
                            .padding(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(AdminInputShape14).background(Color(0xFFDDF4F3)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF1F8A84), size = 22.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = "Duplicar un evento",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                                Spacer(Modifier.height(2.dp))
                                BasicText(
                                    text = "Parte de uno que ya hiciste",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onSelectScreenCode("BACK:$originScreenCode") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cancelar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Seleccionar evento a duplicar",
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                            )
                            BasicText(
                                text = "Toca un evento para clonar sus datos y artículos",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable { showDuplicateModal = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF2E324A), size = 18.dp)
                        }
                    }

                    if (eventChoices.isEmpty()) {
                        BasicText(
                            text = "No hay eventos previos disponibles para duplicar.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            eventChoices.forEach { eventChoice ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(AdminButtonShape16)
                                        .background(Color(0xFFF8F8FC))
                                        .border(1.dp, Color(0xFFE5E6F2), AdminButtonShape16)
                                        .clickable {
                                            val clonedEvent = viewModel?.duplicateExistingEvent(eventChoice.id)
                                            val resolvedTitle = clonedEvent?.title ?: "${eventChoice.title} (Copia)"
                                            val resolvedClient = clonedEvent?.clientName ?: eventChoice.clientName
                                            val resolvedDate = clonedEvent?.dateLabel ?: eventChoice.dateLabel
                                            val resolvedVenue = clonedEvent?.venue ?: eventChoice.venue
                                            val resolvedGuests = clonedEvent?.guests ?: eventChoice.guests
                                            viewModel?.setNewEventTitle(resolvedTitle)
                                            viewModel?.setNewEventClient(resolvedClient)
                                            viewModel?.setNewEventDate(resolvedDate)
                                            viewModel?.setNewEventVenue(resolvedVenue)
                                            viewModel?.setNewEventGuests(resolvedGuests.toString())
                                            if (clonedEvent != null) {
                                                viewModel.selectEvent(clonedEvent.id)
                                            }
                                            showDuplicateModal = false
                                            onSelectScreenCode("A05")
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFECEBFF)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.COPY, tint = Color(0xFF4B47D6), size = 18.dp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            BasicText(
                                                text = eventChoice.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                            )
                                            BasicText(
                                                text = "${eventChoice.clientName} · ${formatReadableSpanishDateText(eventChoice.dateLabel)} · ${eventChoice.guests} inv.",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF4B47D6), size = 18.dp)
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clickable { showDuplicateModal = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Volver a opciones",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminManualEventA05(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var fallbackTitle by remember { mutableStateOf("") }
    var fallbackClient by remember { mutableStateOf("") }
    var fallbackDate by remember { mutableStateOf("") }
    var fallbackVenue by remember { mutableStateOf("") }
    var fallbackGuests by remember { mutableStateOf("") }

    val vmTitle = viewModel?.newEventTitle?.collectAsState()?.value
    val vmClient = viewModel?.newEventClient?.collectAsState()?.value
    val vmDate = viewModel?.newEventDate?.collectAsState()?.value
    val vmVenue = viewModel?.newEventVenue?.collectAsState()?.value
    val vmGuests = viewModel?.newEventGuests?.collectAsState()?.value
    val vmClients = viewModel?.clients?.collectAsState()?.value
    val vmError = viewModel?.formError?.collectAsState()?.value
    val dateAvailabilityWarning = viewModel?.dateAvailabilityWarning?.collectAsState()?.value

    val titleValue = vmTitle ?: fallbackTitle
    val clientValue = vmClient ?: fallbackClient
    val dateValue = vmDate ?: fallbackDate
    val venueValue = vmVenue ?: fallbackVenue
    val guestsValue = vmGuests ?: fallbackGuests
    val clientsList = vmClients ?: emptyList()

    var selectedType by remember { mutableStateOf("Cumpleaños") }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var submitAttempted by remember { mutableStateOf(false) }
    var titleTouched by remember { mutableStateOf(false) }
    var clientTouched by remember { mutableStateOf(false) }
    var dateTouched by remember { mutableStateOf(false) }
    var timeTouched by remember { mutableStateOf(false) }
    var venueTouched by remember { mutableStateOf(false) }
    var guestsTouched by remember { mutableStateOf(false) }
    var notesTouched by remember { mutableStateOf(false) }
    val titleBlur = rememberFieldBlurState()
    val clientBlur = rememberFieldBlurState()
    val dateBlur = rememberFieldBlurState()
    val timeBlur = rememberFieldBlurState()
    val venueBlur = rememberFieldBlurState()
    val guestsBlur = rememberFieldBlurState()
    val notesBlur = rememberFieldBlurState()

    val updateTitle: (String) -> Unit = { updatedText ->
        titleTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        val cappedText = updatedText.take(InputValidator.MAX_EVENT_NAME_LENGTH)
        if (viewModel != null) {
            viewModel.setNewEventTitle(cappedText)
        } else {
            fallbackTitle = cappedText
        }
    }
    val updateClient: (String) -> Unit = { updatedText ->
        clientTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        val cappedText = updatedText.take(InputValidator.MAX_FULL_NAME_LENGTH)
        if (viewModel != null) {
            viewModel.setNewEventClient(cappedText)
        } else {
            fallbackClient = cappedText
        }
    }
    val updateDate: (String) -> Unit = { updatedText ->
        dateTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        if (viewModel != null) {
            viewModel.setNewEventDate(updatedText)
        } else {
            fallbackDate = updatedText
        }
    }
    val updateVenue: (String) -> Unit = { updatedText ->
        venueTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        val cappedText = updatedText.take(InputValidator.MAX_ADDRESS_LENGTH)
        if (viewModel != null) {
            viewModel.setNewEventVenue(cappedText)
        } else {
            fallbackVenue = cappedText
        }
    }
    val updateGuests: (String) -> Unit = { updatedText ->
        guestsTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        val digitsOnly = InputValidator.filterDigitsOnly(updatedText, 5)
        if (viewModel != null) {
            viewModel.setNewEventGuests(digitsOnly)
        } else {
            fallbackGuests = digitsOnly
        }
    }
    val updateTime: (String) -> Unit = { updatedText ->
        timeTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        timeText = InputValidator.filterTimeInput(updatedText)
    }
    val updateNotes: (String) -> Unit = { updatedText ->
        notesTouched = true
        localValidationError = null
        viewModel?.clearFormError()
        notesText = updatedText.take(InputValidator.MAX_NOTES_LENGTH)
    }

    val titleError = rememberDebouncedValidationError(
        value = titleValue,
        hasInteracted = titleTouched,
        forceShowError = submitAttempted,
        hasBlurred = titleBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateEventName(candidate, isRequired = true)
    }
    val clientError = rememberDebouncedValidationError(
        value = clientValue,
        hasInteracted = clientTouched,
        forceShowError = submitAttempted,
        hasBlurred = clientBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "El nombre del cliente",
            minLength = InputValidator.MIN_NAME_LENGTH,
            maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
        )
    }
    val dateError = rememberDebouncedValidationError(
        value = dateValue,
        hasInteracted = dateTouched,
        forceShowError = submitAttempted,
        hasBlurred = dateBlur.hasBlurred,
    ) { candidate ->
        validateAdminEventDateText(candidate)
    }
    val timeError = rememberDebouncedValidationError(
        value = timeText,
        hasInteracted = timeTouched,
        forceShowError = submitAttempted,
        hasBlurred = timeBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateTime24(candidate, "La hora del evento")
    }
    val venueError = rememberDebouncedValidationError(
        value = venueValue,
        hasInteracted = venueTouched,
        forceShowError = submitAttempted,
        hasBlurred = venueBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateAddress(candidate, "El lugar del evento")
    }
    val guestsError = rememberDebouncedValidationError(
        value = guestsValue,
        hasInteracted = guestsTouched,
        forceShowError = submitAttempted,
        hasBlurred = guestsBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateGuestCount(candidate)
    }
    val notesError = rememberDebouncedValidationError(
        value = notesText,
        hasInteracted = notesTouched,
        forceShowError = submitAttempted,
        hasBlurred = notesBlur.hasBlurred,
    ) { candidate ->
        InputValidator.validateNotes(
            rawValue = candidate,
            fieldLabel = "Las notas internas",
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }

    val advanceIfStepOneValid: (String) -> Unit = { targetCode ->
        submitAttempted = true
        val firstError = firstValidationError(
            InputValidator.validateEventName(titleValue, isRequired = true),
            InputValidator.validateEntityName(
                rawValue = clientValue,
                fieldLabel = "El nombre del cliente",
                minLength = InputValidator.MIN_NAME_LENGTH,
                maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
            ),
            validateAdminEventDateText(dateValue),
            InputValidator.validateTime24(timeText, "La hora del evento"),
            InputValidator.validateAddress(venueValue, "El lugar del evento"),
            InputValidator.validateGuestCount(guestsValue),
            InputValidator.validateNotes(
                rawValue = notesText,
                fieldLabel = "Las notas internas",
                maxLength = InputValidator.MAX_NOTES_LENGTH,
            ),
        )
        if (firstError == null) {
            localValidationError = null
            viewModel?.clearFormError()
            onSelectScreenCode(targetCode)
        } else {
            localValidationError = firstError
            viewModel?.postError(firstError)
        }
    }

    val displayedError = localValidationError ?: vmError
    val parsedGuestsDisplay = guestsValue.trim().toIntOrNull() ?: 0
    val clientInitial = InputValidator.extractInitials(clientValue, fallback = "C", maxInitials = 1)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("BACK:A04") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Nuevo evento",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "Paso 1 de 3",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }

                AdminWizardProgressHeader(
                    activeStep = 1,
                    onSelectStepOne = {},
                    onSelectStepTwo = { advanceIfStepOneValid("A06") },
                    onSelectStepThree = { advanceIfStepOneValid("A07") },
                )

                if (displayedError != null) {
                    AdminInlineErrorBanner(message = displayedError)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Nombre del evento",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(AdminInputShape14, isAdmin = true)
                            .clip(AdminInputShape14)
                            .background(Color.White)
                            .border(1.dp, if (titleError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicTextField(
                                value = titleValue,
                                onValueChange = updateTitle,
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .rfTrackValidationFocus(focusKey = titleBlur) { focused ->
                                        titleBlur.onFocusStateChanged(focused) { titleTouched = true }
                                    },
                                decorationBox = { innerTextField ->
                                    if (titleValue.isEmpty()) {
                                        BasicText(
                                            text = "Ej. Cumpleaños de Mateo",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }
                    RfValidationErrorText(errorMessage = titleError)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Cliente",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(AdminInputShape14, isAdmin = true)
                            .clip(AdminInputShape14)
                            .background(Color.White)
                            .border(1.dp, if (clientError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(start = 8.dp, end = 12.dp),
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
                                BasicText(
                                    text = clientInitial,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RfColors.VioletaTintInk),
                                )
                            }

                            BasicTextField(
                                value = clientValue,
                                onValueChange = updateClient,
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .rfTrackValidationFocus(focusKey = clientBlur) { focused ->
                                        clientBlur.onFocusStateChanged(focused) { clientTouched = true }
                                    },
                                decorationBox = { innerTextField ->
                                    if (clientValue.isEmpty()) {
                                        BasicText(
                                            text = "Nombre del cliente",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }
                    RfValidationErrorText(errorMessage = clientError)

                    if (clientsList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            clientsList.forEach { clientItem ->
                                val isSelectedClient = clientValue.trim().equals(clientItem.fullName.trim(), ignoreCase = true)
                                val chipBg = if (isSelectedClient) AdminButtonGradient else SolidColor(Color.White)
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(AdminPillShape)
                                        .background(chipBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelectedClient) Color.Transparent else Color(0xFFE9E9F2),
                                            shape = AdminPillShape,
                                        )
                                        .clickable { updateClient(clientItem.fullName) }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = clientItem.fullName,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelectedClient) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelectedClient) Color.White else Color(0xFF2E324A),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText(
                        text = "Tipo de evento",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    val typeRows = listOf(
                        listOf("Boda" to RfIconKind.STAR, "Cumpleaños" to RfIconKind.SPARK),
                        listOf("Baby shower" to RfIconKind.STAR, "Corporativo" to RfIconKind.BOX),
                        listOf("Otro" to RfIconKind.SPARK),
                    )
                    typeRows.forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { typePair ->
                                val label = typePair.first
                                val iconKind = typePair.second
                                val isSelected = selectedType == label
                                val typeBg = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(typeBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                            shape = CircleShape,
                                        )
                                        .clickable { selectedType = label }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        RfIcon(kind = iconKind, tint = if (isSelected) Color.White else Color(0xFF5F6377), size = 16.dp)
                                        BasicText(
                                            text = label,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF2E324A),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Fecha",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .rfInputShadow(AdminInputShape14, isAdmin = true)
                                .clip(AdminInputShape14)
                                .background(Color.White)
                                .border(1.dp, if (dateError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                                .padding(start = 12.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            RfMaskedDateField(
                                value = dateValue,
                                onValueChange = updateDate,
                                onOpenCalendar = { showDatePickerModal = true },
                                placeholder = "Elegir fecha",
                                isAdmin = true,
                                compactInline = true,
                                embeddedInContainer = true,
                                hasError = dateError != null,
                                onFocusChanged = { focused ->
                                    dateBlur.onFocusStateChanged(focused) { dateTouched = true }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        RfValidationErrorText(errorMessage = dateError)
                        LaunchedEffect(dateValue) {
                            val trimmedDate = dateValue.trim()
                            if (trimmedDate.length >= 8) {
                                viewModel?.checkDraftAvailabilityForDate(trimmedDate)
                            }
                        }
                        if (!dateAvailabilityWarning.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF2C9))
                                    .border(1.dp, Color(0xFFD9A441), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                BasicText(
                                    text = dateAvailabilityWarning,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7A5700)),
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Hora",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .rfInputShadow(AdminInputShape14, isAdmin = true)
                                .clip(AdminInputShape14)
                                .background(Color.White)
                                .border(1.dp, if (timeError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF5F6377), size = 18.dp)
                                BasicTextField(
                                    value = timeText,
                                    onValueChange = updateTime,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .rfTrackValidationFocus(focusKey = timeBlur) { focused ->
                                            timeBlur.onFocusStateChanged(focused) { timeTouched = true }
                                        },
                                    decorationBox = { innerTextField ->
                                        if (timeText.isEmpty()) {
                                            BasicText(
                                                text = "16:00",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }
                        RfValidationErrorText(errorMessage = timeError)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Lugar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(AdminInputShape14, isAdmin = true)
                            .clip(AdminInputShape14)
                            .background(Color.White)
                            .border(1.dp, if (venueError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicTextField(
                                value = venueValue,
                                onValueChange = updateVenue,
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .rfTrackValidationFocus(focusKey = venueBlur) { focused ->
                                        venueBlur.onFocusStateChanged(focused) { venueTouched = true }
                                    },
                                decorationBox = { innerTextField ->
                                    if (venueValue.isEmpty()) {
                                        BasicText(
                                            text = "Dirección o nombre del local",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }
                    }
                    RfValidationErrorText(errorMessage = venueError)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .rfInputShadow(AdminInputShape14, isAdmin = true)
                            .clip(AdminInputShape14)
                            .background(Color.White)
                            .border(1.dp, if (guestsError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(horizontal = 14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                RfIcon(kind = RfIconKind.USER, tint = Color(0xFF5F6377), size = 18.dp)
                                BasicText(
                                    text = "Invitados",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF2F2F8))
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                val nextGuests = (parsedGuestsDisplay - 5).coerceAtLeast(5)
                                                updateGuests(nextGuests.toString())
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "−",
                                            style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold),
                                        )
                                    }
                                    BasicTextField(
                                        value = guestsValue,
                                        onValueChange = updateGuests,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            fontFamily = typo.displayFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E324A),
                                            textAlign = TextAlign.Center,
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                        modifier = Modifier
                                            .width(46.dp)
                                            .rfTrackValidationFocus(focusKey = guestsBlur) { focused ->
                                                guestsBlur.onFocusStateChanged(focused) { guestsTouched = true }
                                            },
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                val nextGuests = (parsedGuestsDisplay + 5).coerceAtMost(InputValidator.MAX_GUEST_COUNT)
                                                updateGuests(nextGuests.toString())
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "+",
                                            style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    RfValidationErrorText(errorMessage = guestsError)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Notas internas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .rfInputShadow(AdminInputShape14, isAdmin = true)
                            .clip(AdminInputShape14)
                            .background(Color.White)
                            .border(1.dp, if (notesError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        BasicTextField(
                            value = notesText,
                            onValueChange = updateNotes,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier
                                .fillMaxSize()
                                .rfTrackValidationFocus(focusKey = notesBlur) { focused ->
                                    notesBlur.onFocusStateChanged(focused) { notesTouched = true }
                                },
                            decorationBox = { innerTextField ->
                                if (notesText.isEmpty()) {
                                    BasicText(
                                        text = "Notas opcionales de montaje, horario o paleta...",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                    RfValidationErrorText(errorMessage = notesError)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(AdminButtonGradient)
                        .clickable { advanceIfStepOneValid("A06") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            text = "Siguiente: artículos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                        )
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }

        if (showDatePickerModal) {
            RfInteractiveCalendarModal(
                title = "Fecha del evento",
                currentDateText = dateValue,
                isAdmin = true,
                dateMode = RfCalendarDateMode.EVENT_UPCOMING,
                onDismiss = { showDatePickerModal = false },
                onSelectDate = { formattedDate ->
                    updateDate(formattedDate)
                    showDatePickerModal = false
                },
            )
        }
    }
}

@Composable
internal fun RenderAdminEventArticlesA06(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var searchTouched by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var fallbackQuantities by remember {
        mutableStateOf(
            mapOf(
                "art-silla-tiffany" to 40,
                "art-mesa-redonda" to 5,
                "art-arco-rosa" to 1,
                "art-mesa-dulce" to 1,
            ),
        )
    }

    val vmArticles = viewModel?.articles?.collectAsState()?.value
    val vmQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val vmTitle = viewModel?.newEventTitle?.collectAsState()?.value
    val vmDate = viewModel?.newEventDate?.collectAsState()?.value

    val catalogArticles = vmArticles ?: fallbackCatalogArticles()
    val quantitiesMap = vmQuantities ?: fallbackQuantities
    val headerEventLabel = vmTitle?.ifBlank { "Nuevo evento" } ?: "Nuevo evento"
    val rawDateLabel = vmDate.orEmpty()
    val availabilityDateLabel = if (rawDateLabel.isBlank()) {
        "la fecha seleccionada"
    } else {
        formatReadableSpanishDateText(rawDateLabel)
    }
    val searchError = rememberDebouncedValidationError(
        value = searchQuery,
        hasInteracted = searchTouched,
        forceShowError = false,
    ) { candidate ->
        InputValidator.validateSearchQuery(candidate)
    }

    val categoryOptions = remember(catalogArticles) {
        listOf("Todos") + catalogArticles.map { articleItem -> articleItem.category }.distinct()
    }

    val visibleArticles = catalogArticles.filter { articleItem ->
        val matchesCategory = if (selectedCategory == "Todos") {
            true
        } else {
            articleItem.category.equals(selectedCategory, ignoreCase = true)
        }
        val trimmedQuery = searchQuery.trim()
        val matchesSearch = if (trimmedQuery.isEmpty()) {
            true
        } else {
            val matchesName = articleItem.name.contains(trimmedQuery, ignoreCase = true)
            val matchesCode = articleItem.code.contains(trimmedQuery, ignoreCase = true)
            eitherTrue(matchesName, matchesCode)
        }
        bothTrue(matchesCategory, matchesSearch)
    }

    val selectedArticlesCount = catalogArticles.count { articleItem ->
        val qty = quantitiesMap[articleItem.id] ?: 0
        qty > 0
    }
    val totalUnitsCount = catalogArticles.sumOf { articleItem ->
        quantitiesMap[articleItem.id] ?: 0
    }
    val subtotalDop = catalogArticles.sumOf { articleItem ->
        val qty = quantitiesMap[articleItem.id] ?: 0
        qty * articleItem.pricePerDayDop
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A05") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Agregar artículos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "Paso 2 de 3 · $headerEventLabel",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            AdminWizardProgressHeader(
                activeStep = 2,
                onSelectStepOne = { onSelectScreenCode("BACK:A05") },
                onSelectStepTwo = {},
                onSelectStepThree = { onSelectScreenCode("A07") },
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfInputShadow(AdminInputShape14, isAdmin = true)
                        .clip(AdminInputShape14)
                        .background(Color.White)
                        .border(1.dp, if (searchError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { updatedText ->
                                searchTouched = true
                                searchQuery = updatedText.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    BasicText(
                                        text = "Buscar en el inventario",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                }
                RfValidationErrorText(errorMessage = searchError)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categoryOptions.forEach { categoryLabel ->
                    val isSelectedChip = selectedCategory == categoryLabel
                    val chipBrush = if (isSelectedChip) AdminButtonGradient else SolidColor(Color.White)
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(AdminPillShape)
                            .background(chipBrush)
                            .border(
                                width = 1.dp,
                                color = if (isSelectedChip) Color.Transparent else Color(0xFFE9E9F2),
                                shape = AdminPillShape,
                            )
                            .clickable { selectedCategory = categoryLabel }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = categoryLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelectedChip) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelectedChip) Color.White else Color(0xFF2E324A),
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AdminInputShape14)
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF4E6A10), size = 16.dp)
                    BasicText(
                        text = "Disponibilidad verificada para $availabilityDateLabel",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(AdminCardShape20, isAdmin = true)
                    .clip(AdminCardShape20)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
            ) {
                Column {
                    visibleArticles.forEachIndexed { articleIndex, articleItem ->
                        val currentQty = quantitiesMap[articleItem.id] ?: 0
                        val isSelectedItem = currentQty > 0
                        if (articleIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(
                                    photoKey = toRfPhotoKey(articleItem.photo),
                                    contentDescription = articleItem.name,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = articleItem.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                                val isLowStock = articleItem.availableOct23 <= 2
                                val stockTint = if (isLowStock) Color(0xFF7A5700) else Color(0xFF4E6A10)
                                BasicText(
                                    text = "${articleItem.availableOct23} libres de ${articleItem.totalStock} · RD$ ${articleItem.pricePerDayDop}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = stockTint),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelectedItem) Color(0xFFECEBFF) else Color(0xFFF2F2F8))
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.adjustDraftQuantity(articleItem.id, -1)
                                                } else {
                                                    val nextQty = (currentQty - 1).coerceAtLeast(0)
                                                    fallbackQuantities = fallbackQuantities + (articleItem.id to nextQty)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("−", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0)))
                                    }
                                    BasicTextField(
                                        value = currentQty.toString(),
                                        onValueChange = { updatedText ->
                                            val parsedQty = InputValidator.filterDigitsOnly(updatedText, 6)
                                                .toIntOrNull()
                                                ?.coerceIn(0, articleItem.availableOct23.coerceAtLeast(1)) ?: 0
                                            if (viewModel != null) {
                                                viewModel.setDraftQuantity(articleItem.id, parsedQty)
                                            } else {
                                                fallbackQuantities = fallbackQuantities + (articleItem.id to parsedQty)
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFF2E324A),
                                            textAlign = TextAlign.Center,
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                        modifier = Modifier.width(30.dp),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (viewModel != null) {
                                                    viewModel.adjustDraftQuantity(articleItem.id, 1, showPreview = false)
                                                } else {
                                                    val nextQty = (currentQty + 1).coerceAtMost(articleItem.availableOct23.coerceAtLeast(1))
                                                    fallbackQuantities = fallbackQuantities + (articleItem.id to nextQty)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("+", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    BasicText(
                        text = "$selectedArticlesCount artículos · $totalUnitsCount unid.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                    )
                    BasicText(
                        text = "RD$ $subtotalDop",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A07") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            text = "Continuar al resumen",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                        )
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminEventSummaryA07(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var notifyWhatsApp by remember { mutableStateOf(true) }
    var localSubmitError by remember { mutableStateOf<String?>(null) }

    val rawTitle = viewModel?.newEventTitle?.collectAsState()?.value.orEmpty()
    val rawClient = viewModel?.newEventClient?.collectAsState()?.value.orEmpty()
    val rawDate = viewModel?.newEventDate?.collectAsState()?.value.orEmpty()
    val rawVenue = viewModel?.newEventVenue?.collectAsState()?.value.orEmpty()
    val rawGuests = viewModel?.newEventGuests?.collectAsState()?.value.orEmpty()
    val observedQuantities = viewModel?.draftQuantities?.collectAsState()?.value
    val observedArticles = viewModel?.articles?.collectAsState()?.value
    val settings = viewModel?.businessSettings?.collectAsState()?.value ?: BusinessSettings()
    val clientsList = viewModel?.clients?.collectAsState()?.value.orEmpty()
    val vmError = viewModel?.formError?.collectAsState()?.value

    val draftTitle = if (viewModel != null) rawTitle.ifBlank { "Sin título de evento" } else "Cumpleaños de Mateo"
    val clientName = if (viewModel != null) rawClient.ifBlank { "Sin cliente asignado" } else "Laura Peña"
    val draftDate = if (viewModel != null) {
        val formattedDate = formatReadableSpanishDateText(rawDate)
        formattedDate.ifBlank { "Fecha pendiente" }
    } else {
        "Sáb, 7 de noviembre 2026"
    }
    val draftVenue = if (viewModel != null) rawVenue.ifBlank { "Lugar por confirmar" } else "Terraza Los Prados"
    val draftGuests = if (viewModel != null) rawGuests.ifBlank { "0" } else "40"

    val lineItems = remember(viewModel, observedQuantities, observedArticles) {
        if (viewModel != null) {
            viewModel.draftLineItems()
        } else {
            listOf(
                EventLineItem("RF-001", "Silla Tiffany Cristal", 40, 4, PhotoAsset.SILLA_TIFFANY),
                EventLineItem("RF-003", "Mesa redonda para 8", 5, 25, PhotoAsset.MESA_REDONDA),
                EventLineItem("RF-005", "Arco orgánico de globos", 1, 95, PhotoAsset.ARCO_ROSA),
                EventLineItem("RF-006", "Mesa dulce temática", 1, 150, PhotoAsset.MESA_HELADOS),
            )
        }
    }

    val itemsSubtotalDop = lineItems.sumOf { lineItem -> lineItem.subtotalDop }
    val deliveryFeeDop = settings.zones.firstOrNull { zoneItem -> zoneItem.active }?.feeDop ?: 350
    val totalDop = itemsSubtotalDop + deliveryFeeDop
    val depositPercent = settings.depositPercent
    val depositDop = (totalDop * depositPercent) / 100
    val displayedError = localSubmitError ?: vmError

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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("BACK:A06") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Resumen",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "Paso 3 de 3",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }

                AdminWizardProgressHeader(
                    activeStep = 3,
                    onSelectStepOne = { onSelectScreenCode("BACK:A05") },
                    onSelectStepTwo = { onSelectScreenCode("BACK:A06") },
                    onSelectStepThree = {},
                )

                if (displayedError != null) {
                    AdminInlineErrorBanner(message = displayedError)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfHeroBannerShadow(AdminHeroShape22, isAdmin = true)
                        .clip(AdminHeroShape22)
                        .background(AdminHeroGradient),
                ) {
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-10).dp)
                            .size(110.dp),
                    ) {
                        val scaleX = size.width / 120f
                        val scaleY = size.height / 120f
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(22f * scaleX, 92.5f * scaleY),
                            size = Size(76f * scaleX, 13f * scaleY),
                        )
                        drawRoundRect(
                            color = Color(0xFFFFF6EE),
                            topLeft = Offset(30f * scaleX, 58f * scaleY),
                            size = Size(60f * scaleX, 39f * scaleY),
                            cornerRadius = CornerRadius(6f * scaleX, 6f * scaleY),
                        )
                        drawRect(
                            color = Color(0xFFF9B6D3),
                            topLeft = Offset(30f * scaleX, 82f * scaleY),
                            size = Size(60f * scaleX, 6f * scaleY),
                        )
                        val frostingPath = Path().apply {
                            moveTo(30f * scaleX, 64f * scaleY)
                            quadraticTo(30f * scaleX, 58f * scaleY, 36f * scaleX, 58f * scaleY)
                            lineTo(84f * scaleX, 58f * scaleY)
                            quadraticTo(90f * scaleX, 58f * scaleY, 90f * scaleX, 64f * scaleY)
                            lineTo(90f * scaleX, 68f * scaleY)
                            quadraticTo(85f * scaleX, 75f * scaleY, 80f * scaleX, 68f * scaleY)
                            quadraticTo(75f * scaleX, 75f * scaleY, 70f * scaleX, 68f * scaleY)
                            quadraticTo(65f * scaleX, 75f * scaleY, 60f * scaleX, 68f * scaleY)
                            quadraticTo(55f * scaleX, 75f * scaleY, 50f * scaleX, 68f * scaleY)
                            quadraticTo(45f * scaleX, 75f * scaleY, 40f * scaleX, 68f * scaleY)
                            quadraticTo(35f * scaleX, 75f * scaleY, 30f * scaleX, 68f * scaleY)
                            close()
                        }
                        drawPath(frostingPath, Color(0xFFEC4F97))
                        drawRoundRect(Color(0xFF7650E8), Offset(42f * scaleX, 42f * scaleY), Size(5f * scaleX, 17f * scaleY), CornerRadius(2f * scaleX, 2f * scaleY))
                        drawRoundRect(Color(0xFF4AA8E0), Offset(57.5f * scaleX, 38f * scaleY), Size(5f * scaleX, 21f * scaleY), CornerRadius(2f * scaleX, 2f * scaleY))
                        drawRoundRect(Color(0xFF2FB58F), Offset(73f * scaleX, 42f * scaleY), Size(5f * scaleX, 17f * scaleY), CornerRadius(2f * scaleX, 2f * scaleY))
                        drawOval(Color(0xFFF5B53B), Offset(42.5f * scaleX, 33f * scaleY), Size(4f * scaleX, 8f * scaleY))
                        drawOval(Color(0xFFF5B53B), Offset(58f * scaleX, 29f * scaleY), Size(4f * scaleX, 8f * scaleY))
                        drawOval(Color(0xFFF5B53B), Offset(73.5f * scaleX, 33f * scaleY), Size(4f * scaleX, 8f * scaleY))
                    }

                    Column(modifier = Modifier.padding(16.dp).widthIn(max = 230.dp)) {
                        BasicText(
                            text = draftTitle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText(
                            text = "$draftDate · $draftGuests invitados",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFFE9EEFF), size = 14.dp)
                            BasicText(
                                text = draftVenue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)),
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = Color(0xFFE9EEFF), size = 14.dp)
                            BasicText(
                                text = clientName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminCardShape20, isAdmin = true)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp),
                ) {
                    Column {
                        if (lineItems.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectScreenCode("BACK:A06") }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                BasicText(
                                    text = "Sin artículos seleccionados · Toca para agregar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF4B47D6), fontWeight = FontWeight.Medium),
                                )
                                RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                            }
                        } else {
                            lineItems.forEachIndexed { lineIndex, lineItem ->
                                if (lineIndex > 0) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                    ) {
                                        RfPhoto(
                                            photoKey = toRfPhotoKey(lineItem.photo),
                                            contentDescription = lineItem.name,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }

                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        BasicText(
                                            text = lineItem.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false),
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF2E324A)),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        BasicText(
                                            text = "× ${lineItem.quantity}",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                                        )
                                    }

                                    BasicText(
                                        text = "RD$ ${lineItem.subtotalDop}",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                text = "Transporte e instalación",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                            )
                            BasicText(
                                text = "RD$ $deliveryFeeDop",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(
                                text = "Total",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            )
                            BasicText(
                                text = "RD$ $totalDop",
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                text = "Anticipo para reservar ($depositPercent%)",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                            )
                            BasicText(
                                text = "RD$ $depositDop",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), AdminButtonShape16)
                        .clickable { notifyWhatsApp = !notifyWhatsApp }
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
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Enviar cotización a $clientName",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                            )
                            BasicText(
                                text = "Por WhatsApp, al crear el evento",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                            )
                        }

                        val toggleBrush = if (notifyWhatsApp) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 30.dp)
                                .clip(CircleShape)
                                .background(toggleBrush)
                                .padding(3.dp),
                            contentAlignment = if (notifyWhatsApp) Alignment.CenterEnd else Alignment.CenterStart,
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFECECF5)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(AdminButtonGradient)
                        .clickable {
                            val linesSnapshot = viewModel?.draftLineItems().orEmpty()
                            val createdOk = viewModel?.submitNewEvent() ?: true
                            if (createdOk) {
                                localSubmitError = null
                                if (linesSnapshot.isNotEmpty()) {
                                    val resolvedGuests = rawGuests.trim().toIntOrNull() ?: 40
                                    val matchedEmail = clientsList.firstOrNull { clientItem ->
                                        clientItem.fullName.equals(rawClient.trim(), ignoreCase = true)
                                    }?.email.orEmpty()
                                    viewModel?.createFormalQuoteFromAdmin(
                                        eventTitle = rawTitle.ifBlank { draftTitle },
                                        clientName = rawClient.ifBlank { clientName },
                                        clientEmail = matchedEmail,
                                        venue = rawVenue.ifBlank { draftVenue },
                                        dateLabel = rawDate.ifBlank { draftDate },
                                        guests = resolvedGuests,
                                        lines = linesSnapshot,
                                        deliveryFeeDop = deliveryFeeDop,
                                        discountDop = 0,
                                    )
                                }
                                onSelectScreenCode("A11")
                            } else {
                                localSubmitError = viewModel.formError.value ?: "Completa todos los datos obligatorios del evento en el Paso 1."
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = "Crear y enviar cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

private fun fallbackChatThreadsList(): List<ChatThread> = listOf(
    ChatThread(
        id = "thr-camila",
        threadId = "thr-camila",
        contactName = "Camila Rodríguez",
        eventId = "evt-boda-real",
        eventTitle = "Boda Real Rosa & Dorado",
        lastMessage = "¡Perfecto María! Nos vemos a las 9:00 AM en el Pabellón de Orquídeas.",
        timestampLabel = "10:42",
        unreadCount = 1,
        roleTag = "Cliente",
    ),
    ChatThread(
        id = "thr-laura",
        threadId = "thr-laura",
        contactName = "Laura Peña",
        eventId = "evt-cumple-mateo",
        eventTitle = "Cumpleaños de Mateo",
        lastMessage = "¿Podemos sumar 2 bouquets de helio azul y dorado a la cotización?",
        timestampLabel = "09:55",
        unreadCount = 2,
        roleTag = "Cliente",
    ),
    ChatThread(
        id = "thr-valeria",
        threadId = "thr-valeria",
        contactName = "Valeria Santos",
        eventId = "evt-boda-valeria",
        eventTitle = "Boda íntima Jardín Colonial",
        lastMessage = "Recibí la propuesta de Rosa IA, me encantó la combinación marfil y rosa.",
        timestampLabel = "09:18",
        unreadCount = 0,
        roleTag = "Cliente",
    ),
    ChatThread(
        id = "thr-sofia",
        threadId = "thr-sofia",
        contactName = "Sofía Herrera",
        eventId = "evt-baby-sofia",
        eventTitle = "Baby Shower Sofía",
        lastMessage = "Hola, ¿tienen disponible la mesa de helados para el sábado 28 nov?",
        timestampLabel = "Ayer",
        unreadCount = 1,
        roleTag = "Cliente",
    ),
    ChatThread(
        id = "thr-andres",
        threadId = "thr-andres",
        contactName = "Andrés",
        eventId = "evt-boda-real",
        eventTitle = "Almacén · Ruta Camión #01",
        lastMessage = "120 sillas Tiffany enfundadas y rampa lista en muelle #2.",
        timestampLabel = "08:20",
        unreadCount = 0,
        roleTag = "Logística",
    ),
)

private fun isClientChatThread(threadItem: ChatThread): Boolean {
    val normalizedRole = threadItem.roleTag.trim().uppercase()
    val isExplicitClient = normalizedRole.contains("CLIENT")
    val isVipClient = normalizedRole.contains("VIP")
    return eitherTrue(isExplicitClient, isVipClient)
}

private fun avatarPaletteForName(contactName: String): Pair<Color, Color> {
    val codeSum = contactName.sumOf { character -> character.code }
    return when (codeSum % 4) {
        0 -> RfColors.VioletaTint to RfColors.VioletaTintInk
        1 -> Color(0xFFFFF2C9) to Color(0xFF7A5700)
        2 -> Color(0xFFEEF6D6) to Color(0xFF4E6A10)
        else -> Color(0xFFDDF4F3) to Color(0xFF1F8A84)
    }
}

@Composable
internal fun RenderAdminChatsA14(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var searchTouched by remember { mutableStateOf(false) }
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    val searchError = rememberDebouncedValidationError(
        value = searchQuery,
        hasInteracted = searchTouched,
        forceShowError = false,
    ) { candidate ->
        InputValidator.validateSearchQuery(candidate)
    }

    val vmThreads = viewModel?.chatThreads?.collectAsState()?.value
    val allThreads = vmThreads ?: fallbackChatThreadsList()

    val unreadThreadsCount = allThreads.count { threadItem -> threadItem.unreadCount > 0 }
    val clientThreadsCount = allThreads.count { threadItem -> isClientChatThread(threadItem) }
    val totalUnreadMessages = allThreads.sumOf { threadItem -> threadItem.unreadCount }

    val filteredByChip = allThreads.filter { threadItem ->
        when (selectedFilterKey) {
            "UNREAD" -> threadItem.unreadCount > 0
            "CLIENTS" -> isClientChatThread(threadItem)
            else -> true
        }
    }

    val visibleThreads = filteredByChip.filter { threadItem ->
        val trimmedQuery = searchQuery.trim()
        if (trimmedQuery.isEmpty()) {
            true
        } else {
            val matchesContact = threadItem.contactName.contains(trimmedQuery, ignoreCase = true)
            val matchesEvent = threadItem.eventTitle.contains(trimmedQuery, ignoreCase = true)
            val matchesSnippet = threadItem.lastMessage.contains(trimmedQuery, ignoreCase = true)
            val matchesHeader = eitherTrue(matchesContact, matchesEvent)
            eitherTrue(matchesHeader, matchesSnippet)
        }
    }

    val filterChips = listOf(
        "ALL" to "Todos · ${allThreads.size}",
        "UNREAD" to "Sin leer · $unreadThreadsCount",
        "CLIENTS" to "Clientes · $clientThreadsCount",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Chats",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "$totalUnreadMessages sin leer",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(AdminInputShape14)
                        .background(Color.White)
                        .border(1.dp, if (searchError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { updatedText ->
                                searchTouched = true
                                searchQuery = updatedText.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    BasicText(
                                        text = "Buscar cliente o evento",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                }
                RfValidationErrorText(errorMessage = searchError)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterChips.forEach { chipPair ->
                    val filterKey = chipPair.first
                    val chipLabel = chipPair.second
                    val isSelected = selectedFilterKey == filterKey
                    val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(AdminPillShape)
                            .background(chipBrush)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                shape = AdminPillShape,
                            )
                            .clickable { selectedFilterKey = filterKey }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = chipLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF2E324A),
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(AdminCardShape20, isAdmin = true)
                    .clip(AdminCardShape20)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
            ) {
                if (visibleThreads.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "No hay conversaciones que coincidan con este filtro.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    }
                } else {
                    Column {
                        visibleThreads.forEachIndexed { threadIndex, threadItem ->
                            if (threadIndex > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
                            val palette = avatarPaletteForName(threadItem.contactName)
                            val initial = InputValidator.extractInitials(threadItem.contactName, fallback = "C", maxInitials = 1)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel?.selectChatThread(threadItem.id)
                                        onSelectScreenCode("A15")
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
                                        .background(palette.first),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = initial,
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.second),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        BasicText(
                                            text = threadItem.contactName,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 15.sp,
                                                fontWeight = if (threadItem.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                                color = Color(0xFF2E324A),
                                            ),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        BasicText(
                                            text = threadItem.timestampLabel,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                        )
                                    }

                                    BasicText(
                                        text = "${threadItem.eventTitle} · ${threadItem.roleTag}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        BasicText(
                                            text = threadItem.lastMessage,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                        )

                                        if (threadItem.unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFD45C)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                BasicText(
                                                    text = "${threadItem.unreadCount}",
                                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
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

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun isThreadIdentifierMatch(threadItem: ChatThread, targetId: String): Boolean {
    if (targetId.isBlank()) {
        return false
    }
    val matchesPrimaryId = threadItem.id == targetId
    val matchesSecondaryId = threadItem.threadId == targetId
    return eitherTrue(matchesPrimaryId, matchesSecondaryId)
}

private fun isMessageInThread(messageItem: ChatMessageRecord, activeThread: ChatThread): Boolean {
    val matchesId = messageItem.threadId == activeThread.id
    val matchesThreadId = messageItem.threadId == activeThread.threadId
    return eitherTrue(matchesId, matchesThreadId)
}

private fun isOutgoingAdminMessage(messageItem: ChatMessageRecord): Boolean {
    val roleNormalized = messageItem.senderRole.trim().lowercase()
    return roleNormalized == "admin"
}

private fun preselectContactQuoteOrLead(
    contactName: String,
    eventId: String,
    quotes: List<QuoteRecord>,
    leads: List<LeadRecord>,
    viewModel: RosaFiestaViewModel?,
) {
    if (viewModel == null) {
        return
    }
    val primaryToken = contactName.trim().substringBefore(" ").trim()
    val matchedQuote = quotes.firstOrNull { quoteItem ->
        val matchesEvent = bothTrue(eventId.isNotBlank(), quoteItem.eventId == eventId)
        val matchesName = bothTrue(primaryToken.isNotEmpty(), quoteItem.clientName.contains(primaryToken, ignoreCase = true))
        eitherTrue(matchesEvent, matchesName)
    }
    if (matchedQuote != null) {
        viewModel.selectQuote(matchedQuote.id)
    }
    val matchedLead = leads.firstOrNull { leadItem ->
        bothTrue(primaryToken.isNotEmpty(), leadItem.clientName.contains(primaryToken, ignoreCase = true))
    }
    if (matchedLead != null) {
        viewModel.selectLead(matchedLead.id)
    }
}

@Composable
internal fun RenderAdminChatConversationA15(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var messageInput by remember { mutableStateOf("") }
    var messageTouched by remember { mutableStateOf(false) }
    var sendAttempted by remember { mutableStateOf(false) }
    var pendingAttachmentName by remember { mutableStateOf("") }
    var fallbackLocalReplies by remember { mutableStateOf(listOf<ChatMessageRecord>()) }

    val selectedThreadId = viewModel?.selectedChatThreadId?.collectAsState()?.value.orEmpty()
    val vmThreads = viewModel?.chatThreads?.collectAsState()?.value
    val vmThreadMessages = viewModel?.threadMessages?.collectAsState()?.value
    val pickedAttachment = viewModel?.pickedAttachmentFile?.collectAsState()?.value
    val quotesList = viewModel?.quotes?.collectAsState()?.value.orEmpty()
    val leadsList = viewModel?.leads?.collectAsState()?.value.orEmpty()

    val threadsPool = vmThreads ?: fallbackChatThreadsList()
    val activeThread = threadsPool.firstOrNull { threadItem ->
        isThreadIdentifierMatch(threadItem, selectedThreadId)
    } ?: viewModel?.selectedChatThread() ?: threadsPool.firstOrNull()
    if (activeThread == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Chat del evento",
            emptyTitle = "Sin conversaciones activas",
            emptyMessage = "Cuando recibas mensajes de clientes o cotizaciones activas, el chat aparecerá aquí.",
            backTargetCode = "BACK:A14",
            ctaLabel = "Volver a chats",
            ctaTargetCode = "BACK:A14",
            iconKind = RfIconKind.CHAT,
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }

    var suggestionAdded by remember(activeThread.id) { mutableStateOf(false) }

    val resolvedMessages = remember(vmThreadMessages, activeThread, fallbackLocalReplies) {
        if (viewModel != null) {
            val filtered = vmThreadMessages.orEmpty().filter { messageItem ->
                isMessageInThread(messageItem, activeThread)
            }
            if (filtered.isNotEmpty()) {
                filtered
            } else {
                viewModel.messagesForSelectedThread()
            }
        } else {
            listOf(
                ChatMessageRecord(
                    id = "msg-fallback-1",
                    threadId = activeThread.id,
                    contactName = activeThread.contactName,
                    eventId = activeThread.eventId,
                    eventTitle = activeThread.eventTitle,
                    senderName = activeThread.contactName,
                    senderRole = "client",
                    body = activeThread.lastMessage,
                    timestampLabel = activeThread.timestampLabel,
                ),
            ) + fallbackLocalReplies
        }
    }

    val effectiveAttachment = pendingAttachmentName.ifBlank { pickedAttachment?.fileName.orEmpty() }
    val scrollState = rememberScrollState()
    val messageError = rememberDebouncedValidationError(
        value = messageInput,
        hasInteracted = messageTouched,
        forceShowError = sendAttempted,
    ) { candidate ->
        InputValidator.validateChatMessage(
            rawValue = candidate,
            isRequired = effectiveAttachment.isEmpty(),
        )
    }

    val submitAdminReply: () -> Unit = {
        sendAttempted = true
        val validationError = InputValidator.validateChatMessage(
            rawValue = messageInput,
            isRequired = effectiveAttachment.isEmpty(),
        )
        if (validationError == null) {
            val trimmedText = messageInput.trim()
            val outgoingBody = if (trimmedText.isNotEmpty()) trimmedText else "Adjunto: $effectiveAttachment"
            if (viewModel != null) {
                viewModel.sendAdminDirectChatReply(outgoingBody, effectiveAttachment)
                viewModel.clearPickedAttachment()
            } else {
                fallbackLocalReplies = fallbackLocalReplies + ChatMessageRecord(
                    id = "local-${fallbackLocalReplies.size + 1}",
                    threadId = activeThread.id,
                    contactName = activeThread.contactName,
                    eventId = activeThread.eventId,
                    eventTitle = activeThread.eventTitle,
                    senderName = "María",
                    senderRole = "admin",
                    body = outgoingBody,
                    attachmentName = effectiveAttachment,
                    timestampLabel = "Ahora",
                )
            }
            messageInput = ""
            messageTouched = false
            sendAttempted = false
            pendingAttachmentName = ""
        }
    }

    LaunchedEffect(resolvedMessages.size, activeThread.id) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val contactPalette = avatarPaletteForName(activeThread.contactName)
    val contactInitial = InputValidator.extractInitials(activeThread.contactName, fallback = "C", maxInitials = 1)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 84.dp, bottom = 154.dp)
                .verticalScroll(scrollState)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEDF5))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                BasicText(
                    text = "${activeThread.eventTitle} · ${activeThread.roleTag}",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)),
                )
            }

            resolvedMessages.forEach { messageItem ->
                val isAdminTurn = isOutgoingAdminMessage(messageItem)
                val bubbleShape = if (isAdminTurn) {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
                } else {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
                }
                val bubbleBg = if (isAdminTurn) AdminButtonGradient else SolidColor(Color.White)
                Box(
                    modifier = Modifier
                        .align(if (isAdminTurn) Alignment.End else Alignment.Start)
                        .widthIn(max = 300.dp)
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .border(
                            width = 1.dp,
                            color = if (isAdminTurn) Color.Transparent else Color(0xFFECECF5),
                            shape = bubbleShape,
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!isAdminTurn) {
                            BasicText(
                                text = messageItem.senderName,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                            )
                        }
                        BasicText(
                            text = messageItem.body,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = if (isAdminTurn) Color.White else Color(0xFF2E324A),
                                lineHeight = 21.sp,
                            ),
                        )
                        if (messageItem.attachmentName.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAdminTurn) Color(0x29FFFFFF) else Color(0xFFF2F2FA))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    RfIcon(
                                        kind = RfIconKind.RECEIPT,
                                        tint = if (isAdminTurn) Color.White else Color(0xFF4B47D6),
                                        size = 14.dp,
                                    )
                                    BasicText(
                                        text = messageItem.attachmentName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isAdminTurn) Color.White else Color(0xFF2E324A),
                                        ),
                                    )
                                }
                            }
                        }
                        BasicText(
                            text = messageItem.timestampLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 11.sp,
                                color = if (isAdminTurn) Color(0xFFE3E4EF) else Color(0xFF5F6377),
                            ),
                        )
                    }
                }
            }

            val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
            val catalogArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
            val matchedEvent = allEvents.firstOrNull { candidate -> candidate.id == activeThread.eventId }
                ?: allEvents.firstOrNull { candidate -> candidate.title == activeThread.eventTitle }
            val recommendation = buildAdminChatRecommendation(
                matchedEvent = matchedEvent,
                eventTitle = activeThread.eventTitle,
                catalogArticles = catalogArticles,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AdminSoftBannerGradient)
                    .border(1.dp, Color(0xFFDCE0F5), RoundedCornerShape(18.dp))
                    .padding(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MINI, customSize = 36.dp, forceAdminPalette = true)
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = recommendation.headline,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = recommendation.subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    val actionBrush = if (suggestionAdded) SolidColor(Color(0xFFEEF6D6)) else AdminButtonGradient
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(AdminPillShape)
                            .background(actionBrush)
                            .clickable {
                                if (!suggestionAdded) {
                                    val feedbackMsg = "Se agregaron ${recommendation.suggestedQty} ${recommendation.articleName} a ${activeThread.eventTitle}."
                                    val targetId = matchedEvent?.id ?: activeThread.eventId
                                    viewModel?.applySuggestedItemsToEvent(
                                        eventId = targetId,
                                        additionalQuantities = mapOf(recommendation.articleId to recommendation.suggestedQty),
                                        feedbackMessage = feedbackMsg,
                                    )
                                    suggestionAdded = true
                                }
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (suggestionAdded) "Agregado ✓" else "Agregar al evento",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (suggestionAdded) Color(0xFF4E6A10) else Color.White,
                            ),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .clickable { onSelectScreenCode("BACK:A14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(contactPalette.first),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = contactInitial,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = contactPalette.second),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = activeThread.contactName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = activeThread.eventTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            if (activeThread.eventId.isNotBlank()) {
                                viewModel?.selectEvent(activeThread.eventId)
                            }
                            onSelectScreenCode("A13")
                        },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            viewModel?.pickMediaAttachment(listOf("jpg", "jpeg", "png", "pdf")) { pickedFile ->
                                pendingAttachmentName = pickedFile.fileName
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 20.dp)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(top = 10.dp, start = 12.dp, end = 12.dp, bottom = 26.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (effectiveAttachment.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECEBFF))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(
                            text = "Adjunto: $effectiveAttachment",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .clickable {
                                    pendingAttachmentName = ""
                                    viewModel?.clearPickedAttachment()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF4B47D6), size = 14.dp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECEBFF))
                            .clickable {
                                if (activeThread.eventId.isNotBlank()) {
                                    viewModel?.selectEvent(activeThread.eventId)
                                }
                                onSelectScreenCode("A13")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF4B47D6), size = 15.dp)
                            BasicText(
                                text = "Ver evento",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                preselectContactQuoteOrLead(
                                    contactName = activeThread.contactName,
                                    eventId = activeThread.eventId,
                                    quotes = quotesList,
                                    leads = leadsList,
                                    viewModel = viewModel,
                                )
                                onSelectScreenCode("A03")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.RECEIPT, tint = Color(0xFF2E324A), size = 15.dp)
                            BasicText(
                                text = "Abrir cotización",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                val quickConfirmation = "Confirmado el montaje para ${activeThread.eventTitle}."
                                if (viewModel != null) {
                                    viewModel.sendAdminDirectChatReply(quickConfirmation)
                                } else {
                                    messageInput = quickConfirmation
                                }
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Confirmar hora",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .clickable {
                                preselectContactQuoteOrLead(
                                    contactName = activeThread.contactName,
                                    eventId = activeThread.eventId,
                                    quotes = quotesList,
                                    leads = leadsList,
                                    viewModel = viewModel,
                                )
                                onSelectScreenCode("A03")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .clickable {
                                viewModel?.pickMediaAttachment(listOf("jpg", "jpeg", "png", "pdf")) { pickedFile ->
                                    pendingAttachmentName = pickedFile.fileName
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF5F6377), size = 18.dp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .border(
                                width = 1.dp,
                                color = if (messageError != null) Color(0xFFD63031) else Color.Transparent,
                                shape = CircleShape,
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = messageInput,
                            onValueChange = { updatedText ->
                                messageTouched = true
                                sendAttempted = false
                                messageInput = updatedText.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitAdminReply() }),
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (messageInput.isEmpty()) {
                                    BasicText(
                                        text = "Responder a ${activeThread.contactName}…",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient)
                            .clickable { submitAdminReply() },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 19.dp)
                    }
                }
                RfValidationErrorText(errorMessage = messageError)
            }
        }
    }
}

private fun fallbackQuotesList(): List<QuoteRecord> = listOf(
    QuoteRecord(
        id = "quo-boda-camila",
        code = "COT-2026-1023",
        eventId = "evt-boda-real",
        eventTitle = "Boda Real Rosa & Dorado",
        clientName = "Camila Rodríguez",
        clientEmail = "camila@rosafiesta.do",
        venue = "Jardín Botánico",
        dateLabel = "Vie 23 oct",
        guests = 120,
        status = "APPROVED",
        lines = emptyList(),
        deliveryFeeDop = 350,
        discountDop = 480,
        totalDop = 4500,
    ),
    QuoteRecord(
        id = "quo-cumple-mateo",
        code = "COT-2026-1107",
        eventId = "evt-cumple-mateo",
        eventTitle = "Cumpleaños de Mateo",
        clientName = "Laura Peña",
        clientEmail = "laura@rosafiesta.do",
        venue = "Terraza Los Prados",
        dateLabel = "Sáb 7 nov",
        guests = 40,
        status = "PENDING",
        lines = emptyList(),
        deliveryFeeDop = 350,
        discountDop = 50,
        totalDop = 405,
    ),
    QuoteRecord(
        id = "quo-aniversario-oro",
        code = "COT-2026-1001",
        eventId = "evt-aniversario-oro",
        eventTitle = "Aniversario Bodas de Oro",
        clientName = "Roberto Pérez",
        clientEmail = "roberto@correo.do",
        venue = "Club Naco",
        dateLabel = "Jue 1 oct",
        guests = 60,
        status = "EXPIRED",
        lines = emptyList(),
        deliveryFeeDop = 350,
        discountDop = 0,
        totalDop = 1850,
    ),
)

private fun classifyQuoteStatusBucket(status: String): String {
    val normalized = status.trim().uppercase()
    return when (normalized) {
        "APPROVED", "APROBADA", "APROBADO", "SIGNED", "FIRMADA", "CONFIRMED" -> "APPROVED"
        "EXPIRED", "VENCIDA", "VENCIDO", "REJECTED", "RECHAZADA", "DECLINED", "CANCELLED" -> "EXPIRED"
        else -> "SENT"
    }
}

private fun quoteBadgeAppearance(status: String): Triple<String, Color, Color> = when (classifyQuoteStatusBucket(status)) {
    "APPROVED" -> Triple("Aprobada", Color(0xFFECEBFF), Color(0xFF4B47D6))
    "EXPIRED" -> Triple("Vencida", Color(0xFFFDE3EA), Color(0xFFB42345))
    else -> Triple("Enviada", Color(0xFFFFF2C9), Color(0xFF7A5700))
}

@Composable
internal fun RenderAdminQuotesA22(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilterKey by remember { mutableStateOf("ALL") }

    val vmQuotes = viewModel?.quotes?.collectAsState()?.value
    val allQuotes = vmQuotes ?: fallbackQuotesList()

    val totalQuotesCount = allQuotes.size
    val sentQuotesCount = allQuotes.count { quoteItem -> classifyQuoteStatusBucket(quoteItem.status) == "SENT" }
    val approvedQuotesCount = allQuotes.count { quoteItem -> classifyQuoteStatusBucket(quoteItem.status) == "APPROVED" }
    val expiredQuotesCount = allQuotes.count { quoteItem -> classifyQuoteStatusBucket(quoteItem.status) == "EXPIRED" }

    val visibleQuotes = allQuotes.filter { quoteItem ->
        if (selectedFilterKey == "ALL") {
            true
        } else {
            classifyQuoteStatusBucket(quoteItem.status) == selectedFilterKey
        }
    }

    val filterChips = listOf(
        "ALL" to "Todas · $totalQuotesCount",
        "SENT" to "Enviadas · $sentQuotesCount",
        "APPROVED" to "Aprobadas · $approvedQuotesCount",
        "EXPIRED" to "Vencidas · $expiredQuotesCount",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cotizaciones",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "$totalQuotesCount registradas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterChips.forEach { chipPair ->
                    val bucketKey = chipPair.first
                    val label = chipPair.second
                    val isSelected = selectedFilterKey == bucketKey
                    val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(AdminPillShape)
                            .background(chipBrush)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                shape = AdminPillShape,
                            )
                            .clickable { selectedFilterKey = bucketKey }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF2E324A),
                            ),
                        )
                    }
                }
            }

            if (visibleQuotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "No hay cotizaciones en esta categoría.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                visibleQuotes.forEach { quoteItem ->
                    val badge = quoteBadgeAppearance(quoteItem.status)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                            .clickable {
                                viewModel?.selectQuote(quoteItem.id)
                                onSelectScreenCode("A03")
                            }
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = quoteItem.eventTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                    )
                                    BasicText(
                                        text = "${quoteItem.clientName} · ${quoteItem.code} · ${formatReadableSpanishDateText(quoteItem.dateLabel)}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(AdminPillShape)
                                        .background(badge.second)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = badge.first,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badge.third),
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = "RD$ ${quoteItem.totalDop}",
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                                    )
                                    BasicText(
                                        text = "Anticipo RD$ ${quoteItem.depositDop}",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(AdminPillShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE3E4EF), AdminPillShape)
                                        .clickable {
                                            val duplicated = viewModel?.duplicateQuoteById(quoteItem.id)
                                            val targetId = duplicated?.id ?: quoteItem.id
                                            viewModel?.selectQuote(targetId)
                                            onSelectScreenCode("A03")
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        RfIcon(kind = RfIconKind.COPY, tint = Color(0xFF151729), size = 14.dp)
                                        BasicText(
                                            text = "Duplicar",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(AdminPillShape)
                                        .background(AdminButtonGradient)
                                        .clickable {
                                            viewModel?.selectQuote(quoteItem.id)
                                            onSelectScreenCode("A03")
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 14.dp)
                                        BasicText(
                                            text = "Ver cotización",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun fallbackNotificationsList(): List<NotificationRecord> = listOf(
    NotificationRecord(
        id = "ntf-1",
        title = "Anticipo pendiente de verificación",
        body = "Ana Gómez registró transferencia POP-772104 por RD$ 642 para Graduación Promo 2026.",
        category = "PAYMENTS",
        createdAtLabel = "Hace 12 min",
        isRead = false,
        recipientRole = "admin",
        actionPath = "/payments",
    ),
    NotificationRecord(
        id = "ntf-2",
        title = "Ruta en montaje activo",
        body = "Pedro completó el 66 % del checklist en Jardín Botánico (Boda Real Rosa & Dorado).",
        category = "LOGISTICS",
        createdAtLabel = "Hace 28 min",
        isRead = false,
        recipientRole = "all",
        actionPath = "/events/evt-boda-real",
    ),
    NotificationRecord(
        id = "ntf-3",
        title = "Nueva solicitud de cotización",
        body = "Valeria Santos solicitó propuesta para Boda íntima Jardín Colonial.",
        category = "LEADS",
        createdAtLabel = "Hace 1 h",
        isRead = false,
        recipientRole = "admin",
        actionPath = "/leads",
    ),
    NotificationRecord(
        id = "ntf-4",
        title = "Stock bajo · Mesa dulce temática",
        body = "Queda 1 unidad libre para el próximo fin de semana.",
        category = "INVENTORY",
        createdAtLabel = "Ayer",
        isRead = true,
        recipientRole = "admin",
        actionPath = "/inventory",
    ),
)

private fun isPaymentNotification(notificationItem: NotificationRecord): Boolean {
    val categoryNormalized = notificationItem.category.trim().uppercase()
    val pathNormalized = notificationItem.actionPath.trim().lowercase()
    val isPaymentCategory = eitherTrue(categoryNormalized == "PAYMENTS", categoryNormalized == "REFUNDS")
    val isPaymentPath = eitherTrue(pathNormalized.contains("payment"), pathNormalized.contains("refund"))
    return eitherTrue(isPaymentCategory, isPaymentPath)
}

private fun isEventNotification(notificationItem: NotificationRecord): Boolean {
    val categoryNormalized = notificationItem.category.trim().uppercase()
    val pathNormalized = notificationItem.actionPath.trim().lowercase()
    val isEventCategory = eitherTrue(categoryNormalized == "EVENTS", categoryNormalized == "LOGISTICS")
    val isEventPath = pathNormalized.contains("event")
    return eitherTrue(isEventCategory, isEventPath)
}

private fun resolveAdminRouteForNotification(notificationItem: NotificationRecord): String {
    if (isPaymentNotification(notificationItem)) {
        return "A20"
    }
    val categoryNormalized = notificationItem.category.trim().uppercase()
    val pathNormalized = notificationItem.actionPath.trim().lowercase()
    val isLeadCategory = eitherTrue(categoryNormalized == "LEADS", categoryNormalized == "QUOTES")
    val isLeadPath = eitherTrue(pathNormalized.contains("lead"), pathNormalized.contains("quote"))
    if (eitherTrue(isLeadCategory, isLeadPath)) {
        return "A02"
    }
    val isInventoryCategory = categoryNormalized == "INVENTORY"
    val isInventoryPath = eitherTrue(pathNormalized.contains("inventory"), pathNormalized.contains("catalog"))
    if (eitherTrue(isInventoryCategory, isInventoryPath)) {
        return "A09"
    }
    return "A11"
}

private fun notificationVisualStyle(notificationItem: NotificationRecord): Triple<RfIconKind, Color, Color> = when (resolveAdminRouteForNotification(notificationItem)) {
    "A20" -> Triple(RfIconKind.WALLET, Color(0xFFEEF6D6), Color(0xFF4E6A10))
    "A02" -> Triple(RfIconKind.ORDERS, Color(0xFFFFF2C9), Color(0xFF7A5700))
    "A09" -> Triple(RfIconKind.WARNING, Color(0xFFFDE3EA), Color(0xFFB42345))
    else -> Triple(RfIconKind.CALENDAR, Color(0xFFECEBFF), Color(0xFF4B47D6))
}

@Composable
internal fun RenderAdminNotificationsA29(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var fallbackReadIds by remember { mutableStateOf(setOf<String>()) }

    val vmNotifications = viewModel?.notifications?.collectAsState()?.value
    val notificationsPool = (vmNotifications ?: fallbackNotificationsList()).map { notificationItem ->
        val markedLocally = fallbackReadIds.contains(notificationItem.id)
        if (markedLocally) notificationItem.copy(isRead = true) else notificationItem
    }

    val unreadCount = notificationsPool.count { notificationItem -> !notificationItem.isRead }
    val paymentsCount = notificationsPool.count { notificationItem -> isPaymentNotification(notificationItem) }
    val eventsCount = notificationsPool.count { notificationItem -> isEventNotification(notificationItem) }

    val visibleNotifications = notificationsPool.filter { notificationItem ->
        when (selectedFilterKey) {
            "UNREAD" -> !notificationItem.isRead
            "PAYMENTS" -> isPaymentNotification(notificationItem)
            "EVENTS" -> isEventNotification(notificationItem)
            else -> true
        }
    }

    val filterChips = listOf(
        "ALL" to "Todas · ${notificationsPool.size}",
        "UNREAD" to "Sin leer · $unreadCount",
        "PAYMENTS" to "Pagos · $paymentsCount",
        "EVENTS" to "Eventos · $eventsCount",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A01") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Notificaciones",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "$unreadCount sin leer",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(AdminPillShape)
                    .background(Color(0xFFECEBFF))
                    .clickable {
                        notificationsPool.forEach { notificationItem ->
                            if (!notificationItem.isRead) {
                                viewModel?.markNotificationAsRead(notificationItem.id)
                                fallbackReadIds = fallbackReadIds + notificationItem.id
                            }
                        }
                        viewModel?.postFeedback("Notificaciones marcadas como leídas")
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Marcar leídas",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterChips.forEach { chipPair ->
                val filterKey = chipPair.first
                val label = chipPair.second
                val isSelected = selectedFilterKey == filterKey
                val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(AdminPillShape)
                        .background(chipBrush)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                            shape = AdminPillShape,
                        )
                        .clickable { selectedFilterKey = filterKey }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = label,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(AdminCardShape20, isAdmin = true)
                .clip(AdminCardShape20)
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
        ) {
            if (visibleNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "No hay notificaciones para este filtro.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                Column {
                    visibleNotifications.forEachIndexed { notificationIndex, notificationItem ->
                        if (notificationIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        val visual = notificationVisualStyle(notificationItem)
                        val rowBg = if (notificationItem.isRead) Color.White else Color(0xFFF7F6FF)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable {
                                    viewModel?.markNotificationAsRead(notificationItem.id)
                                    fallbackReadIds = fallbackReadIds + notificationItem.id
                                    val targetScreen = resolveAdminRouteForNotification(notificationItem)
                                    onSelectScreenCode(targetScreen)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(visual.second),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = visual.first, tint = visual.third, size = 18.dp)
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                BasicText(
                                    text = notificationItem.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        fontWeight = if (notificationItem.isRead) FontWeight.SemiBold else FontWeight.Bold,
                                        color = Color(0xFF151729),
                                    ),
                                )
                                BasicText(
                                    text = notificationItem.body,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicText(
                                    text = notificationItem.createdAtLabel,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                                )
                                if (!notificationItem.isRead) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF5B6CF0)),
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

private fun buildAdminOnboardingStepTransition(isAdvancing: Boolean): ContentTransform = if (isAdvancing) {
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 220))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    )
} else {
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> -fullWidth / 3 },
        ) + fadeIn(animationSpec = tween(durationMillis = 220))
        ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    )
}

@Composable
internal fun RenderAdminEntryChoiceA30(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var onboardingStep by remember { mutableStateOf(0) }
    var rememberChoice by remember { mutableStateOf(true) }
    val isInTourSlides = onboardingStep < 4

    val onboardingSummary = remember(viewModel) {
        viewModel?.getAdminOnboardingSummary() ?: AdminOnboardingSummary(
            activeEventsCount = 3,
            leadEventTitle = "Boda Real Rosa & Dorado",
            leadEventDate = "Vie 23 oct",
            leadEventGuests = 120,
            leadEventVenue = "Jardín Botánico",
            leadEventItemsCount = 145,
            primaryArticleTitle = "Sillas Tiffany doradas",
            primaryArticleAvailableStock = 200,
            monthlyIncomeDop = 12450,
            depositPendingDop = 4500,
            totalArticlesInCatalog = 12,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5FC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAdminOnboardingBackground(step = onboardingStep)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(top = 14.dp, bottom = 10.dp, start = 20.dp, end = 16.dp),
        ) {
            if (isInTourSlides) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfLogo(
                        customSize = 36.dp,
                        insetPadding = false,
                        modifier = Modifier.shadow(elevation = 4.dp, shape = CircleShape, spotColor = Color(0x4D5B6CF0)),
                    )
                    BasicText(
                        text = "Rosa Fiesta",
                        style = typo.displayFamily.let { family ->
                            TextStyle(fontFamily = family, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729))
                        },
                    )
                    Box(
                        modifier = Modifier
                            .clip(AdminPillShape)
                            .background(AdminButtonGradient)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        BasicText(
                            text = "ADMIN",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp, color = Color.White),
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(AdminPillShape)
                        .background(Color.White.copy(alpha = 0.82f))
                        .border(1.dp, Color(0xFFE0E2F2), AdminPillShape)
                        .clickable { onboardingStep = 4 }
                        .padding(horizontal = 15.dp)
                        .testTag("admin-onboarding-skip"),
                ) {
                    BasicText(
                        text = "Omitir",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E4F3), CircleShape)
                        .clickable { onboardingStep = 3 },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 21.dp)
                }

                Box(
                    modifier = Modifier
                        .clip(AdminPillShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFE2E4F3), AdminPillShape)
                        .clickable { onboardingStep = 0 }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFF5B6CF0), size = 14.dp)
                        BasicText(
                            text = "Ver guía visual",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
            }
        }

        val contentBottomPadding = if (isInTourSlides) 96.dp else 20.dp
        AnimatedContent(
            targetState = onboardingStep.coerceIn(0, 4),
            transitionSpec = {
                val isAdvancing = targetState > initialState
                buildAdminOnboardingStepTransition(isAdvancing)
            },
            label = "admin-onboarding-visual-stage",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 68.dp, bottom = contentBottomPadding),
        ) { animatedStep ->
            if (animatedStep < 4) {
                AdminOnboardingVisualSlide(
                    step = animatedStep,
                    summary = onboardingSummary,
                )
            } else {
                AdminOnboardingEntryStage(
                    rememberChoice = rememberChoice,
                    onPrimaryAdminClick = {
                        if (viewModel == null) {
                            onSelectScreenCode("A01")
                        } else {
                            val hasSession = viewModel.hasValidAuthenticatedSession()
                            val isAdminOrStaff = viewModel.isCurrentSessionAdminOrStaff()
                            val canEnterImmediately = bothTrue(hasSession, isAdminOrStaff)
                            if (canEnterImmediately) {
                                onSelectScreenCode("A01")
                            } else {
                                viewModel.clearFormError()
                                viewModel.clearFormFeedback()
                                onSelectScreenCode("s07")
                            }
                        }
                    },
                    onClientModeClick = {
                        viewModel?.switchRole("client")
                        onSwitchAppMode()
                    },
                    onToggleRemember = { rememberChoice = !rememberChoice },
                )
            }
        }

        if (isInTourSlides) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .padding(top = 10.dp, bottom = 18.dp, start = 26.dp, end = 26.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (dotIndex in 0..3) {
                        val isActiveDot = dotIndex == onboardingStep
                        val dotWidth by animateDpAsState(
                            targetValue = if (isActiveDot) 28.dp else 8.dp,
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            label = "admin-visual-dot-width",
                        )
                        val dotHeight by animateDpAsState(
                            targetValue = if (isActiveDot) 6.dp else 8.dp,
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            label = "admin-visual-dot-height",
                        )
                        val dotBrush = if (isActiveDot) {
                            when (onboardingStep) {
                                0 -> Brush.horizontalGradient(listOf(RfColors.IndigoBrand, RfColors.Teal))
                                1 -> Brush.horizontalGradient(listOf(RfColors.VioletaBrand, RfColors.IndigoSecondary))
                                2 -> Brush.horizontalGradient(listOf(RfColors.Teal, Color(0xFF8DB52C)))
                                else -> Brush.horizontalGradient(listOf(RfColors.IndigoSecondary, RfColors.IndigoBrand))
                            }
                        } else {
                            SolidColor(Color(0xFFD9DCEB))
                        }
                        Box(
                            modifier = Modifier
                                .width(dotWidth)
                                .height(dotHeight)
                                .clip(AdminPillShape)
                                .background(dotBrush)
                                .clickable { onboardingStep = dotIndex },
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AnimatedVisibility(
                        visible = onboardingStep > 0,
                        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.85f),
                        exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.85f),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E4F3), CircleShape)
                                .clickable { onboardingStep = (onboardingStep - 1).coerceAtLeast(0) },
                        ) {
                            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 21.dp)
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(AdminHeroGradient)
                            .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color(0x995B6CF0))
                            .clickable { onboardingStep = (onboardingStep + 1).coerceAtMost(4) }
                            .testTag("admin-onboarding-next"),
                    ) {
                        RfIcon(
                            kind = RfIconKind.BACK,
                            tint = Color.White,
                            size = 24.dp,
                            modifier = Modifier.rotate(180f),
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawAdminOnboardingBackground(step: Int) {
    drawRect(Color(0xFFF6F5FC))
    val primaryGlow = when (step) {
        0 -> Color(0xFFE7E4FF)
        1 -> Color(0xFFF2E2FA)
        2 -> Color(0xFFDDF4F3)
        3 -> Color(0xFFE3EAFF)
        else -> Color(0xFFF6E3F4)
    }
    val secondaryGlow = when (step) {
        0 -> Color(0xFFDDF5F6)
        1 -> Color(0xFFE2EBFF)
        2 -> Color(0xFFEEF6D6)
        else -> Color(0xFFDDF3FA)
    }
    drawRect(
        Brush.radialGradient(
            listOf(primaryGlow, primaryGlow.copy(alpha = 0f)),
            center = Offset(size.width * 0.15f, size.height * 0.08f),
            radius = size.width * 1.05f,
        ),
    )
    drawRect(
        Brush.radialGradient(
            listOf(secondaryGlow, secondaryGlow.copy(alpha = 0f)),
            center = Offset(size.width * 0.92f, size.height * 0.42f),
            radius = size.width * 0.9f,
        ),
    )
    val scaleFactor = size.width / 390f
    scale(scaleFactor, scaleFactor, Offset.Zero) {
        drawCircle(color = Color(0xFFD5D7F7), radius = 22f, center = Offset(34f, 750f), style = Stroke(width = 2f))
        drawCircle(color = Color(0xFFCDEEEB), radius = 30f, center = Offset(352f, 695f), style = Stroke(width = 1.6f))
        val diamond = Path().apply {
            moveTo(334f, 452f)
            lineTo(342f, 466f)
            lineTo(334f, 480f)
            lineTo(326f, 466f)
            close()
        }
        drawPath(diamond, color = Color(0xFFA7D9DF), style = Stroke(width = 1.6f))
        drawAdminSparkleStar(center = Offset(58f, 488f), radius = 9f, color = RfColors.IndigoBrand)
        drawAdminSparkleStar(center = Offset(302f, 790f), radius = 10f, color = Color(0xFFF5B53B))
    }
}

private fun DrawScope.drawAdminSparkleStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    val innerRadius = radius * 0.28f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + innerRadius, center.y - innerRadius)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + innerRadius, center.y + innerRadius)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - innerRadius, center.y + innerRadius)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - innerRadius, center.y - innerRadius)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

@Composable
private fun AdminOnboardingVisualSlide(
    step: Int,
    summary: AdminOnboardingSummary,
) {
    val typo = LocalRfTypography.current
    val stepTag = when (step) {
        0 -> "01 · OPERACIONES EN VIVO"
        1 -> "02 · COPILOTO ROSA IA"
        2 -> "03 · INVENTARIO Y PLANOS"
        else -> "04 · FINANZAS Y COBROS"
    }
    val tagTint = when (step) {
        0 -> Color(0xFF4B47D6)
        1 -> Color(0xFF865DEC)
        2 -> Color(0xFF0E7490)
        else -> Color(0xFF4E6A10)
    }
    val headline = when (step) {
        0 -> "Tu semana de eventos, de un vistazo"
        1 -> "Rosa IA cuida tu stock por fecha"
        2 -> "Piezas, fotos y bocetos 2D al día"
        else -> "Anticipos claros y cero fugas"
    }
    val subtitle = when (step) {
        0 -> "Supervisa montajes, rutas de entrega y equipo asignado sin llamadas de último minuto."
        1 -> "Calcula disponibilidad real al instante y prepara cotizaciones listas para aprobar."
        2 -> "Controla sillas, arcos y mesas desde el catálogo hasta el plano de cada salón."
        else -> "Verifica transferencias, controla saldos y sigue tus ingresos mensuales en tiempo real."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            when (step) {
                0 -> AdminOnboardingStageOps(summary = summary)
                1 -> AdminOnboardingStageRosa(summary = summary)
                2 -> AdminOnboardingStageInventory(summary = summary)
                else -> AdminOnboardingStageFinance(summary = summary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x1F5B6CF0))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.94f),
                            Color(0xFFFAF9FF).copy(alpha = 0.92f),
                        ),
                    ),
                )
                .border(1.dp, tagTint.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(tagTint.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = stepTag,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = tagTint, letterSpacing = 0.8.sp),
                    )
                }
                BasicText(
                    text = headline,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 24.sp),
                )
                BasicText(
                    text = subtitle,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377), lineHeight = 21.sp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AdminOnboardingStageOps(summary: AdminOnboardingSummary) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAdminStageBlob(
                startColor = Color(0xFFE0DEFF),
                endColor = Color(0xFFD5F3F7),
                starPositions = listOf(Offset(56f, 44f), Offset(306f, 70f), Offset(288f, 286f)),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 26.dp)
                .size(width = 186.dp, height = 132.dp)
                .rotate(6f)
                .shadow(14.dp, RoundedCornerShape(22.dp), spotColor = Color(0x335B6CF0))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(6.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.CORPORATIVO,
                contentDescription = "Evento corporativo",
                cornerRadius = 17.dp,
                bottomScrim = true,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(AdminPillShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                BasicText(
                    text = "${summary.activeEventsCount} eventos activos",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 42.dp)
                .size(width = 236.dp, height = 164.dp)
                .rotate(-3f)
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x455B6CF0))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(6.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.BODA,
                contentDescription = summary.leadEventTitle,
                cornerRadius = 19.dp,
                bottomScrim = true,
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(AdminPillShape)
                    .background(Color(0xCC151729))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF39D98A)))
                BasicText(
                    text = "Montaje en curso",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
            ) {
                BasicText(
                    text = summary.leadEventTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                )
                BasicText(
                    text = "${summary.leadEventDate} · ${summary.leadEventVenue}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFFE7E9FA)),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-6).dp, y = (-16).dp)
                .width(232.dp)
                .rotate(2f)
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0x385B6CF0))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E5F5), RoundedCornerShape(20.dp))
                .padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AdminHeroGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.TRUCK, tint = Color.White, size = 20.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "${summary.leadEventItemsCount} pzas despachadas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Equipo: María, Pedro y Yuli",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF6D6)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 14.dp)
                }
            }
        }
    }
}

@Composable
private fun AdminOnboardingStageRosa(summary: AdminOnboardingSummary) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAdminStageBlob(
                startColor = Color(0xFFF0E6FE),
                endColor = Color(0xFFE0ECFF),
                starPositions = listOf(Offset(76f, 52f), Offset(286f, 44f), Offset(312f, 240f)),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                RosaAvatar(size = RosaAvatarSize.LARGE, customSize = 74.dp, glow = true, forceAdminPalette = true)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .shadow(10.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp), spotColor = Color(0x555B6CF0))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 6.dp, bottomStart = 18.dp))
                    .background(AdminButtonGradient)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                BasicText(
                    text = "¿Qué piezas quedan para el ${summary.leadEventDate}?",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(0.92f)
                    .shadow(14.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp), spotColor = Color(0x385B6CF0))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE4E6F5), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp))
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(
                            text = "¡Stock listo sin sobreventa!",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                        Box(
                            modifier = Modifier
                                .clip(AdminPillShape)
                                .background(Color(0xFFEEF6D6))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            BasicText(
                                text = "${summary.primaryArticleAvailableStock} libres",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RfPhoto(
                            photoKey = RfPhotoKey.SILLA_TIFFANY,
                            contentDescription = "Silla Tiffany",
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f).height(64.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.MESA_REDONDA,
                            contentDescription = "Mesa redonda",
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f).height(64.dp),
                        )
                        RfPhoto(
                            photoKey = RfPhotoKey.CENTRO_FLORAL,
                            contentDescription = "Centro floral",
                            cornerRadius = 12.dp,
                            modifier = Modifier.weight(1f).height(64.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOnboardingStageInventory(summary: AdminOnboardingSummary) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAdminStageBlob(
                startColor = Color(0xFFD6F3EE),
                endColor = Color(0xFFE7ECFF),
                starPositions = listOf(Offset(46f, 64f), Offset(298f, 38f), Offset(48f, 276f)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(98.dp)
                    .rotate(-3f)
                    .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x332FB8C8))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(5.dp),
            ) {
                RfPhoto(
                    photoKey = RfPhotoKey.ARCO_ROSA,
                    contentDescription = "Arco orgánico",
                    cornerRadius = 15.dp,
                    bottomScrim = true,
                    modifier = Modifier.fillMaxSize(),
                )
                BasicText(
                    text = "Arcos y globos",
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(98.dp)
                    .offset(y = 12.dp)
                    .rotate(3f)
                    .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x335B6CF0))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(5.dp),
            ) {
                RfPhoto(
                    photoKey = RfPhotoKey.MESA_HELADOS,
                    contentDescription = "Mesa dulce",
                    cornerRadius = 15.dp,
                    bottomScrim = true,
                    modifier = Modifier.fillMaxSize(),
                )
                BasicText(
                    text = "Estaciones dulces",
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
                .height(108.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0x385B6CF0))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E4F4), RoundedCornerShape(22.dp))
                .padding(12.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.TABLE, tint = Color(0xFF4B47D6), size = 16.dp)
                        BasicText(
                            text = "Boceto 2D del evento",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(AdminPillShape)
                            .background(Color(0xFFECEBFF))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        BasicText(
                            text = "${summary.totalArticlesInCatalog} artículos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                    val gridStep = 18.dp.toPx()
                    var lineX = gridStep
                    while (lineX < size.width) {
                        drawLine(Color(0xFFF1F2FA), Offset(lineX, 0f), Offset(lineX, size.height), strokeWidth = 1f)
                        lineX += gridStep
                    }
                    val stageRectWidth = size.width * 0.24f
                    drawRoundRect(
                        color = Color(0xFFEAE7FF),
                        topLeft = Offset(size.width * 0.38f, 6.dp.toPx()),
                        size = Size(stageRectWidth, 16.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    )
                    val centers = listOf(
                        Offset(size.width * 0.22f, size.height * 0.62f),
                        Offset(size.width * 0.50f, size.height * 0.66f),
                        Offset(size.width * 0.78f, size.height * 0.62f),
                    )
                    val tableRadius = 11.dp.toPx()
                    val chairOffsetRadius = 17.dp.toPx()
                    val chairRadius = 2.8.dp.toPx()
                    for (centerPoint in centers) {
                        drawCircle(RfColors.IndigoTint, radius = tableRadius, center = centerPoint)
                        drawCircle(RfColors.IndigoBrand, radius = tableRadius, center = centerPoint, style = Stroke(width = 2.dp.toPx()))
                        for (chairIdx in 0 until 6) {
                            val angleRad = (chairIdx * (2.0 * PI) / 6.0)
                            val cx = centerPoint.x + (cos(angleRad) * chairOffsetRadius).toFloat()
                            val cy = centerPoint.y + (sin(angleRad) * chairOffsetRadius).toFloat()
                            drawCircle(Color(0xFF5B6CF0), radius = chairRadius, center = Offset(cx, cy))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOnboardingStageFinance(summary: AdminOnboardingSummary) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAdminStageBlob(
                startColor = Color(0xFFE8E6FF),
                endColor = Color(0xFFEEF6D6),
                starPositions = listOf(Offset(54f, 44f), Offset(312f, 52f), Offset(34f, 240f)),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp, start = 14.dp, end = 14.dp)
                .fillMaxWidth()
                .height(148.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x385B6CF0))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E5F5), RoundedCornerShape(24.dp))
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        BasicText(
                            text = "Facturado este mes",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)),
                        )
                        BasicText(
                            text = "RD$ ${summary.monthlyIncomeDop}",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(AdminPillShape)
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFF4E6A10), size = 14.dp)
                        BasicText(
                            text = "+24 %",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                        )
                    }
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
                    val heights = listOf(0.42f, 0.58f, 0.50f, 0.74f, 0.66f, 0.92f)
                    val slotWidth = size.width / heights.size
                    val barWidth = slotWidth * 0.54f
                    val trendPoints = mutableListOf<Offset>()
                    heights.forEachIndexed { index, ratio ->
                        val left = index * slotWidth + (slotWidth - barWidth) / 2f
                        val barHeight = size.height * ratio
                        val top = size.height - barHeight
                        val isPeak = index == heights.lastIndex
                        val barBrush = if (isPeak) {
                            Brush.verticalGradient(listOf(Color(0xFF7C5CF0), Color(0xFF2FB8C8)))
                        } else {
                            SolidColor(Color(0xFFEAEBFF))
                        }
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                        )
                        trendPoints.add(Offset(left + barWidth / 2f, (top - 8.dp.toPx()).coerceAtLeast(4.dp.toPx())))
                    }
                    val linePath = Path().apply {
                        trendPoints.forEachIndexed { pointIdx, pt ->
                            if (pointIdx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                    }
                    drawPath(linePath, color = RfColors.IndigoBrand, style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    trendPoints.lastOrNull()?.let { peakPoint ->
                        drawCircle(Color.White, radius = 5.dp.toPx(), center = peakPoint)
                        drawCircle(RfColors.IndigoBrand, radius = 3.2.dp.toPx(), center = peakPoint)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-12).dp)
                .width(248.dp)
                .rotate(-2f)
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0x384E6A10))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0EAD0), RoundedCornerShape(20.dp))
                .padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF6D6)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF4E6A10), size = 20.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Anticipo 30 % verificado",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Pendiente: RD$ ${summary.depositPendingDop}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAdminStageBlob(
    startColor: Color,
    endColor: Color,
    starPositions: List<Offset>,
) {
    val scaleX = size.width / 350f
    val scaleY = size.height / 320f
    scale(scaleX, scaleY, Offset.Zero) {
        val blobPath = Path().apply {
            moveTo(175f, 16f)
            cubicTo(268f, 10f, 340f, 64f, 342f, 152f)
            cubicTo(344f, 240f, 288f, 312f, 178f, 312f)
            cubicTo(68f, 312f, 8f, 268f, 10f, 166f)
            cubicTo(12f, 64f, 82f, 22f, 175f, 16f)
            close()
        }
        drawPath(
            blobPath,
            brush = Brush.linearGradient(
                listOf(startColor, endColor),
                start = Offset(0f, 0f),
                end = Offset(350f, 320f),
            ),
            style = Fill,
        )
        starPositions.forEachIndexed { idx, point ->
            val tint = if (idx % 2 == 0) RfColors.IndigoSecondary else RfColors.Teal
            drawAdminSparkleStar(center = point, radius = 8f, color = tint)
        }
    }
}

@Composable
private fun AdminOnboardingEntryStage(
    rememberChoice: Boolean,
    onPrimaryAdminClick: () -> Unit,
    onClientModeClick: () -> Unit,
    onToggleRemember: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(108.dp),
        ) {
            Box(modifier = Modifier.size(106.dp).background(Color(0x145B6CF0), CircleShape))
            Box(modifier = Modifier.size(116.dp).background(Color(0x1A2FB8C8), CircleShape))
            RfLogo(customSize = 88.dp, insetPadding = false)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText(
                    text = "Rosa Fiesta",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                )
                BasicText(
                    text = "Admin",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = AdminHeroGradient,
                    ),
                )
            }
            BasicText(
                text = "Elige cómo deseas entrar hoy",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.BODA,
                contentDescription = "Eventos",
                cornerRadius = 16.dp,
                modifier = Modifier.weight(1f).height(68.dp),
            )
            RfPhoto(
                photoKey = RfPhotoKey.SILLA_TIFFANY,
                contentDescription = "Mobiliario",
                cornerRadius = 16.dp,
                modifier = Modifier.weight(1f).height(68.dp),
            )
            RfPhoto(
                photoKey = RfPhotoKey.ARCO_ROSA,
                contentDescription = "Decoración",
                cornerRadius = 16.dp,
                modifier = Modifier.weight(1f).height(68.dp),
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .rfHeroBannerShadow(RoundedCornerShape(18.dp), isAdmin = true)
                .clip(RoundedCornerShape(18.dp))
                .background(AdminHeroGradient)
                .clickable(onClick = onPrimaryAdminClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BAR_CHART, tint = Color.White, size = 18.dp)
                }

                BasicText(
                    text = "Entrar al panel Admin",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    modifier = Modifier.weight(1f),
                )

                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 18.dp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x28C850AA))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECE7F1), RoundedCornerShape(18.dp))
                .clickable(onClick = onClientModeClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PARTY, tint = Color.White, size = 17.dp)
                }

                BasicText(
                    text = "Entrar como cliente",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    modifier = Modifier.weight(1f),
                )

                RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
            }
        }

        Row(
            modifier = Modifier
                .clickable(onClick = onToggleRemember)
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val checkBg = if (rememberChoice) AdminButtonGradient else SolidColor(Color.White)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(checkBg)
                    .border(
                        width = 1.5.dp,
                        color = if (rememberChoice) Color.Transparent else Color(0xFFDCDDEA),
                        shape = RoundedCornerShape(7.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (rememberChoice) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                }
            }
            BasicText(
                text = "Recordar mi elección",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
            )
        }
    }
}

private fun defaultDeliveryZones(): List<DeliveryZone> = listOf(
    DeliveryZone(id = "zone-dn", name = "Distrito Nacional", feeDop = 250, etaMinutes = 35, active = true),
    DeliveryZone(id = "zone-sde-sdo", name = "Santo Domingo Este y Oeste", feeDop = 350, etaMinutes = 50, active = true),
    DeliveryZone(id = "zone-sdn", name = "Santo Domingo Norte", feeDop = 400, etaMinutes = 60, active = true),
)

@Composable
private fun SettingsEditableRow(
    icon: RfIconKind,
    label: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorMessage: String? = null,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RfIcon(
                kind = icon,
                tint = if (errorMessage != null) Color(0xFFD63031) else Color(0xFF5F6377),
                size = 18.dp,
            )
            BasicText(
                text = label,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
            )
            if (cursorMask != null) {
                RfMaskedDigitBasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maskTransform = cursorMask,
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF3A3D52), textAlign = TextAlign.End),
                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                    modifier = Modifier.weight(1f),
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF3A3D52), textAlign = TextAlign.End),
                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

private fun validateZoneListForSettings(zones: List<DeliveryZone>): String? {
    zones.forEach { zoneItem ->
        val nameError = InputValidator.validateEntityName(
            rawValue = zoneItem.name,
            fieldLabel = "El nombre de la zona",
            minLength = 2,
            maxLength = 50,
            isRequired = true,
        )
        if (nameError != null) {
            return nameError
        }
        val feeOutOfRange = eitherTrue(zoneItem.feeDop < 0, zoneItem.feeDop > ADMIN_MAX_DELIVERY_FEE_DOP)
        if (feeOutOfRange) {
            return "La tarifa de ${zoneItem.name} debe estar entre RD$ 0 y RD$ $ADMIN_MAX_DELIVERY_FEE_DOP."
        }
        val etaOutOfRange = eitherTrue(zoneItem.etaMinutes < 5, zoneItem.etaMinutes > 240)
        if (etaOutOfRange) {
            return "El ETA de ${zoneItem.name} debe estar entre 5 y 240 minutos."
        }
    }
    return null
}

private enum class AdminSettingsSection(
    val icon: RfIconKind,
    val title: String,
    val fallbackSubtitle: String,
) {
    BUSINESS_DATA(
        icon = RfIconKind.STORE,
        title = "Datos del negocio y facturación",
        fallbackSubtitle = "Nombre, RNC, contacto y horario",
    ),
    DELIVERY_ZONES(
        icon = RfIconKind.LOCATION,
        title = "Entregas · Zonas de cobertura",
        fallbackSubtitle = "Tarifas y zonas de entrega",
    ),
    RESERVATIONS_DEPOSIT(
        icon = RfIconKind.RECEIPT,
        title = "Reservas y anticipo",
        fallbackSubtitle = "Porcentaje de anticipo requerido",
    ),
    AUDIO(
        icon = RfIconKind.BELL,
        title = "Audio y efectos de sonido",
        fallbackSubtitle = "Efectos de sonido y volumen",
    ),
    SECURITY_PREFS(
        icon = RfIconKind.SHIELD,
        title = "Seguridad y preferencias de cuenta",
        fallbackSubtitle = "Notificaciones, biometría y sincronización",
    ),
}

@Composable
internal fun RenderAdminSettingsA31(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val settings = viewModel?.businessSettings?.collectAsState()?.value ?: BusinessSettings(zones = defaultDeliveryZones())
    val vmError = viewModel?.formError?.collectAsState()?.value

    var businessName by remember(settings.businessName) { mutableStateOf(settings.businessName) }
    var rncNumber by remember(settings.rnc) {
        mutableStateOf(InputValidator.formatRncDisplay(settings.rnc.ifBlank { "1-31-84920-2" }))
    }
    var phoneValue by remember(settings.phone) { mutableStateOf(InputValidator.formatPhoneDisplay(settings.phone)) }
    var whatsappValue by remember(settings.whatsapp) { mutableStateOf(InputValidator.formatPhoneDisplay(settings.whatsapp)) }
    var emailValue by remember(settings.email) { mutableStateOf(settings.email) }
    var addressValue by remember(settings.address) { mutableStateOf(settings.address) }
    var businessHoursValue by remember(settings.businessHours) { mutableStateOf(settings.businessHours) }
    var depositPercent by remember(settings.depositPercent) { mutableStateOf(settings.depositPercent.coerceIn(10, 90)) }
    var draftZones by remember(settings.zones) {
        mutableStateOf(settings.zones.ifEmpty { defaultDeliveryZones() })
    }

    var saveAttempted by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var rncTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var whatsappTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }
    var hoursTouched by remember { mutableStateOf(false) }

    var showAddZoneModal by remember { mutableStateOf(false) }
    var newZoneName by remember { mutableStateOf("") }
    var newZoneFeeText by remember { mutableStateOf("") }
    var newZoneEtaText by remember { mutableStateOf("") }
    var zoneNameTouched by remember { mutableStateOf(false) }
    var zoneFeeTouched by remember { mutableStateOf(false) }
    var zoneEtaTouched by remember { mutableStateOf(false) }
    var zoneSubmitAttempted by remember { mutableStateOf(false) }
    var localZoneError by remember { mutableStateOf<String?>(null) }
    var localSaveError by remember { mutableStateOf<String?>(null) }

    val nameError = rememberDebouncedValidationError(
        value = businessName,
        hasInteracted = nameTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateBusinessName(candidate, isRequired = true)
    }
    val rncError = rememberDebouncedValidationError(
        value = rncNumber,
        hasInteracted = rncTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateRnc(candidate, isRequired = true)
    }
    val phoneError = rememberDebouncedValidationError(
        value = phoneValue,
        hasInteracted = phoneTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateDominicanPhone(
            rawValue = candidate,
            isRequired = true,
            allowInternational = true,
            fieldLabel = "El teléfono",
        )
    }
    val whatsappError = rememberDebouncedValidationError(
        value = whatsappValue,
        hasInteracted = whatsappTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateWhatsApp(
            rawValue = candidate,
            isRequired = true,
            allowInternational = true,
        )
    }
    val emailError = rememberDebouncedValidationError(
        value = emailValue,
        hasInteracted = emailTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateEmail(candidate)
    }
    val addressError = rememberDebouncedValidationError(
        value = addressValue,
        hasInteracted = addressTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateAddress(candidate, isRequired = true)
    }
    val hoursError = rememberDebouncedValidationError(
        value = businessHoursValue,
        hasInteracted = hoursTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateBusinessHours(candidate, isRequired = true)
    }

    val zoneNameError = rememberDebouncedValidationError(
        value = newZoneName,
        hasInteracted = zoneNameTouched,
        forceShowError = zoneSubmitAttempted,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "El nombre de la zona",
            minLength = 2,
            maxLength = 50,
            isRequired = true,
        )
    }
    val zoneFeeError = rememberDebouncedValidationError(
        value = newZoneFeeText,
        hasInteracted = zoneFeeTouched,
        forceShowError = zoneSubmitAttempted,
    ) { candidate ->
        InputValidator.validateIntegerCount(
            rawValue = candidate,
            fieldLabel = "La tarifa",
            minValue = 0,
            maxValue = ADMIN_MAX_DELIVERY_FEE_DOP,
            isRequired = true,
        )
    }
    val zoneEtaError = rememberDebouncedValidationError(
        value = newZoneEtaText,
        hasInteracted = zoneEtaTouched,
        forceShowError = zoneSubmitAttempted,
    ) { candidate ->
        InputValidator.validateIntegerCount(
            rawValue = candidate,
            fieldLabel = "El tiempo estimado",
            minValue = 5,
            maxValue = 240,
            isRequired = true,
        )
    }

    var selectedSection by remember { mutableStateOf<AdminSettingsSection?>(null) }
    val displayedError = localSaveError ?: vmError
    val activeHeaderTitle = selectedSection?.title ?: "Ajustes del negocio"

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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .testTag("admin-settings-back-button")
                            .clickable {
                                if (selectedSection != null) {
                                    selectedSection = null
                                } else {
                                    onSelectScreenCode("BACK:A12")
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    BasicText(
                        text = activeHeaderTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                }

                if (displayedError != null) {
                    AdminInlineErrorBanner(message = displayedError)
                }

                when (selectedSection) {
                    null -> {
                        val activeZonesCount = draftZones.count { zone -> zone.active }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(AdminCardShape20, isAdmin = true)
                                .clip(AdminCardShape20)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                        ) {
                            Column {
                                AdminSettingsSection.entries.forEachIndexed { index, section ->
                                    if (index > 0) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                    }
                                    val sectionSubtitle = when (section) {
                                        AdminSettingsSection.BUSINESS_DATA -> businessName.ifBlank { section.fallbackSubtitle }
                                        AdminSettingsSection.DELIVERY_ZONES -> "$activeZonesCount zonas activas"
                                        AdminSettingsSection.RESERVATIONS_DEPOSIT -> "Anticipo actual: $depositPercent%"
                                        AdminSettingsSection.AUDIO -> section.fallbackSubtitle
                                        AdminSettingsSection.SECURITY_PREFS -> section.fallbackSubtitle
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin-settings-menu-${section.name.lowercase()}")
                                            .clickable { selectedSection = section }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFF2F2FA)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = section.icon, tint = Color(0xFF5B6CF0), size = 18.dp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            BasicText(
                                                text = section.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                            )
                                            BasicText(
                                                text = sectionSubtitle,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF8B8FA8), size = 16.dp)
                                    }
                                }
                            }
                        }
                    }

                    AdminSettingsSection.BUSINESS_DATA -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(AdminCardShape20, isAdmin = true)
                                .clip(AdminCardShape20)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                        ) {
                            Column {
                                SettingsEditableRow(
                                    icon = RfIconKind.STORE,
                                    label = "Nombre",
                                    value = businessName,
                                    errorMessage = nameError,
                                    onValueChange = { updatedText ->
                                        nameTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        businessName = updatedText.take(InputValidator.MAX_BUSINESS_NAME_LENGTH)
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.RECEIPT,
                                    label = "RNC",
                                    value = rncNumber,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    errorMessage = rncError,
                                    cursorMask = InputValidator::filterRncInputWithCursor,
                                    onValueChange = { updatedText ->
                                        rncTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        rncNumber = updatedText
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.PHONE,
                                    label = "Teléfono",
                                    value = phoneValue,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    errorMessage = phoneError,
                                    cursorMask = InputValidator::filterPhoneInputWithCursor,
                                    onValueChange = { updatedText ->
                                        phoneTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        phoneValue = updatedText
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.WHATSAPP,
                                    label = "WhatsApp",
                                    value = whatsappValue,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    errorMessage = whatsappError,
                                    cursorMask = InputValidator::filterPhoneInputWithCursor,
                                    onValueChange = { updatedText ->
                                        whatsappTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        whatsappValue = updatedText
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.MAIL,
                                    label = "Correo",
                                    value = emailValue,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    errorMessage = emailError,
                                    onValueChange = { updatedText ->
                                        emailTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        emailValue = updatedText.take(InputValidator.MAX_EMAIL_LENGTH)
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.LOCATION,
                                    label = "Dirección",
                                    value = addressValue,
                                    errorMessage = addressError,
                                    onValueChange = { updatedText ->
                                        addressTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        addressValue = updatedText.take(InputValidator.MAX_ADDRESS_LENGTH)
                                    },
                                )
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                SettingsEditableRow(
                                    icon = RfIconKind.CLOCK,
                                    label = "Horario",
                                    value = businessHoursValue,
                                    errorMessage = hoursError,
                                    onValueChange = { updatedText ->
                                        hoursTouched = true
                                        localSaveError = null
                                        viewModel?.clearFormError()
                                        businessHoursValue = updatedText.take(InputValidator.MAX_BUSINESS_HOURS_LENGTH)
                                    },
                                )
                            }
                        }
                    }

                    AdminSettingsSection.DELIVERY_ZONES -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(AdminCardShape20, isAdmin = true)
                                .clip(AdminCardShape20)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                        ) {
                            Column {
                                draftZones.forEachIndexed { zoneIndex, zoneItem ->
                                    if (zoneIndex > 0) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        val activeDotColor = if (zoneItem.active) Color(0xFF2FB8C8) else Color(0xFFDCDDEA)
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(activeDotColor)
                                                .clickable {
                                                    draftZones = draftZones.map { currentZone ->
                                                        if (currentZone.id == zoneItem.id) {
                                                            currentZone.copy(active = !currentZone.active)
                                                        } else {
                                                            currentZone
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (zoneItem.active) {
                                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 12.dp)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            BasicText(
                                                text = zoneItem.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                            )
                                            BasicText(
                                                text = "${zoneItem.etaMinutes} min · ${if (zoneItem.active) "Activa" else "Pausada"}",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .height(32.dp)
                                                .clip(AdminPillShape)
                                                .background(Color(0xFFF2F2F8))
                                                .padding(horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        draftZones = draftZones.map { currentZone ->
                                                            if (currentZone.id == zoneItem.id) {
                                                                currentZone.copy(feeDop = (currentZone.feeDop - 50).coerceAtLeast(0))
                                                            } else {
                                                                currentZone
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                BasicText("−", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6CF0)))
                                            }

                                            BasicTextField(
                                                value = zoneItem.feeDop.toString(),
                                                onValueChange = { updatedText ->
                                                    val parsedFee = InputValidator.filterDigitsOnly(updatedText, 5)
                                                        .toIntOrNull()
                                                        ?.coerceIn(0, ADMIN_MAX_DELIVERY_FEE_DOP) ?: 0
                                                    draftZones = draftZones.map { currentZone ->
                                                        if (currentZone.id == zoneItem.id) {
                                                            currentZone.copy(feeDop = parsedFee)
                                                        } else {
                                                            currentZone
                                                        }
                                                    }
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                textStyle = TextStyle(
                                                    fontFamily = typo.displayFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF151729),
                                                    textAlign = TextAlign.Center,
                                                ),
                                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                                modifier = Modifier.width(44.dp),
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        draftZones = draftZones.map { currentZone ->
                                                            if (currentZone.id == zoneItem.id) {
                                                                currentZone.copy(feeDop = (currentZone.feeDop + 50).coerceAtMost(ADMIN_MAX_DELIVERY_FEE_DOP))
                                                            } else {
                                                                currentZone
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                BasicText("+", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6CF0)))
                                            }
                                        }
                                    }
                                }

                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clickable {
                                            localZoneError = null
                                            zoneNameTouched = false
                                            zoneFeeTouched = false
                                            zoneEtaTouched = false
                                            zoneSubmitAttempted = false
                                            showAddZoneModal = true
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                                        BasicText(
                                            text = "+ Agregar zona",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AdminSettingsSection.RESERVATIONS_DEPOSIT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(AdminCardShape20, isAdmin = true)
                                .clip(AdminCardShape20)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                BasicText(
                                    text = "Anticipo para reservar",
                                    modifier = Modifier.weight(1f),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                )
                                Row(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(AdminPillShape)
                                        .background(Color(0xFFF2F2F8))
                                        .padding(horizontal = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable { depositPercent = (depositPercent - 5).coerceAtLeast(10) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                    BasicText(
                                        text = "$depositPercent%",
                                        modifier = Modifier.widthIn(min = 38.dp),
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF151729)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable { depositPercent = (depositPercent + 5).coerceAtMost(90) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                    }
                                }
                            }
                        }
                    }

                    AdminSettingsSection.AUDIO -> {
                        AppAudioSettingsCard(
                            viewModel = viewModel,
                            isEnglish = false,
                            isAdminSurface = true,
                        )
                    }

                    AdminSettingsSection.SECURITY_PREFS -> {
                        val adminPushEnabled = viewModel?.pushNotificationsEnabled?.collectAsState()?.value ?: true
                        val adminWhatsappEnabled = viewModel?.whatsappAlertsEnabled?.collectAsState()?.value ?: true
                        val adminBiometricEnabled = viewModel?.biometricLockEnabled?.collectAsState()?.value ?: false
                        val adminWifiOnlySync = viewModel?.autoSyncWifiOnly?.collectAsState()?.value ?: false
                        val adminOnWifi = viewModel?.isOnWifiConnection() ?: true

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(AdminCardShape20, isAdmin = true)
                                .clip(AdminCardShape20)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                        ) {
                            AdminPrefToggleRowA31(
                                title = "Notificaciones push",
                                subtitle = "Alertas de reservas y pagos pendientes",
                                checked = adminPushEnabled,
                                onToggle = { viewModel?.togglePushNotifications() },
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            AdminPrefToggleRowA31(
                                title = "Alertas por WhatsApp",
                                subtitle = "Seguimiento automático a clientes y recordatorios",
                                checked = adminWhatsappEnabled,
                                onToggle = { viewModel?.toggleWhatsappAlerts() },
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            AdminPrefToggleRowA31(
                                title = "Bloqueo biométrico de la app",
                                subtitle = "Solicitar Face ID, Touch ID o PIN al abrir",
                                checked = adminBiometricEnabled,
                                onToggle = { viewModel?.toggleBiometricLock() },
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            AdminPrefToggleRowA31(
                                title = "Sincronizar solo con Wi‑Fi",
                                subtitle = if (adminOnWifi) "Red actual: Wi‑Fi disponible" else "Sin Wi‑Fi · sincronización automática pausada",
                                checked = adminWifiOnlySync,
                                onToggle = { viewModel?.toggleAutoSyncWifiOnly() },
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFECECF5)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(AdminButtonGradient)
                        .clickable {
                            saveAttempted = true
                            val validationError = firstValidationError(
                                InputValidator.validateBusinessName(businessName, isRequired = true),
                                InputValidator.validateRnc(rncNumber, isRequired = true),
                                InputValidator.validateDominicanPhone(
                                    rawValue = phoneValue,
                                    isRequired = true,
                                    allowInternational = true,
                                    fieldLabel = "El teléfono",
                                ),
                                InputValidator.validateWhatsApp(
                                    rawValue = whatsappValue,
                                    isRequired = true,
                                    allowInternational = true,
                                ),
                                InputValidator.validateEmail(emailValue),
                                InputValidator.validateAddress(addressValue, isRequired = true),
                                InputValidator.validateBusinessHours(businessHoursValue, isRequired = true),
                                validateZoneListForSettings(draftZones),
                            )
                            if (validationError != null) {
                                localSaveError = validationError
                                viewModel?.postError(validationError)
                            } else {
                                val updatedSettings = settings.copy(
                                    businessName = businessName.trim(),
                                    rnc = InputValidator.formatRncDisplay(rncNumber.trim()),
                                    address = addressValue.trim(),
                                    phone = InputValidator.formatPhoneDisplay(phoneValue.trim()),
                                    whatsapp = InputValidator.formatPhoneDisplay(whatsappValue.trim()),
                                    email = emailValue.trim(),
                                    businessHours = businessHoursValue.trim(),
                                    depositPercent = depositPercent.coerceIn(10, 90),
                                    zones = draftZones,
                                )
                                val savedOk = viewModel?.saveBusinessConfig(updatedSettings) ?: true
                                if (savedOk) {
                                    localSaveError = null
                                    viewModel?.clearFormError()
                                    if (selectedSection != null) {
                                        selectedSection = null
                                    } else {
                                        onSelectScreenCode("A12")
                                    }
                                } else {
                                    localSaveError = viewModel.formError.value ?: "No se pudieron guardar los ajustes del negocio."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Guardar cambios",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showAddZoneModal,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(150)),
        ) {
            val scrimInteraction = remember { MutableInteractionSource() }
            val cardInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73141632))
                    .clickable(
                        interactionSource = scrimInteraction,
                        indication = null,
                    ) { showAddZoneModal = false },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .animateEnterExit(
                            enter = scaleIn(initialScale = 0.94f, animationSpec = tween(200)),
                            exit = scaleOut(targetScale = 0.94f, animationSpec = tween(150)),
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .clickable(
                            interactionSource = cardInteraction,
                            indication = null,
                        ) {}
                        .padding(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicText(
                            text = "Nueva zona de entrega",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )

                        if (localZoneError != null) {
                            AdminInlineErrorBanner(message = localZoneError.orEmpty())
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(AdminInputShape14)
                                    .background(Color(0xFFF6F6FB))
                                    .border(1.dp, if (zoneNameError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = newZoneName,
                                    onValueChange = { updatedText ->
                                        zoneNameTouched = true
                                        localZoneError = null
                                        newZoneName = updatedText.take(50)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF151729)),
                                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (newZoneName.isEmpty()) {
                                            BasicText(
                                                text = "Nombre de zona (ej. Boca Chica)",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                            RfValidationErrorText(errorMessage = zoneNameError)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .clip(AdminInputShape14)
                                        .background(Color(0xFFF6F6FB))
                                        .border(1.dp, if (zoneFeeError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = newZoneFeeText,
                                        onValueChange = { updatedText ->
                                            zoneFeeTouched = true
                                            localZoneError = null
                                            newZoneFeeText = InputValidator.filterDigitsOnly(updatedText, 5)
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF151729)),
                                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (newZoneFeeText.isEmpty()) {
                                                BasicText(
                                                    text = "Tarifa RD$",
                                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                                                )
                                            }
                                            innerTextField()
                                        },
                                    )
                                }
                                RfValidationErrorText(errorMessage = zoneFeeError)
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .clip(AdminInputShape14)
                                        .background(Color(0xFFF6F6FB))
                                        .border(1.dp, if (zoneEtaError != null) Color(0xFFD63031) else Color(0xFFE3E4EF), AdminInputShape14)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    BasicTextField(
                                        value = newZoneEtaText,
                                        onValueChange = { updatedText ->
                                            zoneEtaTouched = true
                                            localZoneError = null
                                            newZoneEtaText = InputValidator.filterDigitsOnly(updatedText, 3)
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF151729)),
                                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (newZoneEtaText.isEmpty()) {
                                                BasicText(
                                                    text = "ETA (min)",
                                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                                                )
                                            }
                                            innerTextField()
                                        },
                                    )
                                }
                                RfValidationErrorText(errorMessage = zoneEtaError)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(AdminButtonShape16)
                                    .background(Color(0xFFF2F2F8))
                                    .clickable { showAddZoneModal = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Cancelar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(AdminButtonShape16)
                                    .background(AdminButtonGradient)
                                    .clickable {
                                        zoneSubmitAttempted = true
                                        val firstZoneError = firstValidationError(
                                            InputValidator.validateEntityName(
                                                rawValue = newZoneName,
                                                fieldLabel = "El nombre de la zona",
                                                minLength = 2,
                                                maxLength = 50,
                                                isRequired = true,
                                            ),
                                            InputValidator.validateIntegerCount(
                                                rawValue = newZoneFeeText,
                                                fieldLabel = "La tarifa",
                                                minValue = 0,
                                                maxValue = ADMIN_MAX_DELIVERY_FEE_DOP,
                                                isRequired = true,
                                            ),
                                            InputValidator.validateIntegerCount(
                                                rawValue = newZoneEtaText,
                                                fieldLabel = "El tiempo estimado",
                                                minValue = 5,
                                                maxValue = 240,
                                                isRequired = true,
                                            ),
                                        )
                                        if (firstZoneError == null) {
                                            val createdZone = DeliveryZone(
                                                id = "zone-custom-${draftZones.size + 1}",
                                                name = newZoneName.trim(),
                                                feeDop = newZoneFeeText.toIntOrNull() ?: 0,
                                                etaMinutes = newZoneEtaText.toIntOrNull() ?: 45,
                                                active = true,
                                            )
                                            draftZones = draftZones + createdZone
                                            newZoneName = ""
                                            newZoneFeeText = "300"
                                            newZoneEtaText = "45"
                                            zoneNameTouched = false
                                            zoneFeeTouched = false
                                            zoneEtaTouched = false
                                            zoneSubmitAttempted = false
                                            localZoneError = null
                                            showAddZoneModal = false
                                        } else {
                                            localZoneError = firstZoneError
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Agregar",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun fallbackRefundRequestsList(): List<RefundRequest> = listOf(
    RefundRequest(
        id = "ref-rafael-soto",
        eventId = "evt-aniversario-soto",
        eventTitle = "Aniversario Bodas de Plata Soto",
        clientName = "Rafael Soto",
        paidDepositDop = 1800,
        refundPercent = 90,
        refundAmountDop = 1620,
        daysBefore = 19,
        reason = "Reprogramación familiar fuera del país notificada con 19 días de antelación",
        status = "PENDING",
        requestedAtLabel = "19 oct 2026",
    ),
    RefundRequest(
        id = "ref-gabriela-nunez",
        eventId = "evt-baby-gabriela",
        eventTitle = "Baby Shower Pastel Gabriela",
        clientName = "Gabriela Núñez",
        paidDepositDop = 960,
        refundPercent = 50,
        refundAmountDop = 480,
        daysBefore = 9,
        reason = "Adelanto médico de fecha de parto informado a 9 días del montaje",
        status = "PENDING",
        requestedAtLabel = "20 oct 2026",
    ),
)

private fun isRefundProcessed(status: String): Boolean {
    val normalized = status.trim().uppercase()
    return when (normalized) {
        "APPROVED", "APROBADO", "APROBADA", "REFUNDED", "REEMBOLSADO", "REEMBOLSADA", "REIMBURSED", "COMPLETED", "PAGADO" -> true
        else -> false
    }
}

@Composable
internal fun RenderAdminCancellationsA32(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilterKey by remember { mutableStateOf("ALL") }

    val vmRefunds = viewModel?.refundRequests?.collectAsState()?.value
    val refundList = vmRefunds ?: fallbackRefundRequestsList()

    val pendingCount = refundList.count { refundItem -> !isRefundProcessed(refundItem.status) }
    val reimbursedCount = refundList.count { refundItem -> isRefundProcessed(refundItem.status) }

    val visibleRefunds = refundList.filter { refundItem ->
        when (selectedFilterKey) {
            "PENDING" -> !isRefundProcessed(refundItem.status)
            "REFUNDED" -> isRefundProcessed(refundItem.status)
            else -> true
        }
    }

    val filterChips = listOf(
        "ALL" to "Todas · ${refundList.size}",
        "PENDING" to "Por aprobar · $pendingCount",
        "REFUNDED" to "Reembolsadas · $reimbursedCount",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = RfDimens.BottomBarContentClearance),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cancelaciones",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "$pendingCount solicitudes por aprobar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterChips.forEach { chipPair ->
                    val filterKey = chipPair.first
                    val label = chipPair.second
                    val isSelected = selectedFilterKey == filterKey
                    val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(AdminPillShape)
                            .background(chipBrush)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                shape = AdminPillShape,
                            )
                            .clickable { selectedFilterKey = filterKey }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF2E324A),
                            ),
                        )
                    }
                }
            }

            if (visibleRefunds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "No hay solicitudes en esta categoría.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                    )
                }
            } else {
                visibleRefunds.forEach { refundItem ->
                    val isProcessed = isRefundProcessed(refundItem.status)
                    val palette = avatarPaletteForName(refundItem.clientName)
                    val initial = InputValidator.extractInitials(refundItem.clientName, fallback = "C", maxInitials = 1)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                            .clickable {
                                viewModel?.selectRefund(refundItem.id)
                                onSelectScreenCode("A33")
                            }
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(palette.first),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = initial,
                                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = palette.second),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = refundItem.eventTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                    )
                                    BasicText(
                                        text = "${refundItem.clientName} · ${formatReadableSpanishDateText(refundItem.requestedAtLabel)}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(AdminPillShape)
                                        .background(if (isProcessed) Color(0xFFEEF6D6) else Color(0xFFFCE3E8))
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = if (isProcessed) "Reembolsada" else "Por aprobar",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isProcessed) Color(0xFF4E6A10) else Color(0xFFDC2B48),
                                        ),
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AdminInputShape14)
                                    .background(Color(0xFFF6F6FB))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                                    BasicText("RD$ ${refundItem.paidDepositDop}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText("Política", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                                    BasicText("${refundItem.refundPercent}%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText("Reembolso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                                    BasicText("RD$ ${refundItem.refundAmountDop}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                                }
                            }

                            BasicText(
                                text = "Motivo: “${refundItem.reason}” · ${refundItem.daysBefore} días antes del montaje.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF5F6377)),
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(AdminButtonShape16)
                                    .background(if (isProcessed) SolidColor(Color(0xFFF2F2F8)) else AdminButtonGradient)
                                    .clickable {
                                        viewModel?.selectRefund(refundItem.id)
                                        onSelectScreenCode("A33")
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = if (isProcessed) "Ver comprobante de reembolso" else "Revisar reembolso",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProcessed) Color(0xFF2E324A) else Color.White,
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

@Composable
internal fun RenderAdminRefundA33(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var refundMethod by remember { mutableStateOf("Transferencia") }
    var releaseArticles by remember { mutableStateOf(true) }
    var notifyClientWhatsApp by remember { mutableStateOf(true) }

    val selectedRefundId = viewModel?.selectedRefundId?.collectAsState()?.value.orEmpty()
    val vmRefunds = viewModel?.refundRequests?.collectAsState()?.value
    val refundsPool = vmRefunds ?: fallbackRefundRequestsList()
    val activeRefund = refundsPool.firstOrNull { refundItem -> refundItem.id == selectedRefundId }
        ?: viewModel?.selectedRefund()
        ?: refundsPool.firstOrNull()
    if (activeRefund == null) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Reembolso",
            emptyTitle = "Sin solicitudes de reembolso",
            emptyMessage = "No hay cancelaciones o devoluciones pendientes por procesar en este momento.",
            backTargetCode = "BACK:A32",
            ctaLabel = "Volver a cancelaciones",
            ctaTargetCode = "BACK:A32",
            iconKind = RfIconKind.WALLET,
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }

    val firstName = activeRefund.clientName.trim().substringBefore(" ")
    var clientMessage by remember(activeRefund.id) {
        mutableStateOf(
            "Hola $firstName, aprobamos tu solicitud para ${activeRefund.eventTitle}. Te enviamos RD$ ${activeRefund.refundAmountDop} por transferencia en 3 a 5 días hábiles.",
        )
    }
    var clientMessageTouched by remember(activeRefund.id) { mutableStateOf(false) }
    var approveAttempted by remember(activeRefund.id) { mutableStateOf(false) }
    val clientMessageError = rememberDebouncedValidationError(
        value = clientMessage,
        hasInteracted = clientMessageTouched,
        forceShowError = approveAttempted,
    ) { candidate ->
        InputValidator.validateNotes(
            rawValue = candidate,
            fieldLabel = "El mensaje para el cliente",
            maxLength = InputValidator.MAX_NOTES_LENGTH,
            isRequired = notifyClientWhatsApp,
        )
    }

    val retentionDop = (activeRefund.paidDepositDop - activeRefund.refundAmountDop).coerceAtLeast(0)
    val alreadyApproved = isRefundProcessed(activeRefund.status)

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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("BACK:A32") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Reembolso",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "${activeRefund.clientName} · ${activeRefund.eventTitle}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(AdminPillShape)
                            .background(if (alreadyApproved) Color(0xFFEEF6D6) else Color(0xFFFFF2C9))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = if (alreadyApproved) "Reembolsado" else "Pendiente",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alreadyApproved) Color(0xFF4E6A10) else Color(0xFF7A5700),
                            ),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfHeroBannerShadow(AdminHeroShape22, isAdmin = true)
                        .clip(AdminHeroShape22)
                        .background(AdminHeroGradient)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText(
                            text = if (alreadyApproved) "Monto reembolsado" else "Monto a reembolsar (${activeRefund.refundPercent}%)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xD9FFFFFF)),
                        )
                        BasicText(
                            text = "RD$ ${activeRefund.refundAmountDop}",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                        )
                        BasicText(
                            text = "Anticipo RD$ ${activeRefund.paidDepositDop} · Retención RD$ $retentionDop · ${activeRefund.daysBefore} días antes",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xD9FFFFFF)),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminCardShape20, isAdmin = true)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Solicitado el ${formatReadableSpanishDateText(activeRefund.requestedAtLabel)}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                        )
                        BasicText(
                            text = "Motivo reportado: ${activeRefund.reason}",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF2E324A), lineHeight = 19.sp),
                        )
                        if (alreadyApproved) {
                            val approver = activeRefund.approvedBy.ifBlank { "Administración" }
                            val approvedTime = activeRefund.approvedAtLabel.ifBlank { "Confirmado" }
                            BasicText(
                                text = "Aprobado por $approver · $approvedTime",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText(
                        text = "Devolver por",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)),
                    )

                    val isTransfer = refundMethod == "Transferencia"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(if (isTransfer) 2.dp else 1.dp, if (isTransfer) Color(0xFF5B6CF0) else Color(0xFFECECF5), AdminCardShape20)
                            .clickable { refundMethod = "Transferencia" }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFECEBFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4B47D6), size = 19.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText("Transferencia bancaria", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                BasicText("Cuenta registrada de ${activeRefund.clientName}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(if (isTransfer) 6.dp else 2.dp, if (isTransfer) Color(0xFF5B6CF0) else Color(0xFFDCDDEA), CircleShape),
                            )
                        }
                    }

                    val isCash = refundMethod == "Efectivo"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(if (isCash) 2.dp else 1.dp, if (isCash) Color(0xFF5B6CF0) else Color(0xFFECECF5), AdminCardShape20)
                            .clickable { refundMethod = "Efectivo" }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF4E6A10), size = 19.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText("Efectivo o nota de crédito", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                BasicText("Entrega presencial con recibo firmado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(if (isCash) 6.dp else 2.dp, if (isCash) Color(0xFF5B6CF0) else Color(0xFFDCDDEA), CircleShape),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminCardShape20, isAdmin = true)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { releaseArticles = !releaseArticles }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = "Liberar inventario de ${activeRefund.eventTitle}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                )
                                BasicText(
                                    text = "Los artículos reservados vuelven al catálogo",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                                )
                            }

                            val releaseBrush = if (releaseArticles) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 28.dp)
                                    .clip(CircleShape)
                                    .background(releaseBrush)
                                    .padding(3.dp),
                                contentAlignment = if (releaseArticles) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notifyClientWhatsApp = !notifyClientWhatsApp }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(
                                text = "Notificar a ${activeRefund.clientName} por WhatsApp",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                            )

                            val notifyBrush = if (notifyClientWhatsApp) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 28.dp)
                                    .clip(CircleShape)
                                    .background(notifyBrush)
                                    .padding(3.dp),
                                contentAlignment = if (notifyClientWhatsApp) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Mensaje para ${activeRefund.clientName}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(1.dp, if (clientMessageError != null) Color(0xFFD63031) else Color(0xFFECECF5), AdminCardShape20)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        BasicTextField(
                            value = clientMessage,
                            onValueChange = { updatedText ->
                                clientMessageTouched = true
                                approveAttempted = false
                                clientMessage = updatedText.take(InputValidator.MAX_NOTES_LENGTH)
                            },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF3A3D52), lineHeight = 21.sp),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    RfValidationErrorText(errorMessage = clientMessageError)
                }
            }

            val ctaBrush = if (alreadyApproved) SolidColor(Color(0xFFEEF6D6)) else AdminButtonGradient
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFECECF5)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(ctaBrush)
                        .clickable {
                            if (!alreadyApproved) {
                                approveAttempted = true
                                val messageValidation = InputValidator.validateNotes(
                                    rawValue = clientMessage,
                                    fieldLabel = "El mensaje para el cliente",
                                    maxLength = InputValidator.MAX_NOTES_LENGTH,
                                    isRequired = notifyClientWhatsApp,
                                )
                                if (messageValidation == null) {
                                    viewModel?.approveSelectedRefund()
                                    onSelectScreenCode("A32")
                                }
                            } else {
                                onSelectScreenCode("BACK:A32")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (alreadyApproved) {
                            "Reembolso aprobado ✓ · Volver"
                        } else {
                            "Aprobar y enviar reembolso · RD$ ${activeRefund.refundAmountDop}"
                        },
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alreadyApproved) Color(0xFF4E6A10) else Color.White,
                        ),
                    )
                }
            }
        }
    }
}

private fun fallbackReviewRecord(): ReviewRecord = ReviewRecord(
    id = "rev-camila",
    eventId = "evt-boda-real",
    eventTitle = "Boda Real Rosa & Dorado",
    authorName = "Camila Rodríguez",
    rating = 5,
    comment = "El montaje superó nuestras expectativas; cada silla y arreglo llegó impecable y a tiempo.",
    verified = true,
    dateLabel = "18 oct 2026",
    adminReply = "",
)

private fun buildReviewReplyTemplates(review: ReviewRecord): Map<String, String> {
    val firstName = review.authorName.trim().substringBefore(" ")
    return mapOf(
        "Cálida" to "¡Gracias, $firstName! Nos llena de alegría haber sido parte de ${review.eventTitle}. ¡Esperamos celebrar nuevamente contigo! — Equipo Rosa Fiesta",
        "Formal" to "Estimado/a ${review.authorName}, agradecemos sinceramente su valoración sobre ${review.eventTitle}. Seguimos comprometidos con la excelencia en cada montaje. — Rosa Fiesta",
        "Breve" to "¡Muchas gracias por tu reseña sobre ${review.eventTitle}, $firstName! Fue un honor acompañarte. — Rosa Fiesta",
    )
}

@Composable
internal fun RenderAdminReplyReviewA34(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val selectedReviewId = viewModel?.selectedReviewId?.collectAsState()?.value.orEmpty()
    val vmReviews = viewModel?.reviews?.collectAsState()?.value
    val vmError = viewModel?.formError?.collectAsState()?.value

    val resolvedVmReview = vmReviews?.firstOrNull { reviewItem -> reviewItem.id == selectedReviewId }
        ?: viewModel?.selectedReview()
        ?: vmReviews?.firstOrNull()
    val hasViewModel = viewModel != null
    val hasNoReview = resolvedVmReview == null
    val showEmptyReviewsState = if (hasViewModel) hasNoReview else false
    if (showEmptyReviewsState) {
        RenderAdminEmptyFeatureState(
            headerTitle = "Responder reseña",
            emptyTitle = "Sin reseñas por responder",
            emptyMessage = "Cuando tus clientes publiquen opiniones sobre sus eventos aparecerán aquí para responderles.",
            backTargetCode = "BACK:s71",
            iconKind = RfIconKind.STAR,
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val activeReview = resolvedVmReview ?: fallbackReviewRecord()

    val toneTemplates = remember(activeReview.id, activeReview.authorName, activeReview.eventTitle) {
        buildReviewReplyTemplates(activeReview)
    }

    var selectedTone by remember(activeReview.id) { mutableStateOf("Cálida") }
    var replyText by remember(activeReview.id, activeReview.adminReply) {
        mutableStateOf(
            activeReview.adminReply.ifBlank { toneTemplates["Cálida"].orEmpty() },
        )
    }
    var replyTouched by remember(activeReview.id) { mutableStateOf(false) }
    var replySubmitAttempted by remember(activeReview.id) { mutableStateOf(false) }
    var publishAndVerify by remember { mutableStateOf(true) }
    var localReplyError by remember { mutableStateOf<String?>(null) }

    val replyFieldError = rememberDebouncedValidationError(
        value = replyText,
        hasInteracted = replyTouched,
        forceShowError = replySubmitAttempted,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "La respuesta pública",
            minLength = 4,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
            isRequired = true,
        )
    }
    val displayedError = localReplyError ?: vmError
    val palette = avatarPaletteForName(activeReview.authorName)
    val authorInitial = InputValidator.extractInitials(activeReview.authorName, fallback = "C", maxInitials = 1)
    val clampedRating = activeReview.rating.coerceIn(1, 5)

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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("BACK:s71") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = if (activeReview.adminReply.isNotBlank()) "Editar respuesta" else "Responder reseña",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "${activeReview.eventTitle} · ${formatReadableSpanishDateText(activeReview.dateLabel)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }

                if (displayedError != null) {
                    AdminInlineErrorBanner(message = displayedError)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminCardShape20, isAdmin = true)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(palette.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = authorInitial,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = palette.second),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = activeReview.authorName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                BasicText(
                                    text = "${activeReview.eventTitle} · ${formatReadableSpanishDateText(activeReview.dateLabel)}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                for (starPosition in 1..5) {
                                    val filled = starPosition <= clampedRating
                                    RfIcon(
                                        kind = RfIconKind.STAR,
                                        tint = if (filled) Color(0xFFF7B500) else Color(0xFFDCDDEA),
                                        size = 16.dp,
                                    )
                                }
                            }
                        }

                        BasicText(
                            text = "“${activeReview.comment}”",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MINI, customSize = 30.dp, forceAdminPalette = true)
                    BasicText(
                        text = "Plantillas sugeridas por Rosa IA",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                    )
                    Row(
                        modifier = Modifier.clickable {
                            val tones = listOf("Cálida", "Formal", "Breve")
                            val nextIdx = (tones.indexOf(selectedTone) + 1) % tones.size
                            val nextTone = tones[nextIdx]
                            selectedTone = nextTone
                            localReplyError = null
                            replyText = toneTemplates[nextTone].orEmpty()
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RfIcon(kind = RfIconKind.REFRESH, tint = Color(0xFF4B47D6), size = 15.dp)
                        BasicText(
                            text = "Otra",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cálida", "Formal", "Breve").forEach { toneName ->
                        val isSelected = selectedTone == toneName
                        val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(chipBrush)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                    shape = CircleShape,
                                )
                                .clickable {
                                    selectedTone = toneName
                                    localReplyError = null
                                    replyText = toneTemplates[toneName].orEmpty()
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = toneName,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(AdminCardShape20, isAdmin = true)
                            .clip(AdminCardShape20)
                            .background(Color.White)
                            .border(
                                width = 2.dp,
                                color = if (replyFieldError != null) Color(0xFFD63031) else Color(0xFF5B6CF0),
                                shape = AdminCardShape20,
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        BasicTextField(
                            value = replyText,
                            onValueChange = { updatedText ->
                                replyTouched = true
                                replySubmitAttempted = false
                                localReplyError = null
                                viewModel?.clearFormError()
                                replyText = updatedText.take(InputValidator.MAX_NOTES_LENGTH)
                            },
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (replyText.isEmpty()) {
                                    BasicText(
                                        text = "Escribe tu respuesta pública para ${activeReview.authorName}…",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377)),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                    RfValidationErrorText(errorMessage = replyFieldError)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { publishAndVerify = !publishAndVerify }
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Publicar y verificar la reseña",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "La respuesta será visible en la ficha pública",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    val publishBrush = if (publishAndVerify) AdminButtonGradient else SolidColor(Color(0xFFE3E4EF))
                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(publishBrush)
                            .padding(3.dp),
                        contentAlignment = if (publishAndVerify) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFECECF5)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                        .clip(AdminButtonShape16)
                        .background(AdminButtonGradient)
                        .clickable {
                            replySubmitAttempted = true
                            val replyValidation = InputValidator.validateEntityName(
                                rawValue = replyText,
                                fieldLabel = "La respuesta pública",
                                minLength = 4,
                                maxLength = InputValidator.MAX_NOTES_LENGTH,
                                isRequired = true,
                            )
                            if (replyValidation != null) {
                                localReplyError = replyValidation
                                viewModel?.postError(replyValidation)
                            } else {
                                val trimmedReply = replyText.trim()
                                val publishedOk = viewModel?.replyToSelectedReview(trimmedReply) ?: true
                                if (publishedOk) {
                                    localReplyError = null
                                    viewModel?.clearFormError()
                                    onSelectScreenCode("BACK:s71")
                                } else {
                                    localReplyError = viewModel.formError.value ?: "No se pudo publicar la respuesta."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = "Publicar respuesta",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminEmptyStateA35(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedCategory by remember { mutableStateOf("Todos") }
    var shareConfirmation by remember { mutableStateOf<String?>(null) }

    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
    val vmArticles = viewModel?.articles?.collectAsState()?.value
    val categories = remember(vmCategories, vmArticles) {
        val dynamicNames = when {
            vmCategories != null -> vmCategories.map { category -> category.name.trim() }.filter { name -> name.isNotEmpty() }
            vmArticles != null -> vmArticles.map { article -> article.category.trim() }.filter { name -> name.isNotEmpty() }
            else -> listOf("Mobiliario", "Mesas", "Decoración", "Globos")
        }
        listOf("Todos") + dynamicNames.distinctBy { name -> name.lowercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF6F6FB))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF1E9FB), Color(0x00F1E9FB)),
                    center = Offset(0f, size.height * 0.12f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = RfDimens.BottomBarContentClearance),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Solicitudes por cotizar",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Filtro activo: $selectedCategory",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                categories.forEach { categoryLabel ->
                    val isSelected = selectedCategory == categoryLabel
                    val chipBrush = if (isSelected) AdminButtonGradient else SolidColor(Color.White)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(36.dp)
                            .clip(AdminPillShape)
                            .background(chipBrush)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFFE9E9F2),
                                shape = AdminPillShape,
                            )
                            .clickable {
                                selectedCategory = categoryLabel
                                shareConfirmation = null
                            }
                            .padding(horizontal = 14.dp),
                    ) {
                        BasicText(
                            text = categoryLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF5F6377),
                            ),
                        )
                    }
                }
            }

            if (shareConfirmation != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AdminInputShape14)
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = shareConfirmation.orEmpty(),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(112.dp)
                        .background(Color(0x8CECEBFF), CircleShape),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFECEBFF), Color(0xFFDDF4F3)))),
                    ) {
                        RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF4B47D6), size = 50.dp)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                BasicText(
                    text = if (selectedCategory == "Todos") "No tienes solicitudes nuevas por cotizar" else "Sin solicitudes en $selectedCategory",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), textAlign = TextAlign.Center),
                )

                Spacer(modifier = Modifier.height(10.dp))

                BasicText(
                    text = "Crea una cotización manual o asistida con Rosa IA, o comparte el enlace de tu catálogo para recibir nuevas solicitudes.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF5F6377),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    ),
                    modifier = Modifier.width(290.dp),
                )

                Spacer(modifier = Modifier.height(26.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(elevation = 12.dp, shape = AdminButtonShape16, spotColor = Color(0xBF5B6CF0))
                            .clip(AdminButtonShape16)
                            .background(AdminButtonGradient)
                            .clickable {
                                onSelectScreenCode("A04")
                            },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Crear cotización manual",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                            .clip(AdminButtonShape16)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), AdminButtonShape16)
                            .clickable {
                                val shared = viewModel?.sharePlainText(
                                    "Catálogo RosaFiesta",
                                    "https://rosafiesta.do/catalogo",
                                ) ?: true
                                if (shared) {
                                    shareConfirmation = "Enlace compartido: https://rosafiesta.do/catalogo"
                                }
                            },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF151729), size = 18.dp)
                            BasicText(
                                text = "Compartir enlace",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminLoadingStateA36(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var isSyncingNow by remember { mutableStateOf(false) }
    var syncStatusLabel by remember { mutableStateOf("Sincronizando inventario, agenda y cobros…") }

    val infiniteTransition = rememberInfiniteTransition(label = "admin-loading-pulse")
    val barProgress by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "admin-loading-bar-progress",
    )

    val skeletonGradient = Brush.horizontalGradient(
        listOf(Color(0xFFE4E5F0), Color(0xFFF1F1F8), Color(0xFFE4E5F0)),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF6F6FB))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 1.1f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = RfDimens.BottomBarContentClearance),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Actualizando panel",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = syncStatusLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(AdminPillShape)
                        .background(AdminButtonGradient)
                        .clickable {
                            if (!isSyncingNow) {
                                isSyncingNow = true
                                syncStatusLabel = "Conectando con servidor RosaFiesta…"
                                if (viewModel != null) {
                                    viewModel.syncWithServer { syncedOk ->
                                        isSyncingNow = false
                                        syncStatusLabel = if (syncedOk) {
                                            "Sincronización completada"
                                        } else {
                                            "Modo local activo · reintentando"
                                        }
                                    }
                                } else {
                                    isSyncingNow = false
                                    syncStatusLabel = "Sincronización completada"
                                }
                            }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 14.dp)
                        BasicText(
                            text = "Sincronizar ahora",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(AdminPillShape)
                    .background(Color(0xFFE3E4EF)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barProgress)
                        .height(6.dp)
                        .clip(AdminPillShape)
                        .background(AdminHeroGradient),
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(24.dp)).background(skeletonGradient))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skeletonGradient))
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skeletonGradient))
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skeletonGradient))
            }

            Box(modifier = Modifier.fillMaxWidth(0.32f).height(14.dp).clip(RoundedCornerShape(10.dp)).background(skeletonGradient))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(skeletonGradient))
                Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(skeletonGradient))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(AdminCardShape20, isAdmin = true)
                    .clip(AdminCardShape20)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    for (skeletonRow in 0..1) {
                        if (skeletonRow > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(skeletonGradient))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.fillMaxWidth(0.70f).height(13.dp).clip(RoundedCornerShape(10.dp)).background(skeletonGradient))
                                Box(modifier = Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(10.dp)).background(skeletonGradient))
                            }
                            Box(modifier = Modifier.size(width = 56.dp, height = 16.dp).clip(RoundedCornerShape(10.dp)).background(skeletonGradient))
                        }
                    }
                }
            }
        }
    }
}

private fun fallbackOfflineQueueItems(): List<OfflineQueueItem> = listOf(
    OfflineQueueItem(
        id = "oq-1",
        actionLabel = "Ajuste de stock · Silla Tiffany dorada",
        entityCode = "RF-001",
        timeAgo = "Hace 4 min",
        synced = false,
    ),
    OfflineQueueItem(
        id = "oq-2",
        actionLabel = "Registro de anticipo · Camila Rodríguez",
        entityCode = "EVT-1023",
        timeAgo = "Hace 9 min",
        synced = false,
    ),
    OfflineQueueItem(
        id = "oq-3",
        actionLabel = "Checklist de ruta actualizado",
        entityCode = "RUT-01",
        timeAgo = "Hace 18 min",
        synced = true,
    ),
)

@Composable
private fun OfflineQueueInspectorModal(
    visible: Boolean,
    queueItems: List<OfflineQueueItem>,
    onDismiss: () -> Unit,
    onSyncAndOpenHome: () -> Unit,
) {
    val typo = LocalRfTypography.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        val scrimInteraction = remember { MutableInteractionSource() }
        val sheetSlideModifier = rememberModalSheetSlideModifier()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
                .background(Color(0x7A141632))
                .clickable(
                    interactionSource = scrimInteraction,
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetSlideModifier)
                    .clip(AdminSheetShape28)
                    .background(Color.White)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Cola de sincronización offline",
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                            )
                            BasicText(
                                text = "${queueItems.size} operaciones registradas en este dispositivo",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F8))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF151729), size = 16.dp)
                        }
                    }

                    if (queueItems.isEmpty()) {
                        BasicText(
                            text = "No hay operaciones pendientes en la cola local.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            queueItems.forEach { queueItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(AdminInputShape14)
                                        .background(Color(0xFFF7F7FC))
                                        .border(1.dp, Color(0xFFE6E7F2), AdminInputShape14)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (queueItem.synced) Color(0xFFEEF6D6) else Color(0xFFFFF2C9)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(
                                            kind = if (queueItem.synced) RfIconKind.CHECK else RfIconKind.CLOCK,
                                            tint = if (queueItem.synced) Color(0xFF4E6A10) else Color(0xFF7A5700),
                                            size = 15.dp,
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            text = queueItem.actionLabel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                                        )
                                        BasicText(
                                            text = "${queueItem.entityCode} · ${queueItem.timeAgo}",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(AdminPillShape)
                                            .background(if (queueItem.synced) Color(0xFFEEF6D6) else Color(0xFFFFF2C9))
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                    ) {
                                        BasicText(
                                            text = if (queueItem.synced) "Sincronizado" else "Pendiente",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (queueItem.synced) Color(0xFF4E6A10) else Color(0xFF7A5700),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(AdminButtonShape16)
                            .background(AdminButtonGradient)
                            .clickable(onClick = onSyncAndOpenHome),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 17.dp)
                            BasicText(
                                text = "Sincronizar y volver al inicio",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderAdminErrorStateA37(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var showQueueModal by remember { mutableStateOf(false) }

    val vmQueue = viewModel?.offlineQueue?.collectAsState()?.value
    val queueItems = vmQueue ?: fallbackOfflineQueueItems()
    val pendingChangesCount = queueItems.count { queueItem -> !queueItem.synced }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(Color(0xFF151729))
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color.White, size = 18.dp)
                BasicText(
                    text = "Sin conexión · $pendingChangesCount cambios por sincronizar",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
                BasicText(
                    text = "Ver",
                    modifier = Modifier.clickable { showQueueModal = true },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD45C)),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 92.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = RfDimens.BottomBarContentClearance),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Inventario offline",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "${queueItems.size} acciones en cola local",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0x80FCE3E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE3E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.WARNING,
                            tint = Color(0xFFB42345),
                            size = 50.dp,
                            strokeWidthFactor = 0.075f,
                        )
                    }
                }

                BasicText(
                    text = "No pudimos conectar con el servidor",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), textAlign = TextAlign.Center),
                )

                BasicText(
                    text = "Tus cambios ($pendingChangesCount pendientes) están protegidos en la cola local y se sincronizarán al reconectar.",
                    modifier = Modifier.widthIn(max = 300.dp),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp, textAlign = TextAlign.Center),
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                            .clip(AdminButtonShape16)
                            .background(AdminButtonGradient)
                            .clickable {
                                viewModel?.syncOfflineQueue()
                                onSelectScreenCode("A09")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Reintentar",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                            .clip(AdminButtonShape16)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), AdminButtonShape16)
                            .clickable { showQueueModal = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Ver cola de cambios ($pendingChangesCount)",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                        )
                    }
                }
            }
        }

        OfflineQueueInspectorModal(
            visible = showQueueModal,
            queueItems = queueItems,
            onDismiss = { showQueueModal = false },
            onSyncAndOpenHome = {
                viewModel?.syncOfflineQueue()
                showQueueModal = false
                onSelectScreenCode("A09")
            },
        )
    }
}

@Composable
internal fun RenderAdminSuccessStateA38(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var showQueueModal by remember { mutableStateOf(false) }

    val vmQueue = viewModel?.offlineQueue?.collectAsState()?.value
    val queueItems = vmQueue ?: fallbackOfflineQueueItems()
    val pendingCount = queueItems.count { queueItem -> !queueItem.synced }
    val syncedCount = queueItems.count { queueItem -> queueItem.synced }

    Box(modifier = Modifier.fillMaxSize()) {
        RenderAdminEventDetailA13(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF151729))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB9D84A)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF2E3A08), size = 18.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = if (pendingCount == 0) "Cola sincronizada al día" else "$pendingCount cambios listos para sincronizar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                    BasicText(
                        text = "$syncedCount sincronizados · ${queueItems.size} en historial local",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xBFFFFFFF)),
                    )
                }

                BasicText(
                    text = "Ver",
                    modifier = Modifier.clickable { showQueueModal = true },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD45C)),
                )

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(AdminPillShape)
                        .background(AdminButtonGradient)
                        .clickable {
                            viewModel?.syncOfflineQueue()
                            onSelectScreenCode("A13")
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Sincronizar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        OfflineQueueInspectorModal(
            visible = showQueueModal,
            queueItems = queueItems,
            onDismiss = { showQueueModal = false },
            onSyncAndOpenHome = {
                viewModel?.syncOfflineQueue()
                showQueueModal = false
                onSelectScreenCode("A13")
            },
        )
    }
}

@Composable
internal fun RenderAdminEmptyFeatureState(
    headerTitle: String,
    emptyTitle: String,
    emptyMessage: String,
    backTargetCode: String,
    iconKind: RfIconKind,
    onSelectScreenCode: (String) -> Unit,
    ctaLabel: String? = null,
    ctaTargetCode: String? = null,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode(backTargetCode) },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            BasicText(
                text = headerTitle,
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(AdminCardShape20, isAdmin = true)
                    .clip(AdminCardShape20)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4FC)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = iconKind, tint = Color(0xFF5B6CF0), size = 26.dp)
                    }
                    BasicText(
                        text = emptyTitle,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), textAlign = TextAlign.Center),
                    )
                    BasicText(
                        text = emptyMessage,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 19.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
                    )
                    val hasCtaAction = bothTrue(ctaLabel != null, ctaTargetCode != null)
                    if (hasCtaAction) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(AdminButtonShape16)
                                .background(AdminButtonGradient)
                                .clickable { onSelectScreenCode(ctaTargetCode.orEmpty()) },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = ctaLabel.orEmpty(),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class AdminChatRecommendationSpec(
    val articleId: String,
    val articleName: String,
    val suggestedQty: Int,
    val headline: String,
    val subtitle: String,
)

private fun buildAdminChatRecommendation(
    matchedEvent: ClientEvent?,
    eventTitle: String,
    catalogArticles: List<CatalogArticle>,
): AdminChatRecommendationSpec {
    val guestCount = matchedEvent?.guests?.coerceAtLeast(16) ?: 60
    val currentChairCount = matchedEvent?.items
        ?.filter { line ->
            val isChairCode = line.articleCode == "RF-001"
            val hasChairWord = line.name.lowercase().contains("silla")
            if (isChairCode) true else hasChairWord
        }
        ?.sumOf { line -> line.quantity }
        ?: 0
    val chairDeficit = (guestCount - currentChairCount).coerceAtLeast(0)
    val chairArticle = catalogArticles.firstOrNull { article -> article.id == "art-silla-tiffany" }
        ?: catalogArticles.firstOrNull { article ->
            val inCategory = article.category.lowercase().contains("silla")
            val inName = article.name.lowercase().contains("silla")
            if (inCategory) true else inName
        }
    val tableArticle = catalogArticles.firstOrNull { article -> article.id == "art-mesa-redonda" }
        ?: catalogArticles.firstOrNull()
    val useChairs = if (chairArticle != null) chairDeficit > 0 else false
    val selectedArticle = if (useChairs) chairArticle else tableArticle ?: chairArticle
    val resolvedId = selectedArticle?.id ?: "art-silla-tiffany"
    val resolvedName = selectedArticle?.name ?: "Sillas Tiffany"
    val availableStock = selectedArticle?.totalStock?.coerceAtLeast(1) ?: 20
    val rawQty = if (useChairs) chairDeficit else ((guestCount + 9) / 10).coerceAtLeast(1)
    val boundedQty = rawQty.coerceIn(1, availableStock.coerceAtMost(24))
    return AdminChatRecommendationSpec(
        articleId = resolvedId,
        articleName = resolvedName,
        suggestedQty = boundedQty,
        headline = "Sugerencia Rosa IA · +$boundedQty $resolvedName",
        subtitle = "Stock disponible ($availableStock u.) · $eventTitle",
    )
}

@Composable
private fun AdminPrefToggleRowA31(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
            )
            BasicText(
                text = subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
            )
        }
        RfSwitch(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
    }
}
