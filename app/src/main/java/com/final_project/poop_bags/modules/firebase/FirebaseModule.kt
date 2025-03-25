package com.final_project.poop_bags.modules.firebase

import com.final_project.poop_bags.models.FirebaseModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseModel(): FirebaseModel {
        return FirebaseModel()
    }
}