package com.example.ktxtravelapplication.mapActivity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityMapBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.KtxLinesList
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
import com.example.ktxtravelapplication.mapActivity.tourData.TourData
import com.example.ktxtravelapplication.temaActivity.festivalAdapter
import com.example.ktxtravelapplication.temaActivity.festivalDatas
import com.example.ktxtravelapplication.temaActivity.festivalInfomationActivity
import com.example.ktxtravelapplication.temaActivity.stationDatas
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL
import java.text.SimpleDateFormat
import kotlin.io.path.Path

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var locationSource: FusedLocationSource
    lateinit var naverMap: NaverMap
    lateinit var tour_line: MutableList<StationPositions>
    lateinit var markers: MutableList<Marker>
    lateinit var tour_markers: MutableList<Marker>
    lateinit var tourList: MutableList<TourData>
    lateinit var pathList: MutableList<PathOverlay>
    lateinit var line: String
    lateinit var infoType: String
    lateinit var database: FirebaseDatabase
    lateinit var binding: ActivityMapBinding
    lateinit var saveLineName: String
    lateinit var saveInfoType: String
    lateinit var lineList: MutableList<StationPositions>
    var maxDist = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveLineName = ""
        saveInfoType = ""

        // 액션바를 툴바로 교체
        setSupportActionBar(binding.mapToolbar)
        supportActionBar!!.setTitle("")

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

        // 뒤로가기 작동 함수
        fun backAction() {
            val returnIntent = Intent() // 인텐트 생성
            returnIntent.putExtra("ktxLine", line) // 선택된 ktx노선 값을 넘김
            returnIntent.putExtra("infoType", infoType) // 선택된 정보타입 값을 넘김
            returnIntent.putExtra("maxDist", maxDist)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        var initTime = 0L
        val toast = Toast.makeText(this, "지도화면을 나가시려면 뒤로가기를 한번 더 눌러주세요.", Toast.LENGTH_SHORT)
        // 뒤로가기 버튼 클릭 리스너
        binding.mapBackBtn.setOnClickListener{
            if(System.currentTimeMillis() - initTime > 3000) {
                toast.show()
                initTime = System.currentTimeMillis()
            }
            else{
                backAction()
            }
        }

        // 드로어 레이아웃 열고 닫기 버튼
        binding.mapDrawerBtn.setOnClickListener {
            if(binding.mapNavView.isActivated == false){
                binding.mapDrawer.openDrawer(binding.mapNavView, true)
                binding.mapNavView.isActivated = true
            }
            else{
                binding.mapDrawer.closeDrawer(binding.mapNavView, true)
                binding.mapNavView.isActivated = false
            }
        }

        // 뒤로가기 버튼
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 드로어 레이아웃이 열려있을 떄 뒤로가기 버튼을 누를경우 드로어 레이아웃을 닫는다.
                if(binding.mapNavView.isActivated == true) {
                    binding.mapDrawer.closeDrawer(binding.mapNavView, true)
                    binding.mapNavView.isActivated = false
                }
                // 드로어 레이아웃이 닫혀있다면 메인화면으로 돌아간다.
                else{
                    if(System.currentTimeMillis() - initTime > 3000) {
                        toast.show()
                        initTime = System.currentTimeMillis()
                    }
                    else{
                        backAction()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // ktx노선 배열
        val ktxLines = arrayOf("경부선", "호남선", "경전선", "전라선", "강릉선", "중앙선", "중부내륙선", "동해선")
        // 현재 선택된 라디오버튼 변수
        var lineChecked = 0
        // 마커 리스트 선언
        markers = mutableListOf<Marker>()
        tour_line = mutableListOf()

        tour_markers = mutableListOf()
        tourList = mutableListOf()

        // ----------------------------------파이어스토어 사용시..--------------------------------
        database = FirebaseDatabase.getInstance()
        /* ktx노선 정보를 파이어베이스에 쓰기
        val myRef = database.getReference("ktxLines")

        fun firebaseInsert(ktxLinesList: MutableList<StationPositions>) {
            var lineName = ""
            if(ktxLinesList == KtxLinesList().gyeongbuLine) lineName = "gyeongbuLine"
            else if(ktxLinesList == KtxLinesList().donghaeLine) lineName = "donghaeLine"
            else if(ktxLinesList == KtxLinesList().gangneungLine) lineName = "gangneungLine"
            else if(ktxLinesList == KtxLinesList().jungangLine) lineName = "jungangLine"
            else if(ktxLinesList == KtxLinesList().jeollaLine) lineName = "jeollaLine"
            else if(ktxLinesList == KtxLinesList().gyeongjeonLine) lineName = "gyeongjeonLine"
            else if(ktxLinesList == KtxLinesList().honamLine) lineName = "honamLine"
            else if(ktxLinesList == KtxLinesList().jungbuNaeryukLine) lineName = "jungbuNaeryukLine"

            for(i in 0..ktxLinesList.size - 1) {
                val stationNum = ktxLinesList[i].stationNum
                val stationName = ktxLinesList[i].stationName
                val stationEngName = ktxLinesList[i].stationEngName
                val stationAddress = ktxLinesList[i].stationAddress
                val latitude = ktxLinesList[i].latitude
                val longitude = ktxLinesList[i].longitude
                val stationInfomation = ktxLinesList[i].stationInfomation
                val likeCount = ktxLinesList[i].likeCount

                myRef.child(lineName).child(stationEngName).child("stationNum").setValue(stationNum)
                myRef.child(lineName).child(stationEngName).child("stationName").setValue(stationName)
                myRef.child(lineName).child(stationEngName).child("stationEngName").setValue(stationEngName)
                myRef.child(lineName).child(stationEngName).child("stationAddress").setValue(stationAddress)
                myRef.child(lineName).child(stationEngName).child("latitude").setValue(latitude)
                myRef.child(lineName).child(stationEngName).child("longitude").setValue(longitude)
                myRef.child(lineName).child(stationEngName).child("stationInfomation").setValue(stationInfomation)
                myRef.child(lineName).child(stationEngName).child("likeCount").setValue(likeCount)
            }
        }
        firebaseInsert(KtxLinesList().gyeongbuLine)
        firebaseInsert(KtxLinesList().jeollaLine)
        firebaseInsert(KtxLinesList().gangneungLine)
        firebaseInsert(KtxLinesList().gyeongjeonLine)
        firebaseInsert(KtxLinesList().honamLine)
        firebaseInsert(KtxLinesList().jungangLine)
        firebaseInsert(KtxLinesList().jungbuNaeryukLine)
        firebaseInsert(KtxLinesList().donghaeLine)*/
        //---------------------------api 파싱 후 파이어베이스로 데이터 전달-----------------------
        // 관광지 정보 저장
        fun fetchXML(url: String, contentNumber: Int, nearStation: String) {
            lateinit var page : String // url 주소 통해 전달받은 내용 저장할 변수
            //xml 데이터 가져와서 파싱
            // 외부에서 데이터 가져올 때 화면 계속 동작하도록 AsyncTask 이용

            class getDangerGrade: AsyncTask<Void, Void, Void>() {
                //url이용해서 xml 읽어오기
                override fun doInBackground(vararg p0: Void?): Void? {
                    // 데이터 스트림 형태로 가져오기
                    val stream = URL(url).openStream()
                    val bufReader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                    page = bufReader.readLine()
                    /*//한줄씩 읽어서 스트링 형태로 바꾼 후 page에 저장
                    page = ""
                    var line = bufReader.readLine()
                    while(line != null){
                        page += line
                        line = bufReader.readLine()
                    }*/

                    return null
                }

                // 읽어온 xml 파싱하기
                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)

                    val json = JSONObject(page).getJSONObject("response")
                        .getJSONObject("body")
                    if(json.get("items").toString() == ""){}
                    else {
                        val jsonArray = json.getJSONObject("items").getJSONArray("item")
                        for (j in 0..jsonArray.length() - 1) {
                            val jsonObject = jsonArray.getJSONObject(j)
                            var firstimage = jsonObject.getString("firstimage")
                            var title = jsonObject.getString("title")
                            var addr1 = jsonObject.getString("addr1")
                            var addr2 = jsonObject.getString("addr2")
                            var dist = jsonObject.getString("dist")
                            var mapx = jsonObject.getString("mapx")
                            var mapy = jsonObject.getString("mapy")
                            var tel = jsonObject.getString("tel")
                            var contentId = jsonObject.getString("contentid")
                            var contentTypeId = jsonObject.getString("contenttypeid")

                            tourList.add(TourData(title, addr1, addr2, firstimage, dist.toDouble(), mapy.toDouble(), mapx.toDouble(),
                                "","", tel,0, contentId.toInt(), contentTypeId.toInt(), nearStation))
                        }
                    }

                    /*var tagTitle = false
                    var tagAddr1 = false
                    var tagAddr2 = false
                    var tagImage = false
                    var tagDist = false
                    var tagMapY = false
                    var tagMapX = false
                    var tagTel = false
                    var tagContentId = false
                    var tagContentTypeId = false

                    var firstimage = ""
                    var title = ""
                    var addr1 = ""
                    var addr2 = ""
                    var dist = 0.0
                    var mapx = 0.0
                    var mapy = 0.0
                    var tel = ""
                    var infomation = ""
                    var likeCount = 0
                    var contentId = 0
                    var contentTypeId = 0

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

                            if(tagName.equals("title")) tagTitle = true
                            else if(tagName.equals("addr1")) tagAddr1 = true
                            else if(tagName.equals("addr2")) tagAddr2 = true
                            else if(tagName.equals("firstimage")) tagImage = true
                            else if(tagName.equals("dist")) tagDist = true
                            else if(tagName.equals("mapx")) tagMapX = true
                            else if(tagName.equals("mapy")) tagMapY = true
                            else if(tagName.equals("tel")) tagTel = true
                            else if(tagName.equals("contentid")) tagContentId = true
                            else if(tagName.equals("contenttypeid")) tagContentTypeId = true
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagImage) {
                                firstimage = xpp.text
                                tagImage = false
                            }
                            else if(tagTitle) {
                                title = xpp.text
                                tagTitle = false

                                tourList.add(TourData(title, addr1, addr2, firstimage, dist, mapy, mapx,infomation,"",tel,likeCount, contentId, contentTypeId, nearStation))
                            }
                            else if(tagAddr1) {
                                addr1 = xpp.text
                                tagAddr1 = false
                            }
                            else if(tagAddr2) {
                                addr2 = xpp.text
                                tagAddr2 = false
                            }
                            else if(tagDist) {
                                dist = xpp.text.toDouble()
                                tagDist = false
                            }
                            else if(tagMapX){
                                mapx = xpp.text.toDouble()
                                tagMapX = false
                            }
                            else if(tagMapY) {
                                mapy = xpp.text.toDouble()
                                tagMapY = false
                            }
                            else if(tagTel) {
                                tel = xpp.text
                                tagTel = false
                            }
                            else if(tagContentId) {
                                contentId = xpp.text.toInt()
                                tagContentId = false
                            }
                            else if(tagContentTypeId) {
                                contentTypeId = xpp.text.toInt()
                                tagContentTypeId = false
                            }
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }*/

                    //관광지 데이터를 파이어베이스에 저장
                    var myRef = database.getReference("")
                    if(contentNumber == 12) {myRef = database.getReference("tourDatas")}
                    else if(contentNumber == 15) {myRef = database.getReference("festivalDatas")} // 행사
                    else if(contentNumber == 32) {myRef = database.getReference("accommodationDatas")} // 숙박
                    else if(contentNumber == 39) {myRef = database.getReference("foodshopDatas")} // 음식점

                    var lineName = line

                    for(i in 0..tourList.size - 1) {
                        val title = tourList[i].title.replace("[", "(").replace("]", ")").replace(".", "/")
                        val addr1 = tourList[i].addr1.replace("[", "(").replace("]", ")").replace(".", "/")
                        val addr2 = tourList[i].addr2?.replace("[", "(")?.replace("]", ")")?.replace(".", "/")
                        val imageUri = tourList[i].imageUri?.replace("[", "(")?.replace("]", ")")
                        val dist = tourList[i].dist
                        val latitude = tourList[i].latitude
                        val longitude = tourList[i].longitude
                        val infomation = tourList[i].infomation
                        val tel = tourList[i].tel
                        val likeCount = tourList[i].likeCount
                        val homepage = tourList[i].homepageUrl
                        val contentId = tourList[i].contentId
                        val contentTypeId = tourList[i].contentTypeId
                        val nearStationName = tourList[i].nearStation

                        myRef.child(lineName).child(i.toString()).child("title").setValue(title)
                        myRef.child(lineName).child(i.toString()).child("addr1").setValue(addr1)
                        myRef.child(lineName).child(i.toString()).child("addr2").setValue(addr2)
                        myRef.child(lineName).child(i.toString()).child("imageUri").setValue(imageUri)
                        myRef.child(lineName).child(i.toString()).child("dist").setValue(dist)
                        myRef.child(lineName).child(i.toString()).child("latitude").setValue(latitude)
                        myRef.child(lineName).child(i.toString()).child("longitude").setValue(longitude)
                        myRef.child(lineName).child(i.toString()).child("infomation").setValue(infomation)
                        myRef.child(lineName).child(i.toString()).child("tel").setValue(tel)
                        myRef.child(lineName).child(i.toString()).child("likeCount").setValue(likeCount)
                        myRef.child(lineName).child(i.toString()).child("contentId").setValue(contentId)
                        myRef.child(lineName).child(i.toString()).child("contentTypeId").setValue(contentTypeId)
                        myRef.child(lineName).child(i.toString()).child("homepage").setValue(homepage)
                        myRef.child(lineName).child(i.toString()).child("nearStation").setValue(nearStationName)
                    }
                }
            }
            Toast.makeText(this@MapActivity, "데이터베이스 저장완료", Toast.LENGTH_SHORT).show()
            getDangerGrade().execute()
        }

        fun tourMarkerSetting(contentNumber: Int){
            val num_of_rows = 20
            val page_no = 1
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = "json"
            val list_yn = "Y"
            var arrange = "D"
            val radius = "3000"
            val contentTypeId = contentNumber.toString()
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/locationBasedList1"

            if(tourList.isEmpty() == false) {
                tourList.clear()
            }

            for(i in 0..tour_line.size - 1) {
                val mapx = tour_line[i].longitude.toString()
                val mapy = tour_line[i].latitude.toString()
                val stationName = tour_line[i].stationName

                val requestUrl = serviceUrl +
                        "?numOfRows=" + num_of_rows +
                        "&pageNo=" + page_no +
                        "&MobileOS=" + mobile_os +
                        "&MobileApp=" + mobile_app +
                        "&_type=" + type +
                        "&listYN=" + list_yn +
                        "&arrange=" + arrange +
                        "&mapX=" + mapx +
                        "&mapY=" + mapy +
                        "&radius=" + radius +
                        "&contentTypeId=" + contentTypeId +
                        "&serviceKey=" + serviceKey

                fetchXML(requestUrl, contentNumber, stationName)
            }
        }

        //----------------------------------------------------------------------------------

        fun drawClose(){
            binding.mapDrawer.closeDrawer(binding.mapNavView, true)
            binding.mapNavView.isActivated = false
        }

        fun markerInit(){
            for(i in 0..tour_markers.size - 1) { tour_markers[i].map = null }
            for(i in 0..pathList.size - 1){ pathList[i].map = null}
        }

        pathList = mutableListOf()
        // 네비게이션 항목 선택시
        binding.mapNavView.setNavigationItemSelectedListener {
            // 네비게이션에서 ktx노선 항목 클릭시
            if(it.itemId == com.example.ktxtravelapplication.R.id.menu_item1){
                AlertDialog.Builder(this).run {
                    setTitle("ktx 노선 선택")
                    // 라디오 선택 상자
                    setSingleChoiceItems(ktxLines, lineChecked, object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, checked: Int) {
                            // 선택한 노선을 토스트 메시지로 알려줌.
                            Toast.makeText(context, "${ktxLines[checked]}을 선택하셨습니다.", Toast.LENGTH_SHORT).show()
                            binding.currentKtxLines.text = "현재 노선 : ${ktxLines[checked]}"
                            lineChecked = checked
                            // 경부선을 선택
                            if(checked == 0) {
                                markerInit()
                                tour_line = KtxLinesList().gyeongbuLine
                                line = "gyeongbuLine"
                                stationMarkerSetting(line)
                            }
                            // 호남선을 선택
                            else if(checked == 1) {
                                markerInit()
                                tour_line = KtxLinesList().honamLine
                                line = "honamLine"
                                stationMarkerSetting(line)
                            }
                            // 경전선을 선택
                            else if(checked == 2) {
                                markerInit()
                                tour_line = KtxLinesList().gyeongjeonLine
                                line = "gyeongjeonLine"
                                stationMarkerSetting("gyeongjeonLine")
                            }
                            // 전라선을 선택
                            else if(checked == 3){
                                markerInit()
                                tour_line = KtxLinesList().jeollaLine
                                line = "jeollaLine"
                                stationMarkerSetting("jeollaLine")
                            }
                            // 강릉선을 선택
                            else if(checked == 4){
                                markerInit()
                                tour_line = KtxLinesList().gangneungLine
                                line = "gangneungLine"
                                stationMarkerSetting("gangneungLine")
                            }
                            // 중앙선을 선택
                            else if(checked == 5){
                                markerInit()
                                tour_line = KtxLinesList().jungangLine
                                line = "jungangLine"
                                stationMarkerSetting("jungangLine")
                            }
                            // 중부내륙선을 선택
                            else if(checked == 6){
                                markerInit()
                                tour_line = KtxLinesList().jungbuNaeryukLine
                                line = "jungbuNaeryukLine"
                                stationMarkerSetting("jungbuNaeryukLine")
                            }
                            // 동해선을 선택
                            else if(checked == 7){
                                markerInit()
                                tour_line = KtxLinesList().donghaeLine
                                line = "donghaeLine"
                                stationMarkerSetting("donghaeLine")
                            }
                        }
                    })
                    setPositiveButton("닫기", null)
                    show()
                }
            }
            // 네비게이션에서 관광지표시 항목 클릭시
            else if(it.itemId == com.example.ktxtravelapplication.R.id.menu_item2) {
                if(tour_line.isEmpty()){
                    Toast.makeText(this, "노선을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "관광지가 표시됩니다.", Toast.LENGTH_SHORT).show()

                    for(i in 0..tour_markers.size - 1) {
                        tour_markers[i].map = null
                    }
                    drawClose()
                    infoType="tourDatas"

                    //tourMarkerSetting(12)
                    infoMarkerSetting(maxDist)
                    binding.markerDeleteBtn.text = "■ 관광지마커 삭제하기"
                }
            }
            // 축제/공연/행사
            else if(it.itemId == com.example.ktxtravelapplication.R.id.menu_item3){
                if(tour_line.isEmpty()){
                    Toast.makeText(this, "노선을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "축제/공연/행사가 표시됩니다.", Toast.LENGTH_SHORT).show()

                    for(i in 0..tour_markers.size - 1) {
                        tour_markers[i].map = null
                    }
                    drawClose()

                    infoType="festivalDatas"

                    //tourMarkerSetting(15)
                    //infoMarkerSetting(maxDist)
                    festivalMarkerSetting(maxDist)
                    binding.markerDeleteBtn.text = "■ 축제마커 삭제하기"
                }
            }
            // 숙박
            else if(it.itemId == com.example.ktxtravelapplication.R.id.menu_item4){
                if(tour_line.isEmpty()){
                    Toast.makeText(this, "노선을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "숙박이 표시됩니다.", Toast.LENGTH_SHORT).show()

                    for(i in 0..tour_markers.size - 1) {
                        tour_markers[i].map = null
                    }
                    drawClose()

                    infoType="accommodationDatas"

                    //tourMarkerSetting(32)
                    infoMarkerSetting(maxDist)
                    binding.markerDeleteBtn.text = "■ 숙박마커 삭제하기"
                }
            }
            // 음식점
            else if(it.itemId == com.example.ktxtravelapplication.R.id.menu_item5){
                if(tour_line.isEmpty()){
                    Toast.makeText(this, "노선을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "음식점이 표시됩니다.", Toast.LENGTH_SHORT).show()

                    for(i in 0..tour_markers.size - 1) {
                        tour_markers[i].map = null
                    }
                    drawClose()

                    infoType="foodshopDatas"

                    //tourMarkerSetting(39)
                    infoMarkerSetting(maxDist)
                    binding.markerDeleteBtn.text = "■ 음식점마커 삭제하기"
                }
            }
            true
        }

        // 마커삭제버튼
        binding.markerDeleteBtn.setOnClickListener {
            if(tour_markers.isEmpty() == false){
                for(i in 0..tour_markers.size - 1) {
                    tour_markers[i].map = null
                }
                tourList.clear()
                binding.markerDeleteBtn.text = "■ 표시 마커 없음"
            }
        }

        binding.mapSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maxDist = p1 * 500
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                for(i in 0..tour_markers.size - 1) {
                    tour_markers[i].map = null
                }
                if(binding.markerDeleteBtn.text == "■ 표시 마커 없음"){
                    if(infoType == "tourDatas") binding.markerDeleteBtn.text = "■ 관광지마커 삭제하기"
                    if(infoType == "festivalDatas") binding.markerDeleteBtn.text = "■ 축제마커 삭제하기"
                    if(infoType == "accommodationDatas") binding.markerDeleteBtn.text = "■ 숙박마커 삭제하기"
                    if(infoType == "foodshopDatas") binding.markerDeleteBtn.text = "■ 음식점마커 삭제하기"
                }
                binding.mapMaxDistText.text = "${maxDist}m"
                if(maxDist == 0){
                    binding.markerDeleteBtn.text = "■ 표시 마커 없음"
                }
                if(infoType == "festivalDatas") festivalMarkerSetting(maxDist)
                else infoMarkerSetting(maxDist)
            }
        })

        // 권한 가져오기
        var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // mapFragment 설정
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(com.example.ktxtravelapplication.R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(com.example.ktxtravelapplication.R.id.map_fragment, it).commit()
            }

        // 네이버 객체 얻기
        mapFragment.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("lineName", line)
        outState.putString("infoType", infoType)
        outState.putInt("maxDist", maxDist)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        saveLineName = ""
        saveInfoType = ""
        saveLineName = savedInstanceState.getString("lineName","")
        saveInfoType = savedInstanceState.getString("infoType", "")
        maxDist = savedInstanceState.getInt("maxDist", 0)
        super.onRestoreInstanceState(savedInstanceState)
    }

    fun stationLineFun(lineName: String, lineList: MutableList<StationPositions>) {
        val gpsList = mutableListOf<LatLng>()

        fun initStationLine(){ // 처음 경로 세팅
            pathList.add(PathOverlay())
            pathList[0].coords = gpsList
            pathList[0].map = naverMap
        }

        fun SrtStationLine(SrtStationEndNum: Int){ //SRT 경로 세팅
            pathList.add(PathOverlay())
            pathList[1].coords = listOf(
                LatLng(lineList[2].latitude, lineList[2].longitude),
                LatLng(lineList[3].latitude, lineList[3].longitude),
                LatLng(lineList[4].latitude, lineList[4].longitude),
                LatLng(lineList[SrtStationEndNum].latitude, lineList[SrtStationEndNum].longitude),
            )
            pathList[1].map = naverMap
        }

        when(lineName){
            "donghaeLine" -> {
                for(i in 0..lineList.size - 1){
                    if(i >= 2 && i <=4){ }
                    else { gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))}
                }
                initStationLine()
                SrtStationLine(6)
            }
            "gangneungLine" -> {
                for(i in 0..lineList.size - 1) {
                    if(i != 12){
                        gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))
                    }
                }
                initStationLine()

                pathList.add(PathOverlay())
                pathList.add(PathOverlay())
                pathList[2].coords = listOf(
                    LatLng(lineList[11].latitude, lineList[11].longitude),
                    LatLng(lineList[12].latitude, lineList[12].longitude)
                )
                pathList[2].map = naverMap
            }
            "gyeongbuLine" -> {
                for(i in 0..lineList.size -1){
                    if((i >= 2 && i <=4) || i == 7 || i == 12 || (i >= 16 && i <= 18)){ }
                    else { gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))}
                }
                initStationLine()
                SrtStationLine(8)

                pathList.add(PathOverlay())
                pathList[2].coords = listOf(
                    LatLng(lineList[6].latitude, lineList[6].longitude),
                    LatLng(lineList[7].latitude, lineList[7].longitude),
                    LatLng(lineList[8].latitude, lineList[8].longitude)
                )
                pathList[2].map = naverMap

                pathList.add(PathOverlay())
                pathList[3].coords = listOf(
                    LatLng(lineList[11].latitude, lineList[11].longitude),
                    LatLng(lineList[12].latitude, lineList[12].longitude),
                    LatLng(lineList[13].latitude, lineList[13].longitude)
                )
                pathList[3].map = naverMap

                pathList.add(PathOverlay())
                pathList[4].coords = listOf(
                    LatLng(lineList[13].latitude, lineList[13].longitude),
                    LatLng(lineList[16].latitude, lineList[16].longitude),
                    LatLng(lineList[17].latitude, lineList[17].longitude),
                    LatLng(lineList[18].latitude, lineList[18].longitude),
                    LatLng(lineList[19].latitude, lineList[19].longitude),
                )
                pathList[4].map = naverMap
            }
            "gyeongjeonLine" -> {
                for(i in 0..lineList.size - 1) {
                    if(i == 10 || (i >= 2 && i <= 4)){ }
                    else {gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))}
                }
                initStationLine()
                SrtStationLine(6)

                pathList.add(PathOverlay())
                pathList[2].coords = listOf(
                    LatLng(lineList[9].latitude, lineList[9].longitude),
                    LatLng(lineList[10].latitude, lineList[10].longitude),
                    LatLng(lineList[11].latitude, lineList[11].longitude)
                )
                pathList[2].map = naverMap
            }
            "honamLine" -> {
                for(i in 0..lineList.size - 1) {
                    if((i >= 2 && i <= 4) || (i >= 10 && i <= 12)){}
                    else { gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude)) }
                }
                initStationLine()
                SrtStationLine(7)

                pathList.add(PathOverlay())
                pathList[2].coords = listOf(
                    LatLng(lineList[8].latitude, lineList[8].longitude),
                    LatLng(lineList[10].latitude, lineList[10].longitude),
                    LatLng(lineList[11].latitude, lineList[11].longitude),
                    LatLng(lineList[12].latitude, lineList[12].longitude),
                    LatLng(lineList[13].latitude, lineList[13].longitude),
                )
                pathList[2].map = naverMap
            }
            "jeollaLine" -> {
                for(i in 0..lineList.size - 1) {
                    if((i >= 2 && i <= 4) || (i >= 10 && i <= 12)){}
                    else { gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude)) }
                }
                initStationLine()
                SrtStationLine(7)

                pathList.add(PathOverlay())
                pathList[2].coords = listOf(
                    LatLng(lineList[8].latitude, lineList[8].longitude),
                    LatLng(lineList[10].latitude, lineList[10].longitude),
                    LatLng(lineList[11].latitude, lineList[11].longitude),
                    LatLng(lineList[12].latitude, lineList[12].longitude),
                    LatLng(lineList[13].latitude, lineList[13].longitude),
                )
                pathList[2].map = naverMap
            }
            "jungangLine" -> {
                for(i in 0..lineList.size - 1) {
                    gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))
                }
                pathList.add(PathOverlay())
                pathList[0].coords = gpsList
                pathList[0].map = naverMap
            }
            "jungbuNaeryukLine" -> {
                for(i in 0..lineList.size - 1) {
                    gpsList.add(LatLng(lineList[i].latitude, lineList[i].longitude))
                }
                pathList.add(PathOverlay())
                pathList[0].coords = gpsList
                pathList[0].map = naverMap
            }
            else -> {}
        }
        for(i in 0..pathList.size - 1) {
            if(i == 1) pathList[i].color = Color.RED
            else pathList[i].color = Color.GREEN
        }
    }

    fun stationMarkerSetting(lineName: String){
        val myRef = database.getReference("ktxLines")
        lineList = mutableListOf<StationPositions>()
        var lineArray = arrayListOf<StationPositions>()
        // 파이어베이스에서 데이터 호출
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children) {
                    for(station in shot.children){
                        if(shot.key.toString() == lineName) {
                            val stationNum = station.child("stationNum").value.toString()
                            val stationName = station.child("stationName").value.toString()
                            val stationEngName = station.child("stationEngName").value.toString()
                            val stationAddress = station.child("stationAddress").value.toString()
                            val latitude = station.child("latitude").value.toString()
                            val longitude = station.child("longitude").value.toString()
                            val stationInfomation = station.child("stationInfomation").value.toString()
                            val likeCount = station.child("likeCount").value.toString()

                            lineList.add(StationPositions(stationNum.toInt(), stationEngName,stationName,stationAddress,
                                latitude.toDouble(),longitude.toDouble(),stationInfomation,likeCount.toInt()))

                            lineArray.add(
                                StationPositions(stationNum.toInt(), stationEngName,stationName,stationAddress,
                                    latitude.toDouble(),longitude.toDouble(),stationInfomation,likeCount.toInt())
                            )
                        }
                    }
                }

                var stationLineState = false
                binding.stationLineBtn.setOnClickListener {
                    if(markers.size <= 0){
                        Toast.makeText(it.context, "노선을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        if(stationLineState == false) {
                            lineList.sortBy { it.stationNum }
                            stationLineFun(lineName, lineList)
                            stationLineState = true
                            binding.stationLineBtn.text = "■ 노선 경로 표시 지우기"
                        }
                        else {
                            for(i in 0..pathList.size - 1){
                                pathList[i].map = null
                            }
                            stationLineState = false
                            binding.stationLineBtn.text = "■ 노선 경로 표시하기"
                        }
                    }
                }

                // 기존에 지도에 남아있던 마커 제거
                for(i in 0..markers.size - 1) {
                    markers[i].map = null
                }
                val dialog = LoadingDialog(this@MapActivity)
                dialog.show()

                // 매개변수로 받은 노선에 맞는 역에 해당하는 마커를 표시
                for(i in 0..lineList.size - 1) {
                    markers.add(Marker())
                    markers[i].position = LatLng(lineList[i].latitude, lineList[i].longitude)
                    markers[i].map = naverMap
                    markers[i].icon = OverlayImage.fromResource(R.drawable.ktxmarker_removebg)
                    markers[i].width = 120
                    markers[i].height = 140
                    markers[i].captionText = lineList[i].stationName + "역"
                    markers[i].captionTextSize = 10f
                    markers[i].captionColor = Color.BLUE
                    markers[i].setCaptionAligns(Align.Top)
                }
                dialog.dismiss()

                // 마커 설정후 지도가 한눈에 보이게 카메라 업뎃
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(36.332165597, 127.434310227), 5.5)
                naverMap.moveCamera(cameraUpdate)

                // 마커 클릭시 정보창 표시
                naverMap.setOnMapClickListener { pointF, latLng ->
                    /*infoWindow.close()*/
                    binding.infoWindowLayout.visibility = View.GONE
                    binding.infoWindowLogoview.visibility = View.INVISIBLE
                }
                // 마커 클릭 이벤트 리스너입니다요~
                val listener = Overlay.OnClickListener {overlay ->
                    val marker = overlay as Marker

                    for(i in 0..lineList.size - 1){
                        if(marker.captionText == lineList[i].stationName + "역"){
                            val storage = Firebase.storage
                            val storageRef = storage.getReference("image")
                            val imageName = lineList[i].stationEngName
                            val stationImage = storageRef.child("${imageName}.jpg")
                            var intentURL = ""
                            val imageURL = stationImage.downloadUrl.addOnSuccessListener {
                                intentURL = it.toString()
                                Glide.with(this@MapActivity)
                                    .load(it)
                                    .placeholder(getDrawable(R.drawable.loading))
                                    .error(getDrawable(R.drawable.notimage))
                                    .fallback(getDrawable(R.drawable.notimage))
                                    .into(binding.infoWindowImage)
                            }.addOnFailureListener{
                                binding.infoWindowImage.setImageDrawable(getDrawable(R.drawable.notimage))
                            }
                            binding.infoWindowName.text = "역명: " + lineList[i].stationName + "역"
                            binding.infoWindowAddress.text = "주소: " + lineList[i].stationAddress
                            binding.infoWindowDist.visibility = View.GONE

                            binding.infoWindowLayout.setOnClickListener{
                                val intent = Intent(this@MapActivity, InfomationPlusActivity::class.java)
                                intent.putExtra("infoTitle", "역 상세정보")
                                intent.putExtra("infoName", lineList[i].stationName)
                                intent.putExtra("infoAddress", "주소 : " + lineList[i].stationAddress)
                                intent.putExtra("infoDescription", lineList[i].stationInfomation)
                                intent.putExtra("infoImage", intentURL)
                                intent.putExtra("lineList", lineArray)
                                intent.putExtra("lineName", line)
                                startActivity(intent)
                            }
                        }
                    }
                    binding.infoWindowLogoview.visibility = View.VISIBLE
                    binding.infoWindowLayout.visibility = View.VISIBLE

                    true
                }
                // 각 마커에 리스너 연결!@!
                for(i in 0..lineList.size - 1) {
                    markers[i].onClickListener = listener
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun festivalMarkerSetting(markerMaxDist: Int){
        val mobile_os = "AND"
        val mobile_app = "AppTest"
        val type = "json"
        val num_of_rows = 100
        val page_no = 1
        val listYN = "Y"
        val arrange = "D"
        val currentTime = System.currentTimeMillis()
        val AllowEventDate = SimpleDateFormat("yyyyMMdd").format(currentTime).toString().toInt()
        val eventStartDate = "20230101"
        val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
        val serviceUrl = "http://apis.data.go.kr/B551011/KorService1/searchFestival1"

        val requestUrl = serviceUrl + "?numOfRows=" + num_of_rows + "&pageNo=" + page_no +
                "&MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                "&_type=" + type + "&listYN=" + listYN + "&arrange=" + arrange +
                "&eventStartDate=" + eventStartDate + "&serviceKey=" + serviceKey

        lateinit var page : String // url 주소 통해 전달받은 내용 저장할 변수
        val festivalList = mutableListOf<festivalDatas>()
        val infoMarkerImage = OverlayImage.fromResource(R.drawable.festivalmarker_removebg)
        //xml 데이터 가져와서 파싱
        // 외부에서 데이터 가져올 때 화면 계속 동작하도록 AsyncTask 이용
        class getDangerGrade: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                // 데이터 스트림 형태로 가져오기
                val stream = URL(requestUrl).openStream()
                val bufReader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                page = bufReader.readLine()

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)

                val json = JSONObject(page).getJSONObject("response")
                    .getJSONObject("body")
                if(json.get("items").toString() == ""){}
                else {
                    val jsonArray = json.getJSONObject("items").getJSONArray("item")
                    for(j in 0..jsonArray.length() - 1){
                        val jsonObject = jsonArray.getJSONObject(j)
                        var addr1 = jsonObject.getString("addr1")
                        var addr2 = jsonObject.getString("addr2")
                        var contentId = jsonObject.getString("contentid").toInt()
                        var contentTypeId = jsonObject.getString("contenttypeid").toInt()
                        var eventStartDate = jsonObject.getString("eventstartdate").toInt()
                        var eventEndDate = jsonObject.getString("eventenddate").toInt()
                        var firstImage = jsonObject.getString("firstimage")
                        var mapx = jsonObject.getString("mapx").toDouble()
                        var mapy = jsonObject.getString("mapy").toDouble()
                        var tel = jsonObject.getString("tel")
                        var title = jsonObject.getString("title")

                        if(eventEndDate - AllowEventDate >= 1 && eventStartDate - AllowEventDate <= 100){
                            for(i in 0..lineList.size - 1){
                                if((mapx - lineList[i].longitude > -(markerMaxDist * 0.00001) && mapx - lineList[i].longitude < (markerMaxDist * 0.00001)) &&
                                    (mapy - lineList[i].latitude > -(markerMaxDist * 0.00001) && mapy - lineList[i].latitude < (markerMaxDist * 0.00001))){
                                    festivalList.add(festivalDatas(addr1 + addr2, contentId, contentTypeId,
                                        eventStartDate, eventEndDate, firstImage, mapx, mapy, tel, title, lineList[i].stationName))
                                }
                            }
                        }
                    }

                    for(i in 0..festivalList.size - 1) {
                        tour_markers.add(Marker())
                        tour_markers[i].position = LatLng(festivalList[i].mapy, festivalList[i].mapx)
                        tour_markers[i].map = naverMap
                        tour_markers[i].icon = infoMarkerImage
                        tour_markers[i].width = 100
                        tour_markers[i].height = 120
                    }

                    // 마커 클릭시 정보창 표시
                    naverMap.setOnMapClickListener { pointF, latLng ->
                        binding.infoWindowLayout.visibility = View.GONE
                        binding.infoWindowLogoview.visibility = View.INVISIBLE
                    }
                    // 마커 클릭 이벤트 리스너
                    val listener = Overlay.OnClickListener {overlay ->
                        val marker = overlay as Marker

                        for(i in 0..festivalList.size - 1) {
                            if(marker.position == LatLng(festivalList[i].mapy, festivalList[i].mapx)){
                                binding.infoWindowName.text = "축제/공연/행사명 : " + festivalList[i].title
                                binding.infoWindowAddress.text = "주소: " + festivalList[i].addr
                                for(j in 0..lineList.size - 1) {
                                    if(festivalList[i].nearStation == lineList[j].stationName){
                                        binding.infoWindowDist.text = "역에서 거리: " +
                                                ((festivalList[i].mapx - lineList[j].longitude)*100000).toString().slice(0..3).replace(".", "") + "m"
                                    }
                                }
                                binding.infoWindowDist.visibility = View.VISIBLE
                                //binding.infoWindowDist.text = "역에서 거리: " + festivalList[i].dist.toInt() + "m"

                                Glide.with(this@MapActivity)
                                    .load(festivalList[i].firstImage)
                                    .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
                                    .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
                                    .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
                                    .into(binding.infoWindowImage) // 이미지를 넣을 뷰

                                binding.infoWindowLayout.setOnClickListener{
                                    val intent = Intent(this@MapActivity, festivalInfomationActivity::class.java)
                                    intent.putExtra("festivalName", "축제/공연/행사명 : " + festivalList[i].title)
                                    intent.putExtra("festivalAddr", "주소 : " + festivalList[i].addr)
                                    intent.putExtra("festivalTel", "전화번호 : " + festivalList[i].tel)
                                    intent.putExtra("contentId", festivalList[i].contentId)
                                    intent.putExtra("contentTypeId", festivalList[i].contentTypeId)
                                    intent.putExtra("startDate", festivalList[i].eventStartDate)
                                    intent.putExtra("endDate", festivalList[i].eventEndDate)
                                    intent.putExtra("nearStation", festivalList[i].nearStation)
                                    intent.putExtra("mapx", festivalList[i].mapx)
                                    intent.putExtra("mapy", festivalList[i].mapy)
                                    intent.putExtra("lineName", line)
                                    intent.putExtra("imageUrl", festivalList[i].firstImage)
                                    startActivity(intent)
                                }
                            }
                        }
                        binding.infoWindowLogoview.visibility = View.VISIBLE
                        binding.infoWindowLayout.visibility = View.VISIBLE

                        true
                    }
                    // 각 마커에 리스너 연결!@!
                    for(i in 0..tour_markers.size - 1) {
                        tour_markers[i].onClickListener = listener
                    }
                }
            }
        }
        getDangerGrade().execute()
    }

    fun infoMarkerSetting(markerMaxDist: Int){
        val myRef = database.getReference(infoType)
        val infoList = mutableListOf<TourData>()
        // 파이어베이스에서 데이터 호출
        myRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children){
                    for(info in shot.children){
                        if(shot.key.toString() == line){
                            val title = info.child("title").value.toString()
                            val addr = info.child("addr1").value.toString()
                            val addr2 = info.child("addr2").value.toString()
                            val latitude = info.child("latitude").value.toString()
                            val longitude = info.child("longitude").value.toString()
                            val dist = info.child("dist").value.toString()
                            val imageUri = info.child("imageUri").value.toString()
                            val tel = info.child("tel").value.toString()
                            var likeCount = info.child("likeCount").value.toString()
                            val contentId = info.child("contentId").value.toString()
                            val contentTypeId = info.child("contentTypeId").value.toString()
                            val nearStation = info.child("nearStation").value.toString()

                            if(dist.toDouble() <= markerMaxDist){
                                infoList.add(TourData(title, addr, addr2, imageUri, dist.toDouble(),
                                    latitude.toDouble(), longitude.toDouble(), "","", tel,
                                    likeCount.toInt(), contentId.toInt(), contentTypeId.toInt(), nearStation))
                            }
                        }
                    }
                }

                var infoName = ""
                var infoTitle = ""
                var infoMarkerImage = OverlayImage.fromResource(R.drawable.tourmarker_removebg)
                if(infoType=="tourDatas"){
                    infoName = "관광지명 : "
                    infoTitle = "관광지 상세정보"
                    infoMarkerImage = OverlayImage.fromResource(R.drawable.tourmarker_removebg)
                }
                else if(infoType=="festivalDatas"){
                    infoName = "축제/공연/행사명 : "
                    infoTitle = "축제/공연/행사 상세정보"
                    infoMarkerImage = OverlayImage.fromResource(R.drawable.festivalmarker_removebg)
                }
                else if(infoType=="accommodationDatas"){
                    infoName = "숙박명 : "
                    infoTitle = "숙박 상세정보"
                    infoMarkerImage = OverlayImage.fromResource(R.drawable.accommodationmarker_removebg)
                }
                else if(infoType=="foodshopDatas"){
                    infoName = "음식점명 : "
                    infoTitle = "음식점 상세정보"
                    infoMarkerImage = OverlayImage.fromResource(R.drawable.foodshopmarker_removebg)
                }

                for(i in 0..infoList.size - 1) {
                    tour_markers.add(Marker())
                    tour_markers[i].position = LatLng(infoList[i].latitude, infoList[i].longitude)
                    tour_markers[i].map = naverMap
                    tour_markers[i].icon = infoMarkerImage
                    tour_markers[i].width = 100
                    tour_markers[i].height = 120
                }

                // 마커 클릭시 정보창 표시
                naverMap.setOnMapClickListener { pointF, latLng ->
                    binding.infoWindowLayout.visibility = View.GONE
                    binding.infoWindowLogoview.visibility = View.INVISIBLE
                }
                // 마커 클릭 이벤트 리스너
                val listener = Overlay.OnClickListener {overlay ->
                    val marker = overlay as Marker

                    for(i in 0..infoList.size - 1) {
                        if(marker.position == LatLng(infoList[i].latitude, infoList[i].longitude)){
                            binding.infoWindowName.text = infoName + infoList[i].title
                            binding.infoWindowAddress.text = "주소: " + infoList[i].addr1 + " " + infoList[i].addr2
                            binding.infoWindowDist.visibility = View.VISIBLE
                            binding.infoWindowDist.text = "역에서 거리: " + infoList[i].dist.toInt() + "m"

                            Glide.with(this@MapActivity)
                                .load(infoList[i].imageUri)
                                .placeholder(getDrawable(R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
                                .error(getDrawable(R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
                                .fallback(getDrawable(R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
                                .into(binding.infoWindowImage) // 이미지를 넣을 뷰

                            binding.infoWindowLayout.setOnClickListener{
                                val intent = Intent(this@MapActivity, InfomationPlusActivity::class.java)
                                intent.putExtra("infoTitle", infoTitle)
                                intent.putExtra("infoName", infoName + infoList[i].title)
                                intent.putExtra("infoAddress", "주소 : " + infoList[i].addr1 + " " + infoList[i].addr2)
                                intent.putExtra("infoTel", "전화번호 : " + infoList[i].tel)
                                intent.putExtra("infoDist", infoList[i].dist.toInt())
                                intent.putExtra("infoContentId", infoList[i].contentId)
                                intent.putExtra("infoContentTypeId", infoList[i].contentTypeId)
                                intent.putExtra("infoType", infoType)
                                intent.putExtra("lineName", line)
                                val tourImage = infoList[i].imageUri
                                intent.putExtra("infoImage", tourImage)
                                startActivity(intent)
                            }
                        }
                    }
                    binding.infoWindowLogoview.visibility = View.VISIBLE
                    binding.infoWindowLayout.visibility = View.VISIBLE

                    true
                }
                // 각 마커에 리스너 연결!@!
                for(i in 0..tour_markers.size - 1) {
                    tour_markers[i].onClickListener = listener
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // 네이버 지도 객체 준비
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val dialog = LoadingDialog(this@MapActivity)
        dialog.show()

        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        val uiSettings = naverMap.uiSettings

        uiSettings.isCompassEnabled = true
        uiSettings.isLocationButtonEnabled = false

        val locationButtonView = binding.infoWindowLocationBtn as LocationButtonView
        locationButtonView.map = naverMap

        uiSettings.setLogoMargin(30, 5, 10, 20)

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        if(saveLineName.isNullOrEmpty() == false) {
            line = saveLineName
            stationMarkerSetting(line)
            if(saveInfoType.isNullOrEmpty() == false){
                infoType = saveInfoType
                infoMarkerSetting(maxDist)
            }
            findViewById<TextView>(R.id.map_max_dist_text).text = "${maxDist}m"
        }
        else{
            line = intent.getStringExtra("ktxLine").toString()
            infoType = intent.getStringExtra("infoType").toString()
            maxDist = intent.getIntExtra("maxDist", 0)
            if(maxDist != 0){
                findViewById<SeekBar>(R.id.map_seekBar).progress = maxDist / 500
                findViewById<TextView>(R.id.map_max_dist_text).text = "${maxDist}m"
            }
            stationMarkerSetting(line)
            infoMarkerSetting(maxDist)
        }

        var currentLine = "노선을 선택해주세요."
        when(line) {
            "gyeongbuLine" -> currentLine = "현재 노선 : 경부선"
            "gyeongjeonLine" -> currentLine = "현재 노선 : 경전선"
            "donghaeLine" -> currentLine = "현재 노선 : 동해선"
            "honamLine" -> currentLine = "현재 노선 : 호남선"
            "jeollaLine" -> currentLine = "현재 노선 : 전라선"
            "gangneungLine" -> currentLine = "현재 노선 : 강릉선"
            "jungangLine" -> currentLine = "현재 노선 : 중앙선"
            "jungbuNaeryukLine" -> currentLine = "현재 노선 : 중부내륙선"
        }
        binding.currentKtxLines.text = currentLine

        var currentInfoType = "■ 표시 마커 없음"
        when(infoType) {
            "tourDatas" -> currentInfoType = "■ 관광지마커 삭제하기"
            "festivalDatas" -> currentInfoType = "■ 축제마커 삭제하기"
            "accommodationDatas" -> currentInfoType = "■ 숙박마커 삭제하기"
            "foodshopDatas" -> currentInfoType = "■ 음식점마커 삭제하기"
        }
        binding.markerDeleteBtn.text = currentInfoType

        dialog.dismiss()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

data class festivalDateDatas(
    val eventStartDate: Int,
    val eventEndDate: Int,
)