package com.example.mypdaviesapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mypdaviesapp.ui.components.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    navController: NavController,
    isAssignMode: Boolean = false
) {
    var flashEnabled by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAssignMode) "Assign Barcode" else "Scan for Cleaning",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Camera Preview
            CameraPreview(
                onBarcodeScanned = { barcode ->
                    if (!hasScanned) {
                        hasScanned = true
                        // Navigate back to ScanBarcodeScreen with the scanned barcode
                        navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Instructions Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = if (isAssignMode) {
                            "Scan the barcode tag to assign it to a client"
                        } else {
                            "Scan the barcode tag to record cleaning"
                        },
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Scanning frame area
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // Corner brackets for scanning frame
                    ScanningFrame()
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom tip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Hold steady and align the barcode within the frame",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanningFrame() {
    val cornerLength = 30.dp
    val cornerWidth = 4.dp
    val frameColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(250.dp)
            .background(Color.Transparent)
    ) {
        // Top-left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerLength, cornerWidth)
                .background(frameColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerWidth, cornerLength)
                .background(frameColor)
        )

        // Top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerLength, cornerWidth)
                .background(frameColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerWidth, cornerLength)
                .background(frameColor)
        )

        // Bottom-left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerLength, cornerWidth)
                .background(frameColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerWidth, cornerLength)
                .background(frameColor)
        )

        // Bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerLength, cornerWidth)
                .background(frameColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerWidth, cornerLength)
                .background(frameColor)
        )
    }
}