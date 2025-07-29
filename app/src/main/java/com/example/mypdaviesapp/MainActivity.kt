package com.example.mypdaviesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mypdaviesapp.nav.CarpetCleaningApp
import com.example.mypdaviesapp.ui.theme.CarpetCleaningTheme
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        db.collection("test").document("testDoc")
            .set(hashMapOf("hello" to "world"))
            .addOnSuccessListener {
                println("✅ Firestore write test OK")
            }
            .addOnFailureListener {
                println("❌ Firestore write test FAILED: ${it.message}")
            }

        setContent {
            CarpetCleaningTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CarpetCleaningApp()
                }
            }
        }
    }
}