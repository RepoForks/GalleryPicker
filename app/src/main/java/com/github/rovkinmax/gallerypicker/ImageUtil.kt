package com.github.rovkinmax.gallerypickerl

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author Rovkin Max
 */
object ImageUtil {
    val PHOTO_WIDTH = 1024
    val PHOTO_HEIGHT = 1024
    val TEMP_CAMERA_FILE_NAME = "temp_external_camera.jpg"

    fun resizeByteArray(bitmapArray: ByteArray): Bitmap {
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.count(), options)
        if (options.outWidth > PHOTO_WIDTH && options.outHeight > PHOTO_HEIGHT) {
            val widthSample = options.outWidth.toDouble() / PHOTO_WIDTH.toDouble()
            val heightSample = options.outHeight.toDouble() / PHOTO_HEIGHT.toDouble()
            options.inSampleSize = Math.ceil(Math.max(heightSample, widthSample)).toInt()
        }
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.count(), options)
    }

    fun cropTop(bitmapArray: ByteArray): Bitmap {
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.count(), options)

        // Create a TOP cropped image from sources.
        val size = Math.min(options.outWidth, options.outHeight)
        options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val inst = BitmapRegionDecoder.newInstance(bitmapArray, 0, bitmapArray.count(), false)
        return inst.decodeRegion(Rect(0, 0, size, size), options)
    }

    fun resizeBitmap(source: Bitmap, reqWidth: Int = PHOTO_WIDTH, reqHeight: Int = PHOTO_HEIGHT): Bitmap {
        if (source.width == reqWidth && source.height == reqHeight) return source
        val maxSize = Math.max(source.width, source.height)
        val minReqSize = Math.min(reqHeight, reqWidth)
        val scale = minReqSize.toDouble() / maxSize.toDouble()
        val width = (source.width * scale).toInt()
        val height = (source.height * scale).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(source, width, height, true)
        if (source != resizedBitmap) {
            source.recycle()
        }
        return resizedBitmap
    }

    fun rotateBitmap(source: Bitmap, angle: Int = 270): Bitmap {
        if (angle == 0) {
            return source
        }
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotatedBitmap != source) {
            source.recycle()
        }
        return rotatedBitmap
    }

    fun saveToFile(pictureFile: File, result: Bitmap): Boolean {
        var picSaved = false
        try {
            val fos = FileOutputStream(pictureFile)
            picSaved = result.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            result.recycle()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return picSaved
    }

    fun createTempFileForImage(context: Context): File {
        return File("${context.filesDir.path}${File.separator}temp.jpg")
    }

    fun copyExternalImageFile(context: Context, fileName: String): String {
        val tempFile = createTempFileForImage(context)
        tempFile.delete()
        tempFile.createNewFile()
        copyFile(File(fileName), tempFile)
        return tempFile.absolutePath
    }

    private fun copyFile(sourceFile: File, destinyFile: File) {
        val input = FileInputStream(sourceFile)
        val output = FileOutputStream(destinyFile)
        val buffer = ByteArray(8192)
        var count = 0
        do {
            count = input.read(buffer)
            if (count > 0) output.write(buffer, 0, count)
        } while (count > 0)
    }
}