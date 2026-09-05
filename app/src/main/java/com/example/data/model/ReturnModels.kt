package com.example.data.model

enum class ReturnStatus(
    val displayName: String,
    val stepIndex: Int,
    val description: String,
    val badgeColorHex: Long = 0xFF4F46E5
) {
    REQUESTED(
        displayName = "Return Requested",
        stepIndex = 0,
        description = "Your return request has been submitted and is waiting for review.",
        badgeColorHex = 0xFF4F46E5 // Indigo
    ),
    UNDER_REVIEW(
        displayName = "Under Review",
        stepIndex = 1,
        description = "Store manager is inspecting your return reason and photos.",
        badgeColorHex = 0xFFD97706 // Amber
    ),
    APPROVED(
        displayName = "Return Approved",
        stepIndex = 2,
        description = "Return request approved. Reverse pickup will be arranged.",
        badgeColorHex = 0xFF059669 // Emerald
    ),
    PICKUP_SCHEDULED(
        displayName = "Pickup Scheduled",
        stepIndex = 3,
        description = "A delivery executive has been assigned to pick up the item.",
        badgeColorHex = 0xFF2563EB // Blue
    ),
    PICKED_UP(
        displayName = "Picked Up",
        stepIndex = 4,
        description = "Item collected from your address and in transit to hub.",
        badgeColorHex = 0xFF0D9488 // Teal
    ),
    PRODUCT_RECEIVED(
        displayName = "Product Received",
        stepIndex = 5,
        description = "Product received at SudhaniHub fulfillment center & verified.",
        badgeColorHex = 0xFF7C3AED // Purple
    ),
    REFUND_PROCESSING(
        displayName = "Refund Processing",
        stepIndex = 6,
        description = "Refund has been approved and sent to bank/wallet gateway.",
        badgeColorHex = 0xFFEA580C // Orange
    ),
    REFUNDED(
        displayName = "Refund Completed",
        stepIndex = 7,
        description = "Refund credited successfully to customer account.",
        badgeColorHex = 0xFF16A34A // Green
    ),
    REJECTED(
        displayName = "Return Declined",
        stepIndex = -1,
        description = "Return request was not approved. Please see rejection reason.",
        badgeColorHex = 0xFFDC2626 // Red
    );

    val isTerminal: Boolean get() = this == REFUNDED || this == REJECTED
    val isPendingReview: Boolean get() = this == REQUESTED || this == UNDER_REVIEW
    val isApprovedOrActive: Boolean get() = this == APPROVED || this == PICKUP_SCHEDULED || this == PICKED_UP || this == PRODUCT_RECEIVED || this == REFUND_PROCESSING

    companion object {
        fun fromString(value: String): ReturnStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                REQUESTED
            }
        }
    }
}

data class ReturnPolicyConfig(
    val defaultReturnWindowDays: Int = 3,
    val isReturnEnabled: Boolean = true,
    val refundDeliveryFeeByDefault: Boolean = false,
    val requirePhotosForComplaints: Boolean = true,
    val requirePhotosForDamaged: Boolean = true,
    val policyNote: String = "Eligible items can be returned within 3 days of delivery in original condition.",
    val disabledReasons: List<String> = emptyList()
)

object ReturnReasons {
    val ALL_REASONS = listOf(
        "Product not needed anymore",
        "Changed my mind",
        "Ordered by mistake",
        "Ordered wrong product",
        "Wrong product received",
        "Different product received",
        "Product is damaged",
        "Product is broken",
        "Product is defective",
        "Product is expired",
        "Product is near expiry",
        "Product quality is not good",
        "Product is spoiled",
        "Product is leaking",
        "Product package is damaged",
        "Product packaging is opened",
        "Product seal is broken",
        "Missing item from order",
        "Product quantity is less than ordered",
        "Product is different from the description",
        "Product does not match the image",
        "Size is incorrect",
        "Fit/size issue",
        "Color is different",
        "Taste is not as expected",
        "Freshness issue",
        "Wrong size/variant received",
        "Accessories or parts are missing",
        "Product has a manufacturing issue",
        "Product arrived in poor condition",
        "Delivery was delayed and product is no longer required",
        "Received duplicate product",
        "Price/offer issue",
        "Other"
    )

    private val FRESH_AND_GROCERY_CATEGORIES = setOf("fruits_veg", "dairy_breakfast", "grocery", "snacks", "beverages")
    private val APPAREL_OR_VARIANT_CATEGORIES = setOf("stationery", "mobile_acc", "personal_care", "home_care", "baby_care")

    fun getReasonsForCategory(categoryId: String, disabledReasons: List<String> = emptyList()): List<String> {
        val filtered = ALL_REASONS.filter { reason ->
            if (reason in disabledReasons) return@filter false

            when {
                // Grocery & perishable reasons
                reason in listOf("Freshness issue", "Product is spoiled", "Taste is not as expected", "Product is expired", "Product is near expiry", "Product is leaking") -> {
                    categoryId in FRESH_AND_GROCERY_CATEGORIES
                }
                // Size & variant reasons
                reason in listOf("Size is incorrect", "Fit/size issue", "Color is different", "Wrong size/variant received") -> {
                    categoryId !in FRESH_AND_GROCERY_CATEGORIES
                }
                // Electronics/accessories
                reason in listOf("Accessories or parts are missing", "Product has a manufacturing issue") -> {
                    categoryId in setOf("mobile_acc", "stationery", "home_care")
                }
                else -> true
            }
        }
        return if (filtered.isEmpty()) ALL_REASONS.filterNot { it in disabledReasons } else filtered
    }

    fun isPhotoRequired(reason: String): Boolean {
        val nonPhotoReasons = setOf(
            "Product not needed anymore",
            "Changed my mind",
            "Ordered by mistake",
            "Ordered wrong product",
            "Delivery was delayed and product is no longer required",
            "Price/offer issue"
        )
        return reason !in nonPhotoReasons
    }
}

data class ReturnRequest(
    val returnId: String,
    val orderId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val productUnit: String = "1 unit",
    val returnQuantity: Int,
    val unitPrice: Double,
    val itemAmount: Double = unitPrice * returnQuantity,
    val refundAmount: Double = itemAmount,
    val isDeliveryFeeRefunded: Boolean = false,
    val deliveryFeeRefundAmount: Double = 0.0,
    val refundMethod: String = "Original Payment Method",
    val refundStatus: String = "Pending", // Pending, Processing, Completed, Rejected
    val refundDate: Long? = null,
    val refundTransactionRef: String? = null,
    val orderCreatedAt: Long,
    val deliveredAt: Long,
    val requestedAt: Long = System.currentTimeMillis(),
    val returnDeadline: Long,
    val reason: String,
    val customerDescription: String = "",
    val photos: List<String> = emptyList(),
    val status: ReturnStatus = ReturnStatus.REQUESTED,
    val rejectionReason: String? = null,
    val pickupDate: Long? = null,
    val pickupTimeSlot: String? = null,
    val pickupPartnerName: String? = null,
    val pickupPartnerPhone: String? = null,
    val adminNotes: String? = null,
    val updatedAt: Long = requestedAt
) {
    val totalRefund: Double get() = refundAmount + if (isDeliveryFeeRefunded) deliveryFeeRefundAmount else 0.0
    val isExpired: Boolean get() = System.currentTimeMillis() > returnDeadline
}

data class CustomerNotification(
    val id: String = "NOTIF-${System.currentTimeMillis()}-${(100..999).random()}",
    val customerId: String,
    val title: String,
    val message: String,
    val type: String = "RETURN_STATUS",
    val referenceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

