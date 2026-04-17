package nodomain.seven.dip.game

import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.core.MediaType
import nodomain.seven.dip.adjudication.*
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.*

fun newTestGame(): Game {
    val origin = T(0.c, 0)

    val game = Game()

    game.input(listOf(
        origin A CAT M BRU i 2,
        origin A POM M BRU i 1,
    ))
    game.adjudicate()

    game.input(listOf(
        T(i, 0) A CAT M Location(CAT, origin) i 2,
        T(i, 0) A BRU M Location(CAT, origin) i 2,

        T(-1.c, 0) A BRU M POM i 1,
        T(-1.c, 0) A POM M Location(POM, origin) i 1,
    ))
    game.adjudicate()
    game.input(listOf(Build(T(-1+i, 0) A CAT)))
    game.adjudicate()

    game.input(listOf(
        T(-1+i, 0) A CAT M Location(CAE, T(i, 0)) i 3,
        T(-1+i, 0) A POM M Location(POM, T(i, 0)) i 3,

        T(i, 1) A CAT S { T(-1+i, 0) A CAT M Location(CAE, T(i, 0)) },
        T(i, 1) A POM M CAE i 3,
        T(i, 1) A BRU S { T(i, 1) A POM M CAE },
    ))
    game.adjudicate()

    return game
}

object GameDAO {
    var currentGame = newTestGame()
}

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
class GameResource {
    @GET
    fun currentGame() = GameDAO.currentGame
}