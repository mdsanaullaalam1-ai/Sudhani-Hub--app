package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "return_requests")
data class ReturnRequestEntity(
    @PrimaryKey val returnId: String,
    val orderId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val productUnit: String,
    val returnQuantity: Int,
    val unitPrice: Double,
    val itemAmount: Double,
    val refundAmount: Double,
    val isDeliveryFeeRefunded: Boolean = false,
    val deliveryFeeRefundAmount: Double = 0.0,
    val refundMethod: String = "Original Payment Method",
    val refundStatus: String = "Pending",
    val refundDate: Long? = null,
    val refundTransactionRef: String? = null,
    val orderCreatedAt: Long,
    val deliveredAt: Long,
    val requestedAt: Long,
    val returnDeadline: Long,
    val reason: String,
    val customerDescription: String = "",
    val photosCsv: String = "", // comma-separated photo URIs/labels
    val status: String,
    val rejectionReason: String? = null,
    val pickupDate: Long? = null,
    val pickupTimeSlot: String? = null,
    val pickupPartnerName: String? = null,
    val pickupPartnerPhone: String? = null,
    val adminNotes: String? = null,
    val updatedAt: Long
)
