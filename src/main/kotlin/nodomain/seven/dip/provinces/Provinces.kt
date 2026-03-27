package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Army
import nodomain.seven.dip.orders.Fleet
import kotlin.enums.enumEntries

interface Province {
    infix fun isAdjacentTo(other: Province): Boolean

    infix fun isAdjacentWith(other: Province): Adjacency {
        return Adjacency { when (it) {
            is Army -> isAdjacentForArmies(other)
            is Fleet -> isAdjacentForFleets(other)
            null -> isAdjacentTo(other) }
        }
    }

    infix fun isAdjacentForFleets(other: Province): Boolean {
        return when (other) {
            is InLand -> false
            else -> isAdjacentTo(other)
        }
    }

    infix fun isAdjacentForArmies(other: Province): Boolean {
        return when (other) {
            is Sea -> false
            else -> isAdjacentTo(other)
        }
    }

    val isSupplyCentre: Boolean;
}

interface InLand: Province {
    override fun isAdjacentForFleets(other: Province): Boolean = false
}

interface Coast: Province {
    fun hasInlandBorderWith(coast: Coast): Boolean

    override fun isAdjacentForFleets(other: Province): Boolean {
        if (other is Coast && hasInlandBorderWith(other)) return false
        return super.isAdjacentForFleets(other)
    }
}

interface Sea: Province {
    override fun isAdjacentForArmies(other: Province): Boolean = false
}

interface Player {
	val homeCentres: List<Province>;
}

inline fun <reified T> setup(): Map<Province, Player> where T : Player, T : Enum<T> =
    enumEntries<T>().asSequence().flatMap { player -> player.homeCentres.map { it to player } }.toMap()
