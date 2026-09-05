package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Beyond20KmPricing
import com.example.data.model.DeliveryPricingConfig
import com.example.data.model.DistancePricingSlab
import com.example.data.repository.SudhaniRepository
import com.example.data.service.PriceCalculationService
import com.example.ui.theme.*

@Composable
fun SmartDeliveryPricingConfigContent(
    modifier: Modifier = Modifier
) {
    val currentConfig by SudhaniRepository.deliveryPricingConfig.collectAsState()

    // Editable state fields
    var tier1_1kg by remember(currentConfig) { mutableStateOf(currentConfig.slab0To3.upToOneKg.toInt().toString()) }
    var tier1_1to3kg by remember(currentConfig) { mutableStateOf(currentConfig.slab0To3.oneToThreeKg.toInt().toString()) }
    var tier1_3to5kg by remember(currentConfig) { mutableStateOf(currentConfig.slab0To3.threeToFiveKg.toInt().toString()) }
    var tier1_above5kg_rate by remember(currentConfig) { mutableStateOf(currentConfig.slab0To3.perExtraKgRate.toInt().toString()) }

    var tier2_1kg by remember(currentConfig) { mutableStateOf(currentConfig.slab3To7.upToOneKg.toInt().toString()) }
    var tier2_1to3kg by remember(currentConfig) { mutableStateOf(currentConfig.slab3To7.oneToThreeKg.toInt().toString()) }
    var tier2_3to5kg by remember(currentConfig) { mutableStateOf(currentConfig.slab3To7.threeToFiveKg.toInt().toString()) }
    var tier2_above5kg_rate by remember(currentConfig) { mutableStateOf(currentConfig.slab3To7.perExtraKgRate.toInt().toString()) }

    var tier3_1kg by remember(currentConfig) { mutableStateOf(currentConfig.slab7To12.upToOneKg.toInt().toString()) }
    var tier3_1to3kg by remember(currentConfig) { mutableStateOf(currentConfig.slab7To12.oneToThreeKg.toInt().toString()) }
    var tier3_3to5kg by remember(currentConfig) { mutableStateOf(currentConfig.slab7To12.threeToFiveKg.toInt().toString()) }
    var tier3_above5kg_rate by remember(currentConfig) { mutableStateOf(currentConfig.slab7To12.perExtraKgRate.toInt().toString()) }

    var tier4_1kg by remember(currentConfig) { mutableStateOf(currentConfig.slab12To20.upToOneKg.toInt().toString()) }
    var tier4_1to3kg by remember(currentConfig) { mutableStateOf(currentConfig.slab12To20.oneToThreeKg.toInt().toString()) }
    var tier4_3to5kg by remember(currentConfig) { mutableStateOf(currentConfig.slab12To20.threeToFiveKg.toInt().toString()) }
    var tier4_above5kg_rate by remember(currentConfig) { mutableStateOf(currentConfig.slab12To20.perExtraKgRate.toInt().toString()) }

    var tier5_base by remember(currentConfig) { mutableStateOf(currentConfig.beyond20Km.baseFirst3KmFee.toInt().toString()) }
    var tier5_per_km by remember(currentConfig) { mutableStateOf(currentConfig.beyond20Km.perAdditionalKmRate.toInt().toString()) }

    var freeDeliveryMinOrder by remember(currentConfig) { mutableStateOf(currentConfig.freeDeliveryMinOrderAmount.toInt().toString()) }
    var freeDeliveryMaxDist by remember(currentConfig) { mutableStateOf(currentConfig.freeDeliveryMaxDistanceKm.toInt().toString()) }
    var maxServiceableRadius by remember(currentConfig) { mutableStateOf(currentConfig.maxDeliveryRadiusKm.toInt().toString()) }

    var saveStatusMessage by remember { mutableStateOf<String?>(null) }

    // Simulator testing states
    var simDistance by remember { mutableStateOf("5.2") }
    var simWeight by remember { mutableStateOf("1.8") }
    var simSubtotal by remember { mutableStateOf("699") }

    // Live preview configuration built from inputs
    val previewConfig = remember(
        tier1_1kg, tier1_1to3kg, tier1_3to5kg, tier1_above5kg_rate,
        tier2_1kg, tier2_1to3kg, tier2_3to5kg, tier2_above5kg_rate,
        tier3_1kg, tier3_1to3kg, tier3_3to5kg, tier3_above5kg_rate,
        tier4_1kg, tier4_1to3kg, tier4_3to5kg, tier4_above5kg_rate,
        tier5_base, tier5_per_km,
        freeDeliveryMinOrder, freeDeliveryMaxDist,
        maxServiceableRadius
    ) {
        val t1_5kg = tier1_3to5kg.toDoubleOrNull() ?: 50.0
        val t2_5kg = tier2_3to5kg.toDoubleOrNull() ?: 60.0
        val t3_5kg = tier3_3to5kg.toDoubleOrNull() ?: 70.0
        val t4_5kg = tier4_3to5kg.toDoubleOrNull() ?: 90.0

        DeliveryPricingConfig(
            freeDeliveryMinOrderAmount = freeDeliveryMinOrder.toDoubleOrNull() ?: 999.0,
            freeDeliveryMaxDistanceKm = freeDeliveryMaxDist.toDoubleOrNull() ?: 7.0,
            maxDeliveryRadiusKm = maxServiceableRadius.toDoubleOrNull() ?: 30.0,
            slab0To3 = DistancePricingSlab(
                minKm = 0.0,
                maxKm = 3.0,
                upToOneKg = tier1_1kg.toDoubleOrNull() ?: 25.0,
                oneToThreeKg = tier1_1to3kg.toDoubleOrNull() ?: 35.0,
                threeToFiveKg = t1_5kg,
                baseAboveFiveKg = t1_5kg,
                perExtraKgRate = tier1_above5kg_rate.toDoubleOrNull() ?: 8.0
            ),
            slab3To7 = DistancePricingSlab(
                minKm = 3.0,
                maxKm = 7.0,
                upToOneKg = tier2_1kg.toDoubleOrNull() ?: 35.0,
                oneToThreeKg = tier2_1to3kg.toDoubleOrNull() ?: 45.0,
                threeToFiveKg = t2_5kg,
                baseAboveFiveKg = t2_5kg,
                perExtraKgRate = tier2_above5kg_rate.toDoubleOrNull() ?: 10.0
            ),
            slab7To12 = DistancePricingSlab(
                minKm = 7.0,
                maxKm = 12.0,
                upToOneKg = tier3_1kg.toDoubleOrNull() ?: 45.0,
                oneToThreeKg = tier3_1to3kg.toDoubleOrNull() ?: 55.0,
                threeToFiveKg = t3_5kg,
                baseAboveFiveKg = t3_5kg,
                perExtraKgRate = tier3_above5kg_rate.toDoubleOrNull() ?: 12.0
            ),
            slab12To20 = DistancePricingSlab(
                minKm = 12.0,
                maxKm = 20.0,
                upToOneKg = tier4_1kg.toDoubleOrNull() ?: 60.0,
                oneToThreeKg = tier4_1to3kg.toDoubleOrNull() ?: 70.0,
                threeToFiveKg = t4_5kg,
                baseAboveFiveKg = t4_5kg,
                perExtraKgRate = tier4_above5kg_rate.toDoubleOrNull() ?: 15.0
            ),
            beyond20Km = Beyond20KmPricing(
                baseFirst3KmFee = tier5_base.toDoubleOrNull() ?: 60.0,
                perAdditionalKmRate = tier5_per_km.toDoubleOrNull() ?: 8.0
            )
        )
    }

    val simDistVal = simDistance.toDoubleOrNull() ?: 0.0
    val simWeightVal = simWeight.toDoubleOrNull() ?: 0.0
    val simSubtotalVal = simSubtotal.toDoubleOrNull() ?: 0.0

    val simResult = PriceCalculationService.calculateSmartDeliveryFee(
        distanceKm = simDistVal,
        totalWeightKg = simWeightVal,
        subtotal = simSubtotalVal,
        pricingConfig = previewConfig
    )
    val simDeliveryFee = simResult.deliveryFee
    val isSimEligibleFree = simResult.isFreeDelivery

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HighDensityIndigoLight,
                border = BorderStroke(1.dp, HighDensityIndigo.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Smart & Balanced Delivery Engine", fontWeight = FontWeight.Black, fontSize = 13.sp, color = HighDensityIndigo)
                        Text(
                            "Delivery charge is automatically computed from customer distance (KM) and order weight (KG). Reasonable for customers and sustainable for SudhaniHub.",
                            fontSize = 11.sp,
                            color = HighDensitySlate700,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Live Simulator Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, HighDensitySlate200),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡ LIVE PRICING SIMULATOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500, letterSpacing = 0.5.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSimEligibleFree) HighDensityEmeraldLight else SudhaniGoldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isSimEligibleFree) "FREE DELIVERY" else "DELIVERY CHARGE: ₹${simDeliveryFee.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSimEligibleFree) HighDensityEmeraldDark else SudhaniGoldDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = simDistance,
                            onValueChange = { simDistance = it },
                            label = { Text("Distance (KM)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = simWeight,
                            onValueChange = { simWeight = it },
                            label = { Text("Weight (KG)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = simSubtotal,
                            onValueChange = { simSubtotal = it },
                            label = { Text("Subtotal (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSimEligibleFree) {
                            "✅ ${simResult.freeDeliveryReason ?: "Free delivery applied!"}"
                        } else if (!simResult.isDeliverable) {
                            "⚠️ ${simResult.rejectionReason ?: "Delivery not serviceable for this location."}"
                        } else {
                            "Calculated: Distance (${PriceCalculationService.formatDistance(simDistVal)}) + Total weight (${PriceCalculationService.formatWeight(simWeightVal)}) = ₹${simDeliveryFee.toInt()}"
                        },
                        fontSize = 11.sp,
                        color = if (!simResult.isDeliverable) HighDensityRed else HighDensitySlate600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Free Delivery Rules Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, HighDensitySlate200),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FREE DELIVERY POLICY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500, letterSpacing = 0.5.sp)
                            Text("Orders above min threshold within radius get free delivery", fontSize = 11.sp, color = HighDensitySlate600)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = freeDeliveryMinOrder,
                            onValueChange = { freeDeliveryMinOrder = it.filter { c -> c.isDigit() } },
                            label = { Text("Min Order (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = freeDeliveryMaxDist,
                            onValueChange = { freeDeliveryMaxDist = it },
                            label = { Text("Max Free Dist (KM)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxServiceableRadius,
                            onValueChange = { maxServiceableRadius = it },
                            label = { Text("Max Radius (KM)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Distance Tier 1: 0–3 KM
        item {
            TierConfigCard(
                title = "DISTANCE 0–3 KM (HYPERLOCAL)",
                badge = "0–3 KM",
                badgeColor = HighDensityEmerald,
                upTo1Kg = tier1_1kg,
                onUpTo1KgChange = { tier1_1kg = it.filter { c -> c.isDigit() } },
                oneTo3Kg = tier1_1to3kg,
                onOneTo3KgChange = { tier1_1to3kg = it.filter { c -> c.isDigit() } },
                threeTo5Kg = tier1_3to5kg,
                onThreeTo5KgChange = { tier1_3to5kg = it.filter { c -> c.isDigit() } },
                extraKgRate = tier1_above5kg_rate,
                onExtraKgRateChange = { tier1_above5kg_rate = it.filter { c -> c.isDigit() } }
            )
        }

        // Distance Tier 2: 3–7 KM
        item {
            TierConfigCard(
                title = "DISTANCE 3–7 KM (LOCAL ZONE)",
                badge = "3–7 KM",
                badgeColor = HighDensityIndigo,
                upTo1Kg = tier2_1kg,
                onUpTo1KgChange = { tier2_1kg = it.filter { c -> c.isDigit() } },
                oneTo3Kg = tier2_1to3kg,
                onOneTo3KgChange = { tier2_1to3kg = it.filter { c -> c.isDigit() } },
                threeTo5Kg = tier2_3to5kg,
                onThreeTo5KgChange = { tier2_3to5kg = it.filter { c -> c.isDigit() } },
                extraKgRate = tier2_above5kg_rate,
                onExtraKgRateChange = { tier2_above5kg_rate = it.filter { c -> c.isDigit() } }
            )
        }

        // Distance Tier 3: 7–12 KM
        item {
            TierConfigCard(
                title = "DISTANCE 7–12 KM (MID RANGE)",
                badge = "7–12 KM",
                badgeColor = HighDensityOrange,
                upTo1Kg = tier3_1kg,
                onUpTo1KgChange = { tier3_1kg = it.filter { c -> c.isDigit() } },
                oneTo3Kg = tier3_1to3kg,
                onOneTo3KgChange = { tier3_1to3kg = it.filter { c -> c.isDigit() } },
                threeTo5Kg = tier3_3to5kg,
                onThreeTo5KgChange = { tier3_3to5kg = it.filter { c -> c.isDigit() } },
                extraKgRate = tier3_above5kg_rate,
                onExtraKgRateChange = { tier3_above5kg_rate = it.filter { c -> c.isDigit() } }
            )
        }

        // Distance Tier 4: 12–20 KM
        item {
            TierConfigCard(
                title = "DISTANCE 12–20 KM (OUTSKIRTS)",
                badge = "12–20 KM",
                badgeColor = Color(0xFF7C3AED),
                upTo1Kg = tier4_1kg,
                onUpTo1KgChange = { tier4_1kg = it.filter { c -> c.isDigit() } },
                oneTo3Kg = tier4_1to3kg,
                onOneTo3KgChange = { tier4_1to3kg = it.filter { c -> c.isDigit() } },
                threeTo5Kg = tier4_3to5kg,
                onThreeTo5KgChange = { tier4_3to5kg = it.filter { c -> c.isDigit() } },
                extraKgRate = tier4_above5kg_rate,
                onExtraKgRateChange = { tier4_above5kg_rate = it.filter { c -> c.isDigit() } }
            )
        }

        // Distance Tier 5: Above 20 KM
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, HighDensitySlate200),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DISTANCE ABOVE 20 KM (LONG DISTANCE)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500, letterSpacing = 0.5.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HighDensityRed.copy(alpha = 0.1f)
                        ) {
                            Text(">20 KM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text("Calculates ₹60 base fee for first 3 KM, then adds ₹8 per additional KM, plus product weight charge.", fontSize = 11.sp, color = HighDensitySlate600)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tier5_base,
                            onValueChange = { tier5_base = it.filter { c -> c.isDigit() } },
                            label = { Text("Base Charge (₹)") },
                            placeholder = { Text("60") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tier5_per_km,
                            onValueChange = { tier5_per_km = it.filter { c -> c.isDigit() } },
                            label = { Text("Per Extra KM (₹)") },
                            placeholder = { Text("8") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Action Buttons: Save Rules & Reset to Defaults
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (saveStatusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HighDensityEmeraldLight,
                        border = BorderStroke(1.dp, HighDensityEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = saveStatusMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityEmeraldDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        SudhaniRepository.updateDeliveryPricingConfig(previewConfig)
                        saveStatusMessage = "✅ Smart delivery pricing configuration successfully saved and active across all checkouts!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_save_pricing_rules_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Smart Pricing Rules", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        val defaultConfig = DeliveryPricingConfig()
                        SudhaniRepository.updateDeliveryPricingConfig(defaultConfig)
                        tier1_1kg = defaultConfig.slab0To3.upToOneKg.toInt().toString()
                        tier1_1to3kg = defaultConfig.slab0To3.oneToThreeKg.toInt().toString()
                        tier1_3to5kg = defaultConfig.slab0To3.threeToFiveKg.toInt().toString()
                        tier1_above5kg_rate = defaultConfig.slab0To3.perExtraKgRate.toInt().toString()

                        tier2_1kg = defaultConfig.slab3To7.upToOneKg.toInt().toString()
                        tier2_1to3kg = defaultConfig.slab3To7.oneToThreeKg.toInt().toString()
                        tier2_3to5kg = defaultConfig.slab3To7.threeToFiveKg.toInt().toString()
                        tier2_above5kg_rate = defaultConfig.slab3To7.perExtraKgRate.toInt().toString()

                        tier3_1kg = defaultConfig.slab7To12.upToOneKg.toInt().toString()
                        tier3_1to3kg = defaultConfig.slab7To12.oneToThreeKg.toInt().toString()
                        tier3_3to5kg = defaultConfig.slab7To12.threeToFiveKg.toInt().toString()
                        tier3_above5kg_rate = defaultConfig.slab7To12.perExtraKgRate.toInt().toString()

                        tier4_1kg = defaultConfig.slab12To20.upToOneKg.toInt().toString()
                        tier4_1to3kg = defaultConfig.slab12To20.oneToThreeKg.toInt().toString()
                        tier4_3to5kg = defaultConfig.slab12To20.threeToFiveKg.toInt().toString()
                        tier4_above5kg_rate = defaultConfig.slab12To20.perExtraKgRate.toInt().toString()

                        tier5_base = defaultConfig.beyond20Km.baseFirst3KmFee.toInt().toString()
                        tier5_per_km = defaultConfig.beyond20Km.perAdditionalKmRate.toInt().toString()
                        freeDeliveryMinOrder = defaultConfig.freeDeliveryMinOrderAmount.toInt().toString()
                        freeDeliveryMaxDist = defaultConfig.freeDeliveryMaxDistanceKm.toInt().toString()
                        maxServiceableRadius = defaultConfig.maxDeliveryRadiusKm.toInt().toString()

                        saveStatusMessage = "🔄 Reset to SudhaniHub standard pricing rules."
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset to Defaults", color = HighDensitySlate700, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TierConfigCard(
    title: String,
    badge: String,
    badgeColor: Color,
    upTo1Kg: String,
    onUpTo1KgChange: (String) -> Unit,
    oneTo3Kg: String,
    onOneTo3KgChange: (String) -> Unit,
    threeTo5Kg: String,
    onThreeTo5KgChange: (String) -> Unit,
    extraKgRate: String,
    onExtraKgRateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500, letterSpacing = 0.5.sp)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = upTo1Kg,
                    onValueChange = onUpTo1KgChange,
                    label = { Text("≤ 1 KG (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = oneTo3Kg,
                    onValueChange = onOneTo3KgChange,
                    label = { Text("1–3 KG (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = threeTo5Kg,
                    onValueChange = onThreeTo5KgChange,
                    label = { Text("3–5 KG (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = extraKgRate,
                    onValueChange = onExtraKgRateChange,
                    label = { Text(">5KG +/KG (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
