package com.final_project.poop_bags.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.final_project.poop_bags.utils.Converters

@Entity(tableName = "stations")
@TypeConverters(Converters::class)
data class Station(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String,
    val owner: String,
    val latitude: Double,
    val longitude: Double,
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    var isFavorite: Boolean = false
)

data class Comment(
    val id: String,
    val userId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) 