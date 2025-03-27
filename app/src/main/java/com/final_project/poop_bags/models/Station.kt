package com.final_project.poop_bags.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.final_project.poop_bags.utils.Converters
import kotlinx.parcelize.Parcelize

@Entity(tableName = "stations")
@TypeConverters(Converters::class)
@Parcelize
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
): Parcelable

@Parcelize
data class Comment(
    val id: String,
    val userId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable