package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.*
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

object IntegrationTest {
    val origin = T(0.c, 0)

    @Test
    fun bouncedAndUnbouncedWheels() {
        val game = Game()

        // Setup
        game.input(listOf(
            origin A CAT M CAE i 1,
            origin A POM M CAE i 3,
        ))
        game.adjudicate()

        game.input(listOf(
            T(i, 0) A CAE M POM i 2,
            T(i, 0) A POM M origin[CAE] i 2,

            T(-i, 0) A CAT M CAE i 3,
            T(-i, 0) A CAE M BRU i 2,
        ))
        game.adjudicate()

        game.inputBuilds(listOf(Build(T(-1+i, 0) A CAT)))
        game.adjudicate()

        // Wheel
        game.input(listOf(
            T(-1+i, 0) A CAT M T(-1.c, 0)[CAE] i 2,
            T(-1+i, 0) A POM M T(-1.c, 0)[POM] i 2,

            T(-1.c, 0) A CAT M T(-1+i, 0)[CAT] i 2,
            T(-1.c, 0) A CAE M T(-1+i, 0)[POM] i 2,
            T(-1.c, 0) A POM M T(-1-i, 0)[BRU] i 2,

            T(-1-i, 0) A CAT M T(-1.c, 0)[CAT] i 2,
            T(-1-i, 0) A BRU M CAT i 2,
        ))
        game.adjudicate()
        for (board in game.timeplanes.flatMap { it.boards() }) println(board)

        println(game.gameState)
    }
}
