package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val name: String,
    val phone: String,
    val email: String,
    val totalOrders: Int = 0,
    val totalSpending: Double = 0.0,
    val registeredAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val role: String = "CUSTOMER"
)
