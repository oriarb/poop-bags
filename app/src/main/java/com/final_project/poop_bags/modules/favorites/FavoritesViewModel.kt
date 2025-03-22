package com.final_project.poop_bags.modules.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    val favoritePosts = repository.getFavoritePosts()
        .catch { e -> 
            if (e.javaClass.simpleName != "CancellationException") {
                _error.postValue("Error loading favorites: ${e.message}")
            }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromFavorites(post: Post) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(post.postId)
            } catch (e: Exception) {
                _error.postValue("Failed to remove from favorites: ${e.message}")
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            try {
                repository.toggleLike(post.postId)
            } catch (e: Exception) {
                _error.postValue("Failed to toggle like: ${e.message}")
            }
        }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = 
        repository.isPostLiked(postId)
            .catch { e ->
                _error.postValue("Error checking if post is liked: ${e.message}")
                emit(false)
            }
} 