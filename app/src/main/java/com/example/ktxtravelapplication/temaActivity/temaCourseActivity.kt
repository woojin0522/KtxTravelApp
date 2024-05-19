package com.example.ktxtravelapplication.temaActivity

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityTemaCourseBinding
import com.example.ktxtravelapplication.databinding.CourseItemBinding
import com.example.ktxtravelapplication.databinding.FestivalItemBinding
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import java.text.SimpleDateFormat

class temaCourseActivity : AppCompatActivity() {
    lateinit var stationList: MutableList<stationDatas>
    lateinit var courseList: MutableList<courseDatas>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityTemaCourseBinding.inflate(layoutInflater)

        setSupportActionBar(binding.temaCourseToolbar)
        supportActionBar?.setTitle("")

        setContentView(binding.root)

        stationList = mutableListOf()
        courseList = mutableListOf()

        val database = FirebaseDatabase.getInstance()

        val lineName = intent.getStringExtra("ktxLine")
        binding.courseLineName.text = "노선 : ${lineName}"

        var linePath = ""
        if(lineName == "경부선") linePath = "gyeongbuLine"
        else if(lineName == "호남선") linePath = "honamLine"
        else if(lineName == "경전선") linePath = "gyeongjeonLine"
        else if(lineName == "전라선") linePath = "jeollaLine"
        else if(lineName == "강릉선") linePath = "gangneungLine"
        else if(lineName == "중앙선") linePath = "jungangLine"
        else if(lineName == "중부내륙선") linePath = "jungbuNaeryukLine"
        else if(lineName == "동해선") linePath = "donghaeLine"

        fun fetchInfoXML(stationList: MutableList<stationDatas>) {
            val dialog = LoadingDialog(this@temaCourseActivity)
            dialog.show()

            for(i in 0..stationList.size - 1){
                val num_of_rows = 10
                val page_no = 1
                val mobile_os = "AND"
                val mobile_app = "AppTest"
                val type = ""
                val listYN = "Y"
                val arrange = "D"
                val mapX = stationList[i].longitude
                val mapY = stationList[i].latitude
                val radius = 2000
                val contentTypeId = 25
                val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
                val serviceUrl = "https://apis.data.go.kr/B551011/KorService1/locationBasedList1"

                val requestUrl = serviceUrl + "?numOfRows=" + num_of_rows + "&pageNo=" + page_no +
                        "&MobileOS=" + mobile_os + "&MobileApp=" + mobile_app +
                        "&_type=" + type + "&listYN=" + listYN + "&arrange=" + arrange +
                        "&mapX=" + mapX + "&mapY=" + mapY + "&radius=" + radius +
                        "&contentTypeId=" + contentTypeId + "&serviceKey=" + serviceKey

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

                        var tagContentId = false
                        var tagFirstImage = false
                        var tagMapx = false
                        var tagMapy = false
                        var tagTitle = false

                        var contentId = 0
                        var firstImage = ""
                        var mapx = 0.0
                        var mapy = 0.0
                        var title = ""

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

                                if(tagName.equals("contentid")) tagContentId = true
                                else if(tagName.equals("firstimage")) tagFirstImage = true
                                else if(tagName.equals("mapx")) tagMapx = true
                                else if(tagName.equals("mapy")) tagMapy = true
                                else if(tagName.equals("title")) tagTitle = true
                            }

                            if(eventType == XmlPullParser.TEXT) {
                                if(tagContentId) {
                                    contentId = xpp.text.toInt()
                                    tagContentId = false
                                }
                                else if(tagFirstImage) {
                                    firstImage = xpp.text
                                    tagFirstImage = false
                                }
                                else if(tagMapx) {
                                    mapx = xpp.text.toDouble()
                                    tagMapx = false
                                }
                                else if(tagMapy) {
                                    mapy = xpp.text.toDouble()
                                    tagMapy = false
                                }
                                else if(tagTitle) {
                                    title = xpp.text

                                    courseList.add(courseDatas(title, contentId, firstImage, mapx, mapy, stationList[i].stationName))

                                    tagTitle = false
                                }
                            }
                            if(eventType == XmlPullParser.END_TAG){}

                            eventType = xpp.next()
                        }
                        if(i == stationList.size - 1){
                            binding.courseRecyclerView.adapter = CourseAdapter(courseList, linePath)
                            binding.courseRecyclerView.layoutManager = LinearLayoutManager(this@temaCourseActivity)

                            dialog.dismiss()
                        }
                    }
                }
                getDangerGrade().execute()
            }
        }

        val myRef = database.getReference("ktxLines").child(linePath)
        CoroutineScope(Dispatchers.IO).launch{
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    runBlocking {
                        for(shot in snapshot.children){
                            val stationName = shot.child("stationName").value.toString()
                            val latitude = shot.child("latitude").value.toString().toDouble()
                            val longitude = shot.child("longitude").value.toString().toDouble()
                            var stationNum = shot.child("stationNum").value.toString().toInt()
                            stationList.add(stationDatas(stationName, latitude, longitude, stationNum))
                        }
                    }
                    stationList.sortBy{it.stationNum}

                    fetchInfoXML(stationList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}

class CourseAdapter(val datas: MutableList<courseDatas>, val lineName: String) : RecyclerView.Adapter<CourseAdapter.ViewHolder>(){
    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: CourseAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseAdapter.ViewHolder {
        val binding = CourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: CourseItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(pos: Int){
            binding.courseName.text = datas[pos].title
            binding.courseNearStation.text = "주변 KTX역 : ${datas[pos].nearStation}역"

            Glide.with(itemView.context)
                .load(datas[pos].firstImage)
                .placeholder(itemView.context.getDrawable(R.drawable.notimage))
                .error(itemView.context.getDrawable(R.drawable.notimage))
                .fallback(itemView.context.getDrawable(R.drawable.notimage))
                .into(binding.courseImage)

            itemView.setOnClickListener {
                val intent = Intent(it.context, courseInfomationActivity::class.java)
                intent.putExtra("courseName", datas[pos].title)
                intent.putExtra("contentId", datas[pos].contentId)
                intent.putExtra("imageUrl", datas[pos].firstImage)
                intent.putExtra("lineName", lineName)
                intent.putExtra("nearStation", datas[pos].nearStation)
                itemView.context.startActivity(intent)
            }
        }
    }
}

data class courseDatas(
    val title: String,
    val contentId: Int,
    val firstImage: String,
    val mapx: Double,
    val mapy: Double,
    val nearStation: String
)