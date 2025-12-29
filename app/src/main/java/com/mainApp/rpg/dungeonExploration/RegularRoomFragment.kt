package com.mainApp.rpg.dungeonExploration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mainApp.rpg.databinding.DungeonCharacterUiBinding
import com.mainApp.rpg.databinding.FragmentRegularRoomBinding
import com.mainApp.rpg.enemy.EnemyCharacter
import com.mainApp.rpg.attack.Attack
import com.mainApp.rpg.playerCharacter.PlayerCharacter
import com.mainApp.rpg.playerCharacter.StatusEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
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

        if (bossRoom) {
            viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomBoss(), 1)
        } else {
            val rand = Random()
            for (i in 2 downTo 0) {
                if (rand.nextInt(2) == 1) {
                    viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomEnemy(), i)
                    viewModel.setSelectedEnemy(i)
                }
            }
            if (viewModel.getEnemiesSize() == 0) {
                val randInd = Random().nextInt(3)
                viewModel.addEnemy(viewModel.getDungeon()!!.rollRandomEnemy(), randInd)
                viewModel.setSelectedEnemy(randInd)
            }
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
                disallowChoosingTarget()
                simulateRound(attack, target)
            }
        }

        viewModel.readyForNextRoom.observe(viewLifecycleOwner) {
            awardExpForEnemyKilled()
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

    fun disallowChoosingTarget() {
        choosingTarget = false
        binding.centeredText.text = ""
    }

    fun chooseTarget(ind: Int) {
        if (!choosingTarget) return

        if (viewModel.getEnemy(ind) == null) return
        disallowChoosingTarget()

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
        val enemyThatWillAttack = viewModel.getSelectedEnemy()
        if (enemyThatWillAttack == null) {
            viewModel.setReadyForNextRoom()
            return
        }
        val playerCharacter = viewModel.getSelectedCharacter()!!

        val (damage, hitType) = playerCharacter.calculateDamageForAttack(playerAttack)

        val (damageTaken, msg) = enemyCharacter.takeAttack(damage, playerAttack, hitType)
        playerCharacter.takeCostFromAttackUsed(playerAttack)

        (requireContext() as DungeonExplorationActivity).updateStats()
        updateStats()

        launchAttackAnimation(1500, playerAttack.name, damageTaken, hitType)

        delay(200)

        if (enemyThatWillAttack.isDead()) {
            simulateRoundChange(playerCharacter, enemyThatWillAttack)
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

        val enemyAttack: Attack? = enemyCharacter.chooseRandAttack()
        if (enemyAttack == null) {
            moveToNextRound()
            return
        }
        val (damage, hitType) = enemyCharacter.calculateDamageForAttack(enemyAttack)

        val (damageTaken, msg) = characterOnDefense.takeAttack(enemyAttack, damage, hitType)

        (requireContext() as DungeonExplorationActivity).updateStats()

        launchAttackAnimation(1500, enemyAttack.name, damageTaken, hitType)

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

    fun awardExpForEnemyKilled() {
        val dungeon = viewModel.getDungeon()
        if (dungeon != null) {
            val exp = if (bossRoom) dungeon.getExpForBoss() else dungeon.getExpForEnemy()
            viewModel.addExpEarned(exp)
        }
    }

    fun moveToNextRound() {
        viewModel.cycleSelectedCharacter()
        viewModel.cycleSelectedEnemy()
        updateStats()
        (requireContext() as DungeonExplorationActivity).updateStats()
        (requireContext() as DungeonExplorationActivity).setAttacks()
    }

    suspend fun launchAttackAnimation(len: Long, attackName: String, damage: Int, hitType: Attack.HitTypes) {
        binding.attackAnimation.visibility = View.VISIBLE
        binding.attackAnimation.speed = 600.0f / len
        binding.attackAnimation.playAnimation()

        binding.hitDescriptionText.text = attackName
        binding.damageAmountText.text = "Damage: $damage\nHit Type: $hitType"

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
            if (i < statusEffects.size) {
                val imgID = StatusEffect.getImgFromID(statusEffects[i].id)
                if (imgID != null) statusEffectImages[i].setImageResource(imgID)
                statusEffectImages[i].visibility = View.VISIBLE
            } else {
                statusEffectImages[i].visibility = View.INVISIBLE
            }
        }
    }

    fun updateProgressBars(enemy: EnemyCharacter, container: DungeonCharacterUiBinding) {
        container.healthProgressbar.max = enemy.baseStats.getHealth()
        container.healthProgressbar.progress = enemy.currentStats.getHealth()

        container.manaProgressbar.max = enemy.baseStats.getMana()
        container.manaProgressbar.progress = enemy.currentStats.getMana()

        container.staminaProgressbar.max = enemy.baseStats.getStamina()
        container.staminaProgressbar.progress = enemy.currentStats.getStamina()

        if (enemy.getShieldHitPoints() > 0) {
            container.healthProgressbar.max += enemy.getShieldHitPoints()
            container.healthProgressbar.secondaryProgress = enemy.getShieldHitPoints() + enemy.currentStats.getHealth()
        } else {
            container.healthProgressbar.secondaryProgress = 0
        }

    }

}