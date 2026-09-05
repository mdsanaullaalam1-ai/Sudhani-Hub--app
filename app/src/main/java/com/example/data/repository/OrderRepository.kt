package com.example.data.repository

import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.OrderWithItems
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(private val orderDao: OrderDao) {

    fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrdersWithItems().map { list ->
            list.map { it.toDomainOrder() }
        }
    }

    fun getCustomerOrders(customerId: String): Flow<List<Order>> {
        return orderDao.getCustomerOrders(customerId).map { list ->
            list.map { it.toDomainOrder() }
        }
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return orderDao.getOrderWithItemsById(orderId).map { it?.toDomainOrder() }
    }

    suspend fun createOrder(order: Order): Order {
        val orderEntity = OrderEntity(
            orderId = order.orderId,
            customerId = order.customerId,
            customerName = order.customerName,
            customerPhone = order.customerPhone,
            deliveryAddress = order.deliveryAddress,
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            discount = order.discount,
            couponCode = order.couponCode,
            totalAmount = order.totalAmount,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            orderStatus = order.orderStatus.name,
            createdAt = order.createdAt,
            deliveryPartnerId = order.deliveryPartnerId,
            deliveryPartnerName = order.deliveryPartnerName,
            deliveryPartnerPhone = order.deliveryPartnerPhone,
            deliveryPartnerVehicle = order.deliveryPartnerVehicle,
            estimatedDeliveryMinutes = order.estimatedDeliveryMinutes,
            deliveryOtp = order.deliveryOtp,
            deliveryArea = order.deliveryArea,
            pincode = order.pincode
        )
        orderDao.insertOrder(orderEntity)

        val itemEntities = order.items.map { item ->
            OrderItemEntity(
                orderId = order.orderId,
                productId = item.productId,
                productName = item.productName,
                productImage = item.productImage,
                quantity = item.quantity,
                price = item.price,
                total = item.total
            )
        }
        orderDao.insertOrderItems(itemEntities)
        return order
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        orderDao.updateOrderStatus(orderId, newStatus.name)
    }

    suspend fun updateDeliveryPartner(
        orderId: String,
        partnerId: String,
        partnerName: String,
        partnerPhone: String,
        partnerVehicle: String
    ) {
        orderDao.updateDeliveryPartner(orderId, partnerId, partnerName, partnerPhone, partnerVehicle)
    }

    private fun OrderWithItems.toDomainOrder(): Order {
        return Order(
            orderId = order.orderId,
            customerId = order.customerId,
            customerName = order.customerName,
            customerPhone = order.customerPhone,
            deliveryAddress = order.deliveryAddress,
            items = items.map {
                OrderItem(
                    productId = it.productId,
                    productName = it.productName,
                    productImage = it.productImage,
                    quantity = it.quantity,
                    price = it.price,
                    total = it.total
                )
            },
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            discount = order.discount,
            couponCode = order.couponCode,
            totalAmount = order.totalAmount,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            orderStatus = try {
                OrderStatus.valueOf(order.orderStatus)
            } catch (e: Exception) {
                OrderStatus.NEW
            },
            createdAt = order.createdAt,
            deliveryPartnerId = order.deliveryPartnerId,
            deliveryPartnerName = order.deliveryPartnerName ?: "Ramesh Verma",
            deliveryPartnerPhone = order.deliveryPartnerPhone ?: "+91 98765 43210",
            deliveryPartnerVehicle = order.deliveryPartnerVehicle ?: "Electric Scooter (DL-3S-4412)",
            estimatedDeliveryMinutes = order.estimatedDeliveryMinutes,
            deliveryOtp = order.deliveryOtp,
            deliveryArea = order.deliveryArea,
            pincode = order.pincode
        )
    }
}
