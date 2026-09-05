package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.*

class SudhaniApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val orderRepository: OrderRepository by lazy { OrderRepository(database.orderDao()) }
    val couponRepository: CouponRepository by lazy { CouponRepository(database.couponDao()) }
    val paymentRepository: PaymentRepository by lazy { PaymentRepository(database.paymentDao()) }
    val customerRepository: CustomerRepository by lazy { CustomerRepository(database.customerDao()) }
    val deliveryPartnerRepository: DeliveryPartnerRepository by lazy { DeliveryPartnerRepository(database.deliveryPartnerDao()) }
    val productRepository: ProductRepository by lazy { ProductRepository(database.productDao()) }
    val savedPaymentMethodRepository: SavedPaymentMethodRepository by lazy { SavedPaymentMethodRepository(database.savedPaymentMethodDao()) }
    val returnRepository: ReturnRepository by lazy { ReturnRepository(database.returnRequestDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.example.ui.theme.ThemeManager.init(this)
        com.example.data.firebase.FirebaseConfig.initialize(this)
        SudhaniRepository.init(
            context = this,
            orderRepo = orderRepository,
            couponRepo = couponRepository,
            paymentRepo = paymentRepository,
            customerRepo = customerRepository,
            partnerRepo = deliveryPartnerRepository,
            productRepo = productRepository,
            savedPaymentRepo = savedPaymentMethodRepository,
            returnRepo = returnRepository
        )
    }

    companion object {
        lateinit var instance: SudhaniApplication
            private set
    }
}
