package com.example.bulletinboardtwo.act

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.bulletinboardtwo.adapters.ImageAdapter
import com.example.bulletinboardtwo.databinding.ActivityDescriptionBinding
import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDescriptionBinding
    private lateinit var adapter: ImageAdapter
    private var ad: Ad? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fbCall.setOnClickListener { call() }
        binding.fbSendMassage.setOnClickListener { sendEmail() }

        init()
    }

    private fun init(){
        adapter= ImageAdapter()
          binding.viewPager.adapter=adapter
        getIntentFromMainAct()
        imageCounter()
    }

    private fun getIntentFromMainAct(){
        ad=intent.getSerializableExtra(AD) as Ad
        if(ad!=null)updateUI(ad!!)
    }

    private fun updateUI(ad:Ad){
        ImageManager.fillImageArray(ad,adapter)
        fillTextView(ad)
    }

    private fun fillTextView(ad:Ad)= with(binding){
        tvTitle.text=ad.title
        tvDescription.text=ad.description
        tvEmail.text=ad.email
        tvPrice.text=ad.price
        tvTel.text=ad.tel
        tvCountry.text=ad.country
        tvCity.text=ad.city 
        tvIndex.text=ad.index
        tvSend.text=isWithSend(ad.withSend.toBoolean())

    }

    private fun isWithSend(withSend: Boolean): String {
        return if (withSend) {
             "Yes"
        } else {
            "No"
        }
    }

    private fun call(){
        val callUri="tel:${ad?.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data=callUri.toUri()
        startActivity(iCall)
    }

    private fun sendEmail(){

        val iEmail = Intent(Intent.ACTION_SEND)
        iEmail.type="massage/rfc822"
        iEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Объявлениу")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление")
        }
        startActivity(Intent.createChooser(iEmail,"Открыть с "))
        startActivity(iEmail)
    }

    private fun imageCounter(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCountText="${position +1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text=imageCountText
            }
        })
    }

    companion object{
        const val AD="ad"
    }

}