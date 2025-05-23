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
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.net.URL

class temaCourseActivity : AppCompatActivity() {
    lateinit var stationList: MutableList<stationDatas>
    lateinit var courseList: MutableList<courseDatas>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityTemaCourseBinding.inflate(layoutInflater)

        setSupportActionBar(binding.temaCourseToolbar)
        supportActionBar?.setTitle("")

        binding.temaCourseBackBtn.setOnClickListener {
            finish()
        }

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
            val dialog = LoadingDialog(this)
            dialog.show()

            for(i in 0..stationList.size - 1) {
                val num_of_rows = 10
                val page_no = 1
                val mobile_os = "AND"
                val mobile_app = "AppTest"
                val type = "json"
                val listYN = "Y"
                val arrange = "D"
                val mapX = stationList[i].longitude.toString()
                val mapY = stationList[i].latitude.toString()
                val radius = "2000"
                val contentTypeId = "25"
                val serviceKey = "e46t%2FAlWggwGsJUF83Wf0XJ3VQijD7S8SNd%2Fs7TcbccStSNHqy1aQfXBRwMkttdlcNu7Aob3cDOGLa11VzRf7Q%3D%3D"
                var serviceUrl = "https://apis.data.go.kr/B551011/KorService1/locationBasedList1"

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
                                var contentId = jsonObject.getString("contentid")
                                var firstImage = jsonObject.getString("firstimage")
                                var mapx = jsonObject.getString("mapx")
                                var mapy = jsonObject.getString("mapy")
                                var title = jsonObject.getString("title")

                                courseList.add(courseDatas(title.toString(), contentId.toInt(),
                                    firstImage, mapx.toDouble(), mapy.toDouble(), stationList[i].stationName))
                            }
                            val adapter = CourseAdapter(courseList, linePath, stationList)
                            binding.courseRecyclerView.adapter = adapter
                            binding.courseRecyclerView.layoutManager = LinearLayoutManager(this@temaCourseActivity)

                            dialog.dismiss()
                        }
                    }
                }
                getDangerGrade().execute()
            }
        }

        val myRef = database.getReference("ktxLines").child(linePath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children){
                    val stationName = shot.child("stationName").value.toString()
                    val latitude = shot.child("latitude").value.toString().toDouble()
                    val longitude = shot.child("longitude").value.toString().toDouble()
                    var stationNum = shot.child("stationNum").value.toString().toInt()
                    stationList.add(stationDatas(stationName, latitude, longitude, stationNum))
                }
                stationList.sortBy { it.stationNum }

                fetchInfoXML(stationList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class CourseAdapter(val datas: MutableList<courseDatas>, val lineName: String, val stationList: MutableList<stationDatas>) : RecyclerView.Adapter<CourseAdapter.ViewHolder>(){
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
                intent.putExtra("mapX", datas[pos].mapx)
                intent.putExtra("mapY", datas[pos].mapy)
                intent.putExtra("lineName", lineName)
                intent.putExtra("nearStation", datas[pos].nearStation)
                intent.putExtra("stationList", stationList as Serializable)
                itemView.context.startActivity(intent)
            }
        }
    }
}