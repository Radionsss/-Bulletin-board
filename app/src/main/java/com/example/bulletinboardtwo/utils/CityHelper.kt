package com.example.bulletinboardtwo.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

object CityHelper {
    fun getAllCountries(context: Context): ArrayList<String> {
        val countries = arrayListOf<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFIle = String(bytesArray)
            val jsonObject = JSONObject(jsonFIle)
            val countryNames = jsonObject.names()
            if (countryNames != null) {
                for (i in 0 until countryNames.length()) {
                    countries.add(countryNames.getString(i))
                }
            }
            inputStream.close()
        } catch (e: IOException) {

        }
        return countries
    }

    fun getAllCity(country: String, context: Context): ArrayList<String> {
        val cityList = arrayListOf<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFIle = String(bytesArray)
            val jsonObject = JSONObject(jsonFIle)
            val cityNames = jsonObject.getJSONArray(country)
            for (i in 0 until cityNames.length()) {
                cityList.add(cityNames.getString(i))
            }
            inputStream.close()
        } catch (e: IOException) {

        }
        return cityList
    }


    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String> {
        val filteredList = ArrayList<String>()
        for (selection: String in list) {
            if (searchText == null) {
                filteredList.add("No result")
                return filteredList
            }
            if (selection.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT)))
                filteredList.add(selection)
        }

        if (filteredList.size == 0) filteredList.add("Совподений не найдено")
        return filteredList
    }
}