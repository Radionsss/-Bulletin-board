package com.example.bulletinboardtwo.model

import com.example.bulletinboardtwo.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager/*(*//*val readDataCallback: ReadDataCallback?*//**//*Это для null*//*)*/ {
    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) {
            db.child(ad.key ?: "empty").child(auth.uid!!)
                .child(AD_NODE)
                .setValue(ad).addOnCompleteListener {
                    val adFilter =FilterManager.createFilter(ad)
                    db.child(ad.key ?: "empty")
                        .child(FILTER_NODE)
                        .setValue(adFilter).addOnCompleteListener {
                            finishWorkListener.onFinish()
                        }
                }
        }/*else{
            Toast.makeText(
                re,
                "Сначала войдите в аккаунт",
                Toast.LENGTH_SHORT
            ).show()
        }*/
    }
    fun adViewed(ad: Ad) {
        var counter=ad.viewCounter!!.toInt()

        counter++

        if (auth.uid != null)
            db.child(ad.key ?: "empty")
                .child(INFO_NODE)
                .setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter))

    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyFavAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FAVS_NODE/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }
/*firstPage*//*firstPage*//*firstPage*//*firstPage*/
    fun getAllAdsFirstPage(filter: String,readDataCallback: ReadDataCallback?) {
        val query = if(filter.isEmpty()){
            db.orderByChild("/$FILTER_NODE/time").limitToLast(LIMIT_AD)
        }else{
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallback)
    }

   private fun getAllAdsByFilterFirstPage(tempFilter: String): Query {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        return db.orderByChild("/$FILTER_NODE/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(LIMIT_AD)
    }
/*firstPage*//*firstPage*//*firstPage*//*firstPage*/

    /*NextPage*//*NextPage*//*NextPage*//*NextPage*//*NextPage*//*NextPage*/
    fun getAllAdsNextPage(time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/$FILTER_NODE/time").endBefore(time).limitToLast(LIMIT_AD)
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsByFilterNextPage(filter, time, readDataCallback)
        }
    }

    private fun getAllAdsByFilterNextPage(tempFilter: String, time:String,readDataCallback: ReadDataCallback?){
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
       val query= db.orderByChild("/$FILTER_NODE/$orderBy")
            .endBefore(filter + "_$time").limitToLast(LIMIT_AD)
        readNextPageDataFromDb(query,filter,orderBy,readDataCallback)
    }
    /*NextPage*//*NextPage*//*NextPage*//*NextPage*//*NextPage*//*NextPage*/

    /*firstPageCategory*//*firstPageCategory*//*firstPageCategory*//*firstPageCategory*/
    fun getAllAdsFromCatFirstPage(cat: String,filter: String, readDataCallback: ReadDataCallback?) {
        val query = if(filter.isEmpty()){
            db.orderByChild("/$FILTER_NODE/catTime")
                .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(LIMIT_AD)
        }else{
            getAllAdsFromCatByFilterFirstPage(cat,filter)
        }
        readDataFromDb(query, readDataCallback)
    }

   private fun getAllAdsFromCatByFilterFirstPage(cat: String, tempFilter: String): Query {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        return db.orderByChild("/$FILTER_NODE/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(LIMIT_AD)/////
    }
    /*firstPageCategory*//*firstPageCategory*//*firstPageCategory*//*firstPageCategory*//*firstPageCategory*/


    fun getAllAdsFromCatNextPage(cat: String, time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/$FILTER_NODE/catTime")
                .endBefore(cat+"_"+time).limitToLast(LIMIT_AD)
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readDataCallback)
        }
    }

    private fun getAllAdsFromCatByFilterNextPage(cat: String,time:String , tempFilter: String,readDataCallback: ReadDataCallback?) {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        val query = db.orderByChild("/$FILTER_NODE/$orderBy")
            .endBefore(filter+"_"+time).limitToLast(LIMIT_AD)

        readNextPageDataFromDb(query,filter,orderBy,readDataCallback)
    }

/*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*favs*/
   private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            ad.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NODE)
                    .child(uid)
                    .setValue(uid).addOnCompleteListener {
                        if(it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener){
        if(ad.isFav){
            removeFavs(ad,listener)
        }else{
            addToFavs(ad,listener)
        }
    }

    private fun removeFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            ad.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NODE)
                    .child(uid)
                    .removeValue().addOnCompleteListener {
                        if(it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }
    /*favs*//*favs*//*favs*//*favs*//*favs*//*favs*//*faves*//*favs*//*favs*//*favs*//*favs*/
    fun deleteAd(ad: Ad,listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        val map = mapOf(
            "/$FILTER_NODE" to null,
            "/$FAVS_NODE" to null,
            "/$INFO_NODE" to null,
            "/${ad.uid}" to null,
        )
        db.child(ad.key).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) deleteImageFromStorage(ad,0,listener)
        }
    }

    private fun deleteImageFromStorage(ad: Ad, index: Int, listener: FinishWorkListener) {
        val listImage = listOf(ad.mainImage, ad.image2, ad.image3)
        if(ad.mainImage == "empty") listener.onFinish()
        dbStorage.storage.getReferenceFromUrl(listImage[index]).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                if (listImage.size > index + 1) {
                    if (listImage[index + 1] != "empty") {
                        deleteImageFromStorage(ad, index + 1, listener)
                    } else {
                        listener.onFinish()
                    }
                } else {
                    listener.onFinish()
                }
            }
        }
    }

    private fun readDataFromDb(query: Query,readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArrayTemp = ArrayList<Ad>()

                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if(ad==null) ad=it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem=item.child(INFO_NODE).getValue(InfoItem::class.java)

                    val favCounter=item.child(FAVS_NODE).childrenCount
                    val isFav= auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    ad?.isFav=isFav!=null
                    ad?.favCounter= favCounter.toString()


                    ad?.viewCounter=infoItem?.viewsCounter?:"0"
                    ad?.emailsCounter=infoItem?.emailsCounter?:"0"
                    ad?.callsCounter=infoItem?.callsCounter?:"0"

                    if (ad != null) adArrayTemp.add(ad!!)
                }
                readDataCallback?.readData(adArrayTemp)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun readNextPageDataFromDb(query: Query,filter: String ,orderBy:String, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArrayTemp = ArrayList<Ad>()

                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if(ad==null) ad=it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem=item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeItem=item.child(FILTER_NODE).child(orderBy).value.toString()

                    val favCounter=item.child(FAVS_NODE).childrenCount
                    val isFav= auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    ad?.isFav=isFav!=null
                    ad?.favCounter= favCounter.toString()


                    ad?.viewCounter=infoItem?.viewsCounter?:"0"
                    ad?.emailsCounter=infoItem?.emailsCounter?:"0"
                    ad?.callsCounter=infoItem?.callsCounter?:"0"

                    if (ad != null && filterNodeItem.startsWith(filter)) adArrayTemp.add(ad!!)
                }
                readDataCallback?.readData(adArrayTemp)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }
    interface FinishWorkListener {
        fun onFinish()
    }

    companion object{
        const val AD_NODE="ad"
        const val INFO_NODE="info"
        const val MAIN_NODE="main"
        const val FAVS_NODE="favs"
        const val FILTER_NODE="adFilter"
        const val LIMIT_AD=2
    }
}