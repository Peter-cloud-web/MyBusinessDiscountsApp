package com.example.mypdaviesapp.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mypdaviesapp.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBarcodeScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    var manualCode by remember { mutableStateOf("") }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showScanResult by remember { mutableStateOf(false) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var scannedCode by remember { mutableStateOf("") }
    var isAssignMode by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Scan Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { isAssignMode = false },
                            label = { Text("Record Cleaning") },
                            selected = !isAssignMode,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { isAssignMode = true },
                            label = { Text("Assign to Client") },
                            selected = isAssignMode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Camera Scanner (Placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        // TODO: Implement camera scanner
                        Toast.makeText(context, "Camera scanner will be implemented here", Toast.LENGTH_SHORT).show()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap to open camera scanner",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Manual Entry
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Manual Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        label = { Text("Enter barcode") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (manualCode.isNotEmpty()) {
                                        scannedCode = manualCode
                                        if (isAssignMode) {
                                            showAssignDialog = true
                                        } else {
                                            viewModel.scanBarcode(manualCode)
                                            showScanResult = true
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Process")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (manualCode.isNotEmpty()) {
                                scannedCode = manualCode
                                if (isAssignMode) {
                                    showAssignDialog = true
                                } else {
                                    viewModel.scanBarcode(manualCode)
                                    showScanResult = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = manualCode.isNotEmpty() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isAssignMode) "Assign Barcode" else "Record Cleaning")
                        }
                    }
                }
            }

            // Quick Test Buttons (for development)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Quick Test",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                manualCode = "CC123456789"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sample Code")
                        }

                        OutlinedButton(
                            onClick = {
                                manualCode = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }

    // Assign Dialog
    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Barcode to Client") },
            text = {
                Column {
                    Text("Barcode: $scannedCode")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (clientName.isNotEmpty() && clientPhone.isNotEmpty()) {
                            viewModel.assignBarcode(scannedCode, clientName, clientPhone)
                            showAssignDialog = false
                            clientName = ""
                            clientPhone = ""
                            manualCode = ""
                        }
                    },
                    enabled = clientName.isNotEmpty() && clientPhone.isNotEmpty()
                ) {
                    Text("Assign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Scan Result Dialog
    uiState.scanResult?.let { result ->
        if (showScanResult) {
            AlertDialog(
                onDismissRequest = {
                    showScanResult = false
                    viewModel.clearScanResult()
                    manualCode = ""
                },
                title = {
                    Text(
                        if (result.isDiscountEligible) "Discount Eligible!" else "Cleaning Recorded",
                        color = if (result.isDiscountEligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Text("Client: ${result.client.name}")
                        Text("Phone: ${result.client.phoneNumber}")
                        Spacer(modifier = Modifier.height(8.dp))

                        if (result.isDiscountEligible) {
                            Text(
                                "🎉 ${result.discountPercentage}% Discount Applied!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Scan count has been reset to 0")
                        } else {
                            Text("Scan count: ${result.scanCount}/10")
                            Text("${10 - result.scanCount} more scans for discount")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showScanResult = false
                            viewModel.clearScanResult()
                            manualCode = ""
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}