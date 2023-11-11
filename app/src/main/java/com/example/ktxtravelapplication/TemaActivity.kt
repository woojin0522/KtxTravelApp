package com.example.ktxtravelapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ktxtravelapplication.databinding.ActivityTemaBinding

class TemaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTemaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.temaToolbar)
        supportActionBar?.setTitle("")
    }
}