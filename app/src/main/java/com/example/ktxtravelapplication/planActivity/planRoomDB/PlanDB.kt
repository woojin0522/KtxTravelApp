package com.example.ktxtravelapplication.planActivity.planRoomDB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlanEntity::class], version = 1 )
abstract class PlanDB: RoomDatabase() {
    abstract fun getDao(): planDao
}