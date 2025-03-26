package com.final_project.poop_bags.modules.location

import android.content.Context
import com.final_project.poop_bags.utils.LocationUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
class LocationUtilModule {

    @Provides
    fun provideLocationUtil(@ApplicationContext context: Context): LocationUtil {
        return LocationUtil(context)
    }
}