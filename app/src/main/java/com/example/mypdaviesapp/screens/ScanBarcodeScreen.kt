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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Handle barcode scanned from camera
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")?.observeForever { barcode ->
            barcode?.let {
                scannedCode = it
                manualCode = it

                // Clear the saved state to prevent re-processing
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")

                if (isAssignMode) {
                    showAssignDialog = true
                } else {
                    viewModel.scanBarcode(it)
                    showScanResult = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan Barcode",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                    Text(
                        if (isAssignMode) "Assign new barcode tags to clients" else "Record cleaning service for existing clients",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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

            // Camera Scanner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable {
                        navController.navigate("camera_scanner/$isAssignMode")
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "📱 Tap to open camera scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Supports QR codes, barcodes, and more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                        placeholder = { Text("e.g., CC123456789") },
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
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
                                scannedCode = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }

            // Show last scanned info if available
            if (scannedCode.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "✅ Last Scanned",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            scannedCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Assign Dialog
    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = {
                Text(
                    "Assign Barcode to Client",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Barcode:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                scannedCode,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter client's full name") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("e.g., +254712345678") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotEmpty() && clientPhone.isNotEmpty()) {
                            viewModel.assignBarcode(scannedCode, clientName, clientPhone)
                            showAssignDialog = false
                            clientName = ""
                            clientPhone = ""
                            manualCode = ""
                            scannedCode = ""
                            Toast.makeText(context, "✅ Barcode assigned successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = clientName.isNotEmpty() && clientPhone.isNotEmpty()
                ) {
                    Text("Assign")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAssignDialog = false
                    clientName = ""
                    clientPhone = ""
                }) {
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
                    scannedCode = ""
                },
                title = {
                    Text(
                        if (result.isDiscountEligible) "🎉 Discount Eligible!" else "✅ Cleaning Recorded",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isDiscountEligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text("Client: ${result.client.name}")
                                Text("Phone: ${result.client.phoneNumber}")
                                Text("Barcode: $scannedCode")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (result.isDiscountEligible) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        "🎉 ${result.discountPercentage}% Discount Applied!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Scan count has been reset to 0")
                                }
                            }
                        } else {
                            Text("Scan count: ${result.scanCount}/10")
                            Text("${10 - result.scanCount} more scans needed for discount")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showScanResult = false
                            viewModel.clearScanResult()
                            manualCode = ""
                            scannedCode = ""
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}