package com.final_project.poop_bags.modules.stationDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationDetailsViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _station = MutableLiveData<Station?>()
    val station: LiveData<Station?> = _station

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isStationLiked = MutableLiveData<Boolean>()
    val isStationLiked: LiveData<Boolean> = _isStationLiked

    private val _isStationFavourite = MutableLiveData<Boolean>()
    val isStationFavourite: LiveData<Boolean> = _isStationFavourite

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    fun fetchStation(stationId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedStation = stationRepository.getStationById(stationId)
                stationRepository.isStationLiked(stationId).collect {
                    _isStationLiked.value = it
                }
                stationRepository.isStationFavourite(stationId).collect {
                    _isStationFavourite.value = it
                }
                _station.value = fetchedStation
            } catch (e: Exception) {
                _error.value = "Error fetching station: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user: User = userRepository.getUserProfile()
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = "Error fetching user: ${e.message}"
            }
        }
    }

    fun addComment(comment: String) {
        viewModelScope.launch {
            try {
                val stationId = _station.value?.id
                if (stationId != null) {
                    stationRepository.addComment(stationId, comment)
                    val updatedStation = stationRepository.getStationById(stationId)
                    _station.value = updatedStation
                } else {
                    _error.value = "Station ID is null"
                }
            } catch (e: Exception) {
                _error.value = "Error adding comment: ${e.message}"
            }
        }
    }

    fun fetchUser(userId: String, onUserFetched: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val fetchedUser = userRepository.getUserById(userId)
                onUserFetched(fetchedUser)
            } catch (e: Exception) {
                _error.value = "Error fetching user: ${e.message}"
                onUserFetched(null)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun toggleLike(stationId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                stationRepository.toggleLike(stationId)
                stationRepository.isStationLiked(stationId).collect {
                    _isStationLiked.value = it
                }
                val updatedStation = stationRepository.getStationById(stationId)
                _station.value = updatedStation
            } catch (e: Exception) {
                _error.value = "Error toggling like: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            try {
                stationRepository.toggleFavorite(stationId)
                stationRepository.isStationFavourite(stationId).collect {
                    _isStationFavourite.value = it
                }
                val updatedStation = stationRepository.getStationById(stationId)
                _station.value = updatedStation
            } catch (e: Exception) {
                _error.value = "Error toggling favorite: ${e.message}"
            }
        }
    }

}