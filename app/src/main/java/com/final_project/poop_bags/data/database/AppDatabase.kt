package com.final_project.poop_bags.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.local.dao.UserDao
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.local.dao.PostDao

@Database(
    entities = [UserProfile::class, Post::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
} 