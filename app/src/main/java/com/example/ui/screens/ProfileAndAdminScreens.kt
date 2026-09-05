package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.OrderStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    onDismiss: () -> Unit
) {
    val orders by SudhaniRepository.orders.collectAsState()
    val products by SudhaniRepository.products.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Inventory, 2: Orders

    val totalSales = orders.sumOf { it.total }
    val pendingOrders = orders.count { it.orderStatus != OrderStatus.DELIVERED }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SudhaniGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SudhaniHub Admin Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Inventory") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Live Orders") })
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Overview Metrics
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricCard(title = "Total Orders", value = "${orders.size}", modifier = Modifier.weight(1f))
                                    MetricCard(title = "Total Revenue", value = "₹${totalSales.toInt()}", modifier = Modifier.weight(1f))
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricCard(title = "Active / Out", value = "$pendingOrders", modifier = Modifier.weight(1f))
                                    MetricCard(title = "Active SKUs", value = "${products.size}", modifier = Modifier.weight(1f))
                                }
                            }
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SudhaniGreenLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("⚡ Dark Store Performance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniGreenDark)
                                        Text("Average dispatch time: 2.8 mins", fontSize = 12.sp)
                                        Text("Average delivery time: 10.4 mins", fontSize = 12.sp)
                                        Text("Order fulfillment rate: 99.4%", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Inventory Management
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(products) { prod ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(prod.emoji, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Stock: ${prod.stock} | ₹${prod.price.toInt()}", fontSize = 11.sp)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    SudhaniRepository.updateProductStock(prod.id, (prod.stock - 5).coerceAtLeast(0))
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            Text("${prod.stock}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                            IconButton(
                                                onClick = {
                                                    SudhaniRepository.updateProductStock(prod.id, prod.stock + 10)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Live Orders Management
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(orders) { ord ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("#${ord.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("₹${ord.total.toInt()}", fontWeight = FontWeight.Bold, color = SudhaniGreenDark)
                                        }
                                        Text("Status: ${ord.orderStatus.displayName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Rider: ${ord.deliveryPartnerName}", fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { SudhaniRepository.advanceOrderStatus(ord.id) },
                                            modifier = Modifier.fillMaxWidth().height(34.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary)
                                        ) {
                                            Text("Advance Order Status ➜", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary)
            ) {
                Text("Close Dashboard")
            }
        }
    )
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = SudhaniGreenDark)
        }
    }
}
