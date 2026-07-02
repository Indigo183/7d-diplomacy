package nodomain.seven.dip.api
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.game.Game

import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.inject.Inject
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.PUT
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.provinces.RomanPlayers
import kotlin.enums.enumEntries

fun preventReservedTerms(name: String) {
    when(name) {
        "security" -> throw BadRequestException("reserved term may not be used as game name")
    }
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource) {
    @Path("{name}")
    fun game(@PathParam("name") name: String,
             @HeaderParam("UserName") userName: String?,
             @HeaderParam("Password") password: String?) =
        gameResource.with(name, userName, password)

    @PUT
    fun createAccount(user: User) {}

    @GET
    fun getGameNames(user: User): Set<String> = user.orders.keys

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("name") name: String, user: User): String {
        preventReservedTerms(name)
        if (GameDAO.existingGame(name)) throw BadRequestException("game by this name already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        val signUps = SignUps(gm = user, countries = enumEntries<RomanPlayers>())
        GameDAO.storeGame(name, game, signUps)
        return ""
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource {
    lateinit var user: User
    lateinit var name: String
    fun with(name: String, userName: String?, password: String?): GameResource {
        preventReservedTerms(name)
        this.name = name
        if (userName != null && password != null)
            this.user = User(userName, password)
        return this
    }

    @GET
    fun getGame() = try {
        GameDAO.loadGame(name)
    } catch (_ : Exception) {
        throw NotFoundException("no game exists by that name")
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun signUp(@QueryParam("country") country: String): String =
        GameDAO.loadSignUps(name).signUp(user, country).name

    @PATCH
    @Produces(MediaType.TEXT_PLAIN)
    fun adjudicate(): String {
        return "Adjudication not yet implemented"
    }

    @Path("{country}")
    @GET
    fun getOrders(@PathParam("country") country: String): List<Order> =
        user.orders[name] ?: listOf()

    @Path("{country}")
    @POST
    fun postOrders(@PathParam("country") country: String, orders: String): List<Order> = listOf()
}
