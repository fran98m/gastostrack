package com.franm.gastosmama.data

import com.franm.gastosmama.data.icons.OTHER_ICON
import com.franm.gastosmama.data.icons.OTHER_LABEL
import com.franm.gastosmama.data.icons.SEED_CATEGORY_ICONS
import com.franm.gastosmama.data.icons.iconForLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class CategoryTile(val label: String, val iconRes: Int, val isOther: Boolean = false)

/**
 * Category picker rows, most-used first (ties keep seed order — this is the
 * "descending by most-used category" requirement the Categoría screen
 * renders). A custom label (typed via "Otro…") is promoted into the list
 * once it's been used twice. "Otro…" itself is always last.
 *
 * Pure function (no DB access) so it's directly unit-testable — see
 * CategoryOrderingTest.
 */
fun categoryTiles(usage: Map<String, Int>): List<CategoryTile> {
    val seedLabels = SEED_CATEGORY_ICONS.map { it.first }.toSet()
    val customLabels = usage.keys.filter { it !in seedLabels && (usage[it] ?: 0) >= 2 }
    val ordered = (seedLabels + customLabels).sortedByDescending { usage[it] ?: 0 }
    return ordered.map { label -> CategoryTile(label, iconForLabel(label)) } +
        CategoryTile(OTHER_LABEL, OTHER_ICON, isOther = true)
}

class ExpenseRepository(private val dao: AppDao) {

    fun unclaimedExpenses(): Flow<List<Expense>> = dao.unclaimedExpenses()

    fun statements(): Flow<List<Statement>> = dao.statements()

    fun expensesForStatement(statementId: String): Flow<List<Expense>> =
        dao.expensesForStatement(statementId)

    /** Claimed expenses grouped by statement id, for the expandable Historial rows. */
    fun claimedByStatement(): Flow<Map<String, List<Expense>>> =
        dao.claimedExpenses().map { all -> all.filter { it.statementId != null }.groupBy { it.statementId!! } }

    /** Raw usage counts with the seed +1 baseline applied (see CategoryIcons.kt). */
    fun usageCounts(): Flow<Map<String, Int>> = dao.usageCounts().map { rows ->
        val counts = rows.associate { it.label to it.count }.toMutableMap()
        SEED_CATEGORY_ICONS.forEach { (label, _) -> counts[label] = (counts[label] ?: 0) + 1 }
        counts
    }

    fun categoryTiles(usage: Map<String, Int>): List<CategoryTile> = com.franm.gastosmama.data.categoryTiles(usage)

    /** Distinct labels ever used that aren't seed categories, for the "Otro…" autocomplete. */
    fun customLabelHistory(): Flow<List<String>> {
        val seedLabels = SEED_CATEGORY_ICONS.map { it.first }.toSet()
        return dao.allLabelsEverUsed().map { it.filter { l -> l !in seedLabels } }
    }

    suspend fun addExpense(id: String, label: String, amountCents: Long, ts: Long) {
        dao.insertExpense(Expense(id = id, label = label, amountCents = amountCents, ts = ts))
    }

    suspend fun updateExpense(expense: Expense, label: String, amountCents: Long) {
        dao.updateExpense(expense.copy(label = label, amountCents = amountCents))
    }

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    suspend fun sendStatement(id: String, expenses: List<Expense>, sentTs: Long) {
        val statement = Statement(
            id = id,
            fromTs = expenses.minOf { it.ts },
            toTs = sentTs,
            sentTs = sentTs,
            totalCents = expenses.sumOf { it.amountCents },
            count = expenses.size,
        )
        dao.sendStatement(statement)
    }
}
