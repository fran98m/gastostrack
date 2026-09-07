package com.franm.gastosmama.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test fun `formats cents with two decimals`() {
        assertEquals("2.50", formatCents(250L))
        assertEquals("0.00", formatCents(0L))
    }

    @Test fun `groups thousands en-US style`() {
        assertEquals("1,234.50", formatCents(123_450L))
        assertEquals("121.95", formatCents(12_195L))
        assertEquals("10,000.00", formatCents(1_000_000L))
    }

    @Test fun `compact total stays full precision under 1000`() {
        assertEquals("999.99", formatCentsCompact(99_999L))
    }

    @Test fun `compact total switches to K notation from 1000 up`() {
        assertEquals("1.0K", formatCentsCompact(100_000L))
        assertEquals("1.2K", formatCentsCompact(123_450L))
        assertEquals("15.0K", formatCentsCompact(1_500_000L))
    }

    @Test fun `expensesToCsv writes header rows and trailing total`() {
        val expenses = listOf(
            com.franm.gastosmama.data.Expense("e1", "Pan", 250L, isoMillis(2026, 9, 7)),
            com.franm.gastosmama.data.Expense("e2", "Frutera, con coma", 875L, isoMillis(2026, 9, 6)),
        )
        val csv = expensesToCsv(expenses, totalCents = 1125L)
        val lines = csv.trim().lines()
        assertEquals("fecha,gasto,monto", lines[0])
        assertEquals("2026-09-06,\"Frutera, con coma\",8.75", lines[1])
        assertEquals("2026-09-07,Pan,2.50", lines[2])
        assertEquals(",Total,11.25", lines[3])
    }

    private fun isoMillis(year: Int, month: Int, day: Int): Long =
        java.util.Calendar.getInstance().apply { set(year, month - 1, day, 10, 0, 0) }.timeInMillis
}
