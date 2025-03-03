package com.final_project.poop_bags.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
 
    init {
        viewModelScope.launch {
            repository.addSamplePosts()
        }
    }

    val favoritePosts = repository.getFavoritePosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromFavorites(post: Post) {
        viewModelScope.launch {
            repository.toggleFavorite(post.postId)
        }
    }
} 