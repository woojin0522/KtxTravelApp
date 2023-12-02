package com.example.ktxtravelapplication

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ktxtravelapplication.databinding.ActivityMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var locationSource: FusedLocationSource
    lateinit var naverMap: NaverMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바를 툴바로 교체
        setSupportActionBar(binding.mapToolbar)
        supportActionBar!!.setTitle("")

        // 뒤로가기 버튼
        binding.mapBackBtn.setOnClickListener{
            finish()
        }

        // 드로어 레이아웃 열고 닫기 버튼
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

        // 뒤로가기 버튼
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 드로어 레이아웃이 열려있을 떄 뒤로가기 버튼을 누를경우 드로어 레이아웃을 닫는다.
                if(binding.mapNavView.isActivated == true) {
                    binding.mapDrawer.closeDrawer(binding.mapNavView, true)
                    binding.mapNavView.isActivated = false
                }
                // 드로어 레이아웃이 닫혀있다면 메인화면으로 돌아간다.
                else{
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        val ktxLines = arrayOf("경부선", "호남선", "경전선", "전라선", "강릉선", "중앙선", "중부내륙선")
        val gyeongbuLine = arrayOf(
            stationPositions(37.612175933, 126.834157342),
            stationPositions(37.554530651,126.970713923),
            stationPositions(37.515272619, 126.907021401),
            stationPositions(37.266093058, 126.999850621),
            stationPositions(37.416561026, 126.884662956),
            stationPositions(36.794306610, 127.104482191),
            stationPositions(36.619908542, 127.327705362),
            stationPositions(36.332165597, 127.434310227),
            stationPositions(36.113522147, 128.180999088),
            stationPositions(35.881469815, 128.540403081),
            stationPositions(35.879388797, 128.628366313),
            stationPositions(35.798448095, 129.1387937113),
            stationPositions(35.551582289, 129.138493907),
            stationPositions(35.819322921, 128.727612868),
            stationPositions(35.474467976, 128.771203688),
            stationPositions(35.205435931, 128.9971386603),
            stationPositions(35.115078556, 129.041418419),
        )
        var lineChecked = 0
        val markers = mutableListOf<Marker>()

        // 네비게이션 항목 선택시
        binding.mapNavView.setNavigationItemSelectedListener {
            if(it.itemId == R.id.menu_item1){
                AlertDialog.Builder(this).run {
                    setTitle("ktx 노선 선택")
                    setSingleChoiceItems(ktxLines, lineChecked, object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, checked: Int) {
                            Toast.makeText(context, "${ktxLines[checked]}을 선택하셨습니다.", Toast.LENGTH_SHORT).show()
                            binding.currentKtxLines.text = "현재 노선 : ${ktxLines[checked]}"
                            lineChecked = checked
                            if(checked == 0) {
                                for(i in 0..markers.size - 1) {
                                    markers[i].map = null
                                }
                                for(i in 0..gyeongbuLine.size - 1) {
                                    markers.add(Marker())
                                    markers[i].position = LatLng(gyeongbuLine[i].latitude, gyeongbuLine[i].longitude)
                                    markers[i].map = naverMap
                                }
                                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(gyeongbuLine[1].latitude, gyeongbuLine[1].longitude), 5.5)
                                naverMap.moveCamera(cameraUpdate)
                            }
                            else if(checked == 1) {
                                for(i in 0..markers.size - 1) {
                                    markers[i].map = null
                                }
                            }
                        }
                    })
                    setPositiveButton("닫기", null)
                    show()
                }
            }
            else if(it.itemId == R.id.menu_item2) {
                Toast.makeText(this, "관광지가 표시됩니다.", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // 권한 가져오기
        var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // mapFragment 설정
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        // 네이버 객체 얻기
        mapFragment.getMapAsync(this)
    }

    data class stationPositions(
        val latitude: Double,
        val longitude: Double
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // 네이버 지도 객체 준비
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        val uiSettings = naverMap.uiSettings

        uiSettings.isCompassEnabled = true
        uiSettings.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
