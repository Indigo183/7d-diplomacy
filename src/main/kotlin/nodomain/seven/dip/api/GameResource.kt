package nodomain.seven.dip.api
import io.quarkus.security.UnauthorizedException
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
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.GameState
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.Parser
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.Romans
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
    fun createAccount(@HeaderParam("UserName") userName: String?,
                      @HeaderParam("Password") password: String?) {
//        if (! Regex("^(?=.{4,}$)[a-z0-9]+(?:-[a-z0-9]+)*$").matches(userName ?: ""))
//            throw BadRequestException("User name must be an alphanumerical kabab case string of at least 4 characters")
//        if (! Regex("^(?=.{8,}$)[a-z0-9]+(?:-[a-z0-9]+)*$").matches(userName ?: ""))
//            throw BadRequestException("Password must be an alphanumerical kabab case string of at least 8 characters")
        UserDao.signUp(User(userName!!, password!!))
    }

    @GET //DO NOT USE YET. PROMISE FOR THE FUTURE
    fun getGameNames(user: User): Set<String> = UserDao.login(user).orders.keys

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("id") id: String, @HeaderParam("UserName") userName: String,
                   @HeaderParam("Password") password: String): String {
        preventReservedTerms(id)
        if (GameDAO.existingGame(id)) throw BadRequestException("game with this id already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        val signUps = SignUps(gm = UserDao.login(User(userName, password)), countries = enumEntries<RomanPlayers>())
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
    fun signUp(@QueryParam("country") country: String): String {
        val signUps = GameDAO.loadSignUps(id)
        val signedUpCountry = signUps.signUp(UserDao.login(user), country)
        GameDAO.saveSignUps(id, signUps)
        return signedUpCountry.name
    }

    @PATCH
    fun adjudicate(): Game { // not atomized! not safe! very much not enterprise grade!
        val signUps = GameDAO.loadSignUps(id)
        if (signUps.gm.name != UserDao.login(user).name)
            throw UnauthorizedException("Only the GM of this game may adjudicate it!")
        val game = GameDAO.loadGame(id)
        signUps.players.forEach { (userName, country) ->
            val orderingUser = UserDao.getUser(userName)
            game.input(orderingUser.orders[id] ?: listOf(), country)
            orderingUser.orders[id] = listOf()
            UserDao.saveData(orderingUser)
        }
        game.adjudicate()
        GameDAO.saveGame(id, game)
        return game
    }

    @Path("{country}")
    fun orders(@PathParam("country") country: String): OrdersResource {
        return ordersResource.with(id, user)
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
        this.user = UserDao.login(user)
        return this
    }


    @GET
    fun getOrders(@PathParam("country") country: String): List<Inputtable> =
        user.orders[id] ?: listOf()

    @POST
    fun postOrders(@PathParam("country") country: String, orders: String): List<Inputtable> {
        val gameState = GameDAO.loadGame(id).gameState
        val player = GameDAO.loadSignUps(id).players[user.name]
            ?: throw UnauthorizedException("Not signed up as $country in $id")
        val parsedOrders: List<Inputtable> = try {
            getParser<RomanPlayers, Romans>()
                .parseOrderSet(orders, Parser.FullNationalisedFormat.DATC, gameState)[player]
                ?: throw BadRequestException("No orders for $country ware found in your order set")
        } catch (e: Exception) {
            throw BadRequestException("Incorrect format for the parser", e)
        }
        user.orders[id] = parsedOrders
        UserDao.saveData(user)
        return parsedOrders
    }
}
