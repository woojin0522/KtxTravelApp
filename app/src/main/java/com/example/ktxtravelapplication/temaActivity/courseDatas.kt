package com.example.ktxtravelapplication.temaActivity

import java.io.Serializable

data class courseDatas(
    val title: String,
    val contentId: Int,
    val firstImage: String,
    val mapx: Double,
    val mapy: Double,
    val nearStation: String,
) : Serializable