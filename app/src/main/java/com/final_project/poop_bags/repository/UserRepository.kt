package com.final_project.poop_bags.repository

import com.final_project.poop_bags.dao.users.UserDao
import com.final_project.poop_bags.models.firebase.FirebaseModel
import com.final_project.poop_bags.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseModel: FirebaseModel
) {
    suspend fun getUserProfile(): User {
        return withContext(Dispatchers.IO) {
            userDao.getUserProfile() ?: createDefaultProfile()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            var user: User? = null
            try {
                val userData = firebaseModel.getUserData(userId)
                if (userData != null) {
                    user = User(
                        id = userId,
                        username = userData["username"] as? String ?: "",
                        email = userData["email"] as? String ?: "",
                        password = "", // You might not store the password in Firebase
                        image = userData["image"] as? String ?: "",
                        favorites = (userData["favorites"] as? List<String>) ?: emptyList()
                    )
//                    userDao.insertUserProfile(user)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to fetch user from Firebase", e)
            }

            user
        }
    }

    suspend fun updateProfilePicture(cloudinaryUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val currentProfileId = getCurrentUserId()
                userDao.updateProfilePicture(
                    imageUri = cloudinaryUrl,
                    id = currentProfileId
                )

                firebaseModel.updateProfilePicture(
                    currentProfileId,
                    cloudinaryUrl
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
                id = currentProfile.id,
                username = username,
                password = currentProfile.password,
                image = currentProfile.image
            )
            
            firebaseModel.updateUserProfile(currentProfile.id, username, email)
        }
    }

    private suspend fun createDefaultProfile(): User {
        return withContext(Dispatchers.IO) {
            val defaultProfile = User(
                id = "default",
                username = "Guest",
                password = "",
                email = "",
                image = null,
                favorites = emptyList()
            )
            userDao.insertUserProfile(defaultProfile)
            defaultProfile
        }
    }

    suspend fun getCurrentUserId(): String {
        return withContext(Dispatchers.IO) {
            getUserProfile().id
        }
    }
    
    suspend fun getUserFavorites(): List<String> {
        return withContext(Dispatchers.IO) {
            getUserProfile().favorites
        }
    }

    suspend fun updateUserFromFirebase(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userData = firebaseModel.getUserData(userId)
                userData?.let {
                    val user = User(
                        id = userId,
                        username = it["username"] as? String ?: "",
                        email = it["email"] as? String ?: "",
                        password = "",
                        image = it["image"] as? String ?: "",
                        favorites = (it["favorites"] as? List<String>) ?: emptyList()
                    )
                    userDao.deleteAll()
                    userDao.insertUserProfile(user)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to update user from Firebase", e)
            }
        }
    }

    suspend fun createNewUser(userId: String, username: String, email: String) {
        withContext(Dispatchers.IO) {
            try {
                val newUser = User(
                    id = userId,
                    username = username,
                    email = email,
                    password = "",
                    image = "",
                    favorites = emptyList()
                )
                userDao.deleteAll()
                userDao.insertUserProfile(newUser)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to create new user", e)
            }
        }
    }

    suspend fun addToFavorites(stationId: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = getUserProfile()
            val updatedFavorites = currentProfile.favorites + stationId
            userDao.updateFavorites(currentProfile.id, updatedFavorites)
            firebaseModel.updateUserFavorites(currentProfile.id, updatedFavorites)
        }
    }

    suspend fun removeFromFavorites(stationId: String) {
        withContext(Dispatchers.IO) {
            val currentProfile = getUserProfile()
            val updatedFavorites = currentProfile.favorites - stationId
            userDao.updateFavorites(currentProfile.id, updatedFavorites)
            firebaseModel.updateUserFavorites(currentProfile.id, updatedFavorites)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            firebaseModel.signOut()
            userDao.deleteAll()
        }
    }
}
