package com.example.data.repository

import com.example.data.local.dao.CustomerDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.model.Customer
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepository(private val customerDao: CustomerDao) {

    fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveCustomer(customer: Customer) {
        customerDao.insertCustomer(customer.toEntity())
    }

    suspend fun toggleCustomerStatus(customerId: String, isActive: Boolean) {
        customerDao.updateCustomerStatus(customerId, isActive)
    }

    suspend fun incrementCustomerStats(customerId: String, orderAmount: Double) {
        customerDao.incrementCustomerOrderStats(customerId, orderAmount)
    }

    private fun CustomerEntity.toModel(): Customer {
        return Customer(
            customerId = customerId,
            name = name,
            phone = phone,
            email = email,
            totalOrders = totalOrders,
            totalSpending = totalSpending,
            registeredAt = registeredAt,
            isActive = isActive,
            role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.CUSTOMER }
        )
    }

    private fun Customer.toEntity(): CustomerEntity {
        return CustomerEntity(
            customerId = customerId,
            name = name,
            phone = phone,
            email = email,
            totalOrders = totalOrders,
            totalSpending = totalSpending,
            registeredAt = registeredAt,
            isActive = isActive,
            role = role.name
        )
    }
}
