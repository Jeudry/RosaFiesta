package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.PathParser

private data class SvgViewBox(
    val minX: Float,
    val minY: Float,
    val width: Float,
    val height: Float,
)

private data class SvgStyleState(
    val fillColor: Color?,
    val strokeColor: Color?,
    val strokeWidth: Float,
    val strokeCap: StrokeCap,
    val strokeJoin: StrokeJoin,
)

/** Returns true when [bytes] begin with an XML/SVG header containing `<svg`. */
public fun looksLikeSvgBytes(bytes: ByteArray): Boolean {
    if (bytes.size < 5) return false
    val text = bytes.decodeToString(throwOnInvalidSequence = false)
        .trimStart('\uFEFF', ' ', '\t', '\r', '\n')
    if (!text.startsWith("<")) return false
    val headerPrefix = text.take(1024).lowercase()
    return headerPrefix.contains("<svg")
}

internal fun countSvgRenderablePrimitives(bytes: ByteArray): Int {
    if (!looksLikeSvgBytes(bytes)) return 0
    val svgText = bytes.decodeToString(throwOnInvalidSequence = false)
    val supportedPrimitiveTags = setOf("path", "circle", "ellipse", "rect", "line", "polyline", "polygon")
    return extractSvgTags(svgText).count { tag ->
        val isOpeningTag = !tag.isClosing
        val isSupported = tag.name in supportedPrimitiveTags
        when {
            !isOpeningTag -> false

            !isSupported -> false

            tag.name == "path" -> {
                val pathData = tag.attributes["d"]?.trim().orEmpty()
                if (pathData.isEmpty()) {
                    false
                } else {
                    runCatching { PathParser().parsePathString(pathData).toNodes().isNotEmpty() }.getOrDefault(false)
                }
            }

            else -> true
        }
    }
}

/**
 * Rasterizes basic vector SVG primitives into an [ImageBitmap] in pure Compose `commonMain`.
 */
public fun decodeSvgToImageBitmapOrNull(
    bytes: ByteArray,
    targetSizePx: Int = 96,
): ImageBitmap? {
    if (!looksLikeSvgBytes(bytes)) return null
    if (targetSizePx <= 0) return null
    val svgText = bytes.decodeToString(throwOnInvalidSequence = false)
    val elements = extractSvgTags(svgText)
    if (elements.isEmpty()) return null

    val rootTag = elements.firstOrNull { tag -> tag.name == "svg" } ?: return null
    val viewBox = parseSvgViewBox(rootTag.attributes)
    val scaleX = targetSizePx.toFloat() / viewBox.width
    val scaleY = targetSizePx.toFloat() / viewBox.height
    val uniformScale = minOf(scaleX, scaleY)
    val offsetX = (targetSizePx - viewBox.width * uniformScale) / 2f
    val offsetY = (targetSizePx - viewBox.height * uniformScale) / 2f

    val bitmap = ImageBitmap(targetSizePx, targetSizePx)
    val canvas = Canvas(bitmap)
    val defaultTint = Color(0xFF4B47D6)
    val rootStyle = resolveTagStyle(
        attributes = rootTag.attributes,
        parentStyle = SvgStyleState(
            fillColor = defaultTint,
            strokeColor = null,
            strokeWidth = 1.5f,
            strokeCap = StrokeCap.Round,
            strokeJoin = StrokeJoin.Round,
        ),
        defaultCurrentColor = defaultTint,
    )

    canvas.save()
    canvas.translate(offsetX, offsetY)
    canvas.scale(uniformScale, uniformScale)
    canvas.translate(-viewBox.minX, -viewBox.minY)
    drawSvgElements(elements, rootStyle, defaultTint, canvas)
    canvas.restore()
    return bitmap
}

private data class ParsedSvgTag(
    val name: String,
    val isClosing: Boolean,
    val isSelfClosing: Boolean,
    val attributes: Map<String, String>,
)

private val SvgTagRegex = Regex("<(/?)([a-zA-Z0-9:_-]+)([^>]*)>")
private val SvgAttrRegex = Regex("""([a-zA-Z0-9:_-]+)\s*=\s*(['"])(.*?)\2""")
private val NumberTokenRegex = Regex("""[-+]?\d*\.?\d+(?:[eE][-+]?\d+)?""")

private fun extractSvgTags(svgText: String): List<ParsedSvgTag> {
    val result = mutableListOf<ParsedSvgTag>()
    for (match in SvgTagRegex.findAll(svgText)) {
        val isClosing = match.groupValues[1] == "/"
        val tagName = match.groupValues[2].substringAfter(':').lowercase()
        val rawBody = match.groupValues[3]
        val isSelfClosing = rawBody.trimEnd().endsWith("/")
        val attrs = if (isClosing) emptyMap() else parseTagAttributes(rawBody)
        result.add(
            ParsedSvgTag(
                name = tagName,
                isClosing = isClosing,
                isSelfClosing = isSelfClosing,
                attributes = attrs,
            ),
        )
    }
    return result
}

