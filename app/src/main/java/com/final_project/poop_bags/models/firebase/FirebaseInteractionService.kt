package com.final_project.poop_bags.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseInteractionService"

@Singleton
class FirebaseInteractionService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun toggleLike(stationId: String, userId: String): Boolean = suspendCoroutine { continuation ->
        val stationRef = firestore.collection("stations").document(stationId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(stationRef)
            val likes = snapshot.get("likes") as? List<String> ?: listOf()
            
            val updatedLikes = if (likes.contains(userId)) {
                likes - userId
            } else {
                likes + userId
            }
            
            transaction.update(stationRef, "likes", updatedLikes)
            
            !likes.contains(userId)
        }.addOnSuccessListener { wasAdded ->
            Log.d(TAG, "toggleLike: success for station $stationId, user $userId - ${if (wasAdded) "added" else "removed"}")
            continuation.resume(true)
        }.addOnFailureListener { e ->
            Log.e(TAG, "toggleLike: failure for station $stationId", e)
            continuation.resume(false)
        }
    }

    suspend fun addComment(stationId: String, userId: String, text: String): Boolean = suspendCoroutine { continuation ->
        val stationRef = firestore.collection("stations").document(stationId)
        
        val commentId = firestore.collection("comments").document().id
        val newComment = hashMapOf(
            "id" to commentId,
            "userId" to userId,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(stationRef)
            val comments = snapshot.get("comments") as? List<Map<String, Any>> ?: listOf()
            
            val updatedComments = comments + newComment
            
            transaction.update(stationRef, "comments", updatedComments)
        }.addOnSuccessListener {
            Log.d(TAG, "addComment: success for station $stationId")
            continuation.resume(true)
        }.addOnFailureListener { e ->
            Log.e(TAG, "addComment: failure for station $stationId", e)
            continuation.resume(false)
        }
    }
} 