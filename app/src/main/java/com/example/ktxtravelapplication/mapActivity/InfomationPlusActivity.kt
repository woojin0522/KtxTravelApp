package com.example.ktxtravelapplication.mapActivity

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityInfomationPlusBinding
import com.example.ktxtravelapplication.planActivity.PlanActivity
import com.example.ktxtravelapplication.planActivity.TravelPlanActivity
import com.example.ktxtravelapplication.planActivity.planNumber
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class InfomationPlusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInfomationPlusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.infoToolbar)
        supportActionBar!!.setTitle("")

        val infoTitle = intent.getStringExtra("infoTitle")
        val infoName = intent.getStringExtra("infoName")
        val infoAddress = intent.getStringExtra("infoAddress")
        val infoDescription = intent.getStringExtra("infoDescription")
        val infoImage = intent.getStringExtra("infoImage")
        val infoTel = intent.getStringExtra("infoTel")
        val infoDist = intent.getIntExtra("infoDist", 0)

        binding.infoPlusTel.text = infoTel
        binding.infoTitle.text = infoTitle
        binding.infoPlusName.text = infoName
        binding.infoPlusAddress.text = infoAddress
        binding.infoPlusDescription.text = infoDescription
        if(infoDist != 0) {
            binding.infoPlusDist.text = "역에서의 거리 : " + infoDist + "m"
        }
        else {
            binding.infoPlusDist.isVisible = false
        }

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(infoImage) // 불러올 이미지 url
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.error)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.infoImage) // 이미지를 넣을 뷰
        // 여행계획 버튼 클릭시
        /*binding.infoPlusPlanBtn.setOnClickListener {
            val intent = Intent(this, PlanActivity::class.java)
            startActivity(intent)
        }*/

        // 이미지 클릭시 확대
        binding.infoImage.setOnClickListener {
            val intent = Intent(this, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", infoImage)
            val opt = ActivityOptions.makeSceneTransitionAnimation(this, it, "imgTrans")
            startActivity(intent, opt.toBundle())
        }
        //.circleCrop() //동그랗게 자르기
        binding.infoBackBtn.setOnClickListener {
            finish()
        }
    }
}