private fun parseTagAttributes(rawBody: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (match in SvgAttrRegex.findAll(rawBody)) {
        val key = match.groupValues[1].lowercase()
        val value = match.groupValues[3].trim()
        map[key] = value
    }
    val inlineStyle = map["style"]
    if (!inlineStyle.isNullOrBlank()) {
        inlineStyle.split(';').forEach { declaration ->
            val colonIdx = declaration.indexOf(':')
            if (colonIdx > 0) {
                val styleKey = declaration.substring(0, colonIdx).trim().lowercase()
                val styleVal = declaration.substring(colonIdx + 1).trim()
                val hasBothParts = styleKey.isNotEmpty()
                val hasValue = styleVal.isNotEmpty()
                if (hasBothParts) {
                    if (hasValue) {
                        map[styleKey] = styleVal
                    }
                }
            }
        }
    }
    return map
}

private fun parseSvgViewBox(rootAttrs: Map<String, String>): SvgViewBox {
    val rawViewBox = rootAttrs["viewbox"]
    if (!rawViewBox.isNullOrBlank()) {
        val numbers = NumberTokenRegex.findAll(rawViewBox)
            .mapNotNull { match -> match.value.toFloatOrNull() }
            .toList()
        if (numbers.size >= 4) {
            val vbW = numbers[2]
            val vbH = numbers[3]
            if (vbW > 0f) {
                if (vbH > 0f) {
                    return SvgViewBox(numbers[0], numbers[1], vbW, vbH)
                }
            }
        }
    }
    val w = parseDimensionFloat(rootAttrs["width"]) ?: 24f
    val h = parseDimensionFloat(rootAttrs["height"]) ?: 24f
    return SvgViewBox(0f, 0f, if (w > 0f) w else 24f, if (h > 0f) h else 24f)
}

private fun parseDimensionFloat(raw: String?): Float? {
    if (raw.isNullOrBlank()) return null
    val token = NumberTokenRegex.find(raw)?.value ?: return null
    return token.toFloatOrNull()
}

private fun resolveTagStyle(
    attributes: Map<String, String>,
    parentStyle: SvgStyleState,
    defaultCurrentColor: Color,
): SvgStyleState {
    val resolvedFill = if (attributes.containsKey("fill")) {
        parseSvgColor(attributes["fill"], defaultCurrentColor)
    } else {
        parentStyle.fillColor
    }
    val resolvedStroke = if (attributes.containsKey("stroke")) {
        parseSvgColor(attributes["stroke"], defaultCurrentColor)
    } else {
        parentStyle.strokeColor
    }
    val resolvedWidth = parseDimensionFloat(attributes["stroke-width"]) ?: parentStyle.strokeWidth
    val cap = when (attributes["stroke-linecap"]?.lowercase()) {
        "butt" -> StrokeCap.Butt
        "square" -> StrokeCap.Square
        "round" -> StrokeCap.Round
        else -> parentStyle.strokeCap
    }
    val join = when (attributes["stroke-linejoin"]?.lowercase()) {
        "miter" -> StrokeJoin.Miter
        "bevel" -> StrokeJoin.Bevel
        "round" -> StrokeJoin.Round
        else -> parentStyle.strokeJoin
    }
    return SvgStyleState(
        fillColor = resolvedFill,
        strokeColor = resolvedStroke,
        strokeWidth = resolvedWidth,
        strokeCap = cap,
        strokeJoin = join,
    )
}

private fun parseSvgColor(rawColor: String?, currentColor: Color): Color? {
    val trimmed = rawColor?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    val lower = trimmed.lowercase()
    if (lower == "none") return null
    if (lower == "transparent") return null
    if (lower == "currentcolor") return currentColor
    if (lower == "black") return Color.Black
    if (lower == "white") return Color.White
    if (trimmed.startsWith("#")) {
        return parseHexColor(trimmed.substring(1))
    }
    return null
}

private fun parseHexColor(hex: String): Color? {
    return when (hex.length) {
        3 -> {
            val r = hex[0].digitToIntOrNull(16)?.times(17) ?: return null
            val g = hex[1].digitToIntOrNull(16)?.times(17) ?: return null
            val b = hex[2].digitToIntOrNull(16)?.times(17) ?: return null
            Color(red = r, green = g, blue = b)
        }

        6 -> {
            val rgb = hex.toLongOrNull(16) ?: return null
            Color(
                red = ((rgb shr 16) and 0xFF).toInt(),
                green = ((rgb shr 8) and 0xFF).toInt(),
                blue = (rgb and 0xFF).toInt(),
            )
        }

        8 -> {
            val rgba = hex.toLongOrNull(16) ?: return null
            Color(
                red = ((rgba shr 24) and 0xFF).toInt(),
                green = ((rgba shr 16) and 0xFF).toInt(),
                blue = ((rgba shr 8) and 0xFF).toInt(),
                alpha = (rgba and 0xFF).toInt(),
            )
        }

        else -> null
    }
}

