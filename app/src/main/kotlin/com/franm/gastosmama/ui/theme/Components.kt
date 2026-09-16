package com.franm.gastosmama.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 64x64dp "←" back button — every non-home screen's header starts with one. */
@Composable
fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(64.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("←", fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, color = Modernist.Ink)
    }
}

@Composable
fun Kicker(text: String, color: Color = Modernist.MutedInk, fontSize: TextUnit = 22.sp) {
    Text(
        text = text.uppercase(),
        fontFamily = Archivo,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        letterSpacing = 0.04.sp * (fontSize.value / 22f),
        color = color,
    )
}

@Composable fun StrongRule() = Spacer(Modifier.fillMaxWidth().height(2.dp).background(Modernist.StrongDivider))

@Composable fun SoftRule() = Spacer(Modifier.fillMaxWidth().height(2.dp).background(Modernist.SoftDivider))

@Composable fun HeavyRule() = Spacer(Modifier.fillMaxWidth().height(4.dp).background(Modernist.HeavyRule))

/**
 * Full-screen scrim + bottom card with a red "yes" row and an outlined "no" row.
 * [header] is the question above the buttons. Scrim tap counts as "no".
 * Callers overlay it as the last child of a fillMaxSize Box.
 */
@Composable
fun ConfirmSheet(
    yesLabel: String,
    noLabel: String,
    onYes: () -> Unit,
    onNo: () -> Unit,
    header: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(Modernist.Scrim).clickable(onClick = onNo),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth().background(Modernist.Ground)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
        ) {
            header()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 76.dp).background(Modernist.AccentRed)
                        .clickable(onClick = onYes).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(yesLabel, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Modernist.Ground)
                }
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 76.dp)
                        .border(2.dp, Modernist.StrongDivider)
                        .clickable(onClick = onNo).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(noLabel, fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Modernist.Ink)
                }
            }
        }
    }
}

/** Bottom toast — ink background, ground text, auto-dismiss handled by the caller's state. */
@Composable
fun BoxWithToast(message: String?, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        content()
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(tween(250)) { it },
            exit = slideOutVertically(tween(180)) { it },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Modernist.Ink)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text(
                    text = message.orEmpty(),
                    fontFamily = Archivo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Modernist.Ground,
                )
            }
        }
    }
}
