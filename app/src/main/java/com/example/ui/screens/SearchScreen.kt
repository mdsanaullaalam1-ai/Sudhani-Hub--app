package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SudhaniRepository
import com.example.ui.components.FloatingCartBanner
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

enum class SortOption(val title: String) {
    RELEVANCE("Relevance"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var currentSort by remember { mutableStateOf(SortOption.RELEVANCE) }

    val products by SudhaniRepository.products.collectAsState()
    val cart by SudhaniRepository.cart.collectAsState()

    val suggestions = listOf("Amul Milk", "Tomato", "Chakki Atta", "Mangoes", "Chips", "Noodles", "BoAt Charger")

    val searchResults = remember(products, searchQuery, selectedCategoryFilter, currentSort) {
        var list = products.filter { prod ->
            val matchesQuery = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.brand.contains(searchQuery, ignoreCase = true) ||
                    prod.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || prod.categoryId == selectedCategoryFilter
            matchesQuery && matchesCategory
        }

        list = when (currentSort) {
            SortOption.RELEVANCE -> list
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
        }
        list
    }

    val totalItems = cart.values.sumOf { it.quantity }
    val totalPrice = cart.values.sumOf { it.product.price * it.quantity }

    Scaffold(
        containerColor = HighDensitySlate50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = HighDensitySlate900)
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search for groceries, veggies, milk...", fontSize = 13.sp, color = HighDensitySlate400) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = HighDensityIndigo
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = HighDensitySlate600)
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input_field")
                    )
                }

                HorizontalDivider(color = HighDensitySlate200)

                // Filter & Sort Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Filters
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("ALL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighDensityIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = HighDensitySlate700
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategoryFilter == null,
                                    borderColor = if (selectedCategoryFilter == null) HighDensityIndigo else HighDensitySlate200
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        items(SudhaniRepository.categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryFilter = if (isSelected) null else cat.id
                                },
                                label = { Text("${cat.iconName} ${cat.name}".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighDensityIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = HighDensitySlate700
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) HighDensityIndigo else HighDensitySlate200
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Sort Toggle
                    IconButton(
                        onClick = {
                            currentSort = when (currentSort) {
                                SortOption.RELEVANCE -> SortOption.PRICE_LOW_TO_HIGH
                                SortOption.PRICE_LOW_TO_HIGH -> SortOption.PRICE_HIGH_TO_LOW
                                SortOption.PRICE_HIGH_TO_LOW -> SortOption.RELEVANCE
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = if (currentSort != SortOption.RELEVANCE) HighDensityIndigo else HighDensitySlate600
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchResults.isEmpty()) {
                // "No products found" empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SudhaniRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = SudhaniRedDiscount,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "No products found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "We couldn't find matches for '$searchQuery'. Try checking spelling or search popular essentials below.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            searchQuery = ""
                            selectedCategoryFilter = null
                        }
                    ) {
                        Text("Reset Search")
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Quick Search Suggestions when query is short
                    if (searchQuery.isBlank()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Popular Suggestions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                suggestions.take(3).forEach { term ->
                                    SuggestionChip(
                                        onClick = { searchQuery = term },
                                        label = { Text(term) }
                                    )
                                }
                            }
                        }
                    }

                    // Product Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = if (totalItems > 0) 90.dp else 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(searchResults) { prod ->
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
