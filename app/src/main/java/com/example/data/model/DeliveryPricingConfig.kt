package com.example.data.model

/**
 * Slab rate for a given distance band (e.g. 0-3 KM, 3-7 KM, etc.)
 */
data class DistancePricingSlab(
    val minKm: Double,
    val maxKm: Double,
    val upToOneKg: Double,
    val oneToThreeKg: Double,
    val threeToFiveKg: Double,
    val baseAboveFiveKg: Double,
    val perExtraKgRate: Double
) {
    /**
     * Calculates delivery fee for a specific weight in KG within this distance slab
     */
    fun calculateFeeForWeight(weightKg: Double): Double {
        val safeWeight = weightKg.coerceAtLeast(0.0)
        return when {
            safeWeight <= 1.0 -> upToOneKg
            safeWeight <= 3.0 -> oneToThreeKg
            safeWeight <= 5.0 -> threeToFiveKg
            else -> {
                val extraKg = kotlin.math.ceil(safeWeight - 5.0).coerceAtLeast(1.0)
                baseAboveFiveKg + (extraKg * perExtraKgRate)
            }
        }
    }
}

/**
 * Beyond 20 KM Pricing:
 * ₹60 base fee for the first 3 KM + ₹8 per additional KM + weight charge
 */
data class Beyond20KmPricing(
    val baseFirst3KmFee: Double = 60.0,
    val perAdditionalKmRate: Double = 8.0,
    val weightUpToOneKg: Double = 0.0,
    val weightOneToThreeKg: Double = 15.0,
    val weightThreeToFiveKg: Double = 35.0,
    val weightAboveFiveBase: Double = 35.0,
    val weightPerExtraKg: Double = 15.0
) {
    fun calculateFee(distanceKm: Double, weightKg: Double): Double {
        val safeDistance = distanceKm.coerceAtLeast(20.0)
        val additionalKm = kotlin.math.max(0.0, safeDistance - 3.0)
        val distanceFee = baseFirst3KmFee + (additionalKm * perAdditionalKmRate)

        val safeWeight = weightKg.coerceAtLeast(0.0)
        val weightFee = when {
            safeWeight <= 1.0 -> weightUpToOneKg
            safeWeight <= 3.0 -> weightOneToThreeKg
            safeWeight <= 5.0 -> weightThreeToFiveKg
            else -> {
                val extraKg = kotlin.math.ceil(safeWeight - 5.0).coerceAtLeast(1.0)
                weightAboveFiveBase + (extraKg * weightPerExtraKg)
            }
        }
        return distanceFee + weightFee
    }
}

/**
 * Global Admin configurable delivery pricing rules.
 * All pricing values can be adjusted by Admin without code changes.
 */
data class DeliveryPricingConfig(
    val freeDeliveryMinOrderAmount: Double = 999.0,
    val freeDeliveryMaxDistanceKm: Double = 7.0,
    val maxDeliveryRadiusKm: Double = 30.0,
    val slab0To3: DistancePricingSlab = DistancePricingSlab(
        minKm = 0.0,
        maxKm = 3.0,
        upToOneKg = 25.0,
        oneToThreeKg = 35.0,
        threeToFiveKg = 50.0,
        baseAboveFiveKg = 50.0,
        perExtraKgRate = 8.0
    ),
    val slab3To7: DistancePricingSlab = DistancePricingSlab(
        minKm = 3.0,
        maxKm = 7.0,
        upToOneKg = 35.0,
        oneToThreeKg = 45.0,
        threeToFiveKg = 60.0,
        baseAboveFiveKg = 60.0,
        perExtraKgRate = 10.0
    ),
    val slab7To12: DistancePricingSlab = DistancePricingSlab(
        minKm = 7.0,
        maxKm = 12.0,
        upToOneKg = 45.0,
        oneToThreeKg = 55.0,
        threeToFiveKg = 70.0,
        baseAboveFiveKg = 70.0,
        perExtraKgRate = 12.0
    ),
    val slab12To20: DistancePricingSlab = DistancePricingSlab(
        minKm = 12.0,
        maxKm = 20.0,
        upToOneKg = 60.0,
        oneToThreeKg = 70.0,
        threeToFiveKg = 90.0,
        baseAboveFiveKg = 90.0,
        perExtraKgRate = 15.0
    ),
    val beyond20Km: Beyond20KmPricing = Beyond20KmPricing(),
    val globalManualOverrideFee: Double? = null
)
