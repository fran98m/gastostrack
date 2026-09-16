package com.franm.gastosmama.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franm.gastosmama.data.CategoryTile
import com.franm.gastosmama.data.Expense
import com.franm.gastosmama.ui.theme.Archivo
import com.franm.gastosmama.ui.theme.BackButton
import com.franm.gastosmama.ui.theme.ConfirmSheet
import com.franm.gastosmama.ui.theme.HeavyRule
import com.franm.gastosmama.ui.theme.Modernist
import com.franm.gastosmama.ui.theme.StrongRule
import com.franm.gastosmama.util.formatCents

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryScreen(
    amountCents: Long,
    tiles: List<CategoryTile>,
    selectedLabel: String?,
    otherMode: Boolean,
    otherText: String,
    suggestions: List<String>,
    saveLabel: String,
    saveEnabled: Boolean,
    /** Edit flow: the original expense while the "Guardar cambio" sheet is up, else null. */
    editConfirm: Expense?,
    newLabel: String,
    onBack: () -> Unit,
    onBackFromOther: () -> Unit,
    onSelect: (String) -> Unit,
    onOpenOther: () -> Unit,
    onOtherTextChange: (String) -> Unit,
    onPickSuggestion: (String) -> Unit,
    onSave: () -> Unit,
    onConfirmEditYes: () -> Unit,
    onConfirmEditNo: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(top = 8.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(if (otherMode) onBackFromOther else onBack)
            Text(
                "¿EN QUÉ?", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 22.sp,
                color = Modernist.MutedInk, modifier = Modifier.weight(1f),
            )
            Text(
                "$ ${formatCents(amountCents)}", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp, letterSpacing = (-0.88).sp, color = Modernist.Ink, modifier = Modifier.padding(end = 12.dp),
                maxLines = 1, softWrap = false,
            )
        }
        HeavyRule()

        if (otherMode) {
            val focusRequester = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) { focusRequester.requestFocus(); keyboard?.show() }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Box(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 72.dp)
                        .background(Modernist.Surface).border(2.dp, Modernist.AccentRed)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (otherText.isEmpty()) {
                        Text(
                            "Escribe el gasto", fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
                            fontSize = 30.sp, color = Modernist.MutedInk,
                        )
                    }
                    BasicTextField(
                        value = otherText,
                        onValueChange = onOtherTextChange,
                        textStyle = TextStyle(
                            fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, color = Modernist.Ink,
                        ),
                        cursorBrush = SolidColor(Modernist.AccentRed),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    )
                }
                FlowRow(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { s ->
                        Box(
                            Modifier.defaultMinSize(minHeight = 56.dp).border(2.dp, Modernist.StrongDivider)
                                .clickable { onPickSuggestion(s) }.padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(s, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Modernist.Ink)
                        }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(tiles, key = { it.label }) { tile ->
                    CategoryRow(
                        tile = tile,
                        selected = tile.label == selectedLabel,
                        onClick = { if (tile.isOther) onOpenOther() else onSelect(tile.label) },
                    )
                    StrongRule()
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 96.dp)
                .background(Modernist.Ink.copy(alpha = if (saveEnabled) 1f else 0.45f))
                .clickable(enabled = saveEnabled, onClick = onSave)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(saveLabel, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = Modernist.Ground)
            Text("✓", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = Modernist.Ground)
        }
    }
    if (editConfirm != null) {
        EditConfirmSheet(
            before = "${editConfirm.label} $ ${formatCents(editConfirm.amountCents)}",
            after = "$newLabel $ ${formatCents(amountCents)}",
            onYes = onConfirmEditYes, onNo = onConfirmEditNo,
        )
    }
    }
}

/** "Guardar cambio" confirmation: shows the row as it is and as it will be. */
@Composable
private fun EditConfirmSheet(before: String, after: String, onYes: () -> Unit, onNo: () -> Unit) {
    ConfirmSheet(yesLabel = "Sí, guardar", noLabel = "No", onYes = onYes, onNo = onNo) {
        Text("¿Corregir?", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Modernist.MutedInk)
        Text(
            before, fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Modernist.MutedInk,
            textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            "→ $after", fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp,
            letterSpacing = (-0.56).sp, color = Modernist.Ink, modifier = Modifier.padding(bottom = 20.dp),
        )
    }
}

@Composable
private fun CategoryRow(tile: CategoryTile, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(96.dp).background(if (selected) Modernist.RedTint else Modernist.Ground)
            .clickable(onClick = onClick),
    ) {
        if (selected) {
            Box(Modifier.width(8.dp).fillMaxHeight().align(Alignment.CenterStart).background(Modernist.AccentRed))
        }
        Row(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(tile.iconRes),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                colorFilter = ColorFilter.tint(if (tile.isOther) Modernist.MutedInk else Modernist.Ink),
            )
            Text(
                tile.label, fontFamily = Archivo,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 32.sp, color = if (tile.isOther) Modernist.MutedInk else Modernist.Ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
