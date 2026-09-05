package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_payment_methods")
data class SavedPaymentMethodEntity(
    @PrimaryKey val id: String,
    val customerId: String = "USR_101",
    val title: String,
    val subtitle: String,
    val type: String, // "UPI", "DEBIT_CARD", "CREDIT_CARD", "CARD"
    val bankOrProvider: String = "",
    val maskedDetails: String = "",
    val lastFourDigits: String = "",
    val cardNetwork: String = "",
    val cardExpiry: String = "",
    val gatewayTokenRef: String = "",
    val upiVpa: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
