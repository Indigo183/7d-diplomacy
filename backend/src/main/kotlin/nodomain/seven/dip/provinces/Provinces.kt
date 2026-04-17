package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.utils.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.enums.enumEntries

interface Province {
    infix fun isAdjacent(other: Province): Boolean

    infix fun isAdjacentFor(other: Piece): Boolean {
        return when (other) {
            is Army -> other.location.province.isAdjacentForArmies(this)
            is Fleet -> other.location.province.isAdjacentForFleets(this)
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

    val province: Province get() = this
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

    fun hasCoastalParts() = coastalParts == null

    val coastalParts: List<CoastalPart>? get() = null
}

interface CoastalPart: Coast {
    override val province: Coast

    override val coastalParts: List<CoastalPart>? get() = province.coastalParts
}

interface Sea: Province {
    override fun isAdjacentForArmies(other: Province): Boolean = false
}

interface Provinces<T: Province> {
    val entries: List<T>

    fun valueOf(string: String): T =
        entries.first { it.name == string }

    fun trivialPartialParser(string: String): PartiallyParsed<T> =
        PartiallyParsed {valueOf(string.trim().substring(0, 3).uppercase())}

    val nonTrivialNames: Map<String, () -> PartiallyParsed<T>>

    fun asProvince(string: String): PartiallyParsed<T> {
        return nonTrivialNames.asSequence()
            .filter { (name, _) -> string.startsWith(name, ignoreCase = true) }
            .map { (it.value)() }
            .firstOrNull() ?: trivialPartialParser(string)
    }
}


interface Player {
	val homeCentres: List<Province>;
}

inline fun <reified T> setup(): Map<Piece, Player> where T : Player, T : Enum<T> =
    enumEntries<T>().asSequence().flatMap { player -> player.homeCentres.map { T(0.c, 0) A it to player } }.toMap()
