package com.final_project.poop_bags.utils

import android.app.PendingIntent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CloudinaryService @Inject constructor() {
    
    suspend fun uploadImage(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE 
            } else {
                0 
            }
            
            val options = hashMapOf<String, Any>(
                "pending_intent_flags" to pendingIntentFlags
            )
            
            val requestId = MediaManager.get().upload(imageUri)
                .unsigned("mobile_upload")
                .options(options)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CloudinaryService", "Start uploading to Cloudinary")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100)
                        Log.d("CloudinaryService", "Upload progress: $progress%")
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val originalUrl = resultData["url"] as String
                        val httpsUrl = if (originalUrl.startsWith("http://")) {
                            originalUrl.replace("http://", "https://")
                        } else {
                            originalUrl
                        }
                        Log.d("CloudinaryService", "Upload success, URL: $httpsUrl")
                        continuation.resume(httpsUrl)
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CloudinaryService", "Upload error: ${error.description}")
                        continuation.resume("")
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.e("CloudinaryService", "Upload reschedule: ${error.description}")
                    }
                })
                .dispatch()
                
            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
            
        } catch (e: Exception) {
            Log.e("CloudinaryService", "Error uploading image", e)
            continuation.resume("")
        }
    }
} 