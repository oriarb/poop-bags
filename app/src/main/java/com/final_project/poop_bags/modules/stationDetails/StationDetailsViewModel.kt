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

    fun fetchStation(stationId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fetchedStation = stationRepository.getStationById(stationId)
                stationRepository.isStationLiked(stationId).collect {
                    _isStationLiked.value = it
                }
                _station.value = fetchedStation
            } catch (e: Exception) {
                _error.value = "Error fetching station: ${e.message}"
            } finally {
                _isLoading.value = false
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
}