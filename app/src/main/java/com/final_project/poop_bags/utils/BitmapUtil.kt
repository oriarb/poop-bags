package com.final_project.poop_bags.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapUtil {

    fun resizeBitmap(context: Context, drawableRes: Int, width: Int, height: Int): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, drawableRes) ?: return null
        val bitmap = getBitmapFromDrawable(drawable, width, height) ?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getBitmapFromDrawable(drawable: Drawable, width: Int, height: Int): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun calculateSizeByZoom(zoomLevel: Float): Int {
        return (70 * (zoomLevel / 20)).toInt()
    }
}