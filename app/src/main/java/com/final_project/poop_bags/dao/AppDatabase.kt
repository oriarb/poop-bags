package com.final_project.poop_bags.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.dao.users.UserDao
import com.final_project.poop_bags.models.Post
import com.final_project.poop_bags.dao.posts.PostDao
import com.final_project.poop_bags.dao.posts.PostFavoriteDao
import com.final_project.poop_bags.dao.posts.PostLikeDao
import com.final_project.poop_bags.models.PostLike
import com.final_project.poop_bags.models.PostFavorite
import androidx.room.TypeConverters
import com.final_project.poop_bags.utils.StringListConverter

@Database(
    entities = [
        User::class,
        Post::class,
        PostLike::class,
        PostFavorite::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun postLikeDao(): PostLikeDao
    abstract fun postFavoriteDao(): PostFavoriteDao
}