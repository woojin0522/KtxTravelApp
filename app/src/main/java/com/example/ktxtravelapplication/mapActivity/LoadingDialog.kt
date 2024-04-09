package com.example.ktxtravelapplication.mapActivity

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ktxtravelapplication.R

class LoadingDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_dialog)

        //취소 불가능
        setCancelable(false)

        // 배경 투명
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}