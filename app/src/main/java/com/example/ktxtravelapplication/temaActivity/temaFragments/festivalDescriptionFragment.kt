package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentFestivalDescriptionBinding
import com.example.ktxtravelapplication.mapActivity.InfoFullImageActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class festivalDescriptionFragment : Fragment() {
    companion object {
        fun newInstance(festivalInfoList: ArrayList<String>, imageUrl: String): festivalDescriptionFragment {
            val fragment = festivalDescriptionFragment()

            val args = Bundle()
            args.putStringArrayList("festivalInfoList", festivalInfoList)
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentFestivalDescriptionBinding.inflate(inflater, container, false)
        val festivalInfoList = arguments?.getStringArrayList("festivalInfoList")
        val imageUrl = arguments?.getString("imageUrl")

        val festivalName = festivalInfoList?.get(0)
        val festivalAddr = festivalInfoList?.get(1)
        val festivalTel = festivalInfoList?.get(2)
        val contentId = festivalInfoList?.get(3)
        val contentTypeId = festivalInfoList?.get(4)
        var startDate = festivalInfoList?.get(5)
        var endDate = festivalInfoList?.get(6)
        val nearStation = festivalInfoList?.get(8)
        val homepageUrl = festivalInfoList?.get(9)
        val description = festivalInfoList?.get(10)

        binding.festivalInfoName.text = "축제명 : ${festivalName}"
        binding.festivalInfoAddress.text = "주소 : ${festivalAddr}"
        binding.festivalInfoTel.text = "전화번호 : ${festivalTel}"

        binding.festivalInfoNearStation.text = "주변 KTX역 : ${nearStation}역"

        if(homepageUrl != "") {
            binding.festivalInfoHomepage.text = "홈페이지 이동하기"
            binding.festivalInfoHomepage.setOnClickListener {
                binding.festivalInfoHomepage.setTextColor(Color.BLUE)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl))
                startActivity(intent)
            }
        }
        else {
            binding.festivalInfoHomepage.text = "홈페이지를 찾을 수 없습니다."
        }
        binding.festivalInfoDescription.movementMethod = ScrollingMovementMethod.getInstance()
        binding.festivalInfoHomepage.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        startDate = startDate?.slice(0..3) + "." + startDate?.slice(4..5) + "." + startDate?.slice(6..7)
        endDate = endDate?.slice(0..3) + "." + endDate?.slice(4..5) + "." + endDate?.slice(6..7)
        binding.festivalInfoDates.text = "축제/공연/행사 기간 : ${startDate} ~ ${endDate}"

        binding.festivalInfoDescription.text = description?.replace("<br>","")
            ?.replace("<br />","")

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(imageUrl) // 불러올 이미지 url
            .placeholder(getDrawable(context!!.applicationContext, R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(context!!.applicationContext, R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(context!!.applicationContext, R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.festivalInfoImage) // 이미지를 넣을 뷰

        // 이미지 클릭시 확대
        binding.festivalInfoImage.setOnClickListener {
            val intent = Intent(it.context, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        fun fetchInfoIntroXML(contentId: Int, contentTypeId: Int) {
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
                            if(jsonObject.getString("playtime").isNullOrEmpty() == false){
                                binding.festivalInfoPlaytime.visibility = View.VISIBLE
                                binding.festivalInfoPlaytime.text = "공연시간 : " + jsonObject.getString("playtime").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("eventplace").isNullOrEmpty() == false){
                                binding.festivalInfoEventPlace.visibility = View.VISIBLE
                                binding.festivalInfoEventPlace.text = "행사장 위치 : " + jsonObject.getString("eventplace").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("agelimit").isNullOrEmpty() == false){
                                binding.festivalInfoAgelimit.visibility = View.VISIBLE
                                binding.festivalInfoAgelimit.text = "관람 가능 연령 : " + jsonObject.getString("agelimit").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("bookingplace").isNullOrEmpty() == false){
                                binding.festivalInfoBookingplace.visibility = View.VISIBLE
                                binding.festivalInfoBookingplace.text = "예매처 : " + jsonObject.getString("bookingplace").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("placeinfo").isNullOrEmpty() == false){
                                binding.festivalInfoPlaceinfo.visibility = View.VISIBLE
                                binding.festivalInfoPlaceinfo.text = "행사장 위치 안내 : " + jsonObject.getString("placeinfo").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("subevent").isNullOrEmpty() == false){
                                binding.festivalInfoSubevent.visibility = View.VISIBLE
                                binding.festivalInfoSubevent.text = "부대행사 : " + jsonObject.getString("subevent").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("program").isNullOrEmpty() == false){
                                binding.festivalInfoProgram.visibility = View.VISIBLE
                                binding.festivalInfoProgram.text = "행사 프로그램 : " + jsonObject.getString("program").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("usetimefestival").isNullOrEmpty() == false){
                                binding.festivalInfoUsetimefestival.visibility = View.VISIBLE
                                binding.festivalInfoUsetimefestival.text = "이용 요금 : " + jsonObject.getString("usetimefestival").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("discountinfofestival").isNullOrEmpty() == false){
                                binding.festivalInfoDiscountinfofestival.visibility = View.VISIBLE
                                binding.festivalInfoDiscountinfofestival.text = "할인 정보 : " + jsonObject.getString("discountinfofestival").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("spendtimefestival").isNullOrEmpty() == false){
                                binding.festivalInfoSpendtimefestival.visibility = View.VISIBLE
                                binding.festivalInfoSpendtimefestival.text = "관람 소요시간 : " + jsonObject.getString("spendtimefestival").replace("<br>","").replace("<br />","")
                            }
                            if(jsonObject.getString("festivalgrade").isNullOrEmpty() == false){
                                binding.festivalInfoFestivalgrade.visibility = View.VISIBLE
                                binding.festivalInfoFestivalgrade.text = "축제 등급 : " + jsonObject.getString("festivalgrade").replace("<br>","").replace("<br />","")
                            }
                        }
                    }
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoIntroXML(contentId!!.toInt(), contentTypeId!!.toInt())

        return binding.root
    }
}