package nodomain.seven.dip.game

import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.inject.Inject
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GamesResource @Inject constructor(val gameResource: GameResource) {
    @Path("{name}")
    fun game(@PathParam("name") name: String) = gameResource.with(name)

    @POST
    fun create(@QueryParam("name") name: String) {
        if (GameDAO.existingGame(name)) throw BadRequestException("game by this name already exists")
        // in future this endpoint should also permit the creation of games using a different setup from romans
        val game = Game()
        GameDAO.storeGame(name, game)
    }
}

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
class GameResource {
    lateinit var name: String
    fun with(name: String): GameResource {
        this.name = name
        return this
    }

    @GET
    fun getGame() = GameDAO.loadGame(name)
}
