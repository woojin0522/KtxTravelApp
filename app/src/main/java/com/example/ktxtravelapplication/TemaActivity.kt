package com.example.ktxtravelapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktxtravelapplication.databinding.ActivityTemaBinding

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
    }
}

