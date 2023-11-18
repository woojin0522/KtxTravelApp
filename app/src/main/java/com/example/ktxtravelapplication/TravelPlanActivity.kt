package com.example.ktxtravelapplication

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
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
    lateinit var planTitle: String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTravelPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")

        // 뒤로가기 기능 함수
        fun backBtn() {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if(p1 == DialogInterface.BUTTON_POSITIVE) {
                            finish()
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

        // 저장하기 기능 함수
        fun saveBtn() {
            AlertDialog.Builder(this).run {
                val eventHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if(p1 == DialogInterface.BUTTON_POSITIVE) {
                            // 제목 값 저장
                            planTitle = binding.planTitle.text.toString()
                            // 이전화면으로 값 넘겨주기
                            finish()
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


        // 현재 날짜를 초기화. 안드로이드 8 버전 이상부터 사용
        binding.planCalendarDay.text = LocalDate.now().toString()

        // 캘린더뷰에서 선택한 날짜를 불러옴.
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            binding.planCalendarDay.text = "$year-${month + 1}-$dayOfMonth"
        }

        // 여행계획 데이터
        val datas = mutableListOf<PlanDetailDatas>().apply {
            add(PlanDetailDatas("오후 12 : 00", "오후 1 : 00", ""))
        }

        // 시간별 계획 추가버튼 클릭시
        binding.planDetailPlusBtn.setOnClickListener {
            datas.add(PlanDetailDatas("오후 12 : 00", "오후 1 : 00", ""))
            binding.planDetailRecyclerView.adapter?.notifyItemInserted(datas.size)
        }

        // 시간별 계획 저장버튼 클릭시
        binding.planSaveBtn.setOnClickListener {
            // 저장버튼 클릭시 확인여부창 띄우기
            saveBtn()
        }

        // 시간별 계획 리사이클러뷰
        binding.planDetailRecyclerView.adapter = TravelPlanRecyclerAdapter(datas)
        binding.planDetailRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // 화면 전환간 애니메이션 제거
    override fun onPause() {
        super.onPause()
        // 만약 api34일 경우 overrideActivityTransition 사용
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

    inner class ViewHolder(val binding : PlanDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
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

            binding.planDetailTime.setOnClickListener {
                timePic(1)
            }
            binding.planDetailTime2.setOnClickListener {
                timePic(2)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanDetailItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
}