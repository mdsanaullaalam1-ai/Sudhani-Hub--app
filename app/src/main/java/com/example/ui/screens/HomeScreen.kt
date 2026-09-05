package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.FloatingCartBanner
import com.example.ui.components.ProductCard
import com.example.ui.components.SudhaniHeader
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAddress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by SudhaniRepository.products.collectAsState()
    val cart by SudhaniRepository.cart.collectAsState()
    val selectedAddress by SudhaniRepository.selectedAddress.collectAsState()

    val popularProducts = remember(products) { products.filter { it.isPopular } }
    val bestDeals = remember(products) { products.filter { it.isDeal } }
    val recentlyPurchased = remember(products) { products.filter { it.isRecentlyPurchased } }

    val totalItems = cart.values.sumOf { it.quantity }
    val totalPrice = cart.values.sumOf { it.product.price * it.quantity }

    val bannerPromos = listOf(
        PromoBannerData(
            title = "FLAT ₹100 OFF",
            subtitle = "Use code: JALDI100 on ₹399+",
            highlight = "⚡ 10-MIN FLASH DEAL",
            gradient = listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
        ),
        PromoBannerData(
            title = "Farm Fresh Fruits",
            subtitle = "Direct from organic orchards",
            highlight = "🥭 SEASON'S SWEETEST",
            gradient = listOf(Color(0xFFEA580C), Color(0xFF9A3412))
        ),
        PromoBannerData(
            title = "Breakfast & Dairy",
            subtitle = "Milk, Bread, Eggs & Butter Daily",
            highlight = "🥛 AT 6 AM DAILY",
            gradient = listOf(Color(0xFF0D9488), Color(0xFF115E59))
        )
    )

    Box(modifier = modifier.fillMaxSize().background(SudhaniTheme.colors.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (totalItems > 0) 90.dp else 24.dp)
        ) {
            // Sudhani Header
            item {
                SudhaniHeader(
                    address = selectedAddress,
                    onAddressClick = onNavigateToAddress,
                    onProfileClick = onNavigateToProfile,
                    onAdminClick = onOpenAdmin
                )
            }

            // Search Bar Trigger (Adaptive Surface + Pill)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SudhaniTheme.colors.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clickable { onNavigateToSearch() }
                                .testTag("home_search_bar"),
                            shape = RoundedCornerShape(14.dp),
                            color = SudhaniTheme.colors.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = SudhaniTheme.colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Search 'milk', 'bread', 'chips'...",
                                    fontSize = 13.sp,
                                    color = SudhaniTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            // Promo Banners Carousel
            item {
                Spacer(modifier = Modifier.height(10.dp))
                val pagerState = rememberPagerState(pageCount = { bannerPromos.size })
                Column {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(124.dp)
                    ) { page ->
                        val promo = bannerPromos[page]
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.horizontalGradient(promo.gradient))
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = promo.highlight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = promo.title,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = promo.subtitle,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(bannerPromos.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.5.dp)
                                    .size(if (pagerState.currentPage == index) 6.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) HighDensityIndigo else HighDensitySlate200
                                    )
                            )
                        }
                    }
                }
            }

            // Categories Section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    TextButton(
                        onClick = { onNavigateToCategory("all") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VIEW ALL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = SudhaniTheme.colors.goldAccent
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SudhaniRepository.categories) { cat ->
                        CategoryHomePill(
                            category = cat,
                            onClick = { onNavigateToCategory(cat.id) }
                        )
                    }
                }
            }

            // Best Deals Section
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(
                    title = "⚡ SUPER DEALS TODAY",
                    subtitle = "Grab maximum discounts before stocks run out"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bestDeals) { prod ->
                        val qty = cart[prod.id]?.quantity ?: 0
                        Box(modifier = Modifier.width(145.dp)) {
                            ProductCard(
                                product = prod,
                                quantityInCart = qty,
                                onAddToCart = { SudhaniRepository.addToCart(prod) },
                                onDecrement = { SudhaniRepository.decrementQuantity(prod.id) },
                                onCardClick = { onNavigateToProduct(prod.id) }
                            )
                        }
                    }
                }
            }

            // Popular Products
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(
                    title = "⭐ QUICK PICKS IN 10 MINS",
                    subtitle = "Most ordered daily essentials near you"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(popularProducts) { prod ->
                        val qty = cart[prod.id]?.quantity ?: 0
                        Box(modifier = Modifier.width(145.dp)) {
                            ProductCard(
                                product = prod,
                                quantityInCart = qty,
                                onAddToCart = { SudhaniRepository.addToCart(prod) },
                                onDecrement = { SudhaniRepository.decrementQuantity(prod.id) },
                                onCardClick = { onNavigateToProduct(prod.id) }
                            )
                        }
                    }
                }
            }

            // Recently Purchased (if any)
            if (recentlyPurchased.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SectionTitle(
                        title = "🔄 RECENTLY PURCHASED",
                        subtitle = "Quickly reorder your everyday favorites"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentlyPurchased) { prod ->
                            val qty = cart[prod.id]?.quantity ?: 0
                            Box(modifier = Modifier.width(145.dp)) {
                                ProductCard(
                                    product = prod,
                                    quantityInCart = qty,
                                    onAddToCart = { SudhaniRepository.addToCart(prod) },
                                    onDecrement = { SudhaniRepository.decrementQuantity(prod.id) },
                                    onCardClick = { onNavigateToProduct(prod.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Cart Bar
        FloatingCartBanner(
            itemCount = totalItems,
            totalPrice = totalPrice,
            onViewCart = onNavigateToCart,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class PromoBannerData(
    val title: String,
    val subtitle: String,
    val highlight: String,
    val gradient: List<Color>
)

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp,
            color = SudhaniTheme.colors.textPrimary
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = SudhaniTheme.colors.textSecondary
        )
    }
}

@Composable
fun CategoryHomePill(
    category: Category,
    onClick: () -> Unit
) {
    val isDark = SudhaniTheme.isDark
    // Distinct soft pastel tints per category in light mode, sleek dark cards in dark mode
    val (bgColor, borderColor) = if (isDark) {
        SudhaniTheme.colors.surfaceVariant to SudhaniTheme.colors.cardBorder
    } else {
        when (category.id) {
            "fruits_veg" -> Color(0xFFF0FDF4) to Color(0xFFDCFCE7)
            "dairy_bread" -> Color(0xFFEFF6FF) to Color(0xFFDBEAFE)
            "munchies_snacks" -> Color(0xFFFFF7ED) to Color(0xFFFFEDD5)
            "cold_drinks" -> Color(0xFFFAF5FF) to Color(0xFFF3E8FF)
            "instant_food" -> Color(0xFFFEF3C7) to Color(0xFFFDE68A)
            "atta_rice" -> Color(0xFFFFFBEB) to Color(0xFFFEF3C7)
            "personal_care" -> Color(0xFFFDF2F8) to Color(0xFFFCE7F3)
            "cleaning" -> Color(0xFFECFEFF) to Color(0xFFCFFAFE)
            else -> HighDensityIndigoLight to HighDensityIndigo100
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.iconName,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SudhaniTheme.colors.textPrimary,
            maxLines = 2,
            lineHeight = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
