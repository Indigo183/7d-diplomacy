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

fun preventReservedTerms(id: String) {
    when(id) {
        "security" -> throw BadRequestException("reserved term may not be used as game id")
    }
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource) {
    @Path("{id}")
    fun game(@PathParam("id") id: String,
             @HeaderParam("UserName") userName: String?,
             @HeaderParam("Password") password: String?) =
        gameResource.with(id, userName, password)

    @PUT
    fun createAccount(@HeaderParam("UserName") userName: String,
                      @HeaderParam("Password") password: String) {}

    @GET
    fun getGameNames(user: User): Set<String> = user.orders.keys

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("id") id: String, @HeaderParam("UserName") userName: String,
                   @HeaderParam("Password") password: String): String {
        preventReservedTerms(id)
        if (GameDAO.existingGame(id)) throw BadRequestException("game with this id already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        val signUps = SignUps(gm = User(userName, password), countries = enumEntries<RomanPlayers>())
        GameDAO.storeGame(id, game, signUps)
        return ""
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource @Inject constructor(val ordersResource: OrdersResource) {
    lateinit var user: User
    lateinit var id: String
    fun with(id: String, userName: String?, password: String?): GameResource {
        preventReservedTerms(id)
        this.id = id
        if (userName != null && password != null)
            this.user = User(userName, password)
        return this
    }

    @GET
    fun getGame() = try {
        GameDAO.loadGame(id)
    } catch (_ : Exception) {
        throw NotFoundException("no game exists with this id")
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun signUp(@QueryParam("country") country: String): String =
        GameDAO.loadSignUps(id).signUp(user, country).name

    @PATCH
    @Produces(MediaType.TEXT_PLAIN)
    fun adjudicate(): String {
        return "Adjudication not yet implemented"
    }

    @Path("{country}")
    fun orders(@PathParam("country") country: String): OrdersResource {
        return ordersResource.with(name, user)
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class OrdersResource {
    lateinit var user: User
    lateinit var id: String
    fun with(id: String, user: User): OrdersResource {
        preventReservedTerms(id)
        this.id = id
        this.user = user
        return this
    }


    @GET
    fun getOrders(@PathParam("country") country: String): List<Order> =
        user.orders[id] ?: listOf()

    @POST
    fun postOrders(@PathParam("country") country: String, orders: String): List<Order> = listOf()

}
