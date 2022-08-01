package com.example.bytledance_device.Picture

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.bytledance_device.MainActivity
import com.example.bytledance_device.R
import com.example.bytledance_device.databinding.PictureFragmentBinding
import com.example.bytledance_device.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PictureFragment : Fragment() {

    companion object {
        fun newInstance() = PictureFragment()
    }

    private lateinit var viewModel: PictureViewModel
    private lateinit var _binding:PictureFragmentBinding
    private val binding get() = _binding
    private val adapter by lazy {
      Viewpageadapter(viewModel)
    }
    private var viewHolder: ViewpageViewHolder?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel=ViewModelProvider(this, ViewModelFactory(requireActivity().application)).get(PictureViewModel::class.java)
        _binding= PictureFragmentBinding.inflate(inflater, container, false)
        binding.pager.adapter=adapter
        binding.pager.registerOnPageChangeCallback(onpagechangecallback())
        val mScaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleGestureListener())
        val mGestureDetector = GestureDetector(context,simpleOnGestureListener())

        val myOnTouchListener: MainActivity.MyOnTouchListener=object: MainActivity.MyOnTouchListener{

            override fun onTouch(ev: MotionEvent?): Boolean {
                    Log.d("mode",viewModel.mode.toString())
                    if(ev?.pointerCount!!>1)
                        viewModel.setviewscale(1)
                    if(viewModel.mode==1)
                    {
                        mScaleGestureDetector.onTouchEvent(ev)
                        binding.pager.isUserInputEnabled=false
                    }
                    if (viewModel.mode==0)
                    {
                        binding.pager.isUserInputEnabled=true
                    }
                    if (viewModel.mode==2)
                    {
                        mGestureDetector.onTouchEvent(ev)
                        binding.pager.isUserInputEnabled=false
                    }
                return false;
            }
        }
        (getActivity() as MainActivity).registerMyOnTouchListener(myOnTouchListener)
        /*viewModel.photoid.observe(viewLifecycleOwner){
            binding.imageView.setImageURI(Uri.parse(viewModel.photolist!![it]))
        }*/
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.picturemenu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.systemphoto->{
                val photo_intent= Intent()
                photo_intent.setAction(Intent.ACTION_MAIN)
                photo_intent.addCategory(Intent.CATEGORY_APP_GALLERY)
                startActivity(photo_intent)
            }
            R.id.delete->{
                val dialog= MaterialAlertDialogBuilder(requireContext())
                    .setTitle("请选择是否删除")
                    .setPositiveButton("是"){
                        dialog,whitch->
                        val path=viewModel.photolist!![viewHolder!!.absoluteAdapterPosition]
                        requireActivity().contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,MediaStore.Images.Media.DATA + "=?",
                            arrayOf(path))
                        viewModel.refreshphotolist()
                        adapter.refresh()
                    }
                    .setNegativeButton("否"){
                        dialog,whitch->
                    }
                    .create()
                dialog.setCanceledOnTouchOutside(true)
                dialog.show()
            }
            R.id.share->
            {
                val path=viewModel.photolist!![viewHolder!!.absoluteAdapterPosition]
                val share_intent= Intent()
                share_intent.setAction(Intent.ACTION_SEND)
                share_intent.setType("image/*")
                share_intent.putExtra(Intent.EXTRA_STREAM,path)
                val share= Intent.createChooser(share_intent, "share")
                startActivity(share)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    var maxleft=0f
    var maxright=0f
    var maxtop=0f
    var maxbottom=0f
    inner class ScaleGestureListener(): ScaleGestureDetector.OnScaleGestureListener{
        var viewscale:Float = 1f
        var x=0f
        var y=0f
        var screenwidth=requireActivity().getWindowManager().getDefaultDisplay().width
        var screenheight=requireActivity().getWindowManager().getDefaultDisplay().height
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val scale:Float = detector?.getScaleFactor()!!
            if(viewscale*scale>=1)
            {
                viewHolder!!.viewtransform(viewscale*scale,x,y)
                Log.d("iop",viewHolder.toString())
            }
            Log.d("scale",scale.toString())
            return false
        }
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            if(viewscale==1f)
            {
                Log.d(",,,","xy")
                x= detector!!.focusX
                y=detector.focusY
            }
            if (viewHolder==null)
                viewHolder=adapter.getviewholderinstance()
            return true
        }
        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            viewscale*=detector!!.getScaleFactor()
            if(viewscale<1)
            {
                viewscale=1f
                viewModel.setviewscale(0)
            }
            if (viewscale==1f)
            {
                viewModel.setviewscale(0)
                viewHolder!!.viewdrag(0f,0f)
            }
            if (viewscale>1)
                viewModel.setviewscale(2)
            maxleft=x*viewscale-x
            maxright=(screenwidth-x)*(viewscale-1)
            maxtop=(viewscale-1)*y
            maxbottom=(viewscale-1)*(screenheight-y)
            Log.d("view",viewscale.toString())
            Log.d("right",maxright.toString())
            Log.d(",,,",viewModel.mode.toString())
        }
        fun changexy(x:Float,y:Float){
            this.x-=x
            this.y-=y
        }
    }
    inner class simpleOnGestureListener: GestureDetector.SimpleOnGestureListener(){
        var x=0f
        var y=0f
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            x+=distanceX
            y+=distanceY
            Log.d("...",x.toString()+y.toString())
            if(x<=0&&y<=0)
            {
                if (-x<=maxleft&&-y<=maxtop)
                    viewHolder!!.viewdrag(x, y)
                else
                {
                    if(-x>maxleft)
                        x=-maxleft
                    if(-y>maxtop)
                        y=-maxtop
                }
            }
            if(x>0&&y>0)
                if(x<=maxright&&y<=maxbottom)
                    viewHolder!!.viewdrag(x, y)
                else
                {
                    if(x>maxright)
                        x=maxright
                    if(y>maxbottom)
                        y=maxbottom
                }
            if(x>0&&y<=0)
                if(x<=maxright&&-y<=maxtop)
                    viewHolder!!.viewdrag(x, y)
                else
                {
                    if(x>maxright)
                        x=maxright
                    if(-y>maxtop)
                        y=-maxtop
                }
            if(x<=0&&y>0)
                if(-x<=maxleft&&y<=maxbottom)
                    viewHolder!!.viewdrag(x, y)
                else
                {
                    if(-x>maxleft)
                        x=-maxleft
                    if(y>maxbottom)
                        y=maxbottom
                }

            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }
    inner class onpagechangecallback(): ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
                viewHolder=(binding.pager.get(0) as RecyclerView).findViewHolderForAdapterPosition(position) as ViewpageViewHolder
            Log.d("po",viewHolder.toString())
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            Log.d("asdpo",position.toString())
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }
}