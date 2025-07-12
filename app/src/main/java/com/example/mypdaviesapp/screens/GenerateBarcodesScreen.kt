package com.example.mypdaviesapp.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mypdaviesapp.ui.components.PDFGenerator
import com.example.mypdaviesapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateBarcodesScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var quantityText by remember { mutableStateOf("10") }
    var businessName by remember { mutableStateOf("Carpet Cleaning Pro") }
    var isGenerating by remember { mutableStateOf(false) }

    // Observe unassigned barcodes
    val unassignedBarcodes by viewModel.unassignedBarcodes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Generate Barcodes",
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
            // Instructions Card
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
                        "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Generate unassigned barcode tags for carpets\n" +
                                "• Each tag will be unique and trackable\n" +
                                "• Tags must be assigned to clients when scanned\n" +
                                "• After 10 scans, client gets 50% discount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Business Name Input
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating
            )

            // Quantity Input
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Number of Barcodes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating
            )

            // Generate Button
            Button(
                onClick = {
                    val quantity = quantityText.toIntOrNull()
                    if (quantity != null && quantity > 0 && quantity <= 100) {
                        scope.launch {
                            isGenerating = true
                            try {
                                // Generate barcodes directly from repository
                                val generatedCodes = viewModel.repository.generateBarcodes(quantity)

                                // Generate PDF with the newly generated barcodes
                                val pdfGenerator = PDFGenerator(context)
                                val filePath = pdfGenerator.generateBarcodePDF(
                                    barcodes = generatedCodes,
                                    businessName = businessName
                                )

                                if (filePath != null) {
                                    isGenerating = false
                                    Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_LONG).show()
                                    navController.navigateUp()
                                } else {
                                    isGenerating = false
                                    Toast.makeText(context, "Error generating PDF", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                isGenerating = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter a valid quantity (1-100)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate PDF")
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Generated File Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The PDF will be saved to your device's Documents folder and can be accessed through your file manager or shared directly from the app.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Handle UI state messages
        val uiState by viewModel.uiState.collectAsState()

        // Show messages from ViewModel
        uiState.message?.let { message ->
            LaunchedEffect(message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }
    }
}