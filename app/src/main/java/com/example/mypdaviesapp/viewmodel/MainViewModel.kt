package com.example.mypdaviesapp.viewmodel


import com.example.mypdaviesapp.repo.SyncManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypdaviesapp.entities.AppMetadata
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import com.example.mypdaviesapp.repo.MetadataManager
import com.example.mypdaviesapp.states.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repository: CarpetCleaningRepository,
    private val syncManager: SyncManager,
    private val metadataManager: MetadataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    val clients = repository.getAllClients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unassignedBarcodes = repository.getUnassignedBarcodes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _generatedBarcodesCount = MutableStateFlow(0)
    val generatedBarcodesCount = _generatedBarcodesCount.asStateFlow()

    private val _totalClientsCount = MutableStateFlow(0)
    val totalClientsCount = _totalClientsCount.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime = _lastSyncTime.asStateFlow()


    init {
        // Initialize metadata and perform startup sync
        viewModelScope.launch {
            try {
                println("🔧 Initializing app metadata...")
                metadataManager.performInitialMetadataSetup()

                // Load initial metadata values
                loadMetadataValues()

                println("🚀 Auto-syncing on startup...")
                syncManager.syncAll()

                // Update metadata after sync
                loadMetadataValues()
                updateLastSyncTime()

                println("✅ Auto-sync completed")
            } catch (e: Exception) {
                println("❌ Startup initialization failed: ${e.message}")
                _uiState.update {
                    it.copy(error = "Startup sync failed. Please pull to refresh.")
                }
            }
        }
    }

    fun generateBarcodes(count: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val codes = repository.generateBarcodes(count)

                // Update local metadata count
                val newCount = metadataManager.getGeneratedBarcodesCount()
                _generatedBarcodesCount.value = newCount

                // Push immediately to cloud
                syncManager.syncBarcodes()
                syncManager.syncMetadata()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Generated ${codes.size} barcodes successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to generate barcodes: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadMetadataValues() {
        try {
            val generatedCount = metadataManager.getGeneratedBarcodesCount()
            val clientsCount = metadataManager.getTotalClientsCount()
            val lastSync = metadataManager.getMetadata(AppMetadata.LAST_SYNC_TIMESTAMP)?.toLongOrNull() ?: 0L

            _generatedBarcodesCount.value = generatedCount
            _totalClientsCount.value = clientsCount
            _lastSyncTime.value = lastSync

            println("📊 Loaded metadata - Generated: $generatedCount, Clients: $clientsCount")
        } catch (e: Exception) {
            println("⚠️ Failed to load metadata: ${e.message}")
        }
    }

    private suspend fun updateLastSyncTime() {
        val currentTime = System.currentTimeMillis()
        metadataManager.setMetadata(AppMetadata.LAST_SYNC_TIMESTAMP, currentTime.toString())
        _lastSyncTime.value = currentTime
    }

    fun assignBarcode(barcodeCode: String, clientName: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.assignBarcodeToClient(barcodeCode, clientName, phoneNumber)
                .onSuccess { client ->
                    // Sync immediately after assignment
                    try {
                        syncManager.syncClients()
                        syncManager.syncBarcodes()
                        syncManager.syncMetadata()

                        // Update local metadata
                        loadMetadataValues()
                    } catch (e: Exception) {
                        println("⚠️ Post-assignment sync failed: ${e.message}")
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Barcode assigned to ${client.name} successfully"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to assign barcode"
                        )
                    }
                }
        }
    }

    fun pullFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Ensure metadata is synced first for proper app state
                metadataManager.syncMetadataFromCloud()

                // Then sync all data
                syncManager.syncAll()

                // Update local metadata values
                loadMetadataValues()
                updateLastSyncTime()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "✅ Sync completed! All data is up to date."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "❌ Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun scanBarcode(barcodeCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.scanBarcode(barcodeCode)
                .onSuccess { result ->
                    // Sync immediately after scan
                    try {
                        syncManager.syncClients()
                        syncManager.syncBarcodes()
                        syncManager.syncHistory()
                        syncManager.syncMetadata()

                        // Update metadata
                        loadMetadataValues()
                    } catch (e: Exception) {
                        println("⚠️ Post-scan sync failed: ${e.message}")
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            scanResult = result,
                            message = if (result.isDiscountEligible) {
                                "${result.client.name} is eligible for ${result.discountPercentage}% discount!"
                            } else {
                                "Scan recorded for ${result.client.name}. Count: ${result.scanCount}/10"
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to scan barcode"
                        )
                    }
                }
        }
    }

    fun refreshMetadata() {
        viewModelScope.launch {
            try {
                loadMetadataValues()
                _uiState.update {
                    it.copy(message = "Metadata refreshed")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to refresh metadata: ${e.message}")
                }
            }
        }
    }

    fun syncMetadataToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                metadataManager.syncMetadataToCloud()
                updateLastSyncTime()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Metadata synced to cloud"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to sync metadata: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun clearScanResult() {
        _uiState.update { it.copy(scanResult = null) }
    }
}