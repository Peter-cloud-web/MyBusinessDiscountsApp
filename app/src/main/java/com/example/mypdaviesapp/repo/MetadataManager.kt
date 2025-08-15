package com.example.mypdaviesapp.repo

import com.example.mypdaviesapp.dao.AppMetadataDao
import com.example.mypdaviesapp.entities.AppMetadata
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataManager @Inject constructor(
    private val metadataDao: AppMetadataDao,
    private val firestore: FirebaseFirestore
) {
    private val metadataCollection = firestore.collection("app_metadata")

    // Local operations
    suspend fun getMetadata(key: String): String? {
        return metadataDao.getMetadata(key)?.value
    }

    suspend fun setMetadata(key: String, value: String) {
        val metadata = AppMetadata(
            key = key,
            value = value,
            updatedAt = System.currentTimeMillis()
        )
        metadataDao.insertMetadata(metadata)
    }

    suspend fun getGeneratedBarcodesCount(): Int {
        return getMetadata(AppMetadata.GENERATED_BARCODES_COUNT)?.toIntOrNull() ?: 0
    }

    suspend fun setGeneratedBarcodesCount(count: Int) {
        setMetadata(AppMetadata.GENERATED_BARCODES_COUNT, count.toString())
    }

    suspend fun incrementGeneratedBarcodesCount(increment: Int) {
        val currentCount = getGeneratedBarcodesCount()
        setGeneratedBarcodesCount(currentCount + increment)
    }

    suspend fun getTotalClientsCount(): Int {
        return getMetadata(AppMetadata.TOTAL_CLIENTS_COUNT)?.toIntOrNull() ?: 0
    }

    suspend fun setTotalClientsCount(count: Int) {
        setMetadata(AppMetadata.TOTAL_CLIENTS_COUNT, count.toString())
    }

    // Cloud sync operations
    suspend fun syncMetadataToCloud() {
        try {
            val allMetadata = metadataDao.getAllMetadata()

            allMetadata.forEach { metadata ->
                val data = mapOf(
                    "key" to metadata.key,
                    "value" to metadata.value,
                    "updatedAt" to metadata.updatedAt
                )

                metadataCollection
                    .document(metadata.key)
                    .set(data)
                    .await()
            }

            println("✅ Metadata synced to cloud successfully")
        } catch (e: Exception) {
            println("❌ Failed to sync metadata to cloud: ${e.message}")
            throw e
        }
    }

    suspend fun syncMetadataFromCloud() {
        try {
            val snapshot = metadataCollection.get().await()
            val cloudMetadata = mutableListOf<AppMetadata>()

            for (doc in snapshot.documents) {
                val data = doc.data
                if (data != null) {
                    val metadata = AppMetadata(
                        key = data["key"] as String,
                        value = data["value"] as String,
                        updatedAt = data["updatedAt"] as Long
                    )
                    cloudMetadata.add(metadata)
                }
            }

            // Insert all metadata from cloud
            metadataDao.insertAllMetadata(cloudMetadata)

            println("✅ Metadata synced from cloud successfully. Items: ${cloudMetadata.size}")
        } catch (e: Exception) {
            println("❌ Failed to sync metadata from cloud: ${e.message}")
            throw e
        }
    }

    suspend fun performInitialMetadataSetup() {
        try {
            // Check if this is a fresh install by looking for existing metadata
            val existingCount = getGeneratedBarcodesCount()

            if (existingCount == 0) {
                // This might be a fresh install, try to sync from cloud first
                println("📥 Fresh install detected, syncing metadata from cloud...")
                syncMetadataFromCloud()

                // If still 0 after cloud sync, initialize with defaults
                val countAfterSync = getGeneratedBarcodesCount()
                if (countAfterSync == 0) {
                    println("🆕 No cloud metadata found, initializing defaults...")
                    setGeneratedBarcodesCount(0)
                    setTotalClientsCount(0)
                    setMetadata(AppMetadata.LAST_SYNC_TIMESTAMP, System.currentTimeMillis().toString())
                }
            }
        } catch (e: Exception) {
            println("⚠️ Initial metadata setup failed, using defaults: ${e.message}")
            // Set defaults if everything fails
            setGeneratedBarcodesCount(0)
            setTotalClientsCount(0)
        }
    }
}