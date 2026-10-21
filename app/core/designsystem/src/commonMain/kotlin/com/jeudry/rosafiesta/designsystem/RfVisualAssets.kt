package com.jeudry.rosafiesta.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.designsystem.resources.Res
import com.jeudry.rosafiesta.designsystem.resources.foto_arco_rosa
import com.jeudry.rosafiesta.designsystem.resources.foto_baby_shower
import com.jeudry.rosafiesta.designsystem.resources.foto_boda
import com.jeudry.rosafiesta.designsystem.resources.foto_centro_floral
import com.jeudry.rosafiesta.designsystem.resources.foto_corporativo
import com.jeudry.rosafiesta.designsystem.resources.foto_cumple_infantil
import com.jeudry.rosafiesta.designsystem.resources.foto_graduacion
import com.jeudry.rosafiesta.designsystem.resources.foto_mesa_helados
import com.jeudry.rosafiesta.designsystem.resources.foto_mesa_plegable
import com.jeudry.rosafiesta.designsystem.resources.foto_mesa_redonda
import com.jeudry.rosafiesta.designsystem.resources.foto_quinceanera
import com.jeudry.rosafiesta.designsystem.resources.foto_silla_tiffany
import com.jeudry.rosafiesta.designsystem.resources.logo_rosafiesta
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Tamaños normalizados del logotipo oficial (`Logo`, Sección 3 de `entrega-de-diseno.md`).
 */
public enum class RfLogoSize(public val dp: Dp) {
    SMALL(32.dp),
    MEDIUM(42.dp),
    LARGE(64.dp),
    HERO(108.dp),
}

/**
 * Componente `Logo` (Sección 3):
 * Logo oficial **siempre sobre fondo blanco**: círculo blanco con anillo `rgba(234,78,169,.14)` (`RfColors.LogoRing`).
 */
@Composable
public fun RfLogo(
    size: RfLogoSize = RfLogoSize.MEDIUM,
    customSize: Dp? = null,
    modifier: Modifier = Modifier,
) {
    val resolvedSize = customSize ?: size.dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(resolvedSize)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.5.dp, RfColors.LogoRing, CircleShape)
            .padding(resolvedSize * 0.14f),
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_rosafiesta),
            contentDescription = "Logo oficial de RosaFiesta",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Tamaños normalizados del avatar `Rosa` (Sección 3: 28, 38, 52, 56, 76 dp).
 */
public enum class RosaAvatarSize(public val dp: Dp) {
    MINI(28.dp),
    MEDIUM(38.dp),
    LARGE(52.dp),
    TAB_CENTER(56.dp),
    HERO(76.dp),
    XLARGE(76.dp),
}

/**
 * Componente `Rosa` (Sección 3 de `entrega-de-diseno.md`):
 * Mujer con audífonos y micrófono.
 * `bg` es el degradado `hero` en personal y `rosa-admin-bg` (índigo → turquesa) en admin.
 */
