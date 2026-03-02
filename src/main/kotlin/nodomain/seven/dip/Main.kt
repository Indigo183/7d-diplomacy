package nodomain.seven.dip

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.game.Board
import nodomain.seven.dip.adjudication.*
import nodomain.seven.dip.game.boards
import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.RomanPlayers.*
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.*
import kotlin.collections.flatMap

fun main() {
    val origin = T(0.c, 0)
    val game = Game()
    
    game.input(listOf(
        origin A CAT M BRU i 2,
        origin A POM M BRU i 1,
    ))
    game.adjudicate()
    for (board in game.timeplanes.flatMap { it.boards() }) testBoard(board)

    game.input(listOf(
        T(i, 0) A CAT M Location(CAT, origin) i 2,
        T(i, 0) A BRU M Location(CAT, origin) i 2,

        T(-1.c, 0) A BRU M POM i 1,
        T(-1.c, 0) A POM M Location(POM, origin) i 1,
    ))
    game.adjudicate()
    for (board in game.timeplanes.flatMap { it.boards() }) testBoard(board)
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}
