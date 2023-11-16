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
        /*// 토글 버튼 추가
        toggle = ActionBarDrawerToggle(this, binding.mapDrawer, R.string.map_drawer_open,
            R.string.map_drawer_close)
        // 툴바 뒤로가기 버튼 추가
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 뒤로가기 버튼을 메뉴 모양으로 변경
        toggle.syncState()*/
        /*binding.mapNavView.setNavigationItemSelectedListener {
            true
        }*/
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        /*val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(37.566, 126.978),  10.0))  // 카메라 위치 (위도,경도,줌)
            .mapType(NaverMap.MapType.Basic)    //지도 유형
            .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)  //빌딩 표시

        MapFragment.newInstance(options)

        val marker = Marker()
        marker.position = LatLng(37.566, 126.978)
        marker.map = naverMap*/
        val uiSettings = naverMap.uiSettings

        uiSettings.isLocationButtonEnabled = true
    }
    /*// 토글 버튼 클릭시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }*/
}