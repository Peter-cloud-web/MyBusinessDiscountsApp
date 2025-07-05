package com.example.mypdaviesapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.mypdaviesapp.entities.CleaningHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CleaningHistoryDao {
    @Query("SELECT * FROM cleaning_history WHERE clientId = :clientId ORDER BY cleaningDate DESC")
    fun getClientHistory(clientId: String): Flow<List<CleaningHistory>>

    @Query("SELECT * FROM cleaning_history ORDER BY cleaningDate DESC")
    fun getAllHistory(): Flow<List<CleaningHistory>>

    @Insert
    suspend fun insertHistory(history: CleaningHistory)

    @Delete
    suspend fun deleteHistory(history: CleaningHistory)
}