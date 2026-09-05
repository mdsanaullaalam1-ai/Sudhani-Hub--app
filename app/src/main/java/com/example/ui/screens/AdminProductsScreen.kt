package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.ListingStatus
import com.example.data.model.Product
import com.example.data.repository.SudhaniRepository
import com.example.ui.theme.*

@Composable
fun AdminProductsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToDelivery: () -> Unit
) {
    AdminAccessGuard(onNavigateBackToStore = onNavigateBack) {
        val products by SudhaniRepository.products.collectAsState()
        val categories = SudhaniRepository.categories
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }

        var productToEdit by remember { mutableStateOf<Product?>(null) }
        var showAddProductDialog by remember { mutableStateOf(false) }
        var productToDelete by remember { mutableStateOf<Product?>(null) }

        val filteredProducts = remember(products, searchQuery, selectedCategory) {
            products.filter { p ->
                val matchesCat = selectedCategory == null || p.categoryId == selectedCategory
                val q = searchQuery.trim().lowercase()
                val matchesQuery = q.isEmpty() ||
                        p.name.lowercase().contains(q) ||
                        p.brand.lowercase().contains(q)
                matchesCat && matchesQuery
            }
        }

        Scaffold(
            topBar = {
                AdminTopAppBar(
                    title = "Product Inventory",
                    subtitle = "${filteredProducts.size} active catalog items",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityEmerald),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add Product", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            },
            bottomBar = {
                AdminBottomBar(
                    currentTab = AdminTab.PRODUCTS,
                    onSelectTab = { tab ->
                        when (tab) {
                            AdminTab.DASHBOARD -> onNavigateToDashboard()
                            AdminTab.ORDERS -> onNavigateToOrders()
                            AdminTab.PAYMENTS -> onNavigateToPayments()
                            AdminTab.CUSTOMERS -> onNavigateToCustomers()
                            AdminTab.PRODUCTS -> {}
                            AdminTab.DELIVERY -> onNavigateToDelivery()
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HighDensitySlate50)
            ) {
                // Search & Category Filter
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val searchFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedContainerColor = HighDensitySlate50,
                            unfocusedContainerColor = HighDensitySlate50
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search product name, brand...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = HighDensitySlate500, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            colors = searchFieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_products_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All (${products.size})", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(categories) { cat ->
                                val count = products.count { it.categoryId == cat.id }
                                FilterChip(
                                    selected = selectedCategory == cat.id,
                                    onClick = { selectedCategory = cat.id },
                                    label = { Text("${cat.iconName} ${cat.name} ($count)", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighDensityIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Inventory List
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No products found in inventory.", fontSize = 12.sp, color = HighDensitySlate500)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            AdminProductItemCard(
                                product = product,
                                onEdit = { productToEdit = product },
                                onDelete = { productToDelete = product },
                                onIncrementStock = {
                                    SudhaniRepository.updateProductStock(product.id, product.stock + 1)
                                },
                                onDecrementStock = {
                                    if (product.stock > 0) {
                                        SudhaniRepository.updateProductStock(product.id, product.stock - 1)
                                    }
                                },
                                onTogglePublish = {
                                    if (product.isActive) {
                                        SudhaniRepository.unpublishProduct(product.id)
                                    } else {
                                        SudhaniRepository.publishProduct(product.id)
                                    }
                                },
                                onToggleVisibility = {
                                    if (product.isVisible) {
                                        SudhaniRepository.hideProduct(product.id)
                                    } else {
                                        SudhaniRepository.showProduct(product.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddProductDialog) {
            AdminProductFormDialog(
                initialProduct = null,
                categories = categories.map { it.id to it.name },
                onDismiss = { showAddProductDialog = false },
                onSave = { newProd ->
                    SudhaniRepository.addProduct(newProd)
                    showAddProductDialog = false
                }
            )
        }

        // Edit Product Dialog
        if (productToEdit != null) {
            AdminProductFormDialog(
                initialProduct = productToEdit,
                categories = categories.map { it.id to it.name },
                onDismiss = { productToEdit = null },
                onSave = { updated ->
                    SudhaniRepository.updateProduct(updated)
                    productToEdit = null
                }
            )
        }

        // Delete Product Confirmation Dialog
        if (productToDelete != null) {
            val prod = productToDelete!!
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Product?", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                text = { Text("Are you sure you want to permanently remove \"${prod.name}\" from the catalog?") },
                confirmButton = {
                    Button(
                        onClick = {
                            SudhaniRepository.deleteProduct(prod.id)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityRed)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIncrementStock: () -> Unit,
    onDecrementStock: () -> Unit,
    onTogglePublish: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySlate200),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_product_${product.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HighDensitySlate100,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = product.emoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = HighDensitySlate900,
                            maxLines = 1
                        )
                        Text(
                            text = "${product.brand} • ${product.unit} • ${product.categoryId}",
                            fontSize = 10.sp,
                            color = HighDensitySlate500
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (product.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (product.isVisible) HighDensityIndigo else HighDensitySlate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = HighDensityIndigo, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HighDensityRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = HighDensitySlate100)
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Status & Stock Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = HighDensitySlate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${product.mrp.toInt()}",
                        fontSize = 11.sp,
                        color = HighDensitySlate400,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(shape = RoundedCornerShape(3.dp), color = HighDensityEmerald.copy(alpha = 0.15f)) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityEmerald,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Status Badge
                val statusColor = when (product.listingStatus) {
                    ListingStatus.ACTIVE -> HighDensityEmerald
                    ListingStatus.OUT_OF_STOCK -> HighDensityRed
                    ListingStatus.HIDDEN -> HighDensityAmber
                    ListingStatus.INACTIVE -> HighDensitySlate500
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = product.listingStatus.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions: Publish / Unpublish and Stock Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Publish/Unpublish Button
                OutlinedButton(
                    onClick = onTogglePublish,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (product.isActive) "Unpublish" else "Publish",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isActive) HighDensitySlate700 else HighDensityEmerald
                    )
                }

                // Stock Controls (+ / -)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stock: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighDensitySlate600
                    )
                    IconButton(
                        onClick = onDecrementStock,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Black, color = HighDensitySlate700)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (product.stock <= 5) HighDensityRed.copy(alpha = 0.15f) else HighDensitySlate100,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "${product.stock}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (product.stock <= 5) HighDensityRed else HighDensitySlate900,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = onIncrementStock,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("+", fontSize = 14.sp, fontWeight = FontWeight.Black, color = HighDensitySlate700)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductFormDialog(
    initialProduct: Product?,
    categories: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "1 unit") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "99") }
    var mrpText by remember { mutableStateOf(initialProduct?.mrp?.toInt()?.toString() ?: "120") }
    var discountText by remember { mutableStateOf(initialProduct?.discountPercent?.toString() ?: "15") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "20") }
    var emoji by remember { mutableStateOf(initialProduct?.emoji ?: "📦") }
    var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
    var selectedCatId by remember { mutableStateOf(initialProduct?.categoryId ?: categories.firstOrNull()?.first ?: "grocery") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        cursorColor = Color.Black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Product" else "Edit Product",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    colors = textFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(0.5f)
                    )
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (optional)") },
                    colors = textFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (₹)") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it },
                        label = { Text("MRP (₹)") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount %") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock") },
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. 500g, 1 kg)") },
                    colors = textFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = (priceText.toDoubleOrNull() ?: 99.0)
                    val m = (mrpText.toDoubleOrNull() ?: 120.0)
                    val disc = (discountText.toIntOrNull() ?: 0)
                    val stk = (stockText.toIntOrNull() ?: 10)

                    val prod = if (initialProduct == null) {
                        Product(
                            id = "prod_${System.currentTimeMillis()}",
                            categoryId = selectedCatId,
                            name = name.ifEmpty { "New Product" },
                            brand = brand.ifEmpty { "Sudhani Farm" },
                            description = "Fresh quality grocery product from SudhaniHub.",
                            unit = unit.ifEmpty { "1 unit" },
                            price = p,
                            mrp = m,
                            discountPercent = disc,
                            stock = stk,
                            rating = 4.8,
                            reviewCount = 1,
                            emoji = emoji.ifEmpty { "📦" },
                            imageUrl = imageUrl.ifEmpty { "" },
                            isActive = false,
                            isVisible = false,
                            listingStatus = ListingStatus.INACTIVE,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        val newListingStatus = if (stk <= 0) {
                            ListingStatus.OUT_OF_STOCK
                        } else if (initialProduct.isActive && initialProduct.isVisible) {
                            ListingStatus.ACTIVE
                        } else if (!initialProduct.isVisible) {
                            ListingStatus.HIDDEN
                        } else {
                            ListingStatus.INACTIVE
                        }
                        initialProduct.copy(
                            categoryId = selectedCatId,
                            name = name.ifEmpty { initialProduct.name },
                            brand = brand.ifEmpty { initialProduct.brand },
                            unit = unit.ifEmpty { initialProduct.unit },
                            price = p,
                            mrp = m,
                            discountPercent = disc,
                            stock = stk,
                            emoji = emoji.ifEmpty { initialProduct.emoji },
                            imageUrl = imageUrl.ifEmpty { initialProduct.imageUrl },
                            listingStatus = newListingStatus,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    onSave(prod)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityIndigo)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
