package com.final_project.poop_bags.data.repository

import android.net.Uri
import com.final_project.poop_bags.data.ImageCache
import com.final_project.poop_bags.data.models.UserProfile
import com.final_project.poop_bags.data.local.dao.UserDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val imageCache: ImageCache
) {
    suspend fun getUserProfile(): UserProfile {
        return userDao.getUserProfile() ?: createDefaultProfile()
    }

    suspend fun updateProfilePicture(uri: Uri) {
        try {
            val cachedImagePath = imageCache.cacheImage(uri)
            userDao.updateProfilePicture(cachedImagePath)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to update profile picture", e)
        }
    }

    suspend fun clearImageCache() {
        withContext(Dispatchers.IO) {
            try {
                imageCache.clearCache()
                userDao.updateProfilePicture(null)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to clear image cache", e)
            }
        }
    }

    private suspend fun createDefaultProfile(): UserProfile {
        val defaultProfile = UserProfile(
            userId = "default",
            username = "Guest",
            profilePicture = null
        )
        userDao.insertUserProfile(defaultProfile)
        return defaultProfile
    }
}