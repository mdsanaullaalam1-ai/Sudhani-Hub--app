package com.example.data.model

enum class OrderStatus(val displayName: String, val stepIndex: Int, val description: String) {
    NEW("Order Placed", 0, "Your order has been received"),
    CONFIRMED("Confirmed", 1, "Store confirmed your order"),
    PREPARING("Preparing", 2, "Items are being packed with care"),
    READY_FOR_PICKUP("Ready for Pickup", 3, "Packed and ready for rider"),
    OUT_FOR_DELIVERY("Out for Delivery", 4, "Rider is heading to your location"),
    DELIVERED("Delivered", 5, "Order delivered at your doorstep"),
    CANCELLED("Cancelled", -1, "Order has been cancelled");

    companion object {
        fun fromString(value: String): OrderStatus {
            return when (value.uppercase()) {
                "PLACED" -> NEW
                "PICKED_UP" -> READY_FOR_PICKUP
                else -> try {
                    valueOf(value.uppercase())
                } catch (e: Exception) {
                    NEW
                }
            }
        }
    }
}

data class Order(
    val orderId: String,
    val customerId: String = "USR_101",
    val customerName: String = "Rahul Sharma",
    val customerPhone: String = "+91 98765 12345",
    val deliveryAddress: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val totalAmount: Double,
    val couponCode: String? = null,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: OrderStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveryPartnerId: String? = "DP_101",
    val deliveryPartnerName: String = "Ramesh Verma",
    val deliveryPartnerPhone: String = "+91 98765 43210",
    val deliveryPartnerVehicle: String = "Electric Scooter (DL-3S-4412)",
    val estimatedDeliveryMinutes: Int = 11,
    val deliveryOtp: String = "4819",
    val deliveryArea: String? = null,
    val pincode: String? = null,
    val deliveryDistanceKm: Double? = null,
    val totalWeightKg: Double? = null,
    val isFreeDelivery: Boolean = false,
    val deliveredAt: Long? = if (orderStatus == OrderStatus.DELIVERED) createdAt + 25 * 60 * 1000L else null,
    val updatedAt: Long = createdAt
) {
    val id: String get() = orderId
    val total: Double get() = totalAmount

    fun calculateReturnDeadline(windowDays: Int = 3): Long? {
        val deliveryTime = deliveredAt ?: if (orderStatus == OrderStatus.DELIVERED) updatedAt else null
        return deliveryTime?.plus(windowDays * 24L * 60L * 60L * 1000L)
    }

    fun isReturnWindowOpen(windowDays: Int = 3, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (orderStatus != OrderStatus.DELIVERED) return false
        val deadline = calculateReturnDeadline(windowDays) ?: return false
        return currentTime <= deadline
    }

    fun getReturnDeadlineDisplay(windowDays: Int = 3): String {
        val deadline = calculateReturnDeadline(windowDays) ?: return ""
        val formatter = java.text.SimpleDateFormat("d MMMM, hh:mm a", java.util.Locale.getDefault())
        return "Return available until ${formatter.format(java.util.Date(deadline))}"
    }

    fun getRemainingReturnTimeDisplay(windowDays: Int = 3, currentTime: Long = System.currentTimeMillis()): String {
        val deadline = calculateReturnDeadline(windowDays) ?: return ""
        val diffMs = deadline - currentTime
        if (diffMs <= 0) return "Return period expired"

        val diffHours = diffMs / (1000L * 60L * 60L)
        val diffDays = diffHours / 24L
        val remHours = diffHours % 24L

        return when {
            diffDays >= 2 -> "Return available for $diffDays more days"
            diffDays == 1L -> "Return available for 1 more day ($remHours hrs left)"
            diffHours > 1 -> "Return window closing soon ($diffHours hours left)"
            else -> "Return window closing soon (< 1 hour left)"
        }
    }
}
