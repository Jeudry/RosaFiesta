package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jeudry.rosafiesta.data.InteractiveMapClient
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.decodeEncodedImageBitmapOrNull
import com.jeudry.rosafiesta.domain.MapPlaceSuggestion
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

private const val MAP_MIN_ZOOM: Int = 10
private const val MAP_MAX_ZOOM: Int = 18
private const val MAP_DEFAULT_ZOOM: Int = 14
private const val MAP_TILE_DP: Float = 256f
private const val MAP_MAX_LATITUDE: Double = 85.0511
private const val MAX_CACHED_MAP_TILES: Int = 48

private fun longitudeToTileX(longitude: Double, zoom: Int): Double {
    val worldTileSpan = (1 shl zoom).toDouble()
    return ((longitude + 180.0) / 360.0) * worldTileSpan
}

private fun latitudeToTileY(latitude: Double, zoom: Int): Double {
    val clampedLatitude = latitude.coerceIn(-MAP_MAX_LATITUDE, MAP_MAX_LATITUDE)
    val latitudeRadians = clampedLatitude * PI / 180.0
    val mercatorProjection = ln(tan(latitudeRadians) + 1.0 / cos(latitudeRadians))
    val worldTileSpan = (1 shl zoom).toDouble()
    return ((1.0 - mercatorProjection / PI) / 2.0) * worldTileSpan
}

private fun tileXToLongitude(tileX: Double, zoom: Int): Double {
    val worldTileSpan = (1 shl zoom).toDouble()
    return (tileX / worldTileSpan) * 360.0 - 180.0
}

private fun tileYToLatitude(tileY: Double, zoom: Int): Double {
    val worldTileSpan = (1 shl zoom).toDouble()
    val mercatorAngle = PI - 2.0 * PI * (tileY / worldTileSpan)
    val latitudeRadians = atan(sinh(mercatorAngle))
    val latitudeDegrees = latitudeRadians * 180.0 / PI
    return latitudeDegrees.coerceIn(-MAP_MAX_LATITUDE, MAP_MAX_LATITUDE)
}

private fun wrapTileX(rawTileX: Int, zoom: Int): Int {
    val worldTiles = 1 shl zoom
    return ((rawTileX % worldTiles) + worldTiles) % worldTiles
}

private fun formatCoordinatesLabel(latitude: Double, longitude: Double): String {
    val roundedLat = (latitude * 10000.0).roundToInt() / 10000.0
    val roundedLon = (longitude * 10000.0).roundToInt() / 10000.0
    return "$roundedLat, $roundedLon"
}

/**
 * Displays an interactive in-app slippy map modal for searching, panning, tapping,
 * zooming, and confirming an event venue location without leaving the app.
 */
