package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.databinding.FragmentRegularRoomBinding
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import kotlin.getValue

class RegularRoomFragment : Fragment() {
    private var _binding: FragmentRegularRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    private var bossRoom: Boolean = false
    private var firstTime = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegularRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        bossRoom = requireArguments().getBoolean("boss")

        if (bossRoom) {
            viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomBoss())
        } else {
            viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomEnemy())
        }


        updateStats()

        viewModel.attack.observe(viewLifecycleOwner) { attack ->
            if (firstTime) {
                firstTime = false
                return@observe
            }
            if (attack == null) {
                simulateSkipRound()
            } else {
                simulateRound(attack)
            }

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun simulateRound(playerAttack: Attack) {
        lifecycleScope.launch {
            (requireContext() as DungeonExplorationActivity).startBlockingInputs()
            simulatePlayerAttack(playerAttack)
            (requireContext() as DungeonExplorationActivity).stopBlockingInputs()
        }
    }

    fun simulateSkipRound() {
        lifecycleScope.launch {
            (requireContext() as DungeonExplorationActivity).startBlockingInputs()
            simulateEnemyAttack(viewModel.getSelectedCharacter()!!, viewModel.getEnemy()!!)
            (requireContext() as DungeonExplorationActivity).stopBlockingInputs()
        }
    }

    suspend fun simulatePlayerAttack(playerAttack: Attack) {
        val enemyCharacter = viewModel.getEnemy()
        val playerCharacter = viewModel.getSelectedCharacter()
        if (enemyCharacter == null || playerCharacter == null) return

        val (damage, hitType) = playerCharacter.calculateDamageForAttack(playerAttack)

        val (enemyIsDead, msg) = enemyCharacter.takeAttack(damage, playerAttack, hitType)
        updateStats()

        val str = "$hitType\n$msg"

        launchAttackAnimation(1000, str)


        if (enemyIsDead) {
            viewModel.setReadyForNextRoom()
            val dungeon = viewModel.getDungeon()
            if (dungeon == null) return
            val exp = dungeon.getExpForEnemy()
            viewModel.addExpEarned(exp)
            return
        }

        delay(200)

        simulateEnemyAttack(playerCharacter, enemyCharacter)
    }

    suspend fun simulateEnemyAttack(playerCharacter: PlayerCharacter, enemyCharacter: EnemyCharacter) {
        val enemyAttack: Attack = enemyCharacter.chooseRandAttack()
        val (damage, hitType) = enemyCharacter.calculateDamageForAttack(enemyAttack)

        val (isPlayerDead, msg) = playerCharacter.takeAttack(enemyAttack, damage, hitType)

        (requireContext() as DungeonExplorationActivity).updateStats()

        val str = "$hitType\n$msg"

        launchAttackAnimation(1000, str)

        if (!isPlayerDead) playerCharacter.updateNewTurn()
        if (enemyCharacter.isAlive()) enemyCharacter.updateNewTurn()

        (requireContext() as DungeonExplorationActivity).updateStats()
        updateStats()

        moveToNextCharacter(isPlayerDead)
    }

    fun moveToNextCharacter(isPlayerDead: Boolean) {
        if (isPlayerDead) {
            viewModel.removeCurrentMember()
        } else {
            viewModel.cycleSelectedCharacter()
            (requireContext() as DungeonExplorationActivity).updateStats()
            (requireContext() as DungeonExplorationActivity).setAttacks()
        }
    }

    suspend fun launchAttackAnimation(len: Long, attackDesc: String) {
        binding.attackAnimation.visibility = View.VISIBLE
        binding.attackAnimation.speed = 600.0f / len
        binding.attackAnimation.playAnimation()
        binding.hitTypeText.text = attackDesc

        delay(len)

        binding.hitTypeText.text = ""
        binding.attackAnimation.visibility = View.GONE
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