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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind

private val PersonalButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFE6007A)),
)

// ==============================================================================
// s79 · NOTIFICACIONES PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalNotificationsS79(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    BasicText("Notificaciones", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1E24)))
                    BasicText("3 avisos recientes", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val notifs = listOf(
                Triple("¡Tu cotización está lista!", "Boda Real Rosa & Dorado · Revisa los detalles", "Hoy"),
                Triple("Recordatorio de pago", "Abono del 30% vence en 48 horas", "Ayer"),
                Triple("Rosa IA tiene sugerencias", "Nuevos centros florales para tu paleta", "Hace 2 días"),
            )

            notifs.forEach { (title, subtitle, time) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFFF3366), fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s80 · BÚSQUEDA
// ==============================================================================
@Composable
internal fun RenderPersonalSearchS80(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Buscar en Catálogo", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicText("Resultados para: “mesa”", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1E1E24), fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val results = listOf(
                Triple("Mesa Redonda 10 personas", "Para 8 a 10 personas · 1.5 m", "RD$ 25 c/u"),
                Triple("Centro de Mesa Floral", "Flores naturales con base de madera", "RD$ 35 c/u"),
                Triple("Mesa Plegable Rectangular", "Ideal para buffet y catering", "RD$ 45 c/u"),
            )

            results.forEach { (name, desc, pr) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s19") }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        BasicText(pr, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFFF3366), fontSize = 15.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s82 · MIS PAGOS
// ==============================================================================
@Composable
internal fun RenderPersonalPaymentsS82(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Mis Pagos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Total Pagado", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        BasicText("RD$ 3,210", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2FB58F), fontSize = 18.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Saldo Pendiente", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        BasicText("RD$ 7,490", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText("Historial de Recibos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))

            Spacer(modifier = Modifier.height(10.dp))

            val receipts = listOf(
                Pair("Anticipo 30% · Boda Real Rosa", "RD$ 3,210 · 18 oct · Tarjeta Visa"),
            )

            receipts.forEach { (title, subtitle) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s83") }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s86 · AJUSTES DE CUENTA
// ==============================================================================
@Composable
internal fun RenderAccountSettingsS86(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Ajustes", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val options = listOf(
                Pair("Términos y Privacidad", "s87"),
                Pair("Modo Oscuro / Claro", "s15"),
                Pair("Eliminar Cuenta", "s88"),
            )

            options.forEach { (lbl, screen) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screen) }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText(lbl, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                        RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s87 · TÉRMINOS Y PRIVACIDAD
// ==============================================================================
@Composable
internal fun RenderTermsS87(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s86") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Términos y Privacidad", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText("Políticas de Reserva", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))
                    BasicText("Tu fecha queda apartada cuando Rosa Fiesta confirma la cotización y recibe el anticipo del 30%. Las cotizaciones tienen validez de 7 días.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    BasicText("El saldo restante se liquida a más tardar un día antes del montaje.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s88 · ELIMINAR CUENTA
// ==============================================================================
@Composable
internal fun RenderDeleteAccountS88(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFDC2626), size = 32.dp)
            }

            BasicText("¿Eliminar tu cuenta?", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
            BasicText("Esta acción es permanente y eliminará tu historial de eventos y favoritos.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDC2626))
                    .clickable { onSelectScreenCode("s07") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Eliminar Cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s86") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Mantener mi cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// s89 · ESTADO VACÍO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalEmptyStateS89(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE3EC)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFFF3366), size = 32.dp)
            }

            BasicText("Aún no tienes eventos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
            BasicText("Cuéntale a Rosa qué celebras y te armamos la lista en 2 minutos.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PersonalButtonGradient)
                    .clickable { onSelectScreenCode("s26") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Planificar con Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// s90 · ESTADO CARGANDO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalLoadingStateS90(
    onSelectScreenCode: (String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE3EC)))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp)),
                )
            }
        }
    }
}

// ==============================================================================
// s91 · ESTADO SIN CONEXIÓN PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalOfflineStateS91(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color(0xFFDC2626), size = 32.dp)
            }

            BasicText("No hay conexión", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
            BasicText("Revisa tu internet. Tu lista de Mi evento está guardada y la enviaremos cuando vuelvas a conectarte.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PersonalButtonGradient)
                    .clickable { onSelectScreenCode("s24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Ver mis favoritos guardados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// s92 · ESTADO ÉXITO PERSONAL (CON TOAST)
// ==============================================================================
@Composable
internal fun RenderPersonalSuccessStateS92(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF059669))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    BasicText("Guardado en tus favoritos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp))
                    BasicText("Ver", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Silla Tiffany Cristal", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E24)))
                    BasicText("Agregada con éxito a tu lista de favoritos.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PersonalButtonGradient)
                    .clickable { onSelectScreenCode("s18") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Volver al Catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
