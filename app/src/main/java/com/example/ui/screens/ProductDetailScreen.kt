package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by SudhaniRepository.products.collectAsState()
    val cart by SudhaniRepository.cart.collectAsState()
    val wishlist by SudhaniRepository.wishlist.collectAsState()

    val product = remember(products, productId) {
        products.find { it.id == productId } ?: products.first()
    }

    val isWishlisted = wishlist.contains(product.id)
    val quantityInCart = cart[product.id]?.quantity ?: 0

    val relatedProducts = remember(products, product) {
        products.filter { it.categoryId == product.categoryId && it.id != product.id }
    }

    Scaffold(
        containerColor = SudhaniTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = product.brand.uppercase(), fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 0.5.sp, color = SudhaniTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SudhaniTheme.colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { SudhaniRepository.toggleWishlist(product.id) }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) SudhaniGoldDark else SudhaniTheme.colors.textSecondary
                        )
                    }
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                val totalItems = cart.values.sumOf { it.quantity }
                                if (totalItems > 0) {
                                    Badge(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark) { Text("$totalItems", fontWeight = FontWeight.Bold) }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart", tint = SudhaniTheme.colors.textPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SudhaniTheme.colors.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shadowElevation = 8.dp,
                color = SudhaniTheme.colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SudhaniTheme.colors.textPrimary
                        )
                        if (product.mrp > product.price) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MRP ₹${product.mrp.toInt()}",
                                    fontSize = 11.sp,
                                    color = SudhaniTheme.colors.textMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${product.discountPercent}% OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SudhaniGoldDark
                                )
                            }
                        }
                    }

                    if (quantityInCart == 0) {
                        OutlinedButton(
                            onClick = { SudhaniRepository.addToCart(product) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SudhaniGoldPrimary),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("product_detail_add_cart")
                        ) {
                            Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, tint = SudhaniGoldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD TO CART", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp, color = SudhaniGoldDark)
                        }

                        Button(
                            onClick = {
                                SudhaniRepository.addToCart(product)
                                onNavigateToCart()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("product_detail_buy_now")
                        ) {
                            Text("BUY NOW", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp, color = SudhaniNavyDark)
                        }
                    } else {
                        // Stepper + Checkout
                        Row(
                            modifier = Modifier
                                .height(42.dp)
                                .background(SudhaniTheme.colors.chipBackground, RoundedCornerShape(10.dp))
                                .border(1.dp, SudhaniTheme.colors.chipBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { SudhaniRepository.decrementQuantity(product.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", tint = SudhaniTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$quantityInCart IN CART",
                                color = SudhaniTheme.colors.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { SudhaniRepository.addToCart(product) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = SudhaniTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Button(
                            onClick = onNavigateToCart,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SudhaniGoldPrimary, contentColor = SudhaniNavyDark),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text("GO TO CART", color = SudhaniNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Image Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SudhaniTheme.colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.emoji, fontSize = 96.sp)

                        // 10 Min Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = SudhaniGoldPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = SudhaniNavyDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "10 MINS DELIVERY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = SudhaniNavyDark
                                )
                            }
                        }
                    }
                }
            }

            // Title, Brand, Unit
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = product.brand.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = SudhaniGoldDark
                )
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SudhaniTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Unit: ${product.unit}",
                    fontSize = 12.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            }

            // Rating & Stock
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SudhaniTheme.colors.chipBackground,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${product.rating} ★",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniGoldDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${product.reviewCount} reviews)",
                                fontSize = 10.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                    }

                    Surface(
                        color = SudhaniTheme.colors.chipBackground,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder)
                    ) {
                        Text(
                            text = "In Stock (${product.stock} units)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Description
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PRODUCT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = SudhaniTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = SudhaniTheme.colors.textSecondary
                )
            }

            // Freshness & Delivery Promise
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SudhaniTheme.colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SudhaniTheme.colors.chipBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SudhaniGoldDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SudhaniHub Quality Promise",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SudhaniTheme.colors.textPrimary
                            )
                            Text(
                                text = "100% genuine products, hygienically handled & delivered instantly.",
                                fontSize = 10.sp,
                                color = SudhaniTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Related Products
            if (relatedProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "YOU MIGHT ALSO NEED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(relatedProducts) { rel ->
                            val qty = cart[rel.id]?.quantity ?: 0
                            Box(modifier = Modifier.width(148.dp)) {
                                ProductCard(
                                    product = rel,
                                    quantityInCart = qty,
                                    onAddToCart = { SudhaniRepository.addToCart(rel) },
                                    onDecrement = { SudhaniRepository.decrementQuantity(rel.id) },
                                    onCardClick = { onNavigateToProduct(rel.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
