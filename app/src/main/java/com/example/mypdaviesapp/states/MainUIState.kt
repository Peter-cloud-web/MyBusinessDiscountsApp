package com.example.mypdaviesapp.states

import com.example.mypdaviesapp.repo.ScanResult

data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val scanResult: ScanResult? = null
)