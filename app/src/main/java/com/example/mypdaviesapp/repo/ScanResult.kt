package com.example.mypdaviesapp.repo

import com.example.mypdaviesapp.entities.Client

data class ScanResult(
    val client: Client,
    val scanCount: Int,
    val isDiscountEligible: Boolean,
    val discountPercentage: Int
)