package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.bikinggame.databinding.FragmentRegularRoomBinding
import com.example.bikinggame.playerCharacter.Attack
import kotlin.getValue

class RegularRoomFragment : Fragment() {
    private var _binding: FragmentRegularRoomBinding? = null

    private val binding get() = _binding!!

    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegularRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomEnemy())

        updateStats()

        viewModel.attack.observe(viewLifecycleOwner, Observer { attack ->
            simulateRound(attack)
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun simulateRound(playerAttack: Attack) {
        val enemyCharacter = viewModel.getEnemy()!!
        val playerCharacter = viewModel.getSelectedCharacter()!!

        val enemyIsDead = enemyCharacter.takeAttack(playerAttack)

        updateStats()

        if (enemyIsDead) {
            viewModel.setReadyForNextRoom()
            return
        }

        val enemyAttack: Attack = enemyCharacter.chooseRandAttack()

        val isPlayerDead = playerCharacter.takeAttack(enemyAttack)

        (requireActivity() as DungeonExplorationActivity).updateStats()

        if (isPlayerDead) {
            viewModel.setPartyHasDied()
        }
    }

    fun updateStats() {
        try {
            val enemyCharacter = viewModel.getEnemy()!!
            binding.healthProgressbar.progress = (enemyCharacter.currentStats.getHealth().toDouble()
                    / enemyCharacter.baseStats.getHealth().toDouble() * 100.0).toInt()
            binding.manaProgressbar.progress = (enemyCharacter.currentStats.getMana().toDouble()
                    / enemyCharacter.baseStats.getMana().toDouble() * 100.0).toInt()
            binding.staminaProgressbar.progress = (enemyCharacter.currentStats.getStamina().toDouble()
                    / enemyCharacter.baseStats.getStamina().toDouble() * 100.0).toInt()
        } catch (e: Exception) {
            Log.d("Battle Fragment", e.toString())
        }
    }

}