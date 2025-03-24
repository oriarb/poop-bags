package com.final_project.poop_bags.utils

import androidx.room.TypeConverter
import com.final_project.poop_bags.models.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromCommentList(value: List<Comment>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommentList(value: String): List<Comment> {
        val listType = object : TypeToken<List<Comment>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
} 