package com.final_project.poop_bags.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.final_project.poop_bags.utils.Converters

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val password: String,
    val image: String?,
    val email: String,
    val favorites: List<String> = emptyList()
)