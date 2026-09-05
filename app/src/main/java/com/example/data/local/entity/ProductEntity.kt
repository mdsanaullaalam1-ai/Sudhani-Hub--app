package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val brand: String,
    val description: String,
    val unit: String,
    val price: Double,
    val mrp: Double,
    val discountPercent: Int,
    val stock: Int = 50,
    val rating: Double = 4.8,
    val reviewCount: Int = 142,
    val isPopular: Boolean = false,
    val isDeal: Boolean = false,
    val isRecentlyPurchased: Boolean = false,
    val emoji: String = "🛒"
)