@Composable
public fun RosaAvatar(
    size: RosaAvatarSize = RosaAvatarSize.MEDIUM,
    glow: Boolean = false,
    forceAdminPalette: Boolean? = null,
    customSize: Dp? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val useAdmin = forceAdminPalette ?: palette.isAdmin
    val gradient = if (useAdmin) RfColors.AdminHeroAndRosaGradient else RfColors.PersonalHeroGradient
    val avatarDiameter = customSize ?: size.dp
    val outerDiameter = if (glow) avatarDiameter + 10.dp else avatarDiameter
    val glowColor = if (useAdmin) RfColors.IndigoBrand.copy(alpha = 0.28f) else RfColors.RosaBrand.copy(alpha = 0.28f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(outerDiameter),
    ) {
        if (glow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                        ),
                    ),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarDiameter)
                .clip(CircleShape)
                .background(gradient)
                .border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape),
        ) {
            Canvas(modifier = Modifier.size(avatarDiameter * 0.72f)) {
                val w = this.size.width
                val h = this.size.height
                val stroke = Stroke(width = w * 0.068f, cap = StrokeCap.Round, join = StrokeJoin.Round)

                // Cabello / silueta superior de la mujer
                val hairPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.56f)
                    cubicTo(w * 0.20f, h * 0.20f, w * 0.80f, h * 0.20f, w * 0.78f, h * 0.56f)
                    cubicTo(w * 0.82f, h * 0.72f, w * 0.74f, h * 0.86f, w * 0.66f, h * 0.88f)
                    lineTo(w * 0.34f, h * 0.88f)
                    cubicTo(w * 0.26f, h * 0.86f, w * 0.18f, h * 0.72f, w * 0.22f, h * 0.56f)
                    close()
                }
                drawPath(hairPath, color = Color.White.copy(alpha = 0.22f))

                // Diadema de los audífonos (arco superior)
                drawArc(
                    color = Color.White,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.14f, h * 0.12f),
                    size = Size(w * 0.72f, h * 0.62f),
                    style = Stroke(width = w * 0.075f, cap = StrokeCap.Round),
                )

                // Rostro de la mujer
                drawCircle(
                    color = Color.White,
                    radius = w * 0.23f,
                    center = Offset(w * 0.50f, h * 0.52f),
                )

                // Flequillo / onda del cabello sobre la frente
                val bangsPath = Path().apply {
                    moveTo(w * 0.29f, h * 0.45f)
                    quadraticTo(w * 0.44f, h * 0.31f, w * 0.58f, h * 0.40f)
                    quadraticTo(w * 0.66f, h * 0.44f, w * 0.71f, h * 0.47f)
                }
                drawPath(
                    path = bangsPath,
                    color = if (useAdmin) RfColors.IndigoBrand else RfColors.RosaBrand,
                    style = Stroke(width = w * 0.055f, cap = StrokeCap.Round),
                )

                // Ojos y sonrisa amigable
                val featureColor = if (useAdmin) RfColors.AdminInk else RfColors.PersonalInk
                drawCircle(featureColor, radius = w * 0.028f, center = Offset(w * 0.42f, h * 0.51f))
                drawCircle(featureColor, radius = w * 0.028f, center = Offset(w * 0.58f, h * 0.51f))
                drawArc(
                    color = featureColor,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.42f, h * 0.54f),
                    size = Size(w * 0.16f, h * 0.11f),
                    style = Stroke(width = w * 0.038f, cap = StrokeCap.Round),
                )

                // Auricular izquierdo y derecho (copas de los audífonos)
                drawRoundRect(
                    color = RfColors.Sol,
                    topLeft = Offset(w * 0.09f, h * 0.40f),
                    size = Size(w * 0.14f, h * 0.24f),
                    cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
                )
                drawRoundRect(
                    color = RfColors.Sol,
                    topLeft = Offset(w * 0.77f, h * 0.40f),
                    size = Size(w * 0.14f, h * 0.24f),
                    cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
                )

                // Brazo del micrófono saliendo del audífono izquierdo hacia la boca
                val micBoom = Path().apply {
                    moveTo(w * 0.16f, h * 0.60f)
                    quadraticTo(w * 0.24f, h * 0.74f, w * 0.46f, h * 0.67f)
                }
                drawPath(micBoom, color = Color.White, style = stroke)
                // Cápsula del micrófono
                drawCircle(
                    color = RfColors.Sol,
                    radius = w * 0.055f,
                    center = Offset(w * 0.48f, h * 0.67f),
                )
            }
        }
    }
}

/**
 * Claves de fotografía real para el componente `Foto` (Sección 3 de `entrega-de-diseno.md`).
 */
public enum class RfPhotoKey(public val assetKey: String) {
    SILLA_TIFFANY("silla-tiffany"),
    MESA_PLEGABLE("mesa-plegable"),
    MESA_REDONDA("mesa-redonda"),
    CENTRO_FLORAL("centro-floral"),
    ARCO_ROSA("arco-rosa"),
    MESA_HELADOS("mesa-helados"),
    BODA("boda"),
    QUINCEANERA("quinceanera"),
    CUMPLE_INFANTIL("cumple-infantil"),
    BABY_SHOWER("baby-shower"),
    GRADUACION("graduacion"),
    CORPORATIVO("corporativo"),
}

