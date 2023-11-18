package com.example.ktxtravelapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding

class PlanActivity : AppCompatActivity() {
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

/*class PlanRecyclerAdapter(): RecyclerView.Adapter<PlanRecyclerAdapter.ViewHolder>() {
    override fun getItemCount(): Int {

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(position: Int){

        }
    }
}*/
