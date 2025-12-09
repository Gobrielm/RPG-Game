package com.example.bikinggame.playerCharacter

data class StatusEffect (
    private var length: Int, /* Rounds it will last */
    val statDecrease: Pair<BasicStats, Int>, /* Reduce stat perm, for health/stamina/mana*/
    val statDebuff: Pair<BasicStats, Int>, /* Reduce strength, const, etc.*/
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

}