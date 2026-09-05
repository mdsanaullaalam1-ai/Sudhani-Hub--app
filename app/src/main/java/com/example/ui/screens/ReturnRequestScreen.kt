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
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.ReturnReasons
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnRequestScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onReturnSubmitted: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by SudhaniRepository.orders.collectAsState()
    val returnRequests by SudhaniRepository.returnRequests.collectAsState()
    val products by SudhaniRepository.products.collectAsState()
    val policyConfig by SudhaniRepository.returnPolicyConfig.collectAsState()

    val order = remember(orders, orderId) { orders.find { it.id == orderId } }

    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var returnQuantity by remember { mutableStateOf(1) }
    var selectedReason by remember { mutableStateOf("") }
    var otherReasonText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var uploadedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var reasonFilterQuery by remember { mutableStateOf("") }
    var activeReasonCategory by remember { mutableStateOf("All") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedReturnId by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Auto-select first returnable item if none selected
    LaunchedEffect(order) {
        if (order != null && selectedProductId == null) {
            val firstEligible = order.items.firstOrNull { item ->
                val prod = products.find { it.id == item.productId }
                val isNonReturnable = prod?.isReturnable == false
                val alreadyReturned = returnRequests.any { it.orderId == order.orderId && it.productId == item.productId }
                !isNonReturnable && !alreadyReturned
            }
            if (firstEligible != null) {
                selectedProductId = firstEligible.productId
                returnQuantity = 1
            }
        }
    }

    val selectedItem = order?.items?.find { it.productId == selectedProductId }
    val selectedProduct = products.find { it.id == selectedProductId }
    val isPhotoRequired = remember(selectedReason) {
        if (selectedReason.isBlank()) false else ReturnReasons.isPhotoRequired(selectedReason)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Request Product Return", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Order #${order?.orderId ?: orderId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Order not found.", fontWeight = FontWeight.Medium)
            }
            return@Scaffold
        }

        // Check if return window is expired or order cancelled
        val isWindowOpen = order.isReturnWindowOpen(policyConfig.defaultReturnWindowDays)
        val isDelivered = order.orderStatus == OrderStatus.DELIVERED

        if (!policyConfig.isReturnEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = HighDensityRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Returns Disabled", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Returns are temporarily disabled by the store administration.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Back to Orders")
                        }
                    }
                }
            }
            return@Scaffold
        }

        if (!isDelivered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = HighDensityAmber, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Order Not Delivered Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Returns can only be requested once the delivery is marked completed.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Back to Orders")
                        }
                    }
                }
            }
            return@Scaffold
        }

        if (!isWindowOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = HighDensityRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Return Period Expired", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HighDensityRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "The ${policyConfig.defaultReturnWindowDays}-day return window for this order has ended.\nOrders can only be returned within ${policyConfig.defaultReturnWindowDays} days of confirmed delivery.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Back to Orders")
                        }
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Policy Notice Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SudhaniGreenLight.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniGreenPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SudhaniGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${policyConfig.defaultReturnWindowDays}-Day Hassle-Free Returns",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SudhaniGreenDark
                            )
                            Text(
                                text = order.getRemainingReturnTimeDisplay(policyConfig.defaultReturnWindowDays),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SudhaniGreenPrimary
                            )
                        }
                    }
                }
            }

            // Step 1: Select Product to Return
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Select Item to Return", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        order.items.forEach { item ->
                            val product = products.find { it.id == item.productId }
                            val isNonReturnable = product?.isReturnable == false
                            val existingReturn = returnRequests.find { it.orderId == order.orderId && it.productId == item.productId }
                            val isSelected = selectedProductId == item.productId
                            val isEnabled = !isNonReturnable && existingReturn == null

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = isEnabled) {
                                        selectedProductId = item.productId
                                        returnQuantity = 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (isEnabled) {
                                                selectedProductId = item.productId
                                                returnQuantity = 1
                                            }
                                        },
                                        enabled = isEnabled
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.productImage, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Qty: ${item.quantity} • ₹${item.price.toInt()} each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        if (isNonReturnable) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = HighDensityRed.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "🚫 Non-Returnable Item",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (existingReturn != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = HighDensityAmber.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Return: ${existingReturn.status.displayName}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityAmber,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Quantity Selector if selected item has > 1 quantity
                        if (selectedItem != null && selectedItem.quantity > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Quantity to return:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = { if (returnQuantity > 1) returnQuantity-- },
                                        enabled = returnQuantity > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = "$returnQuantity of ${selectedItem.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    FilledIconButton(
                                        onClick = { if (returnQuantity < selectedItem.quantity) returnQuantity++ },
                                        enabled = returnQuantity < selectedItem.quantity,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Select Return Reason
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Reason for Return", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Pills
                        val categories = listOf("All", "Quality / Damage", "Wrong Item", "Preference / Mind Changed")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = activeReasonCategory == cat,
                                    onClick = { activeReasonCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Fast search for reason
                        OutlinedTextField(
                            value = reasonFilterQuery,
                            onValueChange = { reasonFilterQuery = it },
                            placeholder = { Text("Search among 34 return reasons...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (reasonFilterQuery.isNotEmpty()) {
                                    IconButton(onClick = { reasonFilterQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reason List
                        val allFilteredReasons = remember(selectedProduct, reasonFilterQuery, activeReasonCategory, policyConfig) {
                            val categoryId = selectedProduct?.categoryId ?: "grocery"
                            val baseList = ReturnReasons.getReasonsForCategory(categoryId, policyConfig.disabledReasons)
                            baseList.filter { reason ->
                                val matchesQuery = reasonFilterQuery.isBlank() || reason.contains(reasonFilterQuery, ignoreCase = true)
                                val matchesCategory = when (activeReasonCategory) {
                                    "Quality / Damage" -> reason.contains("damage", true) || reason.contains("broken", true) || reason.contains("defect", true) || reason.contains("spoil", true) || reason.contains("leak", true) || reason.contains("expir", true) || reason.contains("quality", true)
                                    "Wrong Item" -> reason.contains("wrong", true) || reason.contains("different", true) || reason.contains("missing", true) || reason.contains("duplicate", true) || reason.contains("quantity", true)
                                    "Preference / Mind Changed" -> reason.contains("mind", true) || reason.contains("mistake", true) || reason.contains("not needed", true) || reason.contains("delayed", true) || reason.contains("price", true)
                                    else -> true
                                }
                                matchesQuery && matchesCategory
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                        ) {
                            allFilteredReasons.take(15).forEach { reason ->
                                val isSelected = selectedReason == reason
                                val reqPhoto = ReturnReasons.isPhotoRequired(reason)

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedReason = reason
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = reason,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (reqPhoto) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = HighDensityAmber.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Photo Req.",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityAmber,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // If "Other" is selected, show custom text field
                        AnimatedVisibility(visible = selectedReason == "Other") {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                OutlinedTextField(
                                    value = otherReasonText,
                                    onValueChange = { otherReasonText = it },
                                    label = { Text("Please specify your reason *", fontSize = 12.sp) },
                                    placeholder = { Text("Enter detailed reason for returning", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Step 3: Description & Remarks
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Additional Details (Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = { descriptionText = it },
                            placeholder = { Text("Add any extra details (e.g. damaged seal, leaking carton, missing item count)...", fontSize = 12.sp) },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Step 4: Photo Uploads
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
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
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("4", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Upload Product Photos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            if (isPhotoRequired) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = HighDensityRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Photo Required *",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SudhaniGreenLight
                                ) {
                                    Text(
                                        text = "Optional",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniGreenDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPhotoRequired) "Please attach clear photos of the damaged/defective product and packaging to speed up approval." else "Attach photos if you would like to help our team verify the return.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Upload action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val newPhoto = "camera_defect_capture_${uploadedPhotos.size + 1}.jpg"
                                    uploadedPhotos = uploadedPhotos + newPhoto
                                    Toast.makeText(context, "Captured photo added", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val newPhoto = "gallery_product_issue_${uploadedPhotos.size + 1}.jpg"
                                    uploadedPhotos = uploadedPhotos + newPhoto
                                    Toast.makeText(context, "Photo uploaded from gallery", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", fontSize = 12.sp)
                            }
                        }

                        // Photo Thumbnails List
                        if (uploadedPhotos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uploadedPhotos) { photoName ->
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HighDensitySlate100)
                                            .border(1.dp, HighDensitySlate300, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = HighDensitySlate500, modifier = Modifier.size(24.dp))
                                            Text(photoName.take(8) + "..", fontSize = 9.sp, color = HighDensitySlate600)
                                        }
                                        IconButton(
                                            onClick = { uploadedPhotos = uploadedPhotos.filter { it != photoName } },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(22.dp)
                                                .background(HighDensityRed.copy(alpha = 0.8f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 5: Refund Calculation Preview
            item {
                val itemPrice = selectedItem?.price ?: 0.0
                val totalProductRefund = itemPrice * returnQuantity
                val isDeliveryRefunded = policyConfig.refundDeliveryFeeByDefault
                val deliveryRefundAmount = if (isDeliveryRefunded) order.deliveryFee else 0.0
                val totalRefund = totalProductRefund + deliveryRefundAmount

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("5", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Refund Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Product Refund (${selectedItem?.productName?.take(20) ?: "Item"} x $returnQuantity)", fontSize = 13.sp)
                            Text("₹${totalProductRefund.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Delivery Charge Refund", fontSize = 13.sp)
                                Text("Delivery fee is non-refundable by default", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(if (deliveryRefundAmount > 0) "₹${deliveryRefundAmount.toInt()}" else "₹0", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Estimated Refund", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "₹${totalRefund.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = SudhaniGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SudhaniGreenPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Refund will be processed to: ${order.paymentMethod} or SudhaniHub Wallet after inspection.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                val effectiveReason = if (selectedReason == "Other") otherReasonText.trim() else selectedReason
                val canSubmit = selectedProductId != null &&
                        effectiveReason.isNotBlank() &&
                        (!isPhotoRequired || uploadedPhotos.isNotEmpty()) &&
                        !isSubmitting

                Button(
                    onClick = {
                        if (selectedProductId == null) {
                            Toast.makeText(context, "Please select an item to return", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (effectiveReason.isBlank()) {
                            Toast.makeText(context, "Please select or enter a return reason", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isPhotoRequired && uploadedPhotos.isEmpty()) {
                            Toast.makeText(context, "Please attach at least one photo for this reason", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSubmitting = true
                        val (success, message) = SudhaniRepository.submitReturnRequest(
                            orderId = order.orderId,
                            productId = selectedProductId!!,
                            returnQuantity = returnQuantity,
                            reason = effectiveReason,
                            customerDescription = descriptionText,
                            photos = uploadedPhotos
                        )

                        isSubmitting = false
                        if (success) {
                            val ret = SudhaniRepository.returnRequests.value.firstOrNull { it.orderId == order.orderId && it.productId == selectedProductId }
                            generatedReturnId = ret?.returnId ?: "RET-${order.orderId}"
                            showSuccessDialog = true
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_return_request_btn")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Return Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Success Confirmation Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onReturnSubmitted(generatedReturnId)
                onNavigateBack()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SudhaniGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SudhaniGreenPrimary, modifier = Modifier.size(34.dp))
                }
            },
            title = {
                Text("Return Request Submitted", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your return request has been submitted successfully.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Return ID: $generatedReturnId",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = SudhaniGreenDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Our store team is reviewing your request. Reverse pickup will be arranged once approved.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onReturnSubmitted(generatedReturnId)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SudhaniGreenPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
