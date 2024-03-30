package com.example.ktxtravelapplication.mapActivity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityMapBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.KtxLinesList
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
import com.example.ktxtravelapplication.mapActivity.tourData.TourData
import com.example.ktxtravelapplication.planActivity.TravelPlanActivity
import com.google.api.ResourceProto.resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var locationSource: FusedLocationSource
    lateinit var naverMap: NaverMap
    lateinit var tour_line: MutableList<StationPositions>
    lateinit var markers: MutableList<Marker>
    lateinit var tour_markers: MutableList<Marker>
    lateinit var tourList: MutableList<TourData>
    lateinit var line: String
    lateinit var infoType: String
    lateinit var database: FirebaseDatabase
    lateinit var binding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바를 툴바로 교체
        setSupportActionBar(binding.mapToolbar)
        supportActionBar!!.setTitle("")

        // 뒤로가기 작동 함수
        fun backAction() {
            val returnIntent = Intent() // 인텐트 생성
            returnIntent.putExtra("ktxLine", line) // 선택된 ktx노선 값을 넘김
            returnIntent.putExtra("infoType", infoType) // 선택된 정보타입 값을 넘김
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
        /*fun fetchXML(url: String, contentNumber: Int) {
            lateinit var page : String // url 주소 통해 전달받은 내용 저장할 변수
            //xml 데이터 가져와서 파싱
            // 외부에서 데이터 가져올 때 화면 계속 동작하도록 AsyncTask 이용

            class getDangerGrade: AsyncTask<Void, Void, Void>() {
                //url이용해서 xml 읽어오기
                override fun doInBackground(vararg p0: Void?): Void? {
                    // 데이터 스트림 형태로 가져오기
                    val stream = URL(url).openStream()
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

                // 읽어온 xml 파싱하기
                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)

                    var tagTitle = false
                    var tagAddr1 = false
                    var tagAddr2 = false
                    var tagImage = false
                    var tagDist = false
                    var tagMapY = false
                    var tagMapX = false
                    var tagTel = false

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
                        }

                        if(eventType == XmlPullParser.TEXT) {
                            if(tagImage) {
                                firstimage = xpp.text
                                tagImage = false
                            }
                            else if(tagTitle) {
                                title = xpp.text
                                tagTitle = false

                                tourList.add(TourData(title, addr1, addr2, firstimage, dist, mapy, mapx,infomation,tel,likeCount))
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
                        }
                        if(eventType == XmlPullParser.END_TAG){}

                        eventType = xpp.next()
                    }
                    //관광지 데이터를 파이어베이스에 저장
                    var myRef = database.getReference("")
                    if(contentNumber == 12) {myRef = database.getReference("tourDatas")}
                    else if(contentNumber == 15) {myRef = database.getReference("festivalDatas")} // 행사
                    else if(contentNumber == 32) {myRef = database.getReference("accommodationDatas")} // 숙박
                    else if(contentNumber == 39) {myRef = database.getReference("foodshopDatas")} // 음식점

                    var lineName = ""
                    if(tour_line == KtxLinesList().gyeongbuLine) lineName = "gyeongbuLine"
                    else if(tour_line == KtxLinesList().donghaeLine) lineName = "donghaeLine"
                    else if(tour_line == KtxLinesList().gangneungLine) lineName = "gangneungLine"
                    else if(tour_line == KtxLinesList().jungangLine) lineName = "jungangLine"
                    else if(tour_line == KtxLinesList().jeollaLine) lineName = "jeollaLine"
                    else if(tour_line == KtxLinesList().gyeongjeonLine) lineName = "gyeongjeonLine"
                    else if(tour_line == KtxLinesList().honamLine) lineName = "honamLine"
                    else if(tour_line == KtxLinesList().jungbuNaeryukLine) lineName = "jungbuNaeryukLine"

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
                    }
                }
            }
            getDangerGrade().execute()
        }*/

        /*fun tourMarkerSetting(contentNumber: Int){
            val num_of_rows = 10
            val page_no = 1
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = ""
            val list_yn = "Y"
            var arrange = "A"
            if(contentNumber == 15){
                arrange = "D"
            }
            val radius = "1000"
            val contentTypeId = contentNumber.toString()
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/locationBasedList1"

            if(tourList.isEmpty() == false) {
                tourList.clear()
            }

            for(i in 0..tour_line.size - 1) {
                val mapx = tour_line[i].longitude.toString()
                val mapy = tour_line[i].latitude.toString()

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

                fetchXML(requestUrl, contentNumber)
            }
        }*/
        //----------------------------------------------------------------------------------

        fun drawClose(){
            binding.mapDrawer.closeDrawer(binding.mapNavView, true)
            binding.mapNavView.isActivated = false
        }

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
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().gyeongbuLine
                                line = "gyeongbuLine"
                                stationMarkerSetting(line)
                            }
                            // 호남선을 선택
                            else if(checked == 1) {
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().honamLine
                                line = "honamLine"
                                stationMarkerSetting(line)
                            }
                            // 경전선을 선택
                            else if(checked == 2) {
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().gyeongjeonLine
                                line = "gyeongjeonLine"
                                stationMarkerSetting("gyeongjeonLine")
                            }
                            // 전라선을 선택
                            else if(checked == 3){
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().jeollaLine
                                line = "jeollaLine"
                                stationMarkerSetting("jeollaLine")
                            }
                            // 강릉선을 선택
                            else if(checked == 4){
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().gangneungLine
                                line = "gangneungLine"
                                stationMarkerSetting("gangneungLine")
                            }
                            // 중앙선을 선택
                            else if(checked == 5){
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().jungangLine
                                line = "jungangLine"
                                stationMarkerSetting("jungangLine")
                            }
                            // 중부내륙선을 선택
                            else if(checked == 6){
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
                                tour_line = KtxLinesList().jungbuNaeryukLine
                                line = "jungbuNaeryukLine"
                                stationMarkerSetting("jungbuNaeryukLine")
                            }
                            // 동해선을 선택
                            else if(checked == 7){
                                for(i in 0..tour_markers.size - 1) {
                                    tour_markers[i].map = null
                                }
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
                    infoMarkerSetting()
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

                    //tourMarkerSetting(15)
                    infoType="festivalDatas"
                    infoMarkerSetting()
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

                    //tourMarkerSetting(32)
                    infoType="accommodationDatas"
                    infoMarkerSetting()
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

                    //tourMarkerSetting(39)
                    infoType="foodshopDatas"
                    infoMarkerSetting()
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
                binding.markerDeleteBtn.text = "■ 표시 마커  없음"
            }
        }

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

    fun stationMarkerSetting(lineName: String){
        val myRef = database.getReference("ktxLines")
        val lineList = mutableListOf<StationPositions>()
        // 파이어베이스에서 데이터 호출
        myRef.addValueEventListener(object: ValueEventListener {
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
                        }
                    }
                }

                // 기존에 지도에 남아있던 마커 제거
                for(i in 0..markers.size - 1) {
                    markers[i].map = null
                }
                // 매개변수로 받은 노선에 맞는 역에 해당하는 마커를 표시
                for(i in 0..lineList.size - 1) {
                    markers.add(Marker())
                    markers[i].position = LatLng(lineList[i].latitude, lineList[i].longitude)
                    markers[i].map = naverMap
                    markers[i].icon = OverlayImage.fromResource(R.drawable.ktxmarker_removebg)
                    markers[i].width = 120
                    markers[i].height = 140
                    /*markers[i].captionText = lineList[i].stationName + "역"
                    markers[i].captionTextSize = 20f
                    markers[i].captionColor = Color.BLUE
                    markers[i].setCaptionAligns(Align.Top)*/
                }
                // 마커 설정후 지도가 한눈에 보이게 카메라 업뎃
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(36.332165597, 127.434310227), 5.5)
                naverMap.moveCamera(cameraUpdate)

                // 마커 클릭시 정보창 표시
                val infoWindow = InfoWindow()
                naverMap.setOnMapClickListener { pointF, latLng ->
                    infoWindow.close()
                }
                // 마커 클릭 이벤트 리스너입니다요~
                val listener = Overlay.OnClickListener {overlay ->
                    val marker = overlay as Marker

                    if (marker.infoWindow == null) {
                        // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                        infoWindow.open(marker)
                    } else {
                        // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                        infoWindow.close()
                    }

                    true
                }

                // 정보창 어댑터 !!
                infoWindow.adapter = object : InfoWindow.ViewAdapter() {
                    override fun getView(p0: InfoWindow): View {
                        val view = layoutInflater.inflate(R.layout.infomation_window,null)
                        for(i in 0..lineList.size-1){
                            // 클릭한 마커에 맞는 정보창을 표시한다~
                            if (markers[i].infoWindow == null){}
                            else {
                                view.findViewById<TextView>(R.id.info_window_name).text = "역명: " + lineList[i].stationName + "역"
                                view.findViewById<TextView>(R.id.info_window_address).text = "주소: " + lineList[i].stationAddress
                                view.findViewById<TextView>(R.id.info_window_dist).isVisible = false

                                val storage = Firebase.storage
                                val storageRef = storage.getReference("image")
                                val imageName = lineList[i].stationEngName
                                val stationImage = storageRef.child("${imageName}.jpg")
                                var intentURL = ""
                                val imageURL = stationImage.downloadUrl.addOnSuccessListener {
                                    intentURL = it.toString() }.addOnFailureListener{}

                                p0.onClickListener = Overlay.OnClickListener {overlay ->
                                    Log.d("test", intentURL)
                                    //상세정보 페이지로 이동
                                    val intent = Intent(this@MapActivity, InfomationPlusActivity::class.java)
                                    intent.putExtra("infoTitle", "역 상세정보")
                                    intent.putExtra("infoName", "역명 : " + lineList[i].stationName + "역")
                                    intent.putExtra("infoAddress", "주소 : " + lineList[i].stationAddress)
                                    intent.putExtra("infoDescription", lineList[i].stationInfomation)
                                    intent.putExtra("infoImage", intentURL)
                                    startActivity(intent)

                                    true
                                }
                            }
                        }
                        // 정보창에 적용할 뷰를 반환!!
                        return view
                    }
                }
                // 각 마커에 리스너 연결!@!
                for(i in 0..lineList.size - 1) {
                    markers[i].onClickListener = listener
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun infoMarkerSetting(){
        val myRef = database.getReference(infoType)
        val infoList = mutableListOf<TourData>()
        // 파이어베이스에서 데이터 호출
        myRef.addValueEventListener(object : ValueEventListener{
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
                            val infomation = info.child("infomation").value.toString()
                            val imageUri = info.child("imageUri").value.toString()
                            val tel = info.child("tel").value.toString()
                            val likeCount = info.child("likeCount").value.toString()

                            infoList.add(TourData(title, addr, addr2, imageUri, dist.toDouble(),
                                latitude.toDouble(), longitude.toDouble(), infomation,tel,likeCount.toInt()))
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
                val infoWindow = InfoWindow()
                naverMap.setOnMapClickListener { pointF, latLng ->
                    infoWindow.close()
                }
                // 마커 클릭 이벤트 리스너
                val listener = Overlay.OnClickListener {overlay ->
                    val marker = overlay as Marker

                    if (marker.infoWindow == null) {
                        // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                        infoWindow.open(marker)
                    } else {
                        // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                        infoWindow.close()
                    }
                    true
                }
                // 정보창 어댑터 !!
                infoWindow.adapter = object : InfoWindow.ViewAdapter() {
                    override fun getView(p0: InfoWindow): View {
                        val view = layoutInflater.inflate(R.layout.infomation_window,null)
                        for(i in 0..infoList.size-1){
                            // 클릭한 마커에 맞는 정보창을 표시한다
                            if (tour_markers[i].infoWindow == null){ }
                            else {
                                val Infomation = infoList[i].infomation
                                view.findViewById<TextView>(R.id.info_window_name).text = infoName + infoList[i].title
                                view.findViewById<TextView>(R.id.info_window_address).text = "주소: " + infoList[i].addr1 + " " + infoList[i].addr2
                                view.findViewById<TextView>(R.id.info_window_dist).text = "역에서 거리: " + infoList[i].dist.toInt() + "m"

                                p0.onClickListener = Overlay.OnClickListener {overlay ->
                                    //상세정보 페이지로 이동
                                    val intent = Intent(this@MapActivity, InfomationPlusActivity::class.java)
                                    intent.putExtra("infoTitle", infoTitle)
                                    intent.putExtra("infoName", infoName + infoList[i].title)
                                    intent.putExtra("infoAddress", "주소 : " + infoList[i].addr1 + " " + infoList[i].addr2)
                                    intent.putExtra("infoDescription", Infomation)
                                    intent.putExtra("infoTel", "전화번호 : " + infoList[i].tel)
                                    intent.putExtra("infoDist", infoList[i].dist.toInt())
                                    val tourImage = infoList[i].imageUri
                                    intent.putExtra("infoImage", tourImage)
                                    startActivity(intent)

                                    true
                                }
                            }
                        }
                        // 정보창에 적용할 뷰를 반환!!
                        return view
                    }
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
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        val uiSettings = naverMap.uiSettings

        uiSettings.isCompassEnabled = true
        uiSettings.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        line = intent.getStringExtra("ktxLine").toString()
        infoType = intent.getStringExtra("infoType").toString()
        stationMarkerSetting(line)
        infoMarkerSetting()

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
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
