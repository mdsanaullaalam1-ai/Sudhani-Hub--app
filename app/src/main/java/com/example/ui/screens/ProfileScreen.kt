package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.SudhanihubLogo
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSavedPayments: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val user by SudhaniRepository.user.collectAsState()
    val wishlist by SudhaniRepository.wishlist.collectAsState()
    val walletBalance by SudhaniRepository.walletBalance.collectAsState()
    val shoppingLists by SudhaniRepository.shoppingLists.collectAsState()
    val savedPayments by SudhaniRepository.savedPaymentMethods.collectAsState()
    val userReviews by SudhaniRepository.userReviews.collectAsState()
    val notifications by SudhaniRepository.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }
    val currentThemeMode by ThemeManager.themeMode.collectAsState()

    // Dialog & Sheet States
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showShoppingListsDialog by remember { mutableStateOf(false) }
    var showSavedPaymentsDialog by remember { mutableStateOf(false) }
    var showReviewsDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showWishlistDialog by remember { mutableStateOf(false) }
    var showOffersDialog by remember { mutableStateOf(false) }
    var showDealsDialog by remember { mutableStateOf(false) }
    var showReferDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SudhanihubLogo(
                            size = 32.dp,
                            showText = false,
                            elevation = 1.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "Sudhanihub Grocery Delivery",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniGoldDark,
                                letterSpacing = 0.3.sp
                            )
                        }
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
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(
                                        containerColor = SudhaniGoldPrimary,
                                        contentColor = SudhaniNavyPrimary
                                    ) {
                                        Text("$unreadNotifs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = SudhaniTheme.colors.textPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SudhaniTheme.colors.surface
                )
            )
        },
        containerColor = SudhaniTheme.colors.background,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP PROFILE CARD WITH OFFICIAL SUDHANIHUB LOGO
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("top_profile_card")
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
                            // Circular Profile Avatar with Gold Accent Ring & Edit Badge
                            UserAvatar(
                                user = user,
                                size = 66.dp,
                                showEditBadge = true,
                                onEditClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .clickable { showEditProfileDialog = true }
                                    .testTag("top_profile_avatar")
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // User Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = SudhaniTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = SudhaniGoldLight,
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, SudhaniGoldBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SudhaniGoldDark,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                "VERIFIED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = SudhaniGoldDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = SudhaniTheme.colors.goldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user.phone,
                                        fontSize = 13.sp,
                                        color = SudhaniTheme.colors.textSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = SudhaniTheme.colors.goldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user.email,
                                        fontSize = 12.sp,
                                        color = SudhaniTheme.colors.textSecondary
                                    )
                                }
                            }

                            // Official Sudhanihub Logo in clean, small position
                            SudhanihubLogo(
                                size = 44.dp,
                                showText = true,
                                elevation = 1.dp,
                                modifier = Modifier.testTag("profile_sudhanihub_logo")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom row of card: Member status & Edit Profile Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SudhaniTheme.colors.chipBackground,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, SudhaniTheme.colors.chipBorder)
                            ) {
                                Text(
                                    text = "SUDHANIHUB MEMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SudhaniTheme.colors.textPrimary,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Edit Profile Button
                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = SudhaniTheme.colors.chipBackground,
                                    contentColor = SudhaniTheme.colors.textPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Profile",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // QUICK ACTIONS (3 EQUALLY SPACED BUTTONS: Orders, Wallet, Address)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Orders Button
                    QuickActionCard(
                        icon = Icons.Default.ReceiptLong,
                        iconTint = SudhaniTheme.colors.textPrimary,
                        iconBg = SudhaniTheme.colors.chipBackground,
                        title = "Orders",
                        subtitle = "History",
                        onClick = onNavigateToOrders,
                        modifier = Modifier.weight(1f).testTag("quick_action_orders")
                    )

                    // Wallet Button
                    QuickActionCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = SudhaniGoldDark,
                        iconBg = SudhaniGoldLight,
                        title = "Wallet",
                        subtitle = "₹${walletBalance.toInt()}",
                        highlightSubtitle = true,
                        onClick = { showWalletDialog = true },
                        modifier = Modifier.weight(1f).testTag("quick_action_wallet")
                    )

                    // Address Button
                    QuickActionCard(
                        icon = Icons.Default.LocationOn,
                        iconTint = SudhaniTheme.colors.textPrimary,
                        iconBg = SudhaniTheme.colors.chipBackground,
                        title = "Address",
                        subtitle = "Saved",
                        onClick = onNavigateToAddresses,
                        modifier = Modifier.weight(1f).testTag("quick_action_address")
                    )
                }
            }

            // ACCOUNT MENU SECTION
            item {
                Text(
                    text = "ACCOUNT SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Appearance Theme Setting
                        AccountMenuItemRow(
                            icon = when (currentThemeMode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            },
                            iconTint = SudhaniGoldDark,
                            title = "Appearance",
                            subtitle = currentThemeMode.title,
                            badgeText = "THEME",
                            onClick = { showAppearanceDialog = true },
                            testTag = "menu_appearance"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.Favorite,
                            iconTint = SudhaniGoldDark,
                            title = "My Wishlist",
                            subtitle = if (wishlist.isEmpty()) "No saved items" else "${wishlist.size} saved favorites",
                            onClick = { showWishlistDialog = true },
                            testTag = "menu_wishlist"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.FormatListBulleted,
                            iconTint = SudhaniTheme.colors.goldAccent,
                            title = "Shopping Lists",
                            subtitle = "${shoppingLists.size} recurring grocery lists",
                            onClick = { showShoppingListsDialog = true },
                            testTag = "menu_shopping_lists"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.Payment,
                            iconTint = SudhaniTheme.colors.goldAccent,
                            title = "Saved Payments",
                            subtitle = "${savedPayments.size} UPI IDs & Cards",
                            onClick = {
                                if (onNavigateToSavedPayments != null) {
                                    onNavigateToSavedPayments()
                                } else {
                                    showSavedPaymentsDialog = true
                                }
                            },
                            testTag = "menu_saved_payments"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.StarOutline,
                            iconTint = SudhaniGoldDark,
                            title = "Ratings & Reviews",
                            subtitle = "${userReviews.size} product ratings submitted",
                            onClick = { showReviewsDialog = true },
                            testTag = "menu_ratings_reviews"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.NotificationsNone,
                            iconTint = SudhaniTheme.colors.goldAccent,
                            title = "Notifications",
                            subtitle = if (unreadNotifs > 0) "$unreadNotifs new delivery & offer updates" else "All caught up",
                            badgeText = if (unreadNotifs > 0) "$unreadNotifs NEW" else null,
                            onClick = { showNotificationsDialog = true },
                            testTag = "menu_notifications"
                        )
                    }
                }
            }

            // MORE FROM SUDHANI HUB SECTION
            item {
                Text(
                    text = "More from Sudhani Hub",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }

            item {
                val chipBg = SudhaniTheme.colors.chipBackground
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sudhani Hub Offers
                        SudhaniFeatureCard(
                            emoji = "🏷️",
                            title = "Sudhani Offers",
                            subtitle = "Up to 50% OFF & coupons",
                            badgeColor = SudhaniGoldLight,
                            onClick = { showOffersDialog = true },
                            modifier = Modifier.weight(1f).testTag("feature_offers")
                        )

                        // Grocery Deals
                        SudhaniFeatureCard(
                            emoji = "⚡",
                            title = "Grocery Deals",
                            subtitle = "Daily flash discounts",
                            badgeColor = chipBg,
                            onClick = { showDealsDialog = true },
                            modifier = Modifier.weight(1f).testTag("feature_deals")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Help & Support
                        SudhaniFeatureCard(
                            emoji = "🎧",
                            title = "Help & Support",
                            subtitle = "24/7 instant chat support",
                            badgeColor = chipBg,
                            onClick = { showSupportDialog = true },
                            modifier = Modifier.weight(1f).testTag("feature_help")
                        )

                        // Refer & Earn
                        SudhaniFeatureCard(
                            emoji = "🎁",
                            title = "Refer & Earn",
                            subtitle = "Get ₹100 grocery credit",
                            badgeColor = SudhaniGoldLight,
                            onClick = { showReferDialog = true },
                            modifier = Modifier.weight(1f).testTag("feature_refer")
                        )
                    }
                }
            }

            // SUPPORT & LEGAL SECTION
            item {
                Text(
                    text = "SUPPORT & LEGAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SudhaniTheme.colors.textPrimary.copy(alpha = 0.8f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AccountMenuItemRow(
                            icon = Icons.Default.SupportAgent,
                            iconTint = SudhaniTheme.colors.goldAccent,
                            title = "Help & Support",
                            subtitle = "FAQs, order issues & instant refunds",
                            onClick = { showSupportDialog = true },
                            testTag = "support_help"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.HelpOutline,
                            iconTint = SudhaniTheme.colors.goldAccent,
                            title = "FAQs",
                            subtitle = "Delivery times, quality & cancellation rules",
                            onClick = { showFaqDialog = true },
                            testTag = "support_faq"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.Description,
                            iconTint = SudhaniTheme.colors.textSecondary,
                            title = "Terms & Conditions",
                            subtitle = "Usage rules & delivery policies",
                            onClick = { showTermsDialog = true },
                            testTag = "support_terms"
                        )
                        HorizontalDivider(color = SudhaniTheme.colors.cardBorder, thickness = 1.dp)

                        AccountMenuItemRow(
                            icon = Icons.Default.PrivacyTip,
                            iconTint = SudhaniTheme.colors.textSecondary,
                            title = "Privacy Policy",
                            subtitle = "How we protect your data & location",
                            onClick = { showPrivacyDialog = true },
                            testTag = "support_privacy"
                        )
                    }
                }
            }

            // STORE ADMIN OPERATIONS SHORTCUT
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.chipBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdmin() }
                        .testTag("admin_operations_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SudhaniNavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SudhaniGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin & Store Operations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "Dark store inventory, pin codes & dispatch simulation",
                                fontSize = 11.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SudhaniTheme.colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // LOGOUT BUTTON
            item {
                Button(
                    onClick = { showLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighDensityRed.copy(alpha = 0.08f),
                        contentColor = HighDensityRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityRed.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // APP VERSION INFO WITH SUDHANIHUB LOGO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SudhanihubLogo(
                        size = 20.dp,
                        showText = false,
                        elevation = 0.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sudhanihub v3.2.0 • 10-Min Fast Grocery Delivery",
                        fontSize = 11.sp,
                        color = SudhaniTheme.colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // DIALOGS
    if (showAppearanceDialog) {
        AppearanceDialog(
            currentMode = currentThemeMode,
            onSelectMode = { mode ->
                ThemeManager.setThemeMode(mode)
            },
            onDismiss = { showAppearanceDialog = false }
        )
    }

    // DIALOGS
    if (showEditProfileDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showWalletDialog) {
        WalletDialog(
            onDismiss = { showWalletDialog = false }
        )
    }

    if (showShoppingListsDialog) {
        ShoppingListsDialog(
            onDismiss = { showShoppingListsDialog = false }
        )
    }

    if (showSavedPaymentsDialog) {
        SavedPaymentsDialog(
            onDismiss = { showSavedPaymentsDialog = false }
        )
    }

    if (showReviewsDialog) {
        ReviewsDialog(
            onDismiss = { showReviewsDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showWishlistDialog) {
        WishlistDialog(
            onDismiss = { showWishlistDialog = false },
            onNavigateToProduct = onNavigateToProduct
        )
    }

    if (showOffersDialog) {
        OffersDialog(
            onDismiss = { showOffersDialog = false }
        )
    }

    if (showDealsDialog) {
        GroceryDealsDialog(
            onDismiss = { showDealsDialog = false },
            onNavigateToProduct = onNavigateToProduct
        )
    }

    if (showReferDialog) {
        ReferAndEarnDialog(
            onDismiss = { showReferDialog = false }
        )
    }

    if (showSupportDialog) {
        SupportDialog(
            onDismiss = { showSupportDialog = false }
        )
    }

    if (showFaqDialog) {
        FaqDialog(
            onDismiss = { showFaqDialog = false }
        )
    }

    if (showTermsDialog) {
        TermsDialog(
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showLogoutConfirmDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutConfirmDialog = false },
            onConfirm = {
                showLogoutConfirmDialog = false
                onLogout()
            }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    highlightSubtitle: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SudhaniTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (highlightSubtitle) SudhaniGoldDark else SudhaniTheme.colors.textSecondary,
                fontWeight = if (highlightSubtitle) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AccountMenuItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SudhaniTheme.colors.textPrimary
                )
                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SudhaniGoldLight,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, SudhaniGoldBorder)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniGoldDark,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SudhaniTheme.colors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SudhaniTheme.colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SudhaniFeatureCard(
    emoji: String,
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SudhaniTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SudhaniTheme.colors.textSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun AppearanceDialog(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SudhaniTheme.colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SudhaniGoldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = SudhaniGoldDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Choose Theme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Text(
                        text = "Sudhanihub Appearance",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionItem(
                    mode = ThemeMode.LIGHT,
                    title = "Light",
                    subtitle = "Clean white background with navy accents",
                    icon = Icons.Default.LightMode,
                    isSelected = currentMode == ThemeMode.LIGHT,
                    onSelect = {
                        onSelectMode(ThemeMode.LIGHT)
                        onDismiss()
                    }
                )
                ThemeOptionItem(
                    mode = ThemeMode.DARK,
                    title = "Dark",
                    subtitle = "Deep navy/black canvas with gold highlights",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentMode == ThemeMode.DARK,
                    onSelect = {
                        onSelectMode(ThemeMode.DARK)
                        onDismiss()
                    }
                )
                ThemeOptionItem(
                    mode = ThemeMode.SYSTEM,
                    title = "System Default",
                    subtitle = "Automatically follows your Android system settings",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentMode == ThemeMode.SYSTEM,
                    onSelect = {
                        onSelectMode(ThemeMode.SYSTEM)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = SudhaniGoldDark)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ThemeOptionItem(
    mode: ThemeMode,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isDark = SudhaniTheme.isDark
    val containerBg = if (isSelected) {
        if (isDark) SudhaniGoldPrimary.copy(alpha = 0.15f) else SudhaniGoldLight.copy(alpha = 0.6f)
    } else {
        SudhaniTheme.colors.chipBackground
    }
    val borderColor = if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.chipBorder

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = containerBg,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_option_${mode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SudhaniGoldPrimary else SudhaniTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) SudhaniNavyDark else SudhaniTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SudhaniTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SudhaniTheme.colors.textSecondary,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SudhaniGoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = SudhaniNavyDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SudhaniTheme.colors.textMuted, CircleShape)
                )
            }
        }
    }
}
