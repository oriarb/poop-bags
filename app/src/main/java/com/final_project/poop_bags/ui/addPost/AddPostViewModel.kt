package com.final_project.poop_bags.ui.addPost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.repositories.ImageCache
import com.final_project.poop_bags.data.repositories.PostRepository
import com.final_project.poop_bags.data.models.Post
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

    private val _isEditMode = MutableLiveData<Boolean>()
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _currentPost = MutableLiveData<Post>()
    val currentPost: LiveData<Post> = _currentPost

    private var selectedImageUri: Uri? = null

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun setEditPost(postId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                postRepository.getPostById(postId)?.let { post ->
                    _currentPost.value = post
                    _isEditMode.value = true
                }
            } catch (e: Exception) {
                _error.value = "Failed to load post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPost(stationName: String, address: String) {
        if (!validateInput(stationName, address)) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val imageUrl = selectedImageUri?.let { uri ->
                    imageCache.cacheImage(uri)
                } ?: _currentPost.value?.imageUrl

                if (imageUrl == null && _currentPost.value?.imageUrl == null) {
                    _error.value = "Please select an image"
                    return@launch
                }

                if (_isEditMode.value == true) {
                    _currentPost.value?.postId?.let { postId ->
                        postRepository.updatePost(
                            postId = postId,
                            title = stationName.trim(),
                            address = address.trim(),
                            imageUrl = imageUrl ?: _currentPost.value?.imageUrl!!
                        )
                        _uploadSuccess.value = true
                    }
                } else {
                    postRepository.addPost(
                        title = stationName.trim(),
                        address = address.trim(),
                        imageUrl = imageUrl!!
                    )
                    _uploadSuccess.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save post"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(stationName: String, address: String): Boolean {
        when {
            selectedImageUri == null && _currentPost.value?.imageUrl == null -> {
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