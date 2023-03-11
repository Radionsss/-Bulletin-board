package com.example.bulletinboardtwo.model

data class Ad(
    val country:String?="",
    val city:String?="",
    val tel:String?="",
    val index:String?="",
    val withSend:String?=null,
    val category:String?=null,
    val title:String?="",
    val price:String?="",
    val description:String?="",
    val email:String?="",
    val mainImage:String="empty",
    val image2:String="empty",
    val image3:String="empty",
    val key:String?="",
    var favCounter: String? ="0",
    val uid:String?="",
    val time:String?="0",
    var isFav:Boolean=false,

    var viewCounter: String? = "0",
    var emailsCounter: String? = "0",
    var callsCounter: String? = "0"
):java.io.Serializable
