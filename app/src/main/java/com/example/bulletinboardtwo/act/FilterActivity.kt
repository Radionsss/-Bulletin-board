package com.example.bulletinboardtwo.act

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.databinding.ActivityFilterBinding
import com.example.bulletinboardtwo.dialogs.DialogSpinnerHelper
import com.example.bulletinboardtwo.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsActionBar()
        onClickSelectCountry()
        onClickSelectCity()
        onClickGone()
        onClickClear()
        getDataFilter()
    }

    private fun onClickSelectCountry() = with(binding) {
        tvSelectCountry.setOnClickListener {
            val list = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, list, tvSelectCountry)
            if (tvSelectCity.text.toString() != getString(R.string.select_city)) {
                tvSelectCity.text = getString(R.string.select_city)
            }
        }
    }

    private fun onClickSelectCity() = with(binding) {
        tvSelectCity.setOnClickListener {
            val selectedCountry = tvSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val list = CityHelper.getAllCity(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, list, tvSelectCity)
            } else {
                Toast.makeText(
                    this@FilterActivity,
                    getString(R.string.no_select_city),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getDataFilter() = with(binding) {
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != null && filter != "empty") {
            val filterArray = filter.split("_")
            if (filterArray[0] != "empty") tvSelectCountry.text = filterArray[0]
            if (filterArray[1] !="empty") tvSelectCity.text = filterArray[1]
            if (filterArray[2] != "empty") edIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private fun onClickGone() = with(binding) {
        btnGoneFilter.setOnClickListener {
            val i = Intent().apply{
                putExtra(FILTER_KEY,createBuilder())
            }
            setResult(RESULT_OK,i)
            finish()
        }
    }
    private fun onClickClear() = with(binding) {
        btnClearFilter.setOnClickListener {
            tvSelectCountry.text= getString(R.string.select_country)
            tvSelectCity.text= getString(R.string.select_city)
            edIndex.setText("")
            checkBoxWithSend.isChecked=false
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun createBuilder(): String = with(binding) {
        val stringBuilder = StringBuilder()
        val arrTempFilter = listOf(
            tvSelectCountry.text,
            tvSelectCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString()
        )

        for ((i, s) in arrTempFilter.withIndex()) {
            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()) {
                stringBuilder.append(s)
                if (i != arrTempFilter.size - 1) stringBuilder.append("_")
            }else{
                stringBuilder.append("empty")
                if (i != arrTempFilter.size - 1) stringBuilder.append("_")
            }
        }

        return stringBuilder.toString()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            val i = Intent().apply{
                putExtra(FILTER_KEY,createBuilder())
            }
            setResult(RESULT_OK,i)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun settingsActionBar(){//home button
        val ab=supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }
    companion object{
        const val FILTER_KEY="data"
    }
}