package com.final_project.poop_bags.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.repository.PostRepository
import com.final_project.poop_bags.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        viewModelScope.launch {
            try {
                val userId = userRepository.getUserProfile().userId
                postRepository.getUserPosts(userId).collect { posts ->
                    val updatedPosts = posts.map { post ->
                        post.copy(isFavorite = postRepository.isPostFavorite(post.postId))
                    }
                    _userPosts.value = updatedPosts
                }
            } catch (e: Exception) {
                _userPosts.value = emptyList()
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.postId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            postRepository.toggleLike(post.postId)
        }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = postRepository.isPostLiked(postId)

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
                // Handle error
            }
        }
    }
}