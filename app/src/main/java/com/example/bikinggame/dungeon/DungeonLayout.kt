package com.example.bikinggame.dungeon

import java.util.Random

enum class DungeonRooms {
    BOSS,
    REST,
    TREASURE,
    REGULAR
}

class DungeonLayout(
    val totalRooms: Int,
    val bossRooms: Int,
    val luck: Float
) {
    val treasureRooms: Int = ((totalRooms - bossRooms) * luck).toInt() + bossRooms
    val restAreas: Int = bossRooms
    val layout = generateLayout()

    fun generateLayout(): Array<DungeonRooms> {
        val layout = Array(totalRooms) { DungeonRooms.REGULAR }

        // Place Boss Rooms & Rest Rooms & Treasure Rooms
        for (i in 1 .. bossRooms) {
            val bossRoom = ((totalRooms - 2) * (i.toFloat()  / bossRooms)).toInt()
            layout[bossRoom] = DungeonRooms.BOSS
            layout[bossRoom - 1] = DungeonRooms.REST
            layout[bossRoom + 1] = DungeonRooms.TREASURE
        }

        // Place Remaining Rooms
        val regularRooms = arrayListOf<Int>()
        for (i in 1 until totalRooms) {
            if (layout[i] == DungeonRooms.REGULAR && layout[i - 1] != DungeonRooms.REGULAR) {
                regularRooms.add(i)
            } else {
                regularRooms.removeLastOrNull()
            }
        }
        val random = Random()

        val roomPlacer = { room: DungeonRooms, numberOfRooms: Int ->
            for (i in 0 until numberOfRooms) {
                val value = random.nextInt(regularRooms.size + 1)
                val roomInd = regularRooms[value]
                regularRooms.removeAt(value)

                layout[roomInd] = room
            }
        }

        roomPlacer(DungeonRooms.TREASURE, treasureRooms - bossRooms)

        return layout
    }

    fun getRoom(roomInd: Int): DungeonRooms {
        return layout[roomInd]
    }
}