private fun drawableForPhotoKey(key: RfPhotoKey): DrawableResource = when (key) {
    RfPhotoKey.SILLA_TIFFANY -> Res.drawable.foto_silla_tiffany
    RfPhotoKey.MESA_PLEGABLE -> Res.drawable.foto_mesa_plegable
    RfPhotoKey.MESA_REDONDA -> Res.drawable.foto_mesa_redonda
    RfPhotoKey.CENTRO_FLORAL -> Res.drawable.foto_centro_floral
    RfPhotoKey.ARCO_ROSA -> Res.drawable.foto_arco_rosa
    RfPhotoKey.MESA_HELADOS -> Res.drawable.foto_mesa_helados
    RfPhotoKey.BODA -> Res.drawable.foto_boda
    RfPhotoKey.QUINCEANERA -> Res.drawable.foto_quinceanera
    RfPhotoKey.CUMPLE_INFANTIL -> Res.drawable.foto_cumple_infantil
    RfPhotoKey.BABY_SHOWER -> Res.drawable.foto_baby_shower
    RfPhotoKey.GRADUACION -> Res.drawable.foto_graduacion
    RfPhotoKey.CORPORATIVO -> Res.drawable.foto_corporativo
}

/**
 * Componente `Foto` (Sección 3): fotos reales de producto y evento con clave como nombre.
 */
@Composable
public fun RfPhoto(
    photoKey: RfPhotoKey,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    bottomScrim: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFFEDEAF4)),
    ) {
        Image(
            painter = painterResource(drawableForPhotoKey(photoKey)),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (bottomScrim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99141223),
                            ),
                        ),
                    ),
            )
        }
    }
}

/**
 * Catálogo de íconos vectoriales (`Icon`, Sección 3: trazo de 2 px y esquinas redondeadas).
 */
public enum class RfIconKind {
    HOME,
    CATALOG,
    SPARK,
    CALENDAR,
    USER,
    ORDERS,
    SETTINGS,
    SEARCH,
    BELL,
    CHECK,
    WARNING,
    TRUCK,
    WALLET,
    CAMERA,
    QR,
    WIFI_OFF,
    PLUS,
    FILTER,
    SHIELD,
    HEART,
    BACK,
    MORE_VERT,
    MIC,
    CHAT,
    SHARE,
    STAR,
    SEND,
    CLOCK,
    LOCATION,
    TRASH,
    EDIT,
    CLOSE,
    SPARKLE,
    MORE_VERTICAL,
    RECEIPT,
}

/**
 * Componente `Icon` (Sección 3): trazo de 2 px y esquinas redondeadas.
 */
