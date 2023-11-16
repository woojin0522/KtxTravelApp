package com.example.ktxtravelapplication

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.ktxtravelapplication.databinding.ActivityMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바를 툴바로 교체
        setSupportActionBar(binding.mapToolbar)
        supportActionBar!!.setTitle("")

        binding.mapBackBtn.setOnClickListener{
            finish()
        }

        binding.mapDrawerBtn.setOnClickListener {
            if(binding.mapNavView.isActivated == false){
                binding.mapDrawer.openDrawer(binding.mapNavView, true)
                binding.mapNavView.isActivated = true
            }
            else{
                binding.mapDrawer.closeDrawer(binding.mapNavView, true)
                binding.mapNavView.isActivated = false
            }
        }

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val uiSettings = naverMap.uiSettings

        uiSettings.isLocationButtonEnabled = true
        uiSettings.isCompassEnabled = true

    }
}