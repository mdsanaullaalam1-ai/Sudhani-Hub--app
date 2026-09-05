package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val couponCode: String?,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String,
    val createdAt: Long,
    val deliveryPartnerId: String?,
    val deliveryPartnerName: String?,
    val deliveryPartnerPhone: String?,
    val deliveryPartnerVehicle: String?,
    val estimatedDeliveryMinutes: Int = 11,
    val deliveryOtp: String = "4819",
    val deliveryArea: String? = null,
    val pincode: String? = null
)
