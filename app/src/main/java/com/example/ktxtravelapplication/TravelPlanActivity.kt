package com.example.ktxtravelapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityTravelPlanBinding
import com.example.ktxtravelapplication.databinding.PlanDetailItemBinding
import java.time.LocalDate

class TravelPlanActivity : AppCompatActivity() {
    // 변수 선언 영역 ------------------------
    lateinit var planTitle: String
    lateinit var planDate: String
    lateinit var binding: ActivityTravelPlanBinding
    // ------------------------ 변수 선언 영역
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩
        binding = ActivityTravelPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바 대신 툴바 사용 및 타이틀 비워 두기
        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")

        // 값 전달받기
        val returnPlanTitle = intent.getStringExtra("Title")
        val returnPlanDate = intent.getStringExtra("Date")
        val returnState = intent.getStringExtra("state")
        if(returnPlanTitle == null || returnPlanDate == null){}
        else {
            binding.planTitle.setText(returnPlanTitle)
            binding.planCalendarDay.text = returnPlanDate
        }

        // 수정모드 일때 버튼 이름을 수정하기로 바꾸기
        if(returnState == "수정"){
            binding.planSaveBtn.text = "수정하기"
        }

        // 함수 영역 -------------------------------------------------------------
        // 제목 값이 비어 있을 경우 경고창 표시
        fun titleEmpty() {
            AlertDialog.Builder(this).run {
                setTitle("경고")
                setMessage("제목을 입력 해주세요.")
                setPositiveButton("확인", null)
                show()
            }
        }
        // 값 저장 및 인텐트 값 넘겨주기 함수
        fun planSave() {
            // 제목 날짜 저장
            planDate = binding.planCalendarDay.text.toString()

            val returnIntent = Intent()
            returnIntent.putExtra("returnTitle", planTitle)
            returnIntent.putExtra("returnDate", planDate)
            setResult(Activity.RESULT_OK, returnIntent)
            Log.d("test", "$planTitle, $planDate")
            // 이전화면으로 값 넘겨주기
            finish()
        }
        // 뒤로가기 버튼 기능 함수
        fun backBtn() {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if(p1 == DialogInterface.BUTTON_POSITIVE) {
                            // 제목값 검사
                            planTitle = binding.planTitle.text.toString()
                            // 제목값 비어있을 경우 경고창 표시
                            if(planTitle == "") titleEmpty()
                            // 제목값 있을 경우 저장
                            else planSave()
                        } else {
                            finish()
                        }
                    }
                }

                setTitle("나가기")
                setIcon(android.R.drawable.ic_dialog_info)
                setMessage("나가기 전에 저장하시겠습니까?")
                setPositiveButton("네", eventHandler)
                setNegativeButton("아니오", eventHandler)
                show()
            }
        }
        // 저장하기 버튼 기능 함수
        fun saveBtn() {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if(p1 == DialogInterface.BUTTON_POSITIVE) {
                            planSave()
                        } else {}
                    }
                }

                setTitle("저장여부")
                setIcon(android.R.drawable.ic_dialog_info)
                setMessage("정말 저장하시겠습니까?")
                setPositiveButton("네", eventHandler)
                setNegativeButton("아니오", eventHandler)
                show()
            }
        }
        // -------------------------------------------------------------함수 영역

        // 뒤로가기 기능 ----------------------------------------------------------
        // 상단바 뒤로가기 버튼
        binding.planBackBtn.setOnClickListener {
            // 화면 종료전 물어보는 창 띄우기
            backBtn()
        }

        // 뒤로가기 버튼 클릭시
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backBtn()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback = callback)
        // ---------------------------------------------------------- 뒤로가기 기능

        // 캘린더 영역 -----------------------------------------------------------
        // 현재 날짜를 초기화. 안드로이드 8 버전 이상부터 사용
        binding.planCalendarDay.text = LocalDate.now().toString()

        // 캘린더뷰에서 선택한 날짜를 불러옴.
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            binding.planCalendarDay.text = "$year-${month + 1}-$dayOfMonth"
        }
        // ----------------------------------------------------------- 캘린더 영역

        // 여행계획 데이터
        val datas = mutableListOf<PlanDetailDatas>().apply {
            add(PlanDetailDatas("오후 12 : 00", "오후 1 : 00", ""))
        }

        // 버튼 작동 영역 --------------------------------------------------------------
        // 시간별 계획 추가 버튼 클릭시
        binding.planDetailPlusBtn.setOnClickListener {
            datas.add(PlanDetailDatas("오후 12 : 00", "오후 1 : 00", ""))
            binding.planDetailRecyclerView.adapter?.notifyItemInserted(datas.size)
        }

        // 시간별 계획 저장 버튼 클릭시
        binding.planSaveBtn.setOnClickListener {
            if(returnState == "수정"){

            }
            else {
                // 저장 버튼 클릭시 확인 여부창 띄우기
                planTitle = binding.planTitle.text.toString()
                if(planTitle == "") titleEmpty()
                else saveBtn()
            }
        }
        // -------------------------------------------------------------- 버튼 작동 영역

        // 시간별 계획 리사이클러뷰
        binding.planDetailRecyclerView.adapter = TravelPlanRecyclerAdapter(datas)
        binding.planDetailRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // 화면 전환간 애니메이션 제거
    override fun onPause() {
        super.onPause()
        // 만약 api34일 경우 overrideActivityTransition 사용 추가 해야함.
        overridePendingTransition(0,0)
    }
}

