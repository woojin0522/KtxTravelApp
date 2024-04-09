package com.example.ktxtravelapplication.ticketActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ktxtravelapplication.databinding.ActivityTicketBinding

class TicketActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰바인딩 선언부
        val binding = ActivityTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.ticketToolbar)
        supportActionBar?.setTitle("")

        // 뒤로가기 버튼
        binding.ticketBackBtn.setOnClickListener {
            finish()
        }

        // 코레일톡 패키지명 변수
        val korailTalk = "com.korail.talk"
        // packageManager.getLaunchIntentForPackage()함수를 통해 코레일톡 앱을 불러옴.
        val korailIntent = packageManager.getLaunchIntentForPackage(korailTalk)

        // 코레일톡 이미지를 클릭하였을 경우 앱이 설치되어있다면 앱을 실행하고, 설치되어있지 않다면 코레일톡 플레이스토어 링크로 이동
        binding.korailTalkImage.setOnClickListener {
            try{
                startActivity(korailIntent)
            }
            catch (e: Exception) {
                val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$korailTalk"))
                startActivity(intentPlayStore)
            }
        }

        // srt 패키지명 변수
        val srtPlay = "kr.co.srail.newapp"
        // srt 앱을 불러옴
        val srtIntent = packageManager.getLaunchIntentForPackage(srtPlay)
        // srt 앱 이미지를 클릭하였을 경우 앱이 설치되어있다면 앱을 실행, 설치되어있지 않다면 srt 앱 플레이스토어 링크로 이동
        binding.srtAppImage.setOnClickListener {
            try{
                startActivity(srtIntent)
            }
            catch (e: Exception) {
                val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$srtPlay"))
                startActivity(intentPlayStore)
            }
        }
    }
}