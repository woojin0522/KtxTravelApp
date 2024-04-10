package com.example.ktxtravelapplication.mapActivity.ktxLinesData

import java.io.Serializable

data class StationPositions(
    val stationNum: Int,
    val stationEngName: String,
    val stationName: String,
    val stationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val stationInfomation: String,
    val likeCount: Int
) : Serializable
