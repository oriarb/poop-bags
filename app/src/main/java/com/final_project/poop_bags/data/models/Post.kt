package com.final_project.poop_bags.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val postId: String,
    val userId: String,
    val title: String,
    val address: String,
    val imageUrl: String,
    var likesCount: Int = 0,
    val commentsCount: Int = 0,
    var isFavorite: Boolean = false
) 