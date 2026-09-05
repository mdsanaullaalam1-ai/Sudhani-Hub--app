package com.example.data.repository

import com.example.data.local.dao.CouponDao
import com.example.data.local.entity.CouponEntity
import com.example.data.model.Coupon
import com.example.data.model.DiscountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class CouponValidationResult {
    data class Success(val coupon: Coupon, val discount: Double, val message: String) : CouponValidationResult()
    data class Error(val message: String) : CouponValidationResult()
}

class CouponRepository(private val couponDao: CouponDao) {

    fun getAllCoupons(): Flow<List<Coupon>> {
        return couponDao.getAllCoupons().map { list ->
            list.map { it.toDomainCoupon() }
        }
    }

    suspend fun getCouponByCode(code: String): Coupon? {
        return couponDao.getCouponByCode(code.trim().uppercase())?.toDomainCoupon()
    }

    suspend fun validateCoupon(
        code: String,
        subtotal: Double,
        customerUsedCodes: Set<String> = emptySet()
    ): CouponValidationResult {
        val cleanCode = code.trim().uppercase()
        val couponEntity = couponDao.getCouponByCode(cleanCode)
            ?: return CouponValidationResult.Error("Coupon is not valid.")

        val coupon = couponEntity.toDomainCoupon()

        if (!coupon.isActive) {
            return CouponValidationResult.Error("Coupon is no longer active.")
        }

        if (coupon.oneTimePerCustomer && customerUsedCodes.contains(cleanCode)) {
            return CouponValidationResult.Error("Coupon already used.")
        }

        if (subtotal < coupon.minimumOrder) {
            val minInt = coupon.minimumOrder.toInt()
            return CouponValidationResult.Error("Minimum order value is ₹$minInt.")
        }

        val discount = when (coupon.discountType) {
            DiscountType.FLAT -> coupon.discountValue
            DiscountType.PERCENTAGE -> (subtotal * coupon.discountValue / 100.0)
        }
        val cappedDiscount = if (coupon.maximumDiscount != null && coupon.maximumDiscount > 0) {
            discount.coerceAtMost(coupon.maximumDiscount)
        } else {
            discount
        }.coerceAtMost(subtotal)

        val discountInt = cappedDiscount.toInt()
        return CouponValidationResult.Success(
            coupon = coupon,
            discount = cappedDiscount,
            message = "✓ ${coupon.code} applied. You saved ₹$discountInt!"
        )
    }

    suspend fun addCoupon(coupon: Coupon) {
        couponDao.insertCoupon(coupon.toEntity())
    }

    suspend fun updateCoupon(coupon: Coupon) {
        couponDao.updateCoupon(coupon.toEntity())
    }

    suspend fun toggleCouponStatus(code: String, isActive: Boolean) {
        couponDao.setCouponActive(code, isActive)
    }

    private fun CouponEntity.toDomainCoupon(): Coupon {
        return Coupon(
            code = code,
            discountType = if (discountType == "PERCENTAGE") DiscountType.PERCENTAGE else DiscountType.FLAT,
            discountValue = discountValue,
            minimumOrder = minimumOrder,
            maximumDiscount = maximumDiscount,
            isActive = isActive,
            expiryDate = expiryDate,
            oneTimePerCustomer = oneTimePerCustomer,
            usageLimit = usageLimit,
            timesUsed = timesUsed,
            description = description
        )
    }

    private fun Coupon.toEntity(): CouponEntity {
        return CouponEntity(
            code = code.uppercase(),
            discountType = discountType.name,
            discountValue = discountValue,
            minimumOrder = minimumOrder,
            maximumDiscount = maximumDiscount,
            isActive = isActive,
            expiryDate = expiryDate,
            oneTimePerCustomer = oneTimePerCustomer,
            usageLimit = usageLimit,
            timesUsed = timesUsed,
            description = description
        )
    }
}
