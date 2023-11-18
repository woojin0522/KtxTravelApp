package com.example.ktxtravelapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding
import com.example.ktxtravelapplication.databinding.PlanItemBinding

class PlanActivity : AppCompatActivity() {
    lateinit var planTitle: String
    lateinit var datas: MutableList<String>
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

        // 상단바 + 버튼을 눌렀을 경우
        binding.planPlusBtn.setOnClickListener {
            val intent = Intent(this, TravelPlanActivity::class.java)
            // 화면 전환간 애니메이션 제거 만약 api34 이상일 경우 overrideActivityTransition 사용
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

    }
}

class PlanRecyclerAdapter(val datas: MutableList<String>): RecyclerView.Adapter<PlanRecyclerAdapter.ViewHolder>() {
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
            binding.title.text = datas[pos]
        }
    }
}
