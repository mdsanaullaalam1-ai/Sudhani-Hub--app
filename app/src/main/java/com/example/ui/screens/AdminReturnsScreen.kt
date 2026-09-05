package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToDelivery: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val context = LocalContext.current
        val returnRequests by SudhaniRepository.returnRequests.collectAsState()
        val policyConfig by SudhaniRepository.returnPolicyConfig.collectAsState()
        val partners by SudhaniRepository.deliveryPartners.collectAsState()
        val orders by SudhaniRepository.orders.collectAsState()

        var searchQuery by remember { mutableStateOf("") }
        var activeFilter by remember { mutableStateOf("All") }
        var showPolicyDialog by remember { mutableStateOf(false) }

        // Action Dialog States
        var activeActionReturn by remember { mutableStateOf<ReturnRequest?>(null) }
        var showApproveDialog by remember { mutableStateOf(false) }
        var showRejectDialog by remember { mutableStateOf(false) }
        var showSchedulePickupDialog by remember { mutableStateOf(false) }
        var showProcessRefundDialog by remember { mutableStateOf(false) }
        var showPhotoPreviewDialog by remember { mutableStateOf<String?>(null) }

        // Filter calculation
        val pendingCount = returnRequests.count { it.status == ReturnStatus.REQUESTED || it.status == ReturnStatus.UNDER_REVIEW }
        val pickupCount = returnRequests.count { it.status == ReturnStatus.APPROVED || it.status == ReturnStatus.PICKUP_SCHEDULED }
        val receivedCount = returnRequests.count { it.status == ReturnStatus.PICKED_UP || it.status == ReturnStatus.PRODUCT_RECEIVED }
        val refundProcessingCount = returnRequests.count { it.status == ReturnStatus.REFUND_PROCESSING }
        val completedRefundCount = returnRequests.count { it.status == ReturnStatus.REFUNDED }
        val rejectedCount = returnRequests.count { it.status == ReturnStatus.REJECTED }

        val filteredList = remember(returnRequests, searchQuery, activeFilter) {
            returnRequests.filter { req ->
                val matchesQuery = searchQuery.isBlank() ||
                        req.returnId.contains(searchQuery, ignoreCase = true) ||
                        req.orderId.contains(searchQuery, ignoreCase = true) ||
                        req.customerName.contains(searchQuery, ignoreCase = true) ||
                        req.customerPhone.contains(searchQuery, ignoreCase = true) ||
                        req.productName.contains(searchQuery, ignoreCase = true) ||
                        req.reason.contains(searchQuery, ignoreCase = true)

                val matchesTab = when (activeFilter) {
                    "Pending" -> req.status == ReturnStatus.REQUESTED || req.status == ReturnStatus.UNDER_REVIEW
                    "Pickup" -> req.status == ReturnStatus.APPROVED || req.status == ReturnStatus.PICKUP_SCHEDULED
                    "Received" -> req.status == ReturnStatus.PICKED_UP || req.status == ReturnStatus.PRODUCT_RECEIVED
                    "Refunded" -> req.status == ReturnStatus.REFUND_PROCESSING || req.status == ReturnStatus.REFUNDED
                    "Rejected" -> req.status == ReturnStatus.REJECTED
                    else -> true
                }

                matchesQuery && matchesTab
            }
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Product Return Management",
                    subtitle = "${returnRequests.size} total requests • $pendingCount pending review",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        IconButton(
                            onClick = { showPolicyDialog = true },
                            modifier = Modifier.testTag("return_policy_settings_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Return Policy Settings", tint = Color.White)
                        }
                    }
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.RETURNS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.RETURNS -> {}
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> onNavigateToPayments()
                            AdminTab.CUSTOMERS -> onNavigateToCustomers()
                            AdminTab.PRODUCTS -> onNavigateToProducts()
                            AdminTab.DELIVERY -> onNavigateToDelivery()
                            AdminTab.SETTINGS -> onNavigateToSettings()
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Return Policy Highlight Bar
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(HighDensityIndigo.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Policy, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Store Return Window: ${policyConfig.defaultReturnWindowDays} Days",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = HighDensitySlate900
                                    )
                                    Text(
                                        text = "Delivery fee refund: ${if (policyConfig.refundDeliveryFeeByDefault) "Enabled" else "Disabled (Admin Discretion)"}",
                                        fontSize = 11.sp,
                                        color = HighDensitySlate600
                                    )
                                }
                            }
                            TextButton(onClick = { showPolicyDialog = true }) {
                                Text("Configure", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityIndigo)
                            }
                        }
                    }
                }

                // Metrics Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReturnMetricCard(
                            label = "Pending",
                            count = pendingCount,
                            color = HighDensityAmber,
                            modifier = Modifier.weight(1f)
                        )
                        ReturnMetricCard(
                            label = "Pickup",
                            count = pickupCount,
                            color = HighDensityIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        ReturnMetricCard(
                            label = "Received",
                            count = receivedCount,
                            color = HighDensityBlue,
                            modifier = Modifier.weight(1f)
                        )
                        ReturnMetricCard(
                            label = "Refunded",
                            count = completedRefundCount,
                            color = HighDensityEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Return ID, Order ID, Customer, Product...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Filter Tabs
                item {
                    val tabs = listOf(
                        "All" to returnRequests.size,
                        "Pending" to pendingCount,
                        "Pickup" to pickupCount,
                        "Received" to receivedCount,
                        "Refunded" to (completedRefundCount + refundProcessingCount),
                        "Rejected" to rejectedCount
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tabs) { (tabName, count) ->
                            FilterChip(
                                selected = activeFilter == tabName,
                                onClick = { activeFilter = tabName },
                                label = {
                                    Text("$tabName ($count)", fontSize = 12.sp, fontWeight = if (activeFilter == tabName) FontWeight.Bold else FontWeight.Medium)
                                }
                            )
                        }
                    }
                }

                // Return Request Cards List
                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = HighDensitySlate400, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No return requests match filter.", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = HighDensitySlate700)
                            }
                        }
                    }
                } else {
                    items(filteredList) { request ->
                        val relatedOrder = orders.find { it.orderId == request.orderId }
                        AdminReturnCard(
                            request = request,
                            order = relatedOrder,
                            onApprove = {
                                activeActionReturn = request
                                showApproveDialog = true
                            },
                            onReject = {
                                activeActionReturn = request
                                showRejectDialog = true
                            },
                            onSchedulePickup = {
                                activeActionReturn = request
                                showSchedulePickupDialog = true
                            },
                            onMarkPickedUp = {
                                SudhaniRepository.markPickupCompleted(request.returnId)
                                Toast.makeText(context, "Item marked Picked Up", Toast.LENGTH_SHORT).show()
                            },
                            onMarkProductReceived = {
                                SudhaniRepository.markProductReceived(request.returnId)
                                Toast.makeText(context, "Product marked Received at Hub", Toast.LENGTH_SHORT).show()
                            },
                            onProcessRefund = {
                                activeActionReturn = request
                                showProcessRefundDialog = true
                            },
                            onViewPhoto = { photoName ->
                                showPhotoPreviewDialog = photoName
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // DIALOGS
        // ==========================================

        // Policy Settings Dialog
        if (showPolicyDialog) {
            ReturnPolicySettingsDialog(
                currentConfig = policyConfig,
                onDismiss = { showPolicyDialog = false },
                onSave = { updatedConfig ->
                    SudhaniRepository.updateReturnPolicyConfig(updatedConfig)
                    showPolicyDialog = false
                    Toast.makeText(context, "Return Policy Updated", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Approve Dialog
        if (showApproveDialog && activeActionReturn != null) {
            val req = activeActionReturn!!
            var adminNotes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showApproveDialog = false },
                title = { Text("Approve Return Request", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Approve return #${req.returnId} for ${req.productName}?")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "The customer will be notified that the return is approved. You can then schedule reverse pickup.",
                            fontSize = 12.sp,
                            color = HighDensitySlate600
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = adminNotes,
                            onValueChange = { adminNotes = it },
                            label = { Text("Admin Notes (Optional)", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Approved. Customer confirmed sealed box.", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            SudhaniRepository.approveReturn(req.returnId, adminNotes.ifBlank { null })
                            showApproveDialog = false
                            Toast.makeText(context, "Return Request Approved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityEmerald)
                    ) {
                        Text("Confirm Approval")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApproveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Reject Dialog
        if (showRejectDialog && activeActionReturn != null) {
            val req = activeActionReturn!!
            var rejectionReason by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Decline Return Request", fontWeight = FontWeight.Bold, color = HighDensityRed) },
                text = {
                    Column {
                        Text("Provide reason for declining return #${req.returnId}:")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Decline Reason *", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Seal opened, past return policy, photo does not show defect", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionReason.isBlank()) {
                                Toast.makeText(context, "Please enter rejection reason", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            SudhaniRepository.rejectReturn(req.returnId, rejectionReason)
                            showRejectDialog = false
                            Toast.makeText(context, "Return Request Declined", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityRed)
                    ) {
                        Text("Decline Return")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Schedule Reverse Pickup Dialog
        if (showSchedulePickupDialog && activeActionReturn != null) {
            val req = activeActionReturn!!
            var selectedSlot by remember { mutableStateOf("Tomorrow (10:00 AM - 01:00 PM)") }
            var selectedPartnerId by remember { mutableStateOf(partners.firstOrNull()?.partnerId ?: "") }

            val slots = listOf(
                "Today (04:00 PM - 07:00 PM)",
                "Tomorrow (10:00 AM - 01:00 PM)",
                "Tomorrow (02:00 PM - 05:00 PM)",
                "Day After Tomorrow (10:00 AM - 01:00 PM)"
            )

            AlertDialog(
                onDismissRequest = { showSchedulePickupDialog = false },
                title = { Text("Schedule Reverse Pickup", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Assign a delivery partner to pick up the item from customer's address:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Pickup Time Slot:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        slots.forEach { slot ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedSlot == slot) HighDensityIndigo.copy(alpha = 0.1f) else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedSlot == slot) HighDensityIndigo else HighDensitySlate300),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedSlot = slot }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selectedSlot == slot, onClick = { selectedSlot = slot })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(slot, fontSize = 12.sp, fontWeight = if (selectedSlot == slot) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Assign Pickup Rider:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        partners.forEach { partner ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPartnerId == partner.partnerId) HighDensityIndigo.copy(alpha = 0.1f) else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPartnerId == partner.partnerId) HighDensityIndigo else HighDensitySlate300),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedPartnerId = partner.partnerId }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selectedPartnerId == partner.partnerId, onClick = { selectedPartnerId = partner.partnerId })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(partner.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${partner.phone} • ${partner.vehicle}", fontSize = 10.sp, color = HighDensitySlate600)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val partner = partners.find { it.partnerId == selectedPartnerId } ?: partners.firstOrNull()
                            SudhaniRepository.schedulePickup(
                                returnId = req.returnId,
                                pickupDate = System.currentTimeMillis() + 86400000L,
                                timeSlot = selectedSlot,
                                partnerName = partner?.name ?: "Sudhani Hub Rider",
                                partnerPhone = partner?.phone ?: "+91 98765 43210"
                            )
                            showSchedulePickupDialog = false
                            Toast.makeText(context, "Reverse Pickup Scheduled", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo)
                    ) {
                        Text("Confirm Pickup Schedule")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSchedulePickupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Process Refund Dialog
        if (showProcessRefundDialog && activeActionReturn != null) {
            val req = activeActionReturn!!
            val relatedOrder = orders.find { it.orderId == req.orderId }
            val orderDeliveryFee = relatedOrder?.deliveryFee ?: 0.0

            var refundAmountText by remember { mutableStateOf(req.refundAmount.toInt().toString()) }
            var refundDeliveryFee by remember { mutableStateOf(policyConfig.refundDeliveryFeeByDefault) }
            var selectedRefundMethod by remember { mutableStateOf(req.refundMethod.ifBlank { "Original UPI" }) }
            var transactionRef by remember { mutableStateOf("TXN_REF_${System.currentTimeMillis().toString().takeLast(6)}") }

            val itemRefund = refundAmountText.toDoubleOrNull() ?: 0.0
            val deliveryRefund = if (refundDeliveryFee) orderDeliveryFee else 0.0
            val totalFinalRefund = itemRefund + deliveryRefund

            AlertDialog(
                onDismissRequest = { showProcessRefundDialog = false },
                title = { Text("Process Customer Refund", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Return ID: ${req.returnId} • ${req.productName}", fontSize = 12.sp, color = HighDensitySlate700)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = refundAmountText,
                            onValueChange = { refundAmountText = it },
                            label = { Text("Product Refund Amount (₹)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delivery Fee Decision (Admin Control!)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighDensitySlate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Refund Delivery Fee (+₹${orderDeliveryFee.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Admin discretionary decision", fontSize = 10.sp, color = HighDensitySlate600)
                                }
                                Switch(
                                    checked = refundDeliveryFee,
                                    onCheckedChange = { refundDeliveryFee = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Total Refund to Issue: ₹${totalFinalRefund.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = HighDensityEmerald)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Refund Destination
                        Text("Refund Destination Method:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val methods = listOf("Original UPI", "SudhaniHub Wallet", "Original Card", "Cash / Offline")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(methods) { method ->
                                FilterChip(
                                    selected = selectedRefundMethod == method,
                                    onClick = { selectedRefundMethod = method },
                                    label = { Text(method, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = transactionRef,
                            onValueChange = { transactionRef = it },
                            label = { Text("Payment Gateway Reference ID", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            SudhaniRepository.approveRefund(
                                returnId = req.returnId,
                                refundAmount = itemRefund,
                                refundDeliveryFee = refundDeliveryFee,
                                refundMethod = selectedRefundMethod
                            )
                            SudhaniRepository.markRefundCompleted(
                                returnId = req.returnId,
                                transactionRef = transactionRef
                            )
                            showProcessRefundDialog = false
                            Toast.makeText(context, "Refund of ₹${totalFinalRefund.toInt()} processed successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityEmerald)
                    ) {
                        Text("Issue Refund (₹${totalFinalRefund.toInt()})")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProcessRefundDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Photo Preview Dialog
        if (showPhotoPreviewDialog != null) {
            AlertDialog(
                onDismissRequest = { showPhotoPreviewDialog = null },
                title = { Text("Product Defect Photo", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HighDensitySlate100)
                                .border(1.dp, HighDensitySlate300, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = HighDensitySlate500, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(showPhotoPreviewDialog ?: "", fontSize = 11.sp, color = HighDensitySlate700)
                                Text("Customer Proof Verified", fontSize = 10.sp, color = HighDensityEmerald, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showPhotoPreviewDialog = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ReturnMetricCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = HighDensitySlate600
            )
        }
    }
}

@Composable
fun AdminReturnCard(
    request: ReturnRequest,
    order: Order?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSchedulePickup: () -> Unit,
    onMarkPickedUp: () -> Unit,
    onMarkProductReceived: () -> Unit,
    onProcessRefund: () -> Unit,
    onViewPhoto: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Return ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.returnId,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = HighDensitySlate900
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HighDensitySlate100
                        ) {
                            Text(
                                text = "Order #${request.orderId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensitySlate600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Requested: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(request.requestedAt))}",
                        fontSize = 11.sp,
                        color = HighDensitySlate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (request.status) {
                        ReturnStatus.REQUESTED -> HighDensityAmber.copy(alpha = 0.15f)
                        ReturnStatus.UNDER_REVIEW -> HighDensityIndigo.copy(alpha = 0.15f)
                        ReturnStatus.APPROVED -> HighDensityBlue.copy(alpha = 0.15f)
                        ReturnStatus.PICKUP_SCHEDULED -> HighDensityIndigo.copy(alpha = 0.15f)
                        ReturnStatus.PICKED_UP -> HighDensityCyan.copy(alpha = 0.15f)
                        ReturnStatus.PRODUCT_RECEIVED -> HighDensityPurple.copy(alpha = 0.15f)
                        ReturnStatus.REFUND_PROCESSING -> HighDensityAmber.copy(alpha = 0.15f)
                        ReturnStatus.REFUNDED -> HighDensityEmerald.copy(alpha = 0.15f)
                        ReturnStatus.REJECTED -> HighDensityRed.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = request.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (request.status) {
                            ReturnStatus.REQUESTED -> HighDensityAmber
                            ReturnStatus.UNDER_REVIEW -> HighDensityIndigo
                            ReturnStatus.APPROVED -> HighDensityBlue
                            ReturnStatus.PICKUP_SCHEDULED -> HighDensityIndigo
                            ReturnStatus.PICKED_UP -> HighDensityCyan
                            ReturnStatus.PRODUCT_RECEIVED -> HighDensityPurple
                            ReturnStatus.REFUND_PROCESSING -> HighDensityAmber
                            ReturnStatus.REFUNDED -> HighDensityEmerald
                            ReturnStatus.REJECTED -> HighDensityRed
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Product & Customer Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.productImage, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Qty: ${request.returnQuantity} • Item Refund: ₹${request.itemAmount.toInt()}", fontSize = 11.sp, color = HighDensitySlate600)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Refund",
                        fontSize = 10.sp,
                        color = HighDensitySlate500
                    )
                    Text(
                        text = "₹${request.totalRefund.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = HighDensityEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Details Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HighDensitySlate100,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.customerName} • ${request.customerPhone}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighDensitySlate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reason & Description
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HighDensitySlate50,
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HighDensityAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Reason: ${request.reason}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (request.customerDescription.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${request.customerDescription}\"",
                            fontSize = 11.sp,
                            color = HighDensitySlate700,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Photos
                    if (request.photos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Photos (${request.photos.size}):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate600)
                            request.photos.forEach { photo ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = HighDensityIndigo.copy(alpha = 0.1f),
                                    modifier = Modifier.clickable { onViewPhoto(photo) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Photo", fontSize = 10.sp, color = HighDensityIndigo, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reverse Pickup Details (if scheduled)
            if (request.pickupPartnerName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HighDensityIndigo.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pickup Slot: ${request.pickupTimeSlot ?: "Scheduled"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityIndigo
                            )
                            Text(
                                text = "Rider: ${request.pickupPartnerName} (${request.pickupPartnerPhone})",
                                fontSize = 10.sp,
                                color = HighDensitySlate700
                            )
                        }
                    }
                }
            }

            // Rejection reason (if rejected)
            if (request.status == ReturnStatus.REJECTED && request.rejectionReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HighDensityRed.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = HighDensityRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Declined Reason:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityRed)
                            Text(request.rejectionReason, fontSize = 11.sp, color = HighDensitySlate800)
                        }
                    }
                }
            }

            // Refund information (if completed)
            if (request.status == ReturnStatus.REFUNDED) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HighDensityEmerald.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HighDensityEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Refund Credited: ₹${request.totalRefund.toInt()} via ${request.refundMethod}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityEmerald
                            )
                            if (request.refundTransactionRef != null) {
                                Text("Txn Ref: ${request.refundTransactionRef}", fontSize = 10.sp, color = HighDensitySlate600)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (request.status) {
                    ReturnStatus.REQUESTED, ReturnStatus.UNDER_REVIEW -> {
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityEmerald),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ReturnStatus.APPROVED -> {
                        Button(
                            onClick = onSchedulePickup,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ElectricMoped, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Schedule Reverse Pickup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ReturnStatus.PICKUP_SCHEDULED -> {
                        Button(
                            onClick = onMarkPickedUp,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Item Picked Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ReturnStatus.PICKED_UP -> {
                        Button(
                            onClick = onMarkProductReceived,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DomainVerification, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Received at Fulfillment Center", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ReturnStatus.PRODUCT_RECEIVED, ReturnStatus.REFUND_PROCESSING -> {
                        Button(
                            onClick = onProcessRefund,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Process & Issue Refund", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ReturnStatus.REFUNDED -> {
                        OutlinedButton(
                            onClick = { /* already completed */ },
                            enabled = false,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refund Completed", fontSize = 12.sp)
                        }
                    }

                    ReturnStatus.REJECTED -> {
                        OutlinedButton(
                            onClick = { /* already declined */ },
                            enabled = false,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Return Declined", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReturnPolicySettingsDialog(
    currentConfig: ReturnPolicyConfig,
    onDismiss: () -> Unit,
    onSave: (ReturnPolicyConfig) -> Unit
) {
    var windowDaysText by remember { mutableStateOf<String>(currentConfig.defaultReturnWindowDays.toString()) }
    var isEnabled by remember { mutableStateOf<Boolean>(currentConfig.isReturnEnabled) }
    var refundDeliveryFee by remember { mutableStateOf<Boolean>(currentConfig.refundDeliveryFeeByDefault) }
    var requirePhotos by remember { mutableStateOf<Boolean>(currentConfig.requirePhotosForDamaged) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Store Return Policy Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Configure global return rules for SudhaniHub:", fontSize = 12.sp, color = HighDensitySlate600)

                // Return Window Days
                OutlinedTextField(
                    value = windowDaysText,
                    onValueChange = { windowDaysText = it },
                    label = { Text("Return Window (Days)", fontSize = 12.sp) },
                    supportingText = { Text("Default is 3 days from delivery date/time.", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 5, 7, 10).forEach { days ->
                        OutlinedButton(
                            onClick = { windowDaysText = days.toString() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("$days Days", fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider()

                // Allow Returns Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Customer Returns", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Allow customers to request returns from order details", fontSize = 11.sp, color = HighDensitySlate600)
                    }
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                // Refund Delivery Fee Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Refund Delivery Fee by Default", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Default policy is non-refundable; admin can still refund per request", fontSize = 11.sp, color = HighDensitySlate600)
                    }
                    Switch(checked = refundDeliveryFee, onCheckedChange = { refundDeliveryFee = it })
                }

                // Require Photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Require Photos for Damaged Items", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Mandatory proof for broken, spoiled, or wrong items", fontSize = 11.sp, color = HighDensitySlate600)
                    }
                    Switch(checked = requirePhotos, onCheckedChange = { requirePhotos = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = windowDaysText.toIntOrNull()?.coerceIn(1, 30) ?: 3
                    val updated = currentConfig.copy(
                        defaultReturnWindowDays = days,
                        isReturnEnabled = isEnabled,
                        refundDeliveryFeeByDefault = refundDeliveryFee,
                        requirePhotosForDamaged = requirePhotos
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
