package com.example.mypdaviesapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mypdaviesapp.ui.components.ActionCard
import com.example.mypdaviesapp.ui.components.ClientListItem
import com.example.mypdaviesapp.ui.components.StatItem
import com.example.mypdaviesapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val unassignedBarcodes by viewModel.unassignedBarcodes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Carpet Cleaning Loyalty",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            // Quick Stats
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
                        "Quick Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Total Clients", clients.size.toString())
                        StatItem("Unassigned Tags", unassignedBarcodes.size.toString())
                        StatItem("Active Discounts", clients.count { it.totalCleanings >= 10 }.toString())
                    }
                }
            }

            // Action Buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ActionCard(
                        title = "Generate Barcodes",
                        description = "Create new barcode tags",
                        icon = Icons.Default.QrCode,
                        onClick = { navController.navigate("generate_barcodes") }
                    )
                }
                item {
                    ActionCard(
                        title = "Scan Barcode",
                        description = "Scan carpet tags",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate("scan_barcode") }
                    )
                }
                item {
                    ActionCard(
                        title = "View Clients",
                        description = "Manage client profiles",
                        icon = Icons.Default.People,
                        onClick = { navController.navigate("clients") }
                    )
                }
                item {
                    ActionCard(
                        title = "Reports",
                        description = "View statistics",
                        icon = Icons.Default.Analytics,
                        onClick = { /* TODO: Navigate to reports */ }
                    )
                }
            }

            // Recent Activity
            if (clients.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Recent Clients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        clients.take(3).forEach { client ->
                            ClientListItem(
                                client = client,
                                onClick = { navController.navigate("client_detail/${client.id}") }
                            )
                        }
                    }
                }
            }
        }

        // Show messages
        uiState.message?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar or toast
                viewModel.clearMessage()
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error message
                viewModel.clearMessage()
            }
        }
    }
}