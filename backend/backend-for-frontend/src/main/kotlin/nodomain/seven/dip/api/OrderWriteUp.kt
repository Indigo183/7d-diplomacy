package nodomain.seven.dip.api

import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.utils.exceptions.UnprocessableEntryException
import java.io.Serializable

@JvmInline
value class OrderWriteUp(val orders: List<Inputtable>): Serializable

data class SignUps(
    val players: MutableMap<Player, Boolean> = mutableMapOf(),
    val countries: List<Player>,
    val properties: MutableSet<GameProperty> = mutableSetOf()
): Serializable {
    fun signUp(country: String): Player {
        val player = countries.find { it.name.equals(country, ignoreCase = true) }
            ?: throw UnprocessableEntryException("Country $country doesn't exist for this game")
        players.putIfAbsent(player, false)
        return player
    }
    fun find(country: String?): Player? = players.keys.find { it.name.equals(country, ignoreCase = true) }
}

interface GameProperty: Serializable {
    companion object {
        fun fromString(string: String): GameProperty = when(string) {
            "started" -> STARTED
            "ended" -> ENDED
            else -> object : Flag(string) {} // custom flag
        }
    }

    abstract class Flag(val name: String): GameProperty {
        protected fun readResolve(): Any = fromString(name)
        override fun toString(): String = name
    }

    object STARTED: Flag("started")

    object ENDED: Flag("ended")
}
