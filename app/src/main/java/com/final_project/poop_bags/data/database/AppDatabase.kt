package com.final_project.poop_bags.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.local.dao.UserDao

@Database(
    entities = [UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
} 