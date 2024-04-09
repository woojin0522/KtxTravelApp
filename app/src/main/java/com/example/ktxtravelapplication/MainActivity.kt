package com.example.ktxtravelapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.example.ktxtravelapplication.databinding.ActivityMainBinding
import com.example.ktxtravelapplication.mapActivity.MapActivity
import com.example.ktxtravelapplication.planActivity.PlanActivity
import com.example.ktxtravelapplication.temaActivity.TemaActivity
import com.example.ktxtravelapplication.ticketActivity.TicketActivity


class MainActivity : AppCompatActivity() {
    lateinit var viewPager_mainImages: ViewPager2
    var currentPosition = 0

    // 자동 슬라이드를 위한 핸들러 선언
    val handler= Handler(Looper.getMainLooper()) {
        setPage()
        true
    }

    // 메인 뷰 생성
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityBinding = ActivityMainBinding.inflate(layoutInflater) // 메인 뷰 바인딩 생성
        setContentView(mainActivityBinding.root) // 메인 뷰를 띄움

        viewPager_mainImages = findViewById(R.id.mainViewPager) // 뷰 페이저가 적용될 뷰에 id값
        viewPager_mainImages.adapter = MainImageViewPagerAdapter(getMainImages()) // 뷰 페이저에 어댑터 적용
        viewPager_mainImages.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 뷰 페이저의 방향을 횡단으로 설정
        viewPager_mainImages.isUserInputEnabled = false // 뷰 페이저 스왑을 제한

        // 네트워크 접속 확인
        fun isNetworkAvailable() : Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        true
                    }
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        true
                    }
                    else -> false
                }
            }
            // api 23 이하
            else {
                return connectivityManager.activeNetworkInfo?.isConnected ?: false
            }
        }
        if(isNetworkAvailable() == false) {
            Toast.makeText(this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_LONG).show()
        }

        // 자동 슬라이드를 위한 뷰페이저 쓰레드
        val thread=Thread(PagerRunnable())
        thread.start()

        // 메인화면 버튼 클릭시 이벤트 리스너
        mainActivityBinding.mainMapButton.setOnClickListener {        // 맵 메뉴로 이동
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        mainActivityBinding.mainTemaButton.setOnClickListener {       // 테마 메뉴로 이동
            val intent = Intent(this, TemaActivity::class.java)
            startActivity(intent)
        }
        mainActivityBinding.mainPlanButton.setOnClickListener {       // 여행계획 메뉴로 이동
            val intent = Intent(this, PlanActivity::class.java)
            startActivity(intent)
        }
        mainActivityBinding.mainTicketButton.setOnClickListener {     // 티켓예매 메뉴로 이동
            val intent = Intent(this, TicketActivity::class.java)
            startActivity(intent)
        }

        // 뒤로가기 버튼을 눌렀을 때 한번 더 확인
        var initTime = 0L
        val toast = Toast.makeText(this, "뒤로가기를 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(System.currentTimeMillis() - initTime > 3000) { // 만약 처음 5초가 지난 상황일 때 initTime은 0이니까 5000 - 0 > 3000 이므로 토스트 메시지 띄우고
                    toast.show()                                   // initTime을 현재까지 지난 시간으로 초기화, 만약 뒤로가기 버튼 한번 더 누르면
                    initTime = System.currentTimeMillis()          // 현재 지난 시간 - initTime은 < 3000 이므로 else문 발동 -> 앱 종료
                }
                else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback = callback)
    }
    
    //자동 슬라이드를 위한 페이지 변경하기
    fun setPage(){
        if(currentPosition==4) currentPosition=0
        viewPager_mainImages.setCurrentItem(currentPosition,true)
        currentPosition+=1
        }

    //자동 슬라이드 : 3초마다 페이지 넘기기
    inner class PagerRunnable:Runnable{
        override fun run() {
            while(true){
                Thread.sleep(3000)
                handler.sendEmptyMessage(0)
            }
        }
    }

    // 뷰 페이저에 적용할 이미지 배열리스트
    private fun getMainImages(): ArrayList<Int> {
        return arrayListOf<Int>(
            R.drawable.mainimage1,
            R.drawable.mainimage2,
            R.drawable.mainimage3,
            R.drawable.mainimage4
        )
    }
}