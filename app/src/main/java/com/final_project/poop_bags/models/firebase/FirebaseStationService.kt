package com.final_project.poop_bags.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseStationService"

@Singleton
class FirebaseStationService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun addStation(name: String, image: String, latitude: Double, longitude: Double): String = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid ?: ""
        val stationId = firestore.collection("stations").document().id
        
        val station = hashMapOf(
            "id" to stationId,
            "name" to name,
            "image" to image,
            "owner" to userId,
            "latitude" to latitude,
            "longitude" to longitude,
            "likes" to listOf<String>(),
            "comments" to listOf<Map<String, Any>>()
        )
        
        firestore.collection("stations").document(stationId).set(station)
            .addOnSuccessListener {
                Log.d(TAG, "addStation: success $station")
                continuation.resume(stationId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addStation: failure", e)
                continuation.resume("")
            }
    }

    suspend fun updateStation(stationId: String, name: String, image: String, latitude: Double, longitude: Double): Boolean = suspendCoroutine { continuation ->
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "image" to image,
            "latitude" to latitude,
            "longitude" to longitude
        )
        
        firestore.collection("stations").document(stationId).update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "updateStation: success for station $stationId")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateStation: failure for station $stationId", e)
                continuation.resume(false)
            }
    }

    suspend fun deleteStation(stationId: String): Boolean = suspendCoroutine { continuation ->
        firestore.collection("stations").document(stationId).delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteStation: success for station $stationId")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteStation: failure for station $stationId", e)
                continuation.resume(false)
            }
    }

    suspend fun getAllStations(): List<Map<String, Any>> = suspendCoroutine { continuation ->
        firestore.collection("stations").get()
            .addOnSuccessListener { documents ->
                val stationsList = mutableListOf<Map<String, Any>>()
                for (document in documents) {
                    document.data?.let { 
                        stationsList.add(it)
                    }
                }
                Log.d(TAG, "getAllStations: success, found ${stationsList.size} stations")
                continuation.resume(stationsList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "getAllStations: failure", e)
                continuation.resume(emptyList())
            }
    }
}