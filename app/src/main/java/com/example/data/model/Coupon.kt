package com.example.data.model

enum class DiscountType {
    FLAT,
    PERCENTAGE
}

data class Coupon(
    val code: String,
    val discountType: DiscountType = DiscountType.FLAT,
    val discountValue: Double,
    val minimumOrder: Double,
    val maximumDiscount: Double? = null,
    val isActive: Boolean = true,
    val expiryDate: String? = null,
    val oneTimePerCustomer: Boolean = false,
    val usageLimit: Int = 100,
    val timesUsed: Int = 0,
    val description: String
) {
    val id: String get() = code
    val flatDiscount: Double get() = if (discountType == DiscountType.FLAT) discountValue else 0.0
    val discountPercent: Int get() = if (discountType == DiscountType.PERCENTAGE) discountValue.toInt() else 0
}
