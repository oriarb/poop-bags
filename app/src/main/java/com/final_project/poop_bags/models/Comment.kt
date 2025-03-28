package com.final_project.poop_bags.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String,
    val userId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable