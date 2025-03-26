package com.final_project.poop_bags.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapUtil {

    fun resizeBitmap(context: Context, drawableRes: Int, width: Int, height: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    fun calculateSizeByZoom(zoomLevel: Float): Int {
        return (70 * (zoomLevel / 20)).toInt()
    }
}