// 여행계획 데이터 클래스
data class PlanDetailDatas(
    val startTime: String,
    val endTime: String,
    val planDetail: String
)

// 시간별 계획 리사이클러뷰 어댑터
class TravelPlanRecyclerAdapter(val datas: MutableList<PlanDetailDatas>) : RecyclerView.Adapter<TravelPlanRecyclerAdapter.ViewHolder>() {
    // 뷰 홀더 선언부
    inner class ViewHolder(val binding : PlanDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // 각 항목에서 작동하는 기능이나 텍스트값 등을 변경하는 함수
        fun bind(pos: Int){
            // TimePickerDialog 함수
            fun timePic(textNum: Int) {
                TimePickerDialog(itemView.context, object: TimePickerDialog.OnTimeSetListener{
                    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
                        val amOrPm: String
                        var hour = p1
                        val minute = p2
                        if(p1 >= 12 && p1 <= 24) {
                            amOrPm = "오후"
                            if(hour == 12) hour = hour
                            else hour = hour - 12
                        }  else{
                            amOrPm = "오전"
                        }
                        // textNum이 1일 경우 시작 시간을 변경, 2일 경우 끝나는 시간을 변경
                        if(textNum == 1) binding.planDetailTime.text = "${amOrPm} ${hour.toString()} : ${minute.toString().padStart(2, '0')}"
                        else binding.planDetailTime2.text = "${amOrPm} ${hour.toString()} : ${minute.toString().padStart(2, '0')}"
                    }
                }, 15, 0, false).show()
            }

            binding.planDetailTime.text = datas[pos].startTime
            binding.planDetailTime2.text = datas[pos].endTime

            // 삭제하기 버튼 클릭시 리사이클러뷰 항목 삭제
            binding.planDeleteBtn.setOnClickListener {
                datas.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, datas.size)
            }

            // 여행계획 시작 시간 클릭시 시간 변경창 띄우기
            binding.planDetailTime.setOnClickListener {
                timePic(1)
            }
            // 여행계획 끝나는 시간 클릭시 시간 변경창 띄우기
            binding.planDetailTime2.setOnClickListener {
                timePic(2)
            }
        }
    }

    // 리사이클러뷰 항목 개수를 반환
    override fun getItemCount(): Int {
        return datas.size
    }

    // 뷰 홀더를 생성.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanDetailItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    // 각 아이템 항목을 뷰 홀더에 맞춰서 출력
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
}