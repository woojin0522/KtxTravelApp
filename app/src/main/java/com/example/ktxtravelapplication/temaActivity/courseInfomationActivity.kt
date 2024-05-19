package com.example.ktxtravelapplication.temaActivity

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityCourseInfomationBinding
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.google.android.material.tabs.TabLayoutMediator
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL

class courseInfomationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCourseInfomationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.courseInfoToolbar)
        supportActionBar!!.setTitle("")

        binding.courseInfoBackBtn.setOnClickListener {
            finish()
        }

        val title = "여행코스 상세정보"
        val courseName = intent.getStringExtra("courseName").toString()
        val contentId = intent.getIntExtra("contentId",0)
        val imageUrl = intent.getStringExtra("imageUrl").toString()
        val lineName = intent.getStringExtra("lineName").toString()
        val nearStation = intent.getStringExtra("nearStation").toString()

        binding.courseInfoTitle.text = title
        binding.courseInfoName.text = "여행코스명 : ${courseName}"
        binding.courseInfoNearStation.text = "주변 KTX역 : ${nearStation}역"

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(imageUrl) // 불러올 이미지 url
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.courseInfoImage) // 이미지를 넣을 뷰

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

                    var tagOverview = false
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

                            if(tagName.equals("overview")) tagOverview = true
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagOverview) {
                                overview = xpp.text
                                tagOverview = false
                            }
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }
                    binding.courseInfoDescription.text = overview
                    binding.courseInfoDescription.movementMethod = ScrollingMovementMethod.getInstance()

                    dialog.dismiss()
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoXML(contentId, 25)

        fun fetchInfoXML2(contentId: Int, contentTypeId: Int) {
            val dialog = LoadingDialog(this)
            dialog.show()

            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = ""
            val num_of_rows = 10
            val page_no = 1
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/detailIntro1"

            val requestUrl = serviceUrl + "?MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                    "&_type=" + type + "&contentId=" + contentId + "&contentTypeId=" + contentTypeId+
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

                    var tagDistance = false
                    var distance = ""
                    var tagTakeTime = false
                    var takeTime = ""

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

                            if(tagName.equals("distance")) tagDistance = true
                            else if(tagName.equals("taketime")) tagTakeTime = true
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagDistance) {
                                distance = xpp.text
                                tagDistance = false
                            }
                            else if(tagTakeTime) {
                                takeTime = xpp.text
                                tagTakeTime = false
                            }
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }
                    binding.courseInfoDistance.text = "코스 총 길이 : ${distance}"
                    binding.courseInfoTakeTime.text = "코스 총 소요시간 : ${takeTime}"
                    dialog.dismiss()
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoXML2(contentId, 25)

        binding.courseInfoHomepage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.courseInfoHomepage.setOnClickListener {
            binding.courseInfoHomepage.setTextColor(Color.BLUE)
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://korean.visitkorea.or.kr/search/search_list.do?keyword=${courseName}"))
            startActivity(intent)
        }
    }
}