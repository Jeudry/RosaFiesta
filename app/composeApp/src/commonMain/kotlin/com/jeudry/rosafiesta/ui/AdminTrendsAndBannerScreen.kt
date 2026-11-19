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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CatalogTrend
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.SeasonBanner
import com.jeudry.rosafiesta.domain.validation.InputValidator

private data class BannerTargetOption(
    val screenCode: String,
    val label: String,
)

private val BannerTargetOptions: List<BannerTargetOption> = listOf(
    BannerTargetOption("s21", "Paquetes"),
    BannerTargetOption("s18", "Catálogo"),
    BannerTargetOption("s30", "Rosa IA"),
    BannerTargetOption("s38", "Nuevo evento"),
)

private val TrendPhotoPresetChoices: List<Pair<String, PhotoAsset>> = listOf(
    "Bodas" to PhotoAsset.BODA,
    "Graduaciones" to PhotoAsset.GRADUACION,
    "Quinceañera" to PhotoAsset.QUINCEANERA,
    "Baby Shower" to PhotoAsset.BABY_SHOWER,
    "Corporativo" to PhotoAsset.CORPORATIVO,
)

private val TrendBgHexOptions: List<Pair<String, String>> = listOf(
    "Lavanda" to "#F3EEF6",
    "Rosa" to "#FDE6F2",
    "Menta" to "#E0F7F2",
    "Ámbar" to "#FFF2C9",
)

/**
 * Full CRUD management screen (`A41`) for Admin seasonal hero banner and Home trends.
 */
