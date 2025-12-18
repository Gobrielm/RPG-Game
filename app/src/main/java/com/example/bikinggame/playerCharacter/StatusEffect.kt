package com.example.bikinggame.playerCharacter

data class StatusEffect (
    val id: Int, /* Unique */
    private var length: Int, /* Rounds it will last */
    val statChangePerRound: Pair<BasicStats, Int>?, /* Reduce stat perm, for health/stamina/mana*/
    val statChange: Pair<BasicStats, Int>?, /* Reduce strength, const, etc.*/
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
            1 to StatusEffect(1, 3, Pair(BasicStats.BaseHealth, -3), null, "Poison"),
            2 to StatusEffect(2, 3, null, Pair(BasicStats.Strength, -5), "Weakness")
        )

        fun getStatusEffect(id: Int): StatusEffect? {
            return statusEffects[id]?.copy()
        }
    }

}