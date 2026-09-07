package com.franm.gastosmama.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KeypadTest {

    @Test fun `digits accumulate`() {
        var s = ""
        for (k in listOf("4", "6", "3", "0")) s = applyKey(s, k)
        assertEquals("4630", s)
    }

    @Test fun `leading zero is replaced not prefixed`() {
        assertEquals("5", applyKey("0", "5"))
    }

    @Test fun `dot on empty gives 0 dot`() {
        assertEquals("0.", applyKey("", "."))
    }

    @Test fun `second dot is ignored`() {
        assertEquals("1.5", applyKey("1.5", "."))
    }

    @Test fun `fractional part capped at two digits`() {
        assertEquals("1.50", applyKey("1.50", "0"))
    }

    @Test fun `integer part capped at six digits`() {
        assertEquals("123456", applyKey("123456", "7"))
    }

    @Test fun `del removes last char`() {
        assertEquals("12.5", applyKey("12.50", "del"))
    }

    @Test fun `amountTextToCents parses whole and fractional parts`() {
        assertEquals(12395L, amountTextToCents("123.95"))
        assertEquals(0L, amountTextToCents(""))
        assertEquals(0L, amountTextToCents("0."))
        assertEquals(500L, amountTextToCents("5"))
        assertEquals(150L, amountTextToCents("1.5"))
    }

    @Test fun `centsToPlainString round-trips amountTextToCents`() {
        assertEquals("46.30", centsToPlainString(4630L))
        assertEquals(4630L, amountTextToCents(centsToPlainString(4630L)))
    }
}
