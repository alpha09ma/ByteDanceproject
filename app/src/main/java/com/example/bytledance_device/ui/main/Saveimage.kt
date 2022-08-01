package com.example.bytledance_device.ui.main

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
import android.media.Image
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Thumbnails.getThumbnail
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.time.LocalDateTime
import java.util.*


class Saveimage {
    companion object{
        @RequiresApi(Build.VERSION_CODES.O)
        fun saveImage(image: Image,activity: Activity): Bitmap?{
            val date:LocalDateTime= LocalDateTime.now()


            val dateFormat: DateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
            val fileName = dateFormat.format(System.currentTimeMillis())
            val path="${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}"

            val width = image.width
            val height = image.height

            val jpegByteBuffer = image.planes[0].buffer
            val jpegByteArray = ByteArray(jpegByteBuffer.remaining())
            jpegByteBuffer.get(jpegByteArray)

            //val bitmapImage = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size, null)
            /*activity.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
                it.close()
            }

            val path=activity.getFileStreamPath(fileName)
            Log.d("???",path.toString())*/
            File(path+"/"+fileName+".jpeg").writeBytes(jpegByteArray)
            Log.d("...",path+"/"+fileName+".jpeg")
            val values = ContentValues()
            values.put(MediaStore.Images.ImageColumns.TITLE, fileName)
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName+".jpeg")
            values.put(MediaStore.Images.ImageColumns.DATA, path+"/"+fileName+".jpeg")
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.ImageColumns.WIDTH, width)
            values.put(MediaStore.Images.ImageColumns.HEIGHT, height)
            values.put(MediaStore.MediaColumns.DATA, path+"/"+fileName+".jpeg")
            activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            return getThumbnail(path+"/"+fileName+".jpeg")
        }
        @RequiresApi(Build.VERSION_CODES.O)
        private fun getThumbnail(jpegPath: String): Bitmap? {
            val exifInterface = ExifInterface(jpegPath)
            val orientationFlag = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val orientation = when (orientationFlag) {
                ExifInterface.ORIENTATION_NORMAL -> 0.0F
                ExifInterface.ORIENTATION_ROTATE_90 -> 90.0F
                ExifInterface.ORIENTATION_ROTATE_180 -> 180.0F
                ExifInterface.ORIENTATION_ROTATE_270 -> 270.0F
                else -> 0.0F
            }

            var thumbnail = if (exifInterface.hasThumbnail()) {
                exifInterface.thumbnailBitmap
            } else {
                val options = BitmapFactory.Options()
                options.inSampleSize = 16
                BitmapFactory.decodeFile(jpegPath, options)
            }
            if (orientation != 0.0F && thumbnail != null) {
                val matrix = Matrix()
                matrix.setRotate(orientation)
                thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.width, thumbnail.height, matrix, true)
            }
            return thumbnail
        }

    }
}