package com.example.data.repository

import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.ProductEntity
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    suspend fun updateStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    suspend fun getProductCount(): Int = productDao.getProductCount()

    suspend fun insertProducts(products: List<Product>) {
        productDao.insertProducts(products.map { it.toEntity() })
    }

    private fun ProductEntity.toModel(): Product {
        return Product(
            id = id,
            categoryId = categoryId,
            name = name,
            brand = brand,
            description = description,
            unit = unit,
            price = price,
            mrp = mrp,
            discountPercent = discountPercent,
            stock = stock,
            rating = rating,
            reviewCount = reviewCount,
            isPopular = isPopular,
            isDeal = isDeal,
            isRecentlyPurchased = isRecentlyPurchased,
            emoji = emoji
        )
    }

    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            categoryId = categoryId,
            name = name,
            brand = brand,
            description = description,
            unit = unit,
            price = price,
            mrp = mrp,
            discountPercent = discountPercent,
            stock = stock,
            rating = rating,
            reviewCount = reviewCount,
            isPopular = isPopular,
            isDeal = isDeal,
            isRecentlyPurchased = isRecentlyPurchased,
            emoji = emoji
        )
    }
}
