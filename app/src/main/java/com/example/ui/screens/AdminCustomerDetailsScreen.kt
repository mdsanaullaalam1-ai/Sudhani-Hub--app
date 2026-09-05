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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminCustomerDetailsScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val customers by SudhaniRepository.customers.collectAsState()
        val customer = customers.find { it.customerId == customerId }
        val orders by SudhaniRepository.orders.collectAsState()
        val customerOrders = orders.filter { it.customerId == customerId }

        if (customer == null) {
            Scaffold(
                topBar = {
                    AdminTopAppBar(
                        title = "Customer Profile",
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
                    Text("Customer ID #$customerId not found.", color = HighDensitySlate600)
                }
            }
            return@AdminAccessGuard
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = customer.name,
                    subtitle = "Account ID: ${customer.customerId}",
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
                // Profile Card
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(HighDensityIndigo.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = HighDensityIndigo
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = HighDensitySlate900
                                        )
                                        Text(
                                            text = "Role: ${customer.role.name} • Registered ${AdminDateUtils.formatDate(customer.registeredAt)}",
                                            fontSize = 10.sp,
                                            color = HighDensitySlate500
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (customer.isActive) HighDensityEmerald.copy(alpha = 0.15f) else HighDensityRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (customer.isActive) "ACTIVE" else "INACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (customer.isActive) HighDensityEmerald else HighDensityRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = HighDensitySlate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Phone: ${customer.phone}", fontSize = 12.sp, color = HighDensitySlate800)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = HighDensitySlate600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email: ${customer.email}", fontSize = 12.sp, color = HighDensitySlate800)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    SudhaniRepository.toggleCustomerStatus(customer.customerId, !customer.isActive)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customer.isActive) HighDensityRed else HighDensityEmerald
                                ),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text(
                                    text = if (customer.isActive) "DEACTIVATE CUSTOMER ACCOUNT" else "ACTIVATE CUSTOMER ACCOUNT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Spending Summary Box
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("TOTAL ORDERS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500)
                                Text("${customerOrders.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HighDensitySlate900)
                                Text("Placed on SudhaniHub", fontSize = 9.sp, color = HighDensitySlate500)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("TOTAL SPENT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500)
                                Text("₹${customerOrders.sumOf { it.totalAmount }.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HighDensityEmerald)
                                Text("Delivered & In-flight", fontSize = 9.sp, color = HighDensitySlate500)
                            }
                        }
                    }
                }

                // Orders History Section
                item {
                    Text(
                        text = "ORDER HISTORY (${customerOrders.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = HighDensitySlate700
                    )
                }

                if (customerOrders.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                Text("No orders found for this customer.", fontSize = 12.sp, color = HighDensitySlate500)
                            }
                        }
                    }
                }

                items(customerOrders, key = { it.orderId }) { ord ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToOrderDetails(ord.orderId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${ord.orderId}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = HighDensityIndigo
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = when (ord.orderStatus) {
                                            OrderStatus.DELIVERED -> HighDensityEmerald.copy(alpha = 0.15f)
                                            OrderStatus.CANCELLED -> HighDensityRed.copy(alpha = 0.15f)
                                            else -> HighDensityOrange.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = ord.orderStatus.displayName.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (ord.orderStatus) {
                                                OrderStatus.DELIVERED -> HighDensityEmerald
                                                OrderStatus.CANCELLED -> HighDensityRed
                                                else -> HighDensityOrange
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${ord.items.size} items • ${AdminDateUtils.formatDateTime(ord.createdAt)}",
                                    fontSize = 10.sp,
                                    color = HighDensitySlate500
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${ord.totalAmount.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = HighDensitySlate900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HighDensitySlate400, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
