package com.final_project.poop_bags.modules.stations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _userStations = MutableStateFlow<List<Station>>(emptyList())

    private val _stations = MutableLiveData<List<Station>>(emptyList())
    val stations: LiveData<List<Station>> = _stations

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    fun loadUserStations(userId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val actualUserId = userId ?: userRepository.getCurrentUserId()
                
                stationRepository.getUserStations(actualUserId)
                    .catch { e ->
                        _error.value = "Error loading stations: ${e.message}"
                        emit(emptyList())
                    }
                    .collect { stationsList ->
                        val favorites = stationRepository.getUserFavorites()
                        val updatedStations = stationsList.map { station ->
                            station.copy(isFavorite = station.id in favorites)
                        }
                        _stations.value = updatedStations
                        _userStations.value = updatedStations
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load stations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStation(station: Station) {
        viewModelScope.launch {
            try {
                stationRepository.deleteStation(station.id)
                
                val updatedStations = _userStations.value.toMutableList()
                updatedStations.removeIf { it.id == station.id }
                _userStations.value = updatedStations
                _stations.value = updatedStations
                
                _success.value = "station deleted successfully"
                
                android.util.Log.d("StationsViewModel", "Deleted station: ${station.id}")
            } catch (e: Exception) {
                _error.value = "Error deleting station: ${e.message}"
                android.util.Log.e("StationsViewModel", "Failed to delete station", e)
            }
        }
    }

    fun toggleLike(station: Station) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                val updatedStations = _userStations.value.map { currentStation ->
                    if (currentStation.id == station.id) {
                        val newLikes = if (currentStation.likes.contains(currentUserId)) {
                            currentStation.likes - currentUserId
                        } else {
                            currentStation.likes + currentUserId
                        }
                        currentStation.copy(likes = newLikes)
                    } else {
                        currentStation
                    }
                }
                
                _userStations.value = updatedStations
                _stations.value = updatedStations.toList()
                
                stationRepository.toggleLike(station.id)
            } catch (e: Exception) {
                _error.value = "Error toggling like: ${e.message}"
                android.util.Log.e("StationsViewModel", "Failed to toggle like", e)
            }
        }
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            try {
                stationRepository.toggleFavorite(station.id)
                
                val updatedStations = _userStations.value.map { 
                    if (it.id == station.id) {
                        it.copy(isFavorite = !it.isFavorite)
                    } else {
                        it
                    }
                }
                _userStations.value = updatedStations
                _stations.value = updatedStations.toList()
                
                _success.value = "the station favorites status changed successfully"
                
                android.util.Log.d("StationsViewModel", "Toggled favorite for station: ${station.id}")
            } catch (e: Exception) {
                _error.value = "Error toggling favorite: ${e.message}"
                android.util.Log.e("StationsViewModel", "Failed to toggle favorite", e)
            }
        }
    }

    fun isStationLiked(stationId: String): Flow<Boolean> = 
        stationRepository.isStationLiked(stationId)
            .catch { e ->
                _error.value = "Error checking if station is liked: ${e.message}"
                android.util.Log.e("StationsViewModel", "Error checking if station is liked", e)
                emit(false)
            }
    
    fun refreshStations() {
        loadUserStations()
    }
    
    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }
} 