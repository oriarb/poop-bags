package com.final_project.poop_bags.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.data.models.Post
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
} 