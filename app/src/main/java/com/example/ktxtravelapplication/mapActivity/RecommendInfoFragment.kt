package com.example.ktxtravelapplication.mapActivity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentRecommendInfoBinding
import com.example.ktxtravelapplication.databinding.TourItemBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
import com.example.ktxtravelapplication.mapActivity.tourData.TourData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecommendInfoFragment : Fragment() {
    lateinit var database : FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance(stationName: String, lineName: String, lineList: ArrayList<StationPositions>,
                        stationAddress: String, stationImage: String) : RecommendInfoFragment{
            val fragment = RecommendInfoFragment()

            val args = Bundle()
            args.putString("stationName", stationName)
            args.putString("lineName", lineName)
            args.putSerializable("lineList", lineList)
            args.putString("stationAddress", stationAddress)
            args.putString("stationImage", stationImage)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentRecommendInfoBinding.inflate(layoutInflater, container, false)

        val nearStationName = arguments?.getString("stationName")
        val lineName = arguments?.getString("lineName")
        val nearStationAddress = arguments?.getString("stationAddress")
        val lineList = arguments?.getSerializable("lineList") as ArrayList<StationPositions>
        val stationImage = arguments?.getString("stationImage")

        database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("tourDatas")
        CoroutineScope(Dispatchers.IO).launch {
            myRef.child(lineName.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val infoList = mutableListOf<TourData>()
                    val infoNumArray = ArrayList<IndexData>()
                    runBlocking {
                        for(shot in snapshot.children){
                            if(shot.child("nearStation").value == nearStationName){
                                val i = shot.key.toString()
                                val title = shot.child("title").value.toString()
                                val addr = shot.child("addr1").value.toString()
                                val addr2 = shot.child("addr2").value.toString()
                                val latitude = shot.child("latitude").value.toString()
                                val longitude = shot.child("longitude").value.toString()
                                val dist = shot.child("dist").value.toString()
                                val infomation = shot.child("infomation").value.toString()
                                val imageUri = shot.child("imageUri").value.toString()
                                val tel = shot.child("tel").value.toString()
                                var likeCount = shot.child("likeCount").value.toString()
                                val homepage = shot.child("homepage").value.toString()
                                val contentId = shot.child("contentId").value.toString()
                                val contentTypeId = shot.child("contentTypeId").value.toString()
                                val nearStation = shot.child("nearStation").value.toString()

                                var homepageUrl = homepage.split("href=")
                                var homepageUrl3 = ""
                                if(homepageUrl.size > 1) {
                                    var homepageUrl2 = homepageUrl[1].split('"')
                                    homepageUrl3 = homepageUrl2[1]
                                }

                                if(dist.toDouble() <= 2000){
                                    infoList.add(
                                        TourData(title, addr, addr2, imageUri, dist.toDouble(), latitude.toDouble(),
                                            longitude.toDouble(), infomation,homepageUrl3, tel,
                                            likeCount.toInt(), contentId.toInt(), contentTypeId.toInt(), nearStation)
                                    )

                                    infoNumArray.add(IndexData(i.toInt(), likeCount.toInt()))
                                }
                            }
                        }
                    }
                    infoList.sortBy{it.likeCount}
                    infoList.reverse()

                    infoNumArray.sortBy{it.likeCount}
                    infoNumArray.reverse()

                    binding.tourInfoRecyclerview.adapter = TourRecyclerAdapter(infoList, infoNumArray, lineName.toString(),
                        nearStationName.toString(), nearStationAddress.toString(), lineList, stationImage.toString())
                    binding.tourInfoRecyclerview.layoutManager = LinearLayoutManager(context)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        return binding.root
    }
}

class TourRecyclerAdapter(val datas: MutableList<TourData>,val numArray: ArrayList<IndexData>, val lineName: String, val stationName: String, val stationAddress: String,
                          val lineList: ArrayList<StationPositions>, val stationImage: String) : RecyclerView.Adapter<TourRecyclerAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TourRecyclerAdapter.ViewHolder {
        val binding = TourItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ViewHolder(val binding: TourItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(pos: Int){
            binding.tourItemName.text = datas[pos].title
            binding.tourItemAddress.text = datas[pos].addr1 + datas[pos].addr2
            binding.tourItemLikeCount.text = "추천수 : ${datas[pos].likeCount}"
            binding.tourItemNumber.text = "${pos + 1}번"

            Glide.with(binding.root)
                .load(datas[pos].imageUri)
                .placeholder(AppCompatResources.getDrawable(itemView.context, R.drawable.loading))
                .error(AppCompatResources.getDrawable(itemView.context, R.drawable.notimage))
                .fallback(AppCompatResources.getDrawable(itemView.context, R.drawable.notimage))
                .into(binding.tourItemImage)

            itemView.setOnClickListener {
                val activity = it.context as InfomationPlusActivity
                val intent = Intent(it.context, InfomationPlusActivity::class.java)

                intent.putExtra("infoTitle", "관광지 상세정보")               // 타이틀
                intent.putExtra("infoName", "관광지명 : " + datas[pos].title)   // 관광지명
                intent.putExtra("infoAddress", "주소 : " + datas[pos].addr1 + datas[pos].addr2)   // 관광지 주소
                intent.putExtra("infoTel", "전화번호 : " + datas[pos].tel)    // 관광지 전화번호
                intent.putExtra("infoDist", datas[pos].dist.toInt())      // 역에서 관광지까지의 거리
                intent.putExtra("infoContentId", datas[pos].contentId)    // 관광지 컨텐츠ID
                intent.putExtra("infoContentTypeId", datas[pos].contentTypeId)   // 관광지 컨텐츠 타입 ID == 12
                intent.putExtra("infoImage", datas[pos].imageUri)  // 이미지 url
                intent.putExtra("lineName", lineName)   // 노선 이름
                intent.putExtra("infoType", "tourDatas")  // infoType
                intent.putExtra("recommendInfoState", true)   // 추천에서 탭 이동
                intent.putExtra("stationName", stationName)    // 역 이름 전송
                intent.putExtra("stationAddress", stationAddress)   // 역 주소 전송
                intent.putExtra("lineList", lineList) // 노선 역 리스트 전송
                intent.putExtra("stationImage", stationImage) // 역 이미지 주소 전송

                it.context.startActivity(intent)
                activity.finish()
            }
        }
    }
}

data class IndexData(
    val index: Int,
    var likeCount: Int,
)