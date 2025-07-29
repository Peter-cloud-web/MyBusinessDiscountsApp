package com.example.mypdaviesapp.repo

import com.example.mypdaviesapp.entities.*
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val repository: CarpetCleaningRepository
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncAll() {
        syncClients()
        syncBarcodes()
        syncHistory()
    }

    suspend fun syncClients() {
        try {
            val cloudSnapshot = firestore.collection("clients").get().await()
            val cloudClients = cloudSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Client::class.java)?.copy(id = doc.id)
            }
            val localClients = repository.getAllClients().first()

            println("🔄 Syncing clients: Cloud=${cloudClients.size}, Local=${localClients.size}")

            // Sync cloud -> local (prioritize cloud data for fresh installs)
            for (cloudClient in cloudClients) {
                val local = localClients.find { it.id == cloudClient.id }
                when {
                    local == null -> {
                        // New client from cloud - insert directly without updating timestamp
                        println("📥 Inserting client from cloud: ${cloudClient.name}")
                        repository.insertClientFromSync(cloudClient)
                    }
                    cloudClient.updatedAt > local.updatedAt -> {
                        // Cloud version is newer
                        println("🔄 Updating client from cloud: ${cloudClient.name}")
                        repository.updateClientFromSync(cloudClient)
                    }
                    local.updatedAt > cloudClient.updatedAt -> {
                        // Local version is newer - push to cloud
                        println("📤 Pushing local client to cloud: ${local.name}")
                        pushClient(local)
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
        } catch (e: Exception) {
            println("❌ Failed to sync clients: ${e.message}")
            throw Exception("Failed to sync clients: ${e.message}")
        }
    }

    suspend fun syncBarcodes() {
        try {
            val cloudSnapshot = firestore.collection("barcodes").get().await()
            val cloudBarcodes = cloudSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Barcode::class.java)?.copy(code = doc.id)
            }
            val localBarcodes = repository.getAllBarcodes().first()

            println("🔄 Syncing barcodes: Cloud=${cloudBarcodes.size}, Local=${localBarcodes.size}")

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
        } catch (e: Exception) {
            println("❌ Failed to sync barcodes: ${e.message}")
            throw Exception("Failed to sync barcodes: ${e.message}")
        }
    }

    suspend fun syncHistory() {
        try {
            // Fixed: Use consistent collection name
            val cloudSnapshot = firestore.collection("cleaning_history").get().await()
            val cloudHistory = cloudSnapshot.documents.mapNotNull { doc ->
                doc.toObject(CleaningHistory::class.java)?.copy(id = doc.id)
            }
            val localHistory = repository.getAllHistory().first()

            println("🔄 Syncing history: Cloud=${cloudHistory.size}, Local=${localHistory.size}")

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
        } catch (e: Exception) {
            println("❌ Failed to sync history: ${e.message}")
            throw Exception("Failed to sync history: ${e.message}")
        }
    }

    private suspend fun pushClient(client: Client) {
        firestore.collection("clients")
            .document(client.id)
            .set(client)
            .await()
    }

    private suspend fun pushBarcode(barcode: Barcode) {
        firestore.collection("barcodes")
            .document(barcode.code)
            .set(barcode)
            .await()
    }

    private suspend fun pushHistory(history: CleaningHistory) {
        firestore.collection("cleaning_history")
            .document(history.id)
            .set(history)
            .await()
    }

    // Optional: Manual push methods for immediate sync
    suspend fun pushClientToCloud(client: Client) {
        pushClient(client)
    }

    suspend fun pushBarcodeToCloud(barcode: Barcode) {
        pushBarcode(barcode)
    }

    suspend fun pushHistoryToCloud(history: CleaningHistory) {
        pushHistory(history)
    }
}