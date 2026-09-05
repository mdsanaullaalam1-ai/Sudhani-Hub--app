package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OrderEntity::class,
        OrderItemEntity::class,
        CouponEntity::class,
        PaymentEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        DeliveryPartnerEntity::class,
        SavedPaymentMethodEntity::class,
        ReturnRequestEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun couponDao(): CouponDao
    abstract fun paymentDao(): PaymentDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun deliveryPartnerDao(): DeliveryPartnerDao
    abstract fun savedPaymentMethodDao(): SavedPaymentMethodDao
    abstract fun returnRequestDao(): ReturnRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudhanihub_v3.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedDatabase(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Also check if database is empty on start and seed if needed
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.couponDao().getCouponCount() == 0) {
                            seedDatabase(instance)
                        } else if (instance.savedPaymentMethodDao().getSavedPaymentMethodCount() == 0) {
                            seedSavedPaymentMethodsOnly(instance)
                        }
                    } catch (_: Exception) {}
                }

                instance
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val defaultCoupons = listOf(
                CouponEntity(
                    code = "WELCOME50",
                    discountType = "FLAT",
                    discountValue = 50.0,
                    minimumOrder = 299.0,
                    maximumDiscount = null,
                    isActive = true,
                    expiryDate = "31 Dec 2026",
                    oneTimePerCustomer = true,
                    usageLimit = 100,
                    timesUsed = 0,
                    description = "₹50 instant discount on orders above ₹299 (One-time use)"
                ),
                CouponEntity(
                    code = "SAVE10",
                    discountType = "PERCENTAGE",
                    discountValue = 10.0,
                    minimumOrder = 499.0,
                    maximumDiscount = 100.0,
                    isActive = true,
                    expiryDate = "31 Dec 2026",
                    oneTimePerCustomer = false,
                    usageLimit = 500,
                    timesUsed = 0,
                    description = "10% OFF up to ₹100 on orders above ₹499"
                ),
                CouponEntity(
                    code = "SAVE75",
                    discountType = "FLAT",
                    discountValue = 75.0,
                    minimumOrder = 699.0,
                    maximumDiscount = null,
                    isActive = true,
                    expiryDate = "31 Dec 2026",
                    oneTimePerCustomer = false,
                    usageLimit = 200,
                    timesUsed = 0,
                    description = "Flat ₹75 OFF on orders above ₹699"
                )
            )
            db.couponDao().insertCoupons(defaultCoupons)

            // Seed initial sample order
            if (db.orderDao().getOrderCount() == 0) {
                val initialOrderId = "SH-78219"
                val orderEntity = OrderEntity(
                    orderId = initialOrderId,
                    customerId = "USR_101",
                    customerName = "Rahul Sharma",
                    customerPhone = "+91 98765 12345",
                    deliveryAddress = "Flat 402, Royal Palms Apartments, 12th Main Road, Sector 4, HSR Layout, Bengaluru - 560102",
                    subtotal = 351.0,
                    deliveryFee = 0.0,
                    discount = 50.0,
                    couponCode = "WELCOME50",
                    totalAmount = 301.0,
                    paymentMethod = "Cash on Delivery",
                    paymentStatus = "Paid",
                    orderStatus = "DELIVERED",
                    createdAt = System.currentTimeMillis() - 86400000L,
                    deliveryPartnerId = "DP_101",
                    deliveryPartnerName = "Ramesh Verma",
                    deliveryPartnerPhone = "+91 98765 43210",
                    deliveryPartnerVehicle = "Electric Scooter (DL-3S-4412)",
                    estimatedDeliveryMinutes = 11,
                    deliveryOtp = "4819",
                    deliveryArea = "HSR Layout",
                    pincode = "560102"
                )
                db.orderDao().insertOrder(orderEntity)

                val initialItems = listOf(
                    OrderItemEntity(
                        orderId = initialOrderId,
                        productId = "db_1",
                        productName = "Amul Taaza Homogenised Toned Milk",
                        productImage = "🥛",
                        quantity = 2,
                        price = 56.0,
                        total = 112.0
                    ),
                    OrderItemEntity(
                        orderId = initialOrderId,
                        productId = "gr_1",
                        productName = "Aashirvaad Shudh Chakki Atta",
                        productImage = "🌾",
                        quantity = 1,
                        price = 239.0,
                        total = 239.0
                    )
                )
                db.orderDao().insertOrderItems(initialItems)

                // Seed corresponding payment
                val initialPayment = PaymentEntity(
                    paymentId = "PAY-78219",
                    orderId = initialOrderId,
                    customerId = "USR_101",
                    customerName = "Rahul Sharma",
                    amount = 301.0,
                    paymentMethod = "Cash on Delivery",
                    paymentStatus = "SUCCESS",
                    transactionRef = "TXN_78219001",
                    createdAt = System.currentTimeMillis() - 86400000L
                )
                db.paymentDao().insertPayment(initialPayment)
            }

            // Seed initial customer
            if (db.customerDao().getCustomerCount() == 0) {
                val customers = listOf(
                    CustomerEntity(
                        customerId = "USR_101",
                        name = "Rahul Sharma",
                        phone = "+91 98765 12345",
                        email = "rahul.sharma@example.com",
                        totalOrders = 1,
                        totalSpending = 301.0,
                        registeredAt = System.currentTimeMillis() - (86400000L * 15),
                        isActive = true,
                        role = "CUSTOMER"
                    ),
                    CustomerEntity(
                        customerId = "USR_ADMIN",
                        name = "Sudhani Admin",
                        phone = "+91 99999 00000",
                        email = "admin@sudhanihub.com",
                        totalOrders = 0,
                        totalSpending = 0.0,
                        registeredAt = System.currentTimeMillis() - (86400000L * 60),
                        isActive = true,
                        role = "ADMIN"
                    )
                )
                db.customerDao().insertCustomers(customers)
            }

            // Seed delivery partners
            if (db.deliveryPartnerDao().getPartnerCount() == 0) {
                val partners = listOf(
                    DeliveryPartnerEntity(
                        partnerId = "DP_101",
                        name = "Ramesh Verma",
                        phone = "+91 98765 43210",
                        vehicle = "Electric Scooter (DL-3S-4412)",
                        isAvailable = true,
                        currentOrderId = null,
                        activeDeliveriesCount = 0,
                        rating = 4.92
                    ),
                    DeliveryPartnerEntity(
                        partnerId = "DP_102",
                        name = "Suresh Kumar",
                        phone = "+91 98765 43211",
                        vehicle = "Hero Electric Optima (KA-05-9821)",
                        isAvailable = true,
                        currentOrderId = null,
                        activeDeliveriesCount = 0,
                        rating = 4.88
                    ),
                    DeliveryPartnerEntity(
                        partnerId = "DP_103",
                        name = "Ankit Sharma",
                        phone = "+91 98765 43212",
                        vehicle = "Ather 450X (KA-03-7741)",
                        isAvailable = true,
                        currentOrderId = null,
                        activeDeliveriesCount = 0,
                        rating = 4.95
                    )
                )
                db.deliveryPartnerDao().insertPartners(partners)
            }

            if (db.savedPaymentMethodDao().getSavedPaymentMethodCount() == 0) {
                seedSavedPaymentMethodsOnly(db)
            }
        }

        private suspend fun seedSavedPaymentMethodsOnly(db: AppDatabase) {
                val defaultPaymentMethods = listOf(
                    SavedPaymentMethodEntity(
                        id = "SPM_GPAY_01",
                        customerId = "USR_101",
                        title = "Google Pay UPI",
                        subtitle = "sanaulla@oksbi",
                        type = "UPI",
                        bankOrProvider = "Google Pay",
                        maskedDetails = "sanaulla@oksbi",
                        upiVpa = "sanaulla@oksbi",
                        isDefault = true,
                        createdAt = System.currentTimeMillis() - 86400000 * 2
                    ),
                    SavedPaymentMethodEntity(
                        id = "SPM_PHONEPE_02",
                        customerId = "USR_101",
                        title = "PhonePe UPI",
                        subtitle = "9508700923@ybl",
                        type = "UPI",
                        bankOrProvider = "PhonePe",
                        maskedDetails = "9508700923@ybl",
                        upiVpa = "9508700923@ybl",
                        isDefault = false,
                        createdAt = System.currentTimeMillis() - 86400000
                    ),
                    SavedPaymentMethodEntity(
                        id = "SPM_CARD_01",
                        customerId = "USR_101",
                        title = "HDFC Bank Platinum Debit Card",
                        subtitle = "•••• •••• •••• 4921",
                        type = "DEBIT_CARD",
                        bankOrProvider = "HDFC Bank",
                        maskedDetails = "•••• •••• •••• 4921",
                        lastFourDigits = "4921",
                        cardNetwork = "VISA",
                        cardExpiry = "08/28",
                        gatewayTokenRef = "tok_rzp_hdfc_4921",
                        isDefault = false,
                        createdAt = System.currentTimeMillis() - 3600000 * 12
                    ),
                    SavedPaymentMethodEntity(
                        id = "SPM_CARD_02",
                        customerId = "USR_101",
                        title = "ICICI Amazon Pay Credit Card",
                        subtitle = "•••• •••• •••• 8014",
                        type = "CREDIT_CARD",
                        bankOrProvider = "ICICI Bank",
                        maskedDetails = "•••• •••• •••• 8014",
                        lastFourDigits = "8014",
                        cardNetwork = "MASTERCARD",
                        cardExpiry = "11/29",
                        gatewayTokenRef = "tok_rzp_icici_8014",
                        isDefault = false,
                        createdAt = System.currentTimeMillis() - 3600000 * 6
                    )
                )
                db.savedPaymentMethodDao().insertSavedPaymentMethods(defaultPaymentMethods)
        }
    }
}
