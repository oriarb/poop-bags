package com.final_project.poop_bags.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val username: String,
    val profilePicture: String?,
    val favouritesCount: Int = 0,
    val postsCount: Int = 0,
    val email: String

)