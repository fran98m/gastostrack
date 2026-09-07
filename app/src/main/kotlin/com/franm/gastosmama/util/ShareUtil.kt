package com.franm.gastosmama.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.franm.gastosmama.data.Expense
import java.io.File

private const val WHATSAPP_PACKAGE = "com.whatsapp"

/** `fecha,gasto,monto` rows oldest→newest, plus a trailing total row — see README §Resumen. */
fun expensesToCsv(expenses: List<Expense>, totalCents: Long): String {
    val sb = StringBuilder("fecha,gasto,monto\n")
    expenses.sortedBy { it.ts }.forEach { e ->
        val needsQuote = e.label.contains(',') || e.label.contains('"')
        val label = if (needsQuote) "\"${e.label.replace("\"", "\"\"")}\"" else e.label
        sb.append("${isoDate(e.ts)},$label,${formatCents(e.amountCents)}\n")
    }
    sb.append(",Total,${formatCents(totalCents)}\n")
    return sb.toString()
}

fun writeCsvToCache(context: Context, fileName: String, content: String): File {
    val dir = File(context.cacheDir, "statements").apply { mkdirs() }
    val file = File(dir, fileName)
    file.writeText(content, Charsets.UTF_8)
    return file
}

/**
 * Shares [message] and [csvFile] together in a single ACTION_SEND intent —
 * WhatsApp gets both the text and the CSV attachment in one share, not two.
 * Targets WhatsApp directly when installed, otherwise falls back to the
 * system chooser.
 *
 * MIME type is a wildcard (any/any), not "text/csv": WhatsApp routes a
 * specific type like text/csv to its narrower "share a document" activity,
 * which drops EXTRA_TEXT once a stream is attached — the caption field
 * simply isn't shown. Its generic wildcard share target keeps both the
 * file and the caption. If that still doesn't resolve on a given WhatsApp
 * version, we fall back to the system chooser rather than crash.
 *
 * The message is also copied to the clipboard first as a last-resort paste
 * option, since there's no reliable way to detect after the fact whether a
 * given WhatsApp build dropped the caption anyway.
 */
fun shareStatement(context: Context, message: String, csvFile: File) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("Mensaje para Enrique", message))

    val uri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", csvFile,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val whatsappInstalled = runCatching {
        context.packageManager.getPackageInfo(WHATSAPP_PACKAGE, 0)
    }.isSuccess

    if (whatsappInstalled) {
        intent.setPackage(WHATSAPP_PACKAGE)
        runCatching { context.startActivity(intent) }.onFailure {
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, null))
        }
    } else {
        context.startActivity(Intent.createChooser(intent, null))
    }
}
