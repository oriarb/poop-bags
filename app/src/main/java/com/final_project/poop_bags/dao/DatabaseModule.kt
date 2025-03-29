package com.final_project.poop_bags.dao

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.final_project.poop_bags.dao.station.StationDao
import com.final_project.poop_bags.dao.users.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "poop_bags_db"
        )
        .addMigrations(MIGRATION_6_7)
        .fallbackToDestructiveMigration()
        .build()
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE stations (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    owner TEXT NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    likes TEXT NOT NULL DEFAULT '[]',
                    comments TEXT NOT NULL DEFAULT '[]',
                    isFavorite INTEGER NOT NULL DEFAULT 0
                )
            """)

            try {
                db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='posts'").use { cursor ->
                    if (cursor.count > 0) {
                        db.execSQL("""
                            INSERT INTO stations (id, name, imageUrl, owner, latitude, longitude, isFavorite)
                            SELECT postId, title, imageUrl, userId, 32.0853, 34.7818, isFavorite
                            FROM posts
                        """)
                        
                        db.execSQL("DROP TABLE IF EXISTS posts")
                        db.execSQL("DROP TABLE IF EXISTS post_likes")
                        db.execSQL("DROP TABLE IF EXISTS post_favorites")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideStationDao(database: AppDatabase): StationDao {
        return database.stationDao()
    }
}
