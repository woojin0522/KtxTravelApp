package com.example.ktxtravelapplication.temaActivity

import java.io.Serializable

data class stationDatas(
    val stationName : String,
    val latitude : Double,
    val longitude : Double,
    val stationNum : Int
) : Serializable