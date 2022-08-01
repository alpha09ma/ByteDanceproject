package com.example.bytledance_device.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bytledance_device.MainActivity
import com.example.bytledance_device.ViewModelFactory
import com.example.bytledance_device.R
import com.example.bytledance_device.databinding.MainFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingDeque



class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }
    private val cameraManager:CameraManager?
    by lazy {
        getSystemService(requireContext(),CameraManager::class.java)
    }
    private lateinit var cameraDevice: CameraDevice
    private lateinit var viewModel: MainViewModel
    private lateinit var _binding:MainFragmentBinding
    private val binding get() = _binding
    private lateinit var frontlenid:String
    private lateinit var behindlenid:String
    private lateinit var previewSurface:Surface
    private lateinit var jpgSurface: Surface
    private lateinit var captureSession:CameraCaptureSession
    private lateinit var deviceOrientationListener:OrientationEventListener
    private var maxZoom:Float=0f
    private var deviceOrientation = 0

    private lateinit var cameraCharacteristics:CameraCharacteristics

        private val captureResults: BlockingQueue<CaptureResult> = LinkedBlockingDeque()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application)).get(MainViewModel::class.java)
        _binding= MainFragmentBinding.inflate(inflater, container, false)
        val cameraList=cameraManager?.cameraIdList
        val outputs= mutableListOf<OutputConfiguration>()
        var imageReader:ImageReader

        cameraList?.forEach {
            cameraCharacteristics = cameraManager!!.getCameraCharacteristics(it)
            Log.d("...",cameraCharacteristics.keys.toString())
            if(cameraCharacteristics[CameraCharacteristics.LENS_FACING]==CameraCharacteristics.LENS_FACING_FRONT)
                frontlenid=it
            if (cameraCharacteristics[CameraCharacteristics.LENS_FACING]==CameraCharacteristics.LENS_FACING_BACK)
                behindlenid=it
        }
        binding.picture.surfaceTextureListener= object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                surface.setDefaultBufferSize(width, height)
                previewSurface= Surface(surface)
                imageReader= ImageReader.newInstance(width,height,ImageFormat.JPEG,5)
                imageReader.setOnImageAvailableListener(object :ImageReader.OnImageAvailableListener {
                    override fun onImageAvailable(reader: ImageReader?) {
                        Log.d("iop","available")
                        val image=reader?.acquireNextImage()
                        if (image != null)
                        {
                            // Save image into sdcard.
                            Log.d("iop",image.javaClass.name)
                            GlobalScope.launch(Dispatchers.Default) {
                                val thumbnail=Saveimage.saveImage(image, requireActivity())
                                withContext(Dispatchers.Main)
                                {
                                    binding.topictureview.setImageBitmap(thumbnail)
                                }
                                image.close()
                            }
                        }
                    }
                },null)
                jpgSurface=imageReader.surface
                outputs.add(OutputConfiguration(previewSurface))
                outputs.add(OutputConfiguration(jpgSurface))
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
                    return false
                }
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
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
        binding.topictureview.setImageURI(Uri.parse(viewModel.photolist!![0]))
        binding.reverse.setOnClickListener {
            viewModel.changelen()
        }
        binding.take.setOnClickListener {
            MainScope().launch {
                caputure()
            }
        }
        binding.topictureview.setOnClickListener {
            findNavController().navigate(R.id.pictureFragment)
        }
        binding.tovideo.setOnClickListener {
            findNavController().navigate(R.id.videoFragment2)
        }
        val mScaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleGestureListener())

        val myOnTouchListener: MainActivity.MyOnTouchListener=object: MainActivity.MyOnTouchListener{
            override fun onTouch(ev: MotionEvent?): Boolean {
                mScaleGestureDetector.onTouchEvent(ev);
                return false;
            }
        }
        (getActivity() as MainActivity).registerMyOnTouchListener(myOnTouchListener)
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStart() {
        Log.d("op","onstart")
        setOrientationEventListener()
        deviceOrientationListener.enable()
        viewModel.refreshphotolist()
        binding.topictureview.setImageURI(Uri.parse(viewModel.photolist!![0]))

        if(checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d("permission","postart")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("权限申请")
                .setMessage("请授予拍照权限，否则应用无法正常使用。")
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
        super.onStart()
    }
    override fun onPause() {
        super.onPause()
        Log.d("op","destroyed")
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
        deviceOrientationListener.disable()
    }
    private lateinit var menu:Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.takepicturemenu,menu)
        this.menu=menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.auto->
                viewModel.changeflash(MainViewModel.FLASH_AUTO)
            R.id.open->
                viewModel.changeflash(MainViewModel.FLASH_ON)
            R.id.off->
                viewModel.changeflash(MainViewModel.FLASH_ON)
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun opencamera(outputs:MutableList<OutputConfiguration>,cameraid:String){
        if(checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
        {
            cameraManager?.openCamera(cameraid,object :CameraDevice.StateCallback(){
                override fun onOpened(camera: CameraDevice) {
                    Log.d("camera","open")
                    cameraDevice=camera
                    cameraCharacteristics = cameraManager!!.getCameraCharacteristics(cameraid)
                    maxZoom = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)!!
                    cameraDevice.createCaptureSession(SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        outputs,
                        Executor {
                            it.run()
                        }
                        ,object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                captureSession=session
                                val maxZoom: Float? = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
                                val rect: Rect? = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

                                setreaptingrequest()
                                Log.d("iop",session.toString())
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.d(",,,",session.toString())
                                Log.d(",,,","whathappen")
                            }
                        }
                    ))
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d("error",error.toString())
                    cameraDevice.close()
                }
                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("camera1",cameraDevice.toString())
                    cameraDevice.close()

                }
            }, null)

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun caputure(){
            val picturerequestbuilder= cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            picturerequestbuilder.addTarget(previewSurface)
            picturerequestbuilder.addTarget(jpgSurface)
            when(viewModel.flash)
            {
                MainViewModel.FLASH_AUTO->
                    picturerequestbuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                MainViewModel.FLASH_ON->{
                    picturerequestbuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    picturerequestbuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_SINGLE)
                }
                MainViewModel.FLASH_OFF->{
                    picturerequestbuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    picturerequestbuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                }
            }
            //picturerequestbuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO)

            picturerequestbuilder.set(CaptureRequest.SCALER_CROP_REGION,cropRegion)
            val jpegOrientation = getJpegOrientation(cameraCharacteristics, deviceOrientation)
            picturerequestbuilder[CaptureRequest.JPEG_ORIENTATION] = jpegOrientation
            val picturerequet=picturerequestbuilder.build()
            Log.d("???",captureSession.toString())
            captureSession.capture(picturerequet,object :CameraCaptureSession.CaptureCallback(){
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    Log.d("iop","completed")
                    Log.d("iop",result.partialResults.toString())
                    result.partialResults.forEach {
                        Log.d("result",it.keys.toString())
                    }
                    //captureResults.put(result)
                    super.onCaptureCompleted(session, request, result)
                }
                override fun onCaptureStarted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    timestamp: Long,
                    frameNumber: Long
                ) {
                    Log.d("iop","start")
                    super.onCaptureStarted(session, request, timestamp, frameNumber)
                }
                override fun onCaptureProgressed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    partialResult: CaptureResult
                ) {
                    super.onCaptureProgressed(session, request, partialResult)
                }
            }, null)
    }
    private lateinit var cropRegion:Rect
    private var left=0f
    private var right=0f
    private var top=0f
    private var bottom=0f
    private fun setreaptingrequest(){
        val prerequestbuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        prerequestbuilder.addTarget(previewSurface)
        cropRegion = prerequestbuilder.get(CaptureRequest.SCALER_CROP_REGION)!!
        captureSession.setRepeatingRequest(prerequestbuilder.build(),object :CameraCaptureSession.CaptureCallback(){},Handler(Looper.myLooper()!!))
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

    inner class ScaleGestureListener(): ScaleGestureDetector.OnScaleGestureListener{
        val screenheight=requireActivity().getWindowManager().getDefaultDisplay().height
        val screenwidth=requireActivity().getWindowManager().getDefaultDisplay().width
        var viewscale:Float = 1f
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val scale:Float = detector?.getScaleFactor()!!
            val mMatrix = Matrix()
            mMatrix.setScale(viewscale*scale, viewscale*scale,screenwidth/2f,screenheight/2f)
            if(viewscale*scale>=1)
            binding.picture.setTransform(mMatrix)

            Log.d("scale",scale.toString())
            return false
        }
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            viewscale*=detector!!.getScaleFactor()
            if(viewscale<1)
                viewscale=1f
            if(viewscale>maxZoom)
                viewscale=maxZoom
            cropRegion.width()*viewscale/2
            Log.d("view",viewscale.toString())
        }


    }
}

