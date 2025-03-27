package com.final_project.poop_bags.modules.addStation

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
class AddStationViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val cloudinaryService: CloudinaryService,
    private val locationUtil: LocationUtil
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _uploadSuccess = MutableLiveData<Boolean>(false)
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _currentStation = MutableLiveData<Station>()
    val currentStation: LiveData<Station> = _currentStation

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
                val station = stationRepository.getStationById(stationId)
                station?.let {
                    _currentStation.value = it
                    _isEditMode.value = true
                }
            } catch (e: Exception) {
                _error.value = "Failed to load station: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveStation(stationName: String) {
        if (!validateInput(stationName)) {
            return
        }
        
        if (selectedImageUri == null) {
            _error.value = "Please provide an image"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val cloudinaryUrl = cloudinaryService.uploadImage(selectedImageUri!!)
                
                if (cloudinaryUrl.isEmpty()) {
                    _error.value = "Failed to upload image. Please try again."
                    _isLoading.value = false
                    return@launch
                }

                val location = if (_isEditMode.value == true) {
                    Location("").apply {
                        latitude = _currentStation.value?.latitude ?: 0.0
                        longitude = _currentStation.value?.longitude ?: 0.0
                    }
                } else {
                    locationUtil.getCurrentLocation()
                }

                if (_isEditMode.value == true) {
                    _currentStation.value?.id?.let { stationId ->
                        stationRepository.updateStation(
                            stationId = stationId,
                            name = stationName.trim(),
                            imageUrl = cloudinaryUrl,
                            latitude = location?.latitude ?: 0.0,
                            longitude = location?.longitude ?: 0.0
                        )
                        _success.value = "Station edited successfully"
                        _uploadSuccess.value = true
                    }
                } else {
                    stationRepository.addStation(
                        name = stationName.trim(),
                        imageUrl = cloudinaryUrl,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0
                    )
                    _success.value = "Station added successfully"
                    _uploadSuccess.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save station"
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