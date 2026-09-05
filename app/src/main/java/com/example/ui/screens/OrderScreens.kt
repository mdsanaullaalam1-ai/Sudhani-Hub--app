package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.ReturnStatus
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onTrackOrder: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Celebratory Green / Gold Badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(SudhaniGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SudhaniGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Order Placed Successfully!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SudhaniGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Order ID: $orderId",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery estimate card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SudhaniGoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SudhaniGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Estimated Delivery in 11 Mins",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF3E2723)
                        )
                        Text(
                            text = "Your items are being packed at the nearest hub",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onTrackOrder,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("track_order_btn")
            ) {
                Icon(Icons.Default.Moped, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Track Order Live", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueShopping,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue Shopping", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onNavigateBack: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onReorder: () -> Unit,
    onRequestReturn: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orders by SudhaniRepository.orders.collectAsState()

    val activeOrders = remember(orders) {
        orders.filter { it.orderStatus != OrderStatus.DELIVERED }
    }
    val previousOrders = remember(orders) {
        orders.filter { it.orderStatus == OrderStatus.DELIVERED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No orders placed yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Active Orders
                if (activeOrders.isNotEmpty()) {
                    item {
                        Text(
                            text = "⚡ Active Orders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniGreenDark
                        )
                    }
                    items(activeOrders) { order ->
                        OrderCard(
                            order = order,
                            isActive = true,
                            onTrackClick = { onTrackOrder(order.id) },
                            onReorderClick = {
                                order.items.forEach { ordItem ->
                                    SudhaniRepository.products.value.find { it.id == ordItem.productId }?.let {
                                        SudhaniRepository.addToCart(it)
                                    }
                                }
                                onReorder()
                            },
                            onRequestReturn = { onRequestReturn(order.id) }
                        )
                    }
                }

                // Previous Orders
                if (previousOrders.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Past Orders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    items(previousOrders) { order ->
                        OrderCard(
                            order = order,
                            isActive = false,
                            onTrackClick = { onTrackOrder(order.id) },
                            onReorderClick = {
                                order.items.forEach { ordItem ->
                                    SudhaniRepository.products.value.find { it.id == ordItem.productId }?.let {
                                        SudhaniRepository.addToCart(it)
                                    }
                                }
                                onReorder()
                            },
                            onRequestReturn = { onRequestReturn(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    isActive: Boolean,
    onTrackClick: () -> Unit,
    onReorderClick: () -> Unit,
    onRequestReturn: (() -> Unit)? = null
) {
    val policyConfig by SudhaniRepository.returnPolicyConfig.collectAsState()
    val returnRequests by SudhaniRepository.returnRequests.collectAsState()
    val existingReturn = remember(returnRequests, order.id) {
        returnRequests.find { it.orderId == order.id }
    }
    val isDelivered = order.orderStatus == OrderStatus.DELIVERED
    val returnWindowOpen = remember(order, policyConfig) {
        order.isReturnWindowOpen(policyConfig.defaultReturnWindowDays)
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.createdAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) SudhaniGreenLight else Color.LightGray.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = order.orderStatus.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) SudhaniGreenDark else Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Items text
            order.items.forEach { item ->
                Text(
                    text = "${item.productImage} ${item.productName} x ${item.quantity}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${order.total.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = SudhaniGreenDark
                    )
                    Text(
                        text = "${order.paymentMethod} (${order.paymentStatus})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        Button(
                            onClick = onTrackClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Track", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        if (isDelivered) {
                            if (existingReturn != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SudhaniGoldLight,
                                    modifier = Modifier.clickable { onTrackClick() }
                                ) {
                                    Text(
                                        text = "Return: ${existingReturn.status.displayName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniGoldDark,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (returnWindowOpen) {
                                OutlinedButton(
                                    onClick = { onRequestReturn?.invoke() ?: onTrackClick() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SudhaniGoldDark)
                                ) {
                                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Return", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onReorderClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onTrackClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Order Details", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onRequestReturn: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orders by SudhaniRepository.orders.collectAsState()
    val returnRequests by SudhaniRepository.returnRequests.collectAsState()
    val policyConfig by SudhaniRepository.returnPolicyConfig.collectAsState()

    val order = remember(orders, orderId) {
        orders.find { it.id == orderId } ?: orders.firstOrNull()
    }
    val orderReturns = remember(returnRequests, order?.id) {
        returnRequests.filter { it.orderId == order?.id }
    }
    val isDelivered = order?.orderStatus == OrderStatus.DELIVERED
    val returnWindowOpen = remember(order, policyConfig) {
        order?.isReturnWindowOpen(policyConfig.defaultReturnWindowDays) ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order #${order?.id ?: ""}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found")
            }
        } else {
            val currentOrder = order
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Status Hero
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricMoped,
                                        contentDescription = null,
                                        tint = SudhaniGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = order.orderStatus.displayName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = order.orderStatus.description,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Arriving in approx 8-10 mins",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Delivery OTP: ${order.deliveryOtp}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SudhaniGold
                                    )
                                }
                            }
                        }
                    }
                }

                // Delivery Partner Details
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = SudhaniGreenDark,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.deliveryPartnerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = order.deliveryPartnerVehicle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = order.deliveryPartnerPhone,
                                    fontSize = 11.sp,
                                    color = SudhaniGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { /* Dial phone */ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Rider",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Step Progression (All 6 statuses: Placed -> Confirmed -> Preparing -> Picked Up -> Out for Delivery -> Delivered)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Live Order Journey",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OrderStatus.values().forEachIndexed { index, status ->
                                val isCompleted = status.stepIndex <= order.orderStatus.stepIndex
                                val isCurrent = status == order.orderStatus

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> SudhaniGold
                                                    isCompleted -> SudhaniGreenPrimary
                                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted && !isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) Color.Black else Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = status.displayName,
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = status.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (index < OrderStatus.values().size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 13.dp)
                                            .width(2.dp)
                                            .height(20.dp)
                                            .background(if (isCompleted) SudhaniGreenPrimary else Color.LightGray.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Return Tracking Section (if any return requested for this order)
                if (orderReturns.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "📦 Return Status & Reverse Logistics",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniGreenDark
                            )

                            orderReturns.forEach { ret ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Return #${ret.returnId}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Requested: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ret.requestedAt))}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (ret.status) {
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
                                                    text = ret.status.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = when (ret.status) {
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
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Product summary in return
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(ret.productImage, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(ret.productName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Text("Reason: ${ret.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("₹${ret.totalRefund.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SudhaniGreenDark)
                                        }

                                        // Reverse pickup banner
                                        if (ret.pickupPartnerName != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = HighDensityIndigo.copy(alpha = 0.08f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = HighDensityIndigo, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Pickup by ${ret.pickupPartnerName} (${ret.pickupPartnerPhone}) • ${ret.pickupTimeSlot ?: "Scheduled"}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = HighDensityIndigo
                                                    )
                                                }
                                            }
                                        }

                                        // Rejection banner
                                        if (ret.status == ReturnStatus.REJECTED && ret.rejectionReason != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = HighDensityRed.copy(alpha = 0.1f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Declined Reason: ${ret.rejectionReason}",
                                                    fontSize = 11.sp,
                                                    color = HighDensityRed,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }

                                        // Refund completed banner
                                        if (ret.status == ReturnStatus.REFUNDED) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = HighDensityEmerald.copy(alpha = 0.1f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HighDensityEmerald, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "₹${ret.totalRefund.toInt()} refunded to ${ret.refundMethod} ${ret.refundTransactionRef?.let { "• Ref: $it" } ?: ""}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityEmerald
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Customer Return Request Box (when Delivered)
                if (isDelivered) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (returnWindowOpen) SudhaniGoldLight else HighDensitySlate100
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (returnWindowOpen) SudhaniGold else HighDensitySlate300
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (returnWindowOpen) Icons.Default.AssignmentReturn else Icons.Default.TimerOff,
                                        contentDescription = null,
                                        tint = if (returnWindowOpen) SudhaniGoldDark else HighDensitySlate600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (returnWindowOpen) "Product Return Policy" else "Return Period Expired",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (returnWindowOpen) SudhaniGoldDark else HighDensitySlate800
                                        )
                                        Text(
                                            text = if (returnWindowOpen) {
                                                val deadline = currentOrder.calculateReturnDeadline(policyConfig.defaultReturnWindowDays) ?: (System.currentTimeMillis() + 3 * 86400000L)
                                                "Window open until ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(deadline))} (${currentOrder.getRemainingReturnTimeDisplay(policyConfig.defaultReturnWindowDays)})"
                                            } else {
                                                "The ${policyConfig.defaultReturnWindowDays}-day return window from delivery has closed"
                                            },
                                            fontSize = 11.sp,
                                            color = if (returnWindowOpen) SudhaniGoldDark.copy(alpha = 0.85f) else HighDensitySlate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (returnWindowOpen) {
                                        "Eligible products can be returned within ${policyConfig.defaultReturnWindowDays} days of delivery if damaged, defective, wrong product, or change of mind."
                                    } else {
                                        "Return requests are no longer accepted for this order. Returns must be initiated within ${policyConfig.defaultReturnWindowDays} days of delivery."
                                    },
                                    fontSize = 12.sp,
                                    color = if (returnWindowOpen) SudhaniGoldDark else HighDensitySlate600
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (returnWindowOpen) {
                                    Button(
                                        onClick = { onRequestReturn(order.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldDark),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("request_return_btn")
                                    ) {
                                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Request Return", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { /* disabled */ },
                                        enabled = false,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("return_period_expired_btn")
                                    ) {
                                        Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Return Period Expired", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Simulator for Testing Status Progression
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniGoldLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🧪 Order Flow Simulator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SudhaniGoldDark
                            )
                            Text(
                                text = "Tap below to test advancing this order to the next stage in real-time.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Button(
                                onClick = { SudhaniRepository.advanceOrderStatus(order.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("advance_order_status_btn")
                            ) {
                                Text(
                                    text = if (order.orderStatus == OrderStatus.DELIVERED) "Order is Completed" else "Simulate Next Status ➜",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
