package com.final_project.poop_bags.data.repositories

import com.final_project.poop_bags.data.database.posts.PostLikeDao
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.database.posts.PostDao
import com.final_project.poop_bags.data.models.PostLike
import com.final_project.poop_bags.data.local.dao.PostFavoriteDao
import com.final_project.poop_bags.data.models.PostFavorite
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.emitAll

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val postLikeDao: PostLikeDao,
    private val postFavoriteDao: PostFavoriteDao,
    private val imageCache: ImageCache,
    private val userRepository: UserRepository
) {
    fun getFavoritePosts(): Flow<List<Post>> = flow {
        val userId = userRepository.getCurrentUserId()
        emitAll(postFavoriteDao.getFavoritePosts(userId))
    }.flowOn(Dispatchers.IO)

    suspend fun toggleFavorite(postId: String) {
        withContext(Dispatchers.IO) {
            val userId = userRepository.getCurrentUserId()
            val existingFavorite = postFavoriteDao.getFavorite(postId, userId)

            if (existingFavorite != null) {
                postFavoriteDao.deleteFavorite(existingFavorite)
            } else {
                val newFavorite = PostFavorite(
                    postId = postId,
                    userId = userId
                )
                postFavoriteDao.insertFavorite(newFavorite)
            }
        }
    }

    suspend fun addSamplePosts() {
        withContext(Dispatchers.IO) {
            val currentUser = userRepository.getUserProfile()
            postDao.deleteAllPosts()
            
            val samplePosts = listOf(
                Post(
                    postId = "sample_1",
                    userId = currentUser.userId,
                    title = "הכלב שלי אוהב לשחק בפארק",
                    imageUrl = "https://images.dog.ceo/breeds/retriever-golden/n02099601_1024.jpg",
                    likesCount = 5,
                    commentsCount = 2,
                    isFavorite = false,
                    address = "המרכז"
                ),
                Post(
                    postId = "sample_2",
                    userId = currentUser.userId,
                    title = "טיול בוקר מושלם",
                    imageUrl = "https://images.dog.ceo/breeds/mountain-swiss/n02107574_1346.jpg",
                    likesCount = 3,
                    commentsCount = 1,
                    isFavorite = false,
                    address = "המרכז"
                ),
                Post(
                    postId = "sample_3",
                    userId = currentUser.userId,
                    title = "הכירו את מקס החדש שלי",
                    imageUrl = "https://images.dog.ceo/breeds/husky/n02110185_10047.jpg",
                    likesCount = 8,
                    commentsCount = 4,
                    isFavorite = false,
                    address = "המרכז"
                ),
                Post(
                    postId = "sample_4",
                    userId = currentUser.userId,
                    title = "יום כיף בחוף הים",
                    imageUrl = "https://images.dog.ceo/breeds/retriever-chesapeake/n02099849_1830.jpg",
                    likesCount = 12,
                    commentsCount = 6,
                    isFavorite = false,
                    address = "המרכז"
                )
            )

            samplePosts.forEach { post ->
                postDao.insertPost(post)
            }

            // הוספת לייקים לדוגמה
            postLikeDao.insertLike(PostLike(
                postId = "sample_1",
                userId = currentUser.userId
            ))
            postLikeDao.insertLike(PostLike(
                postId = "sample_3",
                userId = currentUser.userId
            ))

            // הוספת מועדפים לדוגמה
            postFavoriteDao.insertFavorite(PostFavorite(
                postId = "sample_2",
                userId = currentUser.userId
            ))
            postFavoriteDao.insertFavorite(PostFavorite(
                postId = "sample_4",
                userId = currentUser.userId
            ))
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

    suspend fun isPostFavorite(postId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val userId = userRepository.getCurrentUserId()
            postFavoriteDao.getFavorite(postId, userId) != null
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