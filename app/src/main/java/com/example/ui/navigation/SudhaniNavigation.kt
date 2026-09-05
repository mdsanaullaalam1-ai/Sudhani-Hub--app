package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.SudhaniRepository
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Categories : Screen("categories/{categoryId}") {
        fun createRoute(categoryId: String) = "categories/$categoryId"
    }
    object Search : Screen("search")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    object Cart : Screen("cart")
    object Address : Screen("address")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }
    object MyOrders : Screen("my_orders")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object Profile : Screen("profile")
    object SavedPaymentMethods : Screen("saved_payment_methods")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminOrders : Screen("admin_orders")
    object AdminOrderDetails : Screen("admin_order_details/{orderId}") {
        fun createRoute(orderId: String) = "admin_order_details/$orderId"
    }
    object AdminPayments : Screen("admin_payments")
    object AdminCustomers : Screen("admin_customers")
    object AdminCustomerDetails : Screen("admin_customer_details/{customerId}") {
        fun createRoute(customerId: String) = "admin_customer_details/$customerId"
    }
    object AdminProducts : Screen("admin_products")
    object AdminDelivery : Screen("admin_delivery")
    object AdminDeliveryAreas : Screen("admin_delivery_areas")
    object AdminCoupons : Screen("admin_coupons")
    object AdminSettings : Screen("admin_settings")
    object ReturnRequest : Screen("return_request/{orderId}") {
        fun createRoute(orderId: String) = "return_request/$orderId"
    }
    object AdminReturns : Screen("admin_returns")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun SudhaniAppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cart by SudhaniRepository.cart.collectAsState()
    val cartItemCount = cart.values.sumOf { it.quantity }

    var showAdminDialog by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
        BottomNavItem(Screen.Categories.createRoute("fruits_veg"), "Categories", Icons.Default.Category),
        BottomNavItem(Screen.Search.route, "Search", Icons.Default.Search),
        BottomNavItem(Screen.MyOrders.route, "Orders", Icons.Default.ReceiptLong),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        "categories/{categoryId}",
        Screen.Search.route,
        Screen.MyOrders.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = SudhaniTheme.colors.bottomBarBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.divider),
                    shadowElevation = 4.dp
                ) {
                    NavigationBar(
                        containerColor = SudhaniTheme.colors.bottomBarBackground,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = when {
                                item.route == Screen.Home.route -> currentRoute == Screen.Home.route
                                item.route.startsWith("categories") -> currentRoute == "categories/{categoryId}"
                                item.route == Screen.Search.route -> currentRoute == Screen.Search.route
                                item.route == Screen.MyOrders.route -> currentRoute == Screen.MyOrders.route
                                item.route == Screen.Profile.route -> currentRoute == Screen.Profile.route
                                else -> false
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                },
                                label = {
                                    Text(
                                        text = item.title.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        letterSpacing = 0.4.sp,
                                        color = if (isSelected) SudhaniTheme.colors.bottomBarSelected else SudhaniTheme.colors.bottomBarUnselected
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SudhaniTheme.colors.bottomBarSelected,
                                    unselectedIconColor = SudhaniTheme.colors.bottomBarUnselected,
                                    indicatorColor = SudhaniTheme.colors.bottomBarIndicator
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCategory = { catId ->
                        navController.navigate(Screen.Categories.createRoute(catId))
                    },
                    onNavigateToProduct = { prodId ->
                        navController.navigate(Screen.ProductDetail.createRoute(prodId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onNavigateToAddress = {
                        navController.navigate(Screen.Address.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onOpenAdmin = {
                        navController.navigate(Screen.AdminDashboard.route)
                    }
                )
            }

            composable(
                route = Screen.Categories.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "fruits_veg"
                CategoriesScreen(
                    initialCategoryId = categoryId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { prodId ->
                        navController.navigate(Screen.ProductDetail.createRoute(prodId))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { prodId ->
                        navController.navigate(Screen.ProductDetail.createRoute(prodId))
                    },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: "fv_1"
                ProductDetailScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { prodId ->
                        navController.navigate(Screen.ProductDetail.createRoute(prodId))
                    },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                    onStartShopping = { navController.navigate(Screen.Home.route) },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    },
                    onChangeAddress = { navController.navigate(Screen.Address.route) }
                )
            }

            composable(Screen.Address.route) {
                AddressScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onChangeAddress = { navController.navigate(Screen.Address.route) },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "SH-1001"
                OrderSuccessScreen(
                    orderId = orderId,
                    onTrackOrder = {
                        navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                            popUpTo(Screen.OrderSuccess.route) { inclusive = true }
                        }
                    },
                    onContinueShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.MyOrders.route) {
                MyOrdersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTrackOrder = { orderId ->
                        navController.navigate(Screen.OrderTracking.createRoute(orderId))
                    },
                    onReorder = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onRequestReturn = { orderId ->
                        navController.navigate(Screen.ReturnRequest.createRoute(orderId))
                    }
                )
            }

            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onRequestReturn = { ordId ->
                        navController.navigate(Screen.ReturnRequest.createRoute(ordId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrders = { navController.navigate(Screen.MyOrders.route) },
                    onNavigateToAddresses = { navController.navigate(Screen.Address.route) },
                    onNavigateToProduct = { prodId ->
                        navController.navigate(Screen.ProductDetail.createRoute(prodId))
                    },
                    onOpenAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToSavedPayments = {
                        navController.navigate(Screen.SavedPaymentMethods.route)
                    }
                )
            }

            composable(Screen.SavedPaymentMethods.route) {
                SavedPaymentMethodsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminScreen(
                    onNavigateBackToStore = { navController.popBackStack() },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.AdminOrderDetails.createRoute(orderId))
                    },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) },
                    onNavigateToCoupons = { navController.navigate(Screen.AdminCoupons.route) },
                    onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) },
                    onNavigateToDeliveryAreas = { navController.navigate(Screen.AdminDeliveryAreas.route) },
                    onNavigateToReturns = { navController.navigate(Screen.AdminReturns.route) }
                )
            }

            composable(Screen.AdminOrders.route) {
                AdminOrdersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.AdminOrderDetails.createRoute(orderId))
                    },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(
                route = Screen.AdminOrderDetails.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                AdminOrderDetailsScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCustomerDetails = { customerId ->
                        navController.navigate(Screen.AdminCustomerDetails.createRoute(customerId))
                    }
                )
            }

            composable(Screen.AdminPayments.route) {
                AdminPaymentsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.AdminOrderDetails.createRoute(orderId))
                    },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(Screen.AdminCustomers.route) {
                AdminCustomersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCustomerDetails = { customerId ->
                        navController.navigate(Screen.AdminCustomerDetails.createRoute(customerId))
                    },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(
                route = Screen.AdminCustomerDetails.route,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                AdminCustomerDetailsScreen(
                    customerId = customerId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.AdminOrderDetails.createRoute(orderId))
                    }
                )
            }

            composable(Screen.AdminProducts.route) {
                AdminProductsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(Screen.AdminDelivery.route) {
                AdminDeliveryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.AdminOrderDetails.createRoute(orderId))
                    },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDeliveryAreas = { navController.navigate(Screen.AdminDeliveryAreas.route) }
                )
            }

            composable(Screen.AdminDeliveryAreas.route) {
                AdminDeliveryAreasScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(Screen.AdminCoupons.route) {
                AdminCouponsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(Screen.AdminSettings.route) {
                AdminSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) }
                )
            }

            composable(
                route = Screen.ReturnRequest.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                ReturnRequestScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onReturnSubmitted = { returnId ->
                        navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                            popUpTo(Screen.OrderTracking.createRoute(orderId)) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AdminReturns.route) {
                AdminReturnsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                    onNavigateToPayments = { navController.navigate(Screen.AdminPayments.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToDelivery = { navController.navigate(Screen.AdminDelivery.route) },
                    onNavigateToSettings = { navController.navigate(Screen.AdminSettings.route) }
                )
            }
        }

        if (showAdminDialog) {
            AdminDashboardDialog(
                onDismiss = { showAdminDialog = false }
            )
        }
    }
}
