package com.final_project.poop_bags.dao.posts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.models.PostFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface PostFavoriteDao {
    @Query("SELECT * FROM post_favorites WHERE postId = :postId AND userId = :userId")
    suspend fun getFavorite(postId: String, userId: String): PostFavorite?
    
    @Query("SELECT p.* FROM posts p INNER JOIN post_favorites pf ON p.postId = pf.postId WHERE pf.userId = :userId")
    fun getFavoritePosts(userId: String): Flow<List<Post>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: PostFavorite)
    
    @Delete
    suspend fun deleteFavorite(favorite: PostFavorite)
} 