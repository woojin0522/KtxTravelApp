package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Layout
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentCourseMapBinding
import com.example.ktxtravelapplication.databinding.FragmentFestivalMapBinding
import com.example.ktxtravelapplication.mapActivity.InfomationPlusActivity
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.CourseInfoViewPagerAdapter
import com.example.ktxtravelapplication.temaActivity.courseInfomationActivity
import com.example.ktxtravelapplication.temaActivity.festivalMapData
import com.example.ktxtravelapplication.temaActivity.stationDatas
import com.example.ktxtravelapplication.temaActivity.subCourseDatas
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.ArrowheadPathOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.io.StringReader
import java.net.URL
import kotlin.io.path.Path

class courseMapFragment : Fragment() {
    private lateinit var mapView: MapView
    lateinit var locationSource: FusedLocationSource
    lateinit var binding: FragmentCourseMapBinding
    lateinit var contentList: MutableList<contentDatas>
    companion object {
        fun newInstance(subCourseDataList: MutableList<subCourseDatas>, stationList: MutableList<stationDatas>) : courseMapFragment {
            val fragment = courseMapFragment()

            val args = Bundle()
            args.putSerializable("subCourseDataList", subCourseDataList as Serializable)
            args.putSerializable("stationList", stationList as Serializable)
            fragment.arguments = args

            return fragment
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentCourseMapBinding.inflate(inflater, container, false)

        mapView = binding.courseMapView
        contentList = mutableListOf()
        val dataList = arguments?.getSerializable("subCourseDataList") as MutableList<subCourseDatas>
        val stationList = arguments?.getSerializable("stationList") as MutableList<stationDatas>

        val pathList = mutableListOf<LatLng>()
        binding.courseMapView.getMapAsync{
            locationSource = FusedLocationSource(this, courseMapFragment.LOCATION_PERMISSION_REQUEST_CODE)
            it.locationSource = locationSource

            val uiSettings = it.uiSettings
            uiSettings.isCompassEnabled = true
            uiSettings.isLocationButtonEnabled = false

            val locationButtonView = binding.courseInfoWindowLocationBtn as LocationButtonView
            locationButtonView.map = it

            uiSettings.setLogoMargin(30, 5, 10, 20)

            val markers = mutableListOf<Marker>()

            fun fetchInfoXML(contentId: Int, index: Int) {
                // 관광지 정보 수집
                val mobile_os = "AND"
                val mobile_app = "AppTest"
                val type = ""
                val contentTypeId = 12
                val defaultYN = "Y"
                val firstImageYN = "Y"
                val areacodeYN = "N"
                val catcodeYN = "N"
                val addrinfoYN = "Y"
                val mapinfoYN = "Y"
                val overviewYN = "Y"
                val num_of_rows = 10
                val page_no = 1
                val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
                val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/detailCommon1"

                val requestUrl = serviceUrl + "?MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                        "&_type=" + type + "&contentId=" + contentId + "&contentTypeId=" + contentTypeId +
                        "&defaultYN=" + defaultYN + "&firstImageYN=" + firstImageYN + "&areacodeYN=" + areacodeYN +
                        "&catcodeYN=" + catcodeYN + "&addrinfoYN=" + addrinfoYN + "&mapinfoYN=" + mapinfoYN + "&overviewYN=" + overviewYN +
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

                        var tagTitle = false
                        var tagTel = false
                        var tagHomepage = false
                        var tagFirstImage = false
                        var tagAddr1 = false
                        var tagAddr2 = false
                        var tagMapx = false
                        var tagMapy = false
                        var tagOverview = false

                        var title = ""
                        var tel = ""
                        var homepage = ""
                        var firstImage = ""
                        var addr1 = ""
                        var addr2 = ""
                        var mapx = 0.0
                        var mapy = 0.0
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

                                if(tagName.equals("title")) tagTitle = true
                                else if(tagName.equals("tel")) tagTel = true
                                else if(tagName.equals("homepage")) tagHomepage = true
                                else if(tagName.equals("firstimage")) tagFirstImage = true
                                else if(tagName.equals("addr1")) tagAddr1 = true
                                else if(tagName.equals("addr2")) tagAddr2 = true
                                else if(tagName.equals("mapx")) tagMapx = true
                                else if(tagName.equals("mapy")) tagMapy = true
                                else if(tagName.equals("overview")) tagOverview = true
                            }

                            if(eventType == XmlPullParser.TEXT) {
                                if(tagTitle) {
                                    title = xpp.text
                                    tagTitle = false
                                }
                                else if(tagTel){
                                    tel = xpp.text
                                    tagTel = false
                                }
                                else if(tagHomepage) {
                                    homepage = xpp.text
                                    tagHomepage = false
                                }
                                else if(tagFirstImage) {
                                    firstImage = xpp.text
                                    tagFirstImage = false
                                }
                                else if(tagAddr1) {
                                    addr1 = xpp.text
                                    tagAddr1 = false
                                }
                                else if(tagAddr2) {
                                    addr2 = xpp.text
                                    tagAddr2 = false
                                }
                                else if(tagMapx) {
                                    mapx = xpp.text.toDouble()
                                    tagMapx = false
                                }
                                else if(tagMapy) {
                                    mapy = xpp.text.toDouble()
                                    tagMapy = false
                                }
                                else if(tagOverview) {
                                    overview = xpp.text
                                    tagOverview = false

                                    var homepageUrl1 = homepage.split("href=")
                                    var homepageUrl3 = ""
                                    if(homepageUrl1.size > 1) {
                                        var homepageUrl2 = homepageUrl1[1].split('"')
                                        homepageUrl3 = homepageUrl2[1]
                                    }

                                    contentList.add(contentDatas(title, tel, contentId, homepageUrl3, firstImage,
                                        mapx, mapy, addr1+addr2, overview.replace("<br>","")
                                            .replace("<br />","")))
                                }
                            }
                            if(eventType == XmlPullParser.END_TAG){}

                            eventType = xpp.next()
                        }
                        for(i in 1..contentList.size){
                            markers.add(Marker())
                            markers[i].position = LatLng(contentList[i - 1].mapy, contentList[i - 1].mapx)
                            markers[i].map = it
                            markers[i].captionText = "${i}번. ${contentList[i - 1].title}"
                            markers[i].captionColor = Color.BLUE
                            markers[i].setCaptionAligns(Align.Top)
                            markers[i].icon = MarkerIcons.BLACK
                            markers[i].iconTintColor = Color.RED

                            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng((markers[0].position.latitude + markers[1].position.latitude)/2.0,
                                (markers[0].position.longitude + markers[1].position.longitude)/2.0), 13.0)
                            it.moveCamera(cameraUpdate)

                            if(index == dataList.size - 1){
                                pathList.add(markers[i].position)
                            }
                        }

                        if(pathList.size >= 2){
                            val arrowheadPath = ArrowheadPathOverlay()
                            arrowheadPath.coords = pathList
                            arrowheadPath.width = 15
                            arrowheadPath.headSizeRatio = 3f
                            arrowheadPath.color = Color.GREEN
                            arrowheadPath.map = it
                        }

                        it.setOnMapClickListener { pointF, latLng ->
                            binding.courseInfoWindowLayout.visibility = View.GONE
                            binding.courseInfoWindowLogoview.visibility = View.VISIBLE
                        }
                        val listener = Overlay.OnClickListener { overlay ->
                            val marker = overlay as Marker

                            for(i in 1..contentList.size) {
                                if(marker.position == LatLng(contentList[i - 1].mapy, contentList[i - 1].mapx)){
                                    binding.courseInfoWindowName.text = contentList[i - 1].title
                                    binding.courseInfoWindowAddress.text = "주소 : ${contentList[i - 1].addr}"

                                    Glide.with(this@courseMapFragment)
                                        .load(contentList[i - 1].firstImage)
                                        .placeholder(getDrawable(context!!.applicationContext,R.drawable.loading)) // 이미지 로딩 시작하기 전 표시할 이미지
                                        .error(getDrawable(context!!.applicationContext,R.drawable.notimage)) // 로딩 에러 발생 시 표시할 이미지
                                        .fallback(getDrawable(context!!.applicationContext,R.drawable.notimage)) // 로드할 때 url이 비어있을 경우 표시할 이미지
                                        .into(binding.courseInfoWindowImage) // 이미지를 넣을 뷰

                                    binding.courseInfoWindowLayout.setOnClickListener{
                                        val intent = Intent(this@courseMapFragment.context, InfomationPlusActivity::class.java)
                                        intent.putExtra("infoTitle", "여행코스 상세정보")
                                        intent.putExtra("infoName", "관광지명 : ${contentList[i - 1].title}")
                                        intent.putExtra("infoTel", "전화번호 : ${contentList[i - 1].tel}")
                                        intent.putExtra("infoAddress", "주소 : ${contentList[i - 1].addr}")
                                        intent.putExtra("infoContentId", contentList[i - 1].contentId)
                                        intent.putExtra("infoContentTypeId", contentTypeId)
                                        intent.putExtra("infoImage", contentList[i - 1].firstImage)
                                        intent.putExtra("infoDescription", contentList[i - 1].description)
                                        intent.putExtra("infoHomepage", contentList[i - 1].homepage)
                                        startActivity(intent)
                                    }
                                }
                            }
                            binding.courseInfoWindowLogoview.visibility = View.VISIBLE
                            binding.courseInfoWindowLayout.visibility = View.VISIBLE

                            true
                        }
                        for(i in 1..markers.size - 1){
                            markers[i].onClickListener = listener
                        }
                    }
                }
                getDangerGrade().execute()
            }
            for(i in 0..stationList.size - 1){
                if(stationList[i].stationName == dataList[0].nearStation){
                    markers.add(Marker())
                    markers[0].position = LatLng(stationList[i].latitude, stationList[i].longitude)
                    markers[0].map = it
                    markers[0].captionText = "${dataList[0].nearStation}역"
                    markers[0].captionColor = Color.BLUE
                    markers[0].setCaptionAligns(Align.Top)

                    pathList.add(markers[0].position)
                }
            }

            for(i in 0..dataList.size - 1){
                fetchInfoXML(dataList[i].contentId, i)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

data class contentDatas(
    val title: String,
    val tel: String,
    val contentId: Int,
    val homepage: String,
    val firstImage: String,
    val mapx: Double,
    val mapy: Double,
    val addr: String,
    val description: String
)