package com.example.mypdaviesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import com.example.mypdaviesapp.repo.SyncManager
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
    private val syncManager: SyncManager
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

    init {
        // Auto-sync on app startup
        viewModelScope.launch {
            try {
                println("🚀 Auto-syncing on startup...")
                syncManager.syncAll()
                println("✅ Auto-sync completed")
            } catch (e: Exception) {
                println("❌ Auto-sync failed: ${e.message}")
                // Don't show error to user for auto-sync
            }
        }
    }

    fun generateBarcodes(count: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val codes = repository.generateBarcodes(count)

                // Push immediately to cloud
                syncManager.syncBarcodes()

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

    fun assignBarcode(barcodeCode: String, clientName: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.assignBarcodeToClient(barcodeCode, clientName, phoneNumber)
                .onSuccess { client ->
                    // Sync immediately after assignment
                    try {
                        syncManager.syncClients()
                        syncManager.syncBarcodes()
                    } catch (e: Exception) {
                        // Continue even if sync fails
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
                syncManager.syncAll()
                _uiState.update { it.copy(isLoading = false, message = "✅ Sync completed!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "❌ Sync failed: ${e.message}") }
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
                    } catch (e: Exception) {
                        // Continue even if sync fails
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

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun clearScanResult() {
        _uiState.update { it.copy(scanResult = null) }
    }
}