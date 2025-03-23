package com.final_project.poop_bags.repository

import com.final_project.poop_bags.dao.posts.PostLikeDao
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.dao.posts.PostDao
import com.final_project.poop_bags.models.PostLike
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val postLikeDao: PostLikeDao,
    private val userRepository: UserRepository
) {
    fun getFavoritePosts(): Flow<List<Post>> = flow {
        val favorites = userRepository.getUserFavorites()
        val allPosts = postDao.getAllPosts()
        emit(allPosts.filter { it.postId in favorites })
    }.flowOn(Dispatchers.IO)

    suspend fun toggleFavorite(postId: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = userRepository.getUserProfile()
            val favorites = currentProfile.favorites.toMutableList()
            
            if (postId in favorites) {
                favorites.remove(postId)
            } else {
                favorites.add(postId)
            }
            
            userRepository.updateFavorites(favorites)
        }
    }

    suspend fun addPost(title: String, address: String, imageUrl: String) {
        withContext(Dispatchers.IO) {
            val newPost = Post(
                postId = generatePostId(),
                title = title,
                address = address,
                imageUrl = imageUrl,
                likesCount = 0,
                commentsCount = 0,
                isFavorite = false,
                userId = userRepository.getCurrentUserId()
            )
            postDao.insertPost(newPost)
        }
    }

    private fun generatePostId(): String {
        return "post_${System.currentTimeMillis()}"
    }

    fun getUserPosts(userId: String): Flow<List<Post>> {
        return postDao.getUserPosts(userId)
    }

    suspend fun deletePost(postId: String) {
        withContext(Dispatchers.IO) {
            postDao.deletePost(postId)
        }
    }

    suspend fun toggleLike(postId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = userRepository.getCurrentUserId()
                val existingLike = postLikeDao.getLike(postId, userId)
                
                if (existingLike != null) {
                    postLikeDao.deleteLike(existingLike)
                    val post = postDao.getPostById(postId)
                    post?.let {
                        val newCount = maxOf(0, it.likesCount - 1)
                        postDao.updateLikesCount(postId, newCount)
                    }
                } else {
                    val newLike = PostLike(
                        postId = postId,
                        userId = userId
                    )
                    postLikeDao.insertLike(newLike)
                    val post = postDao.getPostById(postId)
                    post?.let {
                        postDao.updateLikesCount(postId, it.likesCount + 1)
                    }
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to toggle like", e)
            }
        }
    }
    
    fun isPostLiked(postId: String): Flow<Boolean> = flow {
        val userId = userRepository.getCurrentUserId()
        val like = postLikeDao.getLike(postId, userId)
        emit(like != null)
    }.flowOn(Dispatchers.IO)

    suspend fun isPostFavorite(postId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val favorites = userRepository.getUserFavorites()
            postId in favorites
        }
    }

    suspend fun getPostById(postId: String): Post? {
        return withContext(Dispatchers.IO) {
            postDao.getPostById(postId)
        }
    }

    suspend fun updatePost(postId: String, title: String, address: String, imageUrl: String) {
        withContext(Dispatchers.IO) {
            val existingPost = getPostById(postId) ?: throw Exception("Post not found")
            val updatedPost = existingPost.copy(
                title = title,
                address = address,
                imageUrl = imageUrl
            )
            postDao.updatePost(updatedPost)
        }
    }
}