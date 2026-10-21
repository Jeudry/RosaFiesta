package com.jeudry.rosafiesta.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.AdminTabBar
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.PersonalTabBar
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfField
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfIconButton
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfIconTile
import com.jeudry.rosafiesta.designsystem.RfIllo
import com.jeudry.rosafiesta.designsystem.RfIlloKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfOfflineBanner
import com.jeudry.rosafiesta.designsystem.RfPhoneFrame
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfSegmented
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RfToast
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize

/**
 * Vitrina de los componentes reutilizables del sistema de diseño (Secciones 2 y 3 de `entrega-de-diseno.md`).
 */
@Composable
internal fun DesignGallery(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    RfPhoneFrame(
        variant = RfThemeVariant.PERSONAL,
        screenCode = "DS",
        screenTitle = "Componentes del Sistema",
        bottomBar = {
            PersonalTabBar(activeTab = PersonalTab.ROSA, onSelectTab = {})
        },
        modifier = modifier,
    ) {
        val typo = LocalRfTypography.current
        val palette = LocalRfPalette.current

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(palette.bg)
                .padding(bottom = 4.dp),
        ) {
            if (onBack != null) {
                RfButton(
                    label = "← Volver al panel de desarrollo",
                    style = RfButtonStyle.SECONDARY,
                    onClick = onBack,
                )
            }

            // 3.1 Logo (siempre sobre círculo blanco) y Rosa Avatar (mujer con audífonos y micrófono)
            RfCard(padding = 10.dp) {
                BasicText("Logo, Rosa IA e Ilustraciones (globos)", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfLogo(size = RfLogoSize.MEDIUM)
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false, glow = true)
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true, glow = true)
                    RfIllo(kind = RfIlloKind.GLOBOS, size = 44.dp)
                }
            }

            // 3.2 Foto real + Texto gradiente + Chips y Tags
            RfCard(padding = 10.dp) {
                BasicText("Fotografía real, Precio Gradiente, Chips y Tags", style = typo.cardTitle)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RfPhoto(
                        photoKey = RfPhotoKey.SILLA_TIFFANY,
                        contentDescription = "Silla Tiffany dorada",
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            RfGradientPriceText("RD$ 4.00")
                            RfTag(text = "Anticipo 30 %", tone = RfTagTone.SOL)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfChip(label = "Mobiliario", selected = true, onClick = {})
                            RfChip(label = "Flores", selected = false, onClick = {})
                        }
                    }
                }
            }

            // 2.5 Botón icono (44dp), Mosaico de icono (40×40dp), Interruptor (46×28dp), Campo y Segmentado
            RfCard(padding = 10.dp) {
                BasicText("Botón Icono, Mosaico, Switch, Campo y Segmentado", style = typo.cardTitle)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIconButton(icon = RfIconKind.BELL, contentDescription = "Notificaciones")
                    RfIconTile(icon = RfIconKind.SPARKLE, backgroundColor = RfColors.RosaTint, iconTint = RfColors.RosaTintInk)
                    RfSwitch(checked = true)
                }
                Spacer(Modifier.height(6.dp))
                RfSegmented(
                    options = listOf("Personal", "Admin", "Oscuro"),
                    selectedIndex = 0,
                    onSelect = {},
                )
                Spacer(Modifier.height(6.dp))
                RfField(
                    label = "Evento",
                    value = "Boda Real Rosa & Dorado · Vie 23 oct",
                )
            }

            // 3.3 AdminBar + Toast y Banner sin conexión
            RfOfflineBanner(pendingChangesCount = 3, isAdmin = true)
            RfToast(message = "Reserva confirmada · Anticipo RD$ 1,350", actionLabel = "Ver")
            AdminTabBar(activeTab = AdminTab.EVENTOS, onSelectTab = {})
        }
    }
}
