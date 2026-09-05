package com.example.data.model

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val badge: String? = null
)

enum class ListingStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK,
    HIDDEN
}

data class Product(
    val id: String,
    val categoryId: String,
    val name: String,
    val brand: String = "SudhaniHub",
    val description: String = "",
    val unit: String = "1 unit",
    val price: Double,
    val mrp: Double,
    val discountPercent: Int = if (mrp > 0) (((mrp - price) / mrp) * 100).toInt().coerceAtLeast(0) else 0,
    val stock: Int = 50,
    val rating: Double = 4.8,
    val reviewCount: Int = 142,
    val isPopular: Boolean = false,
    val isDeal: Boolean = false,
    val isRecentlyPurchased: Boolean = false,
    val emoji: String = "🛒",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isVisible: Boolean = true,
    val isReturnable: Boolean = true,
    val weightInGrams: Int = 0,
    val listingStatus: ListingStatus = if (stock <= 0) ListingStatus.OUT_OF_STOCK else if (isActive && isVisible) ListingStatus.ACTIVE else ListingStatus.INACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val productId: String get() = id
    val sellingPrice: Double get() = price
    val discount: Int get() = discountPercent
    val isOutOfStock: Boolean get() = stock <= 0 || listingStatus == ListingStatus.OUT_OF_STOCK
    val isPurchasable: Boolean get() = isActive && isVisible && stock > 0 && listingStatus == ListingStatus.ACTIVE

    fun calculateWeightInGrams(): Int {
        if (weightInGrams > 0) return weightInGrams
        return com.example.data.util.WeightParser.parseWeightInGrams(unit, name)
    }
}

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class Address(
    val id: String,
    val label: String, // "Home", "Work", "Other"
    val house: String,
    val street: String,
    val area: String,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean = false,
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val distanceKm: Double? = null
) {
    val fullAddress: String
        get() = "$house, $street, $area, $city - $pincode"
}

data class UserProfile(
    val id: String = "USR_101",
    val name: String = "Md Sanaulla",
    val phone: String = "9508700923",
    val email: String = "m8961091@gmail.com",
    val isVerified: Boolean = true,
    val role: UserRole = UserRole.CUSTOMER,
    val accountStatus: String = "ACTIVE",
    val profilePicturePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    val userId: String get() = id

    val initials: String
        get() {
            val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            return when {
                parts.isEmpty() -> "S"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts.first().first()}${parts.last().first()}".uppercase()
            }
        }
}

enum class UserRole {
    CUSTOMER,
    ADMIN,
    DELIVERY_PARTNER
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    PAID("Paid"),
    SUCCESS("Paid"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    companion object {
        fun fromString(value: String): PaymentStatus {
            return when (value.uppercase()) {
                "PAID" -> PAID
                "SUCCESS" -> SUCCESS
                "PROCESSING" -> PROCESSING
                "PENDING" -> PENDING
                "FAILED" -> FAILED
                "REFUNDED" -> REFUNDED
                else -> PENDING
            }
        }
    }
}

data class Payment(
    val paymentId: String,
    val orderId: String,
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentStatus: PaymentStatus,
    val transactionRef: String = "TXN_${System.currentTimeMillis().toString().takeLast(8)}",
    val createdAt: Long = System.currentTimeMillis()
)

data class Customer(
    val customerId: String,
    val name: String,
    val phone: String,
    val email: String,
    val totalOrders: Int = 0,
    val totalSpending: Double = 0.0,
    val registeredAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val role: UserRole = UserRole.CUSTOMER,
    val accountStatus: String = if (isActive) "ACTIVE" else "DEACTIVATED",
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    val userId: String get() = customerId
    val createdAt: Long get() = registeredAt
}

data class DeliveryPartner(
    val partnerId: String,
    val name: String,
    val phone: String,
    val vehicle: String,
    val isAvailable: Boolean = true,
    val currentOrderId: String? = null,
    val activeDeliveriesCount: Int = 0,
    val rating: Double = 4.85
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val isCredit: Boolean,
    val date: String,
    val description: String
)

data class ShoppingList(
    val id: String,
    val name: String,
    val description: String,
    val itemNames: List<String>,
    val estimatedTotal: Double,
    val iconEmoji: String = "🛒"
)

data class SavedPaymentMethod(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String, // "UPI", "DEBIT_CARD", "CREDIT_CARD", "CARD", "NETBANKING"
    val isDefault: Boolean = false,
    val customerId: String = "USR_101",
    val bankOrProvider: String = "",
    val maskedDetails: String = "",
    val lastFourDigits: String = "", // Only last 4 digits displayed, never full card or CVV
    val cardNetwork: String = "",    // "VISA", "MASTERCARD", "RUPAY"
    val cardExpiry: String = "",     // "MM/YY"
    val gatewayTokenRef: String = "",// Razorpay token reference
    val upiVpa: String = "",         // e.g. "sanaulla@oksbi"
    val createdAt: Long = System.currentTimeMillis()
)

data class ProductReview(
    val id: String,
    val productName: String,
    val productEmoji: String,
    val rating: Int,
    val date: String,
    val comment: String
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: String, // "ORDER", "OFFER", "WALLET", "SYSTEM"
    val isRead: Boolean = false
)

