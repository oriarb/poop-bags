package com.final_project.poop_bags.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.dao.users.UserDao
import androidx.room.TypeConverters
import com.final_project.poop_bags.dao.station.StationDao
import com.final_project.poop_bags.models.Station
import com.final_project.poop_bags.utils.Converters

@Database(
    entities = [
        User::class,
        Station::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stationDao(): StationDao
}