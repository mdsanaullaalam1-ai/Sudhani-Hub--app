package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_partners")
data class DeliveryPartnerEntity(
    @PrimaryKey val partnerId: String,
    val name: String,
    val phone: String,
    val vehicle: String,
    val isAvailable: Boolean = true,
    val currentOrderId: String? = null,
    val activeDeliveriesCount: Int = 0,
    val rating: Double = 4.85
)
