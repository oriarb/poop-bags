package com.final_project.poop_bags.repository

import android.net.Uri
import com.final_project.poop_bags.models.User
import com.final_project.poop_bags.dao.users.UserDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val imageCache: ImageCache
) {
    suspend fun getUserProfile(): User {
        return userDao.getUserProfile() ?: createDefaultProfile()
    }

    suspend fun updateProfilePicture(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val cachedImagePath = imageCache.cacheImage(uri)
                val currentProfile = getUserProfile()
                userDao.updateUserProfile(
                    id = currentProfile.id,
                    username = currentProfile.username,
                    password = currentProfile.password,
                    image = cachedImagePath
                )
            } catch (e: Exception) {
                throw IllegalStateException("Failed to update profile picture", e)
            }
        }
    }

    suspend fun updateUserProfile(username: String, password: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = getUserProfile()
            userDao.updateUserProfile(
                id = currentProfile.id,
                username = username,
                password = password,
                image = currentProfile.image
            )
        }
    }
    
    suspend fun updateFavorites(favorites: List<String>) {
        withContext(Dispatchers.IO) {
            val currentProfile = getUserProfile()
            userDao.updateFavorites(currentProfile.id, favorites)
        }
    }

    private suspend fun createDefaultProfile(): User {
        val defaultProfile = User(
            id = "default",
            username = "Guest",
            password = "",
            image = null,
            favorites = emptyList(),
            email = ""
        )
        userDao.insertUserProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun getCurrentUserId(): String {
        return getUserProfile().id
    }
    
    suspend fun getUserFavorites(): List<String> {
        return getUserProfile().favorites
    }
}