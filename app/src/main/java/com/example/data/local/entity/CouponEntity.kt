package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val discountType: String,
    val discountValue: Double,
    val minimumOrder: Double,
    val maximumDiscount: Double?,
    val isActive: Boolean = true,
    val expiryDate: String?,
    val oneTimePerCustomer: Boolean = false,
    val usageLimit: Int = 100,
    val timesUsed: Int = 0,
    val description: String
)
