package com.example.bytledance_device.ui.main

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel(val context: Context) : ViewModel() {
    companion object{
        val BEHIND_LEN=1
        val FRONT_LEN=0
        val FLASH_AUTO=0
        val FLASH_ON=1
        val FLASH_OFF=2
        val Focus_AUTO=0
        val Focus_Hand=0
    }
private val _cameraid:MutableLiveData<Int> =MutableLiveData<Int>().apply {
    value=1
}
    val cameraid:LiveData<Int> = _cameraid
    private var _photolist = getSystemPhotoList(context)
    val photolist get() = _photolist
    fun changelen()
    {
        when(_cameraid.value)
        {
            FRONT_LEN->_cameraid.postValue(BEHIND_LEN)
            BEHIND_LEN->_cameraid.postValue(FRONT_LEN)
        }
    }
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
    fun refreshphotolist(){
        _photolist=getSystemPhotoList(context)
    }
    private var _flash = FLASH_AUTO
    val flash get() = _flash
    fun changeflash(type:Int){
        _flash=type
    }

}