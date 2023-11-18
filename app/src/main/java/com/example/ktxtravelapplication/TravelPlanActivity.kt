package com.example.ktxtravelapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityTravelPlanBinding
import com.example.ktxtravelapplication.databinding.PlanDetailItemBinding
import java.time.LocalDate

class TravelPlanActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTravelPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")

        // 상단바 뒤로가기 버튼
        binding.planBackBtn.setOnClickListener {
            finish()
        }

        // 현재 날짜를 초기화. 안드로이드 8 버전 이상부터 사용
        binding.planCalendarDay.text = LocalDate.now().toString()

        // 캘린더뷰에서 선택한 날짜를 불러옴.
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            binding.planCalendarDay.text = "$year-${month + 1}-$dayOfMonth"
        }

        // 여행계획 데이터
        val datas = mutableListOf<PlanDetailDatas>().apply {
            add(PlanDetailDatas("오전 12 : 00", "오후 1 : 00"))
        }

        // 시간별 계획 추가버튼 클릭시
        binding.planDetailPlusBtn.setOnClickListener {
            datas.add(PlanDetailDatas("오전 12 : 00", "오후 1 : 00"))
            binding.planDetailRecyclerView.adapter?.notifyDataSetChanged()
        }

        // 시간별 계획 리사이클러뷰
        val layoutManager = LinearLayoutManager.VERTICAL
        binding.planDetailRecyclerView.adapter = TravelPlanRecyclerAdapter(datas)
        binding.planDetailRecyclerView.layoutManager = LinearLayoutManager(this)
    }

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
)

// 시간별 계획 리사이클러뷰 어댑터
class TravelPlanRecyclerAdapter(val datas : MutableList<PlanDetailDatas>) : RecyclerView.Adapter<TravelPlanRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: PlanDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int){
            binding.planDetailTime.text = datas[pos].startTime
            binding.planDetailTime2.text = datas[pos].endTime

            // 삭제하기 버튼 클릭시 리사이클러뷰 항목 삭제
            binding.planDeleteBtn.setOnClickListener {
                datas.removeAt(pos)
                notifyDataSetChanged()
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