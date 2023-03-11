package com.example.bulletinboardtwo.utils

import com.example.bulletinboardtwo.model.Ad
import com.example.bulletinboardtwo.model.AdFilter

object FilterManager {
    fun createFilter(ad: Ad):AdFilter{
        return AdFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.withSend}_${ad.time}",

            "${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.withSend}_${ad.time}"

        )
    }

    fun getFilter(filter: String): String{
        val sBuilderNode=StringBuilder()
        val sBuilderFilter=StringBuilder()
        val filterTempArray=filter.split("_")

        if(filterTempArray[0]!="empty"){
            sBuilderNode.append("country_")
            sBuilderFilter.append("${filterTempArray[0]}_")
        }
        if(filterTempArray[1]!="empty") {
            sBuilderNode.append("city_")
            sBuilderFilter.append("${filterTempArray[1]}_")
        }
        if(filterTempArray[2]!="empty") {
            sBuilderNode.append("index_")
            sBuilderFilter.append("${filterTempArray[2]}_")
        }
        sBuilderFilter.append(filterTempArray[3])
        sBuilderNode.append("withSend_time")

        return "$sBuilderNode|$sBuilderFilter"
    }
}