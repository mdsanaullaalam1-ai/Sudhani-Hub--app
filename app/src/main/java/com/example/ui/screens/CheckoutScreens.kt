package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Address
import com.example.data.model.DeliveryArea
import com.example.data.model.PaymentStatus
import com.example.data.model.SavedPaymentMethod
import com.example.data.repository.SudhaniRepository
import com.example.data.service.PriceCalculationService
import com.example.data.service.RazorpayService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val addresses by SudhaniRepository.addresses.collectAsState()
    val selectedAddress by SudhaniRepository.selectedAddress.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var manualPincodeInput by remember { mutableStateOf("") }
    var checkedPincode by remember { mutableStateOf<String?>(null) }
    var locationFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                val detected = detectLocationAddress(context)
                SudhaniRepository.addAddress(detected)
                SudhaniRepository.selectAddress(detected)
                locationFeedbackMessage = "Detected location in ${detected.area} (${detected.pincode})"
                Toast.makeText(context, "Location set: ${detected.area}, ${detected.pincode}", Toast.LENGTH_SHORT).show()
            } else {
                locationFeedbackMessage = "Location permission was denied. Please enter PIN code manually."
                Toast.makeText(context, "Location permission denied. Enter PIN manually.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        containerColor = SudhaniTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Delivery Addresses", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SudhaniTheme.colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.AddLocationAlt, contentDescription = "Add Address", tint = SudhaniGoldDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SudhaniTheme.colors.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SudhaniGoldPrimary,
                contentColor = SudhaniNavyDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = SudhaniNavyDark) },
                text = { Text("Add New Address", fontWeight = FontWeight.Bold, color = SudhaniNavyDark) },
                modifier = Modifier.testTag("add_address_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Manual PIN Code Checker Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = SudhaniGoldDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Check Delivery Availability",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SudhaniTheme.colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualPincodeInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        manualPincodeInput = it
                                        if (it.length == 6) {
                                            checkedPincode = it
                                        }
                                    }
                                },
                                placeholder = { Text("Enter 6-digit PIN code", fontSize = 13.sp, color = SudhaniTheme.colors.textSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SudhaniTheme.colors.textPrimary,
                                    unfocusedTextColor = SudhaniTheme.colors.textPrimary,
                                    focusedBorderColor = SudhaniGoldPrimary,
                                    unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (manualPincodeInput.isNotBlank()) {
                                            checkedPincode = manualPincodeInput.trim()
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_pin_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (manualPincodeInput.isNotBlank()) {
                                        checkedPincode = manualPincodeInput.trim()
                                    }
                                },
                                enabled = manualPincodeInput.length == 6,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                                modifier = Modifier.testTag("check_pin_button")
                            ) {
                                Text("Check", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
                            }
                        }

                        // Show result if checked
                        if (!checkedPincode.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DeliveryServiceabilityCard(pincode = checkedPincode!!)
                        }
                    }
                }
            }

            // Current Location (GPS) Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
                    border = BorderStroke(1.dp, SudhaniGoldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFine || hasCoarse) {
                                val detected = detectLocationAddress(context)
                                SudhaniRepository.addAddress(detected)
                                SudhaniRepository.selectAddress(detected)
                                locationFeedbackMessage = "Detected location in ${detected.area} (${detected.pincode})"
                                Toast.makeText(context, "Location set: ${detected.area}, ${detected.pincode}", Toast.LENGTH_SHORT).show()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        .testTag("use_current_location_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SudhaniGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = SudhaniNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Current Location (GPS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "Detect your delivery PIN code automatically with location permission",
                                fontSize = 12.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                            if (locationFeedbackMessage != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = locationFeedbackMessage!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SudhaniGoldDark
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Saved Addresses",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary
                )
            }

            items(addresses) { addr ->
                val isSelected = addr.id == selectedAddress.id
                val serviceability = SudhaniRepository.checkServiceability(addr.pincode)
                val area = serviceability.deliveryArea

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    elevation = CardDefaults.cardElevation(if (isSelected) 3.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            SudhaniRepository.selectAddress(addr)
                            onNavigateBack()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                SudhaniRepository.selectAddress(addr)
                                onNavigateBack()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SudhaniGoldDark,
                                unselectedColor = SudhaniTheme.colors.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = addr.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SudhaniTheme.colors.chipBackground,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PIN: ${addr.pincode}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniTheme.colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = SudhaniGoldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "SELECTED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SudhaniGoldDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = addr.fullAddress,
                                fontSize = 13.sp,
                                color = SudhaniTheme.colors.textSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            // Serviceability status tag
                            if (serviceability.isServiceable && area != null) {
                                Surface(
                                    color = SudhaniTheme.colors.chipBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "✓ Delivery available",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SudhaniGoldDark
                                        )
                                        Text(
                                            text = " • ETA: ${area.estimatedDeliveryTime} • Min ₹${area.minimumOrderAmount.toInt()}",
                                            fontSize = 10.sp,
                                            color = SudhaniTheme.colors.textSecondary
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = HighDensityRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Sorry, we don't deliver to this area yet",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAddressDialog(
                onDismiss = { showAddDialog = false },
                onAddressSaved = { newAddr ->
                    SudhaniRepository.addAddress(newAddr)
                    SudhaniRepository.selectAddress(newAddr)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onAddressSaved: (Address) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var house by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }
    var state by remember { mutableStateOf("Karnataka") }
    var pincode by remember { mutableStateOf("560102") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label selection (Home / Work / Other)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Home", "Work", "Other").forEach { tag ->
                        FilterChip(
                            selected = label == tag,
                            onClick = { label = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                OutlinedTextField(
                    value = house,
                    onValueChange = { house = it },
                    label = { Text("House / Flat / Block No.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street / Road / Landmark") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Locality") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                pincode = it
                            }
                        },
                        label = { Text("Pincode") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_pin_input")
                    )
                }

                if (pincode.length == 6) {
                    Spacer(modifier = Modifier.height(4.dp))
                    DeliveryServiceabilityCard(pincode = pincode)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (house.isNotBlank() && area.isNotBlank()) {
                        val newAddr = Address(
                            id = "addr_${System.currentTimeMillis()}",
                            label = label,
                            house = house,
                            street = street.ifBlank { "Main Road" },
                            area = area,
                            city = city,
                            state = state,
                            pincode = pincode,
                            isDefault = true
                        )
                        onAddressSaved(newAddr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark)
            ) {
                Text("Save Address", fontWeight = FontWeight.Bold, color = SudhaniNavyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SudhaniTheme.colors.textSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cart by SudhaniRepository.cart.collectAsState()
    val selectedAddress by SudhaniRepository.selectedAddress.collectAsState()
    val appliedCoupon by SudhaniRepository.appliedCoupon.collectAsState()
    val savedMethods by SudhaniRepository.savedPaymentMethods.collectAsState()
    val cartItems = cart.values.toList()
    val coroutineScope = rememberCoroutineScope()

    // Select default saved payment method if present, otherwise COD
    val defaultSaved = remember(savedMethods) {
        savedMethods.firstOrNull { it.isDefault } ?: savedMethods.firstOrNull()
    }
    var selectedPaymentKey by remember { mutableStateOf<String?>(null) }

    // Initialize selection with default saved method if available
    LaunchedEffect(savedMethods) {
        if (selectedPaymentKey == null) {
            selectedPaymentKey = defaultSaved?.id ?: "COD"
        }
    }

    val currentSelectedKey = selectedPaymentKey ?: (defaultSaved?.id ?: "COD")
    val selectedSavedMethod = savedMethods.firstOrNull { it.id == currentSelectedKey }

    var isPlacingOrder by remember { mutableStateOf(false) }
    var razorpayProcessingMessage by remember { mutableStateOf<String?>(null) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

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
    val grandTotal = priceBreakdown.finalTotal
    val isServiceable = priceBreakdown.isServiceable
    val isMinOrderMet = priceBreakdown.isMinimumOrderMet
    val canCheckout = isServiceable && isMinOrderMet && cartItems.isNotEmpty() && !isPlacingOrder

    Scaffold(
        containerColor = SudhaniTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Checkout & Payment", fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SudhaniTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SudhaniTheme.colors.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shadowElevation = 12.dp,
                color = SudhaniTheme.colors.surface
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
                            text = "TOTAL AMOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textSecondary
                        )
                        Text(
                            text = PriceCalculationService.formatRupees(grandTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (canCheckout) SudhaniGoldDark else SudhaniTheme.colors.textSecondary
                        )
                    }

                    Button(
                        onClick = {
                            if (canCheckout) {
                                coroutineScope.launch {
                                    isPlacingOrder = true
                                    val isCod = currentSelectedKey == "COD"
                                    if (isCod) {
                                        val order = SudhaniRepository.placeOrder(
                                            paymentMethod = "Cash on Delivery",
                                            address = selectedAddress,
                                            customPaymentStatus = PaymentStatus.PENDING
                                        )
                                        isPlacingOrder = false
                                        onOrderPlaced(order.orderId)
                                    } else {
                                        // Process via Razorpay secure gateway
                                        val methodName = selectedSavedMethod?.title ?: "UPI / Online"
                                        razorpayProcessingMessage = "Authorizing payment via Razorpay secure gateway..."
                                        val checkoutOrder = RazorpayService.createCheckoutOrder(grandTotal, "ORD_TMP")
                                        val verification = RazorpayService.verifyAndConfirmPayment(
                                            razorpayOrderId = checkoutOrder.razorpayOrderId,
                                            paymentMethodType = selectedSavedMethod?.type ?: "UPI"
                                        )

                                        if (verification.isSuccess) {
                                            val order = SudhaniRepository.placeOrder(
                                                paymentMethod = methodName,
                                                address = selectedAddress,
                                                customPaymentStatus = PaymentStatus.PAID,
                                                customTransactionRef = verification.paymentId
                                            )
                                            isPlacingOrder = false
                                            razorpayProcessingMessage = null
                                            onOrderPlaced(order.orderId)
                                        } else {
                                            isPlacingOrder = false
                                            razorpayProcessingMessage = null
                                        }
                                    }
                                }
                            }
                        },
                        enabled = canCheckout,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canCheckout) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder,
                            contentColor = SudhaniNavyDark
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("place_order_button")
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(
                                color = SudhaniNavyDark,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when {
                                    !isServiceable -> "Delivery Unavailable"
                                    !isMinOrderMet -> "Min Order ₹${priceBreakdown.minimumOrderAmount.toInt()}"
                                    currentSelectedKey == "COD" -> "Place Order (COD)"
                                    selectedSavedMethod != null -> "Pay ₹${grandTotal.toInt()} with ${selectedSavedMethod.bankOrProvider.ifBlank { selectedSavedMethod.title.take(12) }}"
                                    else -> "Pay & Place Order"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canCheckout) SudhaniNavyDark else SudhaniTheme.colors.textSecondary
                            )
                            if (canCheckout) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = SudhaniNavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Delivery In 11 Mins Banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniNavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SudhaniGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = SudhaniNavyDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (deliveryArea != null) "⚡ Delivery in ${deliveryArea.estimatedDeliveryTime}" else "⚡ Delivery in 10-15 Minutes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (deliveryArea != null) "Servicing ${deliveryArea.areaName}, ${deliveryArea.city}" else "Instant delivery from nearest SudhaniHub dark store",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Delivery Address Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delivering To (${selectedAddress.label})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                            }
                            TextButton(onClick = onChangeAddress) {
                                Text("Change", color = SudhaniGoldDark, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = selectedAddress.fullAddress,
                            fontSize = 13.sp,
                            color = SudhaniTheme.colors.textSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(start = 26.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        DeliveryServiceabilityCard(pincode = selectedAddress.pincode)

                        if (!isServiceable) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = HighDensityRed.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ ${priceBreakdown.serviceabilityMessage ?: "Checkout is disabled because SudhaniHub does not deliver to PIN ${selectedAddress.pincode} yet. Please select another delivery address."}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityRed,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else if (!isMinOrderMet) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = SudhaniRedDiscount.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ Minimum order amount of ₹${priceBreakdown.minimumOrderAmount.toInt()} required for this delivery area. Add ₹${(priceBreakdown.minimumOrderAmount - subtotal).toInt()} more to proceed.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SudhaniRedDiscount,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Order Items Summary Preview
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Order Summary (${cartItems.size} items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SudhaniTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.product.emoji} ${item.product.name} x ${item.quantity}",
                                    fontSize = 13.sp,
                                    color = SudhaniTheme.colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${(item.product.price * item.quantity).toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SudhaniTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Payment Methods
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )

                    TextButton(
                        onClick = { showAddPaymentDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = SudhaniGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Add New Method",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniGoldDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Saved Payment Methods Header
                    if (savedMethods.isNotEmpty()) {
                        Text(
                            text = "SAVED METHODS (1-TAP CHECKOUT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textSecondary
                        )

                        savedMethods.forEach { method ->
                            SavedCheckoutMethodCard(
                                method = method,
                                isSelected = currentSelectedKey == method.id,
                                onSelect = { selectedPaymentKey = method.id }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OTHER PAYMENT OPTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textSecondary
                        )
                    }

                    // Cash on Delivery
                    PaymentMethodCard(
                        title = "Cash on Delivery (COD)",
                        subtitle = "Pay cash or scan QR at your doorstep upon delivery",
                        icon = Icons.Default.Money,
                        isSelected = currentSelectedKey == "COD",
                        onSelect = { selectedPaymentKey = "COD" }
                    )

                    // Online Payment / UPI (Razorpay abstraction placeholder)
                    PaymentMethodCard(
                        title = "Other UPI / NetBanking (Razorpay)",
                        subtitle = "Pay via any other UPI app or Net Banking securely",
                        icon = Icons.Default.QrCodeScanner,
                        isSelected = currentSelectedKey == "ONLINE_GATEWAY",
                        onSelect = { selectedPaymentKey = "ONLINE_GATEWAY" }
                    )
                }
            }

            // Price Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BillRow("Product Subtotal", PriceCalculationService.formatRupees(priceBreakdown.subtotal))
                        BillRow("Total Weight", PriceCalculationService.formatWeight(priceBreakdown.totalWeightKg))
                        BillRow("Delivery Distance", PriceCalculationService.formatDistance(priceBreakdown.deliveryDistanceKm))
                        BillRow(
                            label = "Delivery Charge",
                            amount = if (priceBreakdown.isFreeDelivery) "FREE" else PriceCalculationService.formatRupees(priceBreakdown.deliveryFee),
                            isFree = priceBreakdown.isFreeDelivery
                        )
                        if (priceBreakdown.discount > 0) {
                            BillRow("Discount (${priceBreakdown.couponCode ?: ""})", "-${PriceCalculationService.formatRupees(priceBreakdown.discount)}", isDiscount = true)
                        } else {
                            BillRow("Discount", "₹0")
                        }
                        HorizontalDivider(color = SudhaniTheme.colors.divider)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SudhaniTheme.colors.textPrimary)
                            Text(PriceCalculationService.formatRupees(grandTotal), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SudhaniGoldDark)
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
                    }
                }
            }
        }

        // Razorpay Secure Processing Overlay
        razorpayProcessingMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { /* non-cancellable during transaction */ },
                containerColor = SudhaniTheme.colors.surface,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SudhaniNavyDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SudhaniGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Razorpay Secure Gateway",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "Merchant: Sudhanihub Grocery",
                                fontSize = 11.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SudhaniGoldDark,
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SudhaniTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Total Payable: ₹${grandTotal.toInt()} • 256-Bit SSL Encrypted",
                            fontSize = 11.sp,
                            color = SudhaniTheme.colors.textSecondary
                        )
                    }
                },
                confirmButton = {}
            )
        }

        // Quick Add Payment Dialog from Checkout
        if (showAddPaymentDialog) {
            CheckoutAddPaymentDialog(
                onDismiss = { showAddPaymentDialog = false },
                onMethodAdded = { newMethod ->
                    SudhaniRepository.addSavedPaymentMethod(newMethod)
                    selectedPaymentKey = newMethod.id
                    showAddPaymentDialog = false
                }
            )
        }
    }
}

@Composable
fun DeliveryServiceabilityCard(
    pincode: String,
    modifier: Modifier = Modifier
) {
    if (pincode.isBlank()) return

    val serviceability = SudhaniRepository.checkServiceability(pincode)

    if (serviceability.isServiceable && serviceability.deliveryArea != null) {
        val area = serviceability.deliveryArea
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
            border = BorderStroke(1.dp, SudhaniGoldPrimary.copy(alpha = 0.4f)),
            modifier = modifier
                .fillMaxWidth()
                .testTag("serviceability_available_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SudhaniGoldDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✓ Delivery available",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = SudhaniGoldDark,
                        modifier = Modifier.testTag("delivery_available_text")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "in ${area.areaName} (${area.pincode})",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ESTIMATED TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textSecondary)
                        Text(
                            text = "⚡ ${area.estimatedDeliveryTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SudhaniGoldDark
                        )
                    }
                    Column {
                        Text("DELIVERY FEE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textSecondary)
                        Text(
                            text = if (area.deliveryFee == 0.0) "FREE (₹0)" else "₹${area.deliveryFee.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SudhaniTheme.colors.textPrimary
                        )
                    }
                    Column {
                        Text("MIN ORDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SudhaniTheme.colors.textSecondary)
                        Text(
                            text = "₹${area.minimumOrderAmount.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SudhaniTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityRed.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, HighDensityRed.copy(alpha = 0.3f)),
            modifier = modifier
                .fillMaxWidth()
                .testTag("serviceability_unavailable_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = HighDensityRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sorry, we don't deliver to this area yet.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = HighDensityRed,
                        modifier = Modifier.testTag("delivery_unavailable_text")
                    )
                    Text(
                        text = "PIN: $pincode is outside our active delivery zones.",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

fun detectLocationAddress(context: Context): Address {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        var bestLocation: Location? = null
        if (locationManager != null) {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val l = try {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.getLastKnownLocation(provider)
                    } else null
                } catch (e: SecurityException) {
                    null
                }
                if (l != null && (bestLocation == null || l.accuracy < bestLocation.accuracy)) {
                    bestLocation = l
                }
            }
        }

        if (bestLocation != null && Geocoder.isPresent()) {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val detectedPin = addr.postalCode?.trim() ?: "560038"
                val detectedArea = addr.subLocality ?: addr.locality ?: "Indiranagar"
                val detectedCity = addr.locality ?: addr.subAdminArea ?: "Bengaluru"
                val detectedState = addr.adminArea ?: "Karnataka"
                return Address(
                    id = "addr_loc_${System.currentTimeMillis()}",
                    label = "Current Location",
                    house = addr.featureName ?: "House Pin",
                    street = addr.thoroughfare ?: "Main Road",
                    area = detectedArea,
                    city = detectedCity,
                    state = detectedState,
                    pincode = detectedPin,
                    isDefault = true
                )
            }
        }
    } catch (_: Exception) {}

    // Graceful fallback for emulator or when GPS lock is empty:
    val activeArea = SudhaniRepository.deliveryAreas.value.firstOrNull { it.isActive }
    val pincode = activeArea?.pincode ?: "560038"
    val areaName = activeArea?.areaName ?: "Indiranagar"
    val cityName = activeArea?.city ?: "Bengaluru"
    val stateName = activeArea?.state ?: "Karnataka"

    return Address(
        id = "addr_loc_${System.currentTimeMillis()}",
        label = "Current Location (GPS)",
        house = "Flat 4B, Greenview",
        street = "100 Feet Road",
        area = areaName,
        city = cityName,
        state = stateName,
        pincode = pincode,
        isDefault = true
    )
}

@Composable
fun PaymentMethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SudhaniGoldDark,
                    unselectedColor = SudhaniTheme.colors.textSecondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) SudhaniGoldDark else SudhaniTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SudhaniTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun SavedCheckoutMethodCard(
    method: SavedPaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SudhaniTheme.colors.chipBackground else SudhaniTheme.colors.cardBackground
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .testTag("saved_method_${method.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SudhaniGoldDark,
                    unselectedColor = SudhaniTheme.colors.textSecondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SudhaniNavyDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        method.type.equals("UPI", ignoreCase = true) -> Icons.Default.QrCode
                        method.type.contains("CREDIT", ignoreCase = true) -> Icons.Default.CreditScore
                        else -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = SudhaniGoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    if (method.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SudhaniGoldPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, SudhaniGoldDark)
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
                    text = method.subtitle,
                    fontSize = 12.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = SudhaniGoldDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CheckoutAddPaymentDialog(
    onDismiss: () -> Unit,
    onMethodAdded: (SavedPaymentMethod) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: UPI, 1: Debit, 2: Credit
    val coroutineScope = rememberCoroutineScope()

    // UPI state
    var upiIdInput by remember { mutableStateOf("") }
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var isVerifyingUpi by remember { mutableStateOf(false) }
    var upiVerificationMessage by remember { mutableStateOf<String?>(null) }
    var upiIsVerified by remember { mutableStateOf(false) }
    var upiValidatedHolderName by remember { mutableStateOf<String?>(null) }

    // Card state
    var cardBankName by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var cardNumberInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    var isTokenizingCard by remember { mutableStateOf(false) }
    var cardErrorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SudhaniGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = null,
                            tint = SudhaniGoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Payment Method",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SudhaniTheme.colors.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Method Tabs: UPI | Debit Card | Credit Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("UPI", "Debit Card", "Credit Card").forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SudhaniNavyDark,
                                selectedLabelColor = SudhaniGoldPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (selectedTab == 0) {
                    // UPI Form
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Google Pay", "PhonePe", "Paytm", "Other UPI").forEach { app ->
                                FilterChip(
                                    selected = selectedUpiApp == app,
                                    onClick = { selectedUpiApp = app },
                                    label = { Text(app, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = {
                                upiIdInput = it
                                upiIsVerified = false
                                upiVerificationMessage = null
                            },
                            label = { Text("Virtual Payment Address (UPI ID)") },
                            placeholder = { Text("e.g. mobile@oksbi / name@paytm") },
                            singleLine = true,
                            isError = upiVerificationMessage != null && !upiIsVerified,
                            supportingText = {
                                if (upiVerificationMessage != null) {
                                    Text(
                                        text = upiVerificationMessage ?: "",
                                        color = if (upiIsVerified) SudhaniGoldDark else MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text("Real VPA format: username@bankname", fontSize = 10.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isVerifyingUpi = true
                                        val result = RazorpayService.verifyUpiId(upiIdInput.trim())
                                        isVerifyingUpi = false
                                        upiIsVerified = result.isValid
                                        upiValidatedHolderName = result.accountHolderName
                                        upiVerificationMessage = if (result.isValid) "Verified: ${result.accountHolderName} (${result.detectedProvider})" else (result.errorMessage ?: "Invalid UPI ID")
                                    }
                                },
                                enabled = upiIdInput.isNotBlank() && !isVerifyingUpi,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isVerifyingUpi) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verify VPA", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val finalTitle = if (selectedUpiApp == "Other UPI") "UPI Account" else "$selectedUpiApp UPI"
                                    val newMethod = SavedPaymentMethod(
                                        id = "SPM_UPI_${System.currentTimeMillis()}",
                                        title = finalTitle,
                                        subtitle = upiIdInput.trim(),
                                        type = "UPI",
                                        isDefault = false,
                                        bankOrProvider = selectedUpiApp,
                                        maskedDetails = upiIdInput.trim(),
                                        upiVpa = upiIdInput.trim()
                                    )
                                    onMethodAdded(newMethod)
                                },
                                enabled = upiIdInput.isNotBlank() && upiIsVerified,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SudhaniGoldPrimary,
                                    contentColor = SudhaniNavyDark
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save & Use", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // Card Form (Debit or Credit)
                    val isCredit = selectedTab == 2
                    val cardTypeLabel = if (isCredit) "Credit Card" else "Debit Card"

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardBankName,
                            onValueChange = { cardBankName = it },
                            label = { Text("Bank / Card Nickname") },
                            placeholder = { Text("e.g. HDFC Bank, SBI, ICICI") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = cardHolderName,
                            onValueChange = { cardHolderName = it },
                            label = { Text("Name on Card") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = cardNumberInput,
                            onValueChange = {
                                val digits = it.filter { c -> c.isDigit() }.take(16)
                                cardNumberInput = digits.chunked(4).joinToString(" ")
                            },
                            label = { Text("Card Number (16 Digits)") },
                            placeholder = { Text("•••• •••• •••• ••••") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = cardExpiryInput,
                                onValueChange = {
                                    val digits = it.filter { c -> c.isDigit() }.take(4)
                                    cardExpiryInput = if (digits.length >= 2) "${digits.take(2)}/${digits.drop(2)}" else digits
                                },
                                label = { Text("Exp (MM/YY)") },
                                placeholder = { Text("12/28") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = cardCvvInput,
                                onValueChange = { cardCvvInput = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text("CVV (3 Digits)") },
                                placeholder = { Text("•••") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (cardErrorMessage != null) {
                            Text(
                                text = cardErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }

                        // RBI compliance note
                        Surface(
                            color = SudhaniTheme.colors.chipBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = SudhaniGoldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RBI Tokenized: Full card and CVV are never stored.",
                                    fontSize = 10.sp,
                                    color = SudhaniTheme.colors.textSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isTokenizingCard = true
                                    cardErrorMessage = null
                                    val cleanDigits = cardNumberInput.filter { it.isDigit() }
                                    val last4 = if (cleanDigits.length >= 4) cleanDigits.takeLast(4) else "1234"
                                    val network = when {
                                        cleanDigits.startsWith("4") -> "VISA"
                                        cleanDigits.startsWith("5") -> "MASTERCARD"
                                        cleanDigits.startsWith("6") || cleanDigits.startsWith("8") -> "RUPAY"
                                        else -> "VISA"
                                    }
                                    val expMonth = cardExpiryInput.take(2)
                                    val expYear = cardExpiryInput.takeLast(2)

                                    val tokenResult = RazorpayService.tokenizeCardSecurely(
                                        bankNickname = cardBankName.ifBlank { "Bank Card" },
                                        cardType = if (isCredit) "CREDIT_CARD" else "DEBIT_CARD",
                                        cardholderName = cardHolderName.ifBlank { "Cardholder" },
                                        lastFourDigits = last4,
                                        expiryMonth = expMonth,
                                        expiryYear = expYear,
                                        network = network
                                    )
                                    isTokenizingCard = false

                                    if (tokenResult.isSuccess) {
                                        val newMethod = SavedPaymentMethod(
                                            id = "SPM_CARD_${System.currentTimeMillis()}",
                                            title = "${cardBankName.ifBlank { "Bank" }} $cardTypeLabel",
                                            subtitle = "•••• •••• •••• $last4 ($network)",
                                            type = if (isCredit) "CREDIT_CARD" else "DEBIT_CARD",
                                            isDefault = false,
                                            bankOrProvider = cardBankName.ifBlank { "Bank Card" },
                                            maskedDetails = "•••• •••• •••• $last4",
                                            lastFourDigits = last4,
                                            cardNetwork = network,
                                            cardExpiry = cardExpiryInput,
                                            gatewayTokenRef = tokenResult.gatewayTokenRef
                                        )
                                        onMethodAdded(newMethod)
                                    } else {
                                        cardErrorMessage = tokenResult.errorMessage ?: "Failed to tokenize card"
                                    }
                                }
                            },
                            enabled = cardNumberInput.replace(" ", "").length == 16 &&
                                    cardExpiryInput.length >= 4 &&
                                    cardCvvInput.length == 3 &&
                                    !isTokenizingCard,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SudhaniGoldPrimary,
                                contentColor = SudhaniNavyDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isTokenizingCard) {
                                CircularProgressIndicator(color = SudhaniNavyDark, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Tokenize & Save Card", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
