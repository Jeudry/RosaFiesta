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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
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
    insetPadding: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val resolvedSize = customSize ?: size.dp
    val padding = if (insetPadding) resolvedSize * 0.14f else 0.dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(resolvedSize)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.5.dp, RfColors.LogoRing, CircleShape)
            .padding(padding),
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_rosafiesta),
            contentDescription = "Logo oficial de RosaFiesta",
            contentScale = if (insetPadding) ContentScale.Fit else ContentScale.Crop,
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
        val headband = remember { PathParser().parsePathString("M4.6 13.4V12a7.4 7.4 0 0 1 14.8 0v1.4").toPath() }
        val face = remember { PathParser().parsePathString("M7.6 12.4c0-2.8 1.9-4.9 4.4-4.9s4.4 2.1 4.4 4.9c0 2.7-1.9 4.6-4.4 4.6s-4.4-1.9-4.4-4.6z").toPath() }
        val hairBang = remember { PathParser().parsePathString("M7.9 10.9c1.9 0 3.4-1 4.3-2.7.8 1.6 2.2 2.6 3.9 2.7").toPath() }
        val micBoom = remember { PathParser().parsePathString("M19.4 17.1v.5a2.2 2.2 0 0 1-2.2 2.2h-3.4").toPath() }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarDiameter)
                .clip(CircleShape)
                .background(gradient),
        ) {
            Canvas(modifier = Modifier.size(avatarDiameter * 0.60f)) {
                val s = this.size.width / 24f
                val stroke = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)

                scale(s, s, Offset.Zero) {
                    drawPath(headband, color = Color.White, style = stroke)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(3.1f, 11.9f),
                        size = Size(3f, 5.2f),
                        cornerRadius = CornerRadius(1.5f, 1.5f),
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(17.9f, 11.9f),
                        size = Size(3f, 5.2f),
                        cornerRadius = CornerRadius(1.5f, 1.5f),
                    )
                    drawPath(face, color = Color.White, style = stroke)
                    drawPath(hairBang, color = Color.White, style = stroke)
                    drawCircle(color = Color.White, radius = 0.8f, center = Offset(10.3f, 12.6f))
                    drawCircle(color = Color.White, radius = 0.8f, center = Offset(13.7f, 12.6f))
                    drawPath(micBoom, color = Color.White, style = stroke)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(11.4f, 18.8f),
                        size = Size(3f, 2f),
                        cornerRadius = CornerRadius(1f, 1f),
                    )
                }
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
    STORE,
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
    MORE,
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
    GIFT,
    BOX,
    LOGIN,
    LOGOUT,
    PARTY,
    TREND_UP,
    TREND_DOWN,
    ARROW_RIGHT,
    CHEVRON_DOWN,
    CHEVRON_RIGHT,
}

