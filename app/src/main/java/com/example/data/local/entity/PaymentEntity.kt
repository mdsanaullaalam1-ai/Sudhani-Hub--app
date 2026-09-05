package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val paymentId: String,
    val orderId: String,
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val transactionRef: String,
    val createdAt: Long
)
