package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Coupon
import com.example.data.model.DiscountType
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminCouponsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val coupons by SudhaniRepository.availableCoupons.collectAsState()
        var showAddCouponDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Coupon Discounts",
                    subtitle = "${coupons.count { it.isActive }} active promotional offers",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        Button(
                            onClick = { showAddCouponDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New Coupon", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.COUPONS,
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(coupons, key = { it.code }) { coupon ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                        modifier = Modifier.fillMaxWidth().testTag("admin_coupon_${coupon.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF9333EA).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = coupon.code,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color(0xFF9333EA),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (coupon.discountType == DiscountType.FLAT) "₹${coupon.discountValue.toInt()} Flat OFF" else "${coupon.discountValue.toInt()}% OFF",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = HighDensitySlate900
                                    )
                                }
                                Text(
                                    text = coupon.description,
                                    fontSize = 10.sp,
                                    color = HighDensitySlate600,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Min order: ₹${coupon.minimumOrder.toInt()}" + if (coupon.maximumDiscount != null) " • Max disc: ₹${coupon.maximumDiscount.toInt()}" else "",
                                    fontSize = 9.sp,
                                    color = HighDensitySlate500
                                )
                            }

                            Switch(
                                checked = coupon.isActive,
                                onCheckedChange = { isActive ->
                                    SudhaniRepository.toggleCouponActive(coupon.code, isActive)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddCouponDialog) {
            var code by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var discountAmountText by remember { mutableStateOf("50") }
            var minOrderText by remember { mutableStateOf("199") }

            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black
            )

            AlertDialog(
                onDismissRequest = { showAddCouponDialog = false },
                title = { Text("Create Discount Coupon", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("Coupon Code (e.g. SAVE50)") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = discountAmountText,
                            onValueChange = { discountAmountText = it },
                            label = { Text("Flat Discount Amount (₹)") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = minOrderText,
                            onValueChange = { minOrderText = it },
                            label = { Text("Minimum Order Amount (₹)") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (code.isNotEmpty()) {
                                val c = Coupon(
                                    code = code,
                                    discountType = DiscountType.FLAT,
                                    discountValue = discountAmountText.toDoubleOrNull() ?: 50.0,
                                    minimumOrder = minOrderText.toDoubleOrNull() ?: 199.0,
                                    maximumDiscount = null,
                                    isActive = true,
                                    description = description.ifEmpty { "Flat discount on orders above ₹$minOrderText" }
                                )
                                SudhaniRepository.addOrUpdateCoupon(c)
                                showAddCouponDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
                    ) {
                        Text("Create Coupon")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCouponDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
