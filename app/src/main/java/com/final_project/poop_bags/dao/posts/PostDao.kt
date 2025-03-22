package com.final_project.poop_bags.dao.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.final_project.poop_bags.models.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE isFavorite = 1")
    fun getFavoritePosts(): Flow<List<Post>>
    
    @Query("UPDATE posts SET isFavorite = :isFavorite WHERE postId = :postId")
    suspend fun updateFavoriteStatus(postId: String, isFavorite: Boolean)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)
    
    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: String): Post?
    
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
    
    @Query("SELECT * FROM posts WHERE userId = :userId")
    fun getUserPosts(userId: String): Flow<List<Post>>
    
    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePost(postId: String)
    
    @Query("UPDATE posts SET likesCount = :count WHERE postId = :postId")
    suspend fun updateLikesCount(postId: String, count: Int)

    @Update
    suspend fun updatePost(post: Post)
} 