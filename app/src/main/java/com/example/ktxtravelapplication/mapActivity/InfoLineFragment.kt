package com.example.ktxtravelapplication.mapActivity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentInfoLineBinding
import com.example.ktxtravelapplication.databinding.StationItemBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class InfoLineFragment(val lineArray: ArrayList<StationPositions>, val lineName: String) : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentInfoLineBinding.inflate(inflater ,container, false)

        var line = ""
        when(lineName){
            "gyeongbuLine" -> line = "경부선"
            "gyeongjeonLine" -> line = "경전선"
            "donghaeLine" -> line = "동해선"
            "gangneungLine" -> line = "강릉선"
            "honamLine" -> line = "호남선"
            "jeollaLine" -> line = "전라선"
            "jungangLine" -> line = "중앙선"
            "jungbuNaeryukLine" -> line = "중부내륙선"
        }
        binding.stationInfoLineName.text = line

        val adapter = InfoRecyclerAdapter(lineArray, lineName)
        binding.stationInfoRecyclerview.adapter = adapter
        binding.stationInfoRecyclerview.layoutManager = LinearLayoutManager(context)

        return binding.root
    }
}

class InfoRecyclerAdapter(val datas: ArrayList<StationPositions>, val lineName: String) : RecyclerView.Adapter<InfoRecyclerAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InfoRecyclerAdapter.ViewHolder {
        val binding = StationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfoRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ViewHolder(val binding : StationItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(pos: Int){
            binding.stationItemName.text = datas[pos].stationName + "역"
            binding.stationItemAddress.text = datas[pos].stationAddress

            val storage = Firebase.storage
            val storageRef = storage.getReference("image")
            val imageName = datas[pos].stationEngName
            val stationImage = storageRef.child("${imageName}.jpg")
            var intentURL = ""
            val imageURL = stationImage.downloadUrl.addOnSuccessListener {
                intentURL = it.toString()
                Glide.with(binding.root)
                    .load(it)
                    .placeholder(getDrawable(itemView.context, R.drawable.loading))
                    .error(getDrawable(itemView.context, R.drawable.notimage))
                    .fallback(getDrawable(itemView.context, R.drawable.notimage))
                    .into(binding.stationItemImage)
            }.addOnFailureListener{
                binding.stationItemImage.setImageDrawable(getDrawable(itemView.context, R.drawable.notimage))
            }

            itemView.setOnClickListener {
                val activity = it.context as InfomationPlusActivity
                val intent = Intent(it.context, InfomationPlusActivity::class.java)
                val storage = Firebase.storage
                val storageRef = storage.getReference("image")
                val imageName = datas[pos].stationEngName
                val stationImage = storageRef.child("${imageName}.jpg")
                val imageURL = stationImage.downloadUrl.addOnSuccessListener {
                    intentURL = it.toString()
                }.addOnFailureListener{}

                intent.putExtra("infoTitle", "역 상세정보")
                intent.putExtra("infoName", "역명 : " + datas[pos].stationName + "역")
                intent.putExtra("infoAddress", "주소 : " + datas[pos].stationAddress)
                intent.putExtra("infoDescription", datas[pos].stationInfomation)
                intent.putExtra("infoImage", intentURL)
                intent.putExtra("lineList", datas)
                intent.putExtra("lineName", lineName)
                it.context.startActivity(intent)
                activity.finish()
            }
        }
    }
}