package nodomain.seven.dip.api

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.ws.rs.BadRequestException
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.provinces.Player
import java.io.Serializable

data class User(val name: String, val password: String,
                @field:JsonIgnore val orders: MutableMap<String, List<Inputtable>> = mutableMapOf()) : Serializable {
    override fun equals(other: Any?): Boolean {
        return other is User && other.name == name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (orders.hashCode())
        return result
    }
}

data class SignUps(val gm: User, val players: MutableMap<String, Player> = mutableMapOf(), val countries: List<Player>): Serializable {
    fun signUp(user: User, country: String): Player {
        if (players.containsKey(user.name))
            throw BadRequestException("User ${user.name} already signed up for this game")
        if (players.values.any { it.name.equals(country, ignoreCase = true) })
            throw BadRequestException("Country $country has already been signed up for")
        players.putIfAbsent(user.name,
            countries.find { it.name.equals(country, ignoreCase = true) }
                ?: throw BadRequestException("Country $country doesn't exist for this game"))
        return players[user.name] ?: throw IllegalStateException()
    }
}
