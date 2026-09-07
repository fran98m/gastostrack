package com.franm.gastosmama.util

/**
 * Applies one keypad press to the typed amount string. Rules (design spec):
 * max 6 integer digits, one decimal point, max 2 decimal digits, typing a
 * digit over a lone "0" replaces it, "." on empty gives "0.".
 */
fun applyKey(current: String, key: String): String {
    if (key == "del") return current.dropLast(1)
    if (key == ".") return if (current.contains('.')) current else current.ifEmpty { "0" } + "."

    val dotIdx = current.indexOf('.')
    return if (dotIdx >= 0) {
        val frac = current.substring(dotIdx + 1)
        if (frac.length < 2) current + key else current
    } else {
        when {
            current.length < 6 && current != "0" -> current + key
            current == "0" -> key
            else -> current
        }
    }
}

/** "" or "0" or "0." all mean zero. */
fun amountTextToCents(text: String): Long {
    if (text.isBlank() || text == "0" || text == "0.") return 0L
    val parts = text.split(".", limit = 2)
    val whole = parts[0].ifEmpty { "0" }.toLongOrNull() ?: 0L
    val frac = (parts.getOrNull(1) ?: "").padEnd(2, '0').take(2).toLongOrNull() ?: 0L
    return whole * 100 + frac
}

/** Plain (no thousands grouping) decimal string for pre-filling the amount field on edit. */
fun centsToPlainString(cents: Long): String {
    val whole = cents / 100
    val frac = (cents % 100).toString().padStart(2, '0')
    return "$whole.$frac"
}
