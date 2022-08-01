package com.example.bytledance_device.Picture

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bytledance_device.R

class Viewpageadapter(val viewModel: PictureViewModel):RecyclerView.Adapter<ViewpageViewHolder>() {
    private lateinit var viewholer: ViewpageViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewpageViewHolder {
       val root=LayoutInflater.from(parent.context).inflate(R.layout.viewpageitem,parent,false)
        viewholer= ViewpageViewHolder(root,viewModel)
        Log.d("iii",viewholer.toString())
        return viewholer
    }

    override fun onBindViewHolder(holder: ViewpageViewHolder, position: Int) {
        viewholer=holder
        Log.d("iii",viewholer.toString())
        holder.bind(position)

    }
    override fun getItemCount(): Int {
        return viewModel.photolist!!.size
    }

    fun getviewholderinstance(): ViewpageViewHolder?{

        if (this::viewholer.isInitialized)
            return viewholer
        else
            return null
    }
    fun refresh(){
        notifyDataSetChanged()
    }

}