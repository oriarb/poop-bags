package com.final_project.poop_bags.repository

import android.net.Uri
import com.final_project.poop_bags.models.UserProfile
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
    suspend fun getUserProfile(): UserProfile {
        return userDao.getUserProfile() ?: createDefaultProfile()
    }

    suspend fun updateProfilePicture(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val cachedImagePath = imageCache.cacheImage(uri)
                val currentProfile = getUserProfile()
                userDao.updateUserProfile(
                    userId = currentProfile.userId,
                    username = currentProfile.username,
                    email = currentProfile.email,
                    profilePicture = cachedImagePath
                )
            } catch (e: Exception) {
                throw IllegalStateException("Failed to update profile picture", e)
            }
        }
    }

    suspend fun updateUserProfile(username: String, email: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = getUserProfile()
            userDao.updateUserProfile(
                userId = currentProfile.userId,
                username = username,
                email = email,
                profilePicture = currentProfile.profilePicture
            )
        }
    }

    private suspend fun createDefaultProfile(): UserProfile {
        val defaultProfile = UserProfile(
            userId = "default",
            username = "Guest",
            email = "guest12@gmail.com",
            profilePicture = null,
            favouritesCount = 0,
            postsCount = 0
        )
        userDao.insertUserProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun getCurrentUserId(): String {
        return getUserProfile().userId
    }
}