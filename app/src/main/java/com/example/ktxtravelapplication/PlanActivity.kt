package com.example.ktxtravelapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ktxtravelapplication.databinding.ActivityPlanBinding

class PlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.planToolbar)
        supportActionBar?.setTitle("")
    }
}