@Composable
internal fun RfInAppMapPickerModal(
    initialQuery: String,
    onDismiss: () -> Unit,
    onConfirmLocation: (String) -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current
    val mapClient = remember { InteractiveMapClient() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember(initialQuery) {
        mutableStateOf(initialQuery.trim().take(InputValidator.MAX_SEARCH_QUERY_LENGTH))
    }
    var hasInteractedSearch by remember(initialQuery) { mutableStateOf(false) }
    var forceShowSearchError by remember(initialQuery) { mutableStateOf(false) }
    val searchQueryError = rememberDebouncedValidationError(
        value = searchQuery,
        hasInteracted = hasInteractedSearch,
        forceShowError = forceShowSearchError,
    ) { query ->
        InputValidator.validateSearchQuery(query)
    }
    var centerLatitude by remember { mutableStateOf(InteractiveMapClient.DEFAULT_SANTO_DOMINGO_LAT) }
    var centerLongitude by remember { mutableStateOf(InteractiveMapClient.DEFAULT_SANTO_DOMINGO_LON) }
    var mapZoom by remember { mutableStateOf(MAP_DEFAULT_ZOOM) }
    var selectedLabel by remember(initialQuery) {
        mutableStateOf(
            initialQuery.trim().ifBlank { "Santo Domingo, República Dominicana" },
        )
    }
    var suggestions by remember { mutableStateOf<List<MapPlaceSuggestion>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isResolvingPin by remember { mutableStateOf(false) }
    var pinMoveSequence by remember { mutableStateOf(0) }

    val loadedTiles = remember { mutableStateMapOf<String, ImageBitmap>() }
    val inFlightTileKeys = remember { mutableStateMapOf<String, Boolean>() }
    val tileOrder = remember { mutableListOf<String>() }

    DisposableEffect(mapClient) {
        onDispose {
            loadedTiles.clear()
            inFlightTileKeys.clear()
            tileOrder.clear()
            mapClient.close()
        }
    }

    LaunchedEffect(initialQuery) {
        val trimmedInitial = initialQuery.trim()
        if (trimmedInitial.isNotBlank()) {
            isSearching = true
            val matches = mapClient.searchPlaces(trimmedInitial)
            isSearching = false
            val firstMatch = matches.firstOrNull()
            if (firstMatch != null) {
                centerLatitude = firstMatch.latitude
                centerLongitude = firstMatch.longitude
                mapZoom = 15
                selectedLabel = firstMatch.displayName
            }
        }
    }

    LaunchedEffect(searchQuery) {
        val cleanSearch = searchQuery.trim()
        if (cleanSearch.length < 3) {
            suggestions = emptyList()
            showSuggestions = false
            return@LaunchedEffect
        }
        delay(350)
        isSearching = true
        val results = mapClient.searchPlaces(
            query = cleanSearch,
            biasLat = centerLatitude,
            biasLon = centerLongitude,
        )
        isSearching = false
        suggestions = results
        showSuggestions = results.isNotEmpty()
    }

    LaunchedEffect(pinMoveSequence) {
        if (pinMoveSequence == 0) {
            return@LaunchedEffect
        }
        delay(280)
        isResolvingPin = true
        val resolvedAddress = mapClient.reverseGeocode(centerLatitude, centerLongitude)
        isResolvingPin = false
        selectedLabel = resolvedAddress?.takeIf { text -> text.isNotBlank() }
            ?: formatCoordinatesLabel(centerLatitude, centerLongitude)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val backdropInteractionSource = remember { MutableInteractionSource() }
        val cardInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
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
                    interactionSource = backdropInteractionSource,
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(horizontal = 14.dp, vertical = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .heightIn(min = 480.dp, max = 660.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.lineSoft, RoundedCornerShape(26.dp))
                    .clickable(
                        interactionSource = cardInteractionSource,
                        indication = null,
                        onClick = {},
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MapPickerHeaderRow(
                    onDismiss = onDismiss,
                )

                MapSearchFieldBar(
                    searchQuery = searchQuery,
                    isSearching = isSearching,
                    errorMessage = searchQueryError,
                    onQueryChange = { updatedQuery ->
                        val boundedQuery = updatedQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                        searchQuery = boundedQuery
                        hasInteractedSearch = true
                        forceShowSearchError = false
                        if (boundedQuery.isBlank()) {
                            showSuggestions = false
                        }
                    },
                    onSubmitSearch = {
                        val validationError = InputValidator.validateSearchQuery(searchQuery)
                        if (validationError != null) {
                            hasInteractedSearch = true
                            forceShowSearchError = true
                            return@MapSearchFieldBar
                        }
                        coroutineScope.launch {
                            val cleanSearch = searchQuery.trim()
                            if (cleanSearch.isNotBlank()) {
                                isSearching = true
                                val matches = mapClient.searchPlaces(
                                    query = cleanSearch,
                                    biasLat = centerLatitude,
                                    biasLon = centerLongitude,
                                )
                                isSearching = false
                                suggestions = matches
                                showSuggestions = matches.isNotEmpty()
                                val firstMatch = matches.firstOrNull()
                                if (firstMatch != null) {
                                    centerLatitude = firstMatch.latitude
                                    centerLongitude = firstMatch.longitude
                                    mapZoom = 16
                                    selectedLabel = firstMatch.displayName
                                }
                            }
                        }
                    },
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.chipBg)
                        .border(1.dp, palette.lineSoft, RoundedCornerShape(20.dp))
                        .clipToBounds(),
                ) {
                    InteractiveSlippyMapCanvas(
                        centerLatitude = centerLatitude,
                        centerLongitude = centerLongitude,
                        mapZoom = mapZoom,
                        loadedTiles = loadedTiles,
                        inFlightTileKeys = inFlightTileKeys,
                        tileOrder = tileOrder,
                        mapClient = mapClient,
                        onPanByTiles = { deltaTileX, deltaTileY ->
                            val currentTileX = longitudeToTileX(centerLongitude, mapZoom)
                            val currentTileY = latitudeToTileY(centerLatitude, mapZoom)
                            val nextTileX = currentTileX - deltaTileX
                            val maxTiles = (1 shl mapZoom).toDouble()
                            val clampedTileY = (currentTileY - deltaTileY).coerceIn(0.05, maxTiles - 0.05)
                            centerLongitude = tileXToLongitude(nextTileX, mapZoom)
                            centerLatitude = tileYToLatitude(clampedTileY, mapZoom)
                        },
                        onDragFinished = {
                            pinMoveSequence += 1
                        },
                        onTapAtCoordinates = { tappedLat, tappedLon ->
                            centerLatitude = tappedLat
                            centerLongitude = tappedLon
                            showSuggestions = false
                            pinMoveSequence += 1
                        },
                    )

                    MapZoomAndCenterControls(
                        canZoomIn = mapZoom < MAP_MAX_ZOOM,
                        canZoomOut = mapZoom > MAP_MIN_ZOOM,
                        onZoomIn = {
                            if (mapZoom < MAP_MAX_ZOOM) {
                                mapZoom += 1
                            }
                        },
                        onZoomOut = {
                            if (mapZoom > MAP_MIN_ZOOM) {
                                mapZoom -= 1
                            }
                        },
                        onResetSantoDomingo = {
                            centerLatitude = InteractiveMapClient.DEFAULT_SANTO_DOMINGO_LAT
                            centerLongitude = InteractiveMapClient.DEFAULT_SANTO_DOMINGO_LON
                            mapZoom = MAP_DEFAULT_ZOOM
                            pinMoveSequence += 1
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 26.dp),
                    )

                    BasicText(
                        text = "© Carto • © OpenStreetMap",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 9.sp,
                            color = palette.muted,
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.surface.copy(alpha = 0.88f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )

                    if (showSuggestions) {
                        MapSuggestionsOverlay(
                            suggestions = suggestions,
                            onSelectSuggestion = { chosen ->
                                centerLatitude = chosen.latitude
                                centerLongitude = chosen.longitude
                                mapZoom = 16
                                selectedLabel = chosen.displayName
                                searchQuery = chosen.shortTitle.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                                hasInteractedSearch = true
                                forceShowSearchError = false
                                showSuggestions = false
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(8.dp),
                        )
                    }
                }

                MapSelectedAddressSummary(
                    addressLabel = selectedLabel,
                    latitude = centerLatitude,
                    longitude = centerLongitude,
                    isResolvingPin = isResolvingPin,
                )

                MapPickerActionButtons(
                    confirmEnabled = selectedLabel.isNotBlank(),
                    onCancel = onDismiss,
                    onConfirm = {
                        onConfirmLocation(selectedLabel.trim())
                    },
                )
            }
        }
    }
}

@Composable
private fun MapPickerHeaderRow(
    onDismiss: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = "Elegir ubicación en el mapa",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = palette.ink,
                ),
            )
            BasicText(
                text = "Busca un lugar o arrastra el mapa para mover el pin",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    color = palette.muted,
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.chipBg)
                .semantics { contentDescription = "Cerrar mapa" }
                .clickable(role = Role.Button, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.CLOSE, tint = palette.ink, size = 16.dp)
        }
    }
}

