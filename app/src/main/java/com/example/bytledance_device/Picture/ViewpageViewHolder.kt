package com.example.bytledance_device.Picture

import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.bytledance_device.R

class ViewpageViewHolder(view: View,val viewModel: PictureViewModel,) :RecyclerView.ViewHolder(view){
    val imageView=view.findViewById<ImageView>(R.id.image)

    fun bind(position:Int){
        imageView.setImageURI(Uri.parse(viewModel.photolist!![position]))
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun viewtransform(scale:Float,x:Float,y:Float){
        imageView.pivotX=x
        imageView.pivotY=y
        imageView.scaleX=scale
        imageView.scaleY=scale
        Log.d("transform","qwer")
    }
    fun viewdrag(x:Float,y:Float){
        imageView.translationX=-x
        imageView.translationY=-y
    }

}