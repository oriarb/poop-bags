package com.final_project.poop_bags.modules.firebase

import com.final_project.poop_bags.models.firebase.FirebaseAuthService
import com.final_project.poop_bags.models.firebase.FirebaseUserService
import com.final_project.poop_bags.models.firebase.FirebaseStationService
import com.final_project.poop_bags.models.firebase.FirebaseInteractionService
import com.final_project.poop_bags.models.FirebaseModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {})
        }
        firestore.firestoreSettings = settings
        return firestore
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuthService(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseAuthService {
        return FirebaseAuthService(auth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseUserService(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseUserService {
        return FirebaseUserService(auth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStationService(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseStationService {
        return FirebaseStationService(auth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseInteractionService(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseInteractionService {
        return FirebaseInteractionService(auth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseModel(
        authService: FirebaseAuthService,
        userService: FirebaseUserService,
        stationService: FirebaseStationService,
        interactionService: FirebaseInteractionService
    ): FirebaseModel {
        return FirebaseModel(authService, userService, stationService, interactionService)
    }
}