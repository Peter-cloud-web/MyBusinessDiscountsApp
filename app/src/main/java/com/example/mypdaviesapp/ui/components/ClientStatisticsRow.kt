package com.example.mypdaviesapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mypdaviesapp.entities.Barcode
import com.example.mypdaviesapp.entities.CleaningHistory

@Composable
fun ClientStatisticsRow(
    clientBarcodes: List<Barcode>,
    clientHistory: List<CleaningHistory>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        StatCard(
            title = "Active Carpets",
            value = clientBarcodes.size.toString(),
            icon = Icons.Default.LocalLaundryService,
            modifier = Modifier.weight(1f)
        )

        // Total Cleanings
        StatCard(
            title = "Total Cleanings",
            value = clientHistory.size.toString(),
            icon = Icons.Default.History,
            modifier = Modifier.weight(1f)
        )

        // Discounts Used
        StatCard(
            title = "Discounts Used",
            value = clientHistory.count { it.discountApplied}.toString(),
            icon = Icons.Default.Discount,
            modifier = Modifier.weight(1f)
        )
    }
}