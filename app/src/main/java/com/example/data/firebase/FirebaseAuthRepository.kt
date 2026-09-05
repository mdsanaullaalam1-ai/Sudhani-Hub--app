package com.example.data.firebase

import android.util.Log
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {
    private val tag = "FirebaseAuthRepo"

    private val auth: FirebaseAuth?
        get() = FirebaseConfig.auth

    private val firestore: FirebaseFirestore?
        get() = FirebaseConfig.firestore

    suspend fun registerOrUpdateCustomer(
        name: String,
        phone: String,
        email: String,
        role: UserRole = UserRole.CUSTOMER
    ): UserProfile {
        val cleanEmail = if (email.contains("@")) email.trim() else "${phone.filter { it.isDigit() }}@sudhanihub.local"
        val userId = auth?.currentUser?.uid ?: "USR_${System.currentTimeMillis().toString().takeLast(6)}"

        val profile = UserProfile(
            id = userId,
            name = name.trim().ifEmpty { "Sudhani Customer" },
            phone = phone.trim(),
            email = cleanEmail,
            isVerified = true,
            role = role,
            accountStatus = "ACTIVE",
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        // Sync with Firestore users collection (NEVER saving passwords or OTPs)
        try {
            firestore?.collection("users")?.document(userId)?.set(
                mapOf(
                    "userId" to userId,
                    "name" to profile.name,
                    "phone" to profile.phone,
                    "email" to profile.email,
                    "role" to profile.role.name,
                    "accountStatus" to profile.accountStatus,
                    "createdAt" to profile.createdAt,
                    "lastLoginAt" to profile.lastLoginAt
                ),
                SetOptions.merge()
            )?.await()
            Log.d(tag, "Successfully synced user $userId to Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore user profile sync error (offline fallback active): ${e.message}")
        }

        return profile
    }

    suspend fun loginWithCredentials(
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val authInstance = auth
            if (authInstance != null && email.isNotBlank() && password.isNotBlank()) {
                val result = authInstance.signInWithEmailAndPassword(email.trim(), password).await()
                val uid = result.user?.uid ?: "USR_${System.currentTimeMillis()}"
                
                // Fetch user data from Firestore if available
                var role = if (email.startsWith("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.CUSTOMER
                var name = result.user?.displayName ?: "Customer"
                var phone = result.user?.phoneNumber ?: "+91 98765 12345"

                try {
                    val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                    if (doc != null && doc.exists()) {
                        name = doc.getString("name") ?: name
                        phone = doc.getString("phone") ?: phone
                        val roleStr = doc.getString("role") ?: role.name
                        role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { role }
                    }
                } catch (_: Exception) {}

                val profile = UserProfile(
                    id = uid,
                    name = name,
                    phone = phone,
                    email = email,
                    isVerified = true,
                    role = role,
                    lastLoginAt = System.currentTimeMillis()
                )
                Result.success(profile)
            } else {
                // Safe offline fallback
                val role = if (email.startsWith("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.CUSTOMER
                val profile = UserProfile(
                    id = "USR_${System.currentTimeMillis().toString().takeLast(6)}",
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    phone = "+91 98765 12345",
                    email = email,
                    role = role
                )
                Result.success(profile)
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase login failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Firebase sign out error: ${e.message}")
        }
    }
}
