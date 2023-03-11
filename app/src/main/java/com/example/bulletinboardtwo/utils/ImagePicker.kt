package com.example.bulletinboardtwo.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePicker {
    const val REQUEST_CODE_IMAGES_PICKER = 1234
    const val REQUEST_CODE_SINGLE_IMAGE_PICKER = 123
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode=Mode.Picture
            path="/pix/images"
        }
        return options
    }

    fun getMultiImages(edAct: EditAdsAct, imageCount: Int) {
        edAct.addPixToActivity(R.id.place_holder/*pix экран покрывает все act*/, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct,result.data)
                } //use results as it.data
                PixEventCallback.Status.BACK_PRESSED -> {

                }
            }
        }
    }

    fun addImages(edAct: EditAdsAct, imageCount: Int) {
        edAct.addPixToActivity(R.id.place_holder/*pix экран покрывает все act*/, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>,edAct)
                } //use results as it.data
                PixEventCallback.Status.BACK_PRESSED -> {}
            }
        }
    }


    fun getSingleImage(edAct: EditAdsAct) {
        edAct.addPixToActivity(R.id.place_holder/*pix экран покрывает все act*/, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                  singleImage(edAct,result.data[0])
                } //use results as it.data
                PixEventCallback.Status.BACK_PRESSED -> {

                }
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsAct){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder,edAct.chooseImageFrag!!).commit()
    }

    private fun closePixFrag(edAct: EditAdsAct) {
        val frgList = edAct.supportFragmentManager.fragments
        frgList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it)
                .commit()     //удаляем видемый frag
        }
    }

   private fun getMultiSelectImages(edAct: EditAdsAct, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImageFrag == null) {
            edAct.openSelectedImageFragment(uris as ArrayList<Uri>)
        }  else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            edAct.job = CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.imageSinglePBar.visibility = View.VISIBLE
                val bitmapList = ImageManager.imageResize(uris as ArrayList<Uri>,edAct) as ArrayList<Bitmap>
                edAct.binding.imageSinglePBar.visibility = View.GONE
                edAct.imageAdapter.updateAdapter(bitmapList)
                closePixFrag(edAct)
            }
            //imageAdapter.updateAdapter(returnValues)
        }
    }


    fun singleImage(edAct: EditAdsAct,uri:Uri) {
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)


    }
}
    /*fun showSelectedImage(requestCode: Int, resultCode: Int, data: Intent?,editAct:EditAdsAct){
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_IMAGES_PICKER) {

            if (data != null) {

                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)

                if (returnValues?.size!! > 1 && editAct.chooseImageFrag == null) {

                    editAct.openSelectedImageFragment(returnValues)

                }else if (returnValues.size == 1 && editAct.chooseImageFrag == null) {

                    *//*editAct.job =*//* CoroutineScope(Dispatchers.Main).launch {
                        editAct.binding.imageSinglePBar.visibility=View.VISIBLE
                        val bitmapList = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        editAct.binding.imageSinglePBar.visibility=View.GONE
                        editAct.imageAdapter.updateAdapter(bitmapList)
                    }
                    //imageAdapter.updateAdapter(returnValues)

                } else if (editAct.chooseImageFrag != null) {

                    editAct. chooseImageFrag?.updateAdapter(returnValues)

                }
            }
        }else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_SINGLE_IMAGE_PICKER) {
            if (data != null) {

                val uris = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                editAct.chooseImageFrag?.setSingleImage(uris?.get(0)!!,editAct.editImagePos)
            }
        }
    }*/
