package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfCard
import com.jeudry.rosafiesta.designsystem.RfChip
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.domain.PhotoAsset

internal fun PhotoAsset.toPhotoKey(): RfPhotoKey = when (this) {
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

/**
 * Fila reutilizable de etiqueta + interruptor (`RfSwitch`).
 */
@Composable
internal fun RfPreferenceToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        BasicText(label, style = typo.bodyStrong, modifier = Modifier.weight(1f))
        RfSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Fila reutilizable de resumen clave-valor para cotizaciones, contratos y finanzas.
 */
@Composable
internal fun RfKeyValueSummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        BasicText(label, style = typo.body, modifier = Modifier.weight(1f))
        BasicText(value, style = typo.bodyStrong)
    }
}

/**
 * Selector de cantidad reutilizable con botones rápidos `-10` / `+10`.
 */
@Composable
internal fun RfQuantityStepperRow(
    label: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementLabel: String = "-10",
    incrementLabel: String = "+10",
) {
    val typo = LocalRfTypography.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicText(label, style = typo.bodyStrong)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RfChip(decrementLabel, selected = false, onClick = onDecrement)
            RfChip(incrementLabel, selected = true, onClick = onIncrement)
        }
    }
}

/**
 * Tarjeta reutilizable para secciones legales, privacidad, facturación fiscal y licencias (`s87`).
 */
@Composable
internal fun RfLegalClauseCard(
    badgeText: String,
    tone: RfTagTone,
    title: String,
    bullets: List<String>,
) {
    val typo = LocalRfTypography.current
    RfCard {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(title, style = typo.cardTitle, modifier = Modifier.weight(1f))
            RfTag(text = badgeText, tone = tone)
        }
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            bullets.forEach { line ->
                BasicText("• $line", style = typo.body)
            }
        }
    }
}