@Composable
public fun RfIcon(
    kind: RfIconKind,
    tint: Color = LocalRfPalette.current.ink,
    size: Dp = 20.dp,
    strokeWidthFactor: Float = 0.09f,
    filled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * strokeWidthFactor, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (kind) {
            RfIconKind.HOME -> {
                val p = Path().apply {
                    moveTo(w * 0.15f, h * 0.46f)
                    lineTo(w * 0.5f, h * 0.16f)
                    lineTo(w * 0.85f, h * 0.46f)
                    lineTo(w * 0.85f, h * 0.84f)
                    lineTo(w * 0.15f, h * 0.84f)
                    close()
                }
                if (filled) drawPath(p, color = tint) else drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.CATALOG -> {
                drawRoundRect(tint, Offset(w * 0.15f, h * 0.15f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.15f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.15f, h * 0.55f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.55f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
            }

            RfIconKind.SPARK, RfIconKind.SPARKLE -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    lineTo(w * 0.62f, h * 0.38f)
                    lineTo(w * 0.9f, h * 0.5f)
                    lineTo(w * 0.62f, h * 0.62f)
                    lineTo(w * 0.5f, h * 0.9f)
                    lineTo(w * 0.38f, h * 0.62f)
                    lineTo(w * 0.1f, h * 0.5f)
                    lineTo(w * 0.38f, h * 0.38f)
                    close()
                }
                if (filled) drawPath(p, color = tint) else drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.CALENDAR -> {
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.22f), Size(w * 0.72f, h * 0.64f), CornerRadius(4f, 4f), stroke)
                drawLine(tint, Offset(w * 0.14f, h * 0.38f), Offset(w * 0.86f, h * 0.38f), stroke.width)
                drawLine(tint, Offset(w * 0.32f, h * 0.12f), Offset(w * 0.32f, h * 0.26f), stroke.width)
                drawLine(tint, Offset(w * 0.68f, h * 0.12f), Offset(w * 0.68f, h * 0.26f), stroke.width)
            }

            RfIconKind.USER -> {
                drawCircle(tint, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.34f), style = stroke)
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.18f, h * 0.56f),
                    size = Size(w * 0.64f, h * 0.42f),
                    style = stroke,
                )
            }

            RfIconKind.ORDERS, RfIconKind.RECEIPT -> {
                drawRoundRect(tint, Offset(w * 0.2f, h * 0.14f), Size(w * 0.6f, h * 0.72f), CornerRadius(4f, 4f), stroke)
                drawLine(tint, Offset(w * 0.32f, h * 0.36f), Offset(w * 0.68f, h * 0.36f), stroke.width)
                drawLine(tint, Offset(w * 0.32f, h * 0.52f), Offset(w * 0.68f, h * 0.52f), stroke.width)
                drawLine(tint, Offset(w * 0.32f, h * 0.68f), Offset(w * 0.54f, h * 0.68f), stroke.width)
            }

            RfIconKind.SETTINGS -> {
                drawCircle(tint, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
            }

            RfIconKind.SEARCH -> {
                drawCircle(tint, radius = w * 0.26f, center = Offset(w * 0.44f, h * 0.44f), style = stroke)
                drawLine(tint, Offset(w * 0.64f, h * 0.64f), Offset(w * 0.86f, h * 0.86f), stroke.width)
            }

            RfIconKind.BELL -> {
                drawArc(tint, 180f, 180f, false, Offset(w * 0.22f, h * 0.18f), Size(w * 0.56f, h * 0.54f), style = stroke)
                drawLine(tint, Offset(w * 0.16f, h * 0.72f), Offset(w * 0.84f, h * 0.72f), stroke.width)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.82f))
            }

            RfIconKind.CHECK -> {
                val p = Path().apply {
                    moveTo(w * 0.2f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.74f)
                    lineTo(w * 0.82f, h * 0.28f)
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.WARNING -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.16f)
                    lineTo(w * 0.86f, h * 0.82f)
                    lineTo(w * 0.14f, h * 0.82f)
                    close()
                }
                drawPath(p, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.42f), Offset(w * 0.5f, h * 0.62f), stroke.width)
            }

            RfIconKind.TRUCK -> {
                drawRoundRect(tint, Offset(w * 0.1f, h * 0.28f), Size(w * 0.5f, h * 0.38f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.6f, h * 0.4f), Size(w * 0.26f, h * 0.26f), CornerRadius(3f, 3f), stroke)
                drawCircle(tint, radius = w * 0.09f, center = Offset(w * 0.28f, h * 0.74f), style = stroke)
                drawCircle(tint, radius = w * 0.09f, center = Offset(w * 0.72f, h * 0.74f), style = stroke)
            }

            RfIconKind.WALLET -> {
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.24f), Size(w * 0.72f, h * 0.54f), CornerRadius(4f, 4f), stroke)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.68f, h * 0.51f))
            }

            RfIconKind.CAMERA -> {
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.28f), Size(w * 0.72f, h * 0.52f), CornerRadius(4f, 4f), stroke)
                drawCircle(tint, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.54f), style = stroke)
            }

            RfIconKind.QR -> {
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.16f), Size(w * 0.26f, h * 0.26f), CornerRadius(2f, 2f), stroke)
                drawRoundRect(tint, Offset(w * 0.58f, h * 0.16f), Size(w * 0.26f, h * 0.26f), CornerRadius(2f, 2f), stroke)
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.58f), Size(w * 0.26f, h * 0.26f), CornerRadius(2f, 2f), stroke)
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.7f, h * 0.7f))
            }

            RfIconKind.WIFI_OFF -> {
                drawArc(tint, 210f, 120f, false, Offset(w * 0.16f, h * 0.28f), Size(w * 0.68f, h * 0.5f), style = stroke)
                drawLine(tint, Offset(w * 0.18f, h * 0.18f), Offset(w * 0.82f, h * 0.82f), stroke.width)
            }

            RfIconKind.PLUS -> {
                drawLine(tint, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), stroke.width)
                drawLine(tint, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), stroke.width)
            }

            RfIconKind.FILTER -> {
                drawLine(tint, Offset(w * 0.18f, h * 0.28f), Offset(w * 0.82f, h * 0.28f), stroke.width)
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.72f, h * 0.5f), stroke.width)
                drawLine(tint, Offset(w * 0.38f, h * 0.72f), Offset(w * 0.62f, h * 0.72f), stroke.width)
            }

            RfIconKind.SHIELD -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.14f)
                    lineTo(w * 0.82f, h * 0.28f)
                    lineTo(w * 0.76f, h * 0.66f)
                    lineTo(w * 0.5f, h * 0.88f)
                    lineTo(w * 0.24f, h * 0.66f)
                    lineTo(w * 0.18f, h * 0.28f)
                    close()
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.HEART -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.15f, h * 0.58f, w * 0.12f, h * 0.26f, w * 0.32f, h * 0.22f)
                    cubicTo(w * 0.42f, h * 0.20f, w * 0.48f, h * 0.28f, w * 0.5f, h * 0.34f)
                    cubicTo(w * 0.52f, h * 0.28f, w * 0.58f, h * 0.20f, w * 0.68f, h * 0.22f)
                    cubicTo(w * 0.88f, h * 0.26f, w * 0.85f, h * 0.58f, w * 0.5f, h * 0.82f)
                    close()
                }
                if (filled) drawPath(p, color = tint) else drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.BACK -> {
                val p = Path().apply {
                    moveTo(w * 0.62f, h * 0.22f)
                    lineTo(w * 0.32f, h * 0.50f)
                    lineTo(w * 0.62f, h * 0.78f)
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.MORE_VERT, RfIconKind.MORE_VERTICAL -> {
                drawCircle(tint, radius = w * 0.065f, center = Offset(w * 0.5f, h * 0.24f))
                drawCircle(tint, radius = w * 0.065f, center = Offset(w * 0.5f, h * 0.50f))
                drawCircle(tint, radius = w * 0.065f, center = Offset(w * 0.5f, h * 0.76f))
            }

            RfIconKind.MIC -> {
                drawRoundRect(tint, Offset(w * 0.36f, h * 0.16f), Size(w * 0.28f, h * 0.44f), CornerRadius(w * 0.14f, w * 0.14f), stroke)
                drawArc(tint, 0f, 180f, false, Offset(w * 0.24f, h * 0.34f), Size(w * 0.52f, h * 0.38f), style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.72f), Offset(w * 0.5f, h * 0.86f), stroke.width)
            }

            RfIconKind.CHAT -> {
                val p = Path().apply {
                    moveTo(w * 0.18f, h * 0.22f)
                    lineTo(w * 0.82f, h * 0.22f)
                    lineTo(w * 0.82f, h * 0.66f)
                    lineTo(w * 0.42f, h * 0.66f)
                    lineTo(w * 0.22f, h * 0.82f)
                    lineTo(w * 0.22f, h * 0.66f)
                    lineTo(w * 0.18f, h * 0.66f)
                    close()
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.SHARE -> {
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.72f, h * 0.24f), style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.26f, h * 0.50f), style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.72f, h * 0.76f), style = stroke)
                drawLine(tint, Offset(w * 0.35f, h * 0.45f), Offset(w * 0.63f, h * 0.29f), stroke.width)
                drawLine(tint, Offset(w * 0.35f, h * 0.55f), Offset(w * 0.63f, h * 0.71f), stroke.width)
            }

            RfIconKind.STAR -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.14f)
                    lineTo(w * 0.61f, h * 0.38f)
                    lineTo(w * 0.88f, h * 0.41f)
                    lineTo(w * 0.68f, h * 0.59f)
                    lineTo(w * 0.73f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.72f)
                    lineTo(w * 0.27f, h * 0.85f)
                    lineTo(w * 0.32f, h * 0.59f)
                    lineTo(w * 0.12f, h * 0.41f)
                    lineTo(w * 0.39f, h * 0.38f)
                    close()
                }
                if (filled) drawPath(p, color = tint) else drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.SEND -> {
                val p = Path().apply {
                    moveTo(w * 0.16f, h * 0.46f)
                    lineTo(w * 0.84f, h * 0.18f)
                    lineTo(w * 0.58f, h * 0.84f)
                    lineTo(w * 0.46f, h * 0.56f)
                    close()
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.CLOCK -> {
                drawCircle(tint, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.30f), Offset(w * 0.5f, h * 0.50f), stroke.width)
                drawLine(tint, Offset(w * 0.5f, h * 0.50f), Offset(w * 0.66f, h * 0.58f), stroke.width)
            }

            RfIconKind.LOCATION -> {
                drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.40f), style = stroke)
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.86f)
                    cubicTo(w * 0.22f, h * 0.60f, w * 0.22f, h * 0.18f, w * 0.5f, h * 0.18f)
                    cubicTo(w * 0.78f, h * 0.18f, w * 0.78f, h * 0.60f, w * 0.5f, h * 0.86f)
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.TRASH -> {
                drawLine(tint, Offset(w * 0.22f, h * 0.26f), Offset(w * 0.78f, h * 0.26f), stroke.width)
                drawRoundRect(tint, Offset(w * 0.28f, h * 0.26f), Size(w * 0.44f, h * 0.58f), CornerRadius(3f, 3f), stroke)
            }

            RfIconKind.EDIT -> {
                val p = Path().apply {
                    moveTo(w * 0.22f, h * 0.78f)
                    lineTo(w * 0.34f, h * 0.74f)
                    lineTo(w * 0.76f, h * 0.32f)
                    lineTo(w * 0.68f, h * 0.24f)
                    lineTo(w * 0.26f, h * 0.66f)
                    close()
                }
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.CLOSE -> {
                drawLine(tint, Offset(w * 0.24f, h * 0.24f), Offset(w * 0.76f, h * 0.76f), stroke.width)
                drawLine(tint, Offset(w * 0.76f, h * 0.24f), Offset(w * 0.24f, h * 0.76f), stroke.width)
            }
        }
    }
}

