package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.FragmentTemaCourseBinding
import com.example.ktxtravelapplication.databinding.TemaLineItemBinding
import com.example.ktxtravelapplication.temaActivity.temaCourseActivity

class temaCourseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTemaCourseBinding.inflate(inflater, container, false)

        val temaLineList = mutableListOf("경부선", "호남선", "경전선", "전라선", "강릉선", "중앙선", "중부내륙선", "동해선")

        binding.temaCourseLineRecyclerView.adapter = temaCourseLineAdapter(temaLineList)
        binding.temaCourseLineRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }
}

class temaCourseLineAdapter(val datas: MutableList<String>) : RecyclerView.Adapter<temaCourseLineAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: temaCourseLineAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): temaCourseLineAdapter.ViewHolder {
        val binding = TemaLineItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: TemaLineItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(pos: Int){
            binding.temaLineItemName.text = datas[pos]

            itemView.setOnClickListener {
                val intent = Intent(it.context, temaCourseActivity::class.java)

                intent.putExtra("ktxLine", datas[pos])

                it.context.startActivity(intent)
            }
        }
    }
}