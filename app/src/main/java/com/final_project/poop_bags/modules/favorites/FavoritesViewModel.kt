package com.final_project.poop_bags.modules.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: StationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val _favoriteStations = MutableStateFlow<List<Station>>(emptyList())
    val favoriteStations = _favoriteStations.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteStations()
                .catch { e -> 
                    if (e.javaClass.simpleName != "CancellationException") {
                        _error.postValue("Error loading favorites: ${e.message}")
                    }
                    emit(emptyList())
                }
                .collect { stations ->
                    _favoriteStations.value = stations
                }
        }
    }

    fun removeFromFavorites(station: Station) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(station.id)
                
                val updatedStations = _favoriteStations.value.toMutableList()
                val stationIndex = updatedStations.indexOfFirst { it.id == station.id }
                
                if (stationIndex >= 0) {
                    updatedStations.removeAt(stationIndex)
                    _favoriteStations.value = updatedStations
                }
                
            } catch (e: Exception) {
                _error.postValue("Failed to remove from favorites: ${e.message}")
            }
        }
    }

    fun toggleLike(station: Station) {
        viewModelScope.launch {
            try {
                repository.toggleLike(station.id)
                
                val updatedStations = _favoriteStations.value.toMutableList()
                val index = updatedStations.indexOfFirst { it.id == station.id }
                
                if (index >= 0) {
                    val currentStation = updatedStations[index]
                    val isLiked = repository.isStationLiked(station.id).first()
                    val userId = userRepository.getCurrentUserId()
                    
                    val updatedLikes = if (isLiked) {
                        currentStation.likes + userId
                    } else {
                        currentStation.likes.filter { it != userId }
                    }
                    
                    updatedStations[index] = currentStation.copy(likes = updatedLikes)
                    _favoriteStations.value = updatedStations
                }
            } catch (e: Exception) {
                _error.postValue("Failed to toggle like: ${e.message}")
            }
        }
    }

    fun isStationLiked(stationId: String): Flow<Boolean> = 
        repository.isStationLiked(stationId)
            .catch { e ->
                _error.postValue("Error checking if station is liked: ${e.message}")
                emit(false)
            }
} 