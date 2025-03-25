package com.final_project.poop_bags.models

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseModel @Inject constructor() {
    private val database = Firebase.firestore
    private val auth = Firebase.auth

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }

    fun registerUser(email: String, username: String, password: String, callback: (String, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            val userId: String = authResult.user?.uid.toString()
            addUser(userId, email, username)
            callback("Registration successful", userId)
        }.addOnFailureListener { exception ->
            callback("Registration failed: ${exception.message}", null)
        }
    }

    fun signInUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            val userId = authResult.user?.uid
            if (userId != null) {
                database.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            callback(true, userId)
                        } else {
                            callback(false, null)
                        }
                    }
                    .addOnFailureListener {
                        callback(false, null)
                    }
            } else {
                callback(false, null)
            }
        }.addOnFailureListener {
            callback(false, null)
        }
    }

    private fun addUser(userId: String, email: String, username: String) {
        val user = mutableMapOf<String, Any>(
            "email" to email,
            "username" to username,
            "image" to "",
            "favorites" to listOf<String>()
        )
        database.collection("users").document(userId).set(user).addOnSuccessListener {
            Log.d(TAG, "addUser: success $user")
        }.addOnFailureListener {
            Log.d(TAG, "addUser: failure")
        }
    }

    suspend fun getUserData(userId: String): Map<String, Any>? = suspendCoroutine { continuation ->
        database.collection("users").document(userId).get()
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
        database.collection("users").document(userId).update(updates)
    }

    fun updateUserFavorites(userId: String, favorites: List<String>) {
        val updates = mutableMapOf<String, Any>(
            "favorites" to favorites
        )
        database.collection("users").document(userId).update(updates)
    }
}