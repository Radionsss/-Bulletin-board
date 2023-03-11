package com.example.bulletinboardtwo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManage = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    fun loadAllAdsFirstPage(filter: String) {
        dbManage.getAllAdsFirstPage(filter,object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }
    fun loadAllAdsNextPage(time:String,filter: String) {
        dbManage.getAllAdsNextPage(time,filter,object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatFirstPage(cat:String,filter: String) {
        dbManage.getAllAdsFromCatFirstPage(cat,filter,object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(cat: String,time: String,filter: String) {
        dbManage.getAllAdsFromCatNextPage(cat,time,filter,object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAds() {
        dbManage.getMyAds(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavAds() {
        dbManage.getMyFavAds(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun adViewed(ad:Ad){
        dbManage.adViewed(ad)
    }

    fun onFavAd(ad:Ad){
        dbManage.onFavClick(ad,object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val  updatedList=liveAdsData.value
                val pos=updatedList?.indexOf(ad)
                if(pos!=-1) {
                    pos?.let {
                        val favCounter= if(ad.isFav) ad.favCounter?.toInt()?.minus(1) else ad.favCounter?.toInt()?.plus(1)
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updatedList)
            }
        })
    }

    fun deleteAd(ad: Ad) {
        dbManage.deleteAd(ad,object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val  updatedList=liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }
}