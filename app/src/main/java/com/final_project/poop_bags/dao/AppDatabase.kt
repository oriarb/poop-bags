package com.final_project.poop_bags.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.models.UserProfile
import com.final_project.poop_bags.dao.users.UserDao
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.dao.posts.PostDao
import com.final_project.poop_bags.dao.posts.PostFavoriteDao
import com.final_project.poop_bags.dao.posts.PostLikeDao
import com.final_project.poop_bags.models.PostLike
import com.final_project.poop_bags.models.PostFavorite

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