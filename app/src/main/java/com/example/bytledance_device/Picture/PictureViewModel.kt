package com.example.bytledance_device.Picture

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.text.FieldPosition

class PictureViewModel(val context: Context) : ViewModel() {
    private var _photolist = getSystemPhotoList(context)
    val photolist get() = _photolist
    private fun  getSystemPhotoList(context: Context):List<String>?{
        val contentresolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
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
    private var _mode:Int=0
    val mode get() = _mode
    fun setviewscale(mode:Int){
        _mode=mode
    }
    fun refreshphotolist(){
        _photolist=getSystemPhotoList(context)
    }
}
