package com.final_project.poop_bags.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.final_project.poop_bags.data.database.users.UserDao
import com.final_project.poop_bags.data.database.posts.PostDao
import com.final_project.poop_bags.data.database.posts.PostLikeDao
import com.final_project.poop_bags.data.database.posts.PostFavoriteDao
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_profiles ADD COLUMN email TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS posts")
            
            db.execSQL("""
                CREATE TABLE posts (
                    postId TEXT PRIMARY KEY NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    likesCount INTEGER NOT NULL DEFAULT 0,
                    commentsCount INTEGER NOT NULL DEFAULT 0,
                    isFavorite INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Drop existing table if exists
            db.execSQL("DROP TABLE IF EXISTS post_likes")
            
            // Create new post_likes table
            db.execSQL("""
                CREATE TABLE post_likes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    postId TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY(postId) REFERENCES posts(postId) ON DELETE CASCADE
                )
            """)
            
            // Create index on postId
            db.execSQL("CREATE INDEX index_post_likes_postId ON post_likes(postId)")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS post_favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    postId TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY(postId) REFERENCES posts(postId) ON DELETE CASCADE
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_post_favorites_postId ON post_favorites(postId)")
        }
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun providePostLikeDao(database: AppDatabase): PostLikeDao {
        return database.postLikeDao()
    }

    @Provides
    @Singleton
    fun providePostFavoriteDao(database: AppDatabase): PostFavoriteDao {
        return database.postFavoriteDao()
    }
}