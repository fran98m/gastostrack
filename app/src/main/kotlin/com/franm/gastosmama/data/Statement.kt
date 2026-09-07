package com.franm.gastosmama.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** An immutable record of one "sent to Enrique" batch. */
@Entity(tableName = "statement")
data class Statement(
    @PrimaryKey val id: String,
    val fromTs: Long,
    val toTs: Long,
    val sentTs: Long,
    val totalCents: Long,
    val count: Int,
)
