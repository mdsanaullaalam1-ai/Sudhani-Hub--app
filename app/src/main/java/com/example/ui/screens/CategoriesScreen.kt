package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.FloatingCartBanner
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    initialCategoryId: String = "fruits_veg",
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = SudhaniRepository.categories
    var selectedCategoryId by remember {
        mutableStateOf(
            if (initialCategoryId == "all" || categories.none { it.id == initialCategoryId }) {
                categories.first().id
            } else {
                initialCategoryId
            }
        )
    }

    val products by SudhaniRepository.products.collectAsState()
    val cart by SudhaniRepository.cart.collectAsState()

    val filteredProducts = remember(products, selectedCategoryId) {
        products.filter { it.categoryId == selectedCategoryId }
    }

    val totalItems = cart.values.sumOf { it.quantity }
    val totalPrice = cart.values.sumOf { it.product.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(modifier = Modifier.fillMaxSize().background(HighDensitySlate50)) {
                // Left Categories Sidebar (High Density Quick-Commerce Style)
                LazyColumn(
                    modifier = Modifier
                        .width(84.dp)
                        .fillMaxHeight()
                        .background(HighDensitySlate100)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) Color.White else Color.Transparent
                                )
                                .clickable { selectedCategoryId = cat.id }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("cat_tab_${cat.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) HighDensityIndigoLight else Color.White
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) HighDensityIndigo else HighDensitySlate200,
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = cat.iconName, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = cat.name,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) HighDensityIndigo else HighDensitySlate600,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 11.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Products Grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "More items coming soon in this category!",
                                color = HighDensitySlate600,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = if (totalItems > 0) 90.dp else 24.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredProducts) { prod ->
                                val qty = cart[prod.id]?.quantity ?: 0
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

            // Floating Cart Banner
            FloatingCartBanner(
                itemCount = totalItems,
                totalPrice = totalPrice,
                onViewCart = onNavigateToCart,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
