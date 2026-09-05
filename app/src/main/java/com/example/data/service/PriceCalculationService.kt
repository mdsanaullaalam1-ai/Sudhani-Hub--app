package com.example.data.service

import com.example.data.model.Coupon
import com.example.data.model.DeliveryArea
import com.example.data.model.DeliveryPricingConfig
import com.example.data.model.DiscountType
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

data class SmartDeliveryFeeResult(
    val deliveryFee: Double,
    val isFreeDelivery: Boolean,
    val freeDeliveryReason: String? = null,
    val isDeliverable: Boolean = true,
    val rejectionReason: String? = null,
    val explanation: String = "Delivery charge calculated based on distance and order weight.",
    val distanceKm: Double = 0.0,
    val totalWeightKg: Double = 0.0
)

data class PriceBreakdown(
    val subtotal: Double,
    val deliveryFee: Double,
    val isFreeDelivery: Boolean,
    val discount: Double,
    val couponCode: String?,
    val finalTotal: Double,
    val amountNeededForFreeDelivery: Double,
    val eligibleForDiscountBonus: Boolean,
    val deliveryArea: DeliveryArea? = null,
    val isMinimumOrderMet: Boolean = true,
    val minimumOrderAmount: Double = 0.0,
    val totalWeightKg: Double = 0.0,
    val deliveryDistanceKm: Double = 0.0,
    val isDistanceValid: Boolean = true,
    val isServiceable: Boolean = true,
    val serviceabilityMessage: String? = null,
    val freeDeliveryReason: String? = null,
    val deliveryFeeExplanation: String = "Delivery charge calculated based on distance and order weight."
)

object PriceCalculationService {
    const val BONUS_DISCOUNT_THRESHOLD = 499.0

