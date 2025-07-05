package com.example.mypdaviesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypdaviesapp.repo.CarpetCleaningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CarpetCleaningRepository
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

    fun generateBarcodes(count: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val codes = repository.generateBarcodes(count)
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

    fun scanBarcode(barcodeCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.scanBarcode(barcodeCode)
                .onSuccess { result ->
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