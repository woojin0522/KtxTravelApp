package com.example.ktxtravelapplication.temaActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ktxtravelapplication.databinding.ActivityTemaBinding
import com.example.ktxtravelapplication.temaActivity.temaFragments.temaCourseFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.temaFestivalFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.temaSeasonsFragment
import com.google.android.material.tabs.TabLayoutMediator

class TemaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        val binding = ActivityTemaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바를 액션바 대신
        setSupportActionBar(binding.temaToolbar)
        supportActionBar?.setTitle("")

        // 상단바 뒤로가기 버튼 눌렀을 때
        binding.temaBackBtn.setOnClickListener {
            finish()
        }

        binding.temaTabViewPager2.adapter = TemaViewPagerAdapter(this)
        
        //탭 레이아웃에서 탭을 선택할 때 이벤트 리스너
        TabLayoutMediator(binding.temaTabLayout, binding.temaTabViewPager2) { tab, position ->
            when(position) {
                0 -> tab.text = "계절"
                1 -> tab.text = "추천코스"
                2 -> tab.text = "현재 진행중인 축제"
            }
        }.attach()
    }
}

// 뷰페이저 어댑터
class TemaViewPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    val fragments: List<Fragment>
    init {
        fragments = listOf(temaSeasonsFragment(), temaCourseFragment(), temaFestivalFragment())
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}


