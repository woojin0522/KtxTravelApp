package com.example.ktxtravelapplication.temaActivity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityFestivalInfomationBinding
import com.example.ktxtravelapplication.mapActivity.InfoFullImageActivity
import com.example.ktxtravelapplication.mapActivity.InfoViewPagerAdapter
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.temaFragments.festivalDescriptionFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.festivalMapFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.io.StringReader
import java.net.URL

class festivalInfomationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFestivalInfomationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.festivalInfoToolbar)
        supportActionBar!!.setTitle("")

        binding.festivalInfoBackBtn.setOnClickListener {
            finish()
        }

        val title = "축제/공연/행사 상세정보"
        val festivalName = intent.getStringExtra("festivalName").toString()
        val festivalAddr = intent.getStringExtra("festivalAddr").toString()
        val festivalTel = intent.getStringExtra("festivalTel").toString()
        val contentId = intent.getIntExtra("contentId",0)
        val contentTypeId = intent.getIntExtra("contentTypeId",0)
        val startDate = intent.getIntExtra("startDate",0)
        val endDate = intent.getIntExtra("endDate",0)
        val imageUrl = intent.getStringExtra("imageUrl").toString()
        val lineName = intent.getStringExtra("lineName").toString()
        val nearStation = intent.getStringExtra("nearStation").toString()
        val festivalMapx = intent.getDoubleExtra("mapx", 0.0)
        val festivalMapy = intent.getDoubleExtra("mapy", 0.0)

        var stationMapx = 0.0
        var stationMapy = 0.0

        val festivalMapDataList = mutableListOf<festivalMapData>()
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("ktxLines").child(lineName)
        myRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children){
                    if(shot.child("stationName").value.toString() == nearStation){
                        stationMapx = shot.child("longitude").value.toString().toDouble()
                        stationMapy = shot.child("latitude").value.toString().toDouble()
                    }
                }

                festivalMapDataList.add(
                    festivalMapData(festivalName, festivalMapx, festivalMapy,
                    nearStation, stationMapx, stationMapy)
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val festivalInfoList = arrayListOf<String>(festivalName, festivalAddr, festivalTel, contentId.toString(), contentTypeId.toString(),
            startDate.toString(), endDate.toString(), lineName, nearStation)

        // 관광지 설명과 홈페이지 불러오기
        fun fetchInfoXML(contentId: Int, contentTypeId: Int) {
            val dialog = LoadingDialog(this)
            dialog.show()

            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = ""
            val defaultYN = "Y"
            val firstImageYN = "N"
            val areacodeYN = "N"
            val catcodeYN = "N"
            val addrinfoYN = "N"
            val mapinfoYN = "N"
            val overviewYN = "Y"
            val num_of_rows = 10
            val page_no = 1
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/detailCommon1"

            val requestUrl = serviceUrl + "?MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                    "&_type=" + type + "&contentId=" + contentId + "&contentTypeId=" + contentTypeId +
                    "&defaultYN=" + defaultYN + "&firstImageYN=" + firstImageYN + "&areacodeYN=" + areacodeYN +
                    "&catcodeYN=" + catcodeYN + "&addrinfoYN=" + addrinfoYN + "&mapinfoYN=" + mapinfoYN +
                    "&overviewYN=" + overviewYN + "&numOfRows=" + num_of_rows + "&pageNo=" + page_no + "&serviceKey=" + serviceKey

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

                    var tagHomepage = false
                    var tagOverview = false
                    var homepageUrl = ""
                    var overview = ""

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

                            if(tagName.equals("homepage")) tagHomepage = true
                            else if(tagName.equals("overview")) tagOverview = true
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagHomepage) {
                                homepageUrl = xpp.text
                                tagHomepage = false
                            }
                            else if(tagOverview) {
                                overview = xpp.text
                                tagOverview = false
                            }
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }
                    var homepageUrl1 = homepageUrl.split("href=")
                    var homepageUrl3 = ""
                    if(homepageUrl1.size > 1) {
                        var homepageUrl2 = homepageUrl1[1].split('"')
                        homepageUrl3 = homepageUrl2[1]
                    }

                    dialog.dismiss()
                    festivalInfoList.add(homepageUrl3)
                    festivalInfoList.add(overview)

                    binding.festivalInfoTabViewPager2.adapter = FestivalInfoViewPagerAdapter(this@festivalInfomationActivity, festivalInfoList, festivalMapDataList)
                    binding.festivalInfoTabViewPager2.isUserInputEnabled = false

                    TabLayoutMediator(binding.festivalInfoTabLayout, binding.festivalInfoTabViewPager2) { tab, position ->
                        when(position) {
                            0 -> tab.text = "설명"
                            1 -> tab.text = "개최 장소"
                        }
                    }.attach()
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoXML(contentId, contentTypeId)

        binding.festivalInfoTitle.text = title

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(imageUrl) // 불러올 이미지 url
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.festivalInfoImage) // 이미지를 넣을 뷰

        // 이미지 클릭시 확대
        binding.festivalInfoImage.setOnClickListener {
            val intent = Intent(this, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            val opt = ActivityOptions.makeSceneTransitionAnimation(this, it, "imgTrans")
            startActivity(intent, opt.toBundle())
        }
    }
}

// 뷰페이저 어댑터
class FestivalInfoViewPagerAdapter(activity: FragmentActivity, festivalInfoList: ArrayList<String>, festivalMapDataList: MutableList<festivalMapData>): FragmentStateAdapter(activity) {
    val fragments: List<Fragment>
    init {
        fragments = listOf(festivalDescriptionFragment.newInstance(festivalInfoList), festivalMapFragment.newInstance(festivalMapDataList))
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

data class festivalMapData(
    val festivalName: String,
    val festivalMapx: Double,
    val festivalMapy: Double,
    val stationName: String,
    val stationMapx: Double,
    val stationMapy: Double
) : Serializable