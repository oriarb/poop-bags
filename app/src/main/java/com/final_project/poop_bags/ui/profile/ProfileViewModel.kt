package com.final_project.poop_bags.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _userProfile.value = userRepository.getUserProfile()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                userRepository.updateProfilePicture(uri)
                // רענון הפרופיל אחרי העדכון
                loadUserProfile()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun clearImageCache() {
        viewModelScope.launch {
            try {
                userRepository.clearImageCache()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
