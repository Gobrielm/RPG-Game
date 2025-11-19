package com.example.bikinggame.dungeon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.bikinggame.databinding.FragmentBattleBinding
import com.example.bikinggame.databinding.FragmentStatsPreviewBinding
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.CharacterClass
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlin.getValue

class BattleFragment : Fragment() {
    private var _binding: FragmentBattleBinding? = null

    private val binding get() = _binding!!

    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setStats()
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

        val isAlive = enemyCharacter.takeAttack(playerAttack)

        if (!isAlive) return

        val enemyAttack: Attack = enemyCharacter.chooseRandAttack()

        playerCharacter.takeAttack(enemyAttack)
        setStats()
    }

    fun setStats() {
        try {
            val enemyCharacter = viewModel.getEnemy()!!
            binding.healthProgressbar.progress = (enemyCharacter.currentStats.getHealth()
                .toDouble() / enemyCharacter.baseStats.getHealth() * 100).toInt()
            binding.manaProgressbar.progress = (enemyCharacter.currentStats.getMana()
                .toDouble() / enemyCharacter.baseStats.getMana() * 100).toInt()
            binding.staminaProgressbar.progress = (enemyCharacter.currentStats.getStamina()
                .toDouble() / enemyCharacter.baseStats.getStamina() * 100).toInt()
        } catch (e: Exception) {
            Log.d("Battle Fragment", e.toString())
        }
    }

}