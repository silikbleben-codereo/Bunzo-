package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.remote.FirebaseService
import com.example.data.repository.BunzoRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class BunzoApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var preferencesManager: PreferencesManager
        private set
    lateinit var repository: BunzoRepository
        private set
    lateinit var firebaseService: FirebaseService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Safely ensure FirebaseApp is initialized
        initFirebaseSafely()

        database = AppDatabase.getInstance(this)
        preferencesManager = PreferencesManager(this)
        firebaseService = FirebaseService()
        repository = BunzoRepository(
            cartDao = database.cartDao(),
            favoriteDao = database.favoriteDao(),
            preferencesManager = preferencesManager,
            firebaseService = firebaseService
        )
    }

    private fun initFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.bunzo.restaurant.wkqy")
                    .setProjectId("aistudio-bunzo")
                    .setApiKey("AIzaSyFakeKeyForLocalFallbackEmulator123")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("BunzoApplication", "FirebaseApp initialized with default fallback options")
            }
        } catch (e: Exception) {
            Log.w("BunzoApplication", "FirebaseApp initialization: ${e.message}")
        }
    }

    companion object {
        lateinit var instance: BunzoApplication
            private set
    }
}
