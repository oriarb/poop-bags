package com.final_project.poop_bags.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.final_project.poop_bags.data.models.UserProfile

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profiles SET profilePicture = :pictureUri")
    suspend fun updateProfilePicture(pictureUri: String?)
} 