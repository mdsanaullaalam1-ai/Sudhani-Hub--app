package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SavedPaymentMethod
import com.example.data.repository.SudhaniRepository
import com.example.data.service.RazorpayService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedMethods by SudhaniRepository.savedPaymentMethods.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showMethodTypePicker by remember { mutableStateOf(false) }
    var showAddUpiDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardTypeToAdd by remember { mutableStateOf("DEBIT_CARD") } // "DEBIT_CARD" or "CREDIT_CARD"

    var methodToDelete by remember { mutableStateOf<SavedPaymentMethod?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "UPI", "CARDS"

    val filteredMethods = remember(savedMethods, selectedFilter) {
        when (selectedFilter) {
            "UPI" -> savedMethods.filter { it.type == "UPI" }
            "CARDS" -> savedMethods.filter { it.type.contains("CARD") }
            else -> savedMethods
        }
    }

    Scaffold(
        containerColor = SudhaniTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saved Payment Methods",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textPrimary
                        )
                        Text(
                            text = "Manage your payment methods for faster checkout",
                            fontSize = 11.sp,
                            color = SudhaniTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SudhaniTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SudhaniNavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_sudhanihub_logo),
                            contentDescription = "Sudhanihub",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SudhaniTheme.colors.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TOP BUTTON: "+ Add New Payment Method"
            item {
                Button(
                    onClick = { showMethodTypePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SudhaniGoldPrimary,
                        contentColor = SudhaniNavyDark
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_payment_method_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = SudhaniNavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Add New Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = SudhaniNavyDark
                    )
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${savedMethods.size})", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SudhaniNavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "UPI",
                        onClick = { selectedFilter = "UPI" },
                        label = {
                            Text(
                                "UPI (${savedMethods.count { it.type == "UPI" }})",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SudhaniNavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "CARDS",
                        onClick = { selectedFilter = "CARDS" },
                        label = {
                            Text(
                                "Cards (${savedMethods.count { it.type.contains("CARD") }})",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SudhaniNavyDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // SAVED METHODS LIST
            if (filteredMethods.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                        border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(SudhaniTheme.colors.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = SudhaniGoldDark,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Saved Methods in this category",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ Add New Payment Method' to register a UPI ID or card via Razorpay's secure gateway.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredMethods, key = { it.id }) { method ->
                    SavedMethodCard(
                        method = method,
                        onSetDefault = {
                            SudhaniRepository.setDefaultPaymentMethod(method.id)
                            Toast.makeText(context, "${method.title} set as default", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            methodToDelete = method
                        }
                    )
                }
            }

            // Security & RBI Compliance Guarantee Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
                    border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SudhaniGreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SudhaniGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% RBI Compliant & PCI-DSS Secure",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "Sudhanihub never stores full card numbers, CVVs, or UPI PINs. Saved via Razorpay certified tokenization vault.",
                                fontSize = 10.sp,
                                color = SudhaniTheme.colors.textSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Choose Payment Method Type (UPI, Debit Card, Credit Card)
    if (showMethodTypePicker) {
        ModalBottomSheet(
            onDismissRequest = { showMethodTypePicker = false },
            containerColor = SudhaniTheme.colors.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add New Payment Method",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary
                )
                Text(
                    text = "Choose your preferred payment instrument to add securely:",
                    fontSize = 12.sp,
                    color = SudhaniTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Option 1: UPI
                AddMethodTypeOptionCard(
                    title = "UPI (Unified Payments Interface)",
                    subtitle = "Google Pay, PhonePe, Paytm, BHIM, or any UPI ID",
                    icon = Icons.Default.QrCodeScanner,
                    accentColor = Color(0xFF0F9D58),
                    onClick = {
                        showMethodTypePicker = false
                        showAddUpiDialog = true
                    }
                )

                // Option 2: Debit Card
                AddMethodTypeOptionCard(
                    title = "Debit Card",
                    subtitle = "HDFC, SBI, ICICI, Axis, Kotak - Visa, Mastercard, RuPay",
                    icon = Icons.Default.CreditCard,
                    accentColor = SudhaniGoldDark,
                    onClick = {
                        showMethodTypePicker = false
                        cardTypeToAdd = "DEBIT_CARD"
                        showAddCardDialog = true
                    }
                )

                // Option 3: Credit Card
                AddMethodTypeOptionCard(
                    title = "Credit Card",
                    subtitle = "All Visa, Mastercard, RuPay cards with reward points",
                    icon = Icons.Default.CreditScore,
                    accentColor = Color(0xFF6366F1),
                    onClick = {
                        showMethodTypePicker = false
                        cardTypeToAdd = "CREDIT_CARD"
                        showAddCardDialog = true
                    }
                )
            }
        }
    }

    // ADD UPI DIALOG
    if (showAddUpiDialog) {
        AddUpiDialog(
            onDismiss = { showAddUpiDialog = false },
            onSave = { newMethod ->
                SudhaniRepository.addSavedPaymentMethod(newMethod)
                showAddUpiDialog = false
                Toast.makeText(context, "${newMethod.title} added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ADD CARD DIALOG (Debit / Credit)
    if (showAddCardDialog) {
        AddCardDialog(
            cardType = cardTypeToAdd,
            onDismiss = { showAddCardDialog = false },
            onSave = { newMethod ->
                SudhaniRepository.addSavedPaymentMethod(newMethod)
                showAddCardDialog = false
                Toast.makeText(context, "${newMethod.title} saved securely!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    methodToDelete?.let { method ->
        AlertDialog(
            onDismissRequest = { methodToDelete = null },
            containerColor = SudhaniTheme.colors.surface,
            title = {
                Text(
                    text = "Remove Payment Method?",
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${method.title}' (${method.subtitle})? You can add it back anytime.",
                    fontSize = 13.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        SudhaniRepository.deleteSavedPaymentMethod(method.id)
                        methodToDelete = null
                        Toast.makeText(context, "Payment method removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { methodToDelete = null }) {
                    Text("Cancel", color = SudhaniTheme.colors.textSecondary)
                }
            }
        )
    }
}

/**
 * Clean card representing a saved payment method.
 */
@Composable
private fun SavedMethodCard(
    method: SavedPaymentMethod,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = BorderStroke(
            width = if (method.isDefault) 1.8.dp else 1.dp,
            color = if (method.isDefault) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
        ),
        elevation = CardDefaults.cardElevation(if (method.isDefault) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_card_${method.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Method Icon Container
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                method.type == "UPI" -> Color(0xFF0F9D58).copy(alpha = 0.12f)
                                method.type == "CREDIT_CARD" -> Color(0xFF6366F1).copy(alpha = 0.12f)
                                else -> SudhaniGoldPrimary.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        method.type == "UPI" -> {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "UPI",
                                tint = Color(0xFF0F9D58),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        method.type == "CREDIT_CARD" -> {
                            Icon(
                                imageVector = Icons.Default.CreditScore,
                                contentDescription = "Credit Card",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Debit Card",
                                tint = SudhaniGoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Method Title & Masked Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = method.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (method.isDefault) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SudhaniGoldPrimary,
                                contentColor = SudhaniNavyDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SudhaniNavyDark,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "DEFAULT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Masked details: VPA for UPI, •••• •••• •••• 4921 for cards
                    Text(
                        text = if (method.type == "UPI") {
                            method.upiVpa.ifBlank { method.subtitle }
                        } else {
                            val last4 = method.lastFourDigits.ifBlank { "••••" }
                            "•••• •••• •••• $last4"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SudhaniTheme.colors.textSecondary
                    )

                    // Secondary info: Card expiry or UPI verified label
                    if (method.type.contains("CARD")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            if (method.cardExpiry.isNotBlank()) {
                                Text(
                                    text = "Exp: ${method.cardExpiry}",
                                    fontSize = 10.sp,
                                    color = SudhaniTheme.colors.textSecondary
                                )
                            }
                            if (method.cardNetwork.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SudhaniTheme.colors.chipBackground
                                ) {
                                    Text(
                                        text = method.cardNetwork,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SudhaniTheme.colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Verified UPI Gateway VPA",
                            fontSize = 10.sp,
                            color = Color(0xFF0F9D58),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // "Set as Default" Action Button if not already default
            if (!method.isDefault) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetDefault() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use as default for 1-click checkout",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )
                    Text(
                        text = "Set as Default",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniGoldDark
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet option card for selecting UPI, Debit Card, Credit Card.
 */
@Composable
private fun AddMethodTypeOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SudhaniTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Add UPI ID Modal Dialog.
 * Enforces valid UPI ID format, simulates payment-gateway verification, and saves securely.
 */
@Composable
private fun AddUpiDialog(
    onDismiss: () -> Unit,
    onSave: (SavedPaymentMethod) -> Unit
) {
    var upiInput by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("Google Pay UPI") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var verifiedHolderName by remember { mutableStateOf<String?>(null) }
    var setAsDefault by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F9D58).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF0F9D58),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Add UPI ID",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Text(
                        text = "Verified via Razorpay Gateway",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Provider Selection
                Text(
                    text = "SELECT UPI APP / PROVIDER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Google Pay UPI", "PhonePe UPI", "Other UPI").forEach { provider ->
                        val isSelected = selectedProvider == provider
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SudhaniNavyDark else SudhaniTheme.colors.chipBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedProvider = provider }
                        ) {
                            Text(
                                text = provider.replace(" UPI", ""),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SudhaniTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                // UPI ID Text Field
                OutlinedTextField(
                    value = upiInput,
                    onValueChange = {
                        upiInput = it.trim()
                        verificationError = null
                        verifiedHolderName = null
                    },
                    label = { Text("UPI ID (e.g. mobile@upi or name@oksbi)") },
                    singleLine = true,
                    isError = verificationError != null,
                    supportingText = {
                        if (verificationError != null) {
                            Text(verificationError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        } else if (verifiedHolderName != null) {
                            Text(
                                "✓ Verified: $verifiedHolderName",
                                color = Color(0xFF0F9D58),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        } else {
                            Text("Standard UPI format: username@bank", fontSize = 10.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SudhaniGoldPrimary,
                        unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_id_input")
                )

                // Set as Default Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setAsDefault = !setAsDefault },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = SudhaniGoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Set as default payment method",
                        fontSize = 12.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isVerifying = true
                        verificationError = null
                        val result = RazorpayService.verifyUpiId(upiInput)
                        isVerifying = false

                        if (!result.isValid) {
                            verificationError = result.errorMessage
                        } else {
                            verifiedHolderName = result.accountHolderName
                            val newMethod = SavedPaymentMethod(
                                id = "SPM_UPI_${UUID.randomUUID().toString().take(6)}",
                                title = selectedProvider,
                                subtitle = result.vpa,
                                type = "UPI",
                                bankOrProvider = selectedProvider.replace(" UPI", ""),
                                maskedDetails = result.vpa,
                                upiVpa = result.vpa,
                                isDefault = setAsDefault
                            )
                            onSave(newMethod)
                        }
                    }
                },
                enabled = upiInput.isNotBlank() && !isVerifying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SudhaniGoldPrimary,
                    contentColor = SudhaniNavyDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("verify_and_save_upi_button")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        color = SudhaniNavyDark,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verifying VPA...")
                } else {
                    Text("Verify & Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SudhaniTheme.colors.textSecondary)
            }
        }
    )
}

/**
 * Add Card Modal Dialog (Debit Card / Credit Card).
 * Complies with RBI tokenization rules:
 * NEVER collects/stores raw card numbers or CVV in the app database.
 * Only collects safe display info (Bank nickname, last 4 digits, expiry, network) and stores gateway token reference.
 */
@Composable
private fun AddCardDialog(
    cardType: String, // "DEBIT_CARD" or "CREDIT_CARD"
    onDismiss: () -> Unit,
    onSave: (SavedPaymentMethod) -> Unit
) {
    var selectedBank by remember { mutableStateOf("HDFC Bank") }
    var selectedNetwork by remember { mutableStateOf("VISA") }
    var cardholderName by remember { mutableStateOf("") }
    var lastFourDigits by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("12") }
    var expiryYear by remember { mutableStateOf("28") }
    var setAsDefault by remember { mutableStateOf(false) }

    var isTokenizing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val cardTypeName = if (cardType == "CREDIT_CARD") "Credit Card" else "Debit Card"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SudhaniGoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (cardType == "CREDIT_CARD") Icons.Default.CreditScore else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SudhaniGoldDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Add $cardTypeName",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Text(
                        text = "RBI Compliant Tokenization",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Security Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SudhaniTheme.colors.chipBackground
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SudhaniGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Card numbers & CVV are never saved. Only a bank token and last 4 digits are retained.",
                            fontSize = 10.sp,
                            color = SudhaniTheme.colors.textSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Bank Selection
                Text(
                    text = "SELECT ISSUING BANK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank").forEach { bank ->
                        val isSelected = selectedBank == bank
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SudhaniNavyDark else SudhaniTheme.colors.chipBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBank = bank }
                        ) {
                            Text(
                                text = bank.replace(" Bank", ""),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SudhaniTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Card Network Selection
                Text(
                    text = "CARD NETWORK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("VISA", "MASTERCARD", "RUPAY").forEach { network ->
                        val isSelected = selectedNetwork == network
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SudhaniNavyDark else SudhaniTheme.colors.chipBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.cardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedNetwork = network }
                        ) {
                            Text(
                                text = network,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SudhaniTheme.colors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Cardholder Name
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Name on Card") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SudhaniGoldPrimary,
                        unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Last 4 Digits & Expiry in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = lastFourDigits,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                lastFourDigits = it
                            }
                        },
                        label = { Text("Last 4 Digits") },
                        placeholder = { Text("4921") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SudhaniGoldPrimary,
                            unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_last_four_input")
                    )

                    OutlinedTextField(
                        value = "$expiryMonth/$expiryYear",
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 4) {
                                expiryMonth = clean.take(2).padEnd(2, '0').ifBlank { "12" }
                                expiryYear = clean.drop(2).padEnd(2, '8').ifBlank { "28" }
                            }
                        },
                        label = { Text("Expiry (MM/YY)") },
                        placeholder = { Text("12/28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SudhaniGoldPrimary,
                            unfocusedBorderColor = SudhaniTheme.colors.cardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                // Set as Default
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setAsDefault = !setAsDefault },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = SudhaniGoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Set as default payment method",
                        fontSize = 12.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardholderName.isBlank()) {
                        errorMessage = "Please enter cardholder name"
                        return@Button
                    }
                    if (lastFourDigits.length != 4) {
                        errorMessage = "Please enter the last 4 digits of your card"
                        return@Button
                    }

                    coroutineScope.launch {
                        isTokenizing = true
                        errorMessage = null
                        val tokenResult = RazorpayService.tokenizeCardSecurely(
                            bankNickname = selectedBank,
                            cardType = cardType,
                            cardholderName = cardholderName,
                            lastFourDigits = lastFourDigits,
                            expiryMonth = expiryMonth,
                            expiryYear = expiryYear,
                            network = selectedNetwork
                        )
                        isTokenizing = false

                        if (!tokenResult.isSuccess) {
                            errorMessage = tokenResult.errorMessage
                        } else {
                            val cardTitle = "$selectedBank $cardTypeName"
                            val newMethod = SavedPaymentMethod(
                                id = "SPM_CARD_${UUID.randomUUID().toString().take(6)}",
                                title = cardTitle,
                                subtitle = "•••• •••• •••• $lastFourDigits",
                                type = cardType,
                                bankOrProvider = selectedBank,
                                maskedDetails = "•••• •••• •••• $lastFourDigits",
                                lastFourDigits = lastFourDigits,
                                cardNetwork = selectedNetwork,
                                cardExpiry = tokenResult.expiryFormatted,
                                gatewayTokenRef = tokenResult.gatewayTokenRef,
                                isDefault = setAsDefault
                            )
                            onSave(newMethod)
                        }
                    }
                },
                enabled = cardholderName.isNotBlank() && lastFourDigits.length == 4 && !isTokenizing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SudhaniGoldPrimary,
                    contentColor = SudhaniNavyDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("tokenize_and_save_card_button")
            ) {
                if (isTokenizing) {
                    CircularProgressIndicator(
                        color = SudhaniNavyDark,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tokenizing...")
                } else {
                    Text("Tokenize & Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SudhaniTheme.colors.textSecondary)
            }
        }
    )
}
