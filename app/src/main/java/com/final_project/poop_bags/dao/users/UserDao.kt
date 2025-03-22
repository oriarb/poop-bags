package com.final_project.poop_bags.dao.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.models.UserProfile

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profiles SET username = :username, email = :email, profilePicture = :profilePicture WHERE userId = :userId")
    suspend fun updateUserProfile(userId: String, username: String, email: String, profilePicture: String?)

    @Query("UPDATE user_profiles SET profilePicture = :pictureUri")
    suspend fun updateProfilePicture(pictureUri: String?)
} 