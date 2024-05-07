package com.example.ktxtravelapplication.planActivity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding
import com.example.ktxtravelapplication.planActivity.planRoomDB.PlanDB
import kotlinx.coroutines.runBlocking

var planNumber = 0

class PlanActivity : AppCompatActivity() {
    lateinit var datas: MutableList<planData>
    lateinit var editor: SharedPreferences.Editor
    companion object {
        // sharedPreferences 선언
        lateinit var pref : SharedPreferences
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // pref 변수 초기화 - 저장된 값을 불러옴
        pref = getPreferences(Context.MODE_PRIVATE)
        // 리사이클러뷰 데이터 리스트
        datas = mutableListOf()

        // 뷰 바인딩
        val binding = ActivityPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // pref를 수정하는 변수
        editor = pref.edit()

        // pref에서 "저장횟수" key의 데이터값이 0 이상일 때 작동
        if(pref.getInt("저장횟수", -1) >= 0){
            // 저장횟수만큼 반복하여 데이터를 불러옴.
            for(i in 0..pref.getInt("저장횟수", 0)) {
                val prefPlanNumber = pref.getInt("${i}번 planNumber", 0)
                val prefPlanPos = pref.getInt("${i}번 planPos", 0)
                val prefPlanTitle = pref.getString("${i}번 planTitle", "")
                val prefPlanStartDate = pref.getString("${i}번 planStartDate", "")
                val prefPlanEndDate = pref.getString("${i}번 planEndDate", "")

                // 불러온 데이터를 리사이클러뷰 datas에 추가
                datas.add(
                    planData(prefPlanNumber, prefPlanPos, prefPlanTitle.toString(),
                    prefPlanStartDate.toString(), prefPlanEndDate.toString(), false)
                )

                binding.planRecyclerView.adapter?.notifyItemInserted(i)
            }
        }

        fun planNotTextCheck(){
            if(datas.size > 0){
                binding.planNotPlanText.visibility = View.GONE
            }
            else {
                binding.planNotPlanText.visibility = View.VISIBLE
            }
        }
        planNotTextCheck()

        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")

        // 상단바 뒤로가기 버튼을 눌렀을 경우
        binding.planBackBtn.setOnClickListener {
            finish()
        }

        // 인텐트 발생시켜 화면 전환 후 되돌아왔을 때 처리
        // returnTitle과 returnDate를 발생시킨 인텐트에서 처리, 반환하고 되돌아와서 그 값을 받아 변수에 담는다.
        // 그리고 그 변수를 datas 리스트에 add하여 리사이클러뷰 항목을 추가한다.
        val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            val returnTitle = it.data?.getStringExtra("returnTitle")
            val returnStartDate = it.data?.getStringExtra("returnStartDate")
            val returnEndDate = it.data?.getStringExtra("returnEndDate")
            val returnState = it.data?.getStringExtra("returnState")
            val returnPos = it.data?.getIntExtra("returnPos", 0)
            val returnPlanNumber = it.data?.getIntExtra("returnPlanNumber", 0)
            val returnIndex = it.data?.getIntExtra("returnIndex", 0)

            if(returnTitle != null) {
                // returnState가 저장(추가)일 때 리사이클러 항목 추가
                if(returnState == "저장"){
                    datas.add(planData(planNumber, returnPos, returnTitle.toString(), returnStartDate.toString(), returnEndDate.toString(), false))
                    planNumber = planNumber + 1
                }
                // returnState가 수정일 때 해당 리사이클러 항목 수정
                else{       //=================================================== 여기 수정해야됨 ! !
                    datas[returnIndex!!].planPos = returnPos
                    datas[returnIndex!!].planTitle = returnTitle.toString()
                    datas[returnIndex!!].planStartDate = returnStartDate.toString()
                    datas[returnIndex!!].planEndDate = returnEndDate.toString()
                    binding.planRecyclerView.adapter?.notifyDataSetChanged()
                }

            }
            planNotTextCheck()
            binding.planRecyclerView.adapter?.notifyItemInserted(datas.size)
        }

        // 상단바 + 버튼을 눌렀을 경우
        binding.planPlusBtn.setOnClickListener {
            val intent = Intent(this, TravelPlanActivity::class.java)
            // 화면 전환간 애니메이션 제거 만약 api34 이상일 경우 overrideActivityTransition 사용
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("returnState", "저장")
            intent.putExtra("returnPlanNumber", planNumber)
            intent.putExtra("returnIndex", datas.size)
            requestLauncher.launch(intent)
            planNotTextCheck()
        }

        //
        val db = Room.databaseBuilder(
            applicationContext,
            PlanDB::class.java,
            "PlanDB"
        ).build()
        //
        binding.planMinusBtn.setOnClickListener {
            fun deletePlan() {
                runBlocking {
                    var whileStop = false

                    for (i in 0..datas.size - 1) {
                        if (datas[i].deleteChecked == true) {
                            editor.remove("${i}번 planNumber")
                            editor.remove("${i}번 planPos")
                            editor.remove("${i}번 planTitle")
                            editor.remove("${i}번 planStartDate")
                            editor.remove("${i}번 planEndDate")

                            editor.apply()
                        }
                    }
                    while (true) {
                        for (i in 0..datas.size - 1) {
                            if (datas[i].deleteChecked == true) {
                                db.getDao().allDeletePlan(datas[i].planNumber!!.toInt())

                                datas.removeAt(i)
                                binding.planRecyclerView.adapter?.notifyItemRemoved(i)

                                whileStop = false
                                break
                            } else {
                                whileStop = true
                            }
                        }
                        //
                        if (whileStop == true || datas.size == 0) {
                            if (datas.size == 0) {
                                editor.remove("저장횟수")
                                editor.apply()
                            }
                            planNotTextCheck()
                            break
                        }
                    }
                }
            }

            fun checkNull(message: String) {
                AlertDialog.Builder(this).run{
                    setTitle("경고")
                    setMessage(message)
                    setPositiveButton("확인", null)
                    show()
                }
            }

            if(datas.isEmpty()) {
                checkNull("삭제할 항목이 없습니다.")
            }
            else {
                AlertDialog.Builder(this).run {
                    val eventHandler = object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            if (p1 == DialogInterface.BUTTON_POSITIVE) {
                                var checked = false
                                for (i in 0..datas.size - 1) {
                                    if (datas[i].deleteChecked == true) {
                                        deletePlan()
                                        checked = true
                                        break
                                    }
                                }
                                if (checked == false) {
                                    checkNull("삭제할 항목을 선택해주세요.")
                                }
                            } else {
                            }
                        }
                    }

                    setTitle("경고")
                    setMessage("정말 삭제하시겠습니까?")
                    setPositiveButton("네", eventHandler)
                    setNegativeButton("아니오", eventHandler)
                    show()
                }
            }
        }

        // 리사이클러뷰 생성
        binding.planRecyclerView.adapter = PlanRecyclerAdapter(this, datas, requestLauncher)
        binding.planRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // 액티비티가 종료될 때 작동하는 함수
    override fun onDestroy() {
        super.onDestroy()
        // 리사이클러뷰 datas가 1개 이상 있을때 작동
        if(datas.size > 0){
            // datas에 size만큼 반복하여 데이터를 저장함.
            for(i in 0..datas.size - 1){
                editor.putInt("${i}번 planNumber", datas[i].planNumber!!.toInt())
                editor.putInt("${i}번 planPos", datas[i].planPos!!.toInt())
                editor.putString("${i}번 planTitle", datas[i].planTitle)
                editor.putString("${i}번 planStartDate", datas[i].planStartDate)
                editor.putString("${i}번 planEndDate", datas[i].planEndDate)
                editor.putInt("저장횟수", i)

                editor.apply()
            }
        }
    }
}

// 리사이클러뷰 항목에 쓰이는 데이터 클래스
data class planData(
    var planNumber: Int?,
    var planPos: Int?,
    var planTitle: String,
    var planStartDate: String,
    var planEndDate: String,
    var deleteChecked: Boolean
)