package com.example.ktxtravelapplication.mapActivity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityInfomationPlusBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.database.snapshots
import com.google.firebase.database.values
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL

class InfomationPlusActivity : AppCompatActivity() {
    lateinit var database: FirebaseDatabase
    lateinit var editor: SharedPreferences.Editor
    lateinit var lineName: String
    lateinit var strInfoNum: String
    var likeCheck = false
    companion object{
        lateinit var pref: SharedPreferences
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInfomationPlusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getPreferences(MODE_PRIVATE)
        editor = pref.edit()

        setSupportActionBar(binding.infoToolbar)
        supportActionBar!!.setTitle("")

        val infoTitle = intent.getStringExtra("infoTitle")
        val infoName = intent.getStringExtra("infoName")
        val infoAddress = intent.getStringExtra("infoAddress")
        val infoImage = intent.getStringExtra("infoImage")
        val infoTel = intent.getStringExtra("infoTel")
        val infoDist = intent.getIntExtra("infoDist", 0)
        val contentId = intent.getIntExtra("infoContentId", 0)
        val contentTypeId = intent.getIntExtra("infoContentTypeId", 0)
        lineName = intent.getStringExtra("lineName").toString()
        strInfoNum = ""

        // 관광지 설명과 홈페이지 불러오기
        fun fetchInfoXML(contentId: Int, contentTypeId: Int) {
            val dialog = LoadingDialog(this@InfomationPlusActivity)
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

                    if(homepageUrl3 != "") {
                        binding.infoPlusHomepage.text = "홈페이지 이동하기"
                        binding.infoPlusHomepage.setOnClickListener {
                            binding.infoPlusHomepage.setTextColor(Color.BLUE)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl3))
                            startActivity(intent)
                        }
                    }
                    else {
                        binding.infoPlusHomepage.text = "홈페이지를 찾을 수 없습니다."
                    }

