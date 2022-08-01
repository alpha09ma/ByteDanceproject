package com.example.bytledance_device

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bytledance_device.Picture.PictureViewModel
import com.example.bytledance_device.luxiang.VideoViewModel
import com.example.bytledance_device.ui.main.MainViewModel
import com.example.bytledance_device.video.VideoplayerViewModel

class ViewModelFactory(val app:Application): ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PictureViewModel::class.java))
            return PictureViewModel(app) as T
        if(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(app) as T
        if(modelClass.isAssignableFrom(VideoplayerViewModel::class.java))
            return VideoplayerViewModel(app) as T
        return super.create(modelClass)
    }
}