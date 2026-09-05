package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReturnRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnRequestDao {
    @Query("SELECT * FROM return_requests ORDER BY requestedAt DESC")
    fun getAllReturnRequests(): Flow<List<ReturnRequestEntity>>

    @Query("SELECT * FROM return_requests WHERE customerId = :customerId ORDER BY requestedAt DESC")
    fun getCustomerReturnRequests(customerId: String): Flow<List<ReturnRequestEntity>>

    @Query("SELECT * FROM return_requests WHERE orderId = :orderId")
    fun getReturnRequestsForOrder(orderId: String): Flow<List<ReturnRequestEntity>>

    @Query("SELECT * FROM return_requests WHERE returnId = :returnId LIMIT 1")
    fun getReturnRequestById(returnId: String): Flow<ReturnRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnRequest(request: ReturnRequestEntity)

    @Update
    suspend fun updateReturnRequest(request: ReturnRequestEntity)

    @Query("UPDATE return_requests SET status = :status, rejectionReason = :rejectionReason, updatedAt = :updatedAt WHERE returnId = :returnId")
    suspend fun updateReturnStatus(returnId: String, status: String, rejectionReason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM return_requests WHERE returnId = :returnId")
    suspend fun deleteReturnRequest(returnId: String)
}
