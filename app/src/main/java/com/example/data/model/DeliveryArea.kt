package com.example.data.model

data class DeliveryArea(
    val id: String = "",
    val pincode: String = "",
    val areaName: String = "",
    val city: String = "",
    val state: String = "",
    val deliveryFee: Double = 0.0,
    val minimumOrderAmount: Double = 199.0,
    val estimatedDeliveryTime: String = "10–20 mins",
    val isActive: Boolean = true,
    val minDistanceKm: Double = 0.0,
    val maxDistanceKm: Double = 25.0,
    val defaultDistanceKm: Double = 4.5,
    val freeDeliveryMinOrderAmount: Double = 999.0,
    val manualOverrideFee: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
