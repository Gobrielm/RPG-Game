package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter

interface Dungeon {

    fun getRoom(roomInd: Int): DungeonRooms?

    fun rollRandomEnemy(): EnemyCharacter


    companion object {

        val dungeons = mapOf(
            // Forest Dungeon
            1 to FiniteDungeon(1, 1, DungeonLayout(5, 0, 0.15f)),

            // Cave Dungeon
            2 to FiniteDungeon(2, 2, DungeonLayout(7, 1, 0.15f)),
        )

        fun getDungeon(dungeonID: Int): FiniteDungeon? {
            return dungeons[dungeonID]
        }
    }
}