                    binding.infoPlusDescription.text = overview.replace("<br>","")
                        .replace("<br />","")
                    dialog.dismiss()
                }
            }
            getDangerGrade().execute()
        }

        binding.infoBackBtn.setOnClickListener {
            finish()
        }

        if(infoTitle == "역 상세정보"){                                // 상세 정보창이 역 정보일 경우 실행
            binding.infoPlusTel.isVisible = false
            binding.infoPlusHomepage.isVisible = false
            binding.infoPlusDescription.isVisible = false
            binding.infoAllTab.visibility = View.VISIBLE
            binding.infoPlusName.text = "역명 : " + infoName + "역"
            val lineArray = intent.getSerializableExtra("lineList") as ArrayList<StationPositions>
            lineArray.sortBy { it.stationNum }

            binding.infoPlusLikeLayout.visibility = View.GONE
            binding.infoTabViewPager2.adapter = InfoViewPagerAdapter(this, lineArray, lineName, infoName.toString(), infoAddress.toString(), infoImage.toString())

            TabLayoutMediator(binding.infoTabLayout, binding.infoTabViewPager2) { tab, position ->
                when(position) {
                    0 -> tab.text = "노선"
                    1 -> tab.text = "주변관광지 추천"
                }
            }.attach()
        }
        else{                                                        // 상세 정보창이 관광지, 음식점등의 정보일 경우 실행
            binding.infoPlusLikeLayout.visibility = View.VISIBLE
            binding.infoPlusName.text = infoName

            val infoType = intent.getStringExtra("infoType")

            var strLikeCount = ""
            var intLikeCount = 0
            database = FirebaseDatabase.getInstance()
            val myRef = database.getReference(infoType.toString())
            CoroutineScope(Dispatchers.IO).launch {      // 추천수를 불러옴. 추가로 추천수 변동시 재호출함.
                myRef.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        runBlocking {
                            for(shot in snapshot.children) {
                                //if(shot.key.toString() == lineName){
                                    for(shotChild in shot.children){
                                        if(shotChild.child("contentId").value.toString() == contentId.toString()){
                                            strLikeCount = shotChild.child("likeCount").value.toString()
                                            intLikeCount = strLikeCount.toInt()
                                            strInfoNum = shotChild.key.toString()
                                        }
                                    }
                            }
                        }
                        likeCheck = pref.getBoolean("추천 체크 ${contentId}", false)
                        if(likeCheck) binding.infoPlusLikeBtn.text = "추천취소"
                        else binding.infoPlusLikeBtn.text = "추천하기"

                        binding.infoPlusLikeCount.text = "추천 : ${strLikeCount}"
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            fetchInfoXML(contentId, contentTypeId)

            fun dataInsert(check: Boolean, toastText: String, context: Context){    //추천하기 버튼을 클릭하였을 때 작동하는 함수
                likeCheck = check

                editor.putBoolean("추천 체크 ${contentId}", likeCheck)
                editor.apply()

                myRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(shot in snapshot.children){
                            for(tourData in shot.children){
                                if(tourData.child("contentId").value.toString() == contentId.toString()){
                                    myRef.child(shot.key.toString()).child(tourData.key.toString()).child("likeCount").setValue(intLikeCount)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                if(check) binding.infoPlusLikeBtn.text = "추천취소"
                else binding.infoPlusLikeBtn.text = "추천하기"
            }

            binding.infoPlusLikeBtn.setOnClickListener {
                if(likeCheck == false) {
                    intLikeCount++

                    dataInsert(true, "추천되었습니다.", it.context)
                }
                else {
                    if(intLikeCount>=1) {
                        intLikeCount--

                        dataInsert(false, "추천이 취소 되었습니다.", it.context)
                    }
                    else{
                        likeCheck = false

                        Toast.makeText(it.context, "추천수가 0일 경우 추천취소가 불가능합니다.", Toast.LENGTH_SHORT).show()
                        binding.infoPlusLikeBtn.text = "추천하기"
                    }
                }
            }

            val recommendInfoState = intent.getBooleanExtra("recommendInfoState", false)

            if(recommendInfoState){
                val stationName = intent.getStringExtra("stationName")
                val stationAddress = intent.getStringExtra("stationAddress")
                val lineList = intent.getSerializableExtra("lineList") as ArrayList<StationPositions>
                val stationImage = intent.getStringExtra("stationImage")

                fun backActive(){
                    val intent = Intent(this@InfomationPlusActivity, InfomationPlusActivity::class.java)
                    intent.putExtra("infoTitle", "역 상세정보")
                    intent.putExtra("infoName", stationName)
                    intent.putExtra("infoAddress", stationAddress)
                    intent.putExtra("lineList", lineList)
                    intent.putExtra("lineName", lineName)
                    intent.putExtra("infoImage", stationImage)

                    startActivity(intent)
                    finish()
                }
                // 뒤로가기
                val callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backActive()
                    }
                }
                onBackPressedDispatcher.addCallback(this, callback)

                binding.infoBackBtn.setOnClickListener {
                    backActive()
                }
            }
        }

        binding.infoTitle.text = infoTitle
        binding.infoPlusTel.text = infoTel
        binding.infoPlusAddress.text = infoAddress
        binding.infoPlusDescription.movementMethod = ScrollingMovementMethod.getInstance()
        binding.infoPlusHomepage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if(infoTel != "") {
            binding.infoPlusTel.text = "전화번호를 찾을 수 없습니다."
        }
        if(infoDist != 0) {
            binding.infoPlusDist.text = "역에서의 거리 : ${infoDist}m"
        }
        else {
            binding.infoPlusDist.isVisible = false
        }

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(infoImage) // 불러올 이미지 url
            .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.infoImage) // 이미지를 넣을 뷰

        // 이미지 클릭시 확대
        binding.infoImage.setOnClickListener {
            val intent = Intent(this, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", infoImage)
            val opt = ActivityOptions.makeSceneTransitionAnimation(this, it, "imgTrans")
            startActivity(intent, opt.toBundle())
        }
    }
}

// 뷰페이저 어댑터
class InfoViewPagerAdapter(activity: FragmentActivity, lineArray: ArrayList<StationPositions>, lineName: String, stationName: String, stationAddress: String, stationImage: String): FragmentStateAdapter(activity) {
    val fragments: List<Fragment>
    init {
        fragments = listOf(InfoLineFragment.newInstance(lineArray,lineName), RecommendInfoFragment.newInstance(stationName, lineName, lineArray, stationAddress, stationImage))
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}