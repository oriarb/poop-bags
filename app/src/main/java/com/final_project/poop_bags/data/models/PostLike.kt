package com.final_project.poop_bags.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_likes",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId")]
)
data class PostLike(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)