/**
 * Claves de ilustración vectorial (`Illo`, Sección 3: solo para categorías, momentos y estados vacíos;
 * los productos siempre llevan foto; el arco de globos se mantiene).
 */
public enum class RfIlloKind(public val key: String) {
    GLOBOS("globos"),
    CANDADO("candado"),
    EXITO("exito"),
    CORREO("correo"),
    FLORES("flores"),
    SILLAS("sillas"),
    DULCES("dulces"),
    CALENDARIO("calendario"),
    VACIO("vacio"),
}

private fun resolveIlloKind(rawKeyOrTitle: String): RfIlloKind {
    val norm = rawKeyOrTitle.lowercase()
    return when {
        "globo" in norm || "arco" in norm -> RfIlloKind.GLOBOS
        "candado" in norm || "sesión" in norm || "sesion" in norm || "acceso" in norm || "login" in norm -> RfIlloKind.CANDADO
        "exito" in norm || "éxito" in norm || "confirm" in norm || "enviad" in norm || "listo" in norm -> RfIlloKind.EXITO
        "correo" in norm || "mail" in norm || "mensaje" in norm || "código" in norm -> RfIlloKind.CORREO
        "flor" in norm || "centro" in norm -> RfIlloKind.FLORES
        "silla" in norm || "mesa" in norm || "mobiliario" in norm -> RfIlloKind.SILLAS
        "dulce" in norm || "helado" in norm || "postre" in norm -> RfIlloKind.DULCES
        "fecha" in norm || "calendario" in norm || "agenda" in norm -> RfIlloKind.CALENDARIO
        else -> RfIlloKind.GLOBOS
    }
}

