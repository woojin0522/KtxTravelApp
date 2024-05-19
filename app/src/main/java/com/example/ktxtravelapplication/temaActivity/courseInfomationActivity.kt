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
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import java.io.Serializable
import java.io.StringReader
import java.net.URL

class courseInfomationActivity : AppCompatActivity() {
    lateinit var courseDataList: MutableList<courseDatas>
    lateinit var subCourseDataList: MutableList<subCourseDatas>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCourseInfomationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.courseInfoToolbar)
        supportActionBar!!.setTitle("")

        courseDataList = mutableListOf()
        subCourseDataList = mutableListOf()

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
        val stationList = intent.getSerializableExtra("stationList") as MutableList<stationDatas>

        courseDataList.add(courseDatas(courseName, contentId, imageUrl, mapx, mapy, nearStation))

        fun fetchInfoXML(contentId: Int, contentTypeId: Int, nearStation: String) {
            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = ""
            val num_of_rows = 20
            val page_no = 1
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/detailInfo1"

            val requestUrl = serviceUrl + "?MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                    "&_type=" + type + "&contentId=" + contentId + "&contentTypeId=" + contentTypeId +
                    "&numOfRows=" + num_of_rows + "&pageNo=" + page_no + "&serviceKey=" + serviceKey

            lateinit var page : String // url 주소 통해 전달받은 내용 저장할 변수

            class getDangerGrade: AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg p0: Void?): Void? {

                    // 데이터 스트림 형태로 가져오기
                    val stream = URL(requestUrl).openStream()
                    val bufReader = BufferedReader(InputStreamReader(stream, "UTF-8"))

                    //한줄씩 읽어서 스트링 형태로 바꾼 후 page에 저장
                    page = ""
                    var line = bufReader.readLine()
                    while(line != null){
                        page += line
                        line = bufReader.readLine()
                    }

                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)

                    var tagSubNum = false
                    var tagSubContentId = false
                    var tagSubName = false

                    var subNum = 0
                    var subContentId = 0
                    var subName = ""

                    var factory = XmlPullParserFactory.newInstance() // 파서 생성
                    factory.isNamespaceAware = true // 파서 설정
                    var xpp = factory.newPullParser() // xml 파서

                    // 파싱하기
                    xpp.setInput(StringReader(page))

                    // 파싱 진행
                    var eventType = xpp.eventType
                    while(eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT){}
                        else if(eventType == XmlPullParser.START_TAG) {
                            var tagName = xpp.name

                            if(tagName.equals("subnum")) tagSubNum = true
                            else if(tagName.equals("subcontentid")) tagSubContentId = true
                            else if(tagName.equals("subname")) tagSubName = true
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagSubNum) {
                                subNum = xpp.text.toString().toInt()
                                tagSubNum = false
                            }
                            else if(tagSubContentId) {
                                subContentId = xpp.text.toString().toInt()
                                tagSubContentId = false
                            }
                            else if(tagSubName) {
                                subName = xpp.text

                                subCourseDataList.add(subCourseDatas(subNum, subContentId, subName, nearStation))

                                tagSubName = false
                            }
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }
                    subCourseDataList.sortBy { it.subNum }
                    binding.courseInfoTabViewPager2.adapter = CourseInfoViewPagerAdapter(this@courseInfomationActivity, courseDataList, subCourseDataList, stationList)

                    binding.courseInfoTabViewPager2.isUserInputEnabled = false

                    TabLayoutMediator(binding.courseInfoTabLayout, binding.courseInfoTabViewPager2) { tab, position ->
                        when(position){
                            0 -> tab.text = "설명"
                            1 -> tab.text = "코스 지도"
                        }
                    }.attach()
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoXML(contentId, 25, nearStation)

        binding.courseInfoTitle.text = title

        /*// glide 라이브러리를 이용한 url 이미지 불러오기
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
        })*/
    }
}

// 뷰페이저 어댑터
class CourseInfoViewPagerAdapter(
    activity: FragmentActivity,
    courseDataList: MutableList<courseDatas>,
    subCourseDataList: MutableList<subCourseDatas>,
    stationList: MutableList<stationDatas>
): FragmentStateAdapter(activity) {
    val fragments: List<Fragment>
    init {
        fragments = listOf(courseDescriptionFragment.newInstance(courseDataList), courseMapFragment.newInstance(subCourseDataList, stationList))
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

data class subCourseDatas(
    val subNum: Int,
    val contentId: Int,
    val title: String,
    val nearStation: String
) : Serializable