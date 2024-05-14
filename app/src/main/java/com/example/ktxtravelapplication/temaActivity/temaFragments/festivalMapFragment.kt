package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.annotation.UiThread
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentFestivalMapBinding
import com.example.ktxtravelapplication.mapActivity.MapActivity
import com.example.ktxtravelapplication.temaActivity.festivalMapData
import com.google.firebase.database.collection.LLRBNode
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import java.io.Serializable

class festivalMapFragment : Fragment() {
    private lateinit var mapView: MapView
    lateinit var locationSource: FusedLocationSource
    lateinit var binding: FragmentFestivalMapBinding
    companion object {
        fun newInstance(mapDataList: MutableList<festivalMapData>) : festivalMapFragment{
            val fragment = festivalMapFragment()

            val args = Bundle()
            args.putSerializable("mapDataList", mapDataList as Serializable)
            fragment.arguments = args

            return fragment
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentFestivalMapBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.festival_map_view)
        mapView.onCreate(savedInstanceState)

        val binding = FragmentFestivalMapBinding.inflate(layoutInflater)

        val mapDataList = arguments?.getSerializable("mapDataList") as MutableList<festivalMapData>
        val festivalName = mapDataList[0].festivalName
        val festivalMapx = mapDataList[0].festivalMapx
        val festivalMapy = mapDataList[0].festivalMapy
        val stationName = mapDataList[0].stationName
        val stationMapx = mapDataList[0].stationMapx
        val stationMapy = mapDataList[0].stationMapy

        mapView.getMapAsync {
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

            val marker = Marker()
            marker.position = LatLng(stationMapy, stationMapx)
            marker.captionText = stationName + "역"
            marker.setCaptionAligns(Align.Top)
            marker.captionColor = Color.BLUE
            marker.map = it

            val marker2 = Marker()
            marker2.position = LatLng(festivalMapy, festivalMapx)
            marker2.icon = MarkerIcons.BLACK
            marker2.iconTintColor = Color.RED
            marker2.captionText = festivalName
            marker2.setCaptionAligns(Align.Top)
            marker2.captionColor = Color.BLUE
            marker2.map = it

            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng((festivalMapy + stationMapy)/2.0, (festivalMapx + stationMapx)/2.0), 13.0)
            it.moveCamera(cameraUpdate)

            val uiSettings = it.uiSettings
            uiSettings.isCompassEnabled = true
            uiSettings.isLocationButtonEnabled = true

            it.locationSource = locationSource
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
