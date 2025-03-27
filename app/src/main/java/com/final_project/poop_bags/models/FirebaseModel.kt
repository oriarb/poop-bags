package com.final_project.poop_bags.models

import com.final_project.poop_bags.models.firebase.FirebaseAuthService
import com.final_project.poop_bags.models.firebase.FirebaseUserService
import com.final_project.poop_bags.models.firebase.FirebaseStationService
import com.final_project.poop_bags.models.firebase.FirebaseInteractionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val userService: FirebaseUserService,
    private val stationService: FirebaseStationService,
    private val interactionService: FirebaseInteractionService
) {
    fun isLoggedIn() = authService.isLoggedIn()
    fun signOut() = authService.signOut()
    fun getAuth() = authService.getAuth()
    fun registerUser(email: String, username: String, password: String, callback: (String, String?) -> Unit) = 
        authService.registerUser(email, username, password, callback)
    fun signInUser(email: String, password: String, callback: (Boolean, String?) -> Unit) = 
        authService.signInUser(email, password, callback)
    
    suspend fun getUserData(userId: String) = userService.getUserData(userId)
    fun updateUserProfile(userId: String, username: String, email: String) = 
        userService.updateUserProfile(userId, username, email)
    fun updateUserFavorites(userId: String, favorites: List<String>) = 
        userService.updateUserFavorites(userId, favorites)
    suspend fun updateProfilePicture(userId: String, imageUrl: String) = userService.updateUserProfileImage(userId, imageUrl)
    
    suspend fun addStation(name: String, image: String, latitude: Double, longitude: Double) = 
        stationService.addStation(name, image, latitude, longitude)
    suspend fun updateStation(stationId: String, name: String, image: String, latitude: Double, longitude: Double) = 
        stationService.updateStation(stationId, name, image, latitude, longitude)
    suspend fun deleteStation(stationId: String) = stationService.deleteStation(stationId)
    suspend fun getAllStations() = stationService.getAllStations()

    suspend fun toggleLike(stationId: String, userId: String) = 
        interactionService.toggleLike(stationId, userId)
    suspend fun addComment(stationId: String, userId: String, text: String) = 
        interactionService.addComment(stationId, userId, text)
}