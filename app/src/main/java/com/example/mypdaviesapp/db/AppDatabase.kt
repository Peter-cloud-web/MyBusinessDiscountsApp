package com.example.mypdaviesapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mypdaviesapp.converter.Converters
import com.example.mypdaviesapp.dao.AppMetadataDao
import com.example.mypdaviesapp.dao.BarcodeDao
import com.example.mypdaviesapp.dao.CleaningHistoryDao
import com.example.mypdaviesapp.dao.ClientDao
import com.example.mypdaviesapp.entities.AppMetadata
import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory
import com.example.mypdaviesapp.entities.Client

@Database(
    entities = [Client::class, Barcode::class, CleaningHistory::class,AppMetadata::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun barcodeDao(): BarcodeDao
    abstract fun cleaningHistoryDao(): CleaningHistoryDao
    abstract fun appMetadataDao():AppMetadataDao
}