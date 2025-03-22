package com.final_project.poop_bags.dao.posts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.models.PostLike
import kotlinx.coroutines.flow.Flow

@Dao
interface PostLikeDao {
    @Query("SELECT * FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun getLike(postId: String, userId: String): PostLike?
    
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    fun getLikesCount(postId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: PostLike)
    
    @Delete
    suspend fun deleteLike(like: PostLike)
} 