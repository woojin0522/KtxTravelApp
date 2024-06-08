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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class InfomationPlusActivity : AppCompatActivity() {
    lateinit var database: FirebaseDatabase
    lateinit var editor: SharedPreferences.Editor
    lateinit var lineName: String
    lateinit var strInfoNum: String
    lateinit var festivalDescription: String
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
        festivalDescription = ""

        // 관광지 설명과 홈페이지 불러오기
        fun fetchInfoXML(contentId: Int, contentTypeId: Int) {
            val dialog = LoadingDialog(this@InfomationPlusActivity)
            dialog.show()

            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = "json"
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
                    page = bufReader.readLine()

                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)

                    val json = JSONObject(page).getJSONObject("response")
                        .getJSONObject("body")
                    if(json.get("items").toString() == ""){}
                    else {
                        var homepageUrl = ""
                        var overview = ""
                        val jsonArray = json.getJSONObject("items").getJSONArray("item")
                        for (j in 0..jsonArray.length() - 1) {
                            val jsonObject = jsonArray.getJSONObject(j)
                            homepageUrl = jsonObject.getString("homepage")
                            overview = jsonObject.getString("overview")
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

                        festivalDescription = overview.replace("<br>","")
                            .replace("<br />","")
                        binding.infoPlusDescription.text = festivalDescription

                        dialog.dismiss()
                    }
                }
            }
            getDangerGrade().execute()
        }

        fun fetchInfoIntroXML(contentId: Int, contentTypeId: Int, infoTypeName: String) {
            val dialog = LoadingDialog(this@InfomationPlusActivity)
            dialog.show()

            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = "json"
            val num_of_rows = 10
            val page_no = 1
            val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
            val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/detailIntro1"

            val requestUrl = serviceUrl + "?MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                    "&_type=" + type + "&contentId=" + contentId + "&contentTypeId=" + contentTypeId +
                    "&numOfRows=" + num_of_rows + "&pageNo=" + page_no + "&serviceKey=" + serviceKey

            lateinit var page : String // url 주소 통해 전달받은 내용 저장할 변수

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
                        for (j in 0..jsonArray.length() - 1) {
                            val jsonObject = jsonArray.getJSONObject(j)
                            if(infoTypeName == "관광지"){
                                if(jsonObject.getString("restdate").isNullOrEmpty() == false) {
                                    binding.infoPlusTourRestdate.visibility = View.VISIBLE
                                    binding.infoPlusTourRestdate.text = "휴무일 : " + jsonObject.getString("restdate").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("expguide").isNullOrEmpty() == false) {
                                    binding.infoPlusTourExpguide.visibility = View.VISIBLE
                                    binding.infoPlusTourExpguide.text = "체험안내 : " + jsonObject.getString("expguide").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("accomcount").isNullOrEmpty() == false) {
                                    binding.infoPlusTourAccomcount.visibility = View.VISIBLE
                                    binding.infoPlusTourAccomcount.text = "수용인원 : " + jsonObject.getString("accomcount").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("useseason").isNullOrEmpty() == false) {
                                    binding.infoPlusTourUseseason.visibility = View.VISIBLE
                                    binding.infoPlusTourUseseason.text = "이용시기 : " + jsonObject.getString("useseason").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("usetime").isNullOrEmpty() == false) {
                                    binding.infoPlusTourUsetime.visibility = View.VISIBLE
                                    binding.infoPlusTourUsetime.text = "이용시간 : " + jsonObject.getString("usetime").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("parking").isNullOrEmpty() == false) {
                                    binding.infoPlusTourParking.visibility = View.VISIBLE
                                    binding.infoPlusTourParking.text = "주차시설 : " + jsonObject.getString("parking").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("chkpet").isNullOrEmpty() == false) {
                                    binding.infoPlusTourChkpet.visibility = View.VISIBLE
                                    binding.infoPlusTourChkpet.text = "애완동물 동반가능 여부 : " + jsonObject.getString("chkpet").replace("<br>","").replace("<br />","")
                                }
                            }
                            else if(infoTypeName == "축제"){
                                if(jsonObject.getString("eventstartdate").isNullOrEmpty() == false && jsonObject.getString("eventenddate").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalDates.visibility = View.VISIBLE
                                    binding.infoPlusFestivalDates.text = "축제/공연/행사 기간 : " +
                                            jsonObject.getString("eventstartdate").slice(0..3) + "." +
                                            jsonObject.getString("eventstartdate").slice(4..5) + "." +
                                            jsonObject.getString("eventstartdate").slice(6..7) + " ~ " +
                                            jsonObject.getString("eventenddate").slice(0..3) + "." +
                                            jsonObject.getString("eventenddate").slice(4..5) + "." +
                                            jsonObject.getString("eventenddate").slice(6..7)
                                }
                                if(jsonObject.getString("playtime").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalPlaytime.visibility = View.VISIBLE
                                    binding.infoPlusFestivalPlaytime.text = "공연시간 : " + jsonObject.getString("playtime").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("eventplace").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalEventPlace.visibility = View.VISIBLE
                                    binding.infoPlusFestivalEventPlace.text = "행사장 위치 : " + jsonObject.getString("eventplace").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("agelimit").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalAgelimit.visibility = View.VISIBLE
                                    binding.infoPlusFestivalAgelimit.text = "관람 가능 연령 : " + jsonObject.getString("agelimit").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("bookingplace").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalBookingplace.visibility = View.VISIBLE
                                    binding.infoPlusFestivalBookingplace.text = "예매처 : " + jsonObject.getString("bookingplace").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("placeinfo").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalPlaceinfo.visibility = View.VISIBLE
                                    binding.infoPlusFestivalPlaceinfo.text = "행사장 위치 안내 : " + jsonObject.getString("placeinfo").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("subevent").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalSubevent.visibility = View.VISIBLE
                                    binding.infoPlusFestivalSubevent.text = "부대행사 : " + jsonObject.getString("subevent").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("program").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalProgram.visibility = View.VISIBLE
                                    binding.infoPlusFestivalProgram.text = "행사 프로그램 : " + jsonObject.getString("program").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("usetimefestival").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalUsetimefestival.visibility = View.VISIBLE
                                    binding.infoPlusFestivalUsetimefestival.text = "이용 요금 : " + jsonObject.getString("usetimefestival").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("discountinfofestival").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalDiscountinfofestival.visibility = View.VISIBLE
                                    binding.infoPlusFestivalDiscountinfofestival.text = "할인 정보 : " + jsonObject.getString("discountinfofestival").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("spendtimefestival").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalSpendtimefestival.visibility = View.VISIBLE
                                    binding.infoPlusFestivalSpendtimefestival.text = "관람 소요시간 : " + jsonObject.getString("spendtimefestival").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("festivalgrade").isNullOrEmpty() == false){
                                    binding.infoPlusFestivalFestivalgrade.visibility = View.VISIBLE
                                    binding.infoPlusFestivalFestivalgrade.text = "축제 등급 : " + jsonObject.getString("festivalgrade").replace("<br>","").replace("<br />","")
                                }
                            }
                            else if(infoTypeName == "숙박"){
                                if(jsonObject.getString("goodstay").isNullOrEmpty() == false && jsonObject.getString("goodstay") != "0"){
                                    binding.infoPlusAccomGoodstay.visibility = View.VISIBLE
                                    binding.infoPlusAccomGoodstay.text = "굿스테이 여부 : " + jsonObject.getString("goodstay").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("benikia").isNullOrEmpty() == false && jsonObject.getString("benikia") != "0"){
                                    binding.infoPlusAccomBenikia.visibility = View.VISIBLE
                                    binding.infoPlusAccomBenikia.text = "베니키아 여부 : " + jsonObject.getString("benikia").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("hanok").isNullOrEmpty() == false && jsonObject.getString("hanok") != "0"){
                                    binding.infoPlusAccomHanok.visibility = View.VISIBLE
                                    binding.infoPlusAccomHanok.text = "한옥 여부 : " + jsonObject.getString("hanok").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("roomcount").isNullOrEmpty() == false){
                                    binding.infoPlusAccomRoomcount.visibility = View.VISIBLE
                                    binding.infoPlusAccomRoomcount.text = "객실수 : " + jsonObject.getString("roomcount").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("roomtype").isNullOrEmpty() == false){
                                    binding.infoPlusAccomRoomtype.visibility = View.VISIBLE
                                    binding.infoPlusAccomRoomtype.text = "객실유형 : " + jsonObject.getString("roomtype").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("refundregulation").isNullOrEmpty() == false){
                                    binding.infoPlusAccomRefundregulation.visibility = View.VISIBLE
                                    binding.infoPlusAccomRefundregulation.text = "환불규정 : " + jsonObject.getString("refundregulation").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("checkintime").isNullOrEmpty() == false){
                                    binding.infoPlusAccomCheckintime.visibility = View.VISIBLE
                                    binding.infoPlusAccomCheckintime.text = "입실 시간 : " + jsonObject.getString("checkintime").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("checkouttime").isNullOrEmpty() == false){
                                    binding.infoPlusAccomCheckouttime.visibility = View.VISIBLE
                                    binding.infoPlusAccomCheckouttime.text = "퇴실 시간 : " + jsonObject.getString("checkouttime").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("chkcooking").isNullOrEmpty() == false){
                                    binding.infoPlusAccomChkcooking.visibility = View.VISIBLE
                                    binding.infoPlusAccomChkcooking.text = "객실내 취사 여부 : " + jsonObject.getString("chkcooking").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("seminar").isNullOrEmpty() == false && jsonObject.getString("seminar") != "0"){
                                    binding.infoPlusAccomSeminar.visibility = View.VISIBLE
                                    binding.infoPlusAccomSeminar.text = "세미나실 여부 : " + jsonObject.getString("seminar").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("sports").isNullOrEmpty() == false && jsonObject.getString("sports") != "0"){
                                    binding.infoPlusAccomSports.visibility = View.VISIBLE
                                    binding.infoPlusAccomSports.text = "스포츠 시설 여부 : " + jsonObject.getString("sports").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("sauna").isNullOrEmpty() == false && jsonObject.getString("sauna") != "0"){
                                    binding.infoPlusAccomSauna.visibility = View.VISIBLE
                                    binding.infoPlusAccomSauna.text = "사우나 여부 : " + jsonObject.getString("sauna").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("beauty").isNullOrEmpty() == false && jsonObject.getString("beauty") != "0"){
                                    binding.infoPlusAccomBeauty.visibility = View.VISIBLE
                                    binding.infoPlusAccomBeauty.text = "뷰티시설 여부 : " + jsonObject.getString("beauty").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("beverage").isNullOrEmpty() == false && jsonObject.getString("beverage") != "0"){
                                    binding.infoPlusAccomBeverage.visibility = View.VISIBLE
                                    binding.infoPlusAccomBeverage.text = "식음료장 여부 : " + jsonObject.getString("beverage").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("karaoke").isNullOrEmpty() == false && jsonObject.getString("karaoke") != "0"){
                                    binding.infoPlusAccomKaraoke.visibility = View.VISIBLE
                                    binding.infoPlusAccomKaraoke.text = "노래방 여부 : " + jsonObject.getString("karaoke").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("barbecue").isNullOrEmpty() == false && jsonObject.getString("barbecue") != "0"){
                                    binding.infoPlusAccomBarbecue.visibility = View.VISIBLE
                                    binding.infoPlusAccomBarbecue.text = "바베큐장 여부 : " + jsonObject.getString("barbecue").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("campfire").isNullOrEmpty() == false && jsonObject.getString("campfire") != "0"){
                                    binding.infoPlusAccomCampfire.visibility = View.VISIBLE
                                    binding.infoPlusAccomCampfire.text = "캠프파이어 여부 : " + jsonObject.getString("campfire").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("bicycle").isNullOrEmpty() == false && jsonObject.getString("bicycle") != "0"){
                                    binding.infoPlusAccomBicycle.visibility = View.VISIBLE
                                    binding.infoPlusAccomBicycle.text = "자전거 대여 여부 : " + jsonObject.getString("bicycle").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("fitness").isNullOrEmpty() == false && jsonObject.getString("fitness") != "0"){
                                    binding.infoPlusAccomFitness.visibility = View.VISIBLE
                                    binding.infoPlusAccomFitness.text = "휘트니스 센터 여부 : " + jsonObject.getString("fitness").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("publicpc").isNullOrEmpty() == false && jsonObject.getString("publicpc") != "0"){
                                    binding.infoPlusAccomPublicpc.visibility = View.VISIBLE
                                    binding.infoPlusAccomPublicpc.text = "공용 PC실 여부 : " + jsonObject.getString("publicpc").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("publicbath").isNullOrEmpty() == false && jsonObject.getString("publicbath") != "0"){
                                    binding.infoPlusAccomPublicbath.visibility = View.VISIBLE
                                    binding.infoPlusAccomPublicbath.text = "공용 샤워실 여부 : " + jsonObject.getString("publicbath").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("subfacility").isNullOrEmpty() == false){
                                    binding.infoPlusAccomSubfacility.visibility = View.VISIBLE
                                    binding.infoPlusAccomSubfacility.text = "부대시설 : " + jsonObject.getString("subfacility").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("foodplace").isNullOrEmpty() == false){
                                    binding.infoPlusAccomFoodplace.visibility = View.VISIBLE
                                    binding.infoPlusAccomFoodplace.text = "식음료장 : " + jsonObject.getString("foodplace").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("pickup").isNullOrEmpty() == false){
                                    binding.infoPlusAccomPickup.visibility = View.VISIBLE
                                    binding.infoPlusAccomPickup.text = "픽업서비스 여부 : " + jsonObject.getString("pickup").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("parkinglodging").isNullOrEmpty() == false){
                                    binding.infoPlusAccomParkinglodging.visibility = View.VISIBLE
                                    binding.infoPlusAccomParkinglodging.text = "주차시설 : " + jsonObject.getString("parkinglodging").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("scalelodging").isNullOrEmpty() == false){
                                    binding.infoPlusAccomScalelodging.visibility = View.VISIBLE
                                    binding.infoPlusAccomScalelodging.text = "규모 : " + jsonObject.getString("scalelodging").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("accomcountlodging").isNullOrEmpty() == false){
                                    binding.infoPlusAccomAccomcountlodging.visibility = View.VISIBLE
                                    binding.infoPlusAccomAccomcountlodging.text = "수용인원 : " + jsonObject.getString("accomcountlodging").replace("<br>","").replace("<br />","")
                                }
                            }
                            else {
                                if(jsonObject.getString("seat").isNullOrEmpty() == false){
                                    binding.infoPlusFoodSeat.visibility = View.VISIBLE
                                    binding.infoPlusFoodSeat.text = "좌석수 : " + jsonObject.getString("seat").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("kidsfacility").isNullOrEmpty() == false && jsonObject.getString("kidsfacility") != "0"){
                                    binding.infoPlusFoodKidsfacility.visibility = View.VISIBLE
                                    binding.infoPlusFoodKidsfacility.text = "어린이 놀이방 여부 : " + jsonObject.getString("kidsfacility").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("firstmenu").isNullOrEmpty() == false){
                                    binding.infoPlusFoodFirstmenu.visibility = View.VISIBLE
                                    binding.infoPlusFoodFirstmenu.text = "대표메뉴 : " + jsonObject.getString("firstmenu").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("treatmenu").isNullOrEmpty() == false){
                                    binding.infoPlusFoodTreatmenu.visibility = View.VISIBLE
                                    binding.infoPlusFoodTreatmenu.text = "취급메뉴 : " + jsonObject.getString("treatmenu").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("smoking").isNullOrEmpty() == false){
                                    binding.infoPlusFoodSmoking.visibility = View.VISIBLE
                                    binding.infoPlusFoodSmoking.text = "흡연 가능 여부 : " + jsonObject.getString("smoking").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("packing").isNullOrEmpty() == false){
                                    binding.infoPlusFoodPacking.visibility = View.VISIBLE
                                    binding.infoPlusFoodPacking.text = "포장가능 여부 : " + jsonObject.getString("packing").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("scalefood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodScalefood.visibility = View.VISIBLE
                                    binding.infoPlusFoodScalefood.text = "규모 : " + jsonObject.getString("scalefood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("parkingfood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodParkingfood.visibility = View.VISIBLE
                                    binding.infoPlusFoodParkingfood.text = "주차시설 : " + jsonObject.getString("parkingfood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("opendatefood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodOpendatefood.visibility = View.VISIBLE
                                    binding.infoPlusFoodOpendatefood.text = "개업일 : " + jsonObject.getString("opendatefood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("opentimefood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodOpentimefood.visibility = View.VISIBLE
                                    binding.infoPlusFoodOpentimefood.text = "영업시간 : " + jsonObject.getString("opentimefood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("restdatefood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodRestdatefood.visibility = View.VISIBLE
                                    binding.infoPlusFoodRestdatefood.text = "휴무일 : " + jsonObject.getString("restdatefood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("discountinfofood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodDiscountinfofood.visibility = View.VISIBLE
                                    binding.infoPlusFoodDiscountinfofood.text = "할인정보 : " + jsonObject.getString("discountinfofood").replace("<br>","").replace("<br />","")
                                }
                                if(jsonObject.getString("reservationfood").isNullOrEmpty() == false){
                                    binding.infoPlusFoodReservationfood.visibility = View.VISIBLE
                                    binding.infoPlusFoodReservationfood.text = "예약안내 : " + jsonObject.getString("reservationfood").replace("<br>","").replace("<br />","")
                                }
                            }
                        }

                        dialog.dismiss()
                    }
                }
            }
            getDangerGrade().execute()
        }

        fun infoSetting(){
            binding.infoPlusLikeLayout.visibility = View.VISIBLE
            binding.infoPlusName.text = infoName

            val infoType = intent.getStringExtra("infoType")

            fetchInfoXML(contentId, contentTypeId)

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

            binding.infoTitle.text = infoTitle
            binding.infoPlusTel.text = infoTel
            binding.infoPlusAddress.text = infoAddress
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

            binding.infoBackBtn.setOnClickListener {
                finish()
            }
        }

        if(infoTitle == "역 상세정보"){                                // 상세 정보창이 역 정보일 경우 실행
            binding.infoPlusTel.isVisible = false
            binding.infoPlusHomepage.isVisible = false
            binding.infoPlusDescription.isVisible = false
            binding.infoAllTab.visibility = View.VISIBLE
            binding.infoPlusName.text = "역명 : " + infoName + "역"
            binding.infoPlusAddress.text = "주소 : " + infoAddress
            binding.infoPlusDist.visibility = View.GONE
            binding.infoPlusLine1.visibility = View.GONE
            binding.infoPlusLine2.visibility = View.GONE
            val lineArray = intent.getSerializableExtra("lineList") as ArrayList<StationPositions>
            lineArray.sortBy { it.stationNum }

            binding.infoPlusLikeLayout.visibility = View.GONE
            binding.infoTabViewPager2.adapter = InfoViewPagerAdapter(this, lineArray, lineName, infoName.toString(),
                infoAddress.toString(), infoImage.toString())

            TabLayoutMediator(binding.infoTabLayout, binding.infoTabViewPager2) { tab, position ->
                when(position) {
                    0 -> tab.text = "노선"
                    1 -> tab.text = "주변관광지 추천"
                }
            }.attach()

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

            binding.infoBackBtn.setOnClickListener {
                finish()
            }
        }
        else if(infoTitle == "여행코스 상세정보"){
            binding.infoPlusLikeLayout.visibility = View.GONE
            binding.infoPlusDist.visibility = View.GONE
            binding.infoPlusName.text = infoName
            binding.infoPlusTel.text = infoTel
            binding.infoPlusAddress.text = infoAddress

            val homepage = intent.getStringExtra("infoHomepage")
            val description = intent.getStringExtra("infoDescription")

            if(infoTel != "") {
                binding.infoPlusTel.text = "전화번호를 찾을 수 없습니다."
            }
            if(homepage != "") {
                binding.infoPlusHomepage.text = "홈페이지 이동하기"
                binding.infoPlusHomepage.setOnClickListener {
                    binding.infoPlusHomepage.setTextColor(Color.BLUE)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(homepage))
                    startActivity(intent)
                }
            }
            else {
                binding.infoPlusHomepage.text = "홈페이지를 찾을 수 없습니다."
            }
            binding.infoPlusDescription.text = description
            binding.infoPlusHomepage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            fetchInfoIntroXML(contentId, contentTypeId, "관광지")
        }
        else if(infoTitle == "관광지 상세정보"){
            infoSetting()
            fetchInfoIntroXML(contentId, contentTypeId, "관광지")
        }
        else if(infoTitle == "축제/공연/행사 상세정보"){
            infoSetting()
            fetchInfoIntroXML(contentId, contentTypeId, "축제")
        }
        else if(infoTitle == "숙박 상세정보"){
            infoSetting()
            fetchInfoIntroXML(contentId, contentTypeId, "숙박")
        }
        else {                                                        // 상세 정보창이 관광지, 음식점등의 정보일 경우 실행
            infoSetting()
            fetchInfoIntroXML(contentId, contentTypeId, "음식점")
        }
    }
}

// 뷰페이저 어댑터
class InfoViewPagerAdapter(activity: FragmentActivity, lineArray: ArrayList<StationPositions>, lineName: String,
                           stationName: String, stationAddress: String, stationImage: String): FragmentStateAdapter(activity) {
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
