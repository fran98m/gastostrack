package com.franm.gastosmama.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franm.gastosmama.R
import com.franm.gastosmama.data.Expense
import com.franm.gastosmama.ui.Config
import com.franm.gastosmama.ui.theme.Archivo
import com.franm.gastosmama.ui.theme.BackButton
import com.franm.gastosmama.ui.theme.Kicker
import com.franm.gastosmama.ui.theme.Modernist
import com.franm.gastosmama.ui.theme.SoftRule
import com.franm.gastosmama.ui.theme.StrongRule
import com.franm.gastosmama.util.formatCents
import com.franm.gastosmama.util.formatDayMonth

@Composable
fun StatementScreen(
    expenses: List<Expense>,
    message: String,
    fileName: String,
    shareOpen: Boolean,
    onBack: () -> Unit,
    onOpenShare: () -> Unit,
    onCloseShare: () -> Unit,
    onSendWhatsApp: () -> Unit,
) {
    val sorted = expenses.sortedByDescending { it.ts }
    val total = expenses.sumOf { it.amountCents }
    val oldest = expenses.minOfOrNull { it.ts } ?: System.currentTimeMillis()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(top = 8.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Text(
                    "MENSAJE PARA ${Config.Recipient.uppercase()}", fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp, color = Modernist.MutedInk,
                )
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
                Box(Modifier.fillMaxWidth().background(Modernist.Surface).padding(20.dp)) {
                    Text(message, fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 35.sp, color = Modernist.Ink)
                }

                Spacer(Modifier.height(20.dp))
                Kicker("Adjunto")
                Spacer(Modifier.height(8.dp))

                Column(Modifier.fillMaxWidth().border(2.dp, Modernist.StrongDivider)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(painterResource(R.drawable.ic_cat_file_spreadsheet), null, Modifier.size(40.dp))
                        Column {
                            Text(fileName, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Modernist.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val countLabel = if (expenses.size == 1) "1 gasto · ${formatDayMonth(oldest)}" else "${expenses.size} gastos desde el ${formatDayMonth(oldest)}"
                            Text(countLabel, fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 20.sp, color = Modernist.MutedInk)
                        }
                    }
                    SoftRule()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FECHA", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Modernist.MutedInk)
                        Text("GASTO", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Modernist.MutedInk, modifier = Modifier.weight(1f).padding(start = 12.dp))
                        Text("MONTO", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Modernist.MutedInk)
                    }
                    sorted.forEach { e ->
                        SoftRule()
                        Row(
                            Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp).padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(formatDayMonth(e.ts), fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 22.sp, color = Modernist.MutedInk)
                            Text(e.label, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.Ink, modifier = Modifier.weight(1f).padding(start = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatCents(e.amountCents), fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Modernist.Ink)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).background(Modernist.Surface).padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Total", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.Ink)
                        Text(formatCents(total), fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Modernist.Ink)
                    }
                }

                Text(
                    "El mensaje va igual; el detalle viaja en el archivo. Al enviar quedan marcados como cobrados.",
                    fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 28.sp,
                    color = Modernist.SecondaryInk, modifier = Modifier.padding(top = 16.dp),
                )
            }

            Row(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 84.dp).background(Modernist.AccentRed)
                    .clickable(onClick = onOpenShare).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Enviar por WhatsApp", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = Modernist.Ground)
                Text("↗", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = Modernist.Ground)
            }
        }

        if (shareOpen) {
            Box(Modifier.fillMaxSize().background(Modernist.Scrim).clickable(onClick = onCloseShare), contentAlignment = Alignment.BottomCenter) {
                Column(Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color.White).padding(20.dp, 20.dp, 20.dp, 28.dp)) {
                    Text(
                        "COMPARTIR · TEXTO + $fileName", fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp, color = Modernist.MutedInk, modifier = Modifier.padding(bottom = 14.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        ShareOption(label = "WhatsApp", bg = androidx.compose.ui.graphics.Color(0xFF25D366), glyph = "W", enabled = true, onClick = onSendWhatsApp)
                        ShareOption(label = "Mensajes", bg = Modernist.StrongDivider, glyph = "", enabled = false, onClick = {})
                        ShareOption(label = "Copiar", bg = Modernist.StrongDivider, glyph = "", enabled = false, onClick = {})
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareOption(label: String, bg: androidx.compose.ui.graphics.Color, glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(84.dp).clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            if (glyph.isNotEmpty()) {
                Text(glyph, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = androidx.compose.ui.graphics.Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            color = Modernist.Ink.copy(alpha = if (enabled) 1f else 0.5f),
        )
    }
}
