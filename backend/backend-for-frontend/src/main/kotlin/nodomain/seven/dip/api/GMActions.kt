package nodomain.seven.dip.api

import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.game.GameProperty
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.utils.exceptions.ConflictException
import kotlin.collections.set

fun interface GMAction {
    fun run(gameId: String, context: UriInfo): Response
}

@Dependent
class GMActions @Inject constructor(val gameDAO: GameDAO, val orderDao: OrderDao) {
    fun getActionByName(name: String): GMAction = when (name) {
        "adjudicate" -> adjudicate
        "set-property" -> setProperty
        else -> throw BadRequestException("the action $name is not recognised")
    }

    val adjudicate = GMAction { id, _ ->
        val signUps = gameDAO.loadSignUps(id)
        val game = gameDAO.load(id)
        if (!signUps.properties.contains(GameProperty.STARTED))
            throw ConflictException("a game may only be adjudicated once it has started")
        if (signUps.players.size != signUps.countries.size || !signUps.players.values.all { it })
            throw ConflictException("not all players have readied up")
        val orderDao = orderDao.with(id)
        signUps.players.keys.forEach {
            game.input(orderDao.load(it.name).orders)
            orderDao.save(it.name, OrderWriteUp(listOf()))
            signUps.players[it] = false
        }
        game.adjudicate()
        gameDAO.saveSignUps(id, signUps)
        gameDAO.save(id, game)
        Response.status(200).entity(game).build()
    }

    val setProperty = GMAction { id, context ->
        val signUps = gameDAO.loadSignUps(id)
        val propertyToSet = context.queryParameters["property"]?.first()
        if (propertyToSet === null)
            throw BadRequestException("action set-property requires a property to be set")
        signUps.properties.add(GameProperty.fromString(propertyToSet))
        gameDAO.saveSignUps(id, signUps)
        Response.status(200).entity(signUps.properties.toString()).build()
    }
}