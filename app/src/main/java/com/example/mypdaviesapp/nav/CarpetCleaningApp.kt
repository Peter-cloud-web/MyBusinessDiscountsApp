package com.example.mypdaviesapp.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carpetcleaning.app.ui.screens.ClientDetailScreen
import com.example.mypdaviesapp.screens.CameraScannerScreen
import com.example.mypdaviesapp.screens.ClientsScreen
import com.example.mypdaviesapp.screens.GenerateBarcodesScreen
import com.example.mypdaviesapp.screens.HomeScreen
import com.example.mypdaviesapp.screens.ScanBarcodeScreen

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
        composable("camera_scanner/{isAssignMode}") { backStackEntry ->
            val isAssignMode = backStackEntry.arguments?.getString("isAssignMode")?.toBoolean() ?: false
            CameraScannerScreen(navController, isAssignMode)
        }
        composable("clients") {
            ClientsScreen(navController)
        }
        composable("client_detail/{clientId}") { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable
            ClientDetailScreen(navController, clientId)
        }
    }
}