package com.example.bulletinboardtwo.act


import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletinboardtwo.MainActivity
import com.example.bulletinboardtwo.fragment.FragmentCloseInterface
import com.example.bulletinboardtwo.utils.CityHelper
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.adapters.ImageAdapter
import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.model.DbManager
import com.example.bulletinboardtwo.databinding.ActivityEditAdsBinding
import com.example.bulletinboardtwo.dialogs.DialogSpinnerHelper
import com.example.bulletinboardtwo.fragment.ImageListFragment
import com.example.bulletinboardtwo.utils.ImageManager
import com.example.bulletinboardtwo.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout.LengthCounter
import kotlinx.coroutines.Job
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFragment? = null
    private val dialog = DialogSpinnerHelper()
    lateinit var binding: ActivityEditAdsBinding
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var job: Job? = null

    var editImagePos = 0
    var imageIndex = 0
    private var isEditState=false
    private var ad:Ad?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()
    }



    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter

    }


    /*fill ads*/ /*fill ads*/ /*fill ads*/ /*fill ads*/ /*fill ads*/
    private fun checkEditState(){
        if (isEditState()){
            isEditState=true
            ad=intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if(ad!=null)fillViews(ad!!)
        }
    }

    private fun isEditState():Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE,false)
    }

    private fun fillViews(ad:Ad)= with (binding){
        tvSelectCountry.text=ad.country
        tvSelectCity.text=ad.city
        edTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked= ad.withSend.toBoolean()
        tvCategory.text=ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad,imageAdapter)

    }

    /*fill ads*/ /*fill ads*/ /*fill ads*/ /*fill ads*/ /*fill ads*/
    override fun onStop() {
        super.onStop()
        job?.cancel() // остановить фоновые операции
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_IMAGES_PICKER)
                } else {
                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()

                }
                return
            }
        }
    }*/

    override fun onFragmentClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.updateAdapter(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    fun openSelectedImageFragment(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFragment(this)
        if (newList != null)chooseImageFrag?.resizeSelectedImage(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }


    private fun prepareImageByteArray(bitmap: Bitmap):ByteArray{
        val outputStream=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,outputStream)
        return outputStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageWrite = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageWrite.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageWrite.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray,uri: String, listener: OnCompleteListener<Uri>) {
        val imStorageWrite = dbManager.dbStorage.storage.getReferenceFromUrl(uri)
        val upTask = imStorageWrite.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageWrite.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl)
            .delete().addOnCompleteListener(listener)
    }

    private fun uploadImages() {
        if (imageIndex == 3) {// if (imageAdapter.mainArray.size == imageIndex) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()

        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
                    nextImage(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    fun onClickPublish(view: View) {
        ad = fillAd()
        if (checkOnClickPublish()) {
            toastShow("Все выделеные поля должны быть заполнены")
            return
        }
        binding.publishProgressBar.visibility = View.VISIBLE
        binding.tvWait.visibility = View.VISIBLE
        binding.scrollViewMain.visibility = View.GONE

        uploadImages()
    }

    private fun getUrlFromAd():String{
        return listOf(ad?.mainImage!!,ad?.image2!!,ad?.image3!!)[imageIndex]
    }

    private fun nextImage(uri: String){
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String){
        when(imageIndex){
            0->ad=ad?.copy(mainImage = uri)
            1->ad=ad?.copy(image2 = uri)
            2->ad=ad?.copy(image3 = uri)
        }
    }
    //onClicks

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3 )
        } else {
            openSelectedImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    private fun checkOnClickPublish():Boolean = with(binding){
       return@with tvSelectCountry.text==getString(R.string.select_country)
               ||tvSelectCountry.text==getString(R.string.select_city)
               ||tvSelectCategory.text==getString(R.string.hint_category)
               ||edTel.text.isEmpty()
               ||edIndex.text.isEmpty()
               ||edDescription.text.isEmpty()
               ||edPrice.text.isEmpty()
               ||edTitle.text.isEmpty()

    }

    private fun onPublishFinish():DbManager.FinishWorkListener{
        return object :DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }
        }
    }

    private fun fillAd(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp= Ad(
                tvSelectCountry.text.toString(),
                tvSelectCity.text.toString(),
                edTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvSelectCategory.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                edEmail.text.toString(),
                ad?.mainImage?:"empty",
                ad?.image2?:"empty",
                ad?.image3?:"empty",
                ad?.key?: dbManager.db.push().key,
                "0",
                dbManager.auth.uid,
                ad?.time?:System.currentTimeMillis().toString())
        }
        return adTemp
    }

    fun onClickSelectCountry(view: View) {
        val list = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, list, binding.tvSelectCountry)
        if (binding.tvSelectCity.text.toString() != getString(R.string.select_city)) {
            binding.tvSelectCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = binding.tvSelectCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val list = CityHelper.getAllCity(selectedCountry, this)
            dialog.showSpinnerDialog(this, list, binding.tvSelectCity)
        } else {
            Toast.makeText(
                this,
                getString(R.string.no_select_city),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onClickSelectCategory(view: View) {
        val list = resources.getStringArray(R.array.category_name).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, list, binding.tvSelectCategory)
    }


    private fun imageChangeCounter(){
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        var index = 1
        if (counter == 0) index = 0
        val imageCount = "${counter + index}/${binding.vpImages.adapter?.itemCount}"
        binding.tvImageCounter.text = imageCount
    }

    private fun toastShow(text:String){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}