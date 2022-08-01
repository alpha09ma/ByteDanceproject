package com.example.bytledance_device.luxiang

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bytledance_device.R
import com.example.bytledance_device.databinding.VideoFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor


class VideoFragment : Fragment() {

    companion object {
        fun newInstance() = VideoFragment()
    }
    private val cameraManager: CameraManager?
            by lazy {
                ContextCompat.getSystemService(requireContext(), CameraManager::class.java)
            }
    private lateinit var viewModel: VideoViewModel
    private lateinit var _binding:VideoFragmentBinding
    private val binding get() = _binding
    private lateinit var frontlenid:String
    private lateinit var behindlenid:String
    private lateinit var previewSurface: Surface
    private lateinit var recordSurface: Surface
    private lateinit var record: MediaRecorder
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession:CameraCaptureSession
    private var viewwidth:Int=0
    private var viewheight:Int=0
    private lateinit var deviceOrientationListener:OrientationEventListener
    private var deviceOrientation = 0
    private lateinit var filepath: String
    private lateinit var cameraCharacteristics:CameraCharacteristics
    @RequiresApi(Build.VERSION_CODES.N)
    private val dateFormat: DateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setOrientationEventListener()
        deviceOrientationListener.enable()
        Log.d("view","video")
        viewModel = ViewModelProvider(this).get(VideoViewModel::class.java)
        _binding= VideoFragmentBinding.inflate(inflater, container, false)
        val cameraList=cameraManager?.cameraIdList
        val outputs= mutableListOf<OutputConfiguration>()
        cameraList?.forEach {
            cameraCharacteristics = cameraManager!!.getCameraCharacteristics(it)
            Log.d("...",cameraCharacteristics.keys.toString())
            if(cameraCharacteristics[CameraCharacteristics.LENS_FACING]== CameraCharacteristics.LENS_FACING_FRONT)
                frontlenid=it
            if (cameraCharacteristics[CameraCharacteristics.LENS_FACING]== CameraCharacteristics.LENS_FACING_BACK)
                behindlenid=it
        }

