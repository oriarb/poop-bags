package com.final_project.poop_bags.modules.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val _favoritePosts = MutableStateFlow<List<Post>>(emptyList())
    val favoritePosts = _favoritePosts.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoritePosts()
                .catch { e -> 
                    if (e.javaClass.simpleName != "CancellationException") {
                        _error.postValue("Error loading favorites: ${e.message}")
                    }
                    emit(emptyList())
                }
                .collect { posts ->
                    _favoritePosts.value = posts
                }
        }
    }

    fun removeFromFavorites(post: Post) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(post.postId)
                
                val updatedPosts = _favoritePosts.value.toMutableList()
                val postIndex = updatedPosts.indexOfFirst { it.postId == post.postId }
                
                if (postIndex >= 0) {
                    updatedPosts.removeAt(postIndex)
                    _favoritePosts.value = updatedPosts
                }
                
                android.util.Log.d("FavoritesViewModel", "Toggled favorite for post: ${post.postId} and updated UI")
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to toggle favorite", e)
                _error.postValue("Failed to remove from favorites: ${e.message}")
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            try {
                repository.toggleLike(post.postId)
                
                val updatedPosts = _favoritePosts.value.toMutableList()
                val index = updatedPosts.indexOfFirst { it.postId == post.postId }
                
                if (index >= 0) {
                    val currentPost = updatedPosts[index]
                    val isLiked = repository.isPostLiked(post.postId).first()
                    
                    val newLikesCount = if (isLiked) currentPost.likesCount + 1 else maxOf(0, currentPost.likesCount - 1)
                    updatedPosts[index] = currentPost.copy(likesCount = newLikesCount)
                    _favoritePosts.value = updatedPosts
                    
                    android.util.Log.d("FavoritesViewModel", 
                        "Updated post in UI. ID: ${post.postId}, New likes: $newLikesCount, isLiked: $isLiked")
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to toggle like", e)
                _error.postValue("Failed to toggle like: ${e.message}")
            }
        }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = 
        repository.isPostLiked(postId)
            .catch { e ->
                android.util.Log.e("FavoritesViewModel", "Error checking if post is liked", e)
                _error.postValue("Error checking if post is liked: ${e.message}")
                emit(false)
            }
} 