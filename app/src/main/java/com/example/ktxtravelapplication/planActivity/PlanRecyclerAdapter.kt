package com.example.ktxtravelapplication.planActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.PlanItemBinding

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
            binding.planDeleteCheckbox.isChecked = false

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
                intent.putExtra("returnPos", datas[bindingAdapterPosition].planPos)
                intent.putExtra("returnIndex", bindingAdapterPosition)
                intent.putExtra("returnPlanNumber", datas[bindingAdapterPosition].planNumber )
                requestLaun.launch(intent)
            }

            binding.planDeleteCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){ datas[bindingAdapterPosition].deleteChecked = true}
                else { datas[bindingAdapterPosition].deleteChecked = false }
            }
        }
    }
}