package com.example.ktxtravelapplication.mapActivity

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.ActivityMapBinding
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.KtxLinesList
import com.example.ktxtravelapplication.mapActivity.ktxLinesData.StationPositions
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

        // ktx노선 배열
        val ktxLines = arrayOf("경부선", "호남선", "경전선", "전라선", "강릉선", "중앙선", "중부내륙선")
        // 현재 선택된 라디오버튼 변수
        var lineChecked = 0
        // 마커 리스트 선언
        val markers = mutableListOf<Marker>()

        // 마커 세팅 함수
        fun markerSetting(line: MutableList<StationPositions>) {
            // 기존에 지도에 남아있던 마커 제거
            for(i in 0..markers.size - 1) {
                markers[i].map = null
            }
            // 매개변수로 받은 노선에 맞는 역에 해당하는 마커를 표시
            for(i in 0..line.size - 1) {
                markers.add(Marker())
                markers[i].position = LatLng(line[i].latitude, line[i].longitude)
                markers[i].map = naverMap
            }
            // 마커 설정후 지도가 한눈에 보이게 카메라 업뎃
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(36.332165597, 127.434310227), 5.5)
            naverMap.moveCamera(cameraUpdate)
        }
        /*//파이어스토어 사용시..
        //파이어스토어 객체 생성..
        val db = FirebaseFirestore.getInstance()
        // 파이어스토어에서 값을 가져와 해당 노선에 맞는 마커를 세팅..
        fun dbGet(ktxLine: String, markerSetLine: MutableList<StationPositions>) {
            db.collection("노선데이터")
                .document("KTX노선")
                .collection(ktxLine)
                .get()
                .addOnSuccessListener { result ->
                    for(document in result) {
                        val lat = document["lat"].toString()
                        val latitude = lat.toDouble()
                        val lng = document["lng"].toString()
                        val longitude = lng.toDouble()
                        markerSetLine.add(StationPositions(latitude, longitude))
                    }
                    markerSetting(markerSetLine)
                }
                .addOnFailureListener {exception ->
                    Log.d("test", "Error getting documents: ", exception)
                }
        }*/

        // 네비게이션 항목 선택시
        binding.mapNavView.setNavigationItemSelectedListener {
            // 네비게이션에서 ktx노선 항목 클릭시
            if(it.itemId == R.id.menu_item1){
                AlertDialog.Builder(this).run {
                    setTitle("ktx 노선 선택")
                    setSingleChoiceItems(ktxLines, lineChecked, object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, checked: Int) {
                            Toast.makeText(context, "${ktxLines[checked]}을 선택하셨습니다.", Toast.LENGTH_SHORT).show()
                            binding.currentKtxLines.text = "현재 노선 : ${ktxLines[checked]}"
                            lineChecked = checked
                            if(checked == 0) {
                                /*dbGet("경부선", KtxLinesList().gyeongbuLine)*/
                                markerSetting(KtxLinesList().gyeongbuLine)
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
            // 네비게이션에서 관광지표시 항목 클릭시
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

    /*data class stationPositions(
        val latitude: Double,
        val longitude: Double
    )*/

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
