package com.final_project.poop_bags.data.repository

import com.final_project.poop_bags.data.ImageCache
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.local.dao.PostDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val imageCache: ImageCache
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

    suspend fun addSamplePosts() {
        withContext(Dispatchers.IO) {
            postDao.deleteAllPosts()
            
            val samplePosts = listOf(
                Post(
                    postId = "sample_1",
                    title = "דוגמה ראשונה",
                    imageUrl = "https://example.com/sample1.jpg",
                    likesCount = 5,
                    commentsCount = 2,
                    isFavorite = true
                ),
                Post(
                    postId = "sample_2",
                    title = "דוגמה שנייה",
                    imageUrl = "https://example.com/sample2.jpg",
                    likesCount = 3,
                    commentsCount = 1,
                    isFavorite = false
                )
            )
            
            samplePosts.forEach { post ->
                postDao.insertPost(post)
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
                isFavorite = false
            )
            postDao.insertPost(newPost)
        }
    }

    private fun generatePostId(): String {
        return "post_${System.currentTimeMillis()}"
    }
} 