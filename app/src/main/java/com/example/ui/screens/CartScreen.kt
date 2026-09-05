package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Address
import com.example.data.model.CartItem
import com.example.data.repository.SudhaniRepository
import com.example.data.service.PriceCalculationService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onStartShopping: () -> Unit,
    onOrderPlaced: (String) -> Unit = {},
    onChangeAddress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cart by SudhaniRepository.cart.collectAsState()
    val appliedCoupon by SudhaniRepository.appliedCoupon.collectAsState()
    val addresses by SudhaniRepository.addresses.collectAsState()
    val selectedAddress by SudhaniRepository.selectedAddress.collectAsState()
    val cartItems = cart.values.toList()

    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }
    var couponSuccess by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var showAddressSheet by remember { mutableStateOf(false) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val deliveryArea = SudhaniRepository.getDeliveryAreaForPincode(selectedAddress.pincode)
    val pricingConfig by SudhaniRepository.deliveryPricingConfig.collectAsState()

    val totalWeightGrams = cartItems.sumOf { it.product.calculateWeightInGrams() * it.quantity }
    val totalWeightKg = (totalWeightGrams / 1000.0).coerceAtLeast(0.1)
    val distanceKm = SudhaniRepository.resolveDeliveryDistance(selectedAddress, deliveryArea)

    val priceBreakdown = PriceCalculationService.calculatePrice(
        subtotal = subtotal,
        coupon = appliedCoupon,
        deliveryArea = deliveryArea,
        distanceKm = distanceKm,
        totalWeightKg = totalWeightKg,
        pricingConfig = pricingConfig
    )
    val isServiceable = priceBreakdown.isServiceable
    val isMinOrderMet = priceBreakdown.isMinimumOrderMet
    val canPlaceOrder = isServiceable && isMinOrderMet && !isPlacingOrder && cartItems.isNotEmpty()

    Scaffold(
        containerColor = SudhaniTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MY CART", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp, color = SudhaniTheme.colors.textPrimary)
                        if (cartItems.isNotEmpty()) {
                            Text(
                                "⚡ Delivery in 10-15 mins",
                                fontSize = 11.sp,
                                color = SudhaniGoldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SudhaniTheme.colors.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SudhaniTheme.colors.textPrimary)
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { SudhaniRepository.clearCart() }) {
                            Text("EMPTY", color = SudhaniRedDiscount, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    shadowElevation = 8.dp,
                    color = SudhaniTheme.colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL TO PAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = SudhaniTheme.colors.textMuted
                            )
                            Text(
                                text = PriceCalculationService.formatRupees(priceBreakdown.finalTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canPlaceOrder) SudhaniTheme.colors.textPrimary else SudhaniTheme.colors.textMuted
                            )
                            if (priceBreakdown.discount > 0) {
                                Text(
                                    text = "Saved ${PriceCalculationService.formatRupees(priceBreakdown.discount)} with coupon",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SudhaniGoldDark
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (canPlaceOrder) {
                                    isPlacingOrder = true
                                    val order = SudhaniRepository.placeOrder(
                                        paymentMethod = selectedPaymentMethod,
                                        address = selectedAddress
                                    )
                                    onOrderPlaced(order.orderId)
                                }
                            },
                            enabled = canPlaceOrder,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canPlaceOrder) SudhaniGoldPrimary else SudhaniTheme.colors.chipBackground,
                                contentColor = if (canPlaceOrder) SudhaniNavyDark else SudhaniTheme.colors.textMuted,
                                disabledContainerColor = SudhaniTheme.colors.chipBackground,
                                disabledContentColor = SudhaniTheme.colors.textMuted
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("place_order_button")
                        ) {
                            Text(
                                text = when {
                                    isPlacingOrder -> "PLACING ORDER..."
                                    !isServiceable -> "DELIVERY UNAVAILABLE"
                                    !isMinOrderMet -> "MIN ORDER ₹${priceBreakdown.minimumOrderAmount.toInt()}"
                                    else -> "PLACE ORDER"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                color = if (canPlaceOrder) SudhaniNavyDark else SudhaniTheme.colors.textMuted
                            )
                            if (canPlaceOrder) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = SudhaniNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            // Empty State
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(HighDensityIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = HighDensityIndigo,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Cart is Empty",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensitySlate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You haven't added anything to your cart yet. Browse fresh groceries and daily essentials!",
                        fontSize = 13.sp,
                        color = HighDensitySlate600,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartShopping,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(44.dp)
                            .testTag("empty_cart_shop_button")
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START SHOPPING", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(HighDensitySlate50)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Free Delivery Progress Banner
                item {
                    val isFreeDelivery = priceBreakdown.isFreeDelivery
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFreeDelivery) HighDensityEmeraldLight else HighDensityOrangeLight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFreeDelivery) Color(0xFFBBF7D0) else Color(0xFFFED7AA)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFreeDelivery) Icons.Default.CheckCircle else Icons.Default.ElectricMoped,
                                contentDescription = null,
                                tint = if (isFreeDelivery) HighDensityEmeraldDark else HighDensityOrangeDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isFreeDelivery)
                                        "🎉 Free Instant Delivery unlocked on this order!"
                                    else
                                        "Add ${PriceCalculationService.formatRupees(priceBreakdown.amountNeededForFreeDelivery)} more to get FREE Delivery!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFreeDelivery) HighDensityEmeraldDark else HighDensityOrangeDark
                                )
                                if (priceBreakdown.eligibleForDiscountBonus) {
                                    Text(
                                        text = "✨ Eligible for Exclusive Discounts & Priority Packing (Order > ₹499)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HighDensityIndigo
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Delivery Address Card
                item {
                    Text(
                        text = "Delivery Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SudhaniTheme.colors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedAddress.label == "Home") Icons.Default.Home else Icons.Default.Work,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedAddress.label,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = SudhaniTheme.colors.textPrimary
                                    )
                                    if (selectedAddress.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SudhaniTheme.colors.chipBackground
                                        ) {
                                            Text(
                                                text = "DEFAULT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SudhaniGoldDark,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = selectedAddress.fullAddress,
                                    fontSize = 11.sp,
                                    color = SudhaniTheme.colors.textSecondary,
                                    maxLines = 2
                                )
                            }
                            TextButton(
                                onClick = { showAddressSheet = true },
                                modifier = Modifier.testTag("change_address_button")
                            ) {
                                Text(
                                    text = "CHANGE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SudhaniGoldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    DeliveryServiceabilityCard(pincode = selectedAddress.pincode)

                    if (!isServiceable) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighDensityRed.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Sorry, we don't deliver to this area yet. Please change or select an active delivery address to proceed.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityRed,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else if (!isMinOrderMet) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SudhaniRedDiscount.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Minimum order of ₹${priceBreakdown.minimumOrderAmount.toInt()} required for this area. Add items worth ₹${(priceBreakdown.minimumOrderAmount - subtotal).toInt()} more to place order.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniRedDiscount,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Payment Method Selection
                item {
                    Text(
                        text = "Payment Method",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySlate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Cash on Delivery
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPaymentMethod = "Cash on Delivery" }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "Cash on Delivery",
                                    onClick = { selectedPaymentMethod = "Cash on Delivery" },
                                    colors = RadioButtonDefaults.colors(selectedColor = HighDensityIndigo)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Cash on Delivery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = HighDensitySlate900
                                    )
                                    Text(
                                        text = "Pay cash or scan QR at delivery",
                                        fontSize = 10.sp,
                                        color = HighDensitySlate600
                                    )
                                }
                            }

                            HorizontalDivider(color = HighDensitySlate100, modifier = Modifier.padding(horizontal = 8.dp))

                            // Online Payment
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPaymentMethod = "Online Payment" }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == "Online Payment",
                                    onClick = { selectedPaymentMethod = "Online Payment" },
                                    colors = RadioButtonDefaults.colors(selectedColor = HighDensityIndigo)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Online Payment",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = HighDensitySlate900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = HighDensityEmeraldLight
                                        ) {
                                            Text(
                                                text = "UPI / CARDS",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = HighDensityEmeraldDark,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Google Pay, PhonePe, Cards, NetBanking (Integration-Ready)",
                                        fontSize = 10.sp,
                                        color = HighDensitySlate600
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Cart Items List
                item {
                    Text(
                        text = "Review Items (${cartItems.sumOf { it.quantity }})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySlate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(cartItems) { item ->
                    CartItemRow(
                        cartItem = item,
                        onIncrement = { SudhaniRepository.addToCart(item.product) },
                        onDecrement = { SudhaniRepository.decrementQuantity(item.product.id) },
                        onRemove = { SudhaniRepository.removeFromCart(item.product.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Coupon Code Section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Offers & Coupons",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (appliedCoupon == null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = {
                                            couponInput = it.uppercase()
                                            couponError = null
                                            couponSuccess = null
                                        },
                                        placeholder = { Text("Enter coupon code") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("coupon_input_field")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (couponInput.isBlank()) {
                                                couponError = "Please enter a coupon code"
                                            } else {
                                                val success = SudhaniRepository.applyCoupon(couponInput)
                                                if (success) {
                                                    couponSuccess = "Coupon applied successfully!"
                                                    couponError = null
                                                    couponInput = ""
                                                } else {
                                                    couponError = "Invalid coupon or order below minimum"
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                                        modifier = Modifier.testTag("apply_coupon_btn")
                                    ) {
                                        Text("Apply", color = SudhaniNavyDark, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quick tap coupons
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SudhaniRepository.coupons.forEach { cp ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SudhaniTheme.colors.chipBackground,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                                            modifier = Modifier.clickable {
                                                SudhaniRepository.applyCoupon(cp.code)
                                            }
                                        ) {
                                            Text(
                                                text = "${cp.code} (${if (cp.discountPercent > 0) "${cp.discountPercent}%" else "₹${cp.flatDiscount.toInt()}"})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SudhaniGoldDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = HighDensityEmeraldDark
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "'${appliedCoupon?.code}' Applied",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = HighDensityEmeraldDark
                                            )
                                            Text(
                                                text = appliedCoupon?.description ?: "",
                                                fontSize = 11.sp,
                                                color = HighDensitySlate600
                                            )
                                        }
                                    }
                                    TextButton(onClick = { SudhaniRepository.removeCoupon() }) {
                                        Text("Remove", color = SudhaniRedDiscount, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (couponError != null) {
                                Text(
                                    text = couponError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            if (couponSuccess != null) {
                                Text(
                                    text = couponSuccess!!,
                                    color = HighDensityEmeraldDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Bill Summary Card
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Bill Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BillRow(
                                label = "Product Subtotal",
                                amount = PriceCalculationService.formatRupees(priceBreakdown.subtotal)
                            )
                            BillRow(
                                label = "Total Weight",
                                amount = PriceCalculationService.formatWeight(priceBreakdown.totalWeightKg)
                            )
                            BillRow(
                                label = "Delivery Distance",
                                amount = PriceCalculationService.formatDistance(priceBreakdown.deliveryDistanceKm)
                            )
                            BillRow(
                                label = "Delivery Charge",
                                amount = if (priceBreakdown.isFreeDelivery) "FREE" else PriceCalculationService.formatRupees(priceBreakdown.deliveryFee),
                                isFree = priceBreakdown.isFreeDelivery
                            )

                            if (priceBreakdown.discount > 0) {
                                BillRow(
                                    label = "Coupon Discount (${priceBreakdown.couponCode ?: ""})",
                                    amount = "-${PriceCalculationService.formatRupees(priceBreakdown.discount)}",
                                    isDiscount = true
                                )
                            } else {
                                BillRow(
                                    label = "Discount",
                                    amount = "₹0"
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SudhaniTheme.colors.divider)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Payable",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                                Text(
                                    text = PriceCalculationService.formatRupees(priceBreakdown.finalTotal),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = SudhaniGoldDark
                                )
                            }

                            // Smart Delivery charge explanation note
                            Surface(
                                color = SudhaniGoldPrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SudhaniGoldDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = priceBreakdown.deliveryFeeExplanation,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SudhaniTheme.colors.textSecondary
                                    )
                                }
                            }

                            // Free Delivery Banner or Nudge
                            if (priceBreakdown.isFreeDelivery) {
                                Surface(
                                    color = HighDensityEmeraldDark.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎉 ${priceBreakdown.freeDeliveryReason ?: "Free Delivery Applied (Orders > ₹999 within 7 KM)!"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityEmeraldDark,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else if (priceBreakdown.amountNeededForFreeDelivery > 0) {
                                Surface(
                                    color = SudhaniGoldPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Add ₹${priceBreakdown.amountNeededForFreeDelivery.toInt()} more to get FREE Delivery!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniGoldDark,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            // Unserviceable warning
                            if (!priceBreakdown.isServiceable) {
                                Surface(
                                    color = HighDensityRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚠️ ${priceBreakdown.serviceabilityMessage ?: "Delivery unavailable for selected location."}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityRed,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Delivery Guarantee Note
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = HighDensityEmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Safe & contactless 10-minute delivery guaranteed.",
                            fontSize = 11.sp,
                            color = HighDensitySlate600
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Address Selection Sheet / Dialog
    if (showAddressSheet) {
        AlertDialog(
            onDismissRequest = { showAddressSheet = false },
            containerColor = SudhaniTheme.colors.surface,
            title = {
                Text(
                    text = "Select Delivery Address",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SudhaniTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    addresses.forEach { addr ->
                        val isSelected = addr.id == selectedAddress.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SudhaniTheme.colors.chipBackground else SudhaniTheme.colors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SudhaniRepository.selectAddress(addr)
                                    showAddressSheet = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = addr.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) SudhaniGoldDark else SudhaniTheme.colors.textPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = SudhaniGoldDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = addr.fullAddress,
                                    fontSize = 11.sp,
                                    color = SudhaniTheme.colors.textSecondary
                                )
                                val addrServiceability = SudhaniRepository.checkServiceability(addr.pincode)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (addrServiceability.isServiceable && addrServiceability.deliveryArea != null) {
                                    Text(
                                        text = "✓ Deliverable (${addrServiceability.deliveryArea.estimatedDeliveryTime})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniGoldDark
                                    )
                                } else {
                                    Text(
                                        text = "⚠️ Delivery unavailable",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniRedDiscount
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddressSheet = false
                        onChangeAddress()
                    }
                ) {
                    Text("+ Add New Address", color = SudhaniGoldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressSheet = false }) {
                    Text("Cancel", color = SudhaniTheme.colors.textSecondary)
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SudhaniTheme.colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SudhaniTheme.colors.chipBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cartItem.product.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    color = SudhaniTheme.colors.textPrimary
                )
                Text(
                    text = cartItem.product.unit,
                    fontSize = 10.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₹${(cartItem.product.price * cartItem.quantity).toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = SudhaniGoldDark
                )
            }

            // Stepper
            Row(
                modifier = Modifier
                    .background(SudhaniTheme.colors.chipBackground, RoundedCornerShape(8.dp))
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Minus",
                        tint = SudhaniTheme.colors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "${cartItem.quantity}",
                    color = SudhaniTheme.colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus",
                        tint = SudhaniTheme.colors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BillRow(
    label: String,
    amount: String,
    isFree: Boolean = false,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SudhaniTheme.colors.textSecondary
        )
        Text(
            text = amount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                isFree || isDiscount -> SudhaniGoldDark
                else -> SudhaniTheme.colors.textPrimary
            }
        )
    }
}