private val SkippedSvgContainerTags = setOf("defs", "style", "title")

private fun drawSvgElements(
    elements: List<ParsedSvgTag>,
    rootStyle: SvgStyleState,
    defaultTint: Color,
    canvas: Canvas,
) {
    val styleStack = mutableListOf(rootStyle)
    for (tag in elements) {
        if (tag.name == "svg") continue
        if (tag.name in SkippedSvgContainerTags) continue
        if (tag.isClosing) {
            if (tag.name == "g") {
                if (styleStack.size > 1) {
                    styleStack.removeAt(styleStack.lastIndex)
                }
            }
            continue
        }
        val activeStyle = resolveTagStyle(tag.attributes, styleStack.last(), defaultTint)
        if (tag.name == "g") {
            if (!tag.isSelfClosing) {
                styleStack.add(activeStyle)
            }
            continue
        }
        val shapePath = buildShapePath(tag) ?: continue
        paintShapePath(shapePath, activeStyle, canvas)
    }
}

private fun buildShapePath(tag: ParsedSvgTag): Path? {
    val attrs = tag.attributes
    return when (tag.name) {
        "path" -> {
            val pathData = attrs["d"]?.trim().orEmpty()
            if (pathData.isEmpty()) return null
            runCatching { PathParser().parsePathString(pathData).toPath() }.getOrNull()
        }

        "circle" -> {
            val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
            val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
            val radius = attrs["r"]?.toFloatOrNull() ?: return null
            if (radius <= 0f) return null
            Path().apply { addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius)) }
        }

        "ellipse" -> {
            val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
            val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
            val rx = attrs["rx"]?.toFloatOrNull() ?: return null
            val ry = attrs["ry"]?.toFloatOrNull() ?: return null
            Path().apply { addOval(Rect(cx - rx, cy - ry, cx + rx, cy + ry)) }
        }

        "rect" -> {
            val x = attrs["x"]?.toFloatOrNull() ?: 0f
            val y = attrs["y"]?.toFloatOrNull() ?: 0f
            val w = attrs["width"]?.toFloatOrNull() ?: return null
            val h = attrs["height"]?.toFloatOrNull() ?: return null
            val rx = attrs["rx"]?.toFloatOrNull() ?: attrs["ry"]?.toFloatOrNull() ?: 0f
            val ry = attrs["ry"]?.toFloatOrNull() ?: rx
            Path().apply {
                addRoundRect(RoundRect(Rect(x, y, x + w, y + h), CornerRadius(rx, ry)))
            }
        }

        "line" -> {
            val x1 = attrs["x1"]?.toFloatOrNull() ?: 0f
            val y1 = attrs["y1"]?.toFloatOrNull() ?: 0f
            val x2 = attrs["x2"]?.toFloatOrNull() ?: 0f
            val y2 = attrs["y2"]?.toFloatOrNull() ?: 0f
            Path().apply {
                moveTo(x1, y1)
                lineTo(x2, y2)
            }
        }

        "polyline", "polygon" -> buildPolyPath(attrs["points"], closePath = tag.name == "polygon")

        else -> null
    }
}

private fun buildPolyPath(rawPoints: String?, closePath: Boolean): Path? {
    if (rawPoints.isNullOrBlank()) return null
    val coords = NumberTokenRegex.findAll(rawPoints)
        .mapNotNull { match -> match.value.toFloatOrNull() }
        .toList()
    if (coords.size < 4) return null
    val path = Path()
    path.moveTo(coords[0], coords[1])
    for (index in 2 until coords.size - 1 step 2) {
        path.lineTo(coords[index], coords[index + 1])
    }
    if (closePath) {
        path.close()
    }
    return path
}

private fun paintShapePath(
    path: Path,
    style: SvgStyleState,
    canvas: Canvas,
) {
    if (style.fillColor != null) {
        val fillPaint = Paint().apply {
            color = style.fillColor
            this.style = PaintingStyle.Fill
            isAntiAlias = true
        }
        canvas.drawPath(path, fillPaint)
    }
    if (style.strokeColor != null) {
        val strokePaint = Paint().apply {
            color = style.strokeColor
            this.style = PaintingStyle.Stroke
            strokeWidth = style.strokeWidth
            strokeCap = style.strokeCap
            strokeJoin = style.strokeJoin
            isAntiAlias = true
        }
        canvas.drawPath(path, strokePaint)
    }
}
