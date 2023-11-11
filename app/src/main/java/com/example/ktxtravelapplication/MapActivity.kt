package com.example.ktxtravelapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.ktxtravelapplication.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 선언
        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바를 툴바로 교체
        setSupportActionBar(binding.mapToolbar)
        // 토글 버튼 추가
        toggle = ActionBarDrawerToggle(this, binding.mapDrawer, R.string.map_drawer_open,
            R.string.map_drawer_close)
        // 툴바 뒤로가기 버튼 추가
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 토글 버튼을 메뉴 모양으로 변경
        toggle.syncState()
        /*binding.mapNavView.setNavigationItemSelectedListener {
            true
        }*/
    }

    // 토글 버튼 클릭시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}