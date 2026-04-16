package com.example.proyecto_final_dwa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final_dwa.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}