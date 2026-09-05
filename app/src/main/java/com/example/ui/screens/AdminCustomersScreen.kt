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
import com.example.data.model.Customer
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminCustomersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetails: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val customers by SudhaniRepository.customers.collectAsState()
        var searchQuery by remember { mutableStateOf("") }
        var activeFilter by remember { mutableStateOf<Boolean?>(null) } // null = all, true = active, false = inactive

        val filteredCustomers = remember(customers, searchQuery, activeFilter) {
            customers.filter { c ->
                val matchesActive = activeFilter == null || c.isActive == activeFilter
                val q = searchQuery.trim().lowercase()
                val matchesQuery = q.isEmpty() ||
                        c.name.lowercase().contains(q) ||
                        c.phone.lowercase().contains(q) ||
                        c.email.lowercase().contains(q) ||
                        c.customerId.lowercase().contains(q)
                matchesActive && matchesQuery
            }
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Customer Accounts",
                    subtitle = "${filteredCustomers.size} registered accounts",
                    onNavigateBack = onNavigateBack
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.CUSTOMERS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> onNavigateToPayments()
                            AdminTab.CUSTOMERS -> {}
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
                // Search & Filter Surface
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
                            placeholder = { Text("Search by name, phone, email, customer ID...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = HighDensitySlate500, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            colors = searchFieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_customers_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = activeFilter == null,
                                    onClick = { activeFilter = null },
                                    label = { Text("All (${customers.size})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = activeFilter == true,
                                    onClick = { activeFilter = true },
                                    label = { Text("Active (${customers.count { it.isActive }})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = activeFilter == false,
                                    onClick = { activeFilter = false },
                                    label = { Text("Inactive (${customers.count { !it.isActive }})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Customers List
                if (filteredCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No customer accounts found.", fontSize = 12.sp, color = HighDensitySlate500)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCustomers, key = { it.customerId }) { customer ->
                            AdminCustomerCard(
                                customer = customer,
                                onClick = { onNavigateToCustomerDetails(customer.customerId) },
                                onToggleActive = {
                                    SudhaniRepository.toggleCustomerStatus(customer.customerId, !customer.isActive)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    onToggleActive: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("admin_customer_card_${customer.customerId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(HighDensityIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = HighDensityIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = HighDensitySlate900
                            )
                            if (customer.role.name == "ADMIN") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = RoundedCornerShape(3.dp), color = HighDensityIndigo) {
                                    Text("ADMIN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(
                            text = "ID: ${customer.customerId} • Joined ${AdminDateUtils.formatDate(customer.registeredAt)}",
                            fontSize = 9.sp,
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
                        fontWeight = FontWeight.Bold,
                        color = if (customer.isActive) HighDensityEmerald else HighDensityRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone and Email
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "📞 ${customer.phone}", fontSize = 11.sp, color = HighDensitySlate700)
                Text(text = "✉️ ${customer.email}", fontSize = 11.sp, color = HighDensitySlate700)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HighDensitySlate50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orders: ${customer.totalOrders}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySlate800
                    )
                    Text(
                        text = "Total Spent: ₹${customer.totalSpending.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensityIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleActive,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = if (customer.isActive) "Deactivate Account" else "Activate Account",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.isActive) HighDensityRed else HighDensityEmerald
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View History", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityIndigo)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
