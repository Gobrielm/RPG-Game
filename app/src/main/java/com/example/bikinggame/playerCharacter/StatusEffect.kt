package com.example.bikinggame.playerCharacter

data class StatusEffect (
    private var length: Int, /* Rounds it will last */
    val statDecrease: Pair<BasicStats, Int>?, /* Reduce stat perm, for health/stamina/mana*/
    val statDebuff: Pair<BasicStats, Int>?, /* Reduce strength, const, etc.*/
    val id: Int, /* Unique */
    val name: String
) {

    fun getLength(): Int {
        return length
    }

    /**
     * @return A boolean of whether status effect is over
     */
    fun updateNewTurn(): Boolean {
        length--
        return length <= 0
    }

    companion object {
        val statusEffects = mapOf(
            1 to StatusEffect(3, Pair(BasicStats.BaseHealth, 3), null, 1, "Poison"),
            2 to StatusEffect(3, null, Pair(BasicStats.Strength, 5), 2, "Weakness")
        )

        fun getStatusEffect(id: Int): StatusEffect? {
            return statusEffects[id]
        }
    }

}