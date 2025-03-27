package com.final_project.poop_bags.modules.editStation

import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.repository.StationRepository
import com.final_project.poop_bags.utils.CloudinaryService
import com.final_project.poop_bags.utils.LocationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditStationViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val locationUtil: LocationUtil,
    private val cloudinaryService: CloudinaryService
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
                    cloudinaryService.uploadImage(uri)
                } ?: _station.value?.imageUrl

                if (imageUrl == null || imageUrl.isEmpty()) {
                    _error.value = "Failed to upload image. Please try again."
                    _isLoading.value = false
                    return@launch
                }

                val location = if (updateLocation) {
                    locationUtil.getCurrentLocation().firstOrNull()
                } else {
                    Location("").apply {
                        latitude = _station.value?.latitude ?: 0.0
                        longitude = _station.value?.longitude ?: 0.0
                    }
                }

                _station.value?.id?.let { stationId ->
                    stationRepository.updateStation(
                        stationId = stationId,
                        name = stationName.trim(),
                        imageUrl = imageUrl,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0
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

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }
} 