/**
 * Componente `Illo` (Sección 3): ilustraciones solo para categorías, momentos y estados vacíos (`globos`, `candado`, `exito`, `correo`…).
 * El arco de globos se mantiene.
 */
@Composable
public fun RfIllo(
    title: String = "",
    accentColor: Color = RfColors.RosaBrand,
    size: Dp = 60.dp,
    kind: RfIlloKind = if (title.isNotEmpty()) resolveIlloKind(title) else RfIlloKind.GLOBOS,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .padding(10.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = w * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)

            when (kind) {
                RfIlloKind.GLOBOS -> {
                    // Arco orgánico de globos (se mantiene según Sección 3)
                    drawArc(
                        color = accentColor.copy(alpha = 0.35f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.12f, h * 0.18f),
                        size = Size(w * 0.76f, h * 0.74f),
                        style = Stroke(width = w * 0.10f, cap = StrokeCap.Round),
                    )
                    val balloonCenters = listOf(
                        Offset(w * 0.18f, h * 0.64f) to accentColor,
                        Offset(w * 0.24f, h * 0.42f) to RfColors.Sol,
                        Offset(w * 0.38f, h * 0.26f) to accentColor,
                        Offset(w * 0.56f, h * 0.24f) to RfColors.VioletaBrand,
                        Offset(w * 0.74f, h * 0.38f) to RfColors.Sol,
                        Offset(w * 0.82f, h * 0.60f) to accentColor,
                    )
                    balloonCenters.forEach { (center, col) ->
                        drawCircle(color = col, radius = w * 0.13f, center = center)
                    }
                }

                RfIlloKind.CANDADO -> {
                    drawArc(
                        color = accentColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.30f, h * 0.14f),
                        size = Size(w * 0.40f, h * 0.40f),
                        style = stroke,
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.22f, h * 0.42f),
                        size = Size(w * 0.56f, h * 0.44f),
                        cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
                    )
                    drawCircle(color = RfColors.Sol, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.62f))
                }

                RfIlloKind.EXITO -> {
                    drawCircle(color = RfColors.Lima.copy(alpha = 0.28f), radius = w * 0.44f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(color = RfColors.Lima, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f))
                    val check = Path().apply {
                        moveTo(w * 0.36f, h * 0.52f)
                        lineTo(w * 0.46f, h * 0.64f)
                        lineTo(w * 0.66f, h * 0.38f)
                    }
                    drawPath(check, color = RfColors.PersonalInk, style = stroke)
                    drawCircle(color = RfColors.Sol, radius = w * 0.07f, center = Offset(w * 0.82f, h * 0.20f))
                }

                RfIlloKind.CORREO -> {
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.14f, h * 0.26f),
                        size = Size(w * 0.72f, h * 0.50f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = stroke,
                    )
                    val flap = Path().apply {
                        moveTo(w * 0.16f, h * 0.30f)
                        lineTo(w * 0.50f, h * 0.56f)
                        lineTo(w * 0.84f, h * 0.30f)
                    }
                    drawPath(flap, color = accentColor, style = stroke)
                    drawCircle(color = RfColors.Sol, radius = w * 0.10f, center = Offset(w * 0.78f, h * 0.26f))
                }

                RfIlloKind.FLORES -> {
                    val center = Offset(w * 0.5f, h * 0.46f)
                    val r = w * 0.18f
                    listOf(
                        Offset(center.x, center.y - r),
                        Offset(center.x + r, center.y),
                        Offset(center.x, center.y + r),
                        Offset(center.x - r, center.y),
                    ).forEach { petal ->
                        drawCircle(color = accentColor.copy(alpha = 0.75f), radius = w * 0.14f, center = petal)
                    }
                    drawCircle(color = RfColors.Sol, radius = w * 0.12f, center = center)
                }

                RfIlloKind.SILLAS, RfIlloKind.DULCES, RfIlloKind.CALENDARIO, RfIlloKind.VACIO -> {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.22f),
                        radius = w * 0.38f,
                        center = Offset(w * 0.45f, h * 0.52f),
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.24f, h * 0.24f),
                        size = Size(w * 0.52f, h * 0.52f),
                        cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                        style = stroke,
                    )
                    drawCircle(
                        color = RfColors.Sol,
                        radius = w * 0.11f,
                        center = Offset(w * 0.76f, h * 0.24f),
                    )
                }
            }
        }
    }
}
