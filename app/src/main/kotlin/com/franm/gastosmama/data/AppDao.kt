package com.franm.gastosmama.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class LabelCount(val label: String, val count: Int)

@Dao
interface AppDao {
    @Query("SELECT * FROM expense WHERE statementId IS NULL ORDER BY ts DESC")
    fun unclaimedExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM statement ORDER BY sentTs DESC")
    fun statements(): Flow<List<Statement>>

    @Query("SELECT * FROM expense WHERE statementId = :statementId ORDER BY ts DESC")
    fun expensesForStatement(statementId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE statementId IS NOT NULL")
    fun claimedExpenses(): Flow<List<Expense>>

    /** Usage count per label across ALL expenses (claimed and unclaimed). */
    @Query("SELECT label, COUNT(*) as count FROM expense GROUP BY label")
    fun usageCounts(): Flow<List<LabelCount>>

    @Query("SELECT DISTINCT label FROM expense")
    fun allLabelsEverUsed(): Flow<List<String>>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

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
