package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.databinding.DungeonCharacterUiBinding
import com.example.bikinggame.databinding.FragmentRegularRoomBinding
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.attack.Attack
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class RegularRoomFragment : Fragment() {
    private var _binding: FragmentRegularRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    private var bossRoom: Boolean = false
    private var firstTime = true
    private var choosingTarget = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegularRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        bossRoom = requireArguments().getBoolean("boss")

        // TODO: ADd more enemies
        if (bossRoom) {
            viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomBoss())
        } else {
            viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomEnemy())
            viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomEnemy())
        }

        val buttons = arrayOf(binding.target1Button, binding.target2Button, binding.target3Button)
        for (i in 0 until buttons.size) {
            val button = buttons[i]
            button.setOnClickListener {
                chooseTarget(i)
            }
        }

        updateStats()

        viewModel.attack.observe(viewLifecycleOwner) { attackTargetPair ->
            if (firstTime) {
                firstTime = false
                return@observe
            }
            if (attackTargetPair == null) {
                simulateSkipRound()
            }

            val (attack, target) = attackTargetPair!!
            if (target == -1) {
                allowChoosingTarget()
            } else {
                simulateRound(attack, target)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun allowChoosingTarget() {
        choosingTarget = true
        binding.centeredText.text = "Pick a Target"
    }

    fun chooseTarget(ind: Int) {
        if (!choosingTarget) return

        if (viewModel.getEnemy(ind) == null) return
        binding.centeredText.text = ""
        choosingTarget = false

        simulateRound(viewModel.attack.value!!.first, ind)
    }

    fun simulateRound(playerAttack: Attack, target: Int) {
        lifecycleScope.launch {
            (requireContext() as DungeonExplorationActivity).startBlockingInputs()
            if (playerAttack.friendlyAttack) {
                simulateFriendlyPlayerAttack(playerAttack, target)
            } else {
                simulatePlayerAttack(playerAttack, target)
            }
            (requireContext() as DungeonExplorationActivity).stopBlockingInputs()
        }
    }

    fun simulateSkipRound() {
        lifecycleScope.launch {
            (requireContext() as DungeonExplorationActivity).startBlockingInputs()
            simulateEnemyAttack()
            (requireContext() as DungeonExplorationActivity).stopBlockingInputs()
        }
    }

    suspend fun simulatePlayerAttack(playerAttack: Attack, target: Int) {
        if (viewModel.partyDied.value!! || viewModel.partyDone.value!!) return
        val enemyCharacter = viewModel.getEnemy(target)!!
        if (viewModel.getSelectedEnemy() == null) {
            viewModel.setReadyForNextRoom()
            return
        }
        val playerCharacter = viewModel.getSelectedCharacter()!!

        val (damage, hitType) = playerCharacter.calculateDamageForAttack(playerAttack)

        val (damageTaken, msg) = enemyCharacter.takeAttack(damage, playerAttack, hitType)
        playerCharacter.takeCostFromAttackUsed(playerAttack)

        (requireContext() as DungeonExplorationActivity).updateStats()
        updateStats()

        val str = "$hitType\n$msg"

        launchAttackAnimation(1000, str, damageTaken)

        delay(200)

        if (!enemyCharacter.isAlive()) {
            simulateRoundChange(playerCharacter, enemyCharacter)
            moveToNextRound()
            return
        }

        simulateEnemyAttack()
    }

    suspend fun simulateFriendlyPlayerAttack(playerAttack: Attack, target: Int) {
        if (viewModel.partyDied.value!! || viewModel.partyDone.value!!) return
        val targetCharacter = viewModel.getCharacter(target)!!
        val playerCharacter = viewModel.getSelectedCharacter()!!

        // Heal and apply status effects
        val healing = targetCharacter.takeHealing(playerAttack)
        playerCharacter.takeCostFromAttackUsed(playerAttack)

        (requireContext() as DungeonExplorationActivity).updateStats()
        updateStats()

        launchHealAnimation(1000, healing)

        delay(200)

        simulateEnemyAttack()
    }

    suspend fun simulateEnemyAttack() {
        val enemyCharacter = viewModel.getSelectedEnemy()!!
        val (ind, characterOnDefense) = viewModel.getRandomCharacter()
        (requireContext() as DungeonExplorationActivity).showAttackIndicatorOnCharacter(ind)

        val enemyAttack: Attack = enemyCharacter.chooseRandAttack()
        val (damage, hitType) = enemyCharacter.calculateDamageForAttack(enemyAttack)

        val (damageTaken, msg) = characterOnDefense.takeAttack(enemyAttack, damage, hitType)

        (requireContext() as DungeonExplorationActivity).updateStats()

        val str = "$hitType\n$msg"

        launchAttackAnimation(1000, str, damageTaken)

        simulateRoundChange(characterOnDefense, enemyCharacter)

        (requireContext() as DungeonExplorationActivity).updateStats()
        updateStats()

        delay(350)

        moveToNextRound()
    }

    // Also simulates status effects
    fun simulateRoundChange(characterOnDefense: PlayerCharacter, enemyCharacter: EnemyCharacter?) {
        if (characterOnDefense.isAlive()) characterOnDefense.updateNewTurn()
        if (enemyCharacter != null && enemyCharacter.isAlive()) enemyCharacter.updateNewTurn()
    }

    fun moveToNextRound() {
        viewModel.cycleSelectedCharacter()
        viewModel.cycleSelectedEnemy()
        updateStats()
        (requireContext() as DungeonExplorationActivity).updateStats()
        (requireContext() as DungeonExplorationActivity).setAttacks()
    }

    suspend fun launchAttackAnimation(len: Long, attackDesc: String, damage: Int) {
        binding.attackAnimation.visibility = View.VISIBLE
        binding.attackAnimation.speed = 600.0f / len
        binding.attackAnimation.playAnimation()

        binding.hitDescriptionText.text = attackDesc
        binding.damageAmountText.text = "Damage: $damage"

        delay(len)

        binding.hitDescriptionText.text = ""
        binding.damageAmountText.text = ""
        binding.attackAnimation.visibility = View.GONE
    }

    suspend fun launchHealAnimation(len: Long, healing: Int) {
        binding.healAnimation.visibility = View.VISIBLE
        binding.healAnimation.speed = 600.0f / len
        binding.healAnimation.playAnimation()

        binding.damageAmountText.text = "Healing: $healing"

        delay(len)

        binding.damageAmountText.text = ""
        binding.healAnimation.visibility = View.GONE
    }

    fun updateStats() {
        if (viewModel.partyDone.value!!) return
        if (_binding == null) return
        val enemy1 = viewModel.getEnemy(0)
        if (enemy1 != null) {
            if (enemy1 == viewModel.getSelectedEnemy()) {
                highlightEnemy(binding.enemyUi1)
            } else {
                resetHighlights(binding.enemyUi1)
            }

            if (!enemy1.isAlive()) {
                binding.enemyUi1Container.visibility = View.INVISIBLE
            } else {
                binding.enemyUi1Container.visibility = View.VISIBLE
            }

            binding.enemyUi1.nameTextView.text = enemy1.name
            updateProgressBars(
                enemy1,
                binding.enemyUi1
            )

            updateStatusEffectsOnMainGui(enemy1, binding.enemyUi1)
        } else {
            binding.enemyUi1Container.visibility = View.INVISIBLE
        }

        val enemy2 = viewModel.getEnemy(1)
        if (enemy2 != null) {

            if (enemy2 == viewModel.getSelectedEnemy()) {
                highlightEnemy(binding.enemyUi2)
            } else {
                resetHighlights(binding.enemyUi2)
            }

            if (!enemy2.isAlive()) {
                binding.enemyUi2Container.visibility = View.INVISIBLE
            } else {
                binding.enemyUi2Container.visibility = View.VISIBLE
            }

            binding.enemyUi2.nameTextView.text = enemy2.name
            updateProgressBars(
                enemy2,
                binding.enemyUi2
            )

            updateStatusEffectsOnMainGui(enemy2, binding.enemyUi2)
        } else {
            binding.enemyUi2Container.visibility = View.INVISIBLE
        }

        val enemy3 = viewModel.getEnemy(2)
        if (enemy3 != null) {

            if (enemy3 == viewModel.getSelectedEnemy()) {
                highlightEnemy(binding.enemyUi3)
            } else {
                resetHighlights(binding.enemyUi3)
            }

            if (!enemy3.isAlive()) {
                binding.enemyUi3Container.visibility = View.INVISIBLE
            } else {
                binding.enemyUi3Container.visibility = View.VISIBLE
            }

            binding.enemyUi3.nameTextView.text = enemy3.name
            updateProgressBars(
                enemy3,
                binding.enemyUi3
            )

            binding.enemyUi3

            updateStatusEffectsOnMainGui(enemy3, binding.enemyUi3)
        } else {
            binding.enemyUi3Container.visibility = View.INVISIBLE
        }
    }

    fun highlightEnemy(container: DungeonCharacterUiBinding) {
        container.nameTextView.setTextColor(0xFF22FF22.toInt())
    }

    fun resetHighlights(container: DungeonCharacterUiBinding) {
        container.nameTextView.setTextColor(0xFF000000.toInt())
    }

    fun updateStatusEffectsOnMainGui(enemy: EnemyCharacter, container: DungeonCharacterUiBinding) {
        val statusEffects = enemy.getStatusEffects()

        val statusEffectImages = arrayOf(container.statusEffect1, container.statusEffect2, container.statusEffect3)
        for (i in 0 until 3) {
            if (i < statusEffects.size - 1) {
                // TODO: Set Img here
                statusEffectImages[i].visibility = View.VISIBLE
            } else {
                statusEffectImages[i].visibility = View.INVISIBLE
            }
        }
    }

    fun updateProgressBars(enemy: EnemyCharacter, container: DungeonCharacterUiBinding) {
        container.healthProgressbar.progress = (enemy.currentStats.getHealth().toDouble() / enemy.baseStats.getHealth() * 100.0).toInt()
        container.manaProgressbar.progress = (enemy.currentStats.getMana().toDouble() / enemy.baseStats.getMana() * 100.0).toInt()
        container.staminaProgressbar.progress = (enemy.currentStats.getStamina().toDouble() / enemy.baseStats.getStamina() * 100.0).toInt()
        container.shieldProgressbar.progress = (enemy.getShieldHitPoints().toDouble() / enemy.baseStats.getHealth() * 100.0).toInt()
    }

}