package com.example.ktxtravelapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

// 뷰 어댑터 선언, 매개변수: 적용할 이미지 배열리스트
class MainImageViewPagerAdapter(var mainImages: ArrayList<Int>) :
    RecyclerView.Adapter<MainImageViewPagerAdapter.PagerViewHolder>() { //리사이클러뷰 어댑터를 적용.
    //뷰 홀더 선언부
    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.main_image_view_pager2, parent, false)) {
        val mainImages = itemView.findViewById<ImageView>(R.id.mainImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder = PagerViewHolder((parent))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.mainImages.setImageResource(mainImages[position])
    }

    override fun getItemCount(): Int = mainImages.size
}