package com.example.ktxtravelapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ktxtravelapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
    }
}