package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SavedPaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPaymentMethodDao {

    @Query("SELECT * FROM saved_payment_methods ORDER BY isDefault DESC, createdAt DESC")
    fun getAllSavedPaymentMethods(): Flow<List<SavedPaymentMethodEntity>>

    @Query("SELECT * FROM saved_payment_methods WHERE customerId = :customerId ORDER BY isDefault DESC, createdAt DESC")
    fun getCustomerSavedPaymentMethods(customerId: String): Flow<List<SavedPaymentMethodEntity>>

    @Query("SELECT * FROM saved_payment_methods WHERE id = :id LIMIT 1")
    suspend fun getSavedPaymentMethodById(id: String): SavedPaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPaymentMethod(method: SavedPaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPaymentMethods(methods: List<SavedPaymentMethodEntity>)

    @Query("DELETE FROM saved_payment_methods WHERE id = :id")
    suspend fun deleteSavedPaymentMethod(id: String)

    @Query("UPDATE saved_payment_methods SET isDefault = CASE WHEN id = :id THEN 1 ELSE 0 END WHERE customerId = :customerId")
    suspend fun setDefaultPaymentMethod(id: String, customerId: String)

    @Query("SELECT COUNT(*) FROM saved_payment_methods")
    suspend fun getSavedPaymentMethodCount(): Int
}
