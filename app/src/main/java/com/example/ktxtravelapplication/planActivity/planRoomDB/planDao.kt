package com.example.ktxtravelapplication.planActivity.planRoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface planDao {
    // select
    @Query("SELECT * FROM tblPlan")
    suspend fun getAll(): List<PlanEntity>
    // 리사이클러뷰 클릭시 해당 항목 제목에 맞는 데이터 가져옴
    @Query("SELECT * FROM tblPlan WHERE planNumber = :planNumber")
    suspend fun getPlan(planNumber: Int): List<PlanEntity>

    // insert
    @Insert
    suspend fun insertPlan(planEntity: PlanEntity)

    // update
    @Query("UPDATE tblPlan SET planTitle = :planEditTitle, planDetail = :planEditDetail, " +
            "planStartDate = :planEditStartDate, planEndDate = :planEditEndDate, planStartTime = :planEditStartTime, planEndTime = :planEditEndTime, " +
            "planSelectedDate = :planEditSelectedDate " +
            "WHERE id = :id")
    suspend fun updatePlan(
        id: Int,
        planEditTitle: String,
        planEditDetail: String,
        planEditStartDate: String,
        planEditEndDate: String,
        planEditStartTime: String,
        planEditEndTime: String,
        planEditSelectedDate: String
    )
    @Query("UPDATE tblPlan SET deleteState = :deleteState WHERE id = :id")
    suspend fun updateDeleteState(id: Int, deleteState: Boolean)

    // delete
    @Query("DELETE FROM tblPlan WHERE deleteState = :deleteState")
    suspend fun deletePlan(deleteState: Boolean)
    @Query("DELETE FROM tblPlan WHERE planNumber = :planNumber")
    suspend fun allDeletePlan(planNumber: Int)

}