private val iconPaths: Map<RfIconKind, String> = mapOf(
    RfIconKind.HOME to "M3 10.5L12 3l9 7.5M5 9v11h5v-6h4v6h5V9",
    RfIconKind.CATALOG to "M4 9l1.5-5h13L20 9M4 9h16M4 9a2.7 2.7 0 0 0 5.3 0a2.7 2.7 0 0 0 5.4 0a2.7 2.7 0 0 0 5.3 0M5 11.5V20h14v-8.5M10 20v-5h4v5",
    RfIconKind.STORE to "M4 9l1.5-5h13L20 9M4 9h16M4 9a2.7 2.7 0 0 0 5.3 0a2.7 2.7 0 0 0 5.4 0a2.7 2.7 0 0 0 5.3 0M5 11.5V20h14v-8.5M10 20v-5h4v5",
    RfIconKind.CALENDAR to "M4 6.5A1.5 1.5 0 0 1 5.5 5h13A1.5 1.5 0 0 1 20 6.5v12a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5zM4 10h16M8 3v4M16 3v4",
    RfIconKind.HEART to "M12 20s-7.5-4.6-9.2-9.1C1.7 7.8 3.6 4.5 7 4.5c2 0 3.3 1.1 5 3 1.7-1.9 3-3 5-3 3.4 0 5.3 3.3 4.2 6.4C19.5 15.4 12 20 12 20z",
    RfIconKind.SEARCH to "M4 11a7 7 0 1 0 14 0a7 7 0 1 0 -14 0M20 20l-3.6-3.6",
    RfIconKind.MIC to "M12 3a3 3 0 0 0-3 3v6a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3zM19 11a7 7 0 0 1-14 0M12 18v3",
    RfIconKind.BELL to "M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15zM10 21h4",
    RfIconKind.USER to "M8 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0M4 20c.8-3.6 4-6 8-6s7.2 2.4 8 6",
    RfIconKind.PLUS to "M12 5v14M5 12h14",
    RfIconKind.CHECK to "M5 12.5l4.5 4.5L19 7.5",
    RfIconKind.FILTER to "M4 7h9M17 7h3M15 5v4M4 17h3M11 17h9M9 15v4",
    RfIconKind.BACK to "M19 12H5M11 18l-6-6 6-6",
    RfIconKind.CLOSE to "M18 6L6 18M6 6l12 12",
    RfIconKind.MORE to "M5 12h.01M12 12h.01M19 12h.01",
    RfIconKind.MORE_VERT to "M12 5h.01M12 12h.01M12 19h.01",
    RfIconKind.MORE_VERTICAL to "M12 5h.01M12 12h.01M12 19h.01",
    RfIconKind.SETTINGS to "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z",
    RfIconKind.ORDERS to "M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 13h6M9 17h4",
    RfIconKind.RECEIPT to "M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 13h6M9 17h4",
    RfIconKind.CHAT to "M5 4h14a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1h-8l-5 4v-4H5a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z",
    RfIconKind.SEND to "M21 3L10 14M21 3l-6.5 18-4.5-7-7-4.5z",
    RfIconKind.STAR to "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z",
    RfIconKind.SPARK to "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8zM19 16l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7z",
    RfIconKind.SPARKLE to "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8zM19 16l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7z",
    RfIconKind.CLOCK to "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M12 7v5l3.5 2",
    RfIconKind.LOCATION to "M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21zM9.5 9.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0",
    RfIconKind.TRASH to "M4 7h16M9 7V4.5h6V7M6 7l1 13h10l1-13M10 11v6M14 11v6",
    RfIconKind.EDIT to "M4 20h4L19 9l-4-4L4 16zM13.5 6.5l4 4",
    RfIconKind.SHIELD to "M12 3l8 3v6c0 4.5-3.4 8-8 9-4.6-1-8-4.5-8-9V6zM8.5 12l2.5 2.5 4.5-4.5",
    RfIconKind.TRUCK to "M2 6h11v10H2zM13 9h4.5l3.5 3.5V16h-8M4.5 18.5a1.8 1.8 0 1 0 3.6 0a1.8 1.8 0 1 0 -3.6 0M15.5 18.5a1.8 1.8 0 1 0 3.6 0a1.8 1.8 0 1 0 -3.6 0",
    RfIconKind.WALLET to "M3 6.5A1.5 1.5 0 0 1 4.5 5h15A1.5 1.5 0 0 1 21 6.5v11a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5zM3 10h18M7 15h3",
    RfIconKind.WIFI_OFF to "M1 1l22 22M16.7 8a10.8 10.8 0 0 1 4.3 2.5M5 12.5a7 7 0 0 1 4-1.8M10.7 10.7A7 7 0 0 1 12 10.5c1.4 0 2.7.4 3.8 1.2M8.5 16.5a3.5 3.5 0 0 1 4.8 0M12 20h.01",
    RfIconKind.GIFT to "M3 8h18v4H3zM5 12v9h14v-9M12 8v13M12 8S10.5 3 7.8 3.5C5.5 4 6.5 8 12 8zM12 8s1.5-5 4.2-4.5C18.5 4 17.5 8 12 8z",
    RfIconKind.BOX to "M3 7.5l9-4.5 9 4.5v9L12 21l-9-4.5zM3 7.5l9 4.5 9-4.5M12 12v9",
    RfIconKind.LOGIN to "M15 4h3.5A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5H15M10 16l4-4-4-4M14 12H3",
    RfIconKind.LOGOUT to "M9 4H5.5A1.5 1.5 0 0 0 4 5.5v13A1.5 1.5 0 0 0 5.5 20H9M16 16l4-4-4-4M20 12H9",
    RfIconKind.PARTY to "M4 20l4.5-12 7.5 7.5zM14 4c0 1.5-1 2-1 3.5M17 7.5c1-.8 2.5-1 3.5 0M20 12c-1.4-.3-2.5.2-3.2 1M10 4.5h.01M20 4h.01M21 16h.01",
    RfIconKind.TREND_UP to "M3 17l6-6 4 4 8-8M15 7h6v6",
    RfIconKind.TREND_DOWN to "M3 7l6 6 4-4 8 8M15 17h6v-6",
    RfIconKind.ARROW_RIGHT to "M5 12h14M13 6l6 6-6 6",
    RfIconKind.CHEVRON_DOWN to "M6 9l6 6 6-6",
    RfIconKind.CHEVRON_RIGHT to "M9 18l6-6-6-6",
    RfIconKind.WARNING to "M12 4L2.5 20h19zM12 10v4.5M12 17.5h.01",
    RfIconKind.CAMERA to "M4 7h4l2-3h4l2 3h4a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2zM8 13a4 4 0 1 0 8 0a4 4 0 1 0 -8 0",
    RfIconKind.QR to "M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h2v2h-2zM18 14h2v2h-2zM14 18h2v2h-2zM18 18h2v2h-2z",
    RfIconKind.SHARE to "M15 6a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0M8.6 10.6l6.8-3.6M8.6 13.4l6.8 3.6",
)

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
    val pathString = iconPaths[kind]
    if (pathString != null) {
        val path = remember(pathString) { PathParser().parsePathString(pathString).toPath() }
        Canvas(modifier = modifier.size(size)) {
            val s = this.size.width / 24f
            val stroke = Stroke(width = (24f * strokeWidthFactor).coerceAtLeast(1.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)
            scale(s, s, Offset.Zero) {
                if (filled) {
                    drawPath(path, color = tint)
                } else {
                    drawPath(path, color = tint, style = stroke)
                }
            }
        }
    } else {
        Canvas(modifier = modifier.size(size)) {
            drawCircle(color = tint, radius = this.size.width * 0.4f)
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