@Composable
private fun MapSearchFieldBar(
    searchQuery: String,
    isSearching: Boolean,
    errorMessage: String? = null,
    onQueryChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current
    val hasError = errorMessage != null
    val borderColor = if (hasError) Color(0xFFE53935) else palette.lineSoft

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.chipBg)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RfIcon(kind = RfIconKind.SEARCH, tint = palette.muted, size = 18.dp)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                    BasicText(
                        text = "Buscar salón, jardín, dirección o sector...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            color = palette.muted,
                        ),
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmitSearch() }),
                    cursorBrush = SolidColor(Color(0xFF945AE1)),
                    textStyle = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = palette.ink,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { keyEvent ->
                            val isKeyDown = keyEvent.type == KeyEventType.KeyDown
                            val isEnterKey = if (keyEvent.key == Key.Enter) true else keyEvent.key == Key.NumPadEnter
                            val shouldSubmit = if (isKeyDown) isEnterKey else false
                            if (shouldSubmit) {
                                onSubmitSearch()
                                true
                            } else {
                                false
                            }
                        },
                )
            }
            if (isSearching) {
                BasicText(
                    text = "Buscando...",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF7A4FD8),
                    ),
                )
            } else if (searchQuery.isNotEmpty()) {
                val actionBg = if (palette.isDark) Color(0xFF2B2852) else Color(0xFFECE4FF)
                val actionInk = if (palette.isDark) Color(0xFFA7A3FF) else Color(0xFF7A4FD8)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(actionBg)
                        .semantics { contentDescription = "Buscar dirección" }
                        .clickable(role = Role.Button, onClick = onSubmitSearch)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    BasicText(
                        text = "Ir",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = actionInk,
                        ),
                    )
                }
            }
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun InteractiveSlippyMapCanvas(
    centerLatitude: Double,
    centerLongitude: Double,
    mapZoom: Int,
    loadedTiles: MutableMap<String, ImageBitmap>,
    inFlightTileKeys: MutableMap<String, Boolean>,
    tileOrder: MutableList<String>,
    mapClient: InteractiveMapClient,
    onPanByTiles: (Double, Double) -> Unit,
    onDragFinished: () -> Unit,
    onTapAtCoordinates: (Double, Double) -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val viewportHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val tileSizePx = with(density) { MAP_TILE_DP.dp.toPx() }.coerceAtLeast(128f)

        val centerTileX = longitudeToTileX(centerLongitude, mapZoom)
        val centerTileY = latitudeToTileY(centerLatitude, mapZoom)
        val halfWidthTiles = (viewportWidthPx / 2f) / tileSizePx
        val halfHeightTiles = (viewportHeightPx / 2f) / tileSizePx

        val minTileX = floor(centerTileX - halfWidthTiles).toInt()
        val maxTileX = floor(centerTileX + halfWidthTiles).toInt()
        val maxTilesPerAxis = 1 shl mapZoom
        val minTileY = floor(centerTileY - halfHeightTiles).toInt().coerceAtLeast(0)
        val maxTileY = floor(centerTileY + halfHeightTiles).toInt().coerceAtMost(maxTilesPerAxis - 1)
        val tileGridSignature = "$mapZoom:$minTileX:$maxTileX:$minTileY:$maxTileY"

        LaunchedEffect(tileGridSignature) {
            for (rawTileX in minTileX..maxTileX) {
                val wrappedTileX = wrapTileX(rawTileX, mapZoom)
                for (tileY in minTileY..maxTileY) {
                    val cacheKey = "$mapZoom/$wrappedTileX/$tileY"
                    val alreadyLoaded = loadedTiles.containsKey(cacheKey)
                    val currentlyInFlight = inFlightTileKeys.containsKey(cacheKey)
                    val shouldFetch = if (alreadyLoaded) false else !currentlyInFlight
                    if (shouldFetch) {
                        inFlightTileKeys[cacheKey] = true
                        coroutineScope.launch {
                            try {
                                val tileBytes = mapClient.fetchMapTilePng(
                                    zoom = mapZoom,
                                    tileX = wrappedTileX,
                                    tileY = tileY,
                                )
                                val decodedBitmap = if (tileBytes != null) {
                                    withContext(Dispatchers.Default) {
                                        decodeEncodedImageBitmapOrNull(tileBytes)
                                    }
                                } else {
                                    null
                                }
                                if (decodedBitmap != null) {
                                    if (!loadedTiles.containsKey(cacheKey)) {
                                        if (tileOrder.size >= MAX_CACHED_MAP_TILES) {
                                            val oldestKey = tileOrder.removeAt(0)
                                            loadedTiles.remove(oldestKey)
                                        }
                                        tileOrder.add(cacheKey)
                                    }
                                    loadedTiles[cacheKey] = decodedBitmap
                                }
                            } finally {
                                inFlightTileKeys.remove(cacheKey)
                            }
                        }
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(mapZoom, tileSizePx) {
                    detectDragGestures(
                        onDragEnd = { onDragFinished() },
                        onDragCancel = { onDragFinished() },
                    ) { pointerChange, dragAmount ->
                        pointerChange.consume()
                        val deltaTileX = dragAmount.x / tileSizePx.toDouble()
                        val deltaTileY = dragAmount.y / tileSizePx.toDouble()
                        onPanByTiles(deltaTileX, deltaTileY)
                    }
                }
                .pointerInput(centerLatitude, centerLongitude, mapZoom, tileSizePx) {
                    detectTapGestures { tapOffset ->
                        val activeCenterX = longitudeToTileX(centerLongitude, mapZoom)
                        val activeCenterY = latitudeToTileY(centerLatitude, mapZoom)
                        val deltaPxX = tapOffset.x - (size.width / 2f)
                        val deltaPxY = tapOffset.y - (size.height / 2f)
                        val tappedTileX = activeCenterX + (deltaPxX / tileSizePx.toDouble())
                        val maxAxis = (1 shl mapZoom).toDouble()
                        val tappedTileY = (activeCenterY + (deltaPxY / tileSizePx.toDouble())).coerceIn(0.05, maxAxis - 0.05)
                        val tappedLongitude = tileXToLongitude(tappedTileX, mapZoom)
                        val tappedLatitude = tileYToLatitude(tappedTileY, mapZoom)
                        onTapAtCoordinates(tappedLatitude, tappedLongitude)
                    }
                },
        ) {
            val canvasCenterX = size.width / 2f
            val canvasCenterY = size.height / 2f
            val tilePixelExtent = ceil(tileSizePx).toInt() + 1

            for (rawTileX in minTileX..maxTileX) {
                val wrappedTileX = wrapTileX(rawTileX, mapZoom)
                for (tileY in minTileY..maxTileY) {
                    val leftPx = canvasCenterX + ((rawTileX - centerTileX) * tileSizePx).toFloat()
                    val topPx = canvasCenterY + ((tileY - centerTileY) * tileSizePx).toFloat()
                    val cacheKey = "$mapZoom/$wrappedTileX/$tileY"
                    val tileBitmap = loadedTiles[cacheKey]
                    if (tileBitmap != null) {
                        drawImage(
                            image = tileBitmap,
                            dstOffset = IntOffset(leftPx.roundToInt(), topPx.roundToInt()),
                            dstSize = IntSize(tilePixelExtent, tilePixelExtent),
                        )
                    } else {
                        val placeholderBg = if (palette.isDark) palette.chipBg else Color(0xFFE9E6F4)
                        val placeholderGrid = if (palette.isDark) palette.line else Color(0xFFDAD5EC)
                        drawRect(
                            color = placeholderBg,
                            topLeft = Offset(leftPx, topPx),
                            size = androidx.compose.ui.geometry.Size(tileSizePx, tileSizePx),
                        )
                        drawLine(
                            color = placeholderGrid,
                            start = Offset(leftPx, topPx),
                            end = Offset(leftPx + tileSizePx, topPx),
                            strokeWidth = 1f,
                        )
                        drawLine(
                            color = placeholderGrid,
                            start = Offset(leftPx, topPx),
                            end = Offset(leftPx, topPx + tileSizePx),
                            strokeWidth = 1f,
                        )
                    }
                }
            }

            drawCenterLocationPin(
                pinTip = Offset(canvasCenterX, canvasCenterY),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenterLocationPin(
    pinTip: Offset,
) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.22f),
        radius = 8.dp.toPx(),
        center = Offset(pinTip.x, pinTip.y + 1.dp.toPx()),
    )
    val headCenterY = pinTip.y - 22.dp.toPx()
    val headRadius = 12.dp.toPx()
    val teardropPath = Path().apply {
        moveTo(pinTip.x, pinTip.y)
        lineTo(pinTip.x - headRadius * 0.78f, headCenterY + headRadius * 0.55f)
        lineTo(pinTip.x + headRadius * 0.78f, headCenterY + headRadius * 0.55f)
        close()
    }
    drawPath(path = teardropPath, color = Color(0xFFD93F9E))
    drawCircle(
        color = Color(0xFFD93F9E),
        radius = headRadius,
        center = Offset(pinTip.x, headCenterY),
    )
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = Offset(pinTip.x, headCenterY),
    )
}

@Composable
private fun MapZoomAndCenterControls(
    canZoomIn: Boolean,
    canZoomOut: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetSantoDomingo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface.copy(alpha = 0.95f))
                .border(1.dp, palette.lineSoft, RoundedCornerShape(14.dp)),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .semantics { contentDescription = "Acercar mapa" }
                    .clickable(enabled = canZoomIn, role = Role.Button, onClick = onZoomIn),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "+",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (canZoomIn) palette.ink else palette.muted,
                    ),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .semantics { contentDescription = "Alejar mapa" }
                    .clickable(enabled = canZoomOut, role = Role.Button, onClick = onZoomOut),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "−",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (canZoomOut) palette.ink else palette.muted,
                    ),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.surface.copy(alpha = 0.95f))
                .border(1.dp, palette.lineSoft, CircleShape)
                .semantics { contentDescription = "Centrar en Santo Domingo" }
                .clickable(role = Role.Button, onClick = onResetSantoDomingo),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = RfIconKind.LOCATION,
                tint = if (palette.isDark) Color(0xFFA7A3FF) else Color(0xFF7A4FD8),
                size = 18.dp,
            )
        }
    }
}

