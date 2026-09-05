package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryArea
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDeliveryAreasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val areas by SudhaniRepository.deliveryAreas.collectAsState()

        var searchQuery by remember { mutableStateOf("") }
        var showAddEditDialog by remember { mutableStateOf(false) }
        var editingArea by remember { mutableStateOf<DeliveryArea?>(null) }
        var areaToDelete by remember { mutableStateOf<DeliveryArea?>(null) }
        var selectedSectionTab by remember { mutableIntStateOf(0) }

        val filteredAreas = remember(areas, searchQuery) {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                areas
            } else {
                areas.filter {
                    it.pincode.contains(query, ignoreCase = true) ||
                    it.areaName.contains(query, ignoreCase = true) ||
                    it.city.contains(query, ignoreCase = true) ||
                    it.state.contains(query, ignoreCase = true)
                }
            }
        }

        val activeCount = areas.count { it.isActive }
        val inactiveCount = areas.size - activeCount

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = if (selectedSectionTab == 0) "Smart Pricing Rules" else "Delivery Areas",
                    subtitle = if (selectedSectionTab == 0) "Distance & Weight Slabs • Free Delivery" else "$activeCount active serviceable zones in Firestore",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        if (selectedSectionTab == 1) {
                            Button(
                                onClick = {
                                    editingArea = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("admin_add_area_button")
                            ) {
                                Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
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
                            AdminTab.DELIVERY -> onNavigateToDelivery()
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
            ) {
                TabRow(
                    selectedTabIndex = selectedSectionTab,
                    containerColor = Color.White,
                    contentColor = HighDensityIndigo,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSectionTab]),
                            color = HighDensityIndigo
                        )
                    }
                ) {
                    Tab(
                        selected = selectedSectionTab == 0,
                        onClick = { selectedSectionTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Slabs & Rules", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedSectionTab == 1,
                        onClick = { selectedSectionTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PIN Zones (${areas.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                if (selectedSectionTab == 0) {
                    SmartDeliveryPricingConfigContent()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Metric Cards
                        item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensitySlate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("TOTAL ZONES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = HighDensitySlate400)
                                Text("${areas.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HighDensitySlate900)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensitySlate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ACTIVE ZONES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = HighDensityEmerald)
                                Text("$activeCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HighDensityEmeraldDark)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensitySlate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("INACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = HighDensityRed)
                                Text("$inactiveCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = HighDensityRed)
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_area_search_input"),
                        placeholder = { Text("Search PIN code, Area name, City...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = HighDensitySlate400)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = HighDensitySlate400)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = HighDensityIndigo,
                            unfocusedBorderColor = HighDensitySlate200
                        )
                    )
                }

                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONFIGURED DELIVERY AREAS (${filteredAreas.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = HighDensitySlate700
                        )
                        Text(
                            text = "Firestore: deliveryAreas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityIndigo
                        )
                    }
                }

                if (filteredAreas.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, HighDensitySlate200)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = HighDensitySlate400,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isEmpty()) "No delivery areas found" else "No matching delivery areas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = HighDensitySlate800
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Click '+ Add PIN' to configure your first delivery zone in Firestore.",
                                    fontSize = 12.sp,
                                    color = HighDensitySlate600
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAreas, key = { it.id.ifBlank { it.pincode } }) { area ->
                        DeliveryAreaCard(
                            area = area,
                            onToggleStatus = {
                                SudhaniRepository.toggleDeliveryAreaStatus(area.id)
                            },
                            onEdit = {
                                editingArea = area
                                showAddEditDialog = true
                            },
                            onDelete = {
                                areaToDelete = area
                            }
                        )
                    }
                }
            }
        }
    }
}

        // Add/Edit Dialog
        if (showAddEditDialog) {
            DeliveryAreaAddEditDialog(
                area = editingArea,
                onDismiss = {
                    showAddEditDialog = false
                    editingArea = null
                },
                onSave = { savedArea ->
                    if (editingArea == null) {
                        SudhaniRepository.addDeliveryArea(savedArea)
                    } else {
                        SudhaniRepository.updateDeliveryArea(savedArea)
                    }
                    showAddEditDialog = false
                    editingArea = null
                }
            )
        }

        // Delete Confirmation Dialog
        areaToDelete?.let { area ->
            AlertDialog(
                onDismissRequest = { areaToDelete = null },
                title = { Text("Delete Delivery Area?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to delete delivery area '${area.areaName}' (PIN: ${area.pincode})? Customers in this PIN code will no longer be able to place orders.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            SudhaniRepository.deleteDeliveryArea(area.id)
                            areaToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityRed)
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { areaToDelete = null }) {
                        Text("Cancel", color = HighDensitySlate600)
                    }
                }
            )
        }
    }
}

@Composable
fun DeliveryAreaCard(
    area: DeliveryArea,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("area_card_${area.pincode}"),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (area.isActive) HighDensitySlate200 else HighDensityRed.copy(alpha = 0.3f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (area.isActive) HighDensityIndigoLight else HighDensitySlate100
                    ) {
                        Text(
                            text = area.pincode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (area.isActive) HighDensityIndigo else HighDensitySlate600,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = area.areaName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HighDensitySlate900
                        )
                        Text(
                            text = "${area.city}, ${area.state}",
                            fontSize = 11.sp,
                            color = HighDensitySlate600
                        )
                    }
                }

                // Active / Inactive Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (area.isActive) HighDensityEmeraldLight else HighDensityRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (area.isActive) "ACTIVE" else "INACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (area.isActive) HighDensityEmeraldDark else HighDensityRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = HighDensitySlate100)
            Spacer(modifier = Modifier.height(10.dp))

            // Configuration Attributes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PRICING MODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate400)
                    Text(
                        text = if (area.manualOverrideFee != null) "Fixed ₹${area.manualOverrideFee?.toInt()}" else "⚡ Smart Slabs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (area.manualOverrideFee != null) HighDensityIndigo else HighDensityEmeraldDark
                    )
                }
                Column {
                    Text("DEFAULT DIST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate400)
                    Text(
                        text = "${area.defaultDistanceKm} KM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensitySlate900
                    )
                }
                Column {
                    Text("MIN ORDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate400)
                    Text(
                        text = "₹${area.minimumOrderAmount.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensitySlate900
                    )
                }
                Column {
                    Text("ESTIMATED TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HighDensitySlate400)
                    Text(
                        text = "⚡ ${area.estimatedDeliveryTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = HighDensityIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Button
                OutlinedButton(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("toggle_area_${area.pincode}")
                ) {
                    Text(
                        text = if (area.isActive) "Disable" else "Enable",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (area.isActive) HighDensityRed else HighDensityEmeraldDark
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Edit Button
                FilledTonalButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("edit_area_${area.pincode}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("delete_area_${area.pincode}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = HighDensityRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryAreaAddEditDialog(
    area: DeliveryArea?,
    onDismiss: () -> Unit,
    onSave: (DeliveryArea) -> Unit
) {
    var pincode by remember { mutableStateOf(area?.pincode ?: "") }
    var areaName by remember { mutableStateOf(area?.areaName ?: "") }
    var city by remember { mutableStateOf(area?.city ?: "Gurugram") }
    var state by remember { mutableStateOf(area?.state ?: "Haryana") }
    var deliveryFeeText by remember { mutableStateOf((area?.deliveryFee?.toInt() ?: 0).toString()) }
    var minimumOrderText by remember { mutableStateOf((area?.minimumOrderAmount?.toInt() ?: 199).toString()) }
    var defaultDistanceText by remember { mutableStateOf(area?.defaultDistanceKm?.toString() ?: "3.0") }
    var manualOverrideFeeText by remember { mutableStateOf(area?.manualOverrideFee?.toInt()?.toString() ?: "") }
    var freeDeliveryMinOrderText by remember { mutableStateOf((area?.freeDeliveryMinOrderAmount?.toInt() ?: 999).toString()) }
    var estimatedTime by remember { mutableStateOf(area?.estimatedDeliveryTime ?: "10–20 mins") }
    var isActive by remember { mutableStateOf(area?.isActive ?: true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (area == null) "Add New Delivery Area" else "Edit Delivery Area",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pincode = it
                            }
                        },
                        label = { Text("PIN Code *") },
                        placeholder = { Text("e.g. 122001") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area_pincode_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = areaName,
                        onValueChange = { areaName = it },
                        label = { Text("Area Name *") },
                        placeholder = { Text("e.g. Gurugram Central") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area_name_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City *") },
                            placeholder = { Text("e.g. Gurugram") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_city_input")
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State *") },
                            placeholder = { Text("e.g. Haryana") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_state_input")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = deliveryFeeText,
                            onValueChange = { deliveryFeeText = it.filter { c -> c.isDigit() } },
                            label = { Text("Delivery Fee (₹) *") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_fee_input")
                        )
                        OutlinedTextField(
                            value = minimumOrderText,
                            onValueChange = { minimumOrderText = it.filter { c -> c.isDigit() } },
                            label = { Text("Min Order (₹) *") },
                            placeholder = { Text("199") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_min_order_input")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = defaultDistanceText,
                            onValueChange = { defaultDistanceText = it },
                            label = { Text("Default Dist (KM) *") },
                            placeholder = { Text("3.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_default_dist_input")
                        )
                        OutlinedTextField(
                            value = manualOverrideFeeText,
                            onValueChange = { manualOverrideFeeText = it.filter { c -> c.isDigit() } },
                            label = { Text("Fee Override (₹)") },
                            placeholder = { Text("Smart Slabs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("area_fee_override_input")
                        )
                    }
                    Text(
                        text = "Leave 'Fee Override' empty to let smart distance & weight formula calculate fees automatically.",
                        fontSize = 11.sp,
                        color = HighDensitySlate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = freeDeliveryMinOrderText,
                        onValueChange = { freeDeliveryMinOrderText = it.filter { c -> c.isDigit() } },
                        label = { Text("Free Delivery Min Order (₹)") },
                        placeholder = { Text("999") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area_free_delivery_min_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = estimatedTime,
                        onValueChange = { estimatedTime = it },
                        label = { Text("Estimated Delivery Time *") },
                        placeholder = { Text("e.g. 10–20 mins") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("area_eta_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Area Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (isActive) "Active (Accepts orders)" else "Inactive (Orders blocked)",
                                fontSize = 11.sp,
                                color = if (isActive) HighDensityEmeraldDark else HighDensityRed
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("area_status_switch")
                        )
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = HighDensityRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanPin = pincode.trim()
                    val cleanArea = areaName.trim()
                    val cleanCity = city.trim()
                    val cleanState = state.trim()

                    if (cleanPin.length != 6) {
                        errorMessage = "Please enter a valid 6-digit PIN code"
                        return@Button
                    }
                    if (cleanArea.isBlank()) {
                        errorMessage = "Please enter the area name"
                        return@Button
                    }
                    if (cleanCity.isBlank() || cleanState.isBlank()) {
                        errorMessage = "Please enter both City and State"
                        return@Button
                    }

                    val fee = deliveryFeeText.toDoubleOrNull() ?: 0.0
                    val minOrder = minimumOrderText.toDoubleOrNull() ?: 199.0
                    val eta = if (estimatedTime.isBlank()) "10–20 mins" else estimatedTime.trim()
                    val defaultDist = defaultDistanceText.toDoubleOrNull() ?: 3.0
                    val manualOverride = manualOverrideFeeText.toDoubleOrNull()
                    val freeDelMin = freeDeliveryMinOrderText.toDoubleOrNull() ?: 999.0

                    val targetId = area?.id?.ifBlank { cleanPin } ?: cleanPin

                    val saved = DeliveryArea(
                        id = targetId,
                        pincode = cleanPin,
                        areaName = cleanArea,
                        city = cleanCity,
                        state = cleanState,
                        deliveryFee = fee,
                        minimumOrderAmount = minOrder,
                        estimatedDeliveryTime = eta,
                        isActive = isActive,
                        defaultDistanceKm = defaultDist,
                        manualOverrideFee = manualOverride,
                        freeDeliveryMinOrderAmount = freeDelMin,
                        createdAt = area?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(saved)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                modifier = Modifier.testTag("save_delivery_area_button")
            ) {
                Text("Save Area", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HighDensitySlate600)
            }
        }
    )
}
