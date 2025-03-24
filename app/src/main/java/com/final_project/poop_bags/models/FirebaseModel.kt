package com.final_project.poop_bags.models

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FirebaseModel private constructor() {
    private val database = Firebase.firestore
    private val auth = Firebase.auth

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        database.firestoreSettings = settings
    }

    companion object {
        private val instance = FirebaseModel()

        fun getInstance(): FirebaseModel {
            return instance
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }

    fun registerUser(email: String, username: String, password: String, callback: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            val userId: String = authResult.user?.uid.toString()
            addUser(userId, email, username)
            callback("Registration successful")
        }.addOnFailureListener { exception ->
            Log.d(TAG, "registerUser: failure ${exception.message}")
            callback("Registration failed: ${exception.message}")
        }
    }

    fun signInUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            callback(true)
        }.addOnFailureListener { exception ->
            callback(false)
        }
    }

    private fun addUser(userId: String, email: String, username: String) {
        val user = hashMapOf(
            "email" to email,
            "username" to username,
            "image" to "",
        )
        database.collection("users").document(userId).set(user).addOnSuccessListener {
            Log.d(TAG, "addUser: success ${user}")
        }.addOnFailureListener {
            Log.d(TAG, "addUser: failure")
        }
    }
}