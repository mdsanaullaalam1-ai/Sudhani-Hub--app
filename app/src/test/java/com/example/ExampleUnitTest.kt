package com.example

import com.example.data.model.Coupon
import com.example.data.model.DeliveryArea
import com.example.data.repository.SudhaniRepository
import com.example.data.service.PriceCalculationService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDeliveryFee_withDistanceSlab() {
    val breakdown = PriceCalculationService.calculatePrice(
        subtotal = 150.0,
        coupon = null,
        distanceKm = 2.0,
        totalWeightKg = 1.0
    )
    assertEquals(25.0, breakdown.deliveryFee, 0.01)
    assertFalse(breakdown.isFreeDelivery)
    assertEquals(175.0, breakdown.finalTotal, 0.01)
  }

  @Test
  fun testDeliveryFee_freeDeliveryAboveThreshold() {
    val breakdown = PriceCalculationService.calculatePrice(
        subtotal = 1000.0,
        coupon = null,
        distanceKm = 4.0,
        totalWeightKg = 1.0
    )
    assertEquals(0.0, breakdown.deliveryFee, 0.01)
    assertTrue(breakdown.isFreeDelivery)
    assertEquals(1000.0, breakdown.finalTotal, 0.01)
  }

  @Test
  fun testDeliveryFee_above499_hasBonus() {
    val breakdown = PriceCalculationService.calculatePrice(
        subtotal = 550.0,
        coupon = null,
        distanceKm = 2.0
    )
    assertTrue(breakdown.eligibleForDiscountBonus)
  }

  @Test
  fun testCoupon_discountCappedAtSubtotal() {
    val coupon = Coupon(
        code = "BIG999",
        discountType = com.example.data.model.DiscountType.FLAT,
        discountValue = 1000.0,
        minimumOrder = 0.0,
        description = "Huge discount"
    )
    val discount = PriceCalculationService.calculateDiscount(subtotal = 200.0, coupon = coupon)
    assertEquals(200.0, discount, 0.01)
  }

  @Test
  fun testAreaSpecific_customDeliveryFeeAndMinimumOrder() {
    val area = DeliveryArea(
        id = "area_test_1",
        pincode = "560001",
        areaName = "MG Road",
        city = "Bengaluru",
        state = "Karnataka",
        deliveryFee = 35.0,
        manualOverrideFee = 35.0,
        minimumOrderAmount = 250.0,
        freeDeliveryMinOrderAmount = 300.0,
        defaultDistanceKm = 4.0,
        estimatedDeliveryTime = "15-20 mins",
        isActive = true
    )

    // Subtotal 100 is below minimum order 250
    val breakdownBelowMin = PriceCalculationService.calculatePrice(subtotal = 100.0, coupon = null, deliveryArea = area)
    assertEquals(35.0, breakdownBelowMin.deliveryFee, 0.01)
    assertFalse(breakdownBelowMin.isMinimumOrderMet)
    assertEquals(250.0, breakdownBelowMin.minimumOrderAmount, 0.01)

    // Subtotal 300 meets minimum order and qualifies for free delivery
    val breakdownAboveMin = PriceCalculationService.calculatePrice(subtotal = 300.0, coupon = null, deliveryArea = area)
    assertEquals(0.0, breakdownAboveMin.deliveryFee, 0.01)
    assertTrue(breakdownAboveMin.isMinimumOrderMet)
    assertTrue(breakdownAboveMin.isFreeDelivery)
  }

  @Test
  fun testServiceabilityCheck_knownPincode() {
    val result560038 = SudhaniRepository.checkServiceability("560038")
    assertTrue(result560038.isServiceable)
    assertNotNull(result560038.deliveryArea)
    assertEquals("Indiranagar", result560038.deliveryArea?.areaName)

    val resultUnknown = SudhaniRepository.checkServiceability("999999")
    assertFalse(resultUnknown.isServiceable)
    assertNull(resultUnknown.deliveryArea)
  }
}
