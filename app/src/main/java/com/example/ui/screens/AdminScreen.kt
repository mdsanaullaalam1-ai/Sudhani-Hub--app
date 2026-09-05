package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*
import java.util.*

@Composable
fun AdminScreen(
    onNavigateBackToStore: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeliveryAreas: () -> Unit = {},
    onNavigateToReturns: () -> Unit = {}
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBackToStore) {
        val orders by SudhaniRepository.orders.collectAsState()
        val payments by SudhaniRepository.payments.collectAsState()
        val customers by SudhaniRepository.customers.collectAsState()
        val products by SudhaniRepository.products.collectAsState()
        val partners by SudhaniRepository.deliveryPartners.collectAsState()
        val deliveryAreas by SudhaniRepository.deliveryAreas.collectAsState()
        val returnRequests by SudhaniRepository.returnRequests.collectAsState()

        // Calculate metrics
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        val todayOrdersList = orders.filter { it.createdAt >= startOfToday }
        val todayOrdersCount = todayOrdersList.size
        val totalOrdersCount = orders.size

        val todaySalesAmount = todayOrdersList
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .sumOf { it.totalAmount }
        val totalSalesAmount = orders
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .sumOf { it.totalAmount }

        val newCustomersCount = customers.filter { it.registeredAt >= (System.currentTimeMillis() - 86400000L * 7) }.size
        val pendingPaymentsCount = payments.count { it.paymentStatus == PaymentStatus.PENDING }
        val deliveredOrdersCount = orders.count { it.orderStatus == OrderStatus.DELIVERED }
        val cancelledOrdersCount = orders.count { it.orderStatus == OrderStatus.CANCELLED }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Business Control Panel",
                    subtitle = "Real-time Operations & Metrics",
                    onNavigateBack = onNavigateBackToStore,
                    actions = {
                        TextButton(
                            onClick = {
                                SudhaniRepository.switchUserRole(UserRole.CUSTOMER)
                                onNavigateBackToStore()
                            }
                        ) {
                            Text("Exit Admin", color = HighDensityEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.DASHBOARD,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> {}
                            AdminTab.RETURNS -> onNavigateToReturns()
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> onNavigateToPayments()
                            AdminTab.CUSTOMERS -> onNavigateToCustomers()
                            AdminTab.PRODUCTS -> onNavigateToProducts()
                            AdminTab.DELIVERY -> onNavigateToDelivery()
                            AdminTab.COUPONS -> onNavigateToCoupons()
                            AdminTab.SETTINGS -> onNavigateToSettings()
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
                // Quick Navigation Hub
                item {
                    Text(
                        text = "MANAGEMENT MODULES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = HighDensitySlate700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val pendingReturns = returnRequests.count { it.status == com.example.data.model.ReturnStatus.REQUESTED || it.status == com.example.data.model.ReturnStatus.UNDER_REVIEW }
                            AdminQuickNavChip(
                                label = "Returns",
                                count = if (pendingReturns > 0) "$pendingReturns new" else returnRequests.size.toString(),
                                icon = Icons.Default.AssignmentReturn,
                                color = if (pendingReturns > 0) HighDensityAmber else HighDensityIndigo,
                                onClick = onNavigateToReturns
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Orders",
                                count = orders.size.toString(),
                                icon = Icons.Default.ReceiptLong,
                                color = HighDensityIndigo,
                                onClick = onNavigateToOrders
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Payments",
                                count = payments.size.toString(),
                                icon = Icons.Default.Payment,
                                color = HighDensityEmerald,
                                onClick = onNavigateToPayments
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Customers",
                                count = customers.size.toString(),
                                icon = Icons.Default.People,
                                color = HighDensityOrange,
                                onClick = onNavigateToCustomers
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Products",
                                count = products.size.toString(),
                                icon = Icons.Default.Inventory2,
                                color = HighDensitySlate800,
                                onClick = onNavigateToProducts
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Delivery",
                                count = partners.size.toString(),
                                icon = Icons.Default.DeliveryDining,
                                color = Color(0xFF0284C7),
                                onClick = onNavigateToDelivery
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Delivery Areas",
                                count = "${deliveryAreas.count { it.isActive }} Active",
                                icon = Icons.Default.LocationOn,
                                color = HighDensityEmerald,
                                onClick = onNavigateToDeliveryAreas
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Coupons",
                                count = "Offers",
                                icon = Icons.Default.Discount,
                                color = Color(0xFF9333EA),
                                onClick = onNavigateToCoupons
                            )
                        }
                        item {
                            AdminQuickNavChip(
                                label = "Settings",
                                count = "Config",
                                icon = Icons.Default.Settings,
                                color = HighDensitySlate600,
                                onClick = onNavigateToSettings
                            )
                        }
                    }
                }

                // Dashboard Metric Cards (2x4 Grid)
                item {
                    Text(
                        text = "BUSINESS KEY METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = HighDensitySlate700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Row 1: Today's Orders & Total Orders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(
                                title = "Today's Orders",
                                value = todayOrdersCount.toString(),
                                subtitle = "Placed today",
                                icon = Icons.Default.Today,
                                iconColor = HighDensityIndigo,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToOrders
                            )
                            AdminMetricCard(
                                title = "Total Orders",
                                value = totalOrdersCount.toString(),
                                subtitle = "Lifetime orders",
                                icon = Icons.Default.AllInbox,
                                iconColor = HighDensityIndigo,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToOrders
                            )
                        }

                        // Row 2: Today's Sales & Total Sales
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(
                                title = "Today's Sales",
                                value = "₹${todaySalesAmount.toInt()}",
                                subtitle = "Delivered / In-transit",
                                icon = Icons.Default.TrendingUp,
                                iconColor = HighDensityEmerald,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToPayments
                            )
                            AdminMetricCard(
                                title = "Total Sales",
                                value = "₹${totalSalesAmount.toInt()}",
                                subtitle = "Gross store revenue",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconColor = HighDensityEmerald,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToPayments
                            )
                        }

                        // Row 3: New Customers & Pending Payments
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(
                                title = "New Customers",
                                value = newCustomersCount.toString(),
                                subtitle = "Last 7 days",
                                icon = Icons.Default.PersonAdd,
                                iconColor = HighDensityOrange,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToCustomers
                            )
                            AdminMetricCard(
                                title = "Pending Payments",
                                value = pendingPaymentsCount.toString(),
                                subtitle = "COD / Awaiting collect",
                                icon = Icons.Default.HourglassEmpty,
                                iconColor = if (pendingPaymentsCount > 0) HighDensityOrange else HighDensitySlate400,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToPayments
                            )
                        }

                        // Row 4: Delivered Orders & Cancelled Orders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminMetricCard(
                                title = "Delivered Orders",
                                value = deliveredOrdersCount.toString(),
                                subtitle = "Completed handoffs",
                                icon = Icons.Default.CheckCircle,
                                iconColor = HighDensityEmerald,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToOrders
                            )
                            AdminMetricCard(
                                title = "Cancelled Orders",
                                value = cancelledOrdersCount.toString(),
                                subtitle = "Voided requests",
                                icon = Icons.Default.Cancel,
                                iconColor = if (cancelledOrdersCount > 0) HighDensityRed else HighDensitySlate400,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToOrders
                            )
                        }
                    }
                }

                // Product Return Operations Card
                item {
                    val pendingCount = returnRequests.count { it.status == com.example.data.model.ReturnStatus.REQUESTED || it.status == com.example.data.model.ReturnStatus.UNDER_REVIEW }
                    val pickupCount = returnRequests.count { it.status == com.example.data.model.ReturnStatus.APPROVED || it.status == com.example.data.model.ReturnStatus.PICKUP_SCHEDULED }
                    val refundedCount = returnRequests.count { it.status == com.example.data.model.ReturnStatus.REFUNDED }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (pendingCount > 0) HighDensityAmber.copy(alpha = 0.5f) else HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth()
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
                                            .background(HighDensityAmber.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = HighDensityAmber, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Product Return Requests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HighDensitySlate900)
                                        Text("Reverse logistics, inspections & refunds", fontSize = 11.sp, color = HighDensitySlate500)
                                    }
                                }

                                if (pendingCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HighDensityAmber.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$pendingCount Action Required",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HighDensityAmber,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensitySlate50,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$pendingCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = HighDensityAmber)
                                        Text("Pending Review", fontSize = 10.sp, color = HighDensitySlate600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensitySlate50,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$pickupCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = HighDensityIndigo)
                                        Text("Pickup / Hub", fontSize = 10.sp, color = HighDensitySlate600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensitySlate50,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$refundedCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = HighDensityEmerald)
                                        Text("Refunded", fontSize = 10.sp, color = HighDensitySlate600)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onNavigateToReturns,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Return Requests ➜", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Recent Orders Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT ORDERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp,
                            color = HighDensitySlate700
                        )
                        TextButton(onClick = onNavigateToOrders) {
                            Text("View All (${orders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensityIndigo)
                        }
                    }

                    if (orders.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("No orders found yet.", fontSize = 12.sp, color = HighDensitySlate500)
                            }
                        }
                    }
                }

                items(orders.take(5), key = { it.orderId }) { order ->
                    AdminRecentOrderItem(
                        order = order,
                        onClick = { onNavigateToOrderDetails(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminQuickNavChip(
    label: String,
    count: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("admin_nav_${label.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate800)
                Text(text = count, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = HighDensitySlate500)
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    color = HighDensitySlate500
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = HighDensitySlate900
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = HighDensitySlate500
            )
        }
    }
}

@Composable
fun AdminRecentOrderItem(
    order: Order,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("admin_order_${order.orderId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderId}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = HighDensityIndigo
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                            fontWeight = FontWeight.Bold,
                            color = when (order.orderStatus) {
                                OrderStatus.DELIVERED -> HighDensityEmerald
                                OrderStatus.CANCELLED -> HighDensityRed
                                OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0284C7)
                                else -> HighDensityOrange
                            },
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${order.customerName} • ${order.customerPhone}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HighDensitySlate800,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "${order.items.size} items • ${AdminDateUtils.formatDateTime(order.createdAt)}",
                    fontSize = 10.sp,
                    color = HighDensitySlate500
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${order.totalAmount.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = HighDensitySlate900
                )
                Text(
                    text = order.paymentMethod,
                    fontSize = 9.sp,
                    color = HighDensitySlate500
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = HighDensitySlate400,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
