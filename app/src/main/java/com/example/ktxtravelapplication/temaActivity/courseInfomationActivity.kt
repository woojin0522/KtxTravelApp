package com.example.ktxtravelapplication.temaActivity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityCourseInfomationBinding
import com.example.ktxtravelapplication.mapActivity.InfoFullImageActivity
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.temaFragments.courseDescriptionFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.courseMapFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.festivalDescriptionFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.festivalMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL

class courseInfomationActivity : AppCompatActivity() {
    lateinit var courseDataList: MutableList<courseDatas>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCourseInfomationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.courseInfoToolbar)
        supportActionBar!!.setTitle("")

        courseDataList = mutableListOf()

        binding.courseInfoBackBtn.setOnClickListener {
            finish()
        }

        val title = "여행코스 상세정보"
        val courseName = intent.getStringExtra("courseName").toString()
        val contentId = intent.getIntExtra("contentId",0)
        val imageUrl = intent.getStringExtra("imageUrl").toString()
        val mapx = intent.getDoubleExtra("mapX", 0.0)
        val mapy = intent.getDoubleExtra("mapY", 0.0)
        val lineName = intent.getStringExtra("lineName").toString()
        val nearStation = intent.getStringExtra("nearStation").toString()

        courseDataList.add(courseDatas(courseName, contentId, imageUrl, mapx, mapy, nearStation))

        binding.courseInfoTabViewPager2.adapter = CourseInfoViewPagerAdapter(this@courseInfomationActivity, courseDataList)
        binding.courseInfoTabViewPager2.isUserInputEnabled = false

        TabLayoutMediator(binding.courseInfoTabLayout, binding.courseInfoTabViewPager2) { tab, position ->
            when(position){
                0 -> tab.text = "설명"
                1 -> tab.text = "코스 지도"
            }
        }.attach()

        binding.courseInfoTitle.text = title

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(imageUrl) // 불러올 이미지 url
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.courseInfoImage) // 이미지를 넣을 뷰

        // 이미지 클릭시 확대
        binding.courseInfoImage.setOnClickListener {
            val intent = Intent(this, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            val opt = ActivityOptions.makeSceneTransitionAnimation(this, it, "imgTrans")
            startActivity(intent, opt.toBundle())
        }

        binding.courseInfoTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> binding.courseInfoImage.visibility = View.VISIBLE
                    1 -> binding.courseInfoImage.visibility = View.GONE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}

// 뷰페이저 어댑터
class CourseInfoViewPagerAdapter(activity: FragmentActivity, courseDataList: MutableList<courseDatas>): FragmentStateAdapter(activity) {
    val fragments: List<Fragment>
    init {
        fragments = listOf(courseDescriptionFragment.newInstance(courseDataList), courseMapFragment.newInstance())
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}