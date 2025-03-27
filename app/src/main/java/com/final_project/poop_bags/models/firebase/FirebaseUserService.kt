package com.final_project.poop_bags.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseUserService"

@Singleton
class FirebaseUserService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserData(userId: String): Map<String, Any>? = suspendCoroutine { continuation ->
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    continuation.resume(document.data)
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    fun updateUserProfile(userId: String, username: String, email: String) {
        val updates = mutableMapOf<String, Any>(
            "username" to username,
            "email" to email
        )
        firestore.collection("users").document(userId).update(updates)
    }

    fun updateUserFavorites(userId: String, favorites: List<String>) {
        val updates = mutableMapOf<String, Any>(
            "favorites" to favorites
        )
        firestore.collection("users").document(userId).update(updates)
    }
    
    suspend fun updateUserProfileImage(userId: String, imageUrl: String): Boolean = suspendCoroutine { continuation ->
        val updates = mutableMapOf<String, Any>(
            "image" to imageUrl
        )
        
        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserProfileImage: success for user $userId")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserProfileImage: failure for user $userId", e)
                continuation.resume(false)
            }
    }
} 