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
 * Tamaños normalizados del logotipo oficial (`Logo`, Sección 4.1).
 */
public enum class RfLogoSize(public val dp: Dp) {
    SMALL(28.dp),
    MEDIUM(40.dp),
    LARGE(64.dp),
    HERO(116.dp),
}

/**
 * Componente `Logo` (Sección 4.1): muestra el isotipo/logotipo oficial de RosaFiesta.
 */
@Composable
public fun RfLogo(
    size: RfLogoSize = RfLogoSize.MEDIUM,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Res.drawable.logo_rosafiesta),
        contentDescription = "Logo de RosaFiesta",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size.dp),
    )
}

/**
 * Tamaños normalizados del avatar `Rosa` (Sección 4.2: 28, 38, 52, 76 dp).
 */
public enum class RosaAvatarSize(public val dp: Dp) {
    MINI(28.dp),
    MEDIUM(38.dp),
    LARGE(52.dp),
    HERO(76.dp),
}

/**
 * Componente `Rosa` (Sección 4.2): avatar circular del asistente inteligente Rosa IA.
 * En la App Personal usa el degradado rosa->violeta; en la App Administrativa usa SIEMPRE índigo->turquesa.
 */
@Composable
public fun RosaAvatar(
    size: RosaAvatarSize = RosaAvatarSize.MEDIUM,
    forceAdminPalette: Boolean? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val useAdmin = forceAdminPalette ?: palette.isAdmin
    val gradient = if (useAdmin) RfColors.AdminHeroAndRosaGradient else RfColors.PersonalHeroGradient

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(gradient)
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
    ) {
        Canvas(modifier = Modifier.size(size.dp * 0.54f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val path = Path().apply {
                moveTo(cx, 0f)
                quadraticTo(cx + w * 0.12f, cy - h * 0.12f, w, cy)
                quadraticTo(cx + w * 0.12f, cy + h * 0.12f, cx, h)
                quadraticTo(cx - w * 0.12f, cy + h * 0.12f, 0f, cy)
                quadraticTo(cx - w * 0.12f, cy - h * 0.12f, cx, 0f)
                close()
            }
            drawPath(path = path, color = Color.White)
            drawCircle(
                color = RfColors.Sol,
                radius = w * 0.11f,
                center = Offset(w * 0.82f, h * 0.2f),
            )
        }
    }
}

/**
 * Claves de fotografía real para el componente `Foto` (Sección 4.6).
 */
public enum class RfPhotoKey {
    SILLA_TIFFANY,
    MESA_PLEGABLE,
    MESA_REDONDA,
    CENTRO_FLORAL,
    ARCO_ROSA,
    MESA_HELADOS,
    BODA,
    QUINCEANERA,
    CUMPLE_INFANTIL,
    BABY_SHOWER,
    GRADUACION,
    CORPORATIVO,
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
 * Componente `Foto` (Sección 4.6): fotografía real con esquinas redondeadas y sombreado inferior opcional.
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
 * Catálogo de íconos vectoriales de línea (`Icon`, Sección 4.5: viewBox 24x24, trazo 1.8px redondeado).
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
}

/**
 * Componente `Icon` (Sección 4.5): ícono vectorial de trazo 1.8px con extremos redondeados.
 */
@Composable
public fun RfIcon(
    kind: RfIconKind,
    tint: Color = LocalRfPalette.current.ink,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
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
                drawPath(p, color = tint, style = stroke)
            }

            RfIconKind.CATALOG -> {
                drawRoundRect(tint, Offset(w * 0.15f, h * 0.15f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.15f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.15f, h * 0.55f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.55f), Size(w * 0.3f, h * 0.3f), CornerRadius(3f, 3f), stroke)
            }

            RfIconKind.SPARK -> {
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
                drawPath(p, color = tint, style = stroke)
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

            RfIconKind.ORDERS -> {
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
        }
    }
}

/**
 * Componente `Illo` (Sección 4.7): ilustraciones vectoriales editoriales para categorías y estados vacíos.
 */
@Composable
public fun RfIllo(
    title: String,
    accentColor: Color = RfColors.RosaBrand,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .padding(10.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            drawCircle(
                color = accentColor.copy(alpha = 0.24f),
                radius = w * 0.38f,
                center = Offset(w * 0.42f, h * 0.52f),
            )
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(w * 0.24f, h * 0.24f),
                size = Size(w * 0.52f, h * 0.52f),
                cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
                style = Stroke(width = w * 0.08f, cap = StrokeCap.Round),
            )
            drawCircle(
                color = RfColors.Sol,
                radius = w * 0.12f,
                center = Offset(w * 0.76f, h * 0.24f),
            )
        }
    }
}
