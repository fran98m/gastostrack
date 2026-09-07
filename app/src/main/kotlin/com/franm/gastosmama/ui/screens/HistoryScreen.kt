package com.franm.gastosmama.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franm.gastosmama.data.Expense
import com.franm.gastosmama.data.Statement
import com.franm.gastosmama.ui.theme.Archivo
import com.franm.gastosmama.ui.theme.BackButton
import com.franm.gastosmama.ui.theme.Modernist
import com.franm.gastosmama.ui.theme.StrongRule
import com.franm.gastosmama.util.formatCents
import com.franm.gastosmama.util.formatDayMonth
import java.util.Calendar

@Composable
fun HistoryScreen(
    statements: List<Statement>,
    claimedByStatement: Map<String, List<Expense>>,
    openId: String?,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(top = 8.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("YA COBRADO", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.MutedInk)
        }
        StrongRule()
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(statements, key = { it.id }) { statement ->
                val items = claimedByStatement[statement.id].orEmpty().sortedByDescending { it.ts }
                Column(
                    Modifier.fillMaxWidth().clickable { onToggle(statement.id) }.padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    val year = Calendar.getInstance().apply { timeInMillis = statement.toTs }.get(Calendar.YEAR)
                    Text(
                        "${formatDayMonth(statement.fromTs)} – ${formatDayMonth(statement.toTs)} $year",
                        fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.MutedInk,
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$ ${formatCents(statement.totalCents)}", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp, letterSpacing = (-0.72).sp, color = Modernist.Ink,
                            maxLines = 1, softWrap = false,
                        )
                        Text("${statement.count} gastos", fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 22.sp, color = Modernist.SecondaryInk)
                    }
                }
                StrongRule()
                if (openId == statement.id) {
                    Column(Modifier.fillMaxWidth().background(Modernist.Surface).padding(horizontal = 20.dp, vertical = 8.dp)) {
                        items.forEach { e ->
                            Row(
                                Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Text(formatDayMonth(e.ts), fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 22.sp, color = Modernist.MutedInk)
                                Text(e.label, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.Ink, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                                Text(formatCents(e.amountCents), fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Modernist.Ink)
                            }
                        }
                    }
                    StrongRule()
                }
            }
        }
    }
}
