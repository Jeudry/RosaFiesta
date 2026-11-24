package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.looksLikeSvgBytes
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.CatalogCategory
import com.jeudry.rosafiesta.domain.validation.InputValidator

private fun countArticlesInCategory(
    category: CatalogCategory,
    articles: List<CatalogArticle>,
): Int {
    val liveMatches = articles.count { article ->
        val matchesName = article.category.equals(category.name, ignoreCase = true)
        val matchesId = article.category.equals(category.id, ignoreCase = true)
        if (matchesName) true else matchesId
    }
    if (liveMatches > 0) return liveMatches
    return category.itemCount.coerceAtLeast(0)
}

/**
 * Full CRUD management screen (`A40`) for creating, editing, and deleting Admin catalog categories.
 */
@Composable
internal fun RenderAdminCategoriesA40(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val categories = viewModel?.catalogCategories?.collectAsState()?.value ?: emptyList()
    val articles = viewModel?.articles?.collectAsState()?.value ?: emptyList()
    val formError = viewModel?.formError?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false

    var showEditorSheet by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CatalogCategory?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("cat-default") }
    var selectedSvgFileName by remember { mutableStateOf<String?>(null) }
    var isUploadingSvg by remember { mutableStateOf(false) }
    var hasNameInteracted by remember { mutableStateOf(false) }
    var forceEditorValidation by remember { mutableStateOf(false) }
    var editorLocalError by remember { mutableStateOf<String?>(null) }

    var pendingDeleteCategory by remember { mutableStateOf<CatalogCategory?>(null) }
    var deleteLocalError by remember { mutableStateOf<String?>(null) }

    val palette = RfThemeState.currentPalette
    val pillIndigoBg = if (palette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF)
    val pillIndigoInk = if (palette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)
    val pillDangerBg = if (palette.isDark) Color(0xFF3D1924) else Color(0xFFFCE3E8)
    val pillDangerInk = if (palette.isDark) Color(0xFFFF7A96) else Color(0xFFB42345)
    val pillSuccessBg = if (palette.isDark) Color(0xFF253516) else Color(0xFFEEF6D6)
    val pillSuccessBorder = if (palette.isDark) Color(0xFF405C24) else Color(0xFFD5E8A4)
    val pillSuccessInk = if (palette.isDark) Color(0xFFAEDB5C) else Color(0xFF4E6A10)
    val primaryBrush = Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))

    val openCreateModal: () -> Unit = {
        editingCategory = null
        nameInput = ""
        selectedIconKey = "cat-default"
        selectedSvgFileName = null
        isUploadingSvg = false
        hasNameInteracted = false
        forceEditorValidation = false
        editorLocalError = null
        viewModel?.clearFormError()
        showEditorSheet = true
    }

    val openEditModal: (CatalogCategory) -> Unit = { categoryItem ->
        editingCategory = categoryItem
        nameInput = categoryItem.name
        selectedIconKey = categoryItem.illoKey.trim().ifBlank { "cat-default" }
        selectedSvgFileName = null
        isUploadingSvg = false
        hasNameInteracted = false
        forceEditorValidation = false
        editorLocalError = null
        viewModel?.clearFormError()
        showEditorSheet = true
    }

    val categoryNameValidator: (String) -> String? = { rawName ->
        InputValidator.validateCategoryName(rawName, isRequired = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
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
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Categorías",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                    )
                    BasicText(
                        text = if (categories.isEmpty()) "Sin categorías creadas" else "${categories.size} configuradas en catálogo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("a40-new-category-btn")
                        .height(40.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                        .clip(CircleShape)
                        .background(primaryBrush)
                        .clickable { openCreateModal() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 17.dp)
                        BasicText("Nueva", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            AdminFormErrorBanner(errorMessage = formError)

            if (!formFeedback.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillSuccessBg)
                        .border(1.dp, pillSuccessBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = formFeedback,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = pillSuccessInk),
                    )
                }
            }

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = true)
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.surface)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(pillIndigoBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CATALOG, tint = pillIndigoInk, size = 28.dp)
                        }
                        BasicText(
                            text = "No hay categorías creadas",
                            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        )
                        BasicText(
                            text = "Crea tus propias categorías (por ejemplo: Sillas, Iluminación, Flores o Sonido) para organizar inventario y filtros.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted, lineHeight = 19.sp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = true)
                                .clip(RoundedCornerShape(14.dp))
                                .background(primaryBrush)
                                .clickable { openCreateModal() },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Crear primera categoría",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            } else {
                categories.forEach { categoryItem ->
                    val linkedArticleCount = countArticlesInCategory(categoryItem, articles)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                            .clickable { openEditModal(categoryItem) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(pillIndigoBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfCategoryCustomOrFallbackIcon(
                                    iconUrlOrKey = categoryItem.illoKey,
                                    contentDescription = categoryItem.name,
                                    viewModel = viewModel,
                                    size = 24.dp,
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                BasicText(
                                    text = categoryItem.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                                )
                                BasicText(
                                    text = if (linkedArticleCount == 1) "1 artículo asociado" else "$linkedArticleCount artículos asociados",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(pillIndigoBg)
                                    .clickable { openEditModal(categoryItem) },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.EDIT, tint = pillIndigoInk, size = 17.dp)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(pillDangerBg)
                                    .clickable {
                                        deleteLocalError = null
                                        viewModel?.clearFormError()
                                        pendingDeleteCategory = categoryItem
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.TRASH, tint = pillDangerInk, size = 17.dp)
                            }
                        }
                    }
                }
            }
        }

        AdminBottomSheetOverlay(
            visible = showEditorSheet,
            onDismiss = { showEditorSheet = false },
            verticalSpacing = 12.dp,
        ) {
            val activeEdit = editingCategory
            val isEditingExisting = activeEdit != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = if (isEditingExisting) "Editar categoría" else "Nueva categoría",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                )
                RfIcon(
                    kind = RfIconKind.CLOSE,
                    tint = palette.muted,
                    size = 20.dp,
                    modifier = Modifier.clickable { showEditorSheet = false },
                )
            }

            AdminDebouncedInputField(
                value = nameInput,
                onValueChange = { updatedText ->
                    nameInput = updatedText
                    editorLocalError = null
                },
                validator = categoryNameValidator,
                hasInteracted = hasNameInteracted,
                onInteracted = { hasNameInteracted = true },
                forceShowError = forceEditorValidation,
                label = "Nombre de categoría",
                placeholder = "Ej. Iluminación y pistas",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                useInputShadow = true,
                minHeight = 50.dp,
                horizontalPadding = 14.dp,
                textFontSize = 15.sp,
                textColor = palette.ink,
                labelFontSize = 13.sp,
                labelColor = palette.ink,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = "Ícono SVG personalizado (opcional)",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = palette.ink),
                )
                val hasCustomSvg = isCustomMediaIconKey(selectedIconKey)
                val uploadSvgStatus = when {
                    isUploadingSvg -> "Subiendo ${selectedSvgFileName ?: "archivo.svg"}..."
                    !selectedSvgFileName.isNullOrBlank() -> "$selectedSvgFileName · Listo"
                    hasCustomSvg -> "Ícono SVG personalizado"
                    else -> "Elige un archivo .svg desde tu dispositivo"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.chipBg)
                        .border(1.dp, palette.fieldBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isUploadingSvg) {
                            viewModel?.pickMediaAttachment(listOf("svg")) { pickedFile ->
                                val isValidSvg = looksLikeSvgBytes(pickedFile.bytes)
                                if (!isValidSvg) {
                                    editorLocalError = "El archivo \"${pickedFile.fileName}\" no es un SVG válido."
                                } else {
                                    editorLocalError = null
                                    isUploadingSvg = true
                                    selectedSvgFileName = pickedFile.fileName
                                    viewModel.uploadAndAttachMedia(
                                        purpose = "category-icon",
                                        file = pickedFile,
                                        onError = { message ->
                                            isUploadingSvg = false
                                            editorLocalError = message
                                        },
                                        onUploaded = { uploadedUrl ->
                                            isUploadingSvg = false
                                            selectedIconKey = uploadedUrl
                                        },
                                    )
                                }
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(pillIndigoBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfCategoryCustomOrFallbackIcon(
                            iconUrlOrKey = selectedIconKey,
                            contentDescription = nameInput.ifBlank { "Vista previa SVG" },
                            viewModel = viewModel,
                            size = 28.dp,
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(
                            text = if (hasCustomSvg) "SVG cargado" else "Subir ícono SVG",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                        )
                        BasicText(
                            text = uploadSvgStatus,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = palette.muted),
                        )
                    }
                    if (hasCustomSvg) {
                        Box(
                            modifier = Modifier
                                .testTag("a40-clear-svg-btn")
                                .clip(CircleShape)
                                .background(pillDangerBg)
                                .clickable {
                                    selectedIconKey = "cat-default"
                                    selectedSvgFileName = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Quitar",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = pillDangerInk),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .testTag("a40-upload-svg-btn")
                                .clip(CircleShape)
                                .background(pillIndigoBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Elegir .svg",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = pillIndigoInk),
                            )
                        }
                    }
                }
            }

            AdminFormErrorBanner(errorMessage = editorLocalError ?: formError)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.chipBg)
                        .clickable { showEditorSheet = false },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = palette.muted))
                }
                val canSaveCategory = if (isSubmitting) false else !isUploadingSvg
                Box(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(primaryBrush)
                        .clickable(enabled = canSaveCategory) {
                            forceEditorValidation = true
                            val validationError = categoryNameValidator(nameInput)
                            if (validationError != null) {
                                editorLocalError = validationError
                            } else if (activeEdit != null) {
                                viewModel?.updateCategory(
                                    categoryId = activeEdit.id,
                                    name = nameInput.trim(),
                                    illoKey = selectedIconKey,
                                ) { outcome ->
                                    if (outcome.isSuccess) {
                                        editorLocalError = null
                                        showEditorSheet = false
                                    } else {
                                        editorLocalError = outcome.exceptionOrNull()?.message
                                    }
                                }
                            } else {
                                viewModel?.createCategory(
                                    name = nameInput.trim(),
                                    illoKey = selectedIconKey,
                                ) { outcome ->
                                    if (outcome.isSuccess) {
                                        editorLocalError = null
                                        showEditorSheet = false
                                    } else {
                                        editorLocalError = outcome.exceptionOrNull()?.message
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (isEditingExisting) "Guardar cambios" else "Crear categoría",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }

        val targetDelete = pendingDeleteCategory
        AdminBottomSheetOverlay(
            visible = targetDelete != null,
            onDismiss = { pendingDeleteCategory = null },
            verticalSpacing = 12.dp,
        ) {
            if (targetDelete != null) {
                val linkedCount = countArticlesInCategory(targetDelete, articles)
                BasicText(
                    text = "Eliminar categoría",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                )
                BasicText(
                    text = if (linkedCount > 0) {
                        "\"${targetDelete.name}\" tiene $linkedCount artículos asignados. Reasígnalos a otra categoría antes de eliminarla."
                    } else {
                        "¿Seguro que deseas eliminar \"${targetDelete.name}\"? Esta acción quitará la categoría del catálogo."
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted, lineHeight = 19.sp),
                )
                AdminFormErrorBanner(errorMessage = deleteLocalError)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.chipBg)
                            .clickable { pendingDeleteCategory = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cerrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = palette.muted))
                    }
                    if (linkedCount == 0) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFB42345))
                                .clickable(enabled = !isSubmitting) {
                                    viewModel?.deleteCategory(targetDelete.id) { outcome ->
                                        if (outcome.isSuccess) {
                                            pendingDeleteCategory = null
                                            deleteLocalError = null
                                        } else {
                                            deleteLocalError = outcome.exceptionOrNull()?.message
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Eliminar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }
        }
    }
}
