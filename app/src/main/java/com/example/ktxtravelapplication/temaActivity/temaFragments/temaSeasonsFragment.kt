package com.example.ktxtravelapplication.temaActivity.temaFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ktxtravelapplication.databinding.FragmentTemaSeasonsBinding
import com.example.ktxtravelapplication.temaActivity.temaSeasons.TemaFallActivity
import com.example.ktxtravelapplication.temaActivity.temaSeasons.TemaSpringActivity
import com.example.ktxtravelapplication.temaActivity.temaSeasons.TemaSummerActivity
import com.example.ktxtravelapplication.temaActivity.temaSeasons.TemaWinterActivity

@Suppress("UNREACHABLE_CODE")
class temaSeasonsFragment : Fragment() {
    lateinit var binding: FragmentTemaSeasonsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTemaSeasonsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.temaSpringBtn.setOnClickListener{
            val intent = Intent(activity, TemaSpringActivity::class.java)
            startActivity(intent)
        }
        binding.temaSummerBtn.setOnClickListener {
            val intent = Intent(activity, TemaSummerActivity::class.java)
            startActivity(intent)
        }
        binding.temaFallBtn.setOnClickListener{
            val intent = Intent(activity, TemaFallActivity::class.java)
            startActivity(intent)
        }
        binding.temaWinterBtn.setOnClickListener{
            val intent = Intent(activity, TemaWinterActivity::class.java)
            startActivity(intent)
        }
    }
}