@Composable
private fun MapSuggestionsOverlay(
    suggestions: List<MapPlaceSuggestion>,
    onSelectSuggestion: (MapPlaceSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current

    LazyColumn(
        modifier = modifier
            .heightIn(max = 180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface.copy(alpha = 0.97f))
            .border(1.dp, palette.lineSoft, RoundedCornerShape(16.dp)),
    ) {
        items(suggestions) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSuggestion(suggestion) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF945AE1), size = 16.dp)
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = suggestion.shortTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = palette.ink,
                        ),
                    )
                    BasicText(
                        text = suggestion.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 11.sp,
                            color = palette.muted,
                        ),
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft))
        }
    }
}

@Composable
private fun MapSelectedAddressSummary(
    addressLabel: String,
    latitude: Double,
    longitude: Double,
    isResolvingPin: Boolean,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current

    val summaryBg = if (palette.isDark) Color(0xFF262348) else Color(0xFFF6F1FF)
    val summaryIconTint = if (palette.isDark) Color(0xFFA7A3FF) else Color(0xFF7A4FD8)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(summaryBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RfIcon(kind = RfIconKind.LOCATION, tint = summaryIconTint, size = 18.dp)
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = if (isResolvingPin) "Buscando dirección exacta..." else addressLabel,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = palette.ink,
                ),
            )
            BasicText(
                text = formatCoordinatesLabel(latitude, longitude),
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.sp,
                    color = palette.muted,
                ),
            )
        }
    }
}

@Composable
private fun MapPickerActionButtons(
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.chipBg)
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = "Cancelar",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = palette.ink,
                ),
            )
        }
        Box(
            modifier = Modifier
                .weight(1.4f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)),
                    ),
                )
                .clickable(enabled = confirmEnabled, onClick = onConfirm),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = "Confirmar ubicación",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                ),
            )
        }
    }
}
