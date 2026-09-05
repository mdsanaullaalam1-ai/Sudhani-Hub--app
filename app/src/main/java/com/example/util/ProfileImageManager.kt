package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ProfileImageManager {

    private const val PREFS_NAME = "sudhani_user_profile_prefs"
    private const val KEY_PROFILE_PIC_PATH = "profile_picture_path"
    private const val KEY_NAME = "user_name"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_EMAIL = "user_email"

    private const val TARGET_IMAGE_SIZE = 512
    private const val JPEG_QUALITY = 85

    /**
     * Loads persisted user profile details from SharedPreferences.
     */
    fun loadPersistedUserProfile(context: Context, defaultUser: UserProfile): UserProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPath = prefs.getString(KEY_PROFILE_PIC_PATH, null)
        val name = prefs.getString(KEY_NAME, defaultUser.name) ?: defaultUser.name
        val phone = prefs.getString(KEY_PHONE, defaultUser.phone) ?: defaultUser.phone
        val email = prefs.getString(KEY_EMAIL, defaultUser.email) ?: defaultUser.email

        val validPath = if (savedPath != null && File(savedPath).exists()) {
            savedPath
        } else {
            null
        }

        return defaultUser.copy(
            name = name,
            phone = phone,
            email = email,
            profilePicturePath = validPath
        )
    }

    /**
     * Persists updated profile data into SharedPreferences.
     */
    fun persistUserProfile(context: Context, user: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_NAME, user.name)
            putString(KEY_PHONE, user.phone)
            putString(KEY_EMAIL, user.email)
            if (user.profilePicturePath != null) {
                putString(KEY_PROFILE_PIC_PATH, user.profilePicturePath)
            } else {
                remove(KEY_PROFILE_PIC_PATH)
            }
            apply()
        }
    }

    /**
     * Creates a temporary file in cache for camera captures.
     */
    fun createTempCameraUri(context: Context): Pair<Uri, File> {
        val cameraDir = File(context.cacheDir, "camera_captures")
        if (!cameraDir.exists()) {
            cameraDir.mkdirs()
        }
        val tempFile = File(cameraDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return Pair(uri, tempFile)
    }

    /**
     * Decodes a Bitmap from a Uri with orientation correction and safe downsampling.
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // First decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            // Calculate sample size to limit max dimension to 2048 to prevent OOM
            var inSampleSize = 1
            val maxDimension = max(options.outWidth, options.outHeight)
            while (maxDimension / (inSampleSize * 2) >= 2048) {
                inSampleSize *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            if (decodedBitmap == null) return null

            // Read Exif orientation
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            rotateBitmapIfNeeded(decodedBitmap, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (_: Exception) {
            bitmap
        }
    }

    /**
     * Crops and compresses a Bitmap according to the viewport transformations,
     * scales to 512x512, and saves securely in internal storage.
     */
    fun saveCroppedProfilePicture(
        context: Context,
        sourceBitmap: Bitmap,
        zoomScale: Float,
        panOffsetX: Float, // Normalized or raw offset
        panOffsetY: Float,
        rotationDegrees: Float,
        viewportSizePx: Float
    ): String? {
        return try {
            val avatarsDir = File(context.filesDir, "profile_avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }

            // Create transformed bitmap
            val matrix = Matrix()
            if (rotationDegrees != 0f) {
                matrix.postRotate(rotationDegrees, sourceBitmap.width / 2f, sourceBitmap.height / 2f)
            }

            val rotatedSource = if (rotationDegrees != 0f) {
                Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
            } else {
                sourceBitmap
            }

            // Calculate the visible region based on viewport and zoom
            val minSide = min(rotatedSource.width, rotatedSource.height).toFloat()
            val cropSize = (minSide / max(1f, zoomScale)).coerceAtMost(minSide)

            // Calculate center
            val centerX = rotatedSource.width / 2f - (panOffsetX / max(1f, viewportSizePx)) * cropSize
            val centerY = rotatedSource.height / 2f - (panOffsetY / max(1f, viewportSizePx)) * cropSize

            val left = (centerX - cropSize / 2f).coerceIn(0f, rotatedSource.width - cropSize).toInt()
            val top = (centerY - cropSize / 2f).coerceIn(0f, rotatedSource.height - cropSize).toInt()
            val width = cropSize.toInt().coerceAtMost(rotatedSource.width - left)
            val height = cropSize.toInt().coerceAtMost(rotatedSource.height - top)
            val squareSide = min(width, height)

            val croppedBitmap = Bitmap.createBitmap(rotatedSource, left, top, squareSide, squareSide)
            val finalScaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, true)

            // Save to internal storage
            val targetFile = File(avatarsDir, "avatar_USR_101.jpg")
            FileOutputStream(targetFile).use { out ->
                finalScaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.flush()
            }

            // Clean up temporary bitmaps if created
            if (rotatedSource != sourceBitmap && !rotatedSource.isRecycled) {
                rotatedSource.recycle()
            }
            if (croppedBitmap != finalScaledBitmap && !croppedBitmap.isRecycled) {
                croppedBitmap.recycle()
            }
            if (!finalScaledBitmap.isRecycled) {
                finalScaledBitmap.recycle()
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Directly saves an already processed/cropped Bitmap to internal storage.
     */
    fun saveBitmapDirectly(context: Context, bitmap: Bitmap): String? {
        return try {
            val avatarsDir = File(context.filesDir, "profile_avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }

            val finalScaled = if (bitmap.width != TARGET_IMAGE_SIZE || bitmap.height != TARGET_IMAGE_SIZE) {
                Bitmap.createScaledBitmap(bitmap, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, true)
            } else {
                bitmap
            }

            val targetFile = File(avatarsDir, "avatar_USR_101.jpg")
            FileOutputStream(targetFile).use { out ->
                finalScaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.flush()
            }

            if (finalScaled != bitmap && !finalScaled.isRecycled) {
                finalScaled.recycle()
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Removes the stored profile picture from internal storage.
     */
    fun removeProfilePicture(context: Context): Boolean {
        return try {
            val avatarsDir = File(context.filesDir, "profile_avatars")
            val targetFile = File(avatarsDir, "avatar_USR_101.jpg")
            if (targetFile.exists()) {
                targetFile.delete()
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_PROFILE_PIC_PATH).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
