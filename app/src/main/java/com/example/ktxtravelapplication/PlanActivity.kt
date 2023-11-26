package com.example.ktxtravelapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding
import com.example.ktxtravelapplication.databinding.PlanItemBinding

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
        if(pref.getInt("저장횟수", 0) > 0){
            // 저장횟수만큼 반복하여 데이터를 불러옴.
            for(i in 0..pref.getInt("저장횟수", 0)) {
                val prefPlanNumber = pref.getInt("${i}번 planNumber", 0)
                val prefPlanPos = pref.getInt("${i}번 planPos", 0)
                val prefPlanTitle = pref.getString("${i}번 planTitle", "")
                val prefPlanStartDate = pref.getString("${i}번 planStartDate", "")
                val prefPlanEndDate = pref.getString("${i}번 planEndDate", "")

                // 불러온 데이터를 리사이클러뷰 datas에 추가
                datas.add(planData(prefPlanNumber, prefPlanPos, prefPlanTitle.toString(),
                    prefPlanStartDate.toString(), prefPlanEndDate.toString()))

                binding.planRecyclerView.adapter?.notifyItemInserted(i)
            }
        }

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

            if(returnTitle != null) {
                // returnState가 저장(추가)일 때 리사이클러 항목 추가
                if(returnState == "저장"){
                    datas.add(planData(planNumber, returnPos, returnTitle.toString(), returnStartDate.toString(), returnEndDate.toString()))
                    planNumber = planNumber + 1
                }
                // returnState가 수정일 때 해당 리사이클러 항목 수정
                else{
                    datas[returnPlanNumber!!].planPos = returnPos
                    datas[returnPlanNumber!!].planTitle = returnTitle.toString()
                    datas[returnPlanNumber!!].planStartDate = returnStartDate.toString()
                    datas[returnPlanNumber!!].planEndDate = returnEndDate.toString()
                    binding.planRecyclerView.adapter?.notifyDataSetChanged()
                }

            }
            binding.planRecyclerView.adapter?.notifyItemInserted(datas.size)
        }

        // 상단바 + 버튼을 눌렀을 경우
        binding.planPlusBtn.setOnClickListener {
            val intent = Intent(this, TravelPlanActivity::class.java)
            // 화면 전환간 애니메이션 제거 만약 api34 이상일 경우 overrideActivityTransition 사용
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("returnState", "저장")
            intent.putExtra("returnPlanNumber", planNumber)
            requestLauncher.launch(intent)
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
    var planEndDate: String
)

// 리사이클러뷰 어댑터
class PlanRecyclerAdapter(val context: Context, val datas: MutableList<planData>, val requestLaun: ActivityResultLauncher<Intent>): RecyclerView.Adapter<PlanRecyclerAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlanItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: PlanItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int){
            // 초기값 설정
            binding.title.text = datas[pos].planTitle
            binding.planStartDate.text = datas[pos].planStartDate
            binding.planEndDate.text = datas[pos].planEndDate

            // 리사이클러뷰 각 항목을 클릭하였을 경우 작동
            itemView.setOnClickListener {
                // 수정모드로 액티비티 전환하기
                val activity = context as PlanActivity
                val intent = Intent(activity, TravelPlanActivity::class.java)
                // 인텐트로 필요한 값 넘겨주기
                intent.putExtra("returnTitle", binding.title.text)
                intent.putExtra("returnStartDate", binding.planStartDate.text)
                intent.putExtra("returnEndDate", binding.planEndDate.text)
                intent.putExtra("returnState", "수정")
                intent.putExtra("returnPos", datas[pos].planPos)
                intent.putExtra("returnIndex", pos)
                intent.putExtra("returnPlanNumber", datas[bindingAdapterPosition].planNumber )
                requestLaun.launch(intent)
            }
        }
    }
}
