package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.DeliveryPartner
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminDeliveryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDeliveryAreas: () -> Unit = {}
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val partners by SudhaniRepository.deliveryPartners.collectAsState()
        val orders by SudhaniRepository.orders.collectAsState()
        val activeOrders = orders.filter { it.orderStatus != OrderStatus.DELIVERED && it.orderStatus != OrderStatus.CANCELLED }

        var showAddPartnerDialog by remember { mutableStateOf(false) }
        var orderToAssignPartner by remember { mutableStateOf<Order?>(null) }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Delivery Fleet",
                    subtitle = "${partners.count { it.isAvailable }} of ${partners.size} riders available",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        OutlinedButton(
                            onClick = onNavigateToDeliveryAreas,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Zones", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showAddPartnerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.DELIVERY,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> onNavigateToPayments()
                            AdminTab.CUSTOMERS -> onNavigateToCustomers()
                            AdminTab.PRODUCTS -> onNavigateToProducts()
                            AdminTab.DELIVERY -> {}
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
                // Section 1: Active Orders Requiring Dispatch
                item {
                    Text(
                        text = "ACTIVE DISPATCH ORDERS (${activeOrders.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = HighDensitySlate700
                    )
                }

                if (activeOrders.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                Text("All active orders dispatched or delivered!", fontSize = 12.sp, color = HighDensitySlate500)
                            }
                        }
                    }
                } else {
                    items(activeOrders, key = { it.orderId }) { order ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "#${order.orderId}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = HighDensityIndigo)
                                        Text(text = "${order.customerName} • ${order.deliveryAddress}", fontSize = 10.sp, color = HighDensitySlate600, maxLines = 1)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = order.orderStatus.displayName.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0284C7),
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
                                    Text(
                                        text = if (order.deliveryPartnerName != null) "Rider: ${order.deliveryPartnerName}" else "⚠️ No Rider Assigned",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.deliveryPartnerName != null) HighDensitySlate900 else HighDensityRed
                                    )
                                    Button(
                                        onClick = { orderToAssignPartner = order },
                                        colors = ButtonDefaults.buttonColors(containerColor = HighDensitySlate800),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(if (order.deliveryPartnerName != null) "Reassign" else "Assign Rider", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Fleet Roster
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DELIVERY PARTNER ROSTER (${partners.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = HighDensitySlate700
                    )
                }

                items(partners, key = { it.partnerId }) { partner ->
                    AdminPartnerCard(partner = partner)
                }
            }
        }

        // Add Delivery Partner Dialog
        if (showAddPartnerDialog) {
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var vehicle by remember { mutableStateOf("Electric Scooter") }

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black
            )

            AlertDialog(
                onDismissRequest = { showAddPartnerDialog = false },
                title = { Text("Register Delivery Partner", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Rider Full Name") },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            prefix = { Text("+91 ") },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = vehicle,
                            onValueChange = { vehicle = it },
                            label = { Text("Vehicle Details (e.g. EV Scooter DL-3S)") },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                val p = DeliveryPartner(
                                    partnerId = "DP_${(100..999).random()}",
                                    name = name,
                                    phone = if (phone.startsWith("+91")) phone else "+91 $phone",
                                    vehicle = vehicle.ifEmpty { "Electric Scooter" },
                                    isAvailable = true,
                                    currentOrderId = null,
                                    activeDeliveriesCount = 0,
                                    rating = 5.0
                                )
                                SudhaniRepository.addDeliveryPartner(p)
                                showAddPartnerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo)
                    ) {
                        Text("Add Rider")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPartnerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Assign Partner Dialog for an active order
        if (orderToAssignPartner != null) {
            val ord = orderToAssignPartner!!
            AlertDialog(
                onDismissRequest = { orderToAssignPartner = null },
                title = { Text("Assign Rider to #${ord.orderId}", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        partners.forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ord.deliveryPartnerId == p.partnerId) HighDensityIndigo.copy(alpha = 0.12f) else HighDensitySlate50,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SudhaniRepository.assignDeliveryPartner(ord.orderId, p.partnerId)
                                        orderToAssignPartner = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensitySlate900)
                                        Text("${p.vehicle} • ${p.phone}", fontSize = 10.sp, color = HighDensitySlate600)
                                        Text(if (p.isAvailable) "🟢 Ready" else "🟡 Busy (${p.activeDeliveriesCount} active)", fontSize = 9.sp)
                                    }
                                    if (ord.deliveryPartnerId == p.partnerId) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { orderToAssignPartner = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminPartnerCard(partner: DeliveryPartner) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_partner_${partner.partnerId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = partner.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensitySlate900)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "⭐ ${partner.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HighDensityOrange)
                    }
                    Text(text = "📞 ${partner.phone}", fontSize = 10.sp, color = HighDensitySlate600)
                    Text(text = "🛵 ${partner.vehicle}", fontSize = 10.sp, color = HighDensitySlate500)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (partner.isAvailable) HighDensityEmerald.copy(alpha = 0.15f) else HighDensityOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (partner.isAvailable) "AVAILABLE" else "ON ROUTE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (partner.isAvailable) HighDensityEmerald else HighDensityOrange,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (partner.currentOrderId != null) {
                    Text(text = "Order #${partner.currentOrderId}", fontSize = 9.sp, color = HighDensityIndigo, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
