package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
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
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChatMessageRecord
import com.jeudry.rosafiesta.domain.ChatThread
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
import kotlinx.coroutines.launch

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
        variants = listOf("Estándar"),
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
internal fun RenderAdminCreateEventA04(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var showDuplicateModal by remember { mutableStateOf(false) }
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
        RenderAdminHomeA01(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable {
                    if (showDuplicateModal) {
                        showDuplicateModal = false
                    } else {
                        onSelectScreenCode("A01")
                    }
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
                            .clickable { onSelectScreenCode("A05") }
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
                                    text = "Crear manualmente",
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
                            .clickable { onSelectScreenCode("BACK:A01") },
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
    var fallbackTitle by remember { mutableStateOf("Cumpleaños de Mateo") }
    var fallbackClient by remember { mutableStateOf("Laura Peña") }
    var fallbackDate by remember { mutableStateOf("07/11/2026") }
    var fallbackVenue by remember { mutableStateOf("Terraza Los Prados") }
    var fallbackGuests by remember { mutableStateOf("40") }

    val vmTitle = viewModel?.newEventTitle?.collectAsState()?.value
    val vmClient = viewModel?.newEventClient?.collectAsState()?.value
    val vmDate = viewModel?.newEventDate?.collectAsState()?.value
    val vmVenue = viewModel?.newEventVenue?.collectAsState()?.value
    val vmGuests = viewModel?.newEventGuests?.collectAsState()?.value
    val vmClients = viewModel?.clients?.collectAsState()?.value
    val vmError = viewModel?.formError?.collectAsState()?.value

    val titleValue = vmTitle ?: fallbackTitle
    val clientValue = vmClient ?: fallbackClient
    val dateValue = vmDate ?: fallbackDate
    val venueValue = vmVenue ?: fallbackVenue
    val guestsValue = vmGuests ?: fallbackGuests
    val clientsList = vmClients ?: fallbackClientsList()

    var selectedType by remember { mutableStateOf("Cumpleaños") }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("4:00 p. m.") }
    var notesText by remember { mutableStateOf("Tema de safari, colores verde y beige.") }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val updateTitle: (String) -> Unit = { updatedText ->
        localValidationError = null
        viewModel?.clearFormError()
        if (viewModel != null) {
            viewModel.setNewEventTitle(updatedText)
        } else {
            fallbackTitle = updatedText
        }
    }
    val updateClient: (String) -> Unit = { updatedText ->
        localValidationError = null
        viewModel?.clearFormError()
        if (viewModel != null) {
            viewModel.setNewEventClient(updatedText)
        } else {
            fallbackClient = updatedText
        }
    }
    val updateDate: (String) -> Unit = { updatedText ->
        localValidationError = null
        viewModel?.clearFormError()
        if (viewModel != null) {
            viewModel.setNewEventDate(updatedText)
        } else {
            fallbackDate = updatedText
        }
    }
    val updateVenue: (String) -> Unit = { updatedText ->
        localValidationError = null
        viewModel?.clearFormError()
        if (viewModel != null) {
            viewModel.setNewEventVenue(updatedText)
        } else {
            fallbackVenue = updatedText
        }
    }
    val updateGuests: (String) -> Unit = { updatedText ->
        localValidationError = null
        viewModel?.clearFormError()
        val digitsOnly = updatedText.filter { character -> character.isDigit() }
        if (viewModel != null) {
            viewModel.setNewEventGuests(digitsOnly)
        } else {
            fallbackGuests = digitsOnly
        }
    }

    val advanceIfStepOneValid: (String) -> Unit = { targetCode ->
        val hasTitle = titleValue.trim().isNotEmpty()
        val hasClient = clientValue.trim().isNotEmpty()
        val hasDate = dateValue.trim().isNotEmpty()
        val hasVenue = venueValue.trim().isNotEmpty()
        val parsedGuests = guestsValue.trim().toIntOrNull() ?: 0
        val hasValidGuests = parsedGuests > 0
        val isStepValid = allTrue(hasTitle, hasClient, hasDate, hasVenue, hasValidGuests)
        if (isStepValid) {
            localValidationError = null
            viewModel?.clearFormError()
            onSelectScreenCode(targetCode)
        } else {
            val errorMsg = "Completa nombre del evento, cliente, fecha, lugar e invitados mayores a 0."
            localValidationError = errorMsg
            viewModel?.postError(errorMsg)
        }
    }

    val displayedError = localValidationError ?: vmError
    val parsedGuestsDisplay = guestsValue.trim().toIntOrNull() ?: 0
    val clientInitial = clientValue.trim().firstOrNull()?.uppercase() ?: "C"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
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
                        .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                            modifier = Modifier.fillMaxWidth(),
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
                        .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = clientInitial,
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)),
                            )
                        }

                        BasicTextField(
                            value = clientValue,
                            onValueChange = updateClient,
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.weight(1f),
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
                            .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                            .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicTextField(
                                value = timeText,
                                onValueChange = { updatedText -> timeText = updatedText },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
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
                        .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                            modifier = Modifier.fillMaxWidth(),
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
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .rfInputShadow(AdminInputShape14, isAdmin = true)
                    .clip(AdminInputShape14)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                                modifier = Modifier.width(46.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val nextGuests = parsedGuestsDisplay + 5
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
                        .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    BasicTextField(
                        value = notesText,
                        onValueChange = { updatedText -> notesText = updatedText },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
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

        if (showDatePickerModal) {
            RfInteractiveCalendarModal(
                title = "Fecha del evento",
                currentDateText = dateValue,
                minYear = 2024,
                maxYear = 2030,
                defaultYear = 2026,
                isAdmin = true,
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
    val availabilityDateLabel = vmDate?.ifBlank { "la fecha seleccionada" } ?: "la fecha seleccionada"

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .rfInputShadow(AdminInputShape14, isAdmin = true)
                    .clip(AdminInputShape14)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText },
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
                                            val parsedQty = updatedText.filter { character -> character.isDigit() }
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
                                                    viewModel.adjustDraftQuantity(articleItem.id, 1)
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

            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
        0 -> Color(0xFFFDE6F2) to Color(0xFFC2338F)
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
    var selectedFilterKey by remember { mutableStateOf("ALL") }

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(AdminInputShape14)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
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
                        onValueChange = { updatedText -> searchQuery = updatedText },
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
                            val initial = threadItem.contactName.trim().firstOrNull()?.uppercase() ?: "C"
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
    } ?: viewModel?.selectedChatThread() ?: threadsPool.first()

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

    val submitAdminReply: () -> Unit = {
        val trimmedText = messageInput.trim()
        val hasContent = eitherTrue(trimmedText.isNotEmpty(), effectiveAttachment.isNotEmpty())
        if (hasContent) {
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
            pendingAttachmentName = ""
        }
    }

    LaunchedEffect(resolvedMessages.size, activeThread.id) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val contactPalette = avatarPaletteForName(activeThread.contactName)
    val contactInitial = activeThread.contactName.trim().firstOrNull()?.uppercase() ?: "C"

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
                            text = "Sugerencia Rosa IA · +20 Sillas Tiffany",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "Stock libre verificado para ${activeThread.eventTitle}",
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
                                    viewModel?.adjustDraftQuantity("art-silla-tiffany", 20)
                                    viewModel?.postFeedback("Se agregaron 20 Sillas Tiffany a ${activeThread.eventTitle}")
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
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = messageInput,
                            onValueChange = { updatedText -> messageInput = updatedText },
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
    val coroutineScope = rememberCoroutineScope()
    var onboardingStep by remember { mutableStateOf(0) }
    var rememberChoice by remember { mutableStateOf(true) }
    var showAdminLoginForm by remember { mutableStateOf(false) }
    var fallbackEmail by remember { mutableStateOf("maria@rosafiesta.do") }
    var fallbackPassword by remember { mutableStateOf("") }
    var localAuthError by remember { mutableStateOf<String?>(null) }
    var isSubmittingAdminLogin by remember { mutableStateOf(false) }

    val vmLoginEmail = viewModel?.loginEmail?.collectAsState()?.value
    val vmLoginPassword = viewModel?.loginPassword?.collectAsState()?.value
    val vmError = viewModel?.formError?.collectAsState()?.value

    val emailInput = vmLoginEmail ?: fallbackEmail
    val passwordInput = vmLoginPassword ?: fallbackPassword

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

    val displayedAuthError = localAuthError ?: vmError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF8F7FC))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)),
                    center = Offset(0f, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, size.height * 0.40f),
                    radius = size.width * 0.9f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().zIndex(2f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfLogo(
                        customSize = 42.dp,
                        insetPadding = false,
                        modifier = Modifier.shadow(elevation = 4.dp, shape = CircleShape, spotColor = Color(0x4DB450B4)),
                    )
                    Column {
                        BasicText(
                            text = "Rosa Fiesta",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                        )
                        BasicText(
                            text = "Acceso y guía operativa",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(24.dp)
                        .clip(AdminPillShape)
                        .background(Color(0xFFECEBFF))
                        .padding(horizontal = 10.dp),
                ) {
                    BasicText(
                        text = "PASO ${onboardingStep + 1}/5",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)),
                    )
                }
            }

            AnimatedContent(
                targetState = onboardingStep.coerceIn(0, 4),
                transitionSpec = {
                    val isAdvancing = targetState > initialState
                    buildAdminOnboardingStepTransition(isAdvancing)
                },
                label = "admin-onboarding-tour",
                modifier = Modifier.fillMaxWidth(),
            ) { currentTourStep ->
                AdminOnboardingTourCard(
                    step = currentTourStep,
                    summary = onboardingSummary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (dotIndex in 0..4) {
                        val isActiveDot = dotIndex == onboardingStep
                        val dotWidth by animateDpAsState(
                            targetValue = if (isActiveDot) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "admin-tour-dot",
                        )
                        val dotBrush = if (isActiveDot) AdminHeroGradient else SolidColor(Color(0xFFDCDDEA))
                        Box(
                            modifier = Modifier
                                .width(dotWidth)
                                .height(8.dp)
                                .clip(AdminPillShape)
                                .background(dotBrush)
                                .clickable { onboardingStep = dotIndex },
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onboardingStep > 0) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(AdminPillShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), AdminPillShape)
                                .clickable { onboardingStep = (onboardingStep - 1).coerceAtLeast(0) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Anterior",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            )
                        }
                    }
                    if (onboardingStep < 4) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(AdminPillShape)
                                .background(Color(0xFFECEBFF))
                                .clickable { onboardingStep = (onboardingStep + 1).coerceAtMost(4) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Siguiente",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = true)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AdminHeroGradient)
                    .clickable {
                        val hasImmediateAdminAccess = viewModel?.isCurrentSessionAdminOrStaff() ?: true
                        if (hasImmediateAdminAccess) {
                            localAuthError = null
                            showAdminLoginForm = false
                            onSelectScreenCode("A01")
                        } else {
                            localAuthError = null
                            viewModel.clearFormError()
                            showAdminLoginForm = true
                        }
                    }
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(AdminInputShape14)
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BAR_CHART, tint = Color.White, size = 24.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Entrar al panel Admin",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        BasicText(
                            text = "Eventos, inventario, pagos y clientes",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFFE9EEFF)),
                        )
                    }

                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
                }
            }

            if (showAdminLoginForm) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(AdminCardShape20, isAdmin = true)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFDCE0F5), AdminCardShape20)
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicText(
                            text = "Acceso restringido a Staff y Administración",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "Inicia sesión con una cuenta autorizada para abrir el panel administrativo.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                        )

                        if (displayedAuthError != null) {
                            AdminInlineErrorBanner(message = displayedAuthError)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(AdminInputShape14)
                                .background(Color(0xFFF6F6FB))
                                .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RfIcon(kind = RfIconKind.MAIL, tint = Color(0xFF5F6377), size = 17.dp)
                                BasicTextField(
                                    value = emailInput,
                                    onValueChange = { updatedText ->
                                        localAuthError = null
                                        viewModel?.clearFormError()
                                        if (viewModel != null) {
                                            viewModel.setLoginEmail(updatedText)
                                        } else {
                                            fallbackEmail = updatedText
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A)),
                                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (emailInput.isEmpty()) {
                                            BasicText(
                                                text = "Correo corporativo",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(AdminInputShape14)
                                .background(Color(0xFFF6F6FB))
                                .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RfIcon(kind = RfIconKind.LOCK, tint = Color(0xFF5F6377), size = 17.dp)
                                BasicTextField(
                                    value = passwordInput,
                                    onValueChange = { updatedText ->
                                        localAuthError = null
                                        viewModel?.clearFormError()
                                        if (viewModel != null) {
                                            viewModel.setLoginPassword(updatedText)
                                        } else {
                                            fallbackPassword = updatedText
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A)),
                                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (passwordInput.isEmpty()) {
                                            BasicText(
                                                text = "Contraseña",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF5F6377)),
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(AdminButtonShape16)
                                .background(AdminButtonGradient)
                                .clickable {
                                    if (isSubmittingAdminLogin) {
                                        return@clickable
                                    }
                                    if (viewModel == null) {
                                        localAuthError = null
                                        showAdminLoginForm = false
                                        onSelectScreenCode("A01")
                                        return@clickable
                                    }
                                    isSubmittingAdminLogin = true
                                    coroutineScope.launch {
                                        try {
                                            val loggedIn = viewModel.submitLoginOnline()
                                            val isAuthorizedAdmin = viewModel.isCurrentSessionAdminOrStaff()
                                            val canEnterAdmin = bothTrue(loggedIn, isAuthorizedAdmin)
                                            if (canEnterAdmin) {
                                                localAuthError = null
                                                showAdminLoginForm = false
                                                onSelectScreenCode("A01")
                                            } else if (loggedIn) {
                                                val roleDeniedMessage = "Esta cuenta es de cliente y no tiene acceso al panel Admin."
                                                localAuthError = roleDeniedMessage
                                                viewModel.postError(roleDeniedMessage)
                                            } else {
                                                localAuthError = viewModel.formError.value ?: "Credenciales inválidas. Verifica correo y contraseña."
                                            }
                                        } finally {
                                            isSubmittingAdminLogin = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = if (isSubmittingAdminLogin) "Verificando..." else "Iniciar sesión Admin",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x40C850AA))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECE7F1), RoundedCornerShape(24.dp))
                    .clickable {
                        viewModel?.switchRole("client")
                        onSwitchAppMode()
                    }
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(AdminInputShape14)
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PARTY, tint = Color.White, size = 24.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Entrar como cliente",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                        BasicText(
                            text = "Mira el catálogo y cotizador como tus clientes",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377)),
                        )
                    }

                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { rememberChoice = !rememberChoice }
                    .padding(top = 4.dp),
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
}

@Composable
private fun AdminOnboardingTourCard(
    step: Int,
    summary: AdminOnboardingSummary,
) {
    val typo = LocalRfTypography.current
    val tagLabel = when (step) {
        0 -> "01 · AGENDA EN TIEMPO REAL"
        1 -> "02 · COPILOTO ROSA IA"
        2 -> "03 · INVENTARIO VERIFICADO"
        3 -> "04 · FINANZAS Y COBROS"
        else -> "05 · MODO DUAL ADMIN / CLIENTE"
    }
    val headlineText = when (step) {
        0 -> "${summary.activeEventsCount} eventos activos esta semana"
        1 -> "Cotizaciones automáticas sin sobreventa"
        2 -> "${summary.totalArticlesInCatalog} artículos con stock controlado"
        3 -> "RD$ ${summary.monthlyIncomeDop} facturados este mes"
        else -> "Todo listo para gestionar Rosa Fiesta"
    }
    val bodyDescription = when (step) {
        0 -> "Próximo montaje: ${summary.leadEventTitle} (${summary.leadEventDate}) en ${summary.leadEventVenue} para ${summary.leadEventGuests} invitados (${summary.leadEventItemsCount} pzas)."
        1 -> "Rosa IA cruza fechas, bloquea ${summary.primaryArticleTitle} (${summary.primaryArticleAvailableStock} libres) y sugiere alternativas."
        2 -> "Cada reserva y devolución actualiza el inventario físico y las rutas de despacho al instante."
        3 -> "Controla anticipos y saldos pendientes por RD$ ${summary.depositPendingDop} con comprobantes verificados."
        else -> "Elige abajo si deseas operar el panel administrativo o recorrer la experiencia de cliente."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(24.dp), isAdmin = true)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFECEBFF), Color(0xFFE5F4F8), Color(0xFFF3EEFF)),
                ),
            )
            .border(1.dp, Color(0xFFDCE0F5), RoundedCornerShape(24.dp))
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = tagLabel,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6), letterSpacing = 0.8.sp),
                )
                RosaAvatar(size = RosaAvatarSize.MINI, customSize = 30.dp, forceAdminPalette = true)
            }

            BasicText(
                text = headlineText,
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 26.sp),
            )

            BasicText(
                text = bodyDescription,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF4F5368), lineHeight = 20.sp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AdminInputShape14)
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = summary.leadEventTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "${summary.leadEventDate} · ${summary.leadEventVenue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(AdminPillShape)
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = "RD$ ${summary.monthlyIncomeDop}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                    )
                }
            }
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
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RfIcon(kind = icon, tint = Color(0xFF5F6377), size = 18.dp)
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF3A3D52), textAlign = TextAlign.End),
            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
            modifier = Modifier.weight(1f),
        )
    }
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
    var rncNumber by remember(settings.rnc) { mutableStateOf(settings.rnc.ifBlank { "1-31-84920-2" }) }
    var phoneValue by remember(settings.phone) { mutableStateOf(settings.phone) }
    var whatsappValue by remember(settings.whatsapp) { mutableStateOf(settings.whatsapp) }
    var emailValue by remember(settings.email) { mutableStateOf(settings.email) }
    var addressValue by remember(settings.address) { mutableStateOf(settings.address) }
    var businessHoursValue by remember(settings.businessHours) { mutableStateOf(settings.businessHours) }
    var depositPercent by remember(settings.depositPercent) { mutableStateOf(settings.depositPercent) }
    var draftZones by remember(settings.zones) {
        mutableStateOf(settings.zones.ifEmpty { defaultDeliveryZones() })
    }

    var showAddZoneModal by remember { mutableStateOf(false) }
    var newZoneName by remember { mutableStateOf("") }
    var newZoneFeeText by remember { mutableStateOf("300") }
    var newZoneEtaText by remember { mutableStateOf("45") }
    var localZoneError by remember { mutableStateOf<String?>(null) }
    var localSaveError by remember { mutableStateOf<String?>(null) }

    val displayedError = localSaveError ?: vmError

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

                BasicText(
                    text = "Ajustes del negocio",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
            }

            if (displayedError != null) {
                AdminInlineErrorBanner(message = displayedError)
            }

            BasicText(
                text = "DATOS DEL NEGOCIO Y FACTURACIÓN",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

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
                        onValueChange = { updatedText ->
                            localSaveError = null
                            businessName = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.RECEIPT,
                        label = "RNC",
                        value = rncNumber,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            rncNumber = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.PHONE,
                        label = "Teléfono",
                        value = phoneValue,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            phoneValue = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.WHATSAPP,
                        label = "WhatsApp",
                        value = whatsappValue,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            whatsappValue = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.MAIL,
                        label = "Correo",
                        value = emailValue,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            emailValue = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.LOCATION,
                        label = "Dirección",
                        value = addressValue,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            addressValue = updatedText
                        },
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    SettingsEditableRow(
                        icon = RfIconKind.CLOCK,
                        label = "Horario",
                        value = businessHoursValue,
                        onValueChange = { updatedText ->
                            localSaveError = null
                            businessHoursValue = updatedText
                        },
                    )
                }
            }

            BasicText(
                text = "ENTREGAS · ZONAS DE COBERTURA",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

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
                                        val parsedFee = updatedText.filter { character -> character.isDigit() }.toIntOrNull() ?: 0
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
                                                    currentZone.copy(feeDop = currentZone.feeDop + 50)
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

            BasicText(
                text = "RESERVAS Y ANTICIPO",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

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

            BasicText(
                text = "AUDIO Y EFECTOS DE SONIDO",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

            AppAudioSettingsCard(
                viewModel = viewModel,
                isEnglish = false,
                isAdminSurface = true,
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                    .clip(AdminButtonShape16)
                    .background(AdminButtonGradient)
                    .clickable {
                        val hasBusinessName = businessName.trim().isNotEmpty()
                        val hasRnc = rncNumber.trim().isNotEmpty()
                        val isConfigValid = bothTrue(hasBusinessName, hasRnc)
                        if (!isConfigValid) {
                            val validationError = "El nombre comercial y el RNC son obligatorios."
                            localSaveError = validationError
                            viewModel?.postError(validationError)
                        } else {
                            val updatedSettings = settings.copy(
                                businessName = businessName.trim(),
                                rnc = rncNumber.trim(),
                                address = addressValue.trim(),
                                phone = phoneValue.trim(),
                                whatsapp = whatsappValue.trim(),
                                email = emailValue.trim(),
                                businessHours = businessHoursValue.trim(),
                                depositPercent = depositPercent,
                                zones = draftZones,
                            )
                            val savedOk = viewModel?.saveBusinessConfig(updatedSettings) ?: true
                            if (savedOk) {
                                localSaveError = null
                                onSelectScreenCode("A12")
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

        if (showAddZoneModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73141632))
                    .clickable { showAddZoneModal = false },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(AdminCardShape20)
                        .background(Color.White)
                        .clickable {}
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(AdminInputShape14)
                                .background(Color(0xFFF6F6FB))
                                .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            BasicTextField(
                                value = newZoneName,
                                onValueChange = { updatedText ->
                                    localZoneError = null
                                    newZoneName = updatedText
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

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(AdminInputShape14)
                                    .background(Color(0xFFF6F6FB))
                                    .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = newZoneFeeText,
                                    onValueChange = { updatedText ->
                                        localZoneError = null
                                        newZoneFeeText = updatedText.filter { character -> character.isDigit() }
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

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(AdminInputShape14)
                                    .background(Color(0xFFF6F6FB))
                                    .border(1.dp, Color(0xFFE3E4EF), AdminInputShape14)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                BasicTextField(
                                    value = newZoneEtaText,
                                    onValueChange = { updatedText ->
                                        localZoneError = null
                                        newZoneEtaText = updatedText.filter { character -> character.isDigit() }
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
                                        val cleanedZoneName = newZoneName.trim()
                                        val parsedFee = newZoneFeeText.toIntOrNull() ?: -1
                                        val parsedEta = newZoneEtaText.toIntOrNull() ?: 0
                                        val isZoneValid = allTrue(cleanedZoneName.isNotEmpty(), parsedFee >= 0, parsedEta > 0)
                                        if (isZoneValid) {
                                            val createdZone = DeliveryZone(
                                                id = "zone-custom-${draftZones.size + 1}",
                                                name = cleanedZoneName,
                                                feeDop = parsedFee,
                                                etaMinutes = parsedEta,
                                                active = true,
                                            )
                                            draftZones = draftZones + createdZone
                                            newZoneName = ""
                                            newZoneFeeText = "300"
                                            newZoneEtaText = "45"
                                            localZoneError = null
                                            showAddZoneModal = false
                                        } else {
                                            localZoneError = "Ingresa nombre válido, tarifa en RD$ y tiempo estimado."
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
                    val initial = refundItem.clientName.trim().firstOrNull()?.uppercase() ?: "C"
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
                                        text = "${refundItem.clientName} · ${refundItem.requestedAtLabel}",
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
        ?: refundsPool.first()

    val firstName = activeRefund.clientName.trim().substringBefore(" ")
    var clientMessage by remember(activeRefund.id) {
        mutableStateOf(
            "Hola $firstName, aprobamos tu solicitud para ${activeRefund.eventTitle}. Te enviamos RD$ ${activeRefund.refundAmountDop} por transferencia en 3 a 5 días hábiles.",
        )
    }

    val retentionDop = (activeRefund.paidDepositDop - activeRefund.refundAmountDop).coerceAtLeast(0)
    val alreadyApproved = isRefundProcessed(activeRefund.status)

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
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
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
                        text = "Solicitado el ${activeRefund.requestedAtLabel}",
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
                        .border(1.dp, Color(0xFFECECF5), AdminCardShape20)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value = clientMessage,
                        onValueChange = { updatedText -> clientMessage = updatedText },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF3A3D52), lineHeight = 21.sp),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            val ctaBrush = if (alreadyApproved) SolidColor(Color(0xFFEEF6D6)) else AdminButtonGradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                    .clip(AdminButtonShape16)
                    .background(ctaBrush)
                    .clickable {
                        if (!alreadyApproved) {
                            viewModel?.approveSelectedRefund()
                            onSelectScreenCode("A32")
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

    val activeReview = vmReviews?.firstOrNull { reviewItem -> reviewItem.id == selectedReviewId }
        ?: viewModel?.selectedReview()
        ?: vmReviews?.firstOrNull()
        ?: fallbackReviewRecord()

    val toneTemplates = remember(activeReview.id, activeReview.authorName, activeReview.eventTitle) {
        buildReviewReplyTemplates(activeReview)
    }

    var selectedTone by remember(activeReview.id) { mutableStateOf("Cálida") }
    var replyText by remember(activeReview.id, activeReview.adminReply) {
        mutableStateOf(
            activeReview.adminReply.ifBlank { toneTemplates["Cálida"].orEmpty() },
        )
    }
    var publishAndVerify by remember { mutableStateOf(true) }
    var localReplyError by remember { mutableStateOf<String?>(null) }

    val displayedError = localReplyError ?: vmError
    val palette = avatarPaletteForName(activeReview.authorName)
    val authorInitial = activeReview.authorName.trim().firstOrNull()?.uppercase() ?: "C"
    val clampedRating = activeReview.rating.coerceIn(1, 5)

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(AdminCardShape20, isAdmin = true)
                    .clip(AdminCardShape20)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF5B6CF0), AdminCardShape20)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = replyText,
                    onValueChange = { updatedText ->
                        localReplyError = null
                        viewModel?.clearFormError()
                        replyText = updatedText
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

            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(AdminButtonShape16, isAdmin = true)
                    .clip(AdminButtonShape16)
                    .background(AdminButtonGradient)
                    .clickable {
                        val trimmedReply = replyText.trim()
                        val publishedOk = viewModel?.replyToSelectedReview(trimmedReply) ?: (trimmedReply.length >= 4)
                        if (publishedOk) {
                            localReplyError = null
                            onSelectScreenCode("BACK:s71")
                        } else {
                            localReplyError = viewModel?.formError?.value
                                ?: "Escribe una respuesta de al menos 4 caracteres antes de publicar."
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

@Composable
internal fun RenderAdminEmptyStateA35(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedCategory by remember { mutableStateOf("Todos") }
    var shareConfirmation by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Todos", "Mobiliario", "Mesas", "Decoración", "Globos")

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
                        .size(144.dp)
                        .background(Color(0x8CECEBFF), CircleShape),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
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

            Box(modifier = Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(24.dp)).background(skeletonGradient))

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
    queueItems: List<OfflineQueueItem>,
    onDismiss: () -> Unit,
    onSyncAndOpenHome: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(5f)
            .background(Color(0x7A141632))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AdminSheetShape28)
                .background(Color.White)
                .clickable {}
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
                        .size(144.dp)
                        .clip(CircleShape)
                        .background(Color(0x80FCE3E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
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

        if (showQueueModal) {
            OfflineQueueInspectorModal(
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

        if (showQueueModal) {
            OfflineQueueInspectorModal(
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
}
