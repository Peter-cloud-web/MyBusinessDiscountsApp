package com.example.mypdaviesapp.repo

import com.example.mypdaviesapp.dao.BarcodeDao
import com.example.mypdaviesapp.dao.CleaningHistoryDao
import com.example.mypdaviesapp.dao.ClientDao
import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory
import com.example.mypdaviesapp.entities.Client
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarpetCleaningRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val barcodeDao: BarcodeDao,
    private val cleaningHistoryDao: CleaningHistoryDao
) {
    // Client operations
    fun getAllClients() = clientDao.getAllClients()
    suspend fun getClientById(id: String) = clientDao.getClientById(id)
    suspend fun getClientByPhone(phone: String) = clientDao.getClientByPhone(phone)
    suspend fun insertClient(client: Client) = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)

    // Barcode operations
    fun getAllBarcodes() = barcodeDao.getAllBarcodes()
    fun getUnassignedBarcodes() = barcodeDao.getUnassignedBarcodes()
    fun getClientBarcodes(clientId: String) = barcodeDao.getClientBarcodes(clientId)
    suspend fun getBarcodeByCode(code: String) = barcodeDao.getBarcodeByCode(code)
    suspend fun insertBarcode(barcode: Barcode) = barcodeDao.insertBarcode(barcode)
    suspend fun insertBarcodes(barcodes: List<Barcode>) = barcodeDao.insertBarcodes(barcodes)
    suspend fun updateBarcode(barcode: Barcode) = barcodeDao.updateBarcode(barcode)
    suspend fun getUnassignedBarcodeCount() = barcodeDao.getUnassignedBarcodeCount()

    // History operations
    fun getClientHistory(clientId: String) = cleaningHistoryDao.getClientHistory(clientId)
    fun getAllHistory() = cleaningHistoryDao.getAllHistory()
    suspend fun insertHistory(history: CleaningHistory) = cleaningHistoryDao.insertHistory(history)

    // Complex operations
    suspend fun generateBarcodes(count: Int): List<String> {
        val barcodes = mutableListOf<Barcode>()
        repeat(count) {
            val code = generateUniqueCode()
            barcodes.add(Barcode(code = code))
        }
        insertBarcodes(barcodes)
        return barcodes.map { it.code }
    }

    suspend fun assignBarcodeToClient(barcodeCode: String, clientName: String, phoneNumber: String): Result<Client> {
        return try {
            val barcode = getBarcodeByCode(barcodeCode)
            if (barcode == null) {
                return Result.failure(Exception("Barcode not found"))
            }
            if (barcode.isAssigned) {
                return Result.failure(Exception("Barcode already assigned"))
            }

            // Check if client exists
            val existingClient = getClientByPhone(phoneNumber)
            val client = existingClient ?: Client(
                name = clientName,
                phoneNumber = phoneNumber
            )

            // Save client if new
            if (existingClient == null) {
                insertClient(client)
            }

            // Update barcode
            updateBarcode(
                barcode.copy(
                    clientId = client.id,
                    isAssigned = true,
                    assignedAt = System.currentTimeMillis()
                )
            )

            Result.success(client)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanBarcode(barcodeCode: String): Result<ScanResult> {
        return try {
            val barcode = getBarcodeByCode(barcodeCode)
            if (barcode == null) {
                return Result.failure(Exception("Barcode not found"))
            }
            if (!barcode.isAssigned || barcode.clientId == null) {
                return Result.failure(Exception("Barcode not assigned to any client"))
            }

            val client = getClientById(barcode.clientId)
            if (client == null) {
                return Result.failure(Exception("Client not found"))
            }

            val newScanCount = barcode.scanCount + 1
            val isDiscountEligible = newScanCount >= 10

            // Update barcode
            updateBarcode(
                barcode.copy(
                    scanCount = if (isDiscountEligible) 0 else newScanCount,
                    lastScanned = System.currentTimeMillis()
                )
            )

            // Update client
            updateClient(
                client.copy(
                    totalCleanings = client.totalCleanings + 1,
                    discountsUsed = if (isDiscountEligible) client.discountsUsed + 1 else client.discountsUsed,
                    lastVisit = System.currentTimeMillis()
                )
            )

            // Add to history
            insertHistory(
                CleaningHistory(
                    clientId = client.id,
                    barcodeId = barcodeCode,
                    discountApplied = isDiscountEligible,
                    discountPercentage = if (isDiscountEligible) 50 else 0
                )
            )

            Result.success(
                ScanResult(
                    client = client,
                    scanCount = newScanCount,
                    isDiscountEligible = isDiscountEligible,
                    discountPercentage = if (isDiscountEligible) 50 else 0
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