package com.franm.gastosmama.util

import java.util.Calendar
import kotlin.math.abs

private val MESES = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
)
private val DIAS = listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb")

/** "121.95", "1,234.50" — en-US grouping, always 2 decimals, no currency symbol. */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val abs = abs(cents)
    val whole = abs / 100
    val frac = (abs % 100).toString().padStart(2, '0')
    val grouped = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    return "$sign$grouped.$frac"
}

/** "121.95" under $1,000, "1.2K" / "15.0K" from $1,000 up — keeps the home total from ever being wide enough to clip. */
fun formatCentsCompact(cents: Long): String {
    val absDollars = abs(cents) / 100.0
    if (absDollars < 1000.0) return formatCents(cents)
    val sign = if (cents < 0) "-" else ""
    val thousands = absDollars / 1000.0
    return "$sign${"%.1f".format(thousands)}K"
}

private fun calendarAt(ts: Long): Calendar = Calendar.getInstance().apply { timeInMillis = ts }

/** "7 sep" */
fun formatDayMonth(ts: Long): String {
    val cal = calendarAt(ts)
    return "${cal.get(Calendar.DAY_OF_MONTH)} ${MESES[cal.get(Calendar.MONTH)]}"
}

/** Groups expenses by calendar day, independent of time-of-day. */
fun dayKey(ts: Long): String {
    val cal = calendarAt(ts)
    return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
}

/** "mié 3 sep" — used for home-list day headers that aren't "Hoy"/"Ayer". */
fun weekdayDayMonth(ts: Long): String {
    val cal = calendarAt(ts)
    val dow = cal.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY == 1
    return "${DIAS[dow]} ${formatDayMonth(ts)}"
}

/** "2026-09-07" for the CSV export. */
fun isoDate(ts: Long): String {
    val cal = calendarAt(ts)
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
    )
}

fun currentMonthAbbrev(): String = MESES[Calendar.getInstance().get(Calendar.MONTH)]

/** "sep7" — month abbreviation immediately followed by day, for filenames (no spaces). */
private fun compactDayMonth(ts: Long): String {
    val cal = calendarAt(ts)
    return "${MESES[cal.get(Calendar.MONTH)]}${cal.get(Calendar.DAY_OF_MONTH)}"
}

/**
 * "gastos-mama-sep1-oct3-2026.csv" — reflects the statement's actual period
 * (oldest unclaimed expense → send time), not just "this month", so it's
 * correct whether the batch is same-day or crosses a month/year boundary.
 */
fun statementFileNameFor(fromTs: Long, toTs: Long): String {
    val year = calendarAt(toTs).get(Calendar.YEAR)
    return "gastos-mama-${compactDayMonth(fromTs)}-${compactDayMonth(toTs)}-$year.csv"
}

/** "Hoy" / "Ayer" / "mié 3 sep" — the home list's day-group header. */
fun dayGroupLabel(ts: Long, now: Long = System.currentTimeMillis()): String = when (dayKey(ts)) {
    dayKey(now) -> "Hoy"
    dayKey(now - 86_400_000L) -> "Ayer"
    else -> weekdayDayMonth(ts)
}
