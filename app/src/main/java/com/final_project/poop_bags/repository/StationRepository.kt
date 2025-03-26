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
import kotlinx.coroutines.flow.first

@Singleton
class StationRepository @Inject constructor(
    private val stationDao: StationDao,
    private val userRepository: UserRepository
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
                userRepository.removeFromFavorites(stationId)
            } else {
                userRepository.addToFavorites(stationId)
            }
        }
    }

    suspend fun addStation(name: String, imageUrl: String, latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val currentUserId = userRepository.getCurrentUserId()
            val newStation = Station(
                id = generateStationId(),
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
            stationDao.deleteStation(stationId)
        }
    }

    suspend fun toggleLike(stationId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = userRepository.getCurrentUserId()
                val station = stationDao.getStationById(stationId)
                
                station?.let {
                    val currentLikes = it.likes.toMutableList()
                    
                    if (userId in currentLikes) {
                        currentLikes.remove(userId)
                    } else {
                        currentLikes.add(userId)
                    }
                    
                    val updatedStation = it.copy(likes = currentLikes)
                    stationDao.updateStation(updatedStation)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to toggle like", e)
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
            val station = stationDao.getStationById(stationId)
            
            station?.let {
                val updatedStation = it.copy(
                    name = name,
                    imageUrl = imageUrl,
                    latitude = latitude,
                    longitude = longitude
                )
                stationDao.updateStation(updatedStation)
            } ?: throw IllegalStateException("Station not found")
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
} 