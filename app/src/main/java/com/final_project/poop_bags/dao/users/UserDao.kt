package com.final_project.poop_bags.dao.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.models.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserProfile(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("UPDATE users SET username = :username, password = :password, image = :image WHERE id = :id")
    suspend fun updateUserProfile(id: String, username: String, password: String, image: String?)

    @Query("UPDATE users SET image = :imageUri WHERE id = :id")
    suspend fun updateProfilePicture(imageUri: String, id: String)
    
    @Query("UPDATE users SET favorites = :favorites WHERE id = :userId")
    suspend fun updateFavorites(userId: String, favorites: List<String>)
} 