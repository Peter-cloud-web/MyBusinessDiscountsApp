package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barcodes")
data class Barcode(
    @PrimaryKey val code: String,
    val clientId: String? = null,
    val isAssigned: Boolean = false,
    val scanCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val assignedAt: Long? = null,
    val lastScanned: Long? = null
)