package com.example.mypdaviesapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mypdaviesapp.ui.components.ActionCard
import com.example.mypdaviesapp.ui.components.ClientListItem
import com.example.mypdaviesapp.viewmodel.MainViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val unassignedBarcodes by viewModel.unassignedBarcodes.collectAsState()

    // SwipeRefresh state
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = uiState.isLoading
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                viewModel.pullFromCloud()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                // Header with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column {
                        Text(
                            "Welcome Back",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A154B)
                        )
                        Text(
                            "Here's your business overview",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF8B0000)
                        )

                        // Add sync status indicator
                        if (uiState.isLoading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4A154B)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Syncing data...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8B0000)
                                )
                            }
                        }
                    }
                }

                // Enhanced Stats Cards
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Primary Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfessionalStatCard(
                            title = "Total Clients",
                            value = clients.size.toString(),
                            subtitle = "Active customers",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                        ProfessionalStatCard(
                            title = "Unassigned Tags",
                            value = unassignedBarcodes.size.toString(),
                            subtitle = "Available tags",
                            icon = Icons.Default.QrCode,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Secondary Stats
                    ProfessionalStatCard(
                        title = "Active Loyalty Members",
                        value = clients.count { it.totalCleanings >= 10 }.toString(),
                        subtitle = "Customers eligible for discounts",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.fillMaxWidth(),
                        isHighlighted = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section Header
                    Text(
                        "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A154B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Enhanced Action Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        item {
                            ProfessionalActionCard(
                                title = "Generate Barcodes",
                                description = "Create new barcode tags",
                                icon = Icons.Default.QrCode,
                                onClick = { navController.navigate("generate_barcodes") }
                            )
                        }
                        item {
                            ProfessionalActionCard(
                                title = "Scan Barcode",
                                description = "Scan carpet tags",
                                icon = Icons.Default.CameraAlt,
                                onClick = { navController.navigate("scan_barcode") }
                            )
                        }
                        item {
                            ProfessionalActionCard(
                                title = "Client Management",
                                description = "Manage profiles",
                                icon = Icons.Default.People,
                                onClick = { navController.navigate("clients") }
                            )
                        }
                        item {
                            ProfessionalActionCard(
                                title = "Analytics",
                                description = "View reports",
                                icon = Icons.Default.Analytics,
                                onClick = { /* TODO: Navigate to reports */ }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recent Activity Section
                    if (clients.isNotEmpty()) {
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Recent Clients",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A154B)
                                    )
                                    TextButton(
                                        onClick = { navController.navigate("clients") }
                                    ) {
                                        Text("View All")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                clients.take(3).forEach { client ->
                                    ClientListItem(
                                        client = client,
                                        onClick = { navController.navigate("client_detail/${client.id}") }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Show success/error messages as snackbars
        uiState.message?.let { message ->
            LaunchedEffect(message) {
                // You can show a snackbar here if needed
                viewModel.clearMessage()
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // You can show an error snackbar here if needed
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
fun ProfessionalStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                Color(0xFFE8E4F0)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B0000),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted)
                            Color(0xFF4A154B)
                        else
                            Color(0xFF2C3E50)
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted)
                        Color(0xFF4A154B)
                    else
                        Color(0xFF8B0000),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF95A5A6)
            )
        }
    }
}

@Composable
fun ProfessionalActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A154B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B0000)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}