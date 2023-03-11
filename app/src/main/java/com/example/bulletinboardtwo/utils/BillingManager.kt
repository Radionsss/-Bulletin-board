package com.example.bulletinboardtwo.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class BillingManager(val act:AppCompatActivity) {
    private var billingClient:BillingClient? = null

    init {
        setUpBillingClient()
    }

    fun starConnection(){
        billingClient?.startConnection(object : BillingClientStateListener{
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
            }

        })
    }
    fun closeConnection(){
        billingClient?.endConnection()

    }

    private fun getItem(){
        val skuList=ArrayList<String>()
        skuList.add(REMOVE_ADS)
        val scuDetails=SkuDetailsParams.newBuilder()
        scuDetails.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient?.querySkuDetailsAsync(scuDetails.build()){
                result,list->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if(!list.isNullOrEmpty()){
                        val billingFlowParams=BillingFlowParams
                            .newBuilder().setSkuDetails(list[0]).build()
                        billingClient?.launchBillingFlow(act,billingFlowParams)
                    }
                }
            }
        }
    }


    private fun setUpBillingClient(){
        billingClient=BillingClient.newBuilder(act).setListener(getPurchaseListener())
            .enablePendingPurchases().build()
    }

    private fun getPurchaseListener():PurchasesUpdatedListener{
        return PurchasesUpdatedListener{
            result,list->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list?.get(0)?.let { nonConsumableItem(it) }
                }
            }
        }
    }

    private fun nonConsumableItem(purchase: Purchase){
        if(purchase.purchaseState==Purchase.PurchaseState.PURCHASED){
            if(!purchase.isAcknowledged){
                val acParams=AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acParams){result->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        savePurchase(true)
                        Toast.makeText(act,"Спасибо за покупку!", Toast.LENGTH_SHORT).show()
                    }else{
                        savePurchase(false)
                        Toast.makeText(act,"Не удалось реализовать покупку!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun savePurchase(isPurchased: Boolean){
        val pref=act.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE)
        val editor= pref.edit()
        editor.putBoolean(REMOVE_ADS_PREF,isPurchased)
        editor.apply()
    }
    companion object{
        const val REMOVE_ADS="remove_ads"
        const val REMOVE_ADS_PREF="remove_ads_pref"
        const val MAIN_PREF="main_pref"
    }
}
