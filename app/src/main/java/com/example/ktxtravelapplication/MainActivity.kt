package com.example.ktxtravelapplication

import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ktxtravelapplication.databinding.ActivityMainBinding
import com.example.ktxtravelapplication.databinding.MainImageViewPager2Binding

class MainActivity : AppCompatActivity() {
    lateinit var viewPager_mainImages: ViewPager2
    var currentPosition = 0

    val handler= Handler(Looper.getMainLooper()) {
        setPage()
        true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        viewPager_mainImages = findViewById(R.id.mainViewPager)
        viewPager_mainImages.adapter = MainImageViewPagerAdapter(getMainImages())
        viewPager_mainImages.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        //뷰페이저 쓰레드
        val thread=Thread(PagerRunnable())
        thread.start()
    }

    //페이지 변경하기
    fun setPage(){
        if(currentPosition==4) currentPosition=0
        viewPager_mainImages.setCurrentItem(currentPosition,true)
        currentPosition+=1
    }

    //3초마다 페이지 넘기기
    inner class PagerRunnable:Runnable{
        override fun run() {
            while(true){
                Thread.sleep(3000)
                handler.sendEmptyMessage(0)
            }
        }
    }

    // 이미지 배열리스트
    private fun getMainImages(): ArrayList<Int> {
        return arrayListOf<Int>(
            R.drawable.mainimage1,
            R.drawable.mainimage2,
            R.drawable.mainimage3,
            R.drawable.mainimage4
        )
    }
}

// 뷰 어댑터
class MainImageViewPagerAdapter(var mainImages: ArrayList<Int>) :
    RecyclerView.Adapter<MainImageViewPagerAdapter.PagerViewHolder>() {
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