package com.example.safesphere.data

import com.google.firebase.Timestamp

data class Report(
    val id: String = "",
    val userId: String = "",
    val location: String = "",
    val category: String = "",
    val description: String = "",
    val riskLevel: String = "Medium", // "Safe", "Medium", "High"
    val timestamp: Timestamp = Timestamp.now()
)
