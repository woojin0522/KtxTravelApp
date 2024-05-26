package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.app.ActivityOptions
import android.app.Dialog
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
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentCourseDescriptionBinding
import com.example.ktxtravelapplication.mapActivity.InfoFullImageActivity
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.courseDatas
import com.example.ktxtravelapplication.temaActivity.courseInfomationActivity
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.io.StringReader
import java.net.URL

class courseDescriptionFragment : Fragment() {
    companion object {
        fun newInstance(courseDataList: MutableList<courseDatas>) : courseDescriptionFragment {
            val fragment = courseDescriptionFragment()

            val args = Bundle()
            args.putSerializable("courseDataList", courseDataList as Serializable)
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
        val binding = FragmentCourseDescriptionBinding.inflate(inflater, container, false)

        val courseDataList = arguments?.getSerializable("courseDataList") as MutableList<courseDatas>

        binding.courseInfoName.text = "여행코스명 : ${courseDataList[0].title}"
        binding.courseInfoNearStation.text = "주변 KTX역 : ${courseDataList[0].nearStation}역"

        // 관광지 설명과 홈페이지 불러오기
        fun fetchInfoXML(contentId: Int, contentTypeId: Int) {
            binding.courseInfoLoading.visibility = View.VISIBLE
            binding.courseInfoLoading.setBackgroundColor(Color.TRANSPARENT)
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

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
                        var overview = ""
                        val jsonArray = json.getJSONObject("items").getJSONArray("item")
                        for (j in 0..jsonArray.length() - 1) {
                            val jsonObject = jsonArray.getJSONObject(j)
                            overview = jsonObject.getString("overview")
                        }
                        binding.courseInfoDescription.text = overview.replace("<br>","")
                            .replace("<br />","")
                        binding.courseInfoDescription.movementMethod = ScrollingMovementMethod.getInstance()
                    }
                }
            }
            getDangerGrade().execute()
        }
        fetchInfoXML(courseDataList[0].contentId, 25)

        fun fetchInfoXML2(contentId: Int, contentTypeId: Int) {
            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = "json"
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
                    page = bufReader.readLine()

                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)

                    val json = JSONObject(page).getJSONObject("response")
                        .getJSONObject("body")
                    if(json.get("items").toString() == ""){}
                    else {
                        var distance = ""
                        var takeTime = ""
                        val jsonArray = json.getJSONObject("items").getJSONArray("item")
                        for (j in 0..jsonArray.length() - 1) {
                            val jsonObject = jsonArray.getJSONObject(j)
                            distance = jsonObject.getString("distance")
                            takeTime = jsonObject.getString("taketime")
                        }
                        binding.courseInfoDistance.text = "코스 총 길이 : ${distance}"
                        binding.courseInfoTakeTime.text = "코스 총 소요시간 : ${takeTime}"
                    }
                }
            }
            binding.courseInfoLoading.visibility = View.GONE
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            getDangerGrade().execute()
        }
        fetchInfoXML2(courseDataList[0].contentId, 25)

        binding.courseInfoHomepage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.courseInfoHomepage.setOnClickListener {
            binding.courseInfoHomepage.setTextColor(Color.BLUE)
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://korean.visitkorea.or.kr/search/search_list.do?keyword=${courseDataList[0].title}"))
            startActivity(intent)
        }

        // glide 라이브러리를 이용한 url 이미지 불러오기
        Glide.with(this)
            .load(courseDataList[0].firstImage) // 불러올 이미지 url
            .placeholder(getDrawable(context!!.applicationContext,R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(getDrawable(context!!.applicationContext,R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
            .fallback(getDrawable(context!!.applicationContext,R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
            .into(binding.courseInfoImage) // 이미지를 넣을 뷰
        // 이미지 클릭시 확대
        binding.courseInfoImage.setOnClickListener {
            val intent = Intent(it.context, InfoFullImageActivity::class.java)
            intent.putExtra("imageUrl", courseDataList[0].firstImage)
            startActivity(intent)
        }

        return binding.root
    }
}