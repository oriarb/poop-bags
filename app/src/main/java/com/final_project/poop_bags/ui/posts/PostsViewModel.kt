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
                    _userPosts.value = posts
                }
            } catch (e: Exception) {
                // Handle error
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
} 