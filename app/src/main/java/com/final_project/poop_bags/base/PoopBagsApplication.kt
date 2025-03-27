package com.final_project.poop_bags.base

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

@HiltAndroidApp
class PoopBagsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }
    
    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "db8gx0nyb"
            config["api_key"] = "889655264682532"
            config["api_secret"] = "XDNd8wISZU9hZqHfNfgI0dPc9PI"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                config["android_pending_intent_flags"] = android.app.PendingIntent.FLAG_IMMUTABLE.toString()
            }
            
            MediaManager.init(this, config)
            Log.d("PoopBagsApplication", "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e("PoopBagsApplication", "Error initializing Cloudinary", e)
        }
    }
} 