
@file:OptIn(ExperimentalMaterial3Api::class)

package com.carpetcleaning.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController

import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory
import com.example.mypdaviesapp.entities.Client
import com.example.mypdaviesapp.ui.components.BarcodeCard
import com.example.mypdaviesapp.ui.components.CleaningHistoryCard
import com.example.mypdaviesapp.ui.components.ClientInfoCard
import com.example.mypdaviesapp.ui.components.ClientStatisticsRow
import com.example.mypdaviesapp.ui.components.DiscountDialog
import com.example.mypdaviesapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    navController: NavController,
    clientId: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    var client by remember { mutableStateOf<Client?>(null) }
    var clientBarcodes by remember { mutableStateOf<List<Barcode>>(emptyList()) }
    var clientHistory by remember { mutableStateOf<List<CleaningHistory>>(emptyList()) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var selectedBarcode by remember { mutableStateOf<Barcode?>(null) }

    LaunchedEffect(clientId) {
        // Load client data
        viewModel.viewModelScope.launch {
            client = viewModel.repository.getClientById(clientId)
            viewModel.repository.getClientBarcodes(clientId).collect { barcodes ->
                clientBarcodes = barcodes
            }
            viewModel.repository.getClientHistory(clientId).collect { history ->
                clientHistory = history
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client?.name ?: "Client Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        if (client == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Client Information Card
                item {
                    ClientInfoCard(client = client!!)
                }

                // Statistics Cards
                item {
                    ClientStatisticsRow(
                        clientBarcodes = clientBarcodes,
                        clientHistory = clientHistory
                    )
                }

                // Active Barcodes Section
                if (clientBarcodes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Carpet Tags",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(clientBarcodes) { barcode ->
                        BarcodeCard(
                            barcode = barcode,
                            onApplyDiscount = { barcodeToDiscount ->  // Renamed parameter
                                showDiscountDialog = true
                                selectedBarcode = barcodeToDiscount   // Use the renamed parameter
                            }
                        )
                    }
                }

                // Cleaning History Section
                if (clientHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Cleaning History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(clientHistory.take(10)) { history ->
                        CleaningHistoryCard(history = history)
                    }
                }
            }
        }
    }

    // Discount Dialog
    if (showDiscountDialog && selectedBarcode != null) {
        DiscountDialog(
            barcode = selectedBarcode!!,
            onDismiss = { showDiscountDialog = false },
            onConfirm = { barcode ->
                viewModel.viewModelScope.launch {
                    // Reset cleaning count and apply discount
                    val updatedBarcode = barcode.copy(scanCount = 1)
                    viewModel.repository.updateBarcode(updatedBarcode)


                    val discountHistory = CleaningHistory(
                        clientId = clientId,
                        barcodeId = barcode.code,
                        discountApplied = true,
                        discountPercentage = 50
                    )
                    viewModel.repository.insertHistory(discountHistory)
                }
                showDiscountDialog = false
            }
        )
    }
}