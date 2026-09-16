package com.franm.gastosmama.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franm.gastosmama.data.Expense
import com.franm.gastosmama.data.icons.iconForLabel
import com.franm.gastosmama.ui.theme.Archivo
import com.franm.gastosmama.ui.theme.ConfirmSheet
import com.franm.gastosmama.ui.theme.HeavyRule
import com.franm.gastosmama.ui.theme.Modernist
import com.franm.gastosmama.ui.theme.StrongRule
import com.franm.gastosmama.util.currentMonthAbbrev
import com.franm.gastosmama.util.dayKey
import com.franm.gastosmama.util.formatCents
import com.franm.gastosmama.util.formatCentsCompact
import com.franm.gastosmama.util.formatDayMonth

@Composable
fun HomeScreen(
    unclaimed: List<Expense>,
    deleteConfirmId: String?,
    onGoHistory: () -> Unit,
    onGoAdd: () -> Unit,
    onGoStatement: () -> Unit,
    onEdit: (Expense) -> Unit,
    onRequestDelete: (Expense) -> Unit,
    onConfirmDeleteYes: () -> Unit,
    onConfirmDeleteNo: () -> Unit,
) {
    val sorted = remember(unclaimed) { unclaimed.sortedByDescending { it.ts } }
    val total = remember(unclaimed) { unclaimed.sumOf { it.amountCents } }
    val firstOfDay = remember(sorted) {
        val seen = mutableSetOf<String>()
        sorted.associate { e -> e.id to seen.add(dayKey(e.ts)) }
    }
    val confirming = deleteConfirmId?.let { id -> sorted.find { it.id == id } }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 52.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                currentMonthAbbrev().uppercase(),
                fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.AccentRed,
            )
            Box(
                Modifier.defaultMinSize(minHeight = 64.dp).clickable(onClick = onGoHistory),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text("Ya cobrado", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.PressedRed)
            }
        }
        StrongRule()

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (sorted.isEmpty()) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Nada pendiente.", fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp, color = Modernist.MutedInk,
                    )
                    Text(
                        "Agrega el próximo gasto cuando pase.", fontFamily = Archivo,
                        fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Modernist.MutedInk,
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(sorted, key = { it.id }) { expense ->
                        ExpenseRow(
                            expense = expense,
                            showDay = firstOfDay[expense.id] == true,
                            onEdit = { onEdit(expense) },
                            onRequestDelete = { onRequestDelete(expense) },
                        )
                    }
                }
            }
        }

        HeavyRule()
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            // The total is measured first (no weight) so it always gets its
            // full natural width and can never be clipped; the label yields
            // whatever space is left and ellipsizes instead.
            Text(
                text = "sin cobrar",
                fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Modernist.MutedInk,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            Text(
                text = formatCentsCompact(total), fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
                fontSize = 56.sp, letterSpacing = (-1.68).sp, color = Modernist.Ink,
                maxLines = 1, softWrap = false,
            )
        }
        StrongRule()
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier.weight(1f).height(96.dp).background(Modernist.AccentRed).clickable(onClick = onGoAdd),
                contentAlignment = Alignment.Center,
            ) {
                Text("+", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, color = Modernist.Ground)
            }
            val enabled = sorted.isNotEmpty()
            Box(
                Modifier.weight(1f).height(96.dp).background(Modernist.Ground)
                    .clickable(enabled = enabled, onClick = onGoStatement)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    "Enviar a\nEnrique →", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
                    lineHeight = 26.sp, color = Modernist.Ink.copy(alpha = if (enabled) 1f else 0.45f),
                )
            }
        }
    }
    if (confirming != null) {
        DeleteConfirmSheet(expense = confirming, onYes = onConfirmDeleteYes, onNo = onConfirmDeleteNo)
    }
    }
}

/**
 * Swipe-past-threshold confirmation, same pattern as the Amount screen's
 * confirm sheet: scrim + bottom card, "Sí" deletes, "No" (or scrim tap)
 * snaps the row back.
 */
@Composable
private fun DeleteConfirmSheet(expense: Expense, onYes: () -> Unit, onNo: () -> Unit) {
    ConfirmSheet(yesLabel = "Sí", noLabel = "No", onYes = onYes, onNo = onNo) {
        Text(
            "¿Borrar ${expense.label} $ ${formatCents(expense.amountCents)}?",
            fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp,
            letterSpacing = (-0.56).sp, color = Modernist.Ink,
            modifier = Modifier.padding(bottom = 20.dp),
        )
    }
}


private const val DELETE_THRESHOLD_DP = -140f

@Composable
private fun ExpenseRow(expense: Expense, showDay: Boolean, onEdit: () -> Unit, onRequestDelete: () -> Unit) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { DELETE_THRESHOLD_DP.dp.toPx() }
    val touchSlopPx = with(density) { 8.dp.toPx() }
    var offsetX by remember(expense.id) { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).background(Modernist.Ink).padding(end = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Borrar", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Modernist.Ground)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .graphicsLayer { translationX = offsetX }
                .background(Modernist.Ground)
                .pointerInput(expense.id) {
                    // detectHorizontalDragGestures only ever fires for an
                    // actual drag — a plain tap (down+up, no movement) never
                    // reaches onDragEnd, so tap-to-edit was unreachable.
                    // Track the gesture by hand instead, mirroring the
                    // design prototype's down/move/up handlers: a release
                    // with no meaningful movement is a tap.
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var moved = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (offsetX < thresholdPx) { offsetX = 0f; onRequestDelete() }
                                else if (!moved) onEdit()
                                else offsetX = 0f
                                break
                            }
                            val dx = change.positionChange().x
                            if (!moved && kotlin.math.abs(offsetX + dx) > touchSlopPx) moved = true
                            if (moved) {
                                change.consume()
                                offsetX = (offsetX + dx).coerceAtMost(0f)
                            }
                        }
                    }
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
                if (showDay) {
                    Text(
                        text = formatDayMonth(expense.ts).substringBefore(' '),
                        fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Modernist.AccentRed,
                    )
                }
            }
            Image(
                painter = painterResource(iconForLabel(expense.label)),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(Modernist.Ink),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                expense.label, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 26.sp,
                color = Modernist.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            Text(
                formatCents(expense.amountCents), fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = Modernist.Ink,
            )
        }
    }
    StrongRuleThin()
}

@Composable
private fun StrongRuleThin() = Spacer(Modifier.fillMaxWidth().height(2.dp).background(Modernist.RowDivider))
