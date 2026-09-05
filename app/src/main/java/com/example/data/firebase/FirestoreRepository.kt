package com.example.data.firebase

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val tag = "FirestoreRepository"

    private val firestore: FirebaseFirestore?
        get() = FirebaseConfig.firestore

    // Real-time listener for Orders
    fun listenToOrders(onUpdate: (List<Order>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("orders")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Orders snapshot listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val orderId = doc.getString("orderId") ?: doc.id
                                val customerId = doc.getString("customerId") ?: ""
                                val customerName = doc.getString("customerName") ?: ""
                                val customerPhone = doc.getString("customerPhone") ?: ""
                                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                val subtotal = doc.getDouble("subtotal") ?: 0.0
                                val deliveryFee = doc.getDouble("deliveryFee") ?: 0.0
                                val discount = doc.getDouble("discount") ?: 0.0
                                val totalAmount = doc.getDouble("totalAmount") ?: (subtotal + deliveryFee - discount)
                                val couponCode = doc.getString("couponCode")
                                val paymentMethod = doc.getString("paymentMethod") ?: "COD"
                                val paymentStatus = doc.getString("paymentStatus") ?: "Pending"
                                val orderStatusStr = doc.getString("orderStatus") ?: "NEW"
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                val updatedAt = doc.getLong("updatedAt") ?: createdAt
                                val deliveryPartnerId = doc.getString("deliveryPartnerId")
                                val deliveryPartnerName = doc.getString("deliveryPartnerName") ?: "Ramesh Verma"
                                val deliveryPartnerPhone = doc.getString("deliveryPartnerPhone") ?: "+91 98765 43210"
                                val deliveryPartnerVehicle = doc.getString("deliveryPartnerVehicle") ?: "Electric Scooter"
                                val estimatedDeliveryMinutes = doc.getLong("estimatedDeliveryMinutes")?.toInt() ?: 11
                                val deliveryOtp = doc.getString("deliveryOtp") ?: "4819"
                                 val deliveryArea = doc.getString("deliveryArea")
                                val pincode = doc.getString("pincode")
                                val deliveryDistanceKm = doc.getDouble("deliveryDistanceKm")
                                val totalWeightKg = doc.getDouble("totalWeightKg")
                                val isFreeDelivery = doc.getBoolean("isFreeDelivery") ?: (deliveryFee == 0.0 && subtotal > 0.0)

                                @Suppress("UNCHECKED_CAST")
                                val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                                val items = rawItems.map { map ->
                                    OrderItem(
                                        productId = map["productId"]?.toString() ?: "",
                                        productName = map["productName"]?.toString() ?: "",
                                        productImage = map["productImage"]?.toString() ?: "🛒",
                                        quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                                        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                                        total = (map["total"] as? Number)?.toDouble() ?: 0.0
                                    )
                                }

                                Order(
                                    orderId = orderId,
                                    customerId = customerId,
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    deliveryAddress = deliveryAddress,
                                    items = items,
                                    subtotal = subtotal,
                                    deliveryFee = deliveryFee,
                                    discount = discount,
                                    totalAmount = totalAmount,
                                    couponCode = couponCode,
                                    paymentMethod = paymentMethod,
                                    paymentStatus = paymentStatus,
                                    orderStatus = OrderStatus.fromString(orderStatusStr),
                                    createdAt = createdAt,
                                    updatedAt = updatedAt,
                                    deliveryPartnerId = deliveryPartnerId,
                                    deliveryPartnerName = deliveryPartnerName,
                                    deliveryPartnerPhone = deliveryPartnerPhone,
                                    deliveryPartnerVehicle = deliveryPartnerVehicle,
                                    estimatedDeliveryMinutes = estimatedDeliveryMinutes,
                                    deliveryOtp = deliveryOtp,
                                    deliveryArea = deliveryArea,
                                    pincode = pincode,
                                    deliveryDistanceKm = deliveryDistanceKm,
                                    totalWeightKg = totalWeightKg,
                                    isFreeDelivery = isFreeDelivery
                                )
                            } catch (e: Exception) {
                                Log.w(tag, "Failed to parse order doc ${doc.id}: ${e.message}")
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            onUpdate(list)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Failed to register orders listener: ${e.message}")
            null
        }
    }

    // Real-time listener for Products
    fun listenToProducts(onUpdate: (List<Product>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("products")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Products snapshot listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val products = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("productId") ?: doc.id
                                val categoryId = doc.getString("categoryId") ?: "grocery"
                                val name = doc.getString("name") ?: ""
                                val brand = doc.getString("brand") ?: "SudhaniHub"
                                val description = doc.getString("description") ?: ""
                                val unit = doc.getString("unit") ?: "1 unit"
                                val price = doc.getDouble("sellingPrice") ?: doc.getDouble("price") ?: 0.0
                                val mrp = doc.getDouble("mrp") ?: price
                                val discount = doc.getLong("discount")?.toInt() ?: 0
                                val stock = doc.getLong("stock")?.toInt() ?: 0
                                val rating = doc.getDouble("rating") ?: 4.8
                                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 100
                                val isPopular = doc.getBoolean("isPopular") ?: false
                                val isDeal = doc.getBoolean("isDeal") ?: false
                                val isRecentlyPurchased = doc.getBoolean("isRecentlyPurchased") ?: false
                                val emoji = doc.getString("emoji") ?: "🛒"
                                val imageUrl = doc.getString("imageUrl") ?: ""
                                val isActive = doc.getBoolean("isActive") ?: true
                                val isVisible = doc.getBoolean("isVisible") ?: true
                                val listingStatusStr = doc.getString("listingStatus") ?: if (stock <= 0) "OUT_OF_STOCK" else "ACTIVE"
                                val listingStatus = try { ListingStatus.valueOf(listingStatusStr) } catch (_: Exception) { ListingStatus.ACTIVE }
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                val updatedAt = doc.getLong("updatedAt") ?: createdAt

                                Product(
                                    id = id,
                                    categoryId = categoryId,
                                    name = name,
                                    brand = brand,
                                    description = description,
                                    unit = unit,
                                    price = price,
                                    mrp = mrp,
                                    discountPercent = discount,
                                    stock = stock,
                                    rating = rating,
                                    reviewCount = reviewCount,
                                    isPopular = isPopular,
                                    isDeal = isDeal,
                                    isRecentlyPurchased = isRecentlyPurchased,
                                    emoji = emoji,
                                    imageUrl = imageUrl,
                                    isActive = isActive,
                                    isVisible = isVisible,
                                    listingStatus = listingStatus,
                                    createdAt = createdAt,
                                    updatedAt = updatedAt
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (products.isNotEmpty()) {
                            onUpdate(products)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Failed to register products listener: ${e.message}")
            null
        }
    }

    suspend fun saveOrder(order: Order, payment: Payment) {
        val db = firestore ?: return
        try {
            val orderData = hashMapOf(
                "orderId" to order.orderId,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "deliveryAddress" to order.deliveryAddress,
                "items" to order.items.map {
                    mapOf(
                        "productId" to it.productId,
                        "productName" to it.productName,
                        "productImage" to it.productImage,
                        "quantity" to it.quantity,
                        "price" to it.price,
                        "total" to it.total
                    )
                },
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "discount" to order.discount,
                "couponCode" to order.couponCode,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "orderStatus" to order.orderStatus.name,
                "createdAt" to order.createdAt,
                "updatedAt" to System.currentTimeMillis(),
                "deliveryPartnerId" to order.deliveryPartnerId,
                "deliveryPartnerName" to order.deliveryPartnerName,
                "deliveryPartnerPhone" to order.deliveryPartnerPhone,
                "deliveryPartnerVehicle" to order.deliveryPartnerVehicle,
                "estimatedDeliveryMinutes" to order.estimatedDeliveryMinutes,
                "deliveryOtp" to order.deliveryOtp,
                "deliveryArea" to (order.deliveryArea ?: ""),
                "pincode" to (order.pincode ?: ""),
                "deliveryDistanceKm" to (order.deliveryDistanceKm ?: 0.0),
                "totalWeightKg" to (order.totalWeightKg ?: 1.0),
                "isFreeDelivery" to order.isFreeDelivery
            )

            val paymentData = hashMapOf(
                "paymentId" to payment.paymentId,
                "orderId" to payment.orderId,
                "customerId" to payment.customerId,
                "customerName" to payment.customerName,
                "amount" to payment.amount,
                "paymentMethod" to payment.paymentMethod,
                "paymentStatus" to payment.paymentStatus.name,
                "transactionRef" to payment.transactionRef,
                "createdAt" to payment.createdAt
            )

            db.collection("orders").document(order.orderId).set(orderData).await()
            db.collection("payments").document(payment.paymentId).set(paymentData).await()
            Log.d(tag, "Order ${order.orderId} and Payment ${payment.paymentId} saved to Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore saveOrder error: ${e.message}")
        }
    }

    suspend fun updateOrderDeliveryFee(orderId: String, newDeliveryFee: Double) {
        val db = firestore ?: return
        try {
            val safeFee = newDeliveryFee.coerceAtLeast(0.0)
            val docRef = db.collection("orders").document(orderId)
            val snap = docRef.get().await()
            val subtotal = snap.getDouble("subtotal") ?: 0.0
            val discount = snap.getDouble("discount") ?: 0.0
            val newTotal = (subtotal + safeFee - discount).coerceAtLeast(0.0)

            docRef.update(
                mapOf(
                    "deliveryFee" to safeFee,
                    "isFreeDelivery" to (safeFee == 0.0),
                    "totalAmount" to newTotal,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Log.d(tag, "Order $orderId deliveryFee updated to $safeFee")
        } catch (e: Exception) {
            Log.w(tag, "Firestore updateOrderDeliveryFee error: ${e.message}")
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val db = firestore ?: return
        try {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "orderStatus" to newStatus.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Log.d(tag, "Order $orderId status updated to $newStatus in Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore updateOrderStatus error: ${e.message}")
        }
    }

    suspend fun saveProduct(product: Product) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "productId" to product.id,
                "name" to product.name,
                "brand" to product.brand,
                "description" to product.description,
                "imageUrl" to product.imageUrl,
                "categoryId" to product.categoryId,
                "mrp" to product.mrp,
                "sellingPrice" to product.price,
                "discount" to product.discountPercent,
                "stock" to product.stock,
                "isActive" to product.isActive,
                "isVisible" to product.isVisible,
                "listingStatus" to product.listingStatus.name,
                "emoji" to product.emoji,
                "unit" to product.unit,
                "createdAt" to product.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("products").document(product.id).set(data, SetOptions.merge()).await()
            Log.d(tag, "Product ${product.id} saved to Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore saveProduct error: ${e.message}")
        }
    }

    suspend fun deleteProduct(productId: String) {
        val db = firestore ?: return
        try {
            db.collection("products").document(productId).delete().await()
            Log.d(tag, "Product $productId deleted from Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore deleteProduct error: ${e.message}")
        }
    }

    suspend fun updateProductStock(productId: String, newStock: Int) {
        val db = firestore ?: return
        val safeStock = newStock.coerceAtLeast(0)
        val status = if (safeStock == 0) ListingStatus.OUT_OF_STOCK.name else ListingStatus.ACTIVE.name
        try {
            db.collection("products").document(productId).update(
                mapOf(
                    "stock" to safeStock,
                    "listingStatus" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(tag, "Firestore updateStock error: ${e.message}")
        }
    }

    // Real-time listener for Delivery Areas
    fun listenToDeliveryAreas(onUpdate: (List<DeliveryArea>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("deliveryAreas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "DeliveryAreas snapshot listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val areas = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.id
                                val pincode = doc.getString("pincode") ?: ""
                                val areaName = doc.getString("areaName") ?: ""
                                val city = doc.getString("city") ?: ""
                                val state = doc.getString("state") ?: ""
                                 val deliveryFee = doc.getDouble("deliveryFee") ?: 0.0
                                val minimumOrderAmount = doc.getDouble("minimumOrderAmount") ?: 199.0
                                val estimatedDeliveryTime = doc.getString("estimatedDeliveryTime") ?: "10–20 mins"
                                val isActive = doc.getBoolean("isActive") ?: true
                                val minDistanceKm = doc.getDouble("minDistanceKm") ?: 0.0
                                val maxDistanceKm = doc.getDouble("maxDistanceKm") ?: 25.0
                                val defaultDistanceKm = doc.getDouble("defaultDistanceKm") ?: 4.5
                                val freeDeliveryMinOrderAmount = doc.getDouble("freeDeliveryMinOrderAmount") ?: 999.0
                                val manualOverrideFee = doc.getDouble("manualOverrideFee")
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                val updatedAt = doc.getLong("updatedAt") ?: createdAt

                                DeliveryArea(
                                    id = id,
                                    pincode = pincode,
                                    areaName = areaName,
                                    city = city,
                                    state = state,
                                    deliveryFee = deliveryFee,
                                    minimumOrderAmount = minimumOrderAmount,
                                    estimatedDeliveryTime = estimatedDeliveryTime,
                                    isActive = isActive,
                                    minDistanceKm = minDistanceKm,
                                    maxDistanceKm = maxDistanceKm,
                                    defaultDistanceKm = defaultDistanceKm,
                                    freeDeliveryMinOrderAmount = freeDeliveryMinOrderAmount,
                                    manualOverrideFee = manualOverrideFee,
                                    createdAt = createdAt,
                                    updatedAt = updatedAt
                                )
                            } catch (e: Exception) {
                                Log.w(tag, "Failed to parse deliveryArea doc ${doc.id}: ${e.message}")
                                null
                            }
                        }
                        if (areas.isNotEmpty()) {
                            onUpdate(areas)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Failed to register delivery areas listener: ${e.message}")
            null
        }
    }

    suspend fun saveDeliveryArea(area: DeliveryArea) {
        val db = firestore ?: return
        try {
            val docId = if (area.id.isNotBlank()) area.id else if (area.pincode.isNotBlank()) area.pincode else "area_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "pincode" to area.pincode.trim(),
                "areaName" to area.areaName.trim(),
                "city" to area.city.trim(),
                "state" to area.state.trim(),
                "deliveryFee" to area.deliveryFee,
                "minimumOrderAmount" to area.minimumOrderAmount,
                "estimatedDeliveryTime" to area.estimatedDeliveryTime.trim(),
                "isActive" to area.isActive,
                "minDistanceKm" to area.minDistanceKm,
                "maxDistanceKm" to area.maxDistanceKm,
                "defaultDistanceKm" to area.defaultDistanceKm,
                "freeDeliveryMinOrderAmount" to area.freeDeliveryMinOrderAmount,
                "manualOverrideFee" to (area.manualOverrideFee ?: -1.0),
                "createdAt" to area.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("deliveryAreas").document(docId).set(data, SetOptions.merge()).await()
            Log.d(tag, "DeliveryArea $docId saved to Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore saveDeliveryArea error: ${e.message}")
        }
    }

    suspend fun deleteDeliveryArea(areaId: String) {
        val db = firestore ?: return
        try {
            db.collection("deliveryAreas").document(areaId).delete().await()
            Log.d(tag, "DeliveryArea $areaId deleted from Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore deleteDeliveryArea error: ${e.message}")
        }
    }

    suspend fun updateDeliveryAreaStatus(areaId: String, isActive: Boolean) {
        val db = firestore ?: return
        try {
            db.collection("deliveryAreas").document(areaId).update(
                mapOf(
                    "isActive" to isActive,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Log.d(tag, "DeliveryArea $areaId status updated to $isActive")
        } catch (e: Exception) {
            Log.w(tag, "Firestore updateDeliveryAreaStatus error: ${e.message}")
        }
    }

    suspend fun seedInitialDeliveryAreasIfEmpty() {
        val db = firestore ?: return
        try {
            val snapshot = db.collection("deliveryAreas").limit(1).get().await()
            if (snapshot.isEmpty) {
                val initialAreas = listOf(
                    DeliveryArea(
                        id = "122001",
                        pincode = "122001",
                        areaName = "Gurugram",
                        city = "Gurugram",
                        state = "Haryana",
                        deliveryFee = 0.0,
                        minimumOrderAmount = 199.0,
                        estimatedDeliveryTime = "10–20 mins",
                        isActive = true
                    ),
                    DeliveryArea(
                        id = "560102",
                        pincode = "560102",
                        areaName = "HSR Layout",
                        city = "Bengaluru",
                        state = "Karnataka",
                        deliveryFee = 0.0,
                        minimumOrderAmount = 199.0,
                        estimatedDeliveryTime = "10–15 mins",
                        isActive = true
                    ),
                    DeliveryArea(
                        id = "560038",
                        pincode = "560038",
                        areaName = "Indiranagar",
                        city = "Bengaluru",
                        state = "Karnataka",
                        deliveryFee = 0.0,
                        minimumOrderAmount = 199.0,
                        estimatedDeliveryTime = "10–15 mins",
                        isActive = true
                    ),
                    DeliveryArea(
                        id = "560103",
                        pincode = "560103",
                        areaName = "Bellandur",
                        city = "Bengaluru",
                        state = "Karnataka",
                        deliveryFee = 25.0,
                        minimumOrderAmount = 199.0,
                        estimatedDeliveryTime = "15–25 mins",
                        isActive = true
                    )
                )
                for (area in initialAreas) {
                    saveDeliveryArea(area)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore seedInitialDeliveryAreas error: ${e.message}")
        }
    }
}
