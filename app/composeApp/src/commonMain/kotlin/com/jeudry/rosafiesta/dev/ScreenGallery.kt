package com.jeudry.rosafiesta.dev

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.ui.RenderScreenSpec
import com.jeudry.rosafiesta.ui.ScreenCatalog

/**
 * Todos los 133 tableros del sistema de diseño agrupados por las 17 secciones de `entrega-de-diseno.md`.
 */
internal val BoardGroups: List<Pair<String, List<Board>>> =
    ScreenCatalog.allScreens
        .groupBy { "Sección ${it.sectionNumber} · ${it.sectionTitle}" }
        .map { (groupTitle, specs) ->
            groupTitle to specs.map { spec ->
                Board(
                    code = spec.code,
                    name = "${spec.code} — ${spec.title}",
                    isDesktop = spec.isDesktopWeb,
                    content = { RenderScreenSpec(spec = spec) },
                )
            }
        }

/**
 * Tableros canónicos verificados píxel a píxel por las pruebas de capturas (`GalleryScreenshotTest`).
 * Cubren las 17 secciones del catálogo, los 3 modos visuales (`theme.personal`, oscuro `s15` y `theme.admin`) y la vista web `A39`.
 */
internal val GoldenScreenshotBoards: List<Board> = listOf(
    "p00",
    "s01",
    "s07",
    "s14",
    "s15",
    "s18",
    "s27",
    "s28",
    "s33",
    "s37",
    "s40",
    "s46",
    "s50",
    "s88",
    "s79",
    "A01",
    "s55",
    "A03",
    "A05",
    "s68",
    "A16",
    "s69",
    "A28",
    "A30",
    "A37",
    "A39",
).map { code ->
    val spec = ScreenCatalog.findByCode(code)
    Board(
        code = spec.code,
        name = "${spec.code}_${spec.title}",
        isDesktop = spec.isDesktopWeb,
        content = { RenderScreenSpec(spec = spec) },
    )
}

/**
 * Galería interactiva de las 133 pantallas del modo desarrollador.
 */
@Composable
internal fun ScreenGallery(onBack: () -> Unit) {
    var openCode by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedBoard = openCode?.let { code ->
        BoardGroups.flatMap { it.second }.firstOrNull { it.code == code }
    }

    if (selectedBoard != null) {
        RfTheme(variant = RfThemeVariant.PERSONAL) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalRfPalette.current.bg)
                    .padding(12.dp),
            ) {
                RfButton(
                    label = "← Volver a las 133 pantallas (${selectedBoard.name})",
                    style = RfButtonStyle.SECONDARY,
                    onClick = { openCode = null },
                )
                Spacer(Modifier.height(8.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    selectedBoard.content()
                }
            }
        }
        return
    }

    RfTheme(variant = RfThemeVariant.PERSONAL) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bg)
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = "Galería de 133 pantallas",
                    style = typo.screenTitle,
                )
                RfButton(
                    label = "Cerrar",
                    style = RfButtonStyle.SECONDARY,
                    fullWidth = false,
                    onClick = onBack,
                )
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                BoardGroups.forEach { (groupTitle, boards) ->
                    item {
                        BasicText(
                            text = "$groupTitle (${boards.size})",
                            style = typo.sectionTitle,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(boards, key = { it.code }) { board ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.surface)
                                .clickable { openCode = board.code }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            BasicText(board.name, style = typo.bodyStrong)
                            RfTag(
                                text = board.code,
                                tone = if (board.code.startsWith("A") || board.code in setOf("s55", "s56", "s57", "s58", "s59", "s60", "s61", "s62", "s63", "s64", "s65", "s66", "s67", "s68", "s69", "s70", "s71", "s72", "s73", "s74")) {
                                    RfTagTone.INDIGO
                                } else {
                                    RfTagTone.ROSA
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
