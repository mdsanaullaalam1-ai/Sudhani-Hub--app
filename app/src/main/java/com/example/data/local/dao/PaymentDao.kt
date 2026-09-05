package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE orderId = :orderId LIMIT 1")
    suspend fun getPaymentByOrderId(orderId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerPayments(customerId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("UPDATE payments SET paymentStatus = :status WHERE paymentId = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String, status: String)

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getPaymentCount(): Int
}
