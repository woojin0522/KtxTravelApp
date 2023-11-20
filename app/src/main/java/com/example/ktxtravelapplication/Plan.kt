package com.example.ktxtravelapplication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Plan")
data class Plan(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "planTitle")
    val planTitle: String?,

    @ColumnInfo(name = "planStartDate")
    val planStartDate: String?,

    @ColumnInfo(name = "planEndDate")
    val planEndDate: String,


)
