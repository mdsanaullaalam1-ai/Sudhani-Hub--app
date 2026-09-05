package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Address
import com.example.data.model.Product
import com.example.ui.theme.*

@Composable
fun SudhaniHeader(
    address: Address,
    onAddressClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SudhaniTheme.colors.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed & Address
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAddressClick() }
                ) {
                    // Sudhani Initial Avatar Pill with gold accent
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground)
                            .border(1.dp, SudhaniTheme.colors.chipBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SudhaniTheme.colors.goldAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DELIVERY TO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = SudhaniTheme.colors.navyAccent
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Address",
                                tint = SudhaniTheme.colors.navyAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${address.label} - ${address.house}, ${address.area}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SudhaniTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Pills (Admin View & Profile)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SudhaniTheme.colors.chipBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.chipBorder),
                        modifier = Modifier
                            .clickable { onAdminClick() }
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Admin View",
                                tint = SudhaniTheme.colors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ADMIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = SudhaniTheme.colors.textPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SudhaniTheme.colors.chipBackground)
                            .border(1.dp, SudhaniTheme.colors.chipBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = SudhaniTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onDecrement: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SudhaniTheme.colors.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top Image Box with Adaptive Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SudhaniTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Emoji Visual
                Text(
                    text = product.emoji,
                    fontSize = 46.sp
                )

                // Discount Tag
                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(SudhaniRedDiscount, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delivery Time Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = SudhaniTheme.colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SudhaniTheme.colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HighDensityEmerald)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "10 MINS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (SudhaniTheme.isDark) HighDensityEmeraldLight else HighDensityEmeraldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Brand
            Text(
                text = product.brand,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SudhaniTheme.colors.textSecondary
            )

            // Title
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = SudhaniTheme.colors.textPrimary,
                lineHeight = 15.sp,
                modifier = Modifier.height(30.dp)
            )

            // Unit
            Text(
                text = product.unit,
                fontSize = 10.sp,
                color = SudhaniTheme.colors.textMuted,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = SudhaniTheme.colors.textPrimary
                    )
                    if (product.mrp > product.price) {
                        Text(
                            text = "₹${product.mrp.toInt()}",
                            fontSize = 10.sp,
                            color = SudhaniTheme.colors.textMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Primary Button (Sudhanihub Gold/Yellow with dark navy text for high contrast)
                if (quantityInCart == 0) {
                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SudhaniTheme.colors.primaryButton,
                            contentColor = SudhaniTheme.colors.onPrimaryButton
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("add_to_cart_${product.id}")
                    ) {
                        Text(
                            text = "ADD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SudhaniTheme.colors.onPrimaryButton
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .background(SudhaniTheme.colors.primaryButton, RoundedCornerShape(8.dp))
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = SudhaniTheme.colors.onPrimaryButton,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "$quantityInCart",
                            color = SudhaniTheme.colors.onPrimaryButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = SudhaniTheme.colors.onPrimaryButton,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCartBanner(
    itemCount: Int,
    totalPrice: Double,
    onViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = SudhaniTheme.isDark
    AnimatedVisibility(
        visible = itemCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clickable { onViewCart() },
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color(0xFF131C38) else SudhaniNavyPrimary,
            border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, SudhaniGoldPrimary.copy(alpha = 0.5f)) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Surface(
                            color = HighDensityOrange,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$itemCount ${if (itemCount == 1) "ITEM" else "ITEMS"}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "₹${totalPrice.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(if (isDark) SudhaniGoldPrimary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VIEW CART",
                        color = if (isDark) SudhaniNavyPrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) SudhaniNavyPrimary else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
