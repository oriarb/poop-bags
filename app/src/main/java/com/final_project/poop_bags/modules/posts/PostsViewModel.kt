package com.final_project.poop_bags.modules.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.repository.PostRepository
import com.final_project.poop_bags.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadUserPosts()
    }
    
    private fun loadUserPosts() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getUserProfile().id
                postRepository.getUserPosts(userId)
                    .catch { e ->
                        _error.postValue("Error loading posts: ${e.message}")
                        _userPosts.value = emptyList()
                    }
                    .collect { posts ->
                        try {
                            val updatedPosts = posts.map { post ->
                                try {
                                    post.copy(isFavorite = postRepository.isPostFavorite(post.postId))
                                } catch (e: Exception) {
                                    _error.postValue("Error checking favorite status: ${e.message}")
                                    post
                                }
                            }
                            _userPosts.value = updatedPosts
                        } catch (e: Exception) {
                            _error.postValue("Error processing posts: ${e.message}")
                            _userPosts.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                _error.postValue("Error loading user profile: ${e.message}")
                _userPosts.value = emptyList()
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.postId)
            } catch (e: Exception) {
                _error.postValue("Error deleting post: ${e.message}")
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.toggleLike(post.postId)
            } catch (e: Exception) {
                _error.postValue("Error toggling like: ${e.message}")
            }
        }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = 
        postRepository.isPostLiked(postId)
            .catch { e ->
                _error.postValue("Error checking if post is liked: ${e.message}")
                emit(false)
            }

    fun toggleFavorite(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.toggleFavorite(post.postId)
                val updatedPosts = _userPosts.value.map { 
                    if (it.postId == post.postId) {
                        it.copy(isFavorite = !it.isFavorite)
                    } else {
                        it
                    }
                }
                _userPosts.value = updatedPosts
            } catch (e: Exception) {
                _error.postValue("Error toggling favorite: ${e.message}")
            }
        }
    }
}