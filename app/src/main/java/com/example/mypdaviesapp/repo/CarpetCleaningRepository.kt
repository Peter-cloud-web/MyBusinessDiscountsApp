package com.example.mypdaviesapp.repo

import com.example.mypdaviesapp.dao.BarcodeDao
import com.example.mypdaviesapp.dao.CleaningHistoryDao
import com.example.mypdaviesapp.dao.ClientDao
import com.example.mypdaviesapp.dao.AppMetadataDao
import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory
import com.example.mypdaviesapp.entities.Client
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarpetCleaningRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val barcodeDao: BarcodeDao,
    private val cleaningHistoryDao: CleaningHistoryDao,
//    private val metaDao: AppMetadataDao,
//    private val firestore: FirebaseFirestore,
    private val metadataManager: MetadataManager
) {

    // ---------------------------
    // CLIENTS
    // ---------------------------
    fun getAllClients(): Flow<List<Client>> {
        return clientDao.getAllClients()
    }

    suspend fun getClientById(id: String) = clientDao.getClientById(id)

    suspend fun getClientByPhone(phone: String) = clientDao.getClientByPhoneNumber(phone)

    suspend fun insertClient(client: Client) {
        val updatedClient = client.copy(updatedAt = System.currentTimeMillis())
        clientDao.insertClient(updatedClient)
    }

    suspend fun updateClient(client: Client) {
        val updatedClient = client.copy(updatedAt = System.currentTimeMillis())
        clientDao.updateClient(updatedClient)
    }

    // Sync-specific methods that preserve original timestamps
    suspend fun insertClientFromSync(client: Client) {
        clientDao.insertClient(client)
    }

    suspend fun updateClientFromSync(client: Client) {
        clientDao.updateClient(client)
    }

    // ---------------------------
    // BARCODES
    // ---------------------------
    suspend fun getAllBarcodes(): Flow<List<Barcode>> {
        return barcodeDao.getAllBarcodesFlow() // You'll need this method in BarcodeDao
    }

    suspend fun getAllBarcodesAsList(): List<Barcode> {
        return barcodeDao.getAllBarcodes()
    }

    suspend fun getAllHistory(): Flow<List<CleaningHistory>> {
        return cleaningHistoryDao.getAllHistory() // You'll need this method too
    }

    fun getUnassignedBarcodes(): Flow<List<Barcode>> {
        return barcodeDao.getUnassignedBarcodes()
    }

    fun getClientBarcodes(clientId: String) = barcodeDao.getClientBarcodes(clientId)

    suspend fun getBarcodeByCode(code: String) = barcodeDao.getBarcodeByCode(code)

    suspend fun insertBarcode(barcode: Barcode) {
        val updatedBarcode = barcode.copy(updatedAt = System.currentTimeMillis())
        barcodeDao.insertBarcode(updatedBarcode)
    }

    suspend fun insertBarcodes(barcodes: List<Barcode>) {
        val updated = barcodes.map { it.copy(updatedAt = System.currentTimeMillis()) }
        barcodeDao.insertBarcodes(updated)
    }

    suspend fun updateBarcode(barcode: Barcode) {
        val updatedBarcode = barcode.copy(updatedAt = System.currentTimeMillis())
        barcodeDao.updateBarcode(updatedBarcode)
    }

    // Sync-specific methods that preserve original timestamps
    suspend fun insertBarcodeFromSync(barcode: Barcode) {
        barcodeDao.insertBarcode(barcode)
    }

    suspend fun updateBarcodeFromSync(barcode: Barcode) {
        barcodeDao.updateBarcode(barcode)
    }


    suspend fun getUnassignedBarcodeCount() = barcodeDao.getUnassignedBarcodeCount()

    // ---------------------------
    // CLEANING HISTORY
    // ---------------------------
    fun getClientHistory(clientId: String) = cleaningHistoryDao.getClientHistory(clientId)

//    fun getAllHistory() = cleaningHistoryDao.getAllHistory()

    suspend fun getAllHistoryAsList(): Flow<List<CleaningHistory>> {
        return cleaningHistoryDao.getAllHistory()
    }

    suspend fun insertHistory(history: CleaningHistory) {
        val updatedHistory = history.copy(updatedAt = System.currentTimeMillis())
        cleaningHistoryDao.insertHistory(updatedHistory)
    }

    // Sync-specific method that preserves original timestamp
    suspend fun insertHistoryFromSync(history: CleaningHistory) {
        cleaningHistoryDao.insertHistory(history)
    }

    // ---------------------------
    // COMPLEX OPERATIONS
    // ---------------------------
    suspend fun generateBarcodes(count: Int): List<String> {
        return try {
            val currentCount = metadataManager.getGeneratedBarcodesCount()
            val codes = mutableListOf<String>()

            for (i in 1..count) {
                val code = "PDC${String.format("%06d", currentCount + i)}"
                codes.add(code)

                val barcode = Barcode(
                    code = code,
                    createdAt = System.currentTimeMillis()
                )

                barcodeDao.insertBarcode(barcode)
            }

            // Update the generated count
            metadataManager.incrementGeneratedBarcodesCount(count)

            // Also sync metadata to cloud immediately
            metadataManager.syncMetadataToCloud()

            codes
        } catch (e: Exception) {
            throw Exception("Failed to generate barcodes: ${e.message}")
        }
    }

    suspend fun assignBarcodeToClient(
        barcodeCode: String,
        clientName: String,
        phoneNumber: String
    ): Result<Client> {
        return try {
            // Check if barcode exists and is unassigned
            val barcode = barcodeDao.getBarcodeByCode(barcodeCode)
                ?: return Result.failure(Exception("Barcode not found"))

            if (barcode.isAssigned) {
                return Result.failure(Exception("Barcode already assigned"))
            }

            // Check if client already exists by phone number
            var client = clientDao.getClientByPhoneNumber(phoneNumber)

            if (client == null) {
                // Create new client
                client = Client(
                    id = UUID.randomUUID().toString(),
                    name = clientName,
                    phoneNumber = phoneNumber,
                    totalCleanings = 0,
                    discountsUsed = 0,
                    createdAt = System.currentTimeMillis(),
                    lastVisit = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                clientDao.insertClient(client)
            } else {
                // Update existing client's name if different
                if (client.name != clientName) {
                    client = client.copy(
                        name = clientName,
                        updatedAt = System.currentTimeMillis()
                    )
                    clientDao.updateClient(client)
                }
            }

            // Assign barcode to client
            val updatedBarcode = barcode.copy(
                clientId = client.id,
                isAssigned = true,
                assignedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            barcodeDao.updateBarcode(updatedBarcode)

            // Update metadata - get current client count
            val currentClientCount = clientDao.getClientCount()
            metadataManager.setTotalClientsCount(currentClientCount)

            // Sync metadata to cloud
            metadataManager.syncMetadataToCloud()

            Result.success(client)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanBarcode(barcodeCode: String): Result<ScanResult> {
        return try {
            val barcode = getBarcodeByCode(barcodeCode) ?: return Result.failure(Exception("Barcode not found"))
            if (!barcode.isAssigned || barcode.clientId == null) return Result.failure(Exception("Barcode not assigned"))

            val client = getClientById(barcode.clientId) ?: return Result.failure(Exception("Client not found"))

            val newScanCount = barcode.scanCount + 1
            val discount = if (newScanCount >= 10) 50 else 0

            updateBarcode(
                barcode.copy(
                    scanCount = if (discount > 0) 0 else newScanCount,
                    lastScanned = System.currentTimeMillis()
                )
            )

            updateClient(
                client.copy(
                    totalCleanings = client.totalCleanings + 1,
                    discountsUsed = if (discount > 0) client.discountsUsed + 1 else client.discountsUsed,
                    lastVisit = System.currentTimeMillis()
                )
            )

            insertHistory(
                CleaningHistory(
                    clientId = client.id,
                    barcodeId = barcodeCode,
                    discountApplied = discount > 0,
                    discountPercentage = discount
                )
            )

            Result.success(
                ScanResult(
                    client = client,
                    scanCount = newScanCount,
                    isDiscountEligible = discount > 0,
                    discountPercentage = discount
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    private fun generateUniqueCode(): String {
        return "CC${System.currentTimeMillis().toString().takeLast(6)}${(1000..9999).random()}"
    }
}