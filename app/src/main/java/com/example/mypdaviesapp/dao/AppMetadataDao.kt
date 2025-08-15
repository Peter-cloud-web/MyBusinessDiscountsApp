package com.example.mypdaviesapp.dao

import androidx.room.*
import com.example.mypdaviesapp.entities.AppMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetadataDao {

    @Query("SELECT * FROM app_metadata WHERE key = :key")
    suspend fun getMetadata(key: String): AppMetadata?

    @Query("SELECT * FROM app_metadata WHERE key = :key")
    fun getMetadataFlow(key: String): Flow<AppMetadata?>

    @Query("SELECT * FROM app_metadata")
    suspend fun getAllMetadata(): List<AppMetadata>

    @Query("SELECT * FROM app_metadata")
    fun getAllMetadataFlow(): Flow<List<AppMetadata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: AppMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMetadata(metadata: List<AppMetadata>)

    @Update
    suspend fun updateMetadata(metadata: AppMetadata)

    @Delete
    suspend fun deleteMetadata(metadata: AppMetadata)

    @Query("DELETE FROM app_metadata WHERE key = :key")
    suspend fun deleteMetadataByKey(key: String)
}