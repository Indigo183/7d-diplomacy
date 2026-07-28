package nodomain.seven.dip.api

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import nodomain.seven.dip.game.GameProperty.STARTED
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.game.SignUps
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.Parser.FullNationalisedFormat.VERBOSE_WITH_ANNOUNCED_PLAYER
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.Romans
import nodomain.seven.dip.utils.exceptions.ConflictException
import nodomain.seven.dip.utils.exceptions.ForbiddenException
import nodomain.seven.dip.utils.exceptions.UnauthenticatedException
import nodomain.seven.dip.utils.exceptions.UnprocessableEntryException
import org.eclipse.microprofile.openapi.annotations.Operation
import org.jboss.resteasy.reactive.ResponseStatus
import javax.crypto.SecretKey
import kotlin.enums.enumEntries
import kotlin.random.Random

val LOWERCASE_ALPHANUMERIC_WITH_DASHES = Regex("^[a-z0-9-]+$")
fun requireValidGameId(id: String) {
    if (id.length < 4 || !LOWERCASE_ALPHANUMERIC_WITH_DASHES.matches(id))
        throw UnprocessableEntryException("game id must be a lowercase alphanumerical kebab case string of at least 4 characters")
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource, val key: SecretKey, val gameDAO: GameDAO) {
    @Path("{id}")
    fun game(@PathParam("id") id: String) = gameResource.with(id)

    @GET
    fun getGameNames(): Collection<String> = gameDAO.allGames()

    @POST
    @ResponseStatus(201)
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("id") id: String): String {
        requireValidGameId(id)
        if (gameDAO.existingGame(id))
            throw ConflictException("game with this id already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        val signUps = SignUps(countries = enumEntries<RomanPlayers>())
        gameDAO.createAndSave(id, game, signUps)
        return Jwts.builder()
            .claim("gameId", id)
            .claim("isGM", true)
            .signWith(key)
            .compact()
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource @Inject constructor(
    val ordersResource: OrdersResource,
    val key: SecretKey,
    val tokenParser: JwtParser,
    val gameDAO: GameDAO,
    val gmActions: GMActions,
    val orderDao: OrderDao,
    val tokenAccessDAO: TokenAccessDAO
) {
    lateinit var id: String
    fun with(id: String): GameResource {
        requireValidGameId(id)
        if (!gameDAO.existingGame(id)) throw NotFoundException("no game exists with this id")
        this.id = id
        return this
    }

    @GET
    fun getGame() = gameDAO.load(id)

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun getPlayerToken(
        @QueryParam("country") country: String,
        @QueryParam("recovery-key") recoveryKey: String?
    ): String {
        val signUps = try {
            gameDAO.loadSignUps(id)
        } catch (_: Exception) {
            throw NotFoundException("game sign-up object cannot be located")
        }
        val signedUpCountry = signUps.find(country)
            ?: signUps.signUp(country)
                .also { gameDAO.saveSignUps(id, signUps) }
        orderDao.with(id).createIfNotExists(signedUpCountry.name)
        val token = Jwts.builder()
            .claim("gameId", id)
            .claim("country", signedUpCountry)
            .signWith(key)
            .compact()
        tokenAccessDAO.with(id)
        when (recoveryKey?.length) {
            null if (STARTED !in signUps.properties) -> tokenAccessDAO.logCreateToken(country)
            10 if (token.endsWith(recoveryKey)) -> tokenAccessDAO.logRecoverToken(country)
            else -> throw ForbiddenException("invalid recovery key")
        }
        return token
    }

    @Operation(summary = "GM Action")
    @PATCH
    fun gmAction(
        @HeaderParam("Authorisation") token: String,
        @DefaultValue("adjudicate") @QueryParam("action") action: String,
        @Context uriInfo: UriInfo
    ): Response { // not atomised! not safe! very much not enterprise grade!
        val claims: Map<String, Any> = try {
            tokenParser.parseSignedClaims(token.substringAfter("BEARER ")).payload
        } catch (_: Exception) {
            throw UnauthenticatedException("token couldn't be verified")
        }
        if (claims["gameId"] != id || claims["isGM"] === null || !(claims["isGM"] as Boolean))
            throw ForbiddenException("only the GM of this game may take actions it!")
        return gmActions.getActionByName(action).run(id, uriInfo)
    }

    @Path("{country}")
    fun orders(
        @PathParam("country") country: String,
        @HeaderParam("Authorisation") token: String
    ): OrdersResource {
        val claims: Map<String, Any> = try {
            tokenParser.parseSignedClaims(token.substringAfter("BEARER ")).payload
        } catch (_: Exception) {
            throw UnauthenticatedException("token couldn't be verified")
        }
        if (claims["gameId"] != id)
            throw ForbiddenException("supplied token isn't for this game")
        val player = gameDAO.loadSignUps(id).find(claims["country"]?.toString())
        if (player === null || player.name.lowercase() != country.lowercase())
            throw ForbiddenException("supplied token isn't for this country")
        return ordersResource.with(id, player)
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class OrdersResource @Inject constructor(
    val gameDAO: GameDAO,
    val orderDao: OrderDao,
    val tokenAccessDAO: TokenAccessDAO
) {
    lateinit var id: String
    lateinit var player: Player
    fun with(id: String, player: Player): OrdersResource {
        this.id = id
        orderDao.with(id)
        tokenAccessDAO.with(id)
        this.player = player
        return this
    }

    @Path("token-log")
    @GET
    fun getTokenAccessLog(): TokenAccess =
        tokenAccessDAO.load(player.name)

    @Path("ready")
    @POST
    fun setReady(@QueryParam("ready") ready: Boolean?) =
        gameDAO.saveSignUps(id, gameDAO.loadSignUps(id).also {
            it.players[player] = ready ?: false
        })

    @Path("ready")
    @GET
    fun seeReady(): Boolean? = gameDAO.loadSignUps(id).players[player]

    @GET
    fun getOrders(): List<Inputtable> = orderDao.load(player.name).orders

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    fun postTextOrders(orders: String): List<Inputtable> {
        val parsedOrders: List<Inputtable> = try {
            getParser<RomanPlayers, Romans>()
                .parseOrderSet(
                    orders,
                    VERBOSE_WITH_ANNOUNCED_PLAYER,
                    gameDAO.load(id).gameState
                )[player]
        } catch (e: Exception) {
            throw BadRequestException("incorrect format for the parser", e)
        } ?: listOf()
        orderDao.save(player.name, OrderWriteUp(parsedOrders))
        return parsedOrders
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun postJsonOrders(orders: List<Inputtable>): List<Inputtable> {
        orderDao.save(player.name, OrderWriteUp(orders))
        return orders
    }

}
