package com.final_project.poop_bags.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.data.local.dao.UserDao
import com.final_project.poop_bags.data.local.dao.PostDao
import com.final_project.poop_bags.data.local.dao.PostLikeDao
import com.final_project.poop_bags.data.local.dao.PostFavoriteDao
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.models.Post
import com.final_project.poop_bags.data.models.PostLike
import com.final_project.poop_bags.data.models.PostFavorite

@Database(
    entities = [
        UserProfile::class,
        Post::class,
        PostLike::class,
        PostFavorite::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun postLikeDao(): PostLikeDao
    abstract fun postFavoriteDao(): PostFavoriteDao
} 