@Composable
internal fun RenderAdminTrendsA41(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val trends = viewModel?.catalogTrends?.collectAsState()?.value ?: emptyList()
    val savedBanner = viewModel?.seasonBanner?.collectAsState()?.value ?: SeasonBanner()
    val categories = viewModel?.catalogCategories?.collectAsState()?.value ?: emptyList()
    val formError = viewModel?.formError?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    var bannerActive by remember(savedBanner) { mutableStateOf(savedBanner.active) }
    var bannerBadge by remember(savedBanner) { mutableStateOf(savedBanner.badgeLabel) }
    var bannerTitle by remember(savedBanner) { mutableStateOf(savedBanner.title) }
    var bannerCta by remember(savedBanner) { mutableStateOf(savedBanner.ctaLabel) }
    var bannerTarget by remember(savedBanner) { mutableStateOf(savedBanner.targetScreenCode.ifBlank { "s21" }) }
    var hasBannerTitleInteracted by remember(savedBanner) { mutableStateOf(false) }
    var forceBannerValidation by remember(savedBanner) { mutableStateOf(false) }

    var showTrendSheet by remember { mutableStateOf(false) }
    var editingTrend by remember { mutableStateOf<CatalogTrend?>(null) }
    var trendTitle by remember { mutableStateOf("") }
    var trendEventsLabel by remember { mutableStateOf("") }
    var trendPhotoAsset by remember { mutableStateOf(PhotoAsset.BODA) }
    var trendPhotoUrl by remember { mutableStateOf("") }
    var trendBgHex by remember { mutableStateOf("#F3EEF6") }
    var trendLinkedCategory by remember { mutableStateOf("") }
    var trendSortOrderInput by remember { mutableStateOf("1") }
    var trendActive by remember { mutableStateOf(true) }
    var hasTrendTitleInteracted by remember { mutableStateOf(false) }
    var forceTrendValidation by remember { mutableStateOf(false) }
    var trendSheetError by remember { mutableStateOf<String?>(null) }
    var isUploadingTrendPhoto by remember { mutableStateOf(false) }
    var uploadedPhotoName by remember { mutableStateOf<String?>(null) }

    var pendingDeleteTrend by remember { mutableStateOf<CatalogTrend?>(null) }
    var deleteLocalError by remember { mutableStateOf<String?>(null) }

    val primaryBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    val bannerTitleValidator: (String) -> String? = { value ->
        if (bannerActive) InputValidator.validateSeasonBannerTitle(value, isRequired = true) else null
    }
    val trendTitleValidator: (String) -> String? = { value ->
        InputValidator.validateTrendTitle(value, isRequired = true)
    }

    fun openTrendEditor(target: CatalogTrend?) {
        editingTrend = target
        trendTitle = target?.title.orEmpty()
        trendEventsLabel = target?.eventsLabel.orEmpty()
        trendPhotoAsset = target?.photo ?: PhotoAsset.BODA
        trendPhotoUrl = target?.photoUrl.orEmpty()
        trendBgHex = target?.photoBgHex?.ifBlank { "#F3EEF6" } ?: "#F3EEF6"
        trendLinkedCategory = target?.linkedCategory.orEmpty()
        trendSortOrderInput = (target?.sortOrder ?: (trends.size + 1)).toString()
        trendActive = target?.active ?: true
        hasTrendTitleInteracted = false
        forceTrendValidation = false
        trendSheetError = null
        uploadedPhotoName = null
        viewModel?.clearFormError()
        showTrendSheet = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Tendencias y portada",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "${trends.size} tendencias · Portada de Inicio Personal",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("add-trend-button")
                        .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                        .clip(CircleShape)
                        .background(primaryBrush)
                        .clickable { openTrendEditor(null) },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            AdminFeedbackStrip41(formError = formError, formFeedback = formFeedback)

            SeasonBannerEditorCard41(
                bannerActive = bannerActive,
                onBannerActiveChange = { bannerActive = it },
                bannerBadge = bannerBadge,
                onBannerBadgeChange = { bannerBadge = it.take(48) },
                bannerTitle = bannerTitle,
                onBannerTitleChange = { bannerTitle = it.take(80) },
                bannerCta = bannerCta,
                onBannerCtaChange = { bannerCta = it.take(40) },
                bannerTarget = bannerTarget,
                onBannerTargetChange = { bannerTarget = it },
                hasTitleInteracted = hasBannerTitleInteracted,
                onTitleInteracted = { hasBannerTitleInteracted = true },
                forceValidation = forceBannerValidation,
                bannerTitleValidator = bannerTitleValidator,
                isSubmitting = isSubmitting,
                onSaveBanner = {
                    hasBannerTitleInteracted = true
                    forceBannerValidation = true
                    val validationErr = bannerTitleValidator(bannerTitle)
                    if (validationErr != null) {
                        viewModel?.soundPlayer?.playAlertError()
                    } else {
                        viewModel?.saveSeasonBanner(
                            SeasonBanner(
                                id = "default",
                                badgeLabel = bannerBadge.trim(),
                                title = bannerTitle.trim(),
                                ctaLabel = bannerCta.trim(),
                                targetScreenCode = bannerTarget,
                                active = bannerActive,
                            ),
                        )
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "TENDENCIAS PUBLICADAS (${trends.size})",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF5F6377),
                    ),
                )
                BasicText(
                    text = "+ Agregar",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6CF0)),
                    modifier = Modifier.clickable { openTrendEditor(null) },
                )
            }

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFFC2338F), size = 28.dp)
                        BasicText(
                            text = "Aún no hay tendencias creadas",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                        BasicText(
                            text = "Crea tus propias tarjetas de tendencias para mostrarlas en la pantalla de Inicio.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377)),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(primaryBrush)
                                .clickable { openTrendEditor(null) }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            BasicText(
                                text = "Crear primera tendencia",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    trends.forEach { trendItem ->
                        AdminTrendRowCard41(
                            trend = trendItem,
                            viewModel = viewModel,
                            onEdit = { openTrendEditor(trendItem) },
                            onDelete = {
                                deleteLocalError = null
                                viewModel?.clearFormError()
                                pendingDeleteTrend = trendItem
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        AdminBottomSheetOverlay(
            visible = showTrendSheet,
            onDismiss = { showTrendSheet = false },
            verticalSpacing = 12.dp,
        ) {
            val activeEdit = editingTrend
            val isEditingExisting = activeEdit != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = if (isEditingExisting) "Editar tendencia" else "Nueva tendencia",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                )
                RfIcon(
                    kind = RfIconKind.CLOSE,
                    tint = Color(0xFF5F6377),
                    size = 20.dp,
                    modifier = Modifier.clickable { showTrendSheet = false },
                )
            }

            AdminDebouncedInputField(
                value = trendTitle,
                onValueChange = { updatedText ->
                    trendTitle = updatedText.take(50)
                    trendSheetError = null
                },
                validator = trendTitleValidator,
                hasInteracted = hasTrendTitleInteracted,
                onInteracted = { hasTrendTitleInteracted = true },
                forceShowError = forceTrendValidation,
                label = "Título de tendencia",
                placeholder = "Ej. Graduaciones 2026",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                useInputShadow = true,
                minHeight = 48.dp,
                horizontalPadding = 14.dp,
                textFontSize = 15.sp,
                textColor = Color(0xFF2E324A),
                labelFontSize = 13.sp,
                labelColor = Color(0xFF3A3D52),
            )

            AdminDebouncedInputField(
                value = trendEventsLabel,
                onValueChange = { updatedLabel -> trendEventsLabel = updatedLabel.take(36) },
                validator = { null },
                hasInteracted = false,
                onInteracted = {},
                forceShowError = false,
                label = "Etiqueta o subtítulo (opcional)",
                placeholder = "Ej. Temporada escolar · Muy pedido",
                useInputShadow = true,
                minHeight = 46.dp,
                horizontalPadding = 14.dp,
                textFontSize = 14.sp,
                textColor = Color(0xFF2E324A),
                labelFontSize = 13.sp,
                labelColor = Color(0xFF3A3D52),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = "Imagen de portada",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfUploadedOrCatalogPhoto(
                        photoUrl = trendPhotoUrl,
                        fallbackPhotoKey = resolvePhotoKeyForAsset41(trendPhotoAsset),
                        contentDescription = trendTitle.ifBlank { "Vista previa" },
                        viewModel = viewModel,
                        cornerRadius = 14.dp,
                        modifier = Modifier.size(60.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val photoCaption = when {
                            isUploadingTrendPhoto -> "Subiendo ${uploadedPhotoName ?: "imagen"}..."
                            !uploadedPhotoName.isNullOrBlank() -> uploadedPhotoName.orEmpty()
                            trendPhotoUrl.isNotBlank() -> "Foto personalizada cargada"
                            else -> "Sube una foto o elige un respaldo rápido"
                        }
                        BasicText(
                            text = photoCaption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECEBFF))
                                    .clickable(enabled = !isUploadingTrendPhoto) {
                                        viewModel?.pickMediaAttachment(listOf("png", "jpg", "jpeg", "webp")) { pickedFile ->
                                            trendSheetError = null
                                            isUploadingTrendPhoto = true
                                            uploadedPhotoName = pickedFile.fileName
                                            viewModel.uploadAndAttachMedia(
                                                purpose = "trend-photo",
                                                file = pickedFile,
                                                onError = { message ->
                                                    isUploadingTrendPhoto = false
                                                    trendSheetError = message
                                                },
                                                onUploaded = { uploadedUrl ->
                                                    isUploadingTrendPhoto = false
                                                    trendPhotoUrl = uploadedUrl
                                                },
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                BasicText(
                                    text = if (isUploadingTrendPhoto) "Subiendo..." else "Subir foto",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                                )
                            }
                            if (trendPhotoUrl.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFCE3E8))
                                        .clickable {
                                            trendPhotoUrl = ""
                                            uploadedPhotoName = null
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                ) {
                                    BasicText(
                                        text = "Quitar foto",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrendPhotoPresetChoices.forEach { preset ->
                        val isPresetSelected = if (trendPhotoUrl.isBlank()) trendPhotoAsset == preset.second else false
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPresetSelected) Color(0xFF5B6CF0) else Color(0xFFF3F3F8))
                                .clickable {
                                    trendPhotoAsset = preset.second
                                    trendPhotoUrl = ""
                                    uploadedPhotoName = null
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = preset.first,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPresetSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }
            }

            if (categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(
                        text = "Filtrar categoría al tocar (opcional)",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val noCategorySelected = trendLinkedCategory.isBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (noCategorySelected) Color(0xFF5B6CF0) else Color(0xFFF3F3F8))
                                .clickable { trendLinkedCategory = "" }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Paquetes",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (noCategorySelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                        categories.forEach { catItem ->
                            val isCatSelected = trendLinkedCategory.equals(catItem.name, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCatSelected) Color(0xFF5B6CF0) else Color(0xFFF3F3F8))
                                    .clickable { trendLinkedCategory = catItem.name }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                BasicText(
                                    text = catItem.name,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCatSelected) Color.White else Color(0xFF2E324A),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "Visible en Inicio Personal",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF151729)),
                )
                RfSwitch(
                    checked = trendActive,
                    onCheckedChange = { trendActive = it },
                )
            }

            val modalErr = trendSheetError
            if (!modalErr.isNullOrBlank()) {
                BasicText(
                    text = modalErr,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primaryBrush)
                    .clickable(enabled = !isSubmitting) {
                        hasTrendTitleInteracted = true
                        forceTrendValidation = true
                        val errMsg = trendTitleValidator(trendTitle)
                        if (errMsg != null) {
                            trendSheetError = errMsg
                            viewModel?.soundPlayer?.playAlertError()
                        } else if (isUploadingTrendPhoto) {
                            trendSheetError = "Espera a que termine de subirse la foto."
                        } else {
                            val parsedOrder = trendSortOrderInput.toIntOrNull() ?: (trends.size + 1)
                            val payload = CatalogTrend(
                                id = activeEdit?.id.orEmpty(),
                                title = trendTitle.trim(),
                                eventsLabel = trendEventsLabel.trim(),
                                photo = trendPhotoAsset,
                                photoUrl = trendPhotoUrl.trim(),
                                photoBgHex = trendBgHex,
                                linkedCategory = trendLinkedCategory.trim(),
                                linkedPackageId = activeEdit?.linkedPackageId.orEmpty(),
                                sortOrder = parsedOrder,
                                active = trendActive,
                            )
                            if (activeEdit == null) {
                                viewModel?.createTrend(payload) { outcome ->
                                    if (outcome.isSuccess) {
                                        showTrendSheet = false
                                    } else {
                                        trendSheetError = outcome.exceptionOrNull()?.message
                                    }
                                }
                            } else {
                                viewModel?.updateTrend(payload) { outcome ->
                                    if (outcome.isSuccess) {
                                        showTrendSheet = false
                                    } else {
                                        trendSheetError = outcome.exceptionOrNull()?.message
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = if (isEditingExisting) "Guardar cambios" else "Crear tendencia",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }

        val targetDelete = pendingDeleteTrend
        AdminBottomSheetOverlay(
            visible = targetDelete != null,
            onDismiss = { pendingDeleteTrend = null },
            verticalSpacing = 12.dp,
        ) {
            if (targetDelete != null) {
                BasicText(
                    text = "Eliminar tendencia",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                )
                BasicText(
                    text = "¿Seguro que deseas eliminar \"${targetDelete.title}\"? Dejará de mostrarse en Inicio Personal.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)),
                )
                if (!deleteLocalError.isNullOrBlank()) {
                    BasicText(
                        text = deleteLocalError.orEmpty(),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
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
                            .background(Color(0xFFF2F2F8))
                            .clickable { pendingDeleteTrend = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Cancelar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("confirm-delete-trend")
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFB42345))
                            .clickable(enabled = !isSubmitting) {
                                viewModel?.deleteTrend(targetDelete.id) { outcome ->
                                    if (outcome.isSuccess) {
                                        pendingDeleteTrend = null
                                    } else {
                                        deleteLocalError = outcome.exceptionOrNull()?.message ?: "No se pudo eliminar."
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Eliminar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonBannerEditorCard41(
    bannerActive: Boolean,
    onBannerActiveChange: (Boolean) -> Unit,
    bannerBadge: String,
    onBannerBadgeChange: (String) -> Unit,
    bannerTitle: String,
    onBannerTitleChange: (String) -> Unit,
    bannerCta: String,
    onBannerCtaChange: (String) -> Unit,
    bannerTarget: String,
    onBannerTargetChange: (String) -> Unit,
    hasTitleInteracted: Boolean,
    onTitleInteracted: () -> Unit,
    forceValidation: Boolean,
    bannerTitleValidator: (String) -> String?,
    isSubmitting: Boolean,
    onSaveBanner: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Banner de temporada (Inicio)",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                )
                BasicText(
                    text = if (bannerActive) "Visible en la portada de clientes" else "Oculto en Inicio Personal",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                )
            }
            RfSwitch(
                checked = bannerActive,
                onCheckedChange = onBannerActiveChange,
            )
        }

        if (bannerActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bannerBadge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText(
                                text = bannerBadge,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                            )
                        }
                    }
                    BasicText(
                        text = bannerTitle.ifBlank { "Tu título de temporada" },
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = bannerCta.ifBlank { "Explorar paquetes" },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA23FB8)),
                        )
                    }
                }
            }
        }

        AdminDebouncedInputField(
            value = bannerBadge,
            onValueChange = onBannerBadgeChange,
            validator = { null },
            hasInteracted = false,
            onInteracted = {},
            forceShowError = false,
            label = "Etiqueta superior (ej. NUEVA TEMPORADA 2026)",
            placeholder = "Ej. NUEVA TEMPORADA 2026",
            useInputShadow = true,
            minHeight = 46.dp,
            horizontalPadding = 12.dp,
            textFontSize = 14.sp,
            textColor = Color(0xFF2E324A),
            labelFontSize = 12.5.sp,
            labelColor = Color(0xFF3A3D52),
        )

        AdminDebouncedInputField(
            value = bannerTitle,
            onValueChange = onBannerTitleChange,
            validator = bannerTitleValidator,
            hasInteracted = hasTitleInteracted,
            onInteracted = onTitleInteracted,
            forceShowError = forceValidation,
            label = "Título principal del banner",
            placeholder = "Ej. Crea momentos inolvidables",
            useInputShadow = true,
            minHeight = 46.dp,
            horizontalPadding = 12.dp,
            textFontSize = 14.sp,
            textColor = Color(0xFF2E324A),
            labelFontSize = 12.5.sp,
            labelColor = Color(0xFF3A3D52),
        )

        AdminDebouncedInputField(
            value = bannerCta,
            onValueChange = onBannerCtaChange,
            validator = { null },
            hasInteracted = false,
            onInteracted = {},
            forceShowError = false,
            label = "Texto del botón",
            placeholder = "Ej. Explorar paquetes",
            useInputShadow = true,
            minHeight = 46.dp,
            horizontalPadding = 12.dp,
            textFontSize = 14.sp,
            textColor = Color(0xFF2E324A),
            labelFontSize = 12.5.sp,
            labelColor = Color(0xFF3A3D52),
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BannerTargetOptions.forEach { targetOpt ->
                val isSelected = bannerTarget == targetOpt.screenCode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF5B6CF0) else Color(0xFFF3F3F8))
                        .clickable { onBannerTargetChange(targetOpt.screenCode) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    BasicText(
                        text = targetOpt.label,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                .clickable(enabled = !isSubmitting, onClick = onSaveBanner),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = "Guardar portada",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
            )
        }
    }
}

@Composable
private fun AdminTrendRowCard41(
    trend: CatalogTrend,
    viewModel: RosaFiestaViewModel?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val resolvedPhotoKey = resolvePhotoKeyForAsset41(trend.photo)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RfUploadedOrCatalogPhoto(
                photoUrl = trend.photoUrl,
                fallbackPhotoKey = resolvedPhotoKey,
                contentDescription = trend.title,
                viewModel = viewModel,
                cornerRadius = 14.dp,
                modifier = Modifier.size(56.dp),
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                BasicText(
                    text = trend.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                )
                val metaParts = buildList {
                    if (trend.eventsLabel.isNotBlank()) add(trend.eventsLabel)
                    if (trend.linkedCategory.isNotBlank()) add("Cat: ${trend.linkedCategory}")
                    add(if (trend.active) "Visible en Inicio" else "Oculta")
                }
                BasicText(
                    text = metaParts.joinToString(" · "),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .testTag("edit-trend-${trend.id}")
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECEBFF))
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF4B47D6), size = 17.dp)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete-trend-${trend.id}")
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFCE3E8))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.TRASH, tint = Color(0xFFB42345), size = 17.dp)
            }
        }
    }
}

@Composable
private fun AdminFeedbackStrip41(
    formError: String?,
    formFeedback: String?,
) {
    val typo = LocalRfTypography.current
    if (!formError.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFCE3E8))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            BasicText(
                text = formError,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB42345)),
            )
        }
    } else if (!formFeedback.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEEF6D6))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            BasicText(
                text = formFeedback,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)),
            )
        }
    }
}

private fun resolvePhotoKeyForAsset41(asset: PhotoAsset): RfPhotoKey = RfPhotoKey.entries.firstOrNull { entry ->
    val matchesAsset = entry.assetKey == asset.key
    val matchesName = entry.name.equals(asset.key, ignoreCase = true)
    if (matchesAsset) true else matchesName
} ?: RfPhotoKey.BODA
