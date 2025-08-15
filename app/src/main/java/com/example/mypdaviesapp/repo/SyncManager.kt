package com.example.mypdaviesapp.repo
import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory
import com.example.mypdaviesapp.entities.Client
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import com.example.mypdaviesapp.repo.MetadataManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// 3. Fixed SyncManager with proper error handling and debugging
@Singleton
class SyncManager @Inject constructor(
    private val repository: CarpetCleaningRepository,
    private val metadataManager: MetadataManager
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncAll() {
        println("🔄 Starting complete sync...")
        try {
            // First sync metadata from cloud (important for fresh installs)
            println("📊 Syncing metadata from cloud...")
            metadataManager.syncMetadataFromCloud()

            // Then sync all data
            println("👥 Syncing clients...")
            syncClients()

            println("🏷️ Syncing barcodes...")
            syncBarcodes()

            println("📜 Syncing history...")
            syncHistory()

            // Finally sync metadata to cloud (in case local data was newer)
            println("📊 Syncing metadata to cloud...")
            metadataManager.syncMetadataToCloud()

            println("✅ Complete sync finished successfully")
        } catch (e: Exception) {
            println("❌ Sync failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun syncClients() {
        try {
            println("👥 Starting client sync...")

            // Get cloud data
            val cloudSnapshot = firestore.collection("clients").get().await()
            val cloudClients = cloudSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Client(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String ?: "",
                        totalCleanings = (data["totalCleanings"] as? Long)?.toInt() ?: 0,
                        discountsUsed = (data["discountsUsed"] as? Long)?.toInt() ?: 0,
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                        lastVisit = data["lastVisit"] as? Long ?: System.currentTimeMillis(),
                        updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to parse client ${doc.id}: ${e.message}")
                    null
                }
            }

            // Get local data
            val localClients = repository.getAllClients().first()

            println("📊 Client counts - Cloud: ${cloudClients.size}, Local: ${localClients.size}")

            // Sync cloud -> local
            for (cloudClient in cloudClients) {
                val local = localClients.find { it.id == cloudClient.id }
                when {
                    local == null -> {
                        println("📥 Inserting new client from cloud: ${cloudClient.name}")
                        repository.insertClientFromSync(cloudClient)
                    }
                    cloudClient.updatedAt > local.updatedAt -> {
                        println("🔄 Updating client from cloud: ${cloudClient.name} (cloud: ${cloudClient.updatedAt} > local: ${local.updatedAt})")
                        repository.updateClientFromSync(cloudClient)
                    }
                    local.updatedAt > cloudClient.updatedAt -> {
                        println("📤 Local client newer, pushing to cloud: ${local.name}")
                        pushClient(local)
                    }
                    else -> {
                        println("✅ Client in sync: ${local.name}")
                    }
                }
            }

            // Push local-only clients to cloud
            val cloudIds = cloudClients.map { it.id }.toSet()
            for (localClient in localClients) {
                if (!cloudIds.contains(localClient.id)) {
                    println("📤 Pushing new local client to cloud: ${localClient.name}")
                    pushClient(localClient)
                }
            }

            println("✅ Client sync completed")
        } catch (e: Exception) {
            println("❌ Failed to sync clients: ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to sync clients: ${e.message}")
        }
    }

    suspend fun syncBarcodes() {
        try {
            println("🏷️ Starting barcode sync...")

            val cloudSnapshot = firestore.collection("barcodes").get().await()
            val cloudBarcodes = cloudSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Barcode(
                        code = doc.id,
                        clientId = data["clientId"] as? String,
                        isAssigned = data["isAssigned"] as? Boolean ?: false,
                        scanCount = (data["scanCount"] as? Long)?.toInt() ?: 0,
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                        assignedAt = data["assignedAt"] as? Long,
                        lastScanned = data["lastScanned"] as? Long,
                        updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to parse barcode ${doc.id}: ${e.message}")
                    null
                }
            }

            val localBarcodes = repository.getAllBarcodesAsList()

            println("📊 Barcode counts - Cloud: ${cloudBarcodes.size}, Local: ${localBarcodes.size}")

            // Sync cloud -> local
            for (cloudBarcode in cloudBarcodes) {
                val local = localBarcodes.find { it.code == cloudBarcode.code }
                when {
                    local == null -> {
                        println("📥 Inserting barcode from cloud: ${cloudBarcode.code}")
                        repository.insertBarcodeFromSync(cloudBarcode)
                    }
                    cloudBarcode.updatedAt > local.updatedAt -> {
                        println("🔄 Updating barcode from cloud: ${cloudBarcode.code}")
                        repository.updateBarcodeFromSync(cloudBarcode)
                    }
                    local.updatedAt > cloudBarcode.updatedAt -> {
                        println("📤 Pushing local barcode to cloud: ${local.code}")
                        pushBarcode(local)
                    }
                }
            }

            // Push local-only barcodes to cloud
            val cloudCodes = cloudBarcodes.map { it.code }.toSet()
            for (localBarcode in localBarcodes) {
                if (!cloudCodes.contains(localBarcode.code)) {
                    println("📤 Pushing new local barcode to cloud: ${localBarcode.code}")
                    pushBarcode(localBarcode)
                }
            }

            println("✅ Barcode sync completed")
        } catch (e: Exception) {
            println("❌ Failed to sync barcodes: ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to sync barcodes: ${e.message}")
        }
    }

    suspend fun syncHistory() {
        try {
            println("📜 Starting history sync...")

            val cloudSnapshot = firestore.collection("cleaning_history").get().await()
            val cloudHistory = cloudSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    CleaningHistory(
                        id = doc.id,
                        clientId = data["clientId"] as? String ?: "",
                        barcodeId = data["barcodeId"] as? String ?: "",
                        cleaningDate = data["cleaningDate"] as? Long ?: System.currentTimeMillis(),
                        discountApplied = data["discountApplied"] as? Boolean ?: false,
                        discountPercentage = (data["discountPercentage"] as? Long)?.toInt() ?: 0,
                        updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to parse history ${doc.id}: ${e.message}")
                    null
                }
            }

            val localHistory = repository.getAllHistoryAsList().first()

            // FIXED: Use .size (property) instead of .size()
            println("📊 History counts - Cloud: ${cloudHistory.size}, Local: ${localHistory.size}")

            // Sync cloud -> local
            for (cloudRecord in cloudHistory) {
                val local = localHistory.find { it.id == cloudRecord.id }
                when {
                    local == null -> {
                        println("📥 Inserting history from cloud: ${cloudRecord.id}")
                        repository.insertHistoryFromSync(cloudRecord)
                    }
                    cloudRecord.updatedAt > local.updatedAt -> {
                        println("🔄 Updating history from cloud: ${cloudRecord.id}")
                        repository.insertHistoryFromSync(cloudRecord)
                    }
                    local.updatedAt > cloudRecord.updatedAt -> {
                        println("📤 Pushing local history to cloud: ${local.id}")
                        pushHistory(local)
                    }
                }
            }

            // Push local-only history to cloud
            val cloudIds = cloudHistory.map { it.id }.toSet()
            for (localRecord in localHistory) {
                if (!cloudIds.contains(localRecord.id)) {
                    println("📤 Pushing new local history to cloud: ${localRecord.id}")
                    pushHistory(localRecord)
                }
            }

            println("✅ History sync completed")
        } catch (e: Exception) {
            println("❌ Failed to sync history: ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to sync history: ${e.message}")
        }
    }

    private suspend fun pushClient(client: Client) {
        try {
            val data = mapOf(
                "name" to client.name,
                "phoneNumber" to client.phoneNumber,
                "totalCleanings" to client.totalCleanings,
                "discountsUsed" to client.discountsUsed,
                "createdAt" to client.createdAt,
                "lastVisit" to client.lastVisit,
                "updatedAt" to client.updatedAt
            )

            firestore.collection("clients")
                .document(client.id)
                .set(data)
                .await()

            println("✅ Successfully pushed client ${client.name} to cloud")
        } catch (e: Exception) {
            println("❌ Failed to push client ${client.name}: ${e.message}")
            throw e
        }
    }

    private suspend fun pushBarcode(barcode: Barcode) {
        try {
            val data = mapOf(
                "clientId" to barcode.clientId,
                "isAssigned" to barcode.isAssigned,
                "scanCount" to barcode.scanCount,
                "createdAt" to barcode.createdAt,
                "assignedAt" to barcode.assignedAt,
                "lastScanned" to barcode.lastScanned,
                "updatedAt" to barcode.updatedAt
            )

            firestore.collection("barcodes")
                .document(barcode.code)
                .set(data)
                .await()

            println("✅ Successfully pushed barcode ${barcode.code} to cloud")
        } catch (e: Exception) {
            println("❌ Failed to push barcode ${barcode.code}: ${e.message}")
            throw e
        }
    }

    private suspend fun pushHistory(history: CleaningHistory) {
        try {
            val data = mapOf(
                "clientId" to history.clientId,
                "barcodeId" to history.barcodeId,
                "cleaningDate" to history.cleaningDate,
                "discountApplied" to history.discountApplied,
                "discountPercentage" to history.discountPercentage,
                "updatedAt" to history.updatedAt
            )

            firestore.collection("cleaning_history")
                .document(history.id)
                .set(data)
                .await()

            println("✅ Successfully pushed history ${history.id} to cloud")
        } catch (e: Exception) {
            println("❌ Failed to push history ${history.id}: ${e.message}")
            throw e
        }
    }

    suspend fun syncMetadata() {
        try {
            metadataManager.syncMetadataToCloud()
            metadataManager.syncMetadataFromCloud()
        } catch (e: Exception) {
            println("❌ Metadata sync failed: ${e.message}")
            throw e
        }
    }
}