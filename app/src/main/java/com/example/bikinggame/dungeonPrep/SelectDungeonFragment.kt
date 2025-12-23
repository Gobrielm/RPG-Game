package com.example.bikinggame.dungeonPrep

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.databinding.FragmentSelectDungeonBinding
import com.example.bikinggame.homepage.HomePage
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.homepage.inventory.PlayerInventory.playerCharacters
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.iterator
import kotlin.getValue
import kotlin.math.min


class SelectDungeonFragment : Fragment() {

    private var _binding: FragmentSelectDungeonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DungeonPrepViewModel by activityViewModels()
    private var deepestRoomAllowed: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectDungeonBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.dungeonButton.setOnClickListener {
            viewModel.startDungeon()
            if ((requireContext() as DungeonPrepActivity).getCharacters().isEmpty()) {
                lifecycleScope.launch {
                    if (_binding == null) return@launch
                    binding.errorText.text = "Pick a Character"
                    delay(1400)
                    if (_binding == null) return@launch
                    binding.errorText.text = ""
                }
            }
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), HomePage::class.java)
            startActivity(intent)
        }

        binding.floorTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                var closestTen = (binding.floorTextBox.text.toString().toInt() / 10) * 10 // Takes floor to nearest ten
                closestTen = min(closestTen, deepestRoomAllowed)
                binding.floorTextBox.setText(closestTen.toString())
                (requireContext() as DungeonPrepActivity).startingDepth = closestTen
            }
        }

        binding.floorTextBox.setText((requireContext() as DungeonPrepActivity).startingDepth.toString())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun loadDeepestRoom() {
        val filename = "deepest_room"
        try {
            requireContext().openFileInput(filename).bufferedReader().useLines { lines ->
                for (line in lines) {
                    deepestRoomAllowed = line.toInt()
                }
            }

        } catch (err: Exception) {
            Log.d("SelectDungeonFragment", err.toString())
            saveDeepestRoom() // Just save a 0 to avoid future errors
        }
        binding.deepestRoomText.text = "Deepest Room: ${deepestRoomAllowed}"
    }

    fun saveDeepestRoom() {
        val filename = "deepest_room"
        val data = deepestRoomAllowed.toString()

        try {
            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(data.toByteArray())
            }

        } catch (err: Exception) {
            Log.d("SelectDungeonFragment", err.toString())
        }
    }

}