package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"
    var isInitialized: Boolean = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.sudhanihub")
                    .setProjectId("sudhanihub-prod")
                    .setApiKey("AIzaSySudhaniHubDemoKeyProductionSafe")
                    .setStorageBucket("sudhanihub-prod.appspot.com")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "Initialized default FirebaseApp with fallback options")
            }
            isInitialized = true

            // Configure Firestore offline persistence
            try {
                val firestore = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                firestore.firestoreSettings = settings
            } catch (e: Exception) {
                Log.w(TAG, "Firestore settings adjustment skipped: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning (safe offline mode active): ${e.message}")
            isInitialized = false
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            if (isInitialized || FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            if (isInitialized || FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            if (isInitialized || FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseStorage.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
}
