package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "cleaning_history",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class CleaningHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clientId: String,
    val barcodeId: String,
    val cleaningDate: Long = System.currentTimeMillis(),
    val discountApplied: Boolean = false,
    val discountPercentage: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
