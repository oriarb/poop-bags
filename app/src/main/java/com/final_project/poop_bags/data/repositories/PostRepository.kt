package com.final_project.poop_bags.data.repositories

import com.final_project.poop_bags.data.database.posts.PostLikeDao
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.database.posts.PostDao
import com.final_project.poop_bags.data.models.PostLike
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
    fun getFavoritePosts(): Flow<List<Post>> {
        return postDao.getFavoritePosts()
    }

    suspend fun toggleFavorite(postId: String) {
        withContext(Dispatchers.IO) {
            val post = postDao.getPostById(postId)
            post?.let {
                postDao.updateFavoriteStatus(postId, !it.isFavorite)
            }
        }
    }

    suspend fun addPost(title: String, imageUrl: String) {
        withContext(Dispatchers.IO) {
            val newPost = Post(
                postId = generatePostId(),
                title = title,
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
            val userId = userRepository.getCurrentUserId()
            val existingLike = postLikeDao.getLike(postId, userId)
            val post = postDao.getPostById(postId)
            
            if (existingLike != null) {
                postLikeDao.deleteLike(existingLike)
                post?.let {
                    postDao.updateLikesCount(postId, it.likesCount - 1)
                }
            } else {
                val newLike = PostLike(
                    postId = postId,
                    userId = userId
                )
                postLikeDao.insertLike(newLike)
                post?.let {
                    postDao.updateLikesCount(postId, it.likesCount + 1)
                }
            }
        }
    }
    
    fun isPostLiked(postId: String): Flow<Boolean> = flow {
        val userId = userRepository.getCurrentUserId()
        val like = postLikeDao.getLike(postId, userId)
        emit(like != null)
    }.flowOn(Dispatchers.IO)
    
    fun getPostLikesCount(postId: String): Flow<Int> {
        return postLikeDao.getLikesCount(postId)
    }
}