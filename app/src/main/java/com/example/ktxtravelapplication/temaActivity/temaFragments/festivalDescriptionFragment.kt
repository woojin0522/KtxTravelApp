package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.app.ActivityOptions
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
import com.example.ktxtravelapplication.mapActivity.InfoLineFragment
import com.example.ktxtravelapplication.mapActivity.LoadingDialog
import com.example.ktxtravelapplication.temaActivity.festivalInfomationActivity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
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

        return binding.root
    }
}