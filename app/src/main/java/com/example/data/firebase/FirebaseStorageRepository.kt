package com.example.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageRepository {
    private val tag = "FirebaseStorageRepo"

    private val storage: FirebaseStorage?
        get() = FirebaseConfig.storage

    suspend fun uploadProductImage(productId: String, fileUri: Uri): String? {
        val st = storage ?: return null
        return try {
            val ref = st.reference.child("products/$productId.jpg")
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(tag, "Uploaded product image: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.w(tag, "Failed to upload image: ${e.message}")
            null
        }
    }

    suspend fun uploadCategoryIcon(categoryId: String, fileUri: Uri): String? {
        val st = storage ?: return null
        return try {
            val ref = st.reference.child("categories/$categoryId.png")
            ref.putFile(fileUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.w(tag, "Failed to upload category icon: ${e.message}")
            null
        }
    }
}
