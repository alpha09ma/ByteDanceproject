package com.example.bytledance_device

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    public lateinit var picpath:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        //findViewById<ViewPager2>(R.id.Pager).adapter=Viewpageadapter(listOf("拍照","录像"))
        /*if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }*/
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        val requestlaunch:ActivityResultLauncher<Array<String>>
        requestlaunch=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            it.forEach { key,value ->
                if (value)
                    Log.d("permission","Y" )
                else
                    Log.d("permission","N"+key )
            }
        }

        val permissionlist:MutableList<String> = mutableListOf()
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            permissionlist.add(android.Manifest.permission.CAMERA)
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            permissionlist.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            permissionlist.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            permissionlist.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            permissionlist.add(android.Manifest.permission.RECORD_AUDIO)
        requestlaunch.launch(permissionlist.toTypedArray())
        super.onStart()
    }
    interface MyOnTouchListener {
        fun onTouch(ev: MotionEvent?): Boolean
    }

    private val onTouchListeners = ArrayList<MyOnTouchListener>(2)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (listener in onTouchListeners) {
            listener?.onTouch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun registerMyOnTouchListener(myOnTouchListener: MyOnTouchListener) {
        onTouchListeners.add(myOnTouchListener)
    }

    fun unregisterMyOnTouchListener(myOnTouchListener: MyOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener)
    }
}