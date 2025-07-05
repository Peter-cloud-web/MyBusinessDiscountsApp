package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.ForeignKey

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