package com.example.bulletinboardtwo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardtwo.accounthelper.AccountHelper
import com.example.bulletinboardtwo.act.EditAdsAct
import com.example.bulletinboardtwo.act.FilterActivity
import com.example.bulletinboardtwo.adapters.AdsRcAdapter
import com.example.bulletinboardtwo.dialoghelper.DialogConst
import com.example.bulletinboardtwo.dialoghelper.DialogHelper
import com.example.bulletinboardtwo.databinding.ActivityMainBinding
import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.utils.BillingManager
import com.example.bulletinboardtwo.utils.FilterManager
import com.example.bulletinboardtwo.viewmodel.FirebaseViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener/*,ReadDataCallback*/,AdsRcAdapter.AdSetListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private val dialogHelper = DialogHelper(this)
    val myAuth = FirebaseAuth.getInstance()

    private val adsRcAdapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate:Boolean = true

    private var currentCat:String? = null
    private var filter:String = "empty"
    private var filterDb:String = ""
    private var pref: SharedPreferences? = null
    private var isPremiumUser = false
    private var billingManager: BillingManager? =null


    private lateinit var lunchFilter:ActivityResultLauncher<Intent>

    var lunchActGoogle= registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            Log.d("MyLog", "Api 0")
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Log.d("MyLog", "Api error ${e.message}")
        }
    }

            override fun    onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)
                pref=getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE)
                isPremiumUser=pref?.getBoolean(BillingManager.REMOVE_ADS_PREF,false,)!!
                if(!isPremiumUser) {
                    initAds()
                }else{
                    binding.included.adViewMain.visibility=View.GONE
                }


                init()
                // dbManager.readDataFromDb()
                initRecyclerView()
                initViewModel()
                bottomMenuOnCLick()
                scrollListener()
                onActivityResultFilter()
            }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    private fun initRecyclerView() {
        binding.included.rcView.layoutManager= LinearLayoutManager(this@MainActivity)
        binding.included.rcView.adapter = adsRcAdapter
    }


  /*  override fun readData(list: List<Ad>) {
        adsRcAdapter.updateAdapter(list)
    }*/


    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            val list=getAdsCat(it)
            if (!clearUpdate) {
                adsRcAdapter.updateAdapter(list)
            } else {
                adsRcAdapter.updateAdapterWithClear(list)
            }

            binding.included.tvEmpty.visibility=if(adsRcAdapter.adArray.size==0) View.VISIBLE else View.GONE
            if(binding.included.toolbar.title==getString(R.string.ad_my_ads)){
             visibleOrGone(it)
            }
            binding.included.imAddAd.setOnClickListener {
                val intent = Intent(this@MainActivity, EditAdsAct::class.java)
                startActivity(intent)}
        }
    }

    private fun visibleOrGone(it:ArrayList<Ad>){
        binding.included.imAddAd.visibility=if(it.isEmpty()) View.VISIBLE else View.GONE
        binding.included.tvAdd.visibility=if(it.isEmpty()) View.VISIBLE else View.GONE
        binding.included.tvAd.visibility=if(it.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onActivityResultFilter(){
        lunchFilter =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                    //     Log.d(MY_LOG, "Filter: $filter")
                    //   Log.d(MY_LOG, "getFilter: ${FilterManager.getFilter(filter)}")
                    filterDb = FilterManager.getFilter(filter)
                } else if (it.resultCode == RESULT_CANCELED) {
                    filter = "empty"
                    filterDb = ""
                }
            }
    }

    private fun getAdsCat(list:ArrayList<Ad>):ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (currentCat != getString(R.string.ad_default)) {
            tempList.clear()
            list.forEach {
                if (currentCat==it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }
    private fun init() {//Drawer menu open
        currentCat=getString(R.string.ad_default)
        setSupportActionBar(binding.included.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.included.toolbar, R.string.open, R.string.close)
        setNavView()
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tv_acc)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.im_account)

    }

    private fun bottomMenuOnCLick() = with(binding) {
        included.btnNavView.setOnItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_ad -> {
                    if (myAuth.currentUser != null) {
                        if (!myAuth.currentUser?.isAnonymous!!) {
                            val intent = Intent(this@MainActivity, EditAdsAct::class.java)
                            startActivity(intent)
                            true
                        } else {
                            toastShow("Сначал зарегестрируйтесь")
                            true
                        }
                    } else {
                        toastShow("Error")
                        true
                    }
                }
                R.id.id_home -> {
                    currentCat = getString(R.string.ad_default)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
                    included.toolbar.title=getString(R.string.ad_default)
                    true
                }
                R.id.id_favorites_ad -> {
                    firebaseViewModel.loadMyFavAds()
                    included.toolbar.title=getString(R.string.favorites_ad)
                    true
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    included.toolbar.title=getString(R.string.ad_my_ads)
                    true
                }
                else -> {false}
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.id_filter_ad){
            val i=Intent(this, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY,filter)
            }
            lunchFilter.launch(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate=true
        when (item.itemId) {
            R.id.id_my_ads -> {
                firebaseViewModel.loadMyAds()
                binding.included.toolbar.title=getString(R.string.ad_my_ads)
            }
            R.id.id_car -> {
                getAdFromCat(getString(R.string.ad_car))
                binding.included.toolbar.title=getString(R.string.ad_car)
            }
            R.id.id_pc -> {
                getAdFromCat(getString(R.string.ad_pc))
                binding.included.toolbar.title=getString(R.string.ad_pc)
            }
            R.id.id_sp -> {
                getAdFromCat(getString(R.string.ad_smartphone))
                binding.included.toolbar.title=getString(R.string.ad_smartphone)
            }
            R.id.id_dm -> {
                getAdFromCat(getString(R.string.ad_dm))
                binding.included.toolbar.title=getString(R.string.ad_dm)
            }
            R.id.id_remove_ads -> {
                billingManager=BillingManager(this)
                billingManager?.starConnection()
            }
            R.id.id_my_sign_up -> {
               dialogHelper.createSignDialog(DialogConst.SIGN_UP_INDEX)
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_INDEX)
            }
            R.id.id_sign_out -> {
                if(myAuth.currentUser?.isAnonymous==true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                myAuth.signOut()
                dialogHelper.accountHelper.signOutGoogle()

            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdFromCat(cat:String){
        currentCat=cat
       // val catTime="${cat}_0"
        firebaseViewModel.loadAllAdsFromCatFirstPage(cat,filterDb)
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accountHelper.signInAnonymously(object : AccountHelper.CompleteListener {
                override fun onComplete() {
                    tvAccount.text = getString(R.string.guest_sign_in)
                    imAccount.setImageResource(R.drawable.ic_account_0)
                }
            })
            resources.getString(R.string.not_reg)
        } else if (user.isAnonymous) {
            tvAccount.text = getString(R.string.guest_sign_in)
            imAccount.setImageResource(R.drawable.ic_account_0)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }


    private fun scrollListener() = with(binding.included) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN)
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    clearUpdate=false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList:ArrayList<Ad>) {
        adsList[0].let {
            if (currentCat==getString(R.string.ad_default)){
                it.time?.let { it1 -> firebaseViewModel.loadAllAdsNextPage(it1,filterDb) }
            }else{
             //    val catTime="${it.category}_${it.time}"
                firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!,it.time!!,filterDb)
            }
        }
    }

    private fun initAds() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.included.adViewMain.loadAd(adRequest)
    }

    companion object{
        const val EDIT_STATE="editState"
        const val ADS_DATA="adsData"
        const val SCROLL_DOWN=1
        const val MY_LOG="MyLog"
    }
    @SuppressLint("SuspiciousIndentation")
    private fun setNavView() = with(binding) {
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.ads_cat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.red)),
            0, adsCat.title!!.length, 0
        )

        adsCat.title= spanAdsCat

        val accCat = menu.findItem(R.id.acc_cat)
        val spanAccCat = SpannableString(accCat.title)
            spanAccCat.setSpan(ForegroundColorSpan(
                    ContextCompat.getColor(this@MainActivity, R.color.red)),
                0,  accCat.title!!.length, 0)

        accCat.title=   spanAccCat
    }

    override fun onDeleteAd(ad: Ad) {
        firebaseViewModel.deleteAd(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
    }

    override fun onFavAd(ad: Ad) {
        firebaseViewModel.onFavAd(ad)
    }

    override fun onResume() {
        super.onResume()
        binding.included.btnNavView.selectedItemId=R.id.id_home
        binding.included.adViewMain.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.included.adViewMain.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.included.adViewMain.destroy()
        billingManager?.closeConnection()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun toastShow(text:String){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}