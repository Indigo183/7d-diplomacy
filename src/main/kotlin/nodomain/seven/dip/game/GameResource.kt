package nodomain.seven.dip.game

import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.core.MediaType

object GameDAO {
    var currentGame = Game()
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GameResource {
    @GET
    fun currentGame() = GameDAO.currentGame
}