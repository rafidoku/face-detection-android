package com.example.faceverification.common

import android.graphics.*
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageUtils {
    companion object shared {
        fun getCompressBitmap(bmp: Bitmap): Bitmap? {
            val maxHeight = 1920.0
            val maxWidth = 1080.0
            var scaledBitmap: Bitmap? = null
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            var actualHeight: Int = bmp.height
            var actualWidth: Int = bmp.width
            var imgRatio: Int = actualWidth / actualHeight
            val maxRatio = maxWidth / maxHeight

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = (maxHeight / actualHeight).toInt()
                    actualWidth = (imgRatio * actualWidth) as Int
                    actualHeight = maxHeight.toInt()
                } else if (imgRatio > maxRatio) {
                    imgRatio = (maxWidth / actualWidth).toInt()
                    actualHeight = (imgRatio * actualHeight) as Int
                    actualWidth = maxWidth.toInt()
                } else {
                    actualHeight = maxHeight.toInt()
                    actualWidth = maxWidth.toInt()
                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = ByteArray(16 * 1024)

            try {
                scaledBitmap =
                    Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
            }

            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f

            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

            if (scaledBitmap != null) {
                val canvas = Canvas(scaledBitmap)
                canvas.setMatrix(scaleMatrix)
                canvas.drawBitmap(
                    bmp,
                    middleX - bmp.width / 2,
                    middleY - bmp.height / 2,
                    Paint(Paint.FILTER_BITMAP_FLAG)
                )

                val out = ByteArrayOutputStream()
                scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, out)

                val byteArray: ByteArray = out.toByteArray()

                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            } else {
                return Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
            }

        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }
            val totalPixels = (width * height).toFloat()
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
            return inSampleSize
        }
    }
}