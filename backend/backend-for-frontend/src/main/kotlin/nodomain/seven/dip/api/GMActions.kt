package nodomain.seven.dip.api

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.utils.exceptions.ConflictException
import kotlin.collections.set

fun interface GMAction {
    fun run(gameId: String, context: UriInfo): Response
}

fun getActionByName(name: String): GMAction = when (name) {
    "adjudication" -> adjudicate
    else -> throw BadRequestException("The action $name is not recognised")
}

val adjudicate = GMAction { id, _ ->
    val signUps = GameDAO.loadSignUps(id)
    val game = GameDAO.loadGame(id)
    if (signUps.players.size != signUps.countries.size || !signUps.players.values.all { it })
        throw ConflictException("Not all players have readied up")
    val orderDao = OrderDao(id)
    signUps.players.keys.forEach {
        game.input(orderDao.load(it.name).orders)
        orderDao.save(it.name, OrderWriteUp(listOf()))
        signUps.players[it] = false
    }
    game.adjudicate()
    GameDAO.saveSignUps(id, signUps)
    GameDAO.saveGame(id, game)
    Response.status(200).entity(game).build()
}