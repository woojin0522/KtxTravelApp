package com.example.ktxtravelapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.ktxtravelapplication.databinding.ActivityTemaBinding
import com.example.ktxtravelapplication.databinding.TemaItemViewBinding

class TemaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        val binding = ActivityTemaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바를 액션바 대신
        setSupportActionBar(binding.temaToolbar)
        supportActionBar?.setTitle("")

        // 뒤로가기 버튼 눌렀을 때
        binding.temaBackBtn.setOnClickListener {
            finish()
        }

        val temaBinding = TemaItemViewBinding.inflate(layoutInflater)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.temaTagRecyclerview.layoutManager = layoutManager
        binding.temaTagRecyclerview.adapter = TemaRecyclerAdapter()
    }
}

class TemaRecyclerAdapter():
        RecyclerView.Adapter<TemaRecyclerAdapter.TemaRecyclerViewHolder>() {
    private val itemList = mutableListOf<String>().apply {
        add("<#서울특별시>")
        add("<#경기도>")
        add("<#인천시>")
        add("<#강원도>")
        add("<#충청북도>")
        add("<#충청남도>")
        add("<#전라북도>")
        add("<#전라남도>")
        add("<#경상북도>")
        add("<#경상남도>")
    }
    inner class TemaRecyclerViewHolder(val binding: TemaItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
            binding.temaText.text = itemList[pos]
        }
    }
    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemaRecyclerViewHolder {
        val view = TemaItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TemaRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemaRecyclerViewHolder, position: Int) {
        holder.bind(position)
    }
}
