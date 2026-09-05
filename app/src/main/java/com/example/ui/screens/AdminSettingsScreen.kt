package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        var autoAcceptOrders by remember { mutableStateOf(true) }
        var darkStoreLive by remember { mutableStateOf(true) }
        var soundNotifications by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Store Settings",
                    subtitle = "Hub operations & business profile",
                    onNavigateBack = onNavigateBack
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.SETTINGS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> onNavigateToOrders()
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Store Profile
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("STORE DISPATCH HUB", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SudhaniHub Dark Store #01", fontWeight = FontWeight.Black, fontSize = 14.sp, color = HighDensitySlate900)
                            Text("Sector 44, Express Quick Pod • Bengaluru, KA 560034", fontSize = 11.sp, color = HighDensitySlate600)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Standard SLA: 11 Minutes Delivery Guarantee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityEmerald)
                        }
                    }
                }

                // Operational Toggles
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("OPERATIONAL CONTROLS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Dark Store Open / Accepting Orders", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Allows customer app checkout", fontSize = 10.sp, color = HighDensitySlate500)
                                }
                                Switch(checked = darkStoreLive, onCheckedChange = { darkStoreLive = it })
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = HighDensitySlate100)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Auto-Confirm Inbound Orders", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Automatically advances new orders to Confirmed", fontSize = 10.sp, color = HighDensitySlate500)
                                }
                                Switch(checked = autoAcceptOrders, onCheckedChange = { autoAcceptOrders = it })
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = HighDensitySlate100)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order Dispatch Bell & Alerts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Sound notifications on new orders", fontSize = 10.sp, color = HighDensitySlate500)
                                }
                                Switch(checked = soundNotifications, onCheckedChange = { soundNotifications = it })
                            }
                        }
                    }
                }

                // Security & Admin Session
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ADMIN SESSION & SECURITY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = HighDensitySlate500)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current Session: Sudhani Admin (Master)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate900)
                            Text("Active PIN: 9999 • All administrative access logs saved.", fontSize = 10.sp, color = HighDensitySlate600)
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    SudhaniRepository.switchUserRole(UserRole.CUSTOMER)
                                    onNavigateBack()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensitySlate800),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text("Switch to Customer Mode & Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
