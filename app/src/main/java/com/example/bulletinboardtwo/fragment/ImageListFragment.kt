package com.example.bulletinboardtwo.fragment

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.act.EditAdsAct
import com.example.bulletinboardtwo.databinding.ListImageFragmentBinding
import com.example.bulletinboardtwo.dialoghelper.ProgressDialog
import com.example.bulletinboardtwo.utils.AdapterCallback
import com.example.bulletinboardtwo.utils.ImageManager
import com.example.bulletinboardtwo.utils.ImagePicker
import com.example.bulletinboardtwo.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFragment(private val onFragmentClose: FragmentCloseInterface) : BaseAdsFrag(),AdapterCallback { //newList get сыллки
    val adapter = SelectImageRvAdapter(this)
    private var job: Job?=null
    private var addItem: MenuItem?=null
    private val dragCallback=ItemTouchMoveCallback(adapter)
    val touchHelper=ItemTouchHelper(dragCallback)
    private lateinit var binding: ListImageFragmentBinding

    override fun onItemDelete() {
        addItem?.isVisible=true
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFragment)?.commit()
        onFragmentClose.onFragmentClose(adapter.mainArray)
        job?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=ListImageFragmentBinding.inflate(layoutInflater,container,false)
        adView=binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//get all painted items
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()


        binding.apply {

            touchHelper.attachToRecyclerView(rcViewSelectImage)//с каким rc будем работать
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter

        }
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }


    private fun setUpToolbar() {
        binding.apply {
            toolbarFragment.inflateMenu(R.menu.menu_choose_image)

            val deleteItem = toolbarFragment.menu.findItem(R.id.id_delete_image)
            addItem = toolbarFragment.menu.findItem(R.id.id_add_image)
            if( adapter.mainArray.size>2) addItem?.isVisible=false

            toolbarFragment.setNavigationOnClickListener {
                showInterAd()
            }

            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addItem?.isVisible = true
                true
            }

            addItem?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.addImages(activity as EditAdsAct,imageCount)
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>,activity: Activity) {
       resizeSelectedImage(newList, false, activity)
    }

    fun setSingleImage(uri: Uri, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitMapList = ImageManager.imageResize(arrayListOf(uri),activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitMapList[0]
            adapter.notifyItemChanged(pos)

        }
    }
     fun resizeSelectedImage(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {
        job = CoroutineScope(Dispatchers.Main).launch {

            val dialog = ProgressDialog.createProgressDialog(activity )
            val bitMapList = ImageManager.imageResize(newList,activity )
            dialog.dismiss()
            adapter.updateAdapter(bitMapList, needClear)

            if( adapter.mainArray.size>2) addItem?.isVisible=false
        }
    }
}