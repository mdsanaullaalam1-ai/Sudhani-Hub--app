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
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminOrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val orders by SudhaniRepository.orders.collectAsState()
        var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
        var searchQuery by remember { mutableStateOf("") }

        val filteredOrders = remember(orders, selectedStatusFilter, searchQuery) {
            orders.filter { order ->
                val matchesStatus = selectedStatusFilter == null || order.orderStatus == selectedStatusFilter
                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isEmpty() ||
                        order.orderId.lowercase().contains(query) ||
                        order.customerName.lowercase().contains(query) ||
                        order.customerPhone.lowercase().contains(query)
                matchesStatus && matchesQuery
            }
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Orders Management",
                    subtitle = "${filteredOrders.size} of ${orders.size} orders",
                    onNavigateBack = onNavigateBack
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.ORDERS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> {}
                            AdminTab.PAYMENTS -> onNavigateToPayments()
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
                // Search Box
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                            placeholder = { Text("Search by Order ID, Customer Name, Mobile...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = HighDensitySlate500,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = HighDensitySlate500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = searchFieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_orders_search_input")
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
                                    label = { Text("All (${orders.size})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(OrderStatus.values()) { status ->
                                val count = orders.count { it.orderStatus == status }
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

                // Orders List
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = HighDensitySlate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No orders found matching criteria",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensitySlate700
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredOrders, key = { it.orderId }) { order ->
                            AdminOrderCard(
                                order = order,
                                onCardClick = { onNavigateToOrderDetails(order.orderId) },
                                onAdvanceStatus = { SudhaniRepository.advanceOrderStatus(order.orderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onCardClick: () -> Unit,
    onAdvanceStatus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("admin_order_card_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = HighDensityIndigo
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${AdminDateUtils.formatDateTime(order.createdAt)}",
                        fontSize = 10.sp,
                        color = HighDensitySlate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (order.orderStatus) {
                        OrderStatus.DELIVERED -> HighDensityEmerald.copy(alpha = 0.15f)
                        OrderStatus.CANCELLED -> HighDensityRed.copy(alpha = 0.15f)
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7).copy(alpha = 0.15f)
                        else -> HighDensityOrange.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = order.orderStatus.displayName.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = when (order.orderStatus) {
                            OrderStatus.DELIVERED -> HighDensityEmerald
                            OrderStatus.CANCELLED -> HighDensityRed
                            OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7)
                            else -> HighDensityOrange
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = HighDensitySlate500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${order.customerName} (${order.customerPhone})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensitySlate900
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Address Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = HighDensitySlate400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.deliveryAddress,
                    fontSize = 10.sp,
                    color = HighDensitySlate600,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Summary
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HighDensitySlate50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "ITEMS (${order.items.sumOf { it.quantity }} items):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensitySlate500
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.items.joinToString(", ") { "${it.productImage} ${it.productName} ×${it.quantity}" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = HighDensitySlate800,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Price Breakdown & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total: ₹${order.totalAmount.toInt()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = HighDensitySlate900
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = if (order.paymentStatus.equals("Paid", ignoreCase = true) || order.paymentStatus.equals("Success", ignoreCase = true))
                                HighDensityEmerald.copy(alpha = 0.15f)
                            else HighDensityOrange.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${order.paymentMethod} • ${order.paymentStatus}".uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.paymentStatus.equals("Paid", ignoreCase = true) || order.paymentStatus.equals("Success", ignoreCase = true))
                                    HighDensityEmerald
                                else HighDensityOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (order.deliveryPartnerName != null) {
                        Text(
                            text = "🚴 Partner: ${order.deliveryPartnerName}",
                            fontSize = 9.sp,
                            color = HighDensityIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.orderStatus != OrderStatus.DELIVERED && order.orderStatus != OrderStatus.CANCELLED) {
                        Button(
                            onClick = onAdvanceStatus,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("ADVANCE STATUS", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onCardClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Details",
                            tint = HighDensitySlate500,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
