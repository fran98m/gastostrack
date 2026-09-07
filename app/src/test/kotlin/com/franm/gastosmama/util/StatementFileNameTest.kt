package com.franm.gastosmama.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

/** Simulates the exact scenario asked about: does the filename track the real period, across month/year boundaries? */
class StatementFileNameTest {

    private fun ts(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply { set(year, month - 1, day, 9, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

    @Test fun `same-day statement`() {
        val d = ts(2026, 9, 7)
        assertEquals("gastos-mama-sep7-sep7-2026.csv", statementFileNameFor(d, d))
    }

    @Test fun `spans within september`() {
        assertEquals(
            "gastos-mama-sep1-sep20-2026.csv",
            statementFileNameFor(ts(2026, 9, 1), ts(2026, 9, 20)),
        )
    }

    @Test fun `crosses september into october`() {
        assertEquals(
            "gastos-mama-sep28-oct3-2026.csv",
            statementFileNameFor(ts(2026, 9, 28), ts(2026, 10, 3)),
        )
    }

    @Test fun `crosses october into november`() {
        assertEquals(
            "gastos-mama-oct30-nov2-2026.csv",
            statementFileNameFor(ts(2026, 10, 30), ts(2026, 11, 2)),
        )
    }

    @Test fun `crosses a year boundary and uses the send-date year`() {
        assertEquals(
            "gastos-mama-dic30-ene3-2027.csv",
            statementFileNameFor(ts(2026, 12, 30), ts(2027, 1, 3)),
        )
    }
}
