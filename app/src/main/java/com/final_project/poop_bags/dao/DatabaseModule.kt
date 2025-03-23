package com.final_project.poop_bags.dao

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.final_project.poop_bags.dao.users.UserDao
import com.final_project.poop_bags.dao.posts.PostDao
import com.final_project.poop_bags.dao.posts.PostLikeDao
import com.final_project.poop_bags.dao.posts.PostFavoriteDao
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // שים לב: גם אם הטבלה לא קיימת, ניצור אותה מחדש
            db.execSQL("DROP TABLE IF EXISTS users")

            db.execSQL("""
                CREATE TABLE users (
                    id TEXT PRIMARY KEY NOT NULL,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    image TEXT,
                    email TEXT NOT NULL DEFAULT '',
                    favorites TEXT NOT NULL DEFAULT '[]'
                )
            """)
            
            // העתקת נתונים מהטבלה הישנה (אם קיימת)
            try {
                db.execSQL("""
                    INSERT INTO users (id, username, password, image, email, favorites)
                    SELECT userId AS id, username, '' AS password, profilePicture AS image, 
                           email, '[]' AS favorites
                    FROM user_profiles
                """)
                
                // מחיקת הטבלה הישנה אם ההעתקה הצליחה
                db.execSQL("DROP TABLE IF EXISTS user_profiles")
            } catch(e: Exception) {
                // אם ההעתקה נכשלה (למשל כי הטבלה הישנה לא קיימת),
                // נוסיף משתמש ברירת מחדל
                db.execSQL("""
                    INSERT INTO users (id, username, password, image, email, favorites)
                    VALUES ('default', 'Guest', '', NULL, '', '[]')
                """)
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