    /**
     * Calculates delivery fee based on:
     * 1. Delivery Area
     * 2. Customer to delivery location distance in KM
     * 3. Total order weight in KG
     * 4. Free Delivery rules (e.g. Orders > ₹999 within 7 KM)
     * 5. Admin pricing configuration overrides
     */
    fun calculateSmartDeliveryFee(
        distanceKm: Double?,
        totalWeightKg: Double,
        subtotal: Double,
        deliveryArea: DeliveryArea? = null,
        pricingConfig: DeliveryPricingConfig = DeliveryPricingConfig()
    ): SmartDeliveryFeeResult {
        val safeSubtotal = subtotal.coerceAtLeast(0.0)
        val safeWeight = (if (totalWeightKg <= 0.0) 1.0 else totalWeightKg).coerceAtLeast(0.1)

        // 1. Check if area is inactive
        if (deliveryArea != null && !deliveryArea.isActive) {
            return SmartDeliveryFeeResult(
                deliveryFee = 0.0,
                isFreeDelivery = false,
                isDeliverable = false,
                rejectionReason = "We are currently not delivering to ${deliveryArea.areaName} (${deliveryArea.pincode}).",
                distanceKm = distanceKm ?: deliveryArea.defaultDistanceKm,
                totalWeightKg = safeWeight
            )
        }

        // 2. Validate distance
        val resolvedDistance = when {
            distanceKm != null && distanceKm > 0.0 -> distanceKm
            deliveryArea != null && deliveryArea.defaultDistanceKm > 0.0 -> deliveryArea.defaultDistanceKm
            else -> null
        }

        if (resolvedDistance == null || resolvedDistance <= 0.0) {
            return SmartDeliveryFeeResult(
                deliveryFee = 35.0,
                isFreeDelivery = false,
                isDeliverable = false,
                rejectionReason = "Cannot calculate delivery distance. Please select a valid delivery address.",
                distanceKm = 0.0,
                totalWeightKg = safeWeight
            )
        }

        // 3. Check maximum delivery radius
        val maxAllowedRadius = deliveryArea?.maxDistanceKm?.takeIf { it > 0 } ?: pricingConfig.maxDeliveryRadiusKm
        if (resolvedDistance > maxAllowedRadius) {
            val formattedDist = String.format(Locale.US, "%.1f", resolvedDistance)
            return SmartDeliveryFeeResult(
                deliveryFee = 0.0,
                isFreeDelivery = false,
                isDeliverable = false,
                rejectionReason = "Delivery location ($formattedDist KM) is beyond our maximum delivery radius (${maxAllowedRadius.toInt()} KM). Please choose a closer location.",
                distanceKm = resolvedDistance,
                totalWeightKg = safeWeight
            )
        }

        // 4. Check Free Delivery rule:
        // Default: Orders above ₹999 = FREE DELIVERY within 7 KM
        val freeMinOrder = deliveryArea?.freeDeliveryMinOrderAmount?.takeIf { it > 0 } ?: pricingConfig.freeDeliveryMinOrderAmount
        val freeMaxDist = pricingConfig.freeDeliveryMaxDistanceKm

        if (safeSubtotal >= freeMinOrder && resolvedDistance <= freeMaxDist) {
            return SmartDeliveryFeeResult(
                deliveryFee = 0.0,
                isFreeDelivery = true,
                freeDeliveryReason = "Free Delivery unlocked on orders above ₹${freeMinOrder.toInt()} within ${freeMaxDist.toInt()} KM!",
                isDeliverable = true,
                explanation = "Delivery charge calculated based on distance and order weight.",
                distanceKm = resolvedDistance,
                totalWeightKg = safeWeight
            )
        }

        // 5. Admin manual override if specified
        if (deliveryArea?.manualOverrideFee != null && deliveryArea.manualOverrideFee >= 0.0) {
            return SmartDeliveryFeeResult(
                deliveryFee = deliveryArea.manualOverrideFee,
                isFreeDelivery = deliveryArea.manualOverrideFee == 0.0,
                isDeliverable = true,
                explanation = "Custom delivery charge applied for ${deliveryArea.areaName}.",
                distanceKm = resolvedDistance,
                totalWeightKg = safeWeight
            )
        }

        if (pricingConfig.globalManualOverrideFee != null && pricingConfig.globalManualOverrideFee >= 0.0) {
            return SmartDeliveryFeeResult(
                deliveryFee = pricingConfig.globalManualOverrideFee,
                isFreeDelivery = pricingConfig.globalManualOverrideFee == 0.0,
                isDeliverable = true,
                explanation = "Delivery charge calculated based on distance and order weight.",
                distanceKm = resolvedDistance,
                totalWeightKg = safeWeight
            )
        }

        // 6. Calculate fee by distance slab and total order weight
        val calculatedFee = when {
            resolvedDistance <= pricingConfig.slab0To3.maxKm -> {
                pricingConfig.slab0To3.calculateFeeForWeight(safeWeight)
            }
            resolvedDistance <= pricingConfig.slab3To7.maxKm -> {
                pricingConfig.slab3To7.calculateFeeForWeight(safeWeight)
            }
            resolvedDistance <= pricingConfig.slab7To12.maxKm -> {
                pricingConfig.slab7To12.calculateFeeForWeight(safeWeight)
            }
            resolvedDistance <= pricingConfig.slab12To20.maxKm -> {
                pricingConfig.slab12To20.calculateFeeForWeight(safeWeight)
            }
            else -> {
                pricingConfig.beyond20Km.calculateFee(resolvedDistance, safeWeight)
            }
        }

        val roundedFee = ceil(calculatedFee).coerceAtLeast(20.0)

        return SmartDeliveryFeeResult(
            deliveryFee = roundedFee,
            isFreeDelivery = false,
            freeDeliveryReason = null,
            isDeliverable = true,
            explanation = "Delivery charge calculated based on distance and order weight.",
            distanceKm = resolvedDistance,
            totalWeightKg = safeWeight
        )
    }

