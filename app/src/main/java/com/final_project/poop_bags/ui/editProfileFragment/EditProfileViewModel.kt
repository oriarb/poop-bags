package com.final_project.poop_bags.ui.editProfileFragment

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userProfile.value = userRepository.getUserProfile()
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
            _isLoading.value = true
            try {
                userRepository.updateProfilePicture(uri)
                loadUserProfile()
            } catch (e: Exception) {
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
