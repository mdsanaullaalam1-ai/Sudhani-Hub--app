package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CouponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Query("UPDATE coupons SET isActive = :isActive WHERE code = :code")
    suspend fun setCouponActive(code: String, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM coupons")
    suspend fun getCouponCount(): Int
}
