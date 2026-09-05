package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY registeredAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("UPDATE customers SET isActive = :isActive WHERE customerId = :customerId")
    suspend fun updateCustomerStatus(customerId: String, isActive: Boolean)

    @Query("UPDATE customers SET totalOrders = totalOrders + 1, totalSpending = totalSpending + :orderAmount WHERE customerId = :customerId")
    suspend fun incrementCustomerOrderStats(customerId: String, orderAmount: Double)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}
