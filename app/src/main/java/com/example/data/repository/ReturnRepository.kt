package com.example.data.repository

import com.example.data.local.dao.ReturnRequestDao
import com.example.data.local.entity.ReturnRequestEntity
import com.example.data.model.ReturnRequest
import com.example.data.model.ReturnStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReturnRepository(private val returnDao: ReturnRequestDao) {

    fun getAllReturnRequests(): Flow<List<ReturnRequest>> {
        return returnDao.getAllReturnRequests().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getCustomerReturnRequests(customerId: String): Flow<List<ReturnRequest>> {
        return returnDao.getCustomerReturnRequests(customerId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getReturnRequestsForOrder(orderId: String): Flow<List<ReturnRequest>> {
        return returnDao.getReturnRequestsForOrder(orderId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveReturnRequest(request: ReturnRequest) {
        returnDao.insertReturnRequest(request.toEntity())
    }

    suspend fun updateReturnRequest(request: ReturnRequest) {
        returnDao.updateReturnRequest(request.toEntity())
    }

    suspend fun updateStatus(returnId: String, status: ReturnStatus, rejectionReason: String? = null) {
        returnDao.updateReturnStatus(returnId, status.name, rejectionReason, System.currentTimeMillis())
    }

    private fun ReturnRequestEntity.toDomain(): ReturnRequest {
        val photoList = if (photosCsv.isBlank()) emptyList() else photosCsv.split("|||").filter { it.isNotBlank() }
        return ReturnRequest(
            returnId = returnId,
            orderId = orderId,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            productId = productId,
            productName = productName,
            productImage = productImage,
            productUnit = productUnit,
            returnQuantity = returnQuantity,
            unitPrice = unitPrice,
            itemAmount = itemAmount,
            refundAmount = refundAmount,
            isDeliveryFeeRefunded = isDeliveryFeeRefunded,
            deliveryFeeRefundAmount = deliveryFeeRefundAmount,
            refundMethod = refundMethod,
            refundStatus = refundStatus,
            refundDate = refundDate,
            refundTransactionRef = refundTransactionRef,
            orderCreatedAt = orderCreatedAt,
            deliveredAt = deliveredAt,
            requestedAt = requestedAt,
            returnDeadline = returnDeadline,
            reason = reason,
            customerDescription = customerDescription,
            photos = photoList,
            status = ReturnStatus.fromString(status),
            rejectionReason = rejectionReason,
            pickupDate = pickupDate,
            pickupTimeSlot = pickupTimeSlot,
            pickupPartnerName = pickupPartnerName,
            pickupPartnerPhone = pickupPartnerPhone,
            adminNotes = adminNotes,
            updatedAt = updatedAt
        )
    }

    private fun ReturnRequest.toEntity(): ReturnRequestEntity {
        return ReturnRequestEntity(
            returnId = returnId,
            orderId = orderId,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            productId = productId,
            productName = productName,
            productImage = productImage,
            productUnit = productUnit,
            returnQuantity = returnQuantity,
            unitPrice = unitPrice,
            itemAmount = itemAmount,
            refundAmount = refundAmount,
            isDeliveryFeeRefunded = isDeliveryFeeRefunded,
            deliveryFeeRefundAmount = deliveryFeeRefundAmount,
            refundMethod = refundMethod,
            refundStatus = refundStatus,
            refundDate = refundDate,
            refundTransactionRef = refundTransactionRef,
            orderCreatedAt = orderCreatedAt,
            deliveredAt = deliveredAt,
            requestedAt = requestedAt,
            returnDeadline = returnDeadline,
            reason = reason,
            customerDescription = customerDescription,
            photosCsv = photos.joinToString("|||"),
            status = status.name,
            rejectionReason = rejectionReason,
            pickupDate = pickupDate,
            pickupTimeSlot = pickupTimeSlot,
            pickupPartnerName = pickupPartnerName,
            pickupPartnerPhone = pickupPartnerPhone,
            adminNotes = adminNotes,
            updatedAt = updatedAt
        )
    }
}
