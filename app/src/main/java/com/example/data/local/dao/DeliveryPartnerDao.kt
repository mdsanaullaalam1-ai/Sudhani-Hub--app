package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DeliveryPartnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryPartnerDao {
    @Query("SELECT * FROM delivery_partners ORDER BY name ASC")
    fun getAllPartners(): Flow<List<DeliveryPartnerEntity>>

    @Query("SELECT * FROM delivery_partners WHERE partnerId = :partnerId LIMIT 1")
    suspend fun getPartnerById(partnerId: String): DeliveryPartnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: DeliveryPartnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<DeliveryPartnerEntity>)

    @Query("UPDATE delivery_partners SET isAvailable = :isAvailable, currentOrderId = :currentOrderId, activeDeliveriesCount = :activeCount WHERE partnerId = :partnerId")
    suspend fun updatePartnerAssignment(partnerId: String, isAvailable: Boolean, currentOrderId: String?, activeCount: Int)

    @Query("SELECT COUNT(*) FROM delivery_partners")
    suspend fun getPartnerCount(): Int
}
