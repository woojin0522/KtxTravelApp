package com.example.ktxtravelapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding
import com.example.ktxtravelapplication.databinding.PlanItemBinding

class PlanActivity : AppCompatActivity() {
    lateinit var datas: MutableList<planData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val returnDate = it.data?.getStringExtra("returnDate")

            if(returnTitle != null) {
                datas.add(planData(returnTitle.toString(), returnDate.toString()))
            }
            Log.d("test", "$datas")
            binding.planRecyclerView.adapter?.notifyItemInserted(datas.size)
        }

        // 상단바 + 버튼을 눌렀을 경우
        binding.planPlusBtn.setOnClickListener {
            val intent = Intent(this, TravelPlanActivity::class.java)
            // 화면 전환간 애니메이션 제거 만약 api34 이상일 경우 overrideActivityTransition 사용
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requestLauncher.launch(intent)
        }

        // 리사이클러뷰 데이터 리스트
        datas = mutableListOf()

        // 리사이클러뷰 생성
        binding.planRecyclerView.adapter = PlanRecyclerAdapter(datas)
        binding.planRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}

data class planData(
    val planTitle: String,
    val planDate: String
)

class PlanRecyclerAdapter(val datas: MutableList<planData>): RecyclerView.Adapter<PlanRecyclerAdapter.ViewHolder>() {
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
            binding.title.text = datas[pos].planTitle
            binding.planDate.text = datas[pos].planDate

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, TravelPlanActivity::class.java)
                itemView.context.startActivity(intent)
            }
        }
    }
}
