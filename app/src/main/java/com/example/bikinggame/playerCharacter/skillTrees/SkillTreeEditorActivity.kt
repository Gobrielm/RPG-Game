package com.example.bikinggame.playerCharacter.skillTrees

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.databinding.ActivitySkillTreeEditorBinding
import com.example.bikinggame.gui.PointEditorView

class SkillTreeEditorActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySkillTreeEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySkillTreeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editor = binding.pointEditor

            binding.exportButton.setOnClickListener {
            val json = editor.exportPointsAsJson()
            Log.d("POINT_EDITOR", json)
        }
    }
}