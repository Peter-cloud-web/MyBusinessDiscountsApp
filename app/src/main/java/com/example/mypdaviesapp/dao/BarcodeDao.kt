package com.example.mypdaviesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mypdaviesapp.entities.Barcode
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {
    @Query("SELECT * FROM barcodes ORDER BY createdAt DESC")
    fun getAllBarcodes(): Flow<List<Barcode>>

    @Query("SELECT * FROM barcodes WHERE isAssigned = 0")
    fun getUnassignedBarcodes(): Flow<List<Barcode>>

    @Query("SELECT * FROM barcodes WHERE clientId = :clientId")
    fun getClientBarcodes(clientId: String): Flow<List<Barcode>>

    @Query("SELECT * FROM barcodes WHERE code = :code")
    suspend fun getBarcodeByCode(code: String): Barcode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcode(barcode: Barcode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcodes(barcodes: List<Barcode>)

    @Update
    suspend fun updateBarcode(barcode: Barcode)

    @Delete
    suspend fun deleteBarcode(barcode: Barcode)

    @Query("SELECT COUNT(*) FROM barcodes WHERE isAssigned = 0")
    suspend fun getUnassignedBarcodeCount(): Int
}