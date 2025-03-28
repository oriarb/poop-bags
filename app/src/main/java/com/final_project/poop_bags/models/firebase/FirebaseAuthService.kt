package com.final_project.poop_bags.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseAuthService"

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }

    fun getAuth(): FirebaseAuth {
        return auth
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
                firestore.collection("users").document(userId).get()
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
        firestore.collection("users").document(userId).set(user).addOnSuccessListener {
            Log.d(TAG, "addUser: success $user")
        }.addOnFailureListener {
            Log.d(TAG, "addUser: failure")
        }
    }
} 