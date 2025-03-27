package com.final_project.poop_bags.modules.editProfile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.repository.UserRepository
import com.final_project.poop_bags.utils.CloudinaryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ImageUploadStatus {
    data object Loading : ImageUploadStatus()
    data object Success : ImageUploadStatus()
    data class Error(val message: String) : ImageUploadStatus()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
    ) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _imageUploadStatus = MutableLiveData<ImageUploadStatus>()
    val imageUploadStatus: LiveData<ImageUploadStatus> = _imageUploadStatus

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = userRepository.getUserProfile()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(username: String, email: String) {
        if (!validateInputs(username, email)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.updateUserProfile(username, email)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update profile"
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _imageUploadStatus.value = ImageUploadStatus.Loading
            _isLoading.value = true
            
            try {
                val cloudinaryUrl = cloudinaryService.uploadImage(uri)
                
                if (cloudinaryUrl.isEmpty()) {
                    _imageUploadStatus.value = ImageUploadStatus.Error("Failed to upload image. Please try again.")
                    _error.value = "Failed to upload image. Please try again."
                    _isLoading.value = false
                    return@launch
                }
                
                userRepository.updateProfilePicture(cloudinaryUrl)
                
                _imageUploadStatus.value = ImageUploadStatus.Success
                
                loadUserProfile()
            } catch (e: Exception) {
                _imageUploadStatus.value = ImageUploadStatus.Error(
                    e.message ?: "Failed to update profile picture"
                )
                _error.value = e.message ?: "Failed to update profile picture"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInputs(username: String, email: String): Boolean {
        if (username.isBlank()) {
            _error.value = "Username cannot be empty"
            return false
        }

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!email.matches(emailPattern.toRegex())) {
            _error.value = "Invalid email format"
            return false
        }

        return true
    }
}
