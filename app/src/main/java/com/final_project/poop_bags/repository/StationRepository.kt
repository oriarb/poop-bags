package com.final_project.poop_bags.repository

import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.dao.station.StationDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import android.util.Log
import com.final_project.poop_bags.models.Comment
import com.final_project.poop_bags.models.firebase.FirebaseModel
import kotlinx.coroutines.flow.first

@Singleton
class StationRepository @Inject constructor(
    private val stationDao: StationDao,
    private val userRepository: UserRepository,
    private val firebaseModel: FirebaseModel
) {

    val allStations: Flow<List<Station>> = stationDao.getAllStations()

    fun getFavoriteStations(): Flow<List<Station>> = flow {
        val favoriteIds = userRepository.getUserFavorites()
        if (favoriteIds.isNotEmpty()) {
            val favoriteStations = stationDao.getStationsByIds(favoriteIds)
            emit(favoriteStations)
        } else {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun toggleFavorite(stationId: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = userRepository.getUserProfile()
            val favorites = currentProfile.favorites.toMutableList()
            
            if (stationId in favorites) {
                stationDao.updateFavoriteStatus(stationId, false)
                userRepository.removeFromFavorites(stationId)
            } else {
                stationDao.updateFavoriteStatus(stationId, true)
                userRepository.addToFavorites(stationId)
            }
        }
    }

    suspend fun addStation(name: String, imageUrl: String, latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                
                val stationId = firebaseModel.addStation(
                    name = name,
                    image = imageUrl,
                    latitude = latitude,
                    longitude = longitude
                )
                
                val finalStationId = stationId.ifBlank { generateStationId() }
                
                val newStation = Station(
                    id = finalStationId,
                    name = name,
                    imageUrl = imageUrl,
                    owner = currentUserId,
                    latitude = latitude,
                    longitude = longitude,
                    likes = emptyList(),
                    comments = emptyList(),
                    isFavorite = false
                )
                stationDao.insertStation(newStation)
                
                finalStationId
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to add station", e)
                throw IllegalStateException("Failed to add station: ${e.message}", e)
            }
        }
    }

    private fun generateStationId(): String {
        return "station_${System.currentTimeMillis()}"
    }

    fun getUserStations(userId: String): Flow<List<Station>> {
        return stationDao.getUserStations(userId)
    }

    suspend fun deleteStation(stationId: String) {
        withContext(Dispatchers.IO) {
            try {
                val success = firebaseModel.deleteStation(stationId)
                Log.d("StationRepository", "Firebase delete result: $success for station $stationId")
                
                stationDao.deleteStation(stationId)
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to delete station", e)
                throw IllegalStateException("Failed to delete station", e)
            }
        }
    }

    suspend fun toggleLike(stationId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = userRepository.getCurrentUserId()
                val station = stationDao.getStationById(stationId)
                
                station?.let {
                    val currentLikes = it.likes.toMutableList()
                    val isLiked = userId in currentLikes
                    
                    if (isLiked) {
                        currentLikes.remove(userId)
                    } else {
                        currentLikes.add(userId)
                    }
                    
                    val updatedStation = it.copy(likes = currentLikes)
                    stationDao.updateStation(updatedStation)
                    
                    val firebaseResult = firebaseModel.toggleLike(stationId, userId)
                    
                    if (firebaseResult) {
                        Log.d("StationRepository", "Successfully toggled like in Firebase for station: $stationId")
                    } else {
                        Log.w("StationRepository", "Firebase like toggle failed, but local DB updated for station: $stationId")
                    }
                } ?: run {
                    Log.e("StationRepository", "Cannot toggle like - station not found: $stationId")
                    throw IllegalStateException("Station not found with ID: $stationId")
                }
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to toggle like", e)
                throw IllegalStateException("Failed to toggle like: ${e.message}", e)
            }
        }
    }
    
    fun isStationLiked(stationId: String): Flow<Boolean> = flow {
        val station = stationDao.getStationById(stationId)
        val userId = userRepository.getCurrentUserId()
        emit(station?.likes?.contains(userId) == true)
    }.flowOn(Dispatchers.IO)

//    suspend fun addComment(stationId: String, text: String) {
//        withContext(Dispatchers.IO) {
//            val userId = userRepository.getCurrentUserId()
//            val station = stationDao.getStationById(stationId)
//
//            station?.let {
//                val newComment = Comment(
//                    id = "comment_${System.currentTimeMillis()}",
//                    userId = userId,
//                    text = text
//                )
//
//                val updatedComments = it.comments + newComment
//                val updatedStation = it.copy(comments = updatedComments)
//                stationDao.updateStation(updatedStation)
//            }
//        }
//    }

    suspend fun getStationById(stationId: String): Station? {
        return withContext(Dispatchers.IO) {
            stationDao.getStationById(stationId)
        }
    }

    suspend fun updateStation(stationId: String, name: String, imageUrl: String, latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            try {
                val currentStation = stationDao.getStationById(stationId)
                
                if (currentStation != null) {
                    val success = firebaseModel.updateStation(
                        stationId = stationId,
                        name = name,
                        image = imageUrl,
                        latitude = latitude,
                        longitude = longitude
                    )
                    
                    Log.d("StationRepository", "Firebase update result: $success for station $stationId")
                    
                    val updatedStation = currentStation.copy(
                        name = name,
                        imageUrl = imageUrl,
                        latitude = latitude,
                        longitude = longitude
                    )
                    stationDao.updateStation(updatedStation)
                } else {
                    throw IllegalStateException("Station not found with ID: $stationId")
                }
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to update station", e)
                throw IllegalStateException("Failed to update station: ${e.message}", e)
            }
        }
    }

    suspend fun getUserFavorites(): List<String> {
        return withContext(Dispatchers.IO) {
            val currentProfile = userRepository.getUserProfile()
            currentProfile.favorites
        }
    }

    suspend fun updateStationFavoriteStatus() {
        withContext(Dispatchers.IO) {
            try {
                val favorites = userRepository.getUserFavorites()
                
                val stationsList = try {
                    stationDao.getAllStations().first()
                } catch (e: Exception) {
                    Log.e("StationRepository", "Failed to get stations, using empty list", e)
                    emptyList()
                }
                
                stationsList.forEach { station ->
                    try {
                        val isFavorite = favorites.contains(station.id)
                        if (station.isFavorite != isFavorite) {
                            stationDao.updateFavoriteStatus(station.id, isFavorite)
                        }
                    } catch (e: Exception) {
                        Log.e("StationRepository", "Failed to update station ${station.id}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to update station favorite status", e)
                throw e
            }
        }
    }

    suspend fun refreshStationsFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                val stations = firebaseModel.getAllStations()
                val userFavorites = userRepository.getUserFavorites()
                
                val localStations = stations.map { stationData ->
                    Station(
                        id = stationData["id"] as String,
                        name = stationData["name"] as String,
                        imageUrl = stationData["image"] as String,
                        owner = stationData["owner"] as String,
                        latitude = stationData["latitude"] as Double,
                        longitude = stationData["longitude"] as Double,
                        likes = (stationData["likes"] as? List<String>) ?: emptyList(),
                        comments = parseComments(stationData["comments"] as? List<Map<String, Any>> ?: emptyList()),
                        isFavorite = userFavorites.contains(stationData["id"] as String)
                    )
                }
                
                stationDao.deleteAllStations()
                stationDao.insertStations(localStations)
                
                Log.d("StationRepository", "Refreshed ${localStations.size} stations from Firebase")
            } catch (e: Exception) {
                Log.e("StationRepository", "Failed to refresh stations from Firebase", e)
                throw IllegalStateException("Failed to refresh stations: ${e.message}", e)
            }
        }
    }

    private fun parseComments(commentsMaps: List<Map<String, Any>>): List<Comment> {
        return commentsMaps.map { commentMap ->
            Comment(
                id = commentMap["id"] as String,
                userId = commentMap["userId"] as String,
                text = commentMap["text"] as String,
                timestamp = commentMap["timestamp"] as Long
            )
        }
    }
} 