package com.example.bikinggame.dungeonPrep

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.databinding.ActivityDungeonPrepBinding

class DungeonPrepActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonPrepBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}