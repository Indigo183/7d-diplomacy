package nodomain.seven.dip.api

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.ws.rs.BadRequestException
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.utils.exceptions.ConflictException
import nodomain.seven.dip.utils.exceptions.UnprocessableEntryException
import java.io.Serializable

data class User(val name: String, val password: String,
                @field:JsonIgnore val orders: MutableMap<String, List<Inputtable>> = mutableMapOf()) : Serializable {
    override fun equals(other: Any?): Boolean {
        return other is User && other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

data class SignUps(val gm: User, val players: MutableMap<String, Player> = mutableMapOf(), val countries: List<Player>): Serializable {
    fun signUp(user: User, country: String): Player {
        if (players.containsKey(user.name))
            throw BadRequestException("User ${user.name} already signed up for this game")
        if (players.values.any { it.name.equals(country, ignoreCase = true) })
            throw ConflictException("Country $country has already been signed up for")
        players.putIfAbsent(user.name,
            countries.find { it.name.equals(country, ignoreCase = true) }
                ?: throw UnprocessableEntryException("Country $country doesn't exist for this game"))
        return players[user.name] ?: throw IllegalStateException()
    }
}
