package com.franm.gastosmama.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One logged expense. [statementId] is null while unclaimed; sending a
 * statement stamps every currently-unclaimed row with the new statement's id
 * in the same transaction that inserts it (see AppDao.sendStatement).
 *
 * [deletedTs] is the soft-delete stamp: null while the expense is live, the
 * delete time (epoch millis) once swiped away. Every "live" query filters on
 * `deletedTs IS NULL`; the Historial "Borrados" section reads the rest and
 * "Recuperar" stamps the column back to null.
 */
@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey val id: String,
    val label: String,
    val amountCents: Long,
    val ts: Long,
    val statementId: String? = null,
    val deletedTs: Long? = null,
)
