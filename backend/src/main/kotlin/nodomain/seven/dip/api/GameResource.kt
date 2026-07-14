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
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.PUT
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.Parser
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.Romans
import nodomain.seven.dip.utils.exceptions.ConflictException
import nodomain.seven.dip.utils.exceptions.UnprocessableEntryException
import kotlin.enums.enumEntries

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
        if (userName === null || userName.length < 4 || ! Regex("^[A-Za-z0-9-]+$").matches(userName))
            throw UnprocessableEntryException("UserName must be an alphanumerical kabab case string of at least 4 characters")
        if (password === null || password.length < 8 || ! Regex("^[A-Za-z0-9-]+$").matches(password))
            throw UnprocessableEntryException("Password must be an alphanumerical kabab case string of at least 8 characters")
        UserDao.signUp(User(userName, password))
    }

    @GET //DO NOT USE YET. PROMISE FOR THE FUTURE
    fun getGameNames(@HeaderParam("UserName") userName: String?,
                     @HeaderParam("Password") password: String?): Collection<String> {
        if (userName !== null)
            return UserDao.login(User(userName, password!!)).inputs.keys
        return GameDAO.allGames()
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("id") id: String, @HeaderParam("UserName") userName: String,
                   @HeaderParam("Password") password: String): String {
        if (GameDAO.existingGame(id)) throw ConflictException("game with this id already exists")
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
        if (!GameDAO.existingGame(id)) throw NotFoundException("no game exists with this id")
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
        val signUps = try {
            GameDAO.loadSignUps(id)
        } catch (_: Exception) {
            throw NotFoundException("Game sign-up object cannot be located")
        }
        val signedUpCountry = signUps.signUp(UserDao.login(user), country)
        GameDAO.saveSignUps(id, signUps)
        return signedUpCountry.name
    }

    @PATCH
    fun adjudicate(): Game { // not atomized! not safe! very much not enterprise grade!
        val signUps = try {
            GameDAO.loadSignUps(id)
        } catch (_: Exception) {
            throw NotFoundException("Game sign-up object cannot be located")
        }
        if (signUps.gm.name != UserDao.login(user).name)
            throw UnauthorizedException("Only the GM of this game may adjudicate it!")
        val allPlayersReady = signUps.players.keys.asSequence()
            .map(UserDao::getUser)
            .all { it.inputs[id]?.ready ?: false }
        if (! allPlayersReady)
            throw ConflictException("Not all players have readied up")
        val game = GameDAO.loadGame(id)
        signUps.players.forEach { (userName, country) ->
            val orderingUser = UserDao.getUser(userName)
            game.input(orderingUser.inputs[id]?.orders ?: listOf(), country)
            orderingUser.inputs[id] = OrderWriteUp(listOf())
            UserDao.saveData(orderingUser)
        }
        game.adjudicate()
        GameDAO.saveGame(id, game)
        return game
    }

    @Path("{country}")
    fun orders(@PathParam("country") country: String): OrdersResource = ordersResource.with(id, user, country)
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class OrdersResource {
    lateinit var user: User
    lateinit var id: String
    lateinit var player: Player
    fun with(id: String, user: User, country: String): OrdersResource {
        this.id = id
        this.user = UserDao.login(user)
        this.player = GameDAO.loadSignUps(id).players[user.name]
            ?: throw UnauthorizedException("Not signed up as $country in $id")
        return this
    }

    @Path("ready")
    @POST
    fun setReady(@QueryParam("ready") ready: Boolean?) {
        user.inputs[id] = user.inputs[id]?.ready(ready ?: false) ?: OrderWriteUp(listOf(), ready ?: false)
        UserDao.saveData(user)
    }

    @Path("ready")
    @GET
    fun seeReady(): Boolean? = user.inputs[id]?.ready

    @GET
    fun getOrders(): List<Inputtable> =
        user.inputs[id]?.orders ?: listOf()

    @POST
    fun postOrders(orders: String): List<Inputtable> {
        val parsedOrders: List<Inputtable> = try {
            getParser<RomanPlayers, Romans>()
                .parseOrderSet(orders, Parser.FullNationalisedFormat.DATC, GameDAO.loadGame(id).gameState)[player]
        } catch (e: Exception) {
            throw UnprocessableEntryException("Incorrect format for the parser", e)
        } ?: listOf()
        user.inputs[id] = OrderWriteUp(parsedOrders)
        UserDao.saveData(user)
        return parsedOrders
    }
}
