package com.final_project.poop_bags.data

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun cacheImage(uri: Uri): String {
        return try {
            val future = Glide.with(context)
                .asFile()
                .load(uri)
                .submit()

            val cachedFile = future.get()
            cachedFile.absolutePath
        } catch (e: Exception) {
            throw IllegalStateException("Failed to cache image", e)
        }
    }

    fun getCacheDir(): File? {
        return Glide.getPhotoCacheDir(context)
    }

    fun clearCache() {
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
} 