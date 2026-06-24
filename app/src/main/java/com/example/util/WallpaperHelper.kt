package com.example.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object WallpaperHelper {

    /**
     * Download image bitmap from url
     */
    suspend fun downloadBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(imageUrl).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.getInputStream().use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Sets wallpaper as home screen, lock screen or both.
     * @return true if successful
     */
    suspend fun setAsWallpaper(context: Context, bitmap: Bitmap, target: WallpaperTarget): Boolean = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (target) {
                    WallpaperTarget.HOME -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    WallpaperTarget.LOCK -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                    WallpaperTarget.BOTH -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                }
            } else {
                // For pre-Nougat devices, just set the wallpaper standard way (both)
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Saves wallpaper image to the gallery / Pictures folder.
     * @return URI string of saved image if successful, null otherwise
     */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, title: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val sanitizedTitle = title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val filename = "AnimeWallpaper_${sanitizedTitle}_${System.currentTimeMillis()}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AnimeWallpapers")
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false
            resolver.openOutputStream(imageUri).use { out ->
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

enum class WallpaperTarget {
    HOME, LOCK, BOTH
}
