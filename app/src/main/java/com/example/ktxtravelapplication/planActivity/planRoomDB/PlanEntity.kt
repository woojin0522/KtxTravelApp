package com.example.ktxtravelapplication.planActivity.planRoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblPlan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "deleteState")
    val deleteState: Boolean,

    @ColumnInfo(name = "planNumber")
    val planNumber: Int,

    @ColumnInfo(name = "planTitle")
    val Title: String,

    @ColumnInfo(name = "planStartDate")
    val StartDate: String,

    @ColumnInfo(name = "planEndDate")
    val EndDate: String,

    @ColumnInfo(name = "planSelectedDate")
    val SelectedDate: String,

    @ColumnInfo(name = "planStartTime")
    val StartTime: String,

    @ColumnInfo(name = "planEndTime")
    val EndTime: String,

    @ColumnInfo(name = "planDetail")
    val Detail: String,
)
