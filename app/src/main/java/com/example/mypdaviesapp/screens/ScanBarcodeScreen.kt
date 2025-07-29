package com.example.mypdaviesapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFE8E4F0),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF4A154B)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Scan Barcode",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A154B)
                        )
                        Text(
                            "Scan or enter barcode to track carpet cleaning",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF8B0000)
                        )
                    }
                }
            }

            // Main Content
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Mode Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E4F0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Scan Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A154B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (isAssignMode) "Assign new barcode tags to clients" else "Record cleaning service for existing clients",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2C3E50)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isAssignMode = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isAssignMode) Color(0xFF4A154B) else Color.Transparent,
                                    contentColor = if (!isAssignMode) Color.White else Color(0xFF4A154B)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Record Cleaning",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = { isAssignMode = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAssignMode) Color(0xFF4A154B) else Color.Transparent,
                                    contentColor = if (isAssignMode) Color.White else Color(0xFF4A154B)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Assign to Client",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Camera Scanner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {
                            navController.navigate("camera_scanner/$isAssignMode")
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        Color(0xFFE8E4F0),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFF4A154B)
                                )
                            }
                            Text(
                                "📱 Tap to open camera scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A154B)
                            )
                            Text(
                                "Supports QR codes, barcodes, and more",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C3E50)
                            )
                        }
                    }
                }

                // Manual Entry Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF8B0000),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Manual Entry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A154B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it },
                            label = { Text("Enter barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., CC123456789") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4A154B),
                                focusedLabelColor = Color(0xFF4A154B),
                                cursorColor = Color(0xFF4A154B)
                            ),
                            shape = RoundedCornerShape(12.dp),
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
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Process",
                                        tint = Color(0xFF4A154B)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = manualCode.isNotEmpty() && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A154B),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Processing...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    if (isAssignMode) "Assign Barcode" else "Record Cleaning",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Quick Test Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Quick Test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A154B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    manualCode = "CC123456789"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4A154B)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Sample Code",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    manualCode = ""
                                    scannedCode = ""
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF8B0000)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Clear",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Show last scanned info if available
                if (scannedCode.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E4F0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "✅ Last Scanned",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A154B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                scannedCode,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A154B)
                )
            },
            text = {
                Column {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E4F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Barcode:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4A154B)
                            )
                            Text(
                                scannedCode,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter client's full name") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF4A154B)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A154B),
                            focusedLabelColor = Color(0xFF4A154B),
                            cursorColor = Color(0xFF4A154B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("e.g., +254712345678") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF4A154B)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A154B),
                            focusedLabelColor = Color(0xFF4A154B),
                            cursorColor = Color(0xFF4A154B)
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                    enabled = clientName.isNotEmpty() && clientPhone.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A154B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Assign",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAssignDialog = false
                        clientName = ""
                        clientPhone = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF8B0000)
                    )
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
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
                        color = if (result.isDiscountEligible) Color(0xFF4A154B) else Color(0xFF2C3E50)
                    )
                },
                text = {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8E4F0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Client: ${result.client.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2C3E50)
                                )
                                Text(
                                    "Phone: ${result.client.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2C3E50)
                                )
                                Text(
                                    "Barcode: $scannedCode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2C3E50)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (result.isDiscountEligible) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8E4F0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "🎉 ${result.discountPercentage}% Discount Applied!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF4A154B),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Scan count has been reset to 0",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2C3E50)
                                    )
                                }
                            }
                        } else {
                            Text(
                                "Scan count: ${result.scanCount}/10",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C3E50)
                            )
                            Text(
                                "${10 - result.scanCount} more scans needed for discount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C3E50)
                            )
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A154B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "OK",
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}