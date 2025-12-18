package com.example.bikinggame.playerCharacter

import android.util.Log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

private const val TAG: String = "CharacterStats"

enum class BasicStats {
    BaseHealth,
    BaseMana,
    BaseStamina,

    Strength,
    Casting,

    Constitution,
    Intelligence,
    Dexterity;

    companion object {
        fun fromInt(value: Int): BasicStats? {
            return BasicStats.entries.find { it.ordinal == value }
        }
    }
}

class CharacterStats {


    /**
     * @return with (Hit Type, Damage)
     *
     */
    fun calculateDamageForAttack(attack: Attack): Pair<Int, Attack.HitTypes> {
        val baseDamage = attack.getMomentum()
        val hitType = attack.getRandomHitType()
        val statMult = getStatDamageMultiplier(attack.type)

        return Pair(round(baseDamage * hitType.getMultiplier() * statMult).toInt(), hitType)
    }

    fun getStatDamageMultiplier(attackType: Attack.AttackTypes): Float {
        return 1f + when (attackType) {
            Attack.AttackTypes.PHY -> {
                getStrength() / 50.0f
            }
            Attack.AttackTypes.MAG -> {
                getCasting() / 50.0f
            }
            else -> {
                getStrength() / 120.0f
            }
        }
    }

    fun getDamageBlockedForAttack(attackType: Attack.AttackTypes): Int {
        if (attackType == Attack.AttackTypes.PHY) {
            return round(getConstitution() / 2.0f).toInt()
        } else if (attackType == Attack.AttackTypes.RAN) {
            return round(getConstitution() / 4.0f).toInt()
        }
        return 0
    }

    /**
     *  @return Msg
     */
    fun getAttacked(damage: Int, attack: Attack, hitType: Attack.HitTypes, canDodge: Boolean): String {

        val blocked = getDamageBlockedForAttack(attack.type)
        var damage = max(0, damage - blocked)

        if (damage < 0) return ""

        var msg = ""
        if (canDodge) {
            val (status, newMsg) = attemptDodge(attack, hitType)
            if (status) {
                damage = 0
            }
            msg = newMsg
        }

        if (damage != 0 && attack.statusEffectInflictChance != null) {
            val rand: Int = kotlin.random.Random.nextInt(0, 100)
            val (chance, statusEffect) = attack.statusEffectInflictChance
            if (chance > rand) {
                addStatusEffect(statusEffect)
            }
        }

        var health: Int = getHealth()
        health -= damage
        setHealth(if (health > 0) health else 0)
        return msg
    }

    /**
     * @return (If dodge was successful, desc msg)
     */
    fun attemptDodge(attack: Attack, hitType: Attack.HitTypes): Pair<Boolean, String> {
        val rand: Int = kotlin.random.Random.nextInt(0, 100)
        val velocity = attack.velocity * hitType.getMultiplier()

        // Can attempt dodge
        if (getDexterity() > velocity) {
            val cost = round(velocity * 2.0f).toInt()
            if (getStamina() < cost) return Pair(false, "")

            setStamina(getStamina() - cost)

            val chanceToFail = 100 - round(velocity / 2.0f / getDexterity() * 100).toInt()

            val msg = if (rand < chanceToFail) {
                "Failed Dodge"
            } else {
                "Dodged"
            }

            return Pair(rand >= chanceToFail, msg)
        }
        return Pair(false, "")
    }

    fun getPrimaryStat(stat: BasicStats): Int {
        if (stat == BasicStats.BaseHealth) return getHealth()
        if (stat == BasicStats.BaseStamina) return getStamina()
        if (stat == BasicStats.BaseMana) return getMana()
        Log.e("Character Stats", "Tried to Access non primary stat")
        return -1
    }

    fun getHealth(): Int {
        return characterStats[BasicStats.BaseHealth] ?: 0
    }

    fun setHealth(value: Int) {
        characterStats[BasicStats.BaseHealth] = value
    }

    fun getMana(): Int {
        return characterStats[BasicStats.BaseMana] ?: 0
    }

    fun setMana(value: Int) {
        characterStats[BasicStats.BaseMana] = value
    }

    fun getStamina(): Int {
        return characterStats[BasicStats.BaseStamina] ?: 0
    }

    fun setStamina(value: Int) {
        characterStats[BasicStats.BaseStamina] = value
    }

    fun getStrength(): Int {
        return characterStats[BasicStats.Strength]!! - getDebuffForStat(BasicStats.Strength)
    }

    fun getCasting(): Int {
        return characterStats[BasicStats.Casting]!! - getDebuffForStat(BasicStats.Casting)
    }

    fun getConstitution(): Int {
        return characterStats[BasicStats.Constitution]!! - getDebuffForStat(BasicStats.Constitution)
    }

    fun getIntelligence(): Int {
        return characterStats[BasicStats.Intelligence]!! - getDebuffForStat(BasicStats.Intelligence)
    }

    fun getDexterity(): Int {
        return characterStats[BasicStats.Dexterity]!! - getDebuffForStat(BasicStats.Dexterity)
    }

    fun raiseStat(stat: BasicStats, amount: Int) {
        characterStats[stat] = characterStats[stat]!! + amount
    }

    fun lowerStat(stat: BasicStats, amount: Int) {
        characterStats[stat] = characterStats[stat]!! - amount
        characterStats[stat] = max(characterStats[stat]!!, 0)
    }

    fun getDebuffForStat(stat: BasicStats): Int {
        var total = 0
        for (i in 0 until statusEffects.size) {
            val statusEffect = statusEffects[i]
            if (statusEffect.statDebuff?.first == stat) total += statusEffect.statDebuff.second
        }
        return total
    }

    fun addStatusEffect(statusEffect: StatusEffect) {
        if (statusEffects.size >= 3) return // Don't add more than 3 status effects
        statusEffects.add(statusEffect.copy())
    }

    fun getStatusEffects(): ArrayList<StatusEffect> {
        return statusEffects
    }

    fun updateNewTurn() {
        val iterator = statusEffects.iterator()
        while (iterator.hasNext()) {
            val statusEffect = iterator.next()
            if (statusEffect.updateNewTurn()) {
                iterator.remove()
            } else {
                if (statusEffect.statDecrease == null) continue
                val (stat, decrease) = statusEffect.statDecrease
                lowerStat(stat, decrease)
            }
        }
    }

    fun removeAllStatusEffects() {
        statusEffects.clear()
    }

    fun regenStamina(maxStamina: Int) {
        val newStamina = min(maxStamina, round(getStamina() + getDexterity() / 3.0f).toInt())
        setStamina(newStamina)
    }

    fun regenMana(maxMana: Int) {
        val newMana = min(maxMana, round(getMana() + getIntelligence() / 2.0f).toInt())
        setMana(newMana)
    }

    private val characterStats: MutableMap<BasicStats, Int>

    private val statusEffects: ArrayList<StatusEffect> = arrayListOf()

    constructor() {
        characterStats = mutableMapOf(
            BasicStats.BaseHealth to 0,
            BasicStats.BaseMana to 0,
            BasicStats.BaseStamina to 0,
            BasicStats.Strength to 0,
            BasicStats.Casting to 0,
            BasicStats.Constitution to 0,
            BasicStats.Intelligence to 0,
            BasicStats.Dexterity to 0
        )
    }

    constructor(pCharacterStats: MutableMap<BasicStats, Int>) {
        characterStats = pCharacterStats
    }

    constructor(pOtherCharacterStats: CharacterStats): this((pOtherCharacterStats.characterStats).toMutableMap())

    constructor(subClass: CharacterSubClass) {
        characterStats = mutableMapOf(
            BasicStats.BaseHealth to baseHealthMap[subClass]!!,
            BasicStats.BaseMana to baseManaMap[subClass]!!,
            BasicStats.BaseStamina to baseStaminaMap[subClass]!!,
            BasicStats.Strength to baseStrengthMap[subClass]!!,
            BasicStats.Casting to baseCastingMap[subClass]!!,
            BasicStats.Constitution to baseConstitutionMap[subClass]!!,
            BasicStats.Intelligence to baseIntelligenceMap[subClass]!!,
            BasicStats.Dexterity to baseDexterityMap[subClass]!!
        )
    }

    override fun toString(): String {
        return """
            Health: ${characterStats[BasicStats.BaseHealth]}
            Mana: ${characterStats[BasicStats.BaseMana]}
            Stamina: ${characterStats[BasicStats.BaseStamina]}
            Strength: ${characterStats[BasicStats.Strength]}
            Casting: ${characterStats[BasicStats.Casting]}
            Constitution: ${characterStats[BasicStats.Constitution]}
            Intelligence: ${characterStats[BasicStats.Intelligence]}
            Dexterity: ${characterStats[BasicStats.Dexterity]}
        """.trimIndent()
    }

    companion object {
        val baseHealthMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 20,
            CharacterSubClass.RitualMagic to 25,
            CharacterSubClass.Knight to 40,
            CharacterSubClass.North to 35,
            CharacterSubClass.TraditionalRanged to 20,
            CharacterSubClass.NonTraditionalRanged to 25
        )

        val baseManaMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 15,
            CharacterSubClass.RitualMagic to 10,
            CharacterSubClass.Knight to 5,
            CharacterSubClass.North to 5,
            CharacterSubClass.TraditionalRanged to 5,
            CharacterSubClass.NonTraditionalRanged to 10
        )

        val baseStaminaMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 13,
            CharacterSubClass.Knight to 5,
            CharacterSubClass.North to 5,
            CharacterSubClass.TraditionalRanged to 17,
            CharacterSubClass.NonTraditionalRanged to 20
        )

        val baseStrengthMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 3,
            CharacterSubClass.RitualMagic to 4,
            CharacterSubClass.Knight to 12,
            CharacterSubClass.North to 15,
            CharacterSubClass.TraditionalRanged to 8,
            CharacterSubClass.NonTraditionalRanged to 6
        )

        val baseCastingMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 8,
            CharacterSubClass.Knight to 1,
            CharacterSubClass.North to 1,
            CharacterSubClass.TraditionalRanged to 2,
            CharacterSubClass.NonTraditionalRanged to 4
        )

        val baseConstitutionMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 2,
            CharacterSubClass.RitualMagic to 1,
            CharacterSubClass.Knight to 7,
            CharacterSubClass.North to 4,
            CharacterSubClass.TraditionalRanged to 2,
            CharacterSubClass.NonTraditionalRanged to 2
        )

        val baseIntelligenceMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 7,
            CharacterSubClass.Knight to 3,
            CharacterSubClass.North to 2,
            CharacterSubClass.TraditionalRanged to 4,
            CharacterSubClass.NonTraditionalRanged to 2
        )

        val baseDexterityMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 4,
            CharacterSubClass.RitualMagic to 6,
            CharacterSubClass.Knight to 2,
            CharacterSubClass.North to 4,
            CharacterSubClass.TraditionalRanged to 10,
            CharacterSubClass.NonTraditionalRanged to 12
        )

    }
}