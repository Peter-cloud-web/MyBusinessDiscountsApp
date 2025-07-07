package com.example.mypdaviesapp.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.mypdaviesapp.screens.HomeScreen

@Composable
fun CarpetCleaningApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        composable("generate_barcodes") {
            GenerateBarcodesScreen(navController)
        }
        composable("scan_barcode") {
            ScanBarcodeScreen(navController)
        }
        compo cv '  sable("clients") {
            ClientsScreen(navController)
        }
        composable("client_detail/{clientId}") { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable
            ClientDetailScreen(navController, clientId)
        }
    }
}
