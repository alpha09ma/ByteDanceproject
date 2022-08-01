package com.example.bytledance_device.video

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytledance_device.ViewModelFactory
import com.example.bytledance_device.databinding.VideoplayerFragmentBinding

class Videoplayer : Fragment() {

    companion object {
        fun newInstance() = Videoplayer()
    }

    private lateinit var viewModel: VideoplayerViewModel
    private lateinit var _binding:VideoplayerFragmentBinding
    private val binding get() = _binding
    private val adapter by lazy {
        VideoViewadapter(viewModel)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= VideoplayerFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application)).get(VideoplayerViewModel::class.java)
        binding.videopager.adapter=adapter
        return binding.root
    }

}