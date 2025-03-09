package com.final_project.poop_bags.ui.addpost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.ImageCache
import com.final_project.poop_bags.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val imageCache: ImageCache
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var selectedImageUri: Uri? = null
    private var editPostId: String? = null

    fun setEditPost(postId: String) {
        editPostId = postId
        // Load existing post data if needed
    }

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun uploadPost(stationName: String, address: String) {
        if (!validateInput(stationName, address)) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                selectedImageUri?.let { uri ->
                    try {
                        val cachedImagePath = imageCache.cacheImage(uri)
                        postRepository.addPost(
                            title = stationName.trim(),
                            address = address.trim(),
                            imageUrl = cachedImagePath
                        )
                        _uploadSuccess.value = true
                    } catch (e: Exception) {
                        _error.value = "Failed to process image: ${e.message}"
                    }
                } ?: run {
                    _error.value = "Please select an image"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to upload post"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(stationName: String, address: String): Boolean {
        when {
            selectedImageUri == null -> {
                _error.value = "Please select an image"
                return false
            }
            stationName.trim().isEmpty() -> {
                _error.value = "Please enter a station name"
                return false
            }
            address.trim().isEmpty() -> {
                _error.value = "Please enter an address"
                return false
            }
        }
        return true
    }
}