    fun calculateDiscount(subtotal: Double, coupon: Coupon?): Double {
        if (coupon == null || !coupon.isActive || subtotal < coupon.minimumOrder) {
            return 0.0
        }
        val calculated = when (coupon.discountType) {
            DiscountType.FLAT -> coupon.discountValue
            DiscountType.PERCENTAGE -> (subtotal * coupon.discountValue / 100.0)
        }
        val capped = if (coupon.maximumDiscount != null && coupon.maximumDiscount > 0) {
            calculated.coerceAtMost(coupon.maximumDiscount)
        } else {
            calculated
        }
        return capped.coerceAtMost(subtotal).coerceAtLeast(0.0)
    }

    /**
     * Primary price calculation method supporting Distance + Weight calculation.
     */
    fun calculatePrice(
        subtotal: Double,
        coupon: Coupon?,
        deliveryArea: DeliveryArea? = null,
        distanceKm: Double? = null,
        totalWeightKg: Double? = null,
        pricingConfig: DeliveryPricingConfig = DeliveryPricingConfig()
    ): PriceBreakdown {
        val safeSubtotal = subtotal.coerceAtLeast(0.0)
        val weight = (totalWeightKg ?: 1.0).coerceAtLeast(0.1)

        val deliveryResult = calculateSmartDeliveryFee(
            distanceKm = distanceKm,
            totalWeightKg = weight,
            subtotal = safeSubtotal,
            deliveryArea = deliveryArea,
            pricingConfig = pricingConfig
        )

        val deliveryFee = if (safeSubtotal <= 0.0) 0.0 else deliveryResult.deliveryFee
        val isFreeDelivery = deliveryResult.isFreeDelivery && safeSubtotal > 0.0
        val discount = calculateDiscount(safeSubtotal, coupon)
        val rawTotal = (safeSubtotal + deliveryFee - discount)
        val finalTotal = rawTotal.coerceAtLeast(0.0)

        val freeMinOrder = deliveryArea?.freeDeliveryMinOrderAmount?.takeIf { it > 0 } ?: pricingConfig.freeDeliveryMinOrderAmount
        val remainingForFree = if (deliveryResult.distanceKm <= pricingConfig.freeDeliveryMaxDistanceKm) {
            (freeMinOrder - safeSubtotal).coerceAtLeast(0.0)
        } else {
            0.0
        }

        val minOrder = deliveryArea?.minimumOrderAmount ?: 0.0
        val isMinimumMet = safeSubtotal >= minOrder

        return PriceBreakdown(
            subtotal = safeSubtotal,
            deliveryFee = deliveryFee,
            isFreeDelivery = isFreeDelivery,
            discount = discount,
            couponCode = if (discount > 0) coupon?.code else null,
            finalTotal = finalTotal,
            amountNeededForFreeDelivery = remainingForFree,
            eligibleForDiscountBonus = safeSubtotal >= BONUS_DISCOUNT_THRESHOLD,
            deliveryArea = deliveryArea,
            isMinimumOrderMet = isMinimumMet,
            minimumOrderAmount = minOrder,
            totalWeightKg = weight,
            deliveryDistanceKm = deliveryResult.distanceKm,
            isDistanceValid = deliveryResult.isDeliverable && deliveryResult.distanceKm > 0.0,
            isServiceable = deliveryResult.isDeliverable,
            serviceabilityMessage = deliveryResult.rejectionReason,
            freeDeliveryReason = deliveryResult.freeDeliveryReason,
            deliveryFeeExplanation = deliveryResult.explanation
        )
    }

    fun formatRupees(amount: Double): String {
        val intAmount = amount.roundToInt()
        val format = NumberFormat.getIntegerInstance(Locale("en", "IN"))
        return "₹${format.format(intAmount)}"
    }

    fun formatWeight(weightKg: Double): String {
        return if (weightKg < 1.0) {
            "${(weightKg * 1000).roundToInt()} g"
        } else {
            String.format(Locale.US, "%.1f KG", weightKg)
        }
    }

    fun formatDistance(distanceKm: Double): String {
        return String.format(Locale.US, "%.1f KM", distanceKm)
    }
}
