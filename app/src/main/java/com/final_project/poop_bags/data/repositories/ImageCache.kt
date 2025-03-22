package com.final_project.poop_bags.data.repositories

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imageCacheDir: File by lazy {
        File(context.cacheDir, "images").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun cacheImage(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Failed to open input stream")

            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val outputFile = File(imageCacheDir, fileName)

            FileOutputStream(outputFile).use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            throw Exception("Failed to cache image: ${e.message}")
        }
    }
} 