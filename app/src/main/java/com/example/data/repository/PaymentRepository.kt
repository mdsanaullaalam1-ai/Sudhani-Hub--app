package com.example.data.repository

import com.example.data.local.dao.PaymentDao
import com.example.data.local.entity.PaymentEntity
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentRepository(private val paymentDao: PaymentDao) {

    fun getAllPayments(): Flow<List<Payment>> {
        return paymentDao.getAllPayments().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getCustomerPayments(customerId: String): Flow<List<Payment>> {
        return paymentDao.getCustomerPayments(customerId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun recordPayment(payment: Payment) {
        paymentDao.insertPayment(payment.toEntity())
    }

    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus) {
        paymentDao.updatePaymentStatus(paymentId, status.name)
    }

    private fun PaymentEntity.toModel(): Payment {
        return Payment(
            paymentId = paymentId,
            orderId = orderId,
            customerId = customerId,
            customerName = customerName,
            amount = amount,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.fromString(paymentStatus),
            transactionRef = transactionRef,
            createdAt = createdAt
        )
    }

    private fun Payment.toEntity(): PaymentEntity {
        return PaymentEntity(
            paymentId = paymentId,
            orderId = orderId,
            customerId = customerId,
            customerName = customerName,
            amount = amount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus.name,
            transactionRef = transactionRef,
            createdAt = createdAt
        )
    }
}
