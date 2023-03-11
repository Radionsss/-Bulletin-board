package com.example.bulletinboardtwo.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.example.bulletinboardtwo.adapters.ImageAdapter
import com.example.bulletinboardtwo.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImageManager {
    private const val MAX_IMAGE_SIZE =1000

    fun getImageSize(uri: Uri,act:Activity): List<Int> {
        val inputStream=act.contentResolver.openInputStream(uri)
       /* val fileTemp=File(act.cacheDir,"temp.tmp")
        if (inputStream != null) {
            fileTemp.copyInputStreamToFile(inputStream)
        }*/
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream,null, options)
        return listOf(options.outWidth, options.outHeight)
    }

   /*private fun imageRotation(imageFile: File): Int {
        val rotation: Int
        val exif = ExifInterface(imageFile.absoluteFile)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        rotation = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                90
            } else {
                0
            }
        return rotation
    }

    private fun File.copyInputStreamToFile(inputStream:InputStream){
        this.outputStream().use {
            out->
            inputStream.copyTo(out)
        }
    }*/

    fun chooseScaleType(im:ImageView,bitmap: Bitmap){
        if(bitmap.width>bitmap.height){
            im.scaleType=ImageView.ScaleType.CENTER_CROP
        }else{
            im.scaleType=ImageView.ScaleType.CENTER_INSIDE
        }

    }

  suspend fun imageResize(uris: ArrayList<Uri>,act: Activity): List<Bitmap> = withContext(Dispatchers.IO) {
      val tempList = ArrayList<List<Int>>()
      val bitMapList = ArrayList<Bitmap>()
      for (i in uris.indices) {
          val size = getImageSize(uris[i],act)
          val imageRatio = size[0].toFloat() / size[1].toFloat()//0 width

          if (imageRatio > 1) {
              if (size[0] > MAX_IMAGE_SIZE) {//В горизонтальном
                  tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
              } else {
                  tempList.add(listOf(size[0], size[1]))
              }
          } else {
              if (size[1] > MAX_IMAGE_SIZE) {
                  tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
              } else {
                  tempList.add(listOf(size[0], size[1]))
              }
          }
      }

      for(i in uris.indices) {

          /*val e =*/ kotlin.runCatching {
              bitMapList.add(Picasso.get().load(uris[i]).resize(tempList[i][0],tempList[i][1]).get())
          }
        //  Log.d("MyLog", "bitmap ${e.isSuccess}")

      }

      return@withContext bitMapList
  }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitMapList = ArrayList<Bitmap>()
        for (i in uris.indices) {
            kotlin.runCatching {
                bitMapList.add(Picasso.get().load(uris[i]).get())
            }
        }

        return@withContext bitMapList
    }

     fun fillImageArray(ad: Ad,adapter: ImageAdapter){
        val listUris= listOf(ad.mainImage,ad.image2,ad.image3)
        CoroutineScope(Dispatchers.Main).launch{
            val bitmapList=getBitmapFromUris(listUris)
            adapter.updateAdapter(bitmapList as ArrayList<Bitmap>)
        }
    }
}