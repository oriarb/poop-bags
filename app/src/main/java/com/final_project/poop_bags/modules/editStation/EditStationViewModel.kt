package com.final_project.poop_bags.modules.editStation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.repository.ImageCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditStationViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val imageCache: ImageCache
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _station = MutableLiveData<Station>()
    val station: LiveData<Station> = _station

    private var selectedImageUri: Uri? = null

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun loadStation(stationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val loadedStation = stationRepository.getStationById(stationId)
                loadedStation?.let {
                    _station.value = it
                } ?: run {
                    _error.value = "Station not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load station: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStation(stationName: String, updateLocation: Boolean) {
        if (!validateInput(stationName)) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val imageUrl = selectedImageUri?.let { uri ->
                    imageCache.cacheImage(uri)
                } ?: _station.value?.imageUrl

                if (imageUrl == null) {
                    _error.value = "Please select an image"
                    return@launch
                }

                val (latitude, longitude) = if (updateLocation) {
                    getCurrentLocation()
                } else {
                    Pair(_station.value?.latitude ?: 0.0, _station.value?.longitude ?: 0.0)
                }

                _station.value?.id?.let { stationId ->
                    stationRepository.updateStation(
                        stationId = stationId,
                        name = stationName.trim(),
                        imageUrl = imageUrl,
                        latitude = latitude,
                        longitude = longitude
                    )
                    _success.value = "Station updated successfully"
                    _saveSuccess.value = true
                } ?: run {
                    _error.value = "Station ID is missing"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update station"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(stationName: String): Boolean {
        if (stationName.isBlank()) {
            _error.value = "Station name can't be empty"
            return false
        }
        return true
    }

    private fun getCurrentLocation(): Pair<Double, Double> {
        // TODO: להשתמש בפונקציה הקיימת לקבלת המיקום
        return Pair(32.0853, 34.7818)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }
} 