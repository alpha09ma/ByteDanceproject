package com.example.bytledance_device.video

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.bytledance_device.Picture.PictureViewModel
import com.example.bytledance_device.Picture.ViewpageViewHolder
import com.example.bytledance_device.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoViewadapter(val viewModel: VideoplayerViewModel): RecyclerView.Adapter<VideoViewadapter.VideoViewViewHolder>() {
    private var play_stop=true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewViewHolder {
        val root= LayoutInflater.from(parent.context).inflate(R.layout.videoplayer,parent,false)
        return VideoViewViewHolder(root)
    }

    override fun onBindViewHolder(holder: VideoViewViewHolder, position: Int) {
        holder.bind(viewModel.videolist!![position])
        holder.player.setOnClickListener {
            if (play_stop)
            {
                holder.videoView.start()
                holder.player.setImageResource(R.drawable.ic_baseline_pause_24)
                play_stop=false
            }
             else
            {
                holder.videoView.pause()
                holder.player.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                play_stop=true
            }
        }
        holder.stop.setOnClickListener {
            holder.videoView.stopPlayback()
        }
    }
    override fun getItemCount(): Int {
        return viewModel.videolist!!.size
    }
    fun refresh(){
        notifyDataSetChanged()
    }
   inner class VideoViewViewHolder(view: View) :RecyclerView.ViewHolder(view)
   {        val videoView=view.findViewById<VideoView>(R.id.videoView)
        //val surfaceView=view.findViewById<SurfaceView>(R.id.videoView)
        val player=view.findViewById<FloatingActionButton>(R.id.play)
        val stop=view.findViewById<FloatingActionButton>(R.id.stop)
        fun bind(path:String){
            Log.d("path",path)
            videoView.setVideoPath(path)
            videoView.setOnPreparedListener {
                    it.setLooping(true);
            }
        }
   }
}