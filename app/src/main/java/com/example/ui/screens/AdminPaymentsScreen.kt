package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminPaymentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val payments by SudhaniRepository.payments.collectAsState()
        var selectedStatusFilter by remember { mutableStateOf<PaymentStatus?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedPaymentForAction by remember { mutableStateOf<Payment?>(null) }

        val totalCollected = payments.filter { it.paymentStatus == PaymentStatus.SUCCESS }.sumOf { it.amount }
        val totalPending = payments.filter { it.paymentStatus == PaymentStatus.PENDING }.sumOf { it.amount }
        val totalRefunded = payments.filter { it.paymentStatus == PaymentStatus.REFUNDED }.sumOf { it.amount }

        val filteredPayments = remember(payments, selectedStatusFilter, searchQuery) {
            payments.filter { pay ->
                val matchesStatus = selectedStatusFilter == null || pay.paymentStatus == selectedStatusFilter
                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isEmpty() ||
                        pay.paymentId.lowercase().contains(query) ||
                        pay.orderId.lowercase().contains(query) ||
                        pay.customerName.lowercase().contains(query)
                matchesStatus && matchesQuery
            }
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Payment Records",
                    subtitle = "Financial Transactions & Settlements",
                    onNavigateBack = onNavigateBack
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.PAYMENTS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> {}
                            AdminTab.CUSTOMERS -> onNavigateToCustomers()
                            AdminTab.PRODUCTS -> onNavigateToProducts()
                            AdminTab.DELIVERY -> onNavigateToDelivery()
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
            ) {
                // Summary Metrics
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminPaymentSummaryBox(
                                label = "Collected",
                                amount = "₹${totalCollected.toInt()}",
                                count = "${payments.count { it.paymentStatus == PaymentStatus.SUCCESS }} txns",
                                color = HighDensityEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            AdminPaymentSummaryBox(
                                label = "Pending (COD)",
                                amount = "₹${totalPending.toInt()}",
                                count = "${payments.count { it.paymentStatus == PaymentStatus.PENDING }} txns",
                                color = HighDensityOrange,
                                modifier = Modifier.weight(1f)
                            )
                            AdminPaymentSummaryBox(
                                label = "Refunded",
                                amount = "₹${totalRefunded.toInt()}",
                                count = "${payments.count { it.paymentStatus == PaymentStatus.REFUNDED }} txns",
                                color = HighDensityRed,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search
                        val searchFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedContainerColor = HighDensitySlate50,
                            unfocusedContainerColor = HighDensitySlate50
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by Payment ID, Order ID, Customer...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = HighDensitySlate500, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            colors = searchFieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_payments_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedStatusFilter == null,
                                    onClick = { selectedStatusFilter = null },
                                    label = { Text("All (${payments.size})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(PaymentStatus.values()) { status ->
                                val count = payments.count { it.paymentStatus == status }
                                FilterChip(
                                    selected = selectedStatusFilter == status,
                                    onClick = { selectedStatusFilter = status },
                                    label = { Text("${status.displayName} ($count)", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Security Callout
                Surface(
                    color = HighDensitySlate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = HighDensityEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Secure Architecture: No sensitive customer passwords, OTPs, CVV, or card numbers stored.",
                            fontSize = 9.sp,
                            color = HighDensitySlate600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Payments List
                if (filteredPayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No payment transactions found.", fontSize = 12.sp, color = HighDensitySlate500)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPayments, key = { it.paymentId }) { payment ->
                            AdminPaymentCard(
                                payment = payment,
                                onActionClick = { selectedPaymentForAction = payment },
                                onViewOrder = { onNavigateToOrderDetails(payment.orderId) }
                            )
                        }
                    }
                }
            }
        }

        // Action Dialog to change payment status
        if (selectedPaymentForAction != null) {
            val p = selectedPaymentForAction!!
            AlertDialog(
                onDismissRequest = { selectedPaymentForAction = null },
                title = { Text("Update Payment Status #${p.paymentId}", fontSize = 13.sp, fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Current: ${p.paymentStatus.displayName} • ₹${p.amount.toInt()} (${p.paymentMethod})",
                            fontSize = 11.sp,
                            color = HighDensitySlate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PaymentStatus.values().forEach { st ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (p.paymentStatus == st) HighDensityIndigo.copy(alpha = 0.12f) else HighDensitySlate50,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SudhaniRepository.updatePaymentStatus(p.paymentId, st)
                                        selectedPaymentForAction = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(st.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (p.paymentStatus == st) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedPaymentForAction = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminPaymentSummaryBox(
    label: String,
    amount: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.Black, color = HighDensitySlate900)
            Text(text = count, fontSize = 9.sp, color = HighDensitySlate500)
        }
    }
}

@Composable
fun AdminPaymentCard(
    payment: Payment,
    onActionClick: () -> Unit,
    onViewOrder: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.paymentId,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = HighDensityIndigo
                    )
                    Text(
                        text = "Ref: ${payment.transactionRef}",
                        fontSize = 9.sp,
                        color = HighDensitySlate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (payment.paymentStatus) {
                        PaymentStatus.SUCCESS, PaymentStatus.PAID -> HighDensityEmerald.copy(alpha = 0.15f)
                        PaymentStatus.PROCESSING, PaymentStatus.PENDING -> HighDensityOrange.copy(alpha = 0.15f)
                        PaymentStatus.FAILED -> HighDensityRed.copy(alpha = 0.15f)
                        PaymentStatus.REFUNDED -> Color(0xFF9333EA).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = payment.paymentStatus.displayName.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        color = when (payment.paymentStatus) {
                            PaymentStatus.SUCCESS, PaymentStatus.PAID -> HighDensityEmerald
                            PaymentStatus.PROCESSING, PaymentStatus.PENDING -> HighDensityOrange
                            PaymentStatus.FAILED -> HighDensityRed
                            PaymentStatus.REFUNDED -> Color(0xFF9333EA)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${payment.customerName} • ${payment.paymentMethod}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySlate800
                    )
                    Text(
                        text = "${AdminDateUtils.formatDate(payment.createdAt)} at ${AdminDateUtils.formatTime(payment.createdAt)}",
                        fontSize = 10.sp,
                        color = HighDensitySlate500
                    )
                }

                Text(
                    text = "₹${payment.amount.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = HighDensitySlate900
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = HighDensitySlate100)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewOrder,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("View Order #${payment.orderId}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityIndigo)
                }

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensitySlate800),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Update Status", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
