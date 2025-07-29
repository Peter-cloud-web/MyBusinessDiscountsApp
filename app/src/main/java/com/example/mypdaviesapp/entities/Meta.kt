package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meta")
data class Meta(
    @PrimaryKey val key: String,
    val value: Long
)
