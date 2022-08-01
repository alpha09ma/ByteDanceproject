package com.example.bytledance_device.video

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.File

class VideoplayerViewModel(val context: Context) : ViewModel() {
    private var _videolist = getSystemPhotoList(context)
    val videolist get() = _videolist
    private fun  getSystemPhotoList(context: Context):List<String>?{
        val contentresolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val result: MutableList<String> = mutableListOf()
        val cursor: Cursor? =contentresolver.query(uri,null,null,null,null)
        if (cursor == null || cursor.getCount() <= 0) return null
        while (cursor.moveToNext()) {
            val index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = cursor.getString(index) // 文件地址
            val file = File(path)
            if (file.exists()) {
                result.add(path)
                Log.i(ContentValues.TAG, path)
            }
        }
        result.reverse()
        return result
    }

}