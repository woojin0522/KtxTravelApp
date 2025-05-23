package com.example.ktxtravelapplication.temaActivity

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ktxtravelapplication.databinding.ActivityCourseInfomationBinding
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.temaFragments.courseDescriptionFragment
import com.example.ktxtravelapplication.temaActivity.temaFragments.courseMapFragment
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
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
            val dialog = LoadingDialog(this)
            dialog.show()

            // 관광지 정보 수집
            val mobile_os = "AND"
            val mobile_app = "AppTest"
            val type = "json"
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
                            var subNum = jsonObject.getString("subnum").toInt()
                            var subContentId = jsonObject.getString("subcontentid").toInt()
                            var subName = jsonObject.getString("subname")

                            subCourseDataList.add(subCourseDatas(subNum, subContentId, subName, nearStation))
                        }

                        dialog.dismiss()

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