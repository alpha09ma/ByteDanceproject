package com.example.bytledance_device.luxiang

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bytledance_device.ui.main.MainViewModel

class VideoViewModel : ViewModel() {
    companion object{
        val BEHIND_LEN=1
        val FRONT_LEN=0
        val REVERSE=0
        val STOP=1
    }
    private val _cameraid: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value=1
    }
    val cameraid: LiveData<Int> = _cameraid
    var src:Int=0
    fun changelen()
    {
        when(_cameraid.value)
        {
            FRONT_LEN ->_cameraid.postValue(BEHIND_LEN)
           BEHIND_LEN ->_cameraid.postValue(FRONT_LEN)
        }
    }
    fun changesrc(){
        if (src==0)
            src=1
        if(src==1)
            src==0
    }
}