package nodomain.seven.dip.api

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
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
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.Parser
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.RomanPlayers
import nodomain.seven.dip.provinces.Romans
import nodomain.seven.dip.utils.exceptions.ConflictException
import nodomain.seven.dip.utils.exceptions.ForbiddenException
import nodomain.seven.dip.utils.exceptions.UnauthenticatedException
import nodomain.seven.dip.utils.exceptions.UnprocessableEntryException
import javax.crypto.SecretKey
import kotlin.enums.enumEntries

val ALPHANUMERIC_WITH_DASHES = Regex("^[A-Za-z0-9-]+$")
fun requireValidGameId(id: String) {
    if (! ALPHANUMERIC_WITH_DASHES.matches(id))
        throw UnprocessableEntryException("The game Id must be an alphanumerical kabab case string of at least 4 characters")
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource, val key: SecretKey) {
    @Path("{id}")
    fun game(@PathParam("id") id: String) = gameResource.with(id)

    @GET
    fun getGameNames(): Collection<String> = GameDAO.allGames()

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun createGame(@QueryParam("id") id: String): String {
        requireValidGameId(id)
        if (GameDAO.existingGame(id))
            throw ConflictException("game with this id already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        val signUps = SignUps(countries = enumEntries<RomanPlayers>())
        GameDAO.storeGame(id, game, signUps)
        return Jwts.builder()
            .claim("gameId", id)
            .claim("isGM", true)
            .signWith(key)
            .compact()
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource @Inject constructor(val ordersResource: OrdersResource, val key: SecretKey, val tokenParser: JwtParser) {
    lateinit var id: String
    fun with(id: String): GameResource {
        requireValidGameId(id)
        if (!GameDAO.existingGame(id)) throw NotFoundException("no game exists with this id")
        this.id = id
        return this
    }

    @GET
    fun getGame() = try { GameDAO.loadGame(id) }
        catch (_ : Exception) { throw NotFoundException("no game exists with this id") }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun getPlayerToken(@QueryParam("country") country: String, @QueryParam("recovery-key") recoveryKey: String?): String {
        val signUps = try { GameDAO.loadSignUps(id) }
            catch (_: Exception) { throw NotFoundException("Game sign-up object cannot be located") }
        var signedUpCountry = signUps.find(country)
        if (signedUpCountry === null) {
            signedUpCountry = signUps.signUp(country)
            GameDAO.saveSignUps(id, signUps)
        }
        OrderDao(id).createIfNotExists(signedUpCountry.name)
        val token = Jwts.builder()
            .claim("gameId", id)
            .claim("country", signedUpCountry)
            .signWith(key)
            .compact()
        when (recoveryKey?.length) {
            null, 0                             -> TokenAccess.logCreateToken(id, country)
            10 if (token.endsWith(recoveryKey)) -> TokenAccess.logRecoverToken(id, country)
            else                                -> throw ForbiddenException("Invalid recovery key")
        }
        return token
    }

    @PATCH
    fun adjudicate(@HeaderParam("Authorisation") token: String): Game { // not atomized! not safe! very much not enterprise grade!
        val signUps = try { GameDAO.loadSignUps(id) }
            catch (_: Exception) { throw NotFoundException("Game sign-up object cannot be located") }
        val claims: Map<String, Any> = try { tokenParser.parseSignedClaims(token.substringAfter("BEARER ")).payload }
            catch (_: Exception) { throw UnauthenticatedException("Your token couldn't be verified") }
        if (claims["gameId"] != id || claims["isGM"] === null || !(claims["isGM"] as Boolean))
            throw ForbiddenException("Only the GM of this game may adjudicate it!")
        if (signUps.players.size != signUps.countries.size || !signUps.players.values.all { it })
            throw ConflictException("Not all players have readied up")
        val game = GameDAO.loadGame(id)
        val orderDao = OrderDao(id)
        signUps.players.keys.forEach {
            game.input(orderDao.load(it.name).orders)
            orderDao.save(it.name, OrderWriteUp(listOf()))
            signUps.players[it] = false
        }
        game.adjudicate()
        GameDAO.saveSignUps(id, signUps)
        GameDAO.saveGame(id, game)
        return game
    }

    @Path("{country}")
    fun orders(@PathParam("country") country: String, @HeaderParam("Authorisation") token: String): OrdersResource {
        val claims: Map<String, Any> = try {
            tokenParser.parseSignedClaims(token.substringAfter("BEARER ")).payload
        } catch (_: Exception) {
            throw UnauthenticatedException("Your token couldn't be verified")
        }
        if (claims["gameId"] != id)
            throw ForbiddenException("Supplied token isn't for this game")
        val player = GameDAO.loadSignUps(id).find(claims["country"]?.toString())
        if (player === null ||  player.name.lowercase() != country.lowercase())
            throw ForbiddenException("Supplied token isn't for this country")
        return ordersResource.with(id, player)
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class OrdersResource {
    lateinit var id: String
    lateinit var orderDao: OrderDao
    lateinit var player: Player
    fun with(id: String, player: Player): OrdersResource {
        this.id = id
        this.orderDao = OrderDao(id)
        this.player = player
        return this
    }

    @Path("token-log")
    @GET
    fun getTokenAccessLog(): TokenAccess = TokenAccess.load(countryDataDirectory(id).resolve(player.name))

    @Path("ready")
    @POST
    fun setReady(@QueryParam("ready") ready: Boolean?) =
        GameDAO.saveSignUps(id, GameDAO.loadSignUps(id).also {
            it.players[player]= ready ?: false
        })

    @Path("ready")
    @GET
    fun seeReady(): Boolean? = GameDAO.loadSignUps(id).players[player]

    @GET
    fun getOrders(): List<Inputtable> = orderDao.load(player.name).orders

    @POST
    fun postOrders(orders: String): List<Inputtable> {
        val parsedOrders: List<Inputtable> = try {
            getParser<RomanPlayers, Romans>()
                .parseOrderSet(orders, Parser.FullNationalisedFormat.DATC, GameDAO.loadGame(id).gameState)[player]
        } catch (e: Exception) {
            throw UnprocessableEntryException("Incorrect format for the parser", e)
        } ?: listOf()
        orderDao.save(player.name, OrderWriteUp(parsedOrders))
        return parsedOrders
    }
}
