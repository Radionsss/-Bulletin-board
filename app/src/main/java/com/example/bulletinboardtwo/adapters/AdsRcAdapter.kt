package com.example.bulletinboardtwo.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardtwo.MainActivity
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.act.DescriptionActivity
import com.example.bulletinboardtwo.act.EditAdsAct
import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.databinding.AdListItemBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdsHolder>() {
    val adArray = ArrayList<Ad>()
    private var timeFormatter: SimpleDateFormat? =null

    init {
        timeFormatter=SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsHolder {
        val viewBinding =
            AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdsHolder(viewBinding,  act,timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdsHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    class AdsHolder(
        val binding: AdListItemBinding,
        val act: MainActivity,
        val timeFormatter: SimpleDateFormat?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) {
            binding.apply {
                tvDescription.text = ad.description
                tvPrice.text = ad.price
                tvTitle.text = ad.title

                tvPublishTime.text=  getTimeFromMillis(ad.time!!)

                tvViewCounter.text=ad.viewCounter
                tvFavCounter.text=ad.favCounter

                Picasso.get().load(ad.mainImage).into(mainImage)

                isFav(ad)

                showEditPanel(isOwner(ad))

                itemView.setOnClickListener {
                    act.onAdViewed(ad)
                    val i=Intent(binding.root.context,DescriptionActivity::class.java)
                    i.putExtra(DescriptionActivity.AD,ad)
                    binding.root.context.startActivity(i)
                }

                btnDeleteAd.setOnClickListener{
                    act.onDeleteAd(ad)
                }
                btnEditAd.setOnClickListener (onClickEdit(ad))

                imBtnFav.setOnClickListener {
                    if(act.myAuth.currentUser?.isAnonymous==false) act.onFavAd(ad)
                }
            }
        }

        private fun getTimeFromMillis(millis:String):String {
            val c=Calendar.getInstance()
            c.timeInMillis = millis.toLong()
            return timeFormatter?.format(c.time)!!
        }

        private fun isFav(ad:Ad,)= with(binding){
            if(ad.isFav){
                imBtnFav.setImageResource(R.drawable.ic_favorite_presed)
            }else{
                imBtnFav.setImageResource(R.drawable.ic_favorite_normal)
            }
        }

        private fun onClickEdit(ad: Ad): View.OnClickListener {
            return View.OnClickListener {
                val editAdsIntent=Intent(act,EditAdsAct::class.java).apply{
                    putExtra(MainActivity.EDIT_STATE,true)
                    putExtra(MainActivity.ADS_DATA,ad)
                }
                act.startActivity(editAdsIntent)
            }
        }

        private fun isOwner( ad: Ad): Boolean {
            return ad.uid == act.myAuth.uid
        }
        private fun showEditPanel(isOwner: Boolean){
            if(isOwner){
                binding.editPanel.visibility= View.VISIBLE
            }else{
                binding.editPanel.visibility= View.GONE
            }
        }
    }


    fun updateAdapter(newList: List<Ad>) {
        val adsListTemp=ArrayList<Ad>()//чтобы Diff не пересоздовал array а чтоб он видел что к marr добавляется new element
        adsListTemp.addAll(adArray)
        adsListTemp.addAll(newList)
        val difResult=DiffUtil.calculateDiff(DiffUtilHelper(adArray,adsListTemp))
        difResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(adsListTemp)

    }

    fun updateAdapterWithClear(newList: List<Ad>) {
        val difResult=DiffUtil.calculateDiff(DiffUtilHelper(adArray,newList))
        difResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)

    }

    interface AdSetListener{
        fun onDeleteAd(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavAd(ad: Ad)
    }
}