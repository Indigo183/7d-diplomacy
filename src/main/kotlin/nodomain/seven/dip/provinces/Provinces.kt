package nodomain.seven.dip.provinces

import kotlin.enums.enumEntries

interface Province {
    infix fun isAdjacentTo(other: Province): Boolean;
    val isSupplyCenter: Boolean;
}

interface Player {
	// TODO: shoul this be a List or a Set?
	val homeCentres: List<Province>;
}

inline fun <reified T> setup(): Map<Province, Player> where T : Player, T : Enum<T> =
    enumEntries<T>().flatMap { player -> player.homeCentres.map { it to player } }.toMap()
