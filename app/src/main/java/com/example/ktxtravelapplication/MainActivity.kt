package com.example.ktxtravelapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ktxtravelapplication.databinding.ActivityMainBinding
import com.example.ktxtravelapplication.databinding.MainImageViewPager2Binding

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

        // 자동 슬라이드를 위한 뷰페이저 쓰레드
        val thread=Thread(PagerRunnable())
        thread.start()

        // 메인화면 버튼 클릭시 이벤트 리스너
        mainActivityBinding.mainMapButton.setOnClickListener { }
        mainActivityBinding.mainTemaButton.setOnClickListener {  }
        mainActivityBinding.mainPlanButton.setOnClickListener {  }
        mainActivityBinding.mainTicketButton.setOnClickListener {  }

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

// 뷰 어댑터 선언, 매개변수: 적용할 이미지 배열리스트
class MainImageViewPagerAdapter(var mainImages: ArrayList<Int>) :
    RecyclerView.Adapter<MainImageViewPagerAdapter.PagerViewHolder>() { //리사이클러뷰 어댑터를 적용.
    //뷰 홀더 선언부
    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.main_image_view_pager2, parent, false)) {
        val mainImages = itemView.findViewById<ImageView>(R.id.mainImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder = PagerViewHolder((parent))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.mainImages.setImageResource(mainImages[position])
    }

    override fun getItemCount(): Int = mainImages.size
}