        binding.video.surfaceTextureListener= object : TextureView.SurfaceTextureListener{
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                surface.setDefaultBufferSize(width, height)
                previewSurface= Surface(surface)
                record= MediaRecorder()
                viewwidth=width
                viewheight=height
                cameraCharacteristics=cameraManager!!.getCameraCharacteristics(behindlenid)
                setRecord()
                outputs.add(OutputConfiguration(previewSurface))
                outputs.add(OutputConfiguration(recordSurface))
                Log.d("op",outputs.toString())
            }
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.d(",,,",""+width+" "+height)
            }
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d(",,,","destroyed")
                return false
            }
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }
        binding.topicture.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.reversevideo.setOnClickListener {
            if(viewModel.src==VideoViewModel.REVERSE)
            {
                viewModel.changelen()
                Log.d("src","changlen")
            }
            if (viewModel.src==VideoViewModel.STOP)
            {
                record.stop()
                record.reset()
                setRecord()
                outputs.removeLast()
                outputs.add(OutputConfiguration(recordSurface))
                cameraDevice.createCaptureSession(SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputs,
                    Executor {
                        it.run()
                    }
                    ,object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession=session
                            Log.d("iop",session.toString())
                            createpreview(0)
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.d(",,,",session.toString())
                            Log.d(",,,","whathappen")
                        }
                    }
                ))
                //createpreview()
                insertvideoinfo()
                Log.d(",,,",videoThumbnailFromPath(filepath,binding.videotopicture.width,binding.videotopicture.height).toString())
                binding.videotopicture.setImageBitmap(videoThumbnailFromPath(filepath,binding.videotopicture.width,binding.videotopicture.height))
                binding.reversevideo.setImageResource(R.drawable.ic_baseline_cached_24)
            }
        }
        viewModel.cameraid.observe(viewLifecycleOwner){
            if (this::cameraDevice.isInitialized)
                cameraDevice.close()
            if (it==1)
                MainScope().launch {
                    Log.d("ppp","begin")
                    opencamera(outputs,behindlenid)
                }
            else
                MainScope().launch {
                    opencamera(outputs,frontlenid)
                }
        }
        binding.takevideo.setOnClickListener {
            MainScope().launch {
                caputure()
            }
        }
        binding.videotopicture.setOnClickListener {
            findNavController().navigate(R.id.videoplayer)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("permission","postart")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("权限申请")
                .setMessage("请授予录音权限，否则应用无法正常使用。")
                .setPositiveButton("前往授予权限") { dialog, which ->
                    val intent1: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent1.setData(Uri.parse("package:"+requireActivity().packageName))
                    startActivity(intent1)
                }
                .setNegativeButton("退出应用") { _, _ ->
                    Toast.makeText(requireContext(),"应用已退出",Toast.LENGTH_SHORT).show()
                    Process.killProcess(Process.myPid())
                }
                .show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun opencamera(outputs:MutableList<OutputConfiguration>,cameraid:String){
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
        {
            cameraManager?.openCamera(cameraid,object : CameraDevice.StateCallback(){
                override fun onOpened(camera: CameraDevice) {
                    Log.d("camera","open")
                    cameraDevice=camera
                    cameraCharacteristics=cameraManager!!.getCameraCharacteristics(cameraid)
                    cameraDevice.createCaptureSession(SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        outputs,
                        Executor {
                            it.run()
                        }
                        ,object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                captureSession=session
                                Log.d("iop",session.toString())
                                createpreview(0)
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.d(",,,",session.toString())
                                Log.d(",,,","whathappen")
                            }
                        }
                    ))
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    //camera.close()
                }
                override fun onDisconnected(camera: CameraDevice) {
                }
            }, Handler(Looper.myLooper()!!))

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)


    private suspend fun caputure(){
        captureSession.stopRepeating()
        createpreview(1)
        record.start()
        binding.reversevideo.setImageResource(R.drawable.ic_baseline_stop_24)
        viewModel.changesrc()
    }

    override fun onPause() {
        super.onPause()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
        deviceOrientationListener.disable()
    }
    private fun createpreview(type:Int){
        if(type!=1)
        {
            val requestbuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestbuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            requestbuilder.addTarget(previewSurface!!)
            val request=requestbuilder.build()
            captureSession.setRepeatingRequest(request,object :
                CameraCaptureSession.CaptureCallback(){},null)
        }
        if(type==1)
        {
            val requestbuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            requestbuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            requestbuilder.addTarget(previewSurface!!)
            requestbuilder.addTarget(recordSurface)
            val request=requestbuilder.build()
            captureSession.setRepeatingRequest(request,object :
                CameraCaptureSession.CaptureCallback(){},null)
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setRecord(){
        record.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        record.setAudioSource(MediaRecorder.AudioSource.MIC)
        record.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//设置输出格式
        record.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //设置视频编码格式
        record.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置音频编码格式
        record.setVideoSize(viewwidth,viewheight)
        record.setVideoFrameRate(30);//设置帧数
        record.setVideoEncodingBitRate(viewheight*viewwidth*30)
        val jpegOrientation = getJpegOrientation(cameraCharacteristics, deviceOrientation)
        Log.d("yui",jpegOrientation.toString())
        record.setOrientationHint(jpegOrientation)
        //record.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW))
        filepath="${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/${dateFormat.format(System.currentTimeMillis())}.mp4"
        record.setOutputFile(filepath)
        record.prepare()
        recordSurface=record.surface
    }
    private fun getJpegOrientation(cameraCharacteristics: CameraCharacteristics, deviceOrientation: Int): Int {
        var myDeviceOrientation = deviceOrientation
        if (myDeviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0
        }
        val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        Log.d("sesor",myDeviceOrientation.toString())
        // Reverse device orientation for front-facing cameras
        val facingFront = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        if (facingFront) {
            Log.d("...","ppppp")
            myDeviceOrientation = -myDeviceOrientation
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + myDeviceOrientation + 360) % 360
    }
    private fun setOrientationEventListener(){
        deviceOrientationListener=object :OrientationEventListener(activity) {
            override fun onOrientationChanged(orientation: Int) {
                Log.d("???",orientation.toString())
                if (orientation > 350 || orientation < 10)
                    deviceOrientation = 0
                else
                    if (orientation > 80 && orientation < 110)
                        deviceOrientation = 90
                    else
                        if (orientation > 170 && orientation < 190)
                            deviceOrientation = 180
                        else
                            if (orientation > 260 && orientation < 280)
                                deviceOrientation = 270
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun videoThumbnailFromPath(path: String?, width: Int, height: Int): Bitmap? {
        var bmp: Bitmap? = null
        Log.d("bmp",bmp.toString())
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path)//本人测试时用小米手机该方式多次崩溃，考虑到videoview封装了mediaplay，而个人尝试使用mediaplay时setdatasourece同样出现问题，百度搜索推测可能与小米手机解析本地路径方式有关，能力有限，无法解决
            bmp = mediaMetadataRetriever.getScaledFrameAtTime(
                2,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                512,
                384
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (bmp == null) {
            return null
        }
        val ret: Bitmap = resizeForThumbnail(bmp, width, height, null)
        bmp.recycle()
        return ret
    }
    private fun resizeForThumbnail(source: Bitmap, width: Int, height: Int, matrix: Matrix?): Bitmap {
        var source = source
        val w = source.width
        val h = source.height
        if (w > h) {
            source = Bitmap.createBitmap(source, (w - h) / 2, 0, h, h, matrix, false)
        } else if (w < h) {
            source = Bitmap.createBitmap(source, 0, (h - w) / 2, w, w, matrix, false)
        }
        return source
    }
    private fun insertvideoinfo(){
        val values = ContentValues()
        values.put(MediaStore.Video.VideoColumns.TITLE, filepath)
        values.put(MediaStore.Video.VideoColumns.DATA, filepath)
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis())

        values.put(MediaStore.MediaColumns.DATA, filepath)
        requireActivity().contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }
}