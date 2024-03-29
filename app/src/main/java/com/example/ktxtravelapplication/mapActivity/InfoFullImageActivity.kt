package com.example.ktxtravelapplication.mapActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityInfoFullImageBinding

class InfoFullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityInfoFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.error)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.infoFullImageView) // 이미지를 넣을 뷰

        binding.infoFullImageView.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

}