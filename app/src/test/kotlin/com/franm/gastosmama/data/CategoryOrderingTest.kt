package com.franm.gastosmama.data

import com.franm.gastosmama.data.icons.OTHER_LABEL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOrderingTest {

    @Test fun `fresh install keeps seed order (all tied at baseline)`() {
        val labels = categoryTiles(emptyMap()).map { it.label }
        assertEquals(
            listOf("Frutera", "Pan", "Supermaxi", "AlgoMarket", "Rappi", "Química", "Peluquería", "Médico", "Podólogo", OTHER_LABEL),
            labels,
        )
    }

    @Test fun `real usage reorders descending, ties keep seed order`() {
        // Baseline +1 already applied by the caller (ExpenseRepository.usageCounts);
        // here we simulate "Rappi" having been used a lot more than everything else.
        val usage = mapOf(
            "Frutera" to 2, "Pan" to 2, "Supermaxi" to 2, "AlgoMarket" to 2,
            "Rappi" to 9, "Química" to 2, "Peluquería" to 2, "Médico" to 2, "Podólogo" to 2,
        )
        val labels = categoryTiles(usage).map { it.label }
        assertEquals("Rappi", labels.first())
        assertEquals(listOf("Frutera", "Pan", "Supermaxi", "AlgoMarket"), labels.subList(1, 5))
        assertEquals(OTHER_LABEL, labels.last())
    }

    @Test fun `custom label promoted once used twice, otherwise absent`() {
        val usedOnce = categoryTiles(mapOf("Ferretería" to 1)).map { it.label }
        assertTrue("Ferretería" !in usedOnce)

        val usedTwice = categoryTiles(mapOf("Ferretería" to 2)).map { it.label }
        assertTrue("Ferretería" in usedTwice)
        assertEquals(OTHER_LABEL, usedTwice.last()) // "Otro…" always stays last
    }
}
