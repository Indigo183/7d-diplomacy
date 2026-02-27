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
    val origin = T(0.c , 0)
    val game = Game()
    
    game.input(listOf(
        origin A CAT M BRU i 2,
        origin A POM M BRU i 1,
    ))
    game.adjudicate()

    for (board in game.timeplanes.flatMap { it.boards() }) testBoard(board)
}

fun testBoard(board: Board) {
    println("This is, in fact, a Board!\n$board\n\n")
}
