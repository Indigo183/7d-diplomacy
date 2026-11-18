package nodomain.seven.dip.provinces

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.utils.*
import java.io.Serializable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.enums.enumEntries

@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION,
    defaultImpl = Romans::class
)
@JsonSubTypes(JsonSubTypes.Type(Romans::class))
interface ProvincialSpot: Serializable {
    infix fun isAdjacent(other: ProvincialSpot): Boolean

    infix fun isAdjacentFor(other: Piece): Boolean {
        return when (other) {
            is Army -> other.spot.provincialSpot.isAdjacentForArmies(this)
            is Fleet -> other.spot.provincialSpot.isAdjacentForFleets(this)
        }
    }

    infix fun isAdjacentForFleets(other: ProvincialSpot): Boolean {
        return when (other) {
            is InLand -> false
            else -> isAdjacent(other)
        }
    }

    infix fun isAdjacentForArmies(other: ProvincialSpot): Boolean {
        return when (other) {
            is Sea -> false
            else -> isAdjacent(other)
        }
    }

    val province: Province
    val name: String
}

interface Province {
    val name: String
    val isSupplyCentre: Boolean;
}

interface InLand: ProvincialSpot {
    override fun isAdjacentForFleets(other: ProvincialSpot): Boolean = false
}

interface Coast: ProvincialSpot {
    fun hasInlandBorderWith(coast: Coast): Boolean

    override fun isAdjacentForFleets(other: ProvincialSpot): Boolean {
        if (other is Coast && hasInlandBorderWith(other)) return false
        return super.isAdjacentForFleets(other)
    }
}

interface Sea: ProvincialSpot {
    override fun isAdjacentForArmies(other: ProvincialSpot): Boolean = false
}

interface Provinces<T: ProvincialSpot> {
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


interface Player: Serializable {
	val homeCentres: List<Province>;
    val startingUnits: List<Piece>

    val name: String;
}

data class Owned<out T>(val order: T, val player: Player) {
    fun toPair() : Pair<T, Player> = order to player
}
fun <T> Player.having(order: T) = Owned<T>(order, this)

inline fun <reified T> setup(): Map<Piece, Player> where T : Player, T : Enum<T> =
    enumEntries<T>().asSequence().flatMap { player -> player.startingUnits.map { it to player } }.toMap()
