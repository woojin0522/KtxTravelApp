package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ktxtravelapplication.databinding.FragmentTemaFestivalBinding

class temaFestivalFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTemaFestivalBinding.inflate(inflater, container, false)
        return binding.root
    }
}