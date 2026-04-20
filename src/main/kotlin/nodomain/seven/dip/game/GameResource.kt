package nodomain.seven.dip.game

import io.jsonwebtoken.Jwts
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.inject.Inject
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import javax.crypto.SecretKey

fun preventReservedTerms(name: String) {
    when(name) {
        "security" -> throw BadRequestException("reserved term may not be used as game name")
    }
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource, val key: SecretKey) {
    @Path("{name}")
    fun game(@PathParam("name") name: String) = gameResource.with(name)

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun create(@QueryParam("name") name: String): String {
        preventReservedTerms(name)
        if (GameDAO.existingGame(name)) throw BadRequestException("game by this name already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        GameDAO.storeGame(name, game)
        return Jwts.builder()
            .issuer("7dip")
            .claim("game", name)
            .claim("role", "GM")
            .signWith(key)
            .compact()

    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource {
    lateinit var name: String
    fun with(name: String): GameResource {
        preventReservedTerms(name)
        this.name = name
        return this
    }

    @GET
    fun getGame() = try {
        GameDAO.loadGame(name)
    } catch (_ : Exception) {
        throw NotFoundException("no game exists by that name")
    }
}
