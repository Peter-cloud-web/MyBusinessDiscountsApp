package com.example.mypdaviesapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_metadata")
data class AppMetadata(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val GENERATED_BARCODES_COUNT = "generated_barcodes_count"
        const val TOTAL_CLIENTS_COUNT = "total_clients_count"
        const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        const val APP_VERSION = "app_version"
    }
}
