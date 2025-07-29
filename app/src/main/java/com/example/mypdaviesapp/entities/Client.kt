package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val totalCleanings: Int = 0,
    val discountsUsed: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisit: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
