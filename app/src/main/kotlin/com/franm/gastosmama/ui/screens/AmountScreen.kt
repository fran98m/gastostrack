package com.franm.gastosmama.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franm.gastosmama.ui.theme.Archivo
import com.franm.gastosmama.ui.theme.BackButton
import com.franm.gastosmama.ui.theme.Modernist
import com.franm.gastosmama.ui.theme.StrongRule
import com.franm.gastosmama.util.amountTextToCents

private val KEYS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "del")

@Composable
fun AmountScreen(
    amountText: String,
    isEditing: Boolean,
    confirmOpen: Boolean,
    thresholdCents: Long,
    onBack: () -> Unit,
    onKey: (String) -> Unit,
    onNext: () -> Unit,
    onConfirmYes: () -> Unit,
    onConfirmNo: () -> Unit,
) {
    val display = amountText.ifEmpty { "0" }
    val amountCents = amountTextToCents(amountText)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(top = 8.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Text(
                    if (isEditing) "CORREGIR MONTO" else "MONTO",
                    fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.MutedInk,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("$", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 48.sp, color = Modernist.MutedInk)
                Text(
                    display, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 84.sp,
                    letterSpacing = (-1.68).sp, color = Modernist.Ink, modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1, softWrap = false,
                )
                Box(Modifier.padding(start = 8.dp).width(4.dp).height(70.dp).background(Modernist.AccentRed))
            }
            StrongRule()

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f).fillMaxWidth().background(Modernist.StrongDivider),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(KEYS) { key ->
                    Box(
                        Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp).background(Modernist.Ground)
                            .clickable { onKey(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (key == "del") "⌫" else key, fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                            fontSize = 44.sp, color = Modernist.Ink,
                        )
                    }
                }
            }

            val nextEnabled = amountCents > 0
            Row(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 84.dp)
                    .background(Modernist.AccentRed.copy(alpha = if (nextEnabled) 1f else 0.45f))
                    .clickable(enabled = nextEnabled, onClick = onNext)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Siguiente", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Modernist.Ground)
                Text("→", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = Modernist.Ground)
            }
        }

        if (confirmOpen) {
            Box(
                Modifier.fillMaxSize().background(Modernist.Scrim).clickable(onClick = onConfirmNo),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    Modifier.fillMaxWidth().background(Modernist.Ground)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
                ) {
                    Text(
                        "Es más de $ ${thresholdCents / 100}", fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp, color = Modernist.MutedInk,
                    )
                    Text(
                        "¿Son $ $display?", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp,
                        letterSpacing = (-0.88).sp, color = Modernist.Ink, modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth().defaultMinSize(minHeight = 76.dp).background(Modernist.AccentRed)
                                .clickable(onClick = onConfirmYes).padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Sí, es correcto", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Modernist.Ground)
                        }
                        Row(
                            Modifier.fillMaxWidth().defaultMinSize(minHeight = 76.dp)
                                .border(2.dp, Modernist.StrongDivider)
                                .clickable(onClick = onConfirmNo).padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Corregir", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Modernist.Ink)
                        }
                    }
                }
            }
        }
    }
}
