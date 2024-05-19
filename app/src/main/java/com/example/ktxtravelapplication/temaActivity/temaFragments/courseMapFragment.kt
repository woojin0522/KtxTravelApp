package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ktxtravelapplication.R
import com.example.ktxtravelapplication.databinding.FragmentCourseMapBinding
import com.example.ktxtravelapplication.databinding.FragmentFestivalMapBinding
import com.example.ktxtravelapplication.temaActivity.festivalMapData
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

class courseMapFragment : Fragment() {
    private lateinit var mapView: MapView
    lateinit var locationSource: FusedLocationSource
    lateinit var binding: FragmentFestivalMapBinding
    companion object {
        fun newInstance() : courseMapFragment {
            val fragment = courseMapFragment()

            val args = Bundle()
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
        val binding = FragmentCourseMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.course_map_view)
        mapView.onCreate(savedInstanceState)

        val binding = FragmentFestivalMapBinding.inflate(layoutInflater)

        mapView.getMapAsync {
            locationSource = FusedLocationSource(this, courseMapFragment.LOCATION_PERMISSION_REQUEST_CODE)

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