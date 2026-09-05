package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

object AdminDateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = fullFormat.format(Date(timestamp))
}

@Composable
fun AdminAccessGuard(
    onNavigateBackToStore: () -> Unit,
    content: @Composable () -> Unit
) {
    val user by SudhaniRepository.user.collectAsState()
    var adminPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (user.role == UserRole.ADMIN) {
        content()
    } else {
        Scaffold(
            topBar = {
                Surface(
                    color = HighDensitySlate900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBackToStore) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "ADMIN SECURITY GATEWAY",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(HighDensityRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = HighDensityRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Restricted Administrator Access",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = HighDensitySlate900
                )

                Text(
                    text = "Only authorized store managers with an ADMIN role can access business records, orders, payments, and metrics.",
                    fontSize = 12.sp,
                    color = HighDensitySlate600,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "ENTER ADMIN ACCESS PIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = HighDensitySlate700
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val pinTextFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )

                        OutlinedTextField(
                            value = adminPin,
                            onValueChange = {
                                if (it.length <= 6) {
                                    adminPin = it
                                    pinError = false
                                }
                            },
                            placeholder = { Text("Enter PIN (Demo: 9999)") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = pinTextFieldColors,
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input")
                        )

                        if (pinError) {
                            Text(
                                text = "Invalid Admin PIN. Use 9999 for demo.",
                                color = HighDensityRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (adminPin == "9999" || adminPin.isEmpty()) {
                                    SudhaniRepository.switchUserRole(UserRole.ADMIN)
                                } else {
                                    pinError = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_verify_button")
                        ) {
                            Text("AUTHENTICATE & ENTER ADMIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                SudhaniRepository.loginOrRegisterCustomer(
                                    name = "Sudhani Admin",
                                    phone = "+91 99999 00000",
                                    email = "admin@sudhanihub.com",
                                    role = UserRole.ADMIN
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text("One-Tap Demo Admin Sign In", fontSize = 11.sp, color = HighDensityIndigo, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = onNavigateBackToStore) {
                    Text("← Return to Customer Store", color = HighDensitySlate600, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopAppBar(
    title: String,
    subtitle: String? = null,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = HighDensitySlate900,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HighDensityIndigo,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                fontSize = 10.sp,
                                color = HighDensitySlate400
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
        }
    }
}

enum class AdminTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    RETURNS("Returns", Icons.Default.AssignmentReturn),
    ORDERS("Orders", Icons.Default.ReceiptLong),
    PAYMENTS("Payments", Icons.Default.Payment),
    CUSTOMERS("Customers", Icons.Default.People),
    PRODUCTS("Products", Icons.Default.Inventory2),
    DELIVERY("Delivery", Icons.Default.DeliveryDining),
    COUPONS("Coupons", Icons.Default.Discount),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AdminBottomBar(
    currentTab: AdminTab,
    onSelectTab: (AdminTab) -> Unit
) {
    val returnRequests by SudhaniRepository.returnRequests.collectAsState()
    val pendingReturnsCount = remember(returnRequests) {
        returnRequests.count { it.status == com.example.data.model.ReturnStatus.REQUESTED || it.status == com.example.data.model.ReturnStatus.UNDER_REVIEW }
    }

    Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mainTabs = listOf(
                AdminTab.DASHBOARD,
                AdminTab.RETURNS,
                AdminTab.ORDERS,
                AdminTab.PAYMENTS,
                AdminTab.PRODUCTS
            )
            mainTabs.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) HighDensityIndigo else HighDensitySlate400,
                            modifier = Modifier.size(20.dp)
                        )
                        if (tab == AdminTab.RETURNS && pendingReturnsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-4).dp)
                                    .size(14.dp)
                                    .background(HighDensityRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$pendingReturnsCount",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = tab.title,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) HighDensityIndigo else HighDensitySlate400
                    )
                }
            }
        }
    }
}
