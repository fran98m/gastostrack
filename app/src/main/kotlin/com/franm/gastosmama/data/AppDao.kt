package com.franm.gastosmama.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class LabelCount(val label: String, val count: Int)

@Dao
interface AppDao {
    @Query("SELECT * FROM expense WHERE statementId IS NULL AND deletedTs IS NULL ORDER BY ts DESC")
    fun unclaimedExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM statement ORDER BY sentTs DESC")
    fun statements(): Flow<List<Statement>>

    @Query("SELECT * FROM expense WHERE statementId = :statementId AND deletedTs IS NULL ORDER BY ts DESC")
    fun expensesForStatement(statementId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE statementId IS NOT NULL AND deletedTs IS NULL")
    fun claimedExpenses(): Flow<List<Expense>>

    /** Soft-deleted expenses, most recently deleted first — the Historial "Borrados" section. */
    @Query("SELECT * FROM expense WHERE deletedTs IS NOT NULL ORDER BY deletedTs DESC")
    fun deletedExpenses(): Flow<List<Expense>>

    /** Usage count per label across ALL expenses (claimed and unclaimed). */
    @Query("SELECT label, COUNT(*) as count FROM expense WHERE deletedTs IS NULL GROUP BY label")
    fun usageCounts(): Flow<List<LabelCount>>

    @Query("SELECT DISTINCT label FROM expense WHERE deletedTs IS NULL")
    fun allLabelsEverUsed(): Flow<List<String>>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    /** Soft delete: stamp the row instead of removing it so it can be recovered. */
    @Query("UPDATE expense SET deletedTs = :deletedTs WHERE id = :id")
    suspend fun softDeleteExpense(id: String, deletedTs: Long)

    /** Recovery: clear the stamp; the row reappears in its original list. */
    @Query("UPDATE expense SET deletedTs = NULL WHERE id = :id")
    suspend fun restoreExpense(id: String)

    @Query("UPDATE expense SET statementId = :statementId WHERE statementId IS NULL")
    suspend fun claimAllUnclaimed(statementId: String)

    @Insert
    suspend fun insertStatement(statement: Statement)

    /** Atomically stamps every unclaimed expense with the new statement id. */
    @Transaction
    suspend fun sendStatement(statement: Statement) {
        claimAllUnclaimed(statement.id)
        insertStatement(statement)
    }
}
