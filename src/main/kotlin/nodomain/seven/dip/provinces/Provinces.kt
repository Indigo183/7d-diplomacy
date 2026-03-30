package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.Army
import nodomain.seven.dip.orders.Fleet
import nodomain.seven.dip.orders.PartiallyParsed
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.enums.enumEntries

interface Province {
    infix fun isAdjacent(other: Province): Boolean

    infix fun isAdjacentTo(other: Province): Adjacency {
        return Adjacency { when (it) {
            is Army -> isAdjacentForArmies(other)
            is Fleet -> isAdjacentForFleets(other)
            null -> isAdjacent(other) }
        }
    }

    infix fun isAdjacentForFleets(other: Province): Boolean {
        return when (other) {
            is InLand -> false
            else -> isAdjacent(other)
        }
    }

    infix fun isAdjacentForArmies(other: Province): Boolean {
        return when (other) {
            is Sea -> false
            else -> isAdjacent(other)
        }
    }

    val isSupplyCentre: Boolean;
    val name: String
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

interface Provinces {
    val entries: List<Province>

    fun valueOf(string: String): Province =
        entries.first { it.name == string }

    val nonTrivialNames: Map<String, PartiallyParsed<Province>>

    fun asProvince(string: String): PartiallyParsed<Province> {
        return nonTrivialNames.asSequence()
            .filter { (name, _) -> string.startsWith(name, ignoreCase = true) }
            .map { it.value }
            .firstOrNull() ?: PartiallyParsed {valueOf(string.trim().substring(0, 3).uppercase())}
    }
}

class TakeN<R>(val n: Int, val output: R): PartiallyParsed<R> {
    var counter = 0
    override fun isComplete(): Boolean = counter >= n
    override fun feed(string: String) { counter++ }
    override fun provideComplete(): R = output
}

interface Player {
	val homeCentres: List<Province>;
}

inline fun <reified T> setup(): Map<Province, Player> where T : Player, T : Enum<T> =
    enumEntries<T>().asSequence().flatMap { player -> player.homeCentres.map { it to player } }.toMap()
