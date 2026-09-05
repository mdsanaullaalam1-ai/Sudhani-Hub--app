package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.OrderStatus
import com.example.data.repository.SudhaniRepository
import com.example.data.service.PriceCalculationService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailsScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetails: (String) -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val orders by SudhaniRepository.orders.collectAsState()
        val order = orders.find { it.orderId == orderId }
        val partners by SudhaniRepository.deliveryPartners.collectAsState()

        var showAssignPartnerDialog by remember { mutableStateOf(false) }
        var showStatusDialog by remember { mutableStateOf(false) }
        var showOverrideFeeDialog by remember { mutableStateOf(false) }
        var overrideFeeInput by remember { mutableStateOf("") }

        if (order == null) {
            Scaffold(
                topBar = {
                    AdminTopAppBar(
                        title = "Order Details",
                        onNavigateBack = onNavigateBack
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Order #$orderId not found.", color = HighDensitySlate600)
                }
            }
            return@AdminAccessGuard
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Order #${order.orderId}",
                    subtitle = AdminDateUtils.formatDateTime(order.createdAt),
                    onNavigateBack = onNavigateBack
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Header & Quick Actions
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CURRENT ORDER STATUS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = HighDensitySlate500
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (order.orderStatus) {
                                            OrderStatus.DELIVERED -> HighDensityEmerald.copy(alpha = 0.15f)
                                            OrderStatus.CANCELLED -> HighDensityRed.copy(alpha = 0.15f)
                                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                            else -> HighDensityOrange.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = order.orderStatus.displayName.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = when (order.orderStatus) {
                                                OrderStatus.DELIVERED -> HighDensityEmerald
                                                OrderStatus.CANCELLED -> HighDensityRed
                                                OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7)
                                                else -> HighDensityOrange
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showStatusDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                                    modifier = Modifier.testTag("admin_change_status_button")
                                ) {
                                    Text("Change Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = HighDensitySlate200)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Step progress tracker
                            Text(
                                text = "LIFECYCLE PIPELINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = HighDensitySlate500
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val steps = listOf(
                                OrderStatus.NEW,
                                OrderStatus.CONFIRMED,
                                OrderStatus.PREPARING,
                                OrderStatus.READY_FOR_PICKUP,
                                OrderStatus.OUT_FOR_DELIVERY,
                                OrderStatus.DELIVERED
                            )
                            val currentIndex = steps.indexOf(order.orderStatus)

                            steps.forEachIndexed { index, st ->
                                val isDone = index <= currentIndex && order.orderStatus != OrderStatus.CANCELLED
                                val isCurrent = index == currentIndex
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) HighDensityIndigo
                                                else if (isDone) HighDensityEmerald
                                                else HighDensitySlate300
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = st.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal,
                                        color = if (isCurrent) HighDensityIndigo else HighDensitySlate800
                                    )
                                }
                            }
                        }
                    }
                }

                // Customer Information Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CUSTOMER INFORMATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = HighDensitySlate500
                                )
                                TextButton(
                                    onClick = { onNavigateToCustomerDetails(order.customerId) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View Profile", fontSize = 11.sp, color = HighDensityIndigo, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = order.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate900)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = order.customerPhone, fontSize = 12.sp, color = HighDensitySlate700)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = order.deliveryAddress, fontSize = 12.sp, color = HighDensitySlate700)
                            }
                        }
                    }
                }

                // Delivery Partner Assignment Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ASSIGNED DELIVERY PARTNER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = HighDensitySlate500
                                )
                                Button(
                                    onClick = { showAssignPartnerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Assign / Change", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (order.deliveryPartnerName != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = order.deliveryPartnerName!!, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensitySlate900)
                                        Text(text = "${order.deliveryPartnerPhone ?: ""} • ${order.deliveryPartnerVehicle ?: ""}", fontSize = 11.sp, color = HighDensitySlate600)
                                    }
                                }
                            } else {
                                Text("No delivery partner assigned yet.", fontSize = 12.sp, color = HighDensitySlate500)
                            }
                        }
                    }
                }

                // Products List
                item {
                    Text(
                        text = "ORDERED PRODUCTS (${order.items.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = HighDensitySlate700
                    )
                }

                items(order.items) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensitySlate100,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = item.productImage, fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = HighDensitySlate900
                                    )
                                    Text(
                                        text = "₹${item.price.toInt()} × ${item.quantity}",
                                        fontSize = 11.sp,
                                        color = HighDensitySlate600
                                    )
                                }
                            }

                            Text(
                                text = "₹${item.total.toInt()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = HighDensitySlate900
                            )
                        }
                    }
                }

                // Bill Breakdown Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PAYMENT & PRICE BREAKDOWN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = HighDensitySlate500
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 12.sp, color = HighDensitySlate700)
                                Text("₹${order.subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensitySlate900)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Order Weight", fontSize = 12.sp, color = HighDensitySlate700)
                                Text("${PriceCalculationService.formatWeight(order.totalWeightKg ?: 1.0)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensitySlate900)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Distance", fontSize = 12.sp, color = HighDensitySlate700)
                                Text("${PriceCalculationService.formatDistance(order.deliveryDistanceKm ?: 0.0)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HighDensitySlate900)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Delivery Charge", fontSize = 12.sp, color = HighDensitySlate700)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = {
                                            overrideFeeInput = order.deliveryFee.toInt().toString()
                                            showOverrideFeeDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Override", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityIndigo)
                                    }
                                }
                                Text(
                                    if (order.deliveryFee == 0.0) "FREE" else "₹${order.deliveryFee.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HighDensityEmerald
                                )
                            }
                            if (order.discount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount (${order.couponCode ?: "Coupon"})", fontSize = 12.sp, color = HighDensityEmerald)
                                    Text("-₹${order.discount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = HighDensitySlate200)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Final Total", fontSize = 14.sp, fontWeight = FontWeight.Black, color = HighDensitySlate900)
                                Text("₹${order.totalAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = HighDensityIndigo)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payment Method", fontSize = 12.sp, color = HighDensitySlate700)
                                Text(order.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate900)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payment Status", fontSize = 12.sp, color = HighDensitySlate700)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (order.paymentStatus.equals("Paid", ignoreCase = true) || order.paymentStatus.equals("Success", ignoreCase = true))
                                        HighDensityEmerald.copy(alpha = 0.15f)
                                    else HighDensityOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = order.paymentStatus.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.paymentStatus.equals("Paid", ignoreCase = true) || order.paymentStatus.equals("Success", ignoreCase = true))
                                            HighDensityEmerald
                                        else HighDensityOrange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status Change Dialog
        if (showStatusDialog) {
            AlertDialog(
                onDismissRequest = { showStatusDialog = false },
                title = { Text("Update Order Status", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OrderStatus.values().forEach { status ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (order.orderStatus == status) HighDensityIndigo.copy(alpha = 0.12f) else HighDensitySlate50,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SudhaniRepository.updateOrderStatus(order.orderId, status)
                                        showStatusDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = status.displayName,
                                        fontWeight = if (order.orderStatus == status) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (order.orderStatus == status) HighDensityIndigo else HighDensitySlate800
                                    )
                                    if (order.orderStatus == status) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatusDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Assign Delivery Partner Dialog
        if (showAssignPartnerDialog) {
            AlertDialog(
                onDismissRequest = { showAssignPartnerDialog = false },
                title = { Text("Assign Delivery Partner", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        partners.forEach { partner ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (order.deliveryPartnerId == partner.partnerId) HighDensityIndigo.copy(alpha = 0.12f) else HighDensitySlate50,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SudhaniRepository.assignDeliveryPartner(order.orderId, partner.partnerId)
                                        showAssignPartnerDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = partner.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensitySlate900)
                                        Text(text = "${partner.vehicle} • ${partner.phone}", fontSize = 10.sp, color = HighDensitySlate600)
                                        Text(text = if (partner.isAvailable) "🟢 Available" else "🟡 Busy (${partner.activeDeliveriesCount} active)", fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (order.deliveryPartnerId == partner.partnerId) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAssignPartnerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Manual Override Delivery Fee Dialog
        if (showOverrideFeeDialog) {
            AlertDialog(
                onDismissRequest = { showOverrideFeeDialog = false },
                title = {
                    Column {
                        Text("Override Delivery Charge", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("Order #${order.orderId}", fontSize = 11.sp, color = HighDensitySlate500)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Admin can manually set or waive delivery charge for this specific order. Total payable will automatically adjust.",
                            fontSize = 12.sp,
                            color = HighDensitySlate600
                        )

                        OutlinedTextField(
                            value = overrideFeeInput,
                            onValueChange = { overrideFeeInput = it },
                            label = { Text("New Delivery Fee (₹)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0, 25, 35, 50, 70).forEach { fee ->
                                OutlinedButton(
                                    onClick = { overrideFeeInput = fee.toString() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                ) {
                                    Text(if (fee == 0) "FREE" else "₹$fee", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newFee = overrideFeeInput.toDoubleOrNull() ?: 0.0
                            SudhaniRepository.overrideOrderDeliveryFee(order.orderId, newFee)
                            showOverrideFeeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo)
                    ) {
                        Text("Save